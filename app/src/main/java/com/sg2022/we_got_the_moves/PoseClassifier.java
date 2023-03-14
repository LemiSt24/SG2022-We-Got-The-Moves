package com.sg2022.we_got_the_moves;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.log;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

class PoseSample {
  public double[] embedding;
  public String class_name;

  public PoseSample(double[] emb, String name) {
    embedding = emb;
    class_name = name;
  }
}

class EmbeddingDistance {
  public double distance;
  public int index;

  public EmbeddingDistance(int i, double d) {
    index = i;
    distance = d;
  }
}

class EmbeddingDistanceComparator implements Comparator<EmbeddingDistance> {
  @Override
  public int compare(EmbeddingDistance o1, EmbeddingDistance o2) {
    return Double.compare(o1.distance, o2.distance);
  }
}

class ClassificationResult {
  public NormalizedLandmark[] landmarks;
  public double[] embedding;
}

/*
   Ein kNN-Klassifikator für pose embeddings.
*/
public class PoseClassifier {

  double[] current_embedding;
  private List<ClassificationResult> classification_history;
  private int history_depth = 10; // wie viele Schritte kann man in die Vergangenheit blicken?

  public PoseClassifier() {
    classification_history = new ArrayList<ClassificationResult>();
  }


  public void classify(LandmarkProto.NormalizedLandmarkList landmarks) {
    NormalizedLandmark[] normalized_landmarks = NormalizedLandmark.convertFromMediapipe(landmarks);

    double[] embedding = FullBodyPoseEmbedder.generate_embedding(normalized_landmarks);

    ClassificationResult res = new ClassificationResult();
    res.landmarks = normalized_landmarks;
    res.embedding = embedding;

    classification_history.add(res);

    if (classification_history.size() > history_depth) {
      classification_history.remove(0);
    }
  }

  public NormalizedLandmark normalizeLandmark(String landMark) {
    NormalizedLandmark[] landmarks =
        classification_history.get(classification_history.size() - 1).landmarks;
    NormalizedLandmark normLandmark;
    // mehrere Landmarks in "from"
    if (landMark.contains(",")) {
      String[] spl = landMark.split(",");
      normLandmark = NormalizedLandmark.getAverageByNames(landmarks, spl[0], spl[1]);
    } else {
      normLandmark = landmarks[NormalizedLandmark.landmark_names.indexOf(landMark)];
    }
    return normLandmark;
  }

  public double calcAngleDegrees(double x, double y) {
    double angle = atan2(y, x) * 180 / Math.PI;
    return angle;
  }

