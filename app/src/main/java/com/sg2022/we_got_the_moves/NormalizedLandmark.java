package com.sg2022.we_got_the_moves;

import static java.lang.Double.max;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.Arrays;
import java.util.List;

/*
    exerciseState
        - name (pushup_top)
        - exercise (foreign)
        - ordering (0 für erstes, 1 für zweites)

    exerciseState-constraint
        - exerciseStateId
        - constraintId

    constraint
        - id
        - from1 (=left_ankle)
        - to1 (=right_ankle)
        - from2 (left shoulder)
        - to2 (right shoulder)
        - max_diff (rel. abweichung)
        - message_too_large (id in Stringliste)
        - message_too_small (id in StringListe)

        Beispiel Schulterbreit:
        - from1 left_shoulder
        - to1 right_shoulder
        - from2 left_ankle
        - to2 right_ankle
        - max_diff 0.1 (10%)
        - messages müssen dann foreign keys sein
 */

// Quasi eine Struct, damit leichter mit den Landmarks zu rechnen ist.
// Wird vom Normalizer ausgegeben und für alle relevanten Funktionen des Embedders verwendet
public class NormalizedLandmark {
    private final static double torso_size_multiplier = 2.5;

    public final static List<String> landmark_names = Arrays.asList(
            "nose",
            "left_eye_inner", "left_eye", "left_eye_outer",
            "right_eye_inner", "right_eye", "right_eye_outer",
            "left_ear", "right_ear",
            "mouth_left", "mouth_right",
            "left_shoulder", "right_shoulder",
            "left_elbow", "right_elbow",
            "left_wrist", "right_wrist",
            "left_pinky_1", "right_pinky_1",
            "left_index_1", "right_index_1",
            "left_thumb_2", "right_thumb_2",
            "left_hip", "right_hip",
            "left_knee", "right_knee",
            "left_ankle", "right_ankle",
            "left_heel", "right_heel",
            "left_foot_index", "right_foot_index"
    );

    public double x;
    public double y;
    public double z;

    public NormalizedLandmark(double _x, double _y, double _z)
    {
        x = _x;
        y = _y;
        z = _z;
    }

    public NormalizedLandmark add(NormalizedLandmark to)
    {
        double _x = x + to.x;
        double _y = y + to.y;
        double _z = z + to.z;
        return new NormalizedLandmark(_x, _y, _z);
    }

    public NormalizedLandmark subtract(NormalizedLandmark other)
    {
        double _x = x - other.x;
        double _y = y - other.y;
        double _z = z - other.z;
        return new NormalizedLandmark(_x, _y, _z);
    }

    public NormalizedLandmark multiply(double by)
    {
        double _x = x * by;
        double _y = y * by;
        double _z = z * by;
        return new NormalizedLandmark(_x, _y, _z);
    }

    public NormalizedLandmark divide(double by)
    {
        return multiply(1. / by);
    }

    public double norm()
    {
        return Math.sqrt(x*x + y*y + z*z);
    }

    // Konvertierung aus MediaPipe-Pose-Daten
    public static NormalizedLandmark[] convertFromMediapipe(LandmarkProto.NormalizedLandmarkList landmarks)
    {
        NormalizedLandmark[] converted_list = new NormalizedLandmark[landmark_names.size()];
        int index = 0;
        for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
            NormalizedLandmark normalized = new NormalizedLandmark(landmark.getX(), landmark.getY(), landmark.getZ());
            converted_list[index++] = normalized;
        }
        return normalize_landmarks(converted_list);
    }

    // Normalisierung in diesem Fall bedeutet, dass die Größe des Menschen herausgerechnet wird
    private static NormalizedLandmark[] normalize_landmarks(NormalizedLandmark[] landmarks)
    {
        NormalizedLandmark center = getPoseCenter(landmarks);
        // Normalize translation.
        int index = 0;
        for (NormalizedLandmark landmark : landmarks)
        {
            landmarks[index++] = landmark.subtract(center);
        }
        // Normalize scale
        double pose_size = getPoseSize(landmarks);
        index = 0;
        for (NormalizedLandmark landmark : landmarks)
        {
            // Multiplication by 100 is not required, but makes it easier to debug.
            landmarks[index++] = landmark.divide(pose_size).multiply(100);
        }
        return landmarks;
    }

    public double getDistance(NormalizedLandmark to)
    {
        double diff_x = to.x - x;
        double diff_y = to.y - y;
        double diff_z = to.z - z;
        return new NormalizedLandmark(diff_x, diff_y, diff_z).norm();
    }

    // Aus kompletter Pose den Mittelpunkt (Mitte der Hüfte) generieren
    public static NormalizedLandmark getPoseCenter(NormalizedLandmark[] landmarks)
    {
        NormalizedLandmark left_hip = landmarks[landmark_names.indexOf("left_hip")];
        NormalizedLandmark right_hip = landmarks[landmark_names.indexOf("right_hip")];
        return left_hip.add(right_hip).divide(2.);
    }

    public static double getPoseSize(NormalizedLandmark[] landmarks)
    {
        NormalizedLandmark hip_center = getPoseCenter(landmarks);
        NormalizedLandmark left_shoulder = landmarks[landmark_names.indexOf("left_shoulder")];
        NormalizedLandmark right_shoulder = landmarks[landmark_names.indexOf("right_shoulder")];
        NormalizedLandmark shoulder_center = left_shoulder.add(right_shoulder).divide(2.);
        double torso_size = shoulder_center.subtract(hip_center).norm();

        double max_dist = 0.0;
        for (NormalizedLandmark landmark : landmarks)
        {
            double dist = landmark.subtract(hip_center).norm();
            max_dist = max(dist, max_dist);
        }

        return max(torso_size * torso_size_multiplier, max_dist);
    }

    public static NormalizedLandmark getAverageByNames(NormalizedLandmark[] landmarks, String name_from, String name_to)
    {
        NormalizedLandmark from = landmarks[landmark_names.indexOf(name_from)];
        NormalizedLandmark to = landmarks[landmark_names.indexOf(name_to)];
        return from.add(to).divide(2.);
    }

    public static double getDistanceByNames(NormalizedLandmark[] landmarks, String name_from, String name_to)
    {
        NormalizedLandmark from = landmarks[landmark_names.indexOf(name_from)];
        NormalizedLandmark to = landmarks[landmark_names.indexOf(name_to)];
        return from.getDistance(to);
    }
}
