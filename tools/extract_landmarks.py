import argparse
import csv
import cv2
import mediapipe as mp

mp_pose = mp.solutions.pose

# all landmarks
fieldnames = ['NOSE', 'LEFT_EYE_INNER', 'LEFT_EYE', 'LEFT_EYE_OUTER', 'RIGHT_EYE_INNER',
              'RIGHT_EYE', 'RIGHT_EYE_OUTER', 'LEFT_EAR', 'RIGHT_EAR', 'MOUTH_LEFT',
              'MOUTH_RIGHT', 'LEFT_SHOULDER', 'RIGHT_SHOULDER', 'LEFT_ELBOW', 'RIGHT_ELBOW',
              'LEFT_WRIST', 'RIGHT_WRIST', 'LEFT_PINKY', 'RIGHT_PINKY', 'LEFT_INDEX',
              'RIGHT_INDEX', 'LEFT_THUMB', 'RIGHT_THUMB', 'LEFT_HIP', 'RIGHT_HIP',
              'LEFT_KNEE', 'RIGHT_KNEE', 'LEFT_ANKLE', 'RIGHT_ANKLE', 'LEFT_HEEL',
              'RIGHT_HEEL', 'LEFT_FOOT_INDEX', 'RIGHT_FOOT_INDEX']


def main():
  parser = argparse.ArgumentParser(description="Tool to extract (selected) pose landmarks from a video in csv format.")

  parser.add_argument(
      "-i",
      "--input",
      required=True,
      metavar="input",
      help="Name of the input video file.",
  )
  parser.add_argument(
      "-o",
      "--output-file",
      metavar="output files",
      default="out.csv",
      help="Name of the output csv file.",
  )
  parser.add_argument(
      '-l',
      '--landmarks',
      nargs="*",
      metavar="landmark names",
      default=fieldnames,
      help="List of selected landmarks. Defaults to all landmarks.",
  )

  args = parser.parse_args()
  print(args)
  valid_landmarks = list(set(args.landmarks) & set(fieldnames))
  # We need a video.
  if args.input is None:
    print("No video supplied.")
    return
  with open(args.output_file, 'w', newline='') as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=[landmark for valid in valid_landmarks for landmark in [valid + "_x", valid + "_y", valid + "_z"]])
    writer.writeheader()

    with mp_pose.Pose(
      static_image_mode=False,
      model_complexity=2,
      enable_segmentation=False,
      min_detection_confidence=0.5) as pose:

      vidcap = cv2.VideoCapture(args.input)
      success,image = vidcap.read()
      count = 0
      while success:
        # Convert the BGR image to RGB before processing.
        results = pose.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))

        if not results.pose_world_landmarks: # use world coordinates, not image coordinates
          continue

        row = {}
        for landmark in valid_landmarks:
          row[landmark + "_x"] = results.pose_world_landmarks.landmark[getattr(mp_pose.PoseLandmark, landmark)].x
          row[landmark + "_y"] = results.pose_world_landmarks.landmark[getattr(mp_pose.PoseLandmark, landmark)].y
          row[landmark + "_z"] = results.pose_world_landmarks.landmark[getattr(mp_pose.PoseLandmark, landmark)].z

        writer.writerow(row)

        success,image = vidcap.read()
        count += 1

if __name__=="__main__":
  main()