  public boolean judge_constraint(Constraint constraint) {

    NormalizedLandmark normFrom1 = normalizeLandmark(constraint.from1);
    NormalizedLandmark normTo1 = normalizeLandmark(constraint.to1);
    NormalizedLandmark normFrom2 = normalizeLandmark(constraint.from2);
    NormalizedLandmark normTo2 = normalizeLandmark(constraint.to2);

    if (constraint.type == Constraint.TYPE.ANGLE) {
      double angle;

      if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.X) {
        angle =
            calcAngleDegrees(normFrom2.z - normTo2.z, normFrom2.y - normTo2.y)
                - calcAngleDegrees(normFrom1.z - normTo1.z, normFrom1.y - normTo1.y);
      } else if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.Y) {
        angle =
            calcAngleDegrees(normFrom2.x - normTo2.x, normFrom2.z - normTo2.z)
                - calcAngleDegrees(normFrom1.x - normTo1.x, normFrom1.z - normTo1.z);
      } else if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.Z) {
        angle =
            calcAngleDegrees(normFrom2.y - normTo2.y, normFrom2.x - normTo2.x)
                - calcAngleDegrees(normFrom1.y - normTo1.y, normFrom1.x - normTo1.x);
      } else {
        // throw error angle geht nicht mit 3 dimensionen
        throw new IllegalArgumentException("only 2 dimensions for angles supported");
      }

      angle = abs(angle);
      Log.println(Log.DEBUG, "test", "constraint: " + constraint.message);
      Log.println(Log.DEBUG, "test", "angle: " + angle);

      double compareAngle = (double) constraint.compareAngle;

      if (constraint.inequalityType == Constraint.INEQUALITY_TYPE.LESS) {
        if (angle < compareAngle - constraint.maxDiff) return false;
      } else if (constraint.inequalityType == Constraint.INEQUALITY_TYPE.GREATER) {
        if (angle > compareAngle + constraint.maxDiff) return false;
      } else {
        if (angle < compareAngle - constraint.maxDiff || angle > compareAngle + constraint.maxDiff)
          return false;
      }
    } else if (constraint.type == Constraint.TYPE.UPRIGHT) {
      return abs(normFrom1.y - normTo1.y) > (abs(normFrom1.x - normTo1.x) + abs(normFrom1.z - normTo1.z)) / 2.0;
    } else if (constraint.type == Constraint.TYPE.FLOOR_DISTANCE){

      double smallest_y = Double.POSITIVE_INFINITY;

      // niedrigsten Punkt in der aktuellen Pose finden
      NormalizedLandmark[] landmarks =
              classification_history.get(classification_history.size() - 1).landmarks;
      for(int i = 0; i < landmarks.length; i++)
      {
        if(landmarks[i].y < smallest_y)
        {
          smallest_y = landmarks[i].y;
        }
      }
      // Vergleiche angegebenen Punkt mit dem kleinsten Y-Wert
      double dist = normFrom1.y - smallest_y;
      double compareAngle = (double) constraint.compareAngle;

      if(constraint.inequalityType == Constraint.INEQUALITY_TYPE.LESS) {
        return dist <= constraint.maxDiff;
      }
      else {
        return dist >= constraint.maxDiff;
      }
    } else {

      if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.X) {
        normFrom1.x = 0;
        normTo1.x = 0;
        normFrom2.x = 0;
        normTo2.x = 0;
      } else if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.Y) {
        normFrom1.y = 0;
        normTo1.y = 0;
        normFrom2.y = 0;
        normTo2.y = 0;
      } else if (constraint.insignificantDimension == Constraint.INSIGNIFICANT_DIMENSION.Z) {
        normFrom1.z = 0;
        normTo1.z = 0;
        normFrom2.z = 0;
        normTo2.z = 0;
      }
      Log.println(Log.DEBUG, "test", "conatraint: " +constraint.message );
      double dist1 = normFrom1.getDistance(normTo1);
      double dist2 = normFrom2.getDistance(normTo2);
     Log.println(Log.DEBUG, "test", "dist1 " + String.valueOf(dist1));
     Log.println(Log.DEBUG, "test", "dist2 " + String.valueOf(dist2));

      if (constraint.inequalityType == Constraint.INEQUALITY_TYPE.LESS) {
        if (dist1 < dist2 * (1 - constraint.maxDiff)) return false;
      } else if (constraint.inequalityType == Constraint.INEQUALITY_TYPE.GREATER) {
        if (dist1 > dist2 * (1 + constraint.maxDiff)) return false;
      } else {
        if (dist1 < dist2 * (1 - constraint.maxDiff) || dist1 > dist2 * (1 + constraint.maxDiff))
          return false;
        // if (rel < 1.0 - constraint.maxDiff || rel > 1.0 + constraint.maxDiff) return false;
      }
    }
    return true;
  }

  public boolean judgeEnterState(ExerciseState exerciseState) {

    NormalizedLandmark start = normalizeLandmark(exerciseState.enterStateLandmarkStart);
    NormalizedLandmark mid = normalizeLandmark(exerciseState.enterStateLandmarkMid);
    NormalizedLandmark end = normalizeLandmark(exerciseState.enterStateLandmarkEnd);

    double angle;

    if (exerciseState.insignificantDimension == ExerciseState.INSIGNIFICANT_DIMENSION.X) {
      angle =
          calcAngleDegrees(end.z - mid.z, end.y - mid.y)
              - calcAngleDegrees(start.z - mid.z, start.y - mid.y);
    } else if (exerciseState.insignificantDimension == ExerciseState.INSIGNIFICANT_DIMENSION.Y) {
      angle =
          calcAngleDegrees(end.x - mid.x, end.z - mid.z)
              - calcAngleDegrees(start.x - mid.x, start.z - mid.z);
    } else {
      angle =
          calcAngleDegrees(end.y - mid.y, end.x - mid.x)
              - calcAngleDegrees(start.y - mid.y, start.x - mid.x);
    }
    angle = abs(angle);
    //Log.println(Log.DEBUG, "test", "stateid: " + exerciseState.id);
    //Log.println(Log.DEBUG, "test", "angle: " + angle);

    if (exerciseState.comparator == ExerciseState.COMPARATOR.LESS) {
      if (angle < exerciseState.compareAngle) return true;
    } else if (exerciseState.comparator == ExerciseState.COMPARATOR.GREATER) {
      if (angle > exerciseState.compareAngle) return true;
    }
    return false;
  }
}
