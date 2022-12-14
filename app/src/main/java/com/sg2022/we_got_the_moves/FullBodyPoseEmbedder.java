package com.sg2022.we_got_the_moves;

import static java.lang.Double.max;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.Arrays;
import java.util.List;

// Quasi eine Struct, damit leichter mit den Landmarks zu rechnen ist.
// Wird vom Normalizer ausgegeben und für alle relevanten Funktionen des Embedders verwendet
class MyNormalizedLandmark
{
    public double x;
    public double y;
    public double z;

    public MyNormalizedLandmark(double _x, double _y, double _z)
    {
        x = _x;
        y = _y;
        z = _z;
    }

    public MyNormalizedLandmark add(MyNormalizedLandmark to)
    {
        double _x = x + to.x;
        double _y = y + to.y;
        double _z = z + to.z;
        return new MyNormalizedLandmark(_x, _y, _z);
    }

    public MyNormalizedLandmark subtract(MyNormalizedLandmark other)
    {
        double _x = x - other.x;
        double _y = y - other.y;
        double _z = z - other.z;
        return new MyNormalizedLandmark(_x, _y, _z);
    }

    public MyNormalizedLandmark multiply(double by)
    {
        double _x = x * by;
        double _y = y * by;
        double _z = z * by;
        return new MyNormalizedLandmark(_x, _y, _z);
    }

    public MyNormalizedLandmark divide(double by)
    {
        return multiply(1. / by);
    }

    public double norm()
    {
        return Math.sqrt(x*x + y*y + z*z);
    }
};

public class FullBodyPoseEmbedder {
    private final static List<String> landmark_names = Arrays.asList(
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

    private static double torso_size_multiplier = 2.5;

    private static int embedding_size = 20; // Diese Zahl muss, wenn man das Embedding verändert, natürlich angepasst werden

    public static int getEmbedding_size()
    {
        return embedding_size;
    }

    public static double[] generate_embedding(LandmarkProto.NormalizedLandmarkList landmarks)
    {
        return generate_embedding(landmarks, false);
    }
    public static double[] generate_embedding(LandmarkProto.NormalizedLandmarkList landmarks, Boolean flipped)
    {
        assert landmarks.getLandmarkCount() == landmark_names.size();
        MyNormalizedLandmark[] normalized_landmarks = normalize_landmarks(convert_from_mediapipe(landmarks));
        if(flipped)
        {
            for (MyNormalizedLandmark landmark : normalized_landmarks)
            {
                landmark.x = -1 * landmark.x;
            }
        }
        return _generate_embedding(normalized_landmarks);
    }

    private static double[] _generate_embedding(MyNormalizedLandmark[] landmarks)
    {
        // Embedding ist hardcoded, muss also auch von Hand im Python-Script verändert werden
        double[] ret = new double[embedding_size];
        ret[0] = get_distance_by_names(landmarks, "left_shoulder", "left_wrist");
        ret[1] = get_distance(
                get_average_by_names(landmarks, "left_shoulder", "left_wrist"),
                landmarks[landmark_names.indexOf("left_elbow")]
        );

        ret[2] = get_distance_by_names(landmarks, "right_shoulder", "right_wrist");
        ret[3] = get_distance(
                get_average_by_names(landmarks, "right_shoulder", "right_wrist"),
                landmarks[landmark_names.indexOf("right_elbow")]
        );

        ret[4] = get_distance_by_names(landmarks, "left_hip", "left_ankle");
        ret[5] = get_distance(
                get_average_by_names(landmarks, "left_hip", "left_ankle"),
                landmarks[landmark_names.indexOf("left_knee")]
        );

        ret[6] = get_distance_by_names(landmarks, "right_hip", "right_ankle");
        ret[7] = get_distance(
                get_average_by_names(landmarks, "right_hip", "right_ankle"),
                landmarks[landmark_names.indexOf("right_knee")]
        );

        // Four joints
        ret[8] = get_distance_by_names(landmarks, "left_hip", "left_wrist");
        ret[9] = get_distance_by_names(landmarks, "right_hip", "right_wrist");

        // Five joints
        ret[10] = get_distance_by_names(landmarks, "left_shoulder", "left_ankle");
        ret[11] = get_distance_by_names(landmarks, "right_shoulder", "right_ankle");

        ret[12] = get_distance_by_names(landmarks, "left_hip", "left_wrist");
        ret[13] = get_distance_by_names(landmarks, "right_hip", "right_wrist");

        // Cross body
        ret[14] = get_distance_by_names(landmarks, "left_elbow", "right_elbow");
        ret[15] = get_distance_by_names(landmarks, "left_knee", "right_knee");

        ret[16] = get_distance_by_names(landmarks, "left_wrist", "right_wrist");
        ret[17] = get_distance_by_names(landmarks, "left_ankle", "right_ankle");

        // Body bent direction
        ret[18] = get_distance(
                get_average_by_names(landmarks, "left_wrist", "left_ankle"),
                landmarks[landmark_names.indexOf("left_hip")]
        );
        ret[19] = get_distance(
                get_average_by_names(landmarks, "right_wrist", "right_ankle"),
                landmarks[landmark_names.indexOf("right_hip")]
        );

        return ret;
    }

    // Konvertierung in MyNormalizedLandmark arrays, da damit leichter zu rechnen ist
    // damit muss diese Funktion logischerweise vor allen anderen ausgeführt werden
    private static MyNormalizedLandmark[] convert_from_mediapipe(LandmarkProto.NormalizedLandmarkList landmarks)
    {
        MyNormalizedLandmark[] converted_list = new MyNormalizedLandmark[landmark_names.size()];
        int index = 0;
        for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
            MyNormalizedLandmark normalized = new MyNormalizedLandmark(landmark.getX(), landmark.getY(), landmark.getZ());
            converted_list[index++] = normalized;
        }
        return converted_list;
    }

