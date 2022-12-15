import os
import argparse
import csv
import tqdm
import numpy as np

import cv2
import mediapipe as mp
mp_pose = mp.solutions.pose

class FullBodyPoseEmbedder(object):
    """Converts 3D pose landmarks into 3D embedding."""

    def __init__(self, torso_size_multiplier=2.5):
        # Multiplier to apply to the torso to get minimal body size.
        self._torso_size_multiplier = torso_size_multiplier

        # Names of the landmarks as they appear in the prediction.
        self._landmark_names = [
                'nose',
                'left_eye_inner', 'left_eye', 'left_eye_outer',
                'right_eye_inner', 'right_eye', 'right_eye_outer',
                'left_ear', 'right_ear',
                'mouth_left', 'mouth_right',
                'left_shoulder', 'right_shoulder',
                'left_elbow', 'right_elbow',
                'left_wrist', 'right_wrist',
                'left_pinky_1', 'right_pinky_1',
                'left_index_1', 'right_index_1',
                'left_thumb_2', 'right_thumb_2',
                'left_hip', 'right_hip',
                'left_knee', 'right_knee',
                'left_ankle', 'right_ankle',
                'left_heel', 'right_heel',
                'left_foot_index', 'right_foot_index',
        ]

    def __call__(self, landmarks):
        """Normalizes pose landmarks and converts to embedding

        Args:
            landmarks - NumPy array with 3D landmarks of shape (N, 3).

        Result:
            Numpy array with pose embedding of shape (M, 3) where `M` is the number of
            pairwise distances defined in `_get_pose_distance_embedding`.
        """
        assert landmarks.shape[0] == len(self._landmark_names), 'Unexpected number of landmarks: {}'.format(landmarks.shape[0])

        # Get pose landmarks.
        landmarks = np.copy(landmarks)

        # Normalize landmarks.
        landmarks = self._normalize_pose_landmarks(landmarks)

        # Get embedding.
        embedding = self._get_pose_distance_embedding(landmarks)

        return embedding

    def _normalize_pose_landmarks(self, landmarks):
        """Normalizes landmarks translation and scale."""
        landmarks = np.copy(landmarks)

        # Normalize translation.
        pose_center = self._get_pose_center(landmarks)
        landmarks -= pose_center

        # Normalize scale.
        pose_size = self._get_pose_size(landmarks, self._torso_size_multiplier)
        landmarks /= pose_size
        # Multiplication by 100 is not required, but makes it easier to debug.
        landmarks *= 100

        return landmarks

    def _get_pose_center(self, landmarks):
        """Calculates pose center as point between hips."""
        left_hip = landmarks[self._landmark_names.index('left_hip')]
        right_hip = landmarks[self._landmark_names.index('right_hip')]
        center = (left_hip + right_hip) * 0.5
        return center

    def _get_pose_size(self, landmarks, torso_size_multiplier):
        """Calculates pose size.

        It is the maximum of two values:
            * Torso size multiplied by `torso_size_multiplier`
            * Maximum distance from pose center to any pose landmark
        """
        # This approach uses only 2D landmarks to compute pose size.
        landmarks = landmarks[:, :2]

        # Hips center.
        left_hip = landmarks[self._landmark_names.index('left_hip')]
        right_hip = landmarks[self._landmark_names.index('right_hip')]
        hips = (left_hip + right_hip) * 0.5

        # Shoulders center.
        left_shoulder = landmarks[self._landmark_names.index('left_shoulder')]
        right_shoulder = landmarks[self._landmark_names.index('right_shoulder')]
        shoulders = (left_shoulder + right_shoulder) * 0.5

        # Torso size as the minimum body size.
        torso_size = np.linalg.norm(shoulders - hips)

        # Max dist to pose center.
        pose_center = self._get_pose_center(landmarks)
        max_dist = np.max(np.linalg.norm(landmarks - pose_center, axis=1))

        return max(torso_size * torso_size_multiplier, max_dist)

    def _get_pose_distance_embedding(self, landmarks):
        """Converts pose landmarks into 3D embedding.

        We use several pairwise 3D distances to form pose embedding. All distances
        include X and Y components with sign. We differnt types of pairs to cover
        different pose classes. Feel free to remove some or add new.

        Args:
            landmarks - NumPy array with 3D landmarks of shape (N, 3).

        Result:
            Numpy array with pose embedding of shape (M, 3) where `M` is the number of
            pairwise distances.
        """
        embedding = np.array([

                # Two joints.
                # can be used to determine whether arms / legs are straight
                self._get_distance_by_names(landmarks, 'left_shoulder', 'left_wrist'),
                self._get_distance(
                    self._get_average_by_names(landmarks, 'left_shoulder', 'left_wrist'),
                    landmarks[self._landmark_names.index('left_elbow')]),

                self._get_distance_by_names(landmarks, 'right_shoulder', 'right_wrist'),
                self._get_distance(
                    self._get_average_by_names(landmarks, 'right_shoulder', 'right_wrist'),
                    landmarks[self._landmark_names.index('right_elbow')]),

                self._get_distance_by_names(landmarks, 'left_hip', 'left_ankle'),
                self._get_distance(
                    self._get_average_by_names(landmarks, 'left_hip', 'left_ankle'),
                    landmarks[self._landmark_names.index('left_knee')]),

                self._get_distance_by_names(landmarks, 'right_hip', 'right_ankle'),
                self._get_distance(
                    self._get_average_by_names(landmarks, 'right_hip', 'right_ankle'),
                    landmarks[self._landmark_names.index('right_knee')]),

                # Four joints.

                self._get_distance_by_names(landmarks, 'left_hip', 'left_wrist'),
                self._get_distance_by_names(landmarks, 'right_hip', 'right_wrist'),

                # Five joints.

                self._get_distance_by_names(landmarks, 'left_shoulder', 'left_ankle'),
                self._get_distance_by_names(landmarks, 'right_shoulder', 'right_ankle'),

                self._get_distance_by_names(landmarks, 'left_hip', 'left_wrist'),
                self._get_distance_by_names(landmarks, 'right_hip', 'right_wrist'),

                # Cross body.

                self._get_distance_by_names(landmarks, 'left_elbow', 'right_elbow'),
                self._get_distance_by_names(landmarks, 'left_knee', 'right_knee'),

                self._get_distance_by_names(landmarks, 'left_wrist', 'right_wrist'),
                self._get_distance_by_names(landmarks, 'left_ankle', 'right_ankle'),

                # Body bent direction.

                self._get_distance(
                    self._get_average_by_names(landmarks, 'left_wrist', 'left_ankle'),
                    landmarks[self._landmark_names.index('left_hip')]),
                self._get_distance(
                    self._get_average_by_names(landmarks, 'right_wrist', 'right_ankle'),
                    landmarks[self._landmark_names.index('right_hip')]),
        ])

        return embedding

    def _get_average_by_names(self, landmarks, name_from, name_to):
        lmk_from = landmarks[self._landmark_names.index(name_from)]
        lmk_to = landmarks[self._landmark_names.index(name_to)]
        return (lmk_from + lmk_to) * 0.5

    def _get_distance_by_names(self, landmarks, name_from, name_to):
        lmk_from = landmarks[self._landmark_names.index(name_from)]
        lmk_to = landmarks[self._landmark_names.index(name_to)]
        return self._get_distance(lmk_from, lmk_to)

    # hier verändert vom Tutorial:
    # anstatt einen to->from-Vektor zurückzugeben,
    # wird hier tatsächlich ein Abstand berechnet
    def _get_distance(self, lmk_from, lmk_to):
        return np.linalg.norm(lmk_to - lmk_from)


