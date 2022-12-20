package com.sg2022.we_got_the_moves;

public class FullBodyPoseEmbedder {

  private static final int embedding_size =
      20; // Diese Zahl muss, wenn man das Embedding verändert, natürlich angepasst werden

  public static int getEmbedding_size() {
    return embedding_size;
  }

  public static double[] generate_embedding(NormalizedLandmark[] landmarks) {
    return generate_embedding(landmarks, false);
  }

  public static double[] generate_embedding(NormalizedLandmark[] landmarks, Boolean flipped) {
    assert landmarks.length == NormalizedLandmark.landmark_names.size();
    // NormalizedLandmark[] normalized_landmarks =
    // NormalizedLandmark.convertFromMediapipe(landmarks);
    if (flipped) {
      for (NormalizedLandmark landmark : landmarks) {
        landmark.x = -1 * landmark.x;
      }
    }
    return _generate_embedding(landmarks);
  }

  private static double[] _generate_embedding(NormalizedLandmark[] landmarks) {
    // Embedding ist hardcoded, muss also auch von Hand im Python-Script verändert werden
    double[] ret = new double[embedding_size];
    ret[0] = NormalizedLandmark.getDistanceByNames(landmarks, "left_shoulder", "left_wrist");
    ret[1] =
        NormalizedLandmark.getAverageByNames(landmarks, "left_shoulder", "left_wrist")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("left_elbow")]);

    ret[2] = NormalizedLandmark.getDistanceByNames(landmarks, "right_shoulder", "right_wrist");
    ret[3] =
        NormalizedLandmark.getAverageByNames(landmarks, "right_shoulder", "right_wrist")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("right_elbow")]);

    ret[4] = NormalizedLandmark.getDistanceByNames(landmarks, "left_hip", "left_ankle");
    ret[5] =
        NormalizedLandmark.getAverageByNames(landmarks, "left_hip", "left_ankle")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("left_knee")]);

    ret[6] = NormalizedLandmark.getDistanceByNames(landmarks, "right_hip", "right_ankle");
    ret[7] =
        NormalizedLandmark.getAverageByNames(landmarks, "right_hip", "right_ankle")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("right_knee")]);

    // Four joints
    ret[8] = NormalizedLandmark.getDistanceByNames(landmarks, "left_hip", "left_wrist");
    ret[9] = NormalizedLandmark.getDistanceByNames(landmarks, "right_hip", "right_wrist");

    // Five joints
    ret[10] = NormalizedLandmark.getDistanceByNames(landmarks, "left_shoulder", "left_ankle");
    ret[11] = NormalizedLandmark.getDistanceByNames(landmarks, "right_shoulder", "right_ankle");

    ret[12] = NormalizedLandmark.getDistanceByNames(landmarks, "left_hip", "left_wrist");
    ret[13] = NormalizedLandmark.getDistanceByNames(landmarks, "right_hip", "right_wrist");

    // Cross body
    ret[14] = NormalizedLandmark.getDistanceByNames(landmarks, "left_elbow", "right_elbow");
    ret[15] = NormalizedLandmark.getDistanceByNames(landmarks, "left_knee", "right_knee");

    ret[16] = NormalizedLandmark.getDistanceByNames(landmarks, "left_wrist", "right_wrist");
    ret[17] = NormalizedLandmark.getDistanceByNames(landmarks, "left_ankle", "right_ankle");

    // Body bent direction
    ret[18] =
        NormalizedLandmark.getAverageByNames(landmarks, "left_wrist", "left_ankle")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("left_hip")]);
    ret[19] =
        NormalizedLandmark.getAverageByNames(landmarks, "right_wrist", "right_ankle")
            .getDistance(landmarks[NormalizedLandmark.landmark_names.indexOf("right_hip")]);

    return ret;
  }
}