    // Normalisierung in diesem Fall bedeutet, dass die Größe des Menschen herausgerechnet wird
    private static MyNormalizedLandmark[] normalize_landmarks(MyNormalizedLandmark[] landmarks)
    {
        MyNormalizedLandmark center = get_center(landmarks);
        // Normalize translation.
        int index = 0;
        for (MyNormalizedLandmark landmark : landmarks)
        {
            landmarks[index++] = landmark.subtract(center);
        }
        // Normalize scale
        double pose_size = get_size(landmarks);
        index = 0;
        for (MyNormalizedLandmark landmark : landmarks)
        {
            // Multiplication by 100 is not required, but makes it easier to debug.
            landmarks[index++] = landmark.divide(pose_size).multiply(100);
        }
        return landmarks;
    }

    private static MyNormalizedLandmark get_center(MyNormalizedLandmark[] landmarks)
    {
        MyNormalizedLandmark left_hip = landmarks[landmark_names.indexOf("left_hip")];
        MyNormalizedLandmark right_hip = landmarks[landmark_names.indexOf("right_hip")];
        return left_hip.add(right_hip).divide(2.);
    }

    private static double get_size(MyNormalizedLandmark[] landmarks)
    {
        MyNormalizedLandmark hip_center = get_center(landmarks);
        MyNormalizedLandmark left_shoulder = landmarks[landmark_names.indexOf("left_shoulder")];
        MyNormalizedLandmark right_shoulder = landmarks[landmark_names.indexOf("right_shoulder")];
        MyNormalizedLandmark shoulder_center = left_shoulder.add(right_shoulder).divide(2.); // neue Variable eigentlich überflüssig, da left_shoulder das nun auch enthält
        double torso_size = shoulder_center.subtract(hip_center).norm();

        double max_dist = 0.0;
        for (MyNormalizedLandmark landmark : landmarks)
        {
            double dist = landmark.subtract(hip_center).norm();
            max_dist = max(dist, max_dist);
        }

        return max(torso_size * torso_size_multiplier, max_dist);
    }

    private static MyNormalizedLandmark get_average_by_names(MyNormalizedLandmark[] landmarks, String name_from, String name_to)
    {
        MyNormalizedLandmark from = landmarks[landmark_names.indexOf(name_from)];
        MyNormalizedLandmark to = landmarks[landmark_names.indexOf(name_to)];
        return from.add(to).divide(2.);
    }

    private static double get_distance_by_names(MyNormalizedLandmark[] landmarks, String name_from, String name_to)
    {
        MyNormalizedLandmark from = landmarks[landmark_names.indexOf(name_from)];
        MyNormalizedLandmark to = landmarks[landmark_names.indexOf(name_to)];
        return get_distance(from, to);
    }

    // maybe todo das hier in die MyNormalizedLandmark-Klasse auslagern (ist ja eigentlich nur Subtraktion)
    private static double get_distance(MyNormalizedLandmark from, MyNormalizedLandmark to)
    {
        double diff_x = to.x - from.x;
        double diff_y = to.y - from.y;
        double diff_z = to.z - from.z;
        return new MyNormalizedLandmark(diff_x, diff_y, diff_z).norm();
    }
}