def main():
    parser = argparse.ArgumentParser(description="Tool to extract (selected) pose landmarks from a video in csv format, capable of converting poses to an embedding format before export.")

    parser.add_argument(
        "-i",
        "--input",
        required=True,
        metavar="input",
        help="Path, where dataset is found. Expects example images for each class to be in subfolders (pushups_up, pushups_down, ...)",
    )
    parser.add_argument(
        "-o",
        "--output-file",
        metavar="output file",
        default="out.csv",
        help="Name of the output csv file.",
    )
    args = parser.parse_args()
    input_folder = args.input

    embedder = FullBodyPoseEmbedder()

    with open(args.output_file, 'w') as csv_out_file:
        csv_out_writer = csv.writer(csv_out_file, delimiter=',', quoting=csv.QUOTE_MINIMAL)
        pose_class_names = sorted([n for n in os.listdir(input_folder) if not n.startswith('.')])

        for pose_class_name in pose_class_names:
            print(pose_class_name)
            image_names = sorted([
                n for n in os.listdir(os.path.join(input_folder, pose_class_name))
                if not n.startswith('.')])
            for image_name in tqdm.tqdm(image_names, position=0):
                input_frame = cv2.imread(os.path.join(input_folder, pose_class_name, image_name))
                input_frame = cv2.cvtColor(input_frame, cv2.COLOR_BGR2RGB)
                with mp_pose.Pose() as pose_tracker:
                    result = pose_tracker.process(image=input_frame)
                    pose_landmarks = result.pose_landmarks

                if pose_landmarks is not None:
                    assert len(pose_landmarks.landmark) == 33, 'Unexpected number of predicted pose landmarks: {}'.format(len(pose_landmarks.landmark))
                    pose_landmarks = np.array([[lmk.x, lmk.y, lmk.z] for lmk in pose_landmarks.landmark])
                    #pose_landmarks = pose_landmarks.flatten().astype(str).tolist()

                    #csv_out_writer.writerow([image_name, pose_class_name] + pose_landmarks)
                    embedding = embedder(pose_landmarks)
                    embedding = embedding.flatten().astype(str).tolist()
                    csv_out_writer.writerow([image_name, pose_class_name] + embedding)

if __name__=="__main__":
    main()
