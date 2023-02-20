package com.sg2022.we_got_the_moves;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;

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
  public double min_distance;
  public NormalizedLandmark[] landmarks;
  public double[] embedding;
  public Map<String, Integer> result;
}

/*
   Ein kNN-Klassifikator für pose embeddings.
*/
public class PoseClassifier {

  double[] current_embedding;
  private Context context;
  private int top_n_by_max_distance;
  private int top_n_by_mean_distance;
  private String dataset_filename;
  private List<PoseSample> dataset;
  private List<String> class_names;
  private List<ClassificationResult> classification_history;
  private int history_depth = 10; // wie viele Schritte kann man in die Vergangenheit blicken?

  public PoseClassifier(Context myContext, int top_n_max, int top_n_mean, String filename) {
    dataset = new ArrayList<PoseSample>();
    class_names = new ArrayList<String>();

    classification_history = new ArrayList<ClassificationResult>();

    context = myContext;
    top_n_by_max_distance = top_n_max;
    top_n_by_mean_distance = top_n_mean;
    dataset_filename = filename;
    load_pose_samples();
  }

  private void load_pose_samples() {
    InputStreamReader is = null;
    try {
      is = new InputStreamReader(context.getAssets().open(dataset_filename));
    } catch (IOException e) {
      e.printStackTrace();
    }

    BufferedReader reader = new BufferedReader(is);
    String line;
    StringTokenizer st = null;
    try {
      while ((line = reader.readLine()) != null) {
        st = new StringTokenizer(line, ",");
        double[] embedding = new double[FullBodyPoseEmbedder.getEmbedding_size()];

        st.nextToken(); // Starte bei 1, da die ersten Spalte den ursprünglichen Dateinamen enthält
        String class_name = st.nextToken();
        for (int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++) {
          embedding[i] = Float.parseFloat(st.nextToken());
        }
        dataset.add(new PoseSample(embedding, class_name));
        if (!class_names.contains(class_name)) {
          class_names.add(class_name);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, Integer> get_result() {
    return get_result(0);
  }

  // index zählt von "oben", also ist 0 das neuste Ergebnis, 1 das zweitneuste, etc.
  public Map<String, Integer> get_result(int index) {
    if (classification_history.size() > index)
      return classification_history.get(classification_history.size() - 1 - index).result;
    else return new HashMap<>();
  }

  public void classify(LandmarkProto.NormalizedLandmarkList landmarks) {
    NormalizedLandmark[] normalized_landmarks = NormalizedLandmark.convertFromMediapipe(landmarks);

    double[] embedding = FullBodyPoseEmbedder.generate_embedding(normalized_landmarks);
    double[] embedding_flipped =
        FullBodyPoseEmbedder.generate_embedding(normalized_landmarks, true);

    /* Filter by max distance.
     This helps to remove outliers - poses that are almost the same as the
     given one, but has one joint bent into another direction and actually
     represent a different pose class.
    */
    List<EmbeddingDistance> max_dist_heap = new ArrayList<EmbeddingDistance>();
    int index = 0;
    for (PoseSample sample : dataset) {
      double dist_to_embedding = 0.0;
      double dist_to_flipped_embedding = 0.0;
      for (int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++) {
        dist_to_embedding = max(abs(sample.embedding[i] - embedding[i]), dist_to_embedding);
        dist_to_flipped_embedding =
            max(abs(sample.embedding[i] - embedding_flipped[i]), dist_to_flipped_embedding);
      }
      max_dist_heap.add(
          new EmbeddingDistance(index++, min(dist_to_embedding, dist_to_flipped_embedding)));
    }
    max_dist_heap.sort(new EmbeddingDistanceComparator());

    while (max_dist_heap.size() > top_n_by_max_distance) {
      max_dist_heap.remove(max_dist_heap.size() - 1);
    }

    /*
    Filter by mean distance.
    After removing outliers we can find the nearest pose by mean distance.
     */
    List<EmbeddingDistance> mean_dist_heap = new ArrayList<EmbeddingDistance>();
    for (EmbeddingDistance dist : max_dist_heap) {
      PoseSample sample = dataset.get(dist.index);
      double dist_to_embedding = 0.0;
      double dist_to_flipped_embedding = 0.0;
      for (int i = 0; i < FullBodyPoseEmbedder.getEmbedding_size(); i++) {
        dist_to_embedding += abs(sample.embedding[i] - embedding[i]);
        dist_to_flipped_embedding += abs(sample.embedding[i] - embedding_flipped[i]);
      }
      dist_to_embedding /= FullBodyPoseEmbedder.getEmbedding_size();
      dist_to_flipped_embedding /= FullBodyPoseEmbedder.getEmbedding_size();
      mean_dist_heap.add(
          new EmbeddingDistance(dist.index, min(dist_to_embedding, dist_to_flipped_embedding)));
    }
    Collections.sort(mean_dist_heap, new EmbeddingDistanceComparator());

    while (mean_dist_heap.size() > top_n_by_mean_distance) {
      mean_dist_heap.remove(mean_dist_heap.size() - 1);
    }

    Map<String, Integer> ret = new HashMap<String, Integer>();
    for (EmbeddingDistance dist : mean_dist_heap) {
      String class_name = dataset.get(dist.index).class_name;
      if (!ret.containsKey(class_name)) ret.put(class_name, 0);
      ret.put(class_name, ret.get(class_name) + 1);
    }
    // return ret;
    ClassificationResult res = new ClassificationResult();
    res.landmarks = normalized_landmarks;
    res.embedding = embedding;
    res.result = ret;
    res.min_distance = mean_dist_heap.get(0).distance;
    classification_history.add(res);

    if (classification_history.size() > history_depth) {
      classification_history.remove(0);
    }
  }

  // Bewertet die zuletzt übermittelte Pose bezüglich Einschränkungen wie Abstand der Füße etc
  // Rückgabewert ist eine Distanz (z.B. der Unterschied zwischen Fußabstand und Schulterabstand)
  // erwartet (eine Liste von) Strings pro Argument, falls zwei genannt: Landmarks werden gemittelt
  /*  public double get_distance(String from, String to) {
    NormalizedLandmark[] landmarks =
        classification_history.get(classification_history.size() - 1).landmarks;

    NormalizedLandmark lmfrom;
    // mehrere Landmarks in "from"
    if (from.contains(",")) {
      String[] spl = from.split(",");
      lmfrom = NormalizedLandmark.getAverageByNames(landmarks, spl[0], spl[1]);
    } else {
      lmfrom = landmarks[NormalizedLandmark.landmark_names.indexOf(from)];
    }

    NormalizedLandmark lmto;
    // mehrere Landmarks in "from"
    if (to.contains(",")) {
      String[] spl = to.split(",");
      lmto = NormalizedLandmark.getAverageByNames(landmarks, spl[0], spl[1]);
    } else {
      lmto = landmarks[NormalizedLandmark.landmark_names.indexOf(to)];
    }

    return lmfrom.getDistance(lmto);
  }*/

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
        return dist <= compareAngle || dist <= compareAngle * (1.0 - constraint.maxDiff) || dist <= compareAngle * (1.0 + constraint.maxDiff);
      } else if(constraint.inequalityType == Constraint.INEQUALITY_TYPE.EQUAL) {
        return dist == compareAngle || (dist >= compareAngle * (1.0 - constraint.maxDiff) && dist <= compareAngle * (1.0 + constraint.maxDiff));
      } else if(constraint.inequalityType == Constraint.INEQUALITY_TYPE.GREATER) {
        return dist >= compareAngle || dist <= compareAngle * (1.0 - constraint.maxDiff) || dist >= compareAngle * (1.0 + constraint.maxDiff);
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

      double dist1 = normFrom1.getDistance(normTo1);
      double dist2 = normFrom2.getDistance(normTo2);
      Log.println(Log.DEBUG, "test3", "dist1 " + String.valueOf(dist1));
      Log.println(Log.DEBUG, "test3", "dist2 " + String.valueOf(dist2));

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

    if (exerciseState.comparator == ExerciseState.COMPARATOR.LESS) {
      if (angle < exerciseState.compareAngle) return true;
    } else if (exerciseState.comparator == ExerciseState.COMPARATOR.GREATER) {
      if (angle > exerciseState.compareAngle) return true;
    }
    return false;
  }
}
