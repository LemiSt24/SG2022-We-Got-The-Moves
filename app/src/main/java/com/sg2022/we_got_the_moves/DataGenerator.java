package com.sg2022.we_got_the_moves;

import android.util.Log;

import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for generating Data for the App Database.
 * Adapt this class for new Excercises, Workouts, Constraints etc. or change the DB file provided.
 * Chose your way of building the App Database in AppDatabase
 */
public class DataGenerator {

  private static final String TAG = "DataGenerator";

  public static String[] exerciseNames = {
    "Squats", "Side Planks", "Mountain Climbers", "Push-Ups", "Sit-Ups", "Planks", "Bicep Curls", "Romanian Deadlifts"
  };

  public static String[] exerciseInstructions = {
    "1. Stand up with your feet shoulder-width apart.\n\n"
        + "2. Bend your knees, press your hips back and stop the movement once the hip joint is slightly lower than the knees.\n\n"
        + "Be careful not to extend your knees in front of your toe tips in this position.\n\n"
        + "3. Press your heels into the floor to return to the initial position.\n\n"
        + "4. Repeat until set is complete.",
    "1. Lie on your side with your body fully extended.\n\n"
        + "2. Lift your body off the ground and balance your weight between the forearm and the side of the foot.\n\n"
        + "3. Keep your body in a straight line and hold for as long as you can.\n\n"
        + "4. Change sides and repeat.",
    "1. Start on the floor with your body in a straight line and your hands slightly wider than shoulder-width apart. Keep your toes and balls of the feet touching the floor.\n\n"
        + "2. Bring one knee up toward the center of your stomach and set it back down, quickly alternate this movement between legs.\n\n"
        + "3. Continue alternating until the set is complete.",
    "1. With your legs extended back, place the hands below the shoulders, slightly wider than shoulder-width apart.\n\n"
        + "2. Start bending your elbows and lower your chest until it’s just above the floor.\n\n"
        + "3. Push back to the starting position. A 1-second push, 1-second pause, 2-second down count is ideal.\n\n"
        + "4. Repeat until the set is complete.",
    "1. Lie down on your back, keep your knees bent, and your back and feet flat on the mat.\n\n"
        + "2. Slowly lift your torso and sit up.\n\n"
        + "3. Return to the starting position by rolling down one vertebra at a time.\n\n"
        + "4. Repeat the exercise until the set is complete.",
    "1. Begin in a kneeling position with your hands directly beneath your shoulders and your knees positioned hip-width apart.\n\n"
        + "2. Slowly lower your body until your elbows are at a 90-degree angle while keeping your core engaged.\n\n"
        + "3. Hold the position for the duration of the exercise, then release and return to the starting position.\n\n"
        + "4. Remember to engage your core and keep your back straight throughout the entire movement.",
    "1. Stand straight with a weight in each hand, your feet at shoulder width, and hands at your sides.\n\n"
        + "2. Tighten your biceps and lift the dumbbells. Keep the elbows close to your body and the upper arms stationary, only the forearms should move.\n\n"
        + "3. Once the dumbbells are at shoulder level, slowly lower the arms back to the starting position.\n\n"
        + "4. Repeat until the set is complete.",
    "1. Stand straight with a weight in each hand, your feet at shoulder width, and hands in front of your hips.\n\n"
        + "2. While keeping your back straight, lower your upper body and slightly bend your knees until your head is at hip height.\n\n"
        + "3. Slowly raise your upper body back to the starting position.\n\n"
        + "4. Repeat until the set is complete.",
  };

  public static String[] youtubeIds = {
    "Zqc_lc93hak",
    "Fum_2H2cog4",
    "w2iTOneGPdU",
    "v9LABVJzv8A",
    "5bOjqyL0PGE",
    "EvNPYh3OMKw",
    "P8MNX2ocp2U",
    "-DpZGUsnp80"
  };

  public static Exercise.UNIT[] isCountable = {
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.DURATION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.DURATION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.REPETITION,
  };

  public static float[] metScores = {
    5.5f, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    5.0f, // https://fitnessvolt.com/side-planks-calculator
    8.0f, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    3.8f, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    8.0f, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    5.0f, // https://fitnessvolt.com/side-planks-calculator
    5.0f, // estimated (moderate-intensive training between [3.9, 5.9])
    6.0f, // https://hashimashi.com/calories-burned-deadlifting/
  };

  public static String[] workoutNames = {
    "Full Body Power", "Full Body Endurance", "Quick Upper Body", "Quick Core", "Quick Legs"
  };

  public static ExerciseState[] states = {
    // "Squat", "Side-planks", "Mountain-climbers", "Pushup", "Sit-up", "Plank", "Biceps-curl"
    // Squats
    new ExerciseState(
        0,
        1,
        new ArrayList<>(Arrays.asList(1L, 7L, 12L)),
        "left_shoulder,right_shoulder",
        "left_hip,right_hip",
        "left_knee,right_knee",
        ExerciseState.COMPARATOR.GREATER,
        160,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L),
          // top
    new ExerciseState(
        1,
        1,
        new ArrayList<>(Arrays.asList(1L, 2L, 7L, 12L)),
            "left_shoulder,right_shoulder",
            "left_hip,right_hip",
            "left_knee,right_knee",
        ExerciseState.COMPARATOR.LESS,
        120,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L), // bottom
          /*
    // Side-planks
    new ExerciseState(0,2, new ArrayList<>(List.of()), "", "", "", null, 0, null, 500L),
    // Mountain-climbers
    new ExerciseState(0,3, new ArrayList<>(List.of()), "", "", "", null, 0, null, 500L),
    new ExerciseState(1,3, new ArrayList<>(List.of()), "", "", "", null, 0, null, 500L), */
    // Push-Up
    new ExerciseState(
        0,
        4,
        new ArrayList<>(Arrays.asList(3L, 4L)),
        "left_wrist,right_wrist",
        "left_elbow,right_elbow",
        "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.GREATER,
        150,
        ExerciseState.INSIGNIFICANT_DIMENSION.Z, 500L), // top
    new ExerciseState(
        1,
        4,
        new ArrayList<>(List.of(3L)),
        "left_wrist,right_wrist",
        "left_elbow,right_elbow",
        "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.LESS,
        100,
        ExerciseState.INSIGNIFICANT_DIMENSION.Z, 500L), // bottom
    // Sit-Up
    new ExerciseState(
        0,
        5,
        new ArrayList<>(List.of(5L, 8L)),
        "left_shoulder,right_shoulder",
        "left_hip,right_hip",
        "left_knee,right_knee",
        ExerciseState.COMPARATOR.GREATER,
        120,
        ExerciseState.INSIGNIFICANT_DIMENSION.Z, 500L), // bottom
    new ExerciseState(
        1,
        5,
        new ArrayList<>(List.of(5L)),
        "left_shoulder,right_shoulder",
        "left_hip,right_hip",
        "left_knee,right_knee",
        ExerciseState.COMPARATOR.LESS,
        80,
        ExerciseState.INSIGNIFICANT_DIMENSION.Z, 500L), // top
    // Plank
    new ExerciseState(
            0,
            6, new ArrayList<>(List.of(3L, 9L)),
            "",
            "",
            "",
            null, 0,
            null, 500L),
    // Biceps-Curl
    new ExerciseState(
        0,
        7,
        new ArrayList<>(Arrays.asList(1L, 6L, 7L, 13L)),
        "left_wrist,right_wrist",
        "left_elbow,right_elbow",
        "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.GREATER,
        150,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L), // bottom
    new ExerciseState(
        1,
        7,
        new ArrayList<>(Arrays.asList(1L, 6L, 7L, 13L)),
        "left_wrist,right_wrist",
        "left_elbow,right_elbow",
        "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.LESS,
        90,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L), // top

    // Romanian Deadlift
    new ExerciseState(
        0,
        8,
        new ArrayList<>(Arrays.asList(1L, 7L, 10L, 11L, 12L)), //1L, 2L, 3L, 7L, 10L, 11L, 12L
    "left_knee,right_knee",
    "left_hip,right_hip",
    "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.GREATER,
        160,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L), // bottom
    new ExerciseState(
        1,
        8,
        new ArrayList<>(Arrays.asList(1L, 2L, 10L, 11L, 12L)), //1L, 2L, 10L, 11L, 12L
        "left_knee,right_knee",
        "left_hip,right_hip",
        "left_shoulder,right_shoulder",
        ExerciseState.COMPARATOR.LESS,
        135,
        ExerciseState.INSIGNIFICANT_DIMENSION.X, 500L), // top
  };

  public static List<ExerciseState> getDummyExerciseStates(){
    List<ExerciseState> exerciseStates = new ArrayList<>();
    for (ExerciseState exerciseState: states) exerciseStates.add(exerciseState);
    Log.println(Log.DEBUG, "TAG", exerciseStates.toString());
    return exerciseStates;
  }

  public static List<Exercise> getDummyExercises() {
    List<Exercise> e = new ArrayList<>();

    for (int i = 0; i < exerciseNames.length; ++i) {
      if (i == 1 || i == 2)
        //noinspection UnnecessaryContinue
        continue;
      else {
        List<ExerciseState> exerciseStates = new ArrayList<>();
        e.add(
            new Exercise(
                i + 1,
                exerciseNames[i],
                exerciseInstructions[i],
                youtubeIds[i],
                isCountable[i],
                metScores[i]));
      }
    }
    return e;
  }

  public static List<Workout> getDummyWorkouts() {
    List<Workout> w = new ArrayList<>();
    for (int i = 0; i < workoutNames.length; ++i) {
      w.add(new Workout(i + 1, workoutNames[i]));
    }
    return w;
  }

  public static List<WorkoutExercise> getDummyWorkoutExercises() {
    List<Workout> DummyWorkouts = getDummyWorkouts();
    List<Exercise> DummyExercises = getDummyExercises();
    List<WorkoutExercise> we = new ArrayList<>();
    for (int i = 0; i < DummyWorkouts.size(); ++i) {
      
      switch (i)
      {
        case 0: // Full Body Power
          // Planks 60 Sek
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(3).id,
                  new ArrayList<>(List.of(60)),
                  0));
          // 5 x 5 Squats
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(0).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  1));
          // 5 x 5 Sit-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(2).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  2));
          // 5 x 5 Push-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(1).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  3));
          // 5 x 5 Bicep Curls
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(4).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  4));
          break;
        case 1: // Full Body Endurance
          // Planks 60 Sek
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(3).id,
                  new ArrayList<>(List.of(60)),
                  0));
          // 15 Push-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(1).id,
                  new ArrayList<>(List.of(15)),
                  1));
          // 15 Push-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(1).id,
                  new ArrayList<>(List.of(15)),
                  2));
          // 15 Squats
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(0).id,
                  new ArrayList<>(List.of(15)),
                  3));
          // 15 Sit-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(2).id,
                  new ArrayList<>(List.of(15)),
                  4));
          break;
        case 2: // Quick Upper Body
          // 5 x 5 Push-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(1).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  0));
          // 5 x 5 Bicep Curls
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(4).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  1));
          break;
        case 3: // Quick Core
          // 60 Sek Plank
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(3).id,
                  new ArrayList<>(List.of(60)),
                  0));
          // 5 x 5 Sit-Ups
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(2).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  1));
          // 5 x 5 Romanian Deadlifts
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(5).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  2));
          break;
        case 4: // Quick Legs
          // 60 Sek Plank
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(3).id,
                  new ArrayList<>(List.of(60)),
                  0));
          // 5 x 5 Squats
          we.add(
              new WorkoutExercise(
                  DummyWorkouts.get(i).id,
                  DummyExercises.get(0).id,
                  new ArrayList<>(List.of(5,5,5,5,5)),
                  1));
          break;
      }
    }
    return we;
  }

  public static List<FinishedWorkout> getDummyFinsishedWorkouts() {
    List<FinishedWorkout> l = new ArrayList<>();
    l.add(new FinishedWorkout(new Date(), 1, Duration.of(5, ChronoUnit.MINUTES)));
    l.add(new FinishedWorkout(new Date(), 2, Duration.of(5, ChronoUnit.MINUTES)));
    return l;
  }

  public static List<FinishedExercise> getDummyFinsishedExercise() {
    List<WorkoutExercise> we = getDummyWorkoutExercises();
    List<FinishedWorkout> fw = getDummyFinsishedWorkouts();
    List<FinishedExercise> fe = new ArrayList<>();
    for (int i = 0; i < fw.size(); i++) {
      final FinishedWorkout w = fw.get(i);
      List<WorkoutExercise> result =
          we.stream().filter(e -> e.workoutId == w.workoutId).collect(Collectors.toList());
      for (int j = 0; j < result.size(); j++) {
        fe.add(
            new FinishedExercise(
                fw.get(i).workoutId,
                result.get(j).exerciseId,
                30,
                (int) (Math.floor(Math.random() * 100))));
      }
    }
    return fe;
  }

  public static List<Constraint> getDummyConstraints() {
    List<Constraint> constraints = new ArrayList<>();
    // Füße schulterbreit - außen (nach außen mehr Variation erlauben, als nach innen) (squat-global, plank-global, bicep_curl-global, push_up-global)
    constraints.add(
        new Constraint(
            "left_shoulder",
            "right_shoulder",
            "left_ankle",
            "right_ankle",
            0.7,
            "Keep your feet at shoulder width.",
            Constraint.TYPE.DISTANCE,
            Constraint.INEQUALITY_TYPE.LESS,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null));
    // Knie hinter den Fußspitzen (squat-bottom)
    constraints.add(
        new Constraint(
            "left_foot_index,right_foot_index",
            "left_heel,right_heel",
            "left_knee,right_knee",
            "left_heel,right_heel",
            35,
            "Keep knees right behind your toe tips.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.GREATER,
            Constraint.INSIGNIFICANT_DIMENSION.X,
            90));
    // Körper gerade (side_plank-global, mountain_climbers-top, push_up-global,
    // plank-global)
    constraints.add(
        new Constraint(
            "right_shoulder,left_shoulder",
            "right_hip,left_hip",
            "right_ankle,left_ankle",
            "right_hip,left_hip",
            20,
            "Straighten your body.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.X,
            180));
    // Hände auf Schulterhöhe (push_up-top)
    constraints.add(
        new Constraint(
            "left_wrist,right_wrist",
            "left_shoulder,right_shoulder",
            "left_hip,right_hip",
            "left_shoulder,right_shoulder",
            20,
            "Keep your hands at shoulder height.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.X,
            75));
    // Füße flach auf Boden (sit_up-global)
    constraints.add(
        new Constraint(
            "left_ankle,right_ankle",
            "left_knee,right_knee", // unused from here
            "left_ankle,right_ankle",
            "left_knee,right_knee",
            25,
            "Keep your feet flat on the ground.",
            Constraint.TYPE.FLOOR_DISTANCE,
            Constraint.INEQUALITY_TYPE.LESS,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null)); // hier wird compareAngle für eine Distanz verwendet
    // Ellenbogen am Körper (bicep_curl-global)
    constraints.add(
        new Constraint(
            "left_shoulder",
            "right_shoulder",
            "left_elbow",
            "right_elbow",
            0.3,
            "Keep your elbows close to your body.",
            Constraint.TYPE.DISTANCE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null));
    // Körper aufrecht stehend (Squats, Bicep curls)
    constraints.add(
        new Constraint(
            "left_foot_index,right_foot_index",
            "nose",
            "left_elbow", // unused from here
            "right_elbow",
            0,
            "Keep your body in an upright position.",
            Constraint.TYPE.UPRIGHT,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null));
    // Rücken (Schultern) flach auf Boden
    constraints.add(
        new Constraint(
            "left_shoulder,right_shoulder",
            "nose", // unused from here
            "left_elbow",
            "right_elbow",
            25,
            "Lie with your back flat on the floor.",
            Constraint.TYPE.FLOOR_DISTANCE,
            Constraint.INEQUALITY_TYPE.LESS,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null)); // hier wird compareAngle für eine Distanz verwendet
    // annähernd rechter Winkel im Oberarm (Plank)
    constraints.add(
        new Constraint(
            "left_elbow,right_elbow",
                "left_wrist,right_wrist",
                "left_shoulder,right_shoulder",
                "left_elbow,right_elbow",
            30,
            "Keep your arms at approximately 90 degrees.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.Z,
            90));
    // gerade Arme (Romanian Deadlift
    constraints.add(
        new Constraint(
            "left_shoulder",
            "left_elbow",
            "left_wrist",
            "left_elbow",
            45,
            "Keep your arms straight.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.Z,
            180));
    // gerade Arme (Romanian Deadlift
    constraints.add(
        new Constraint(
            "right_shoulder",
            "right_elbow",
            "right_wrist",
            "right_elbow",
            45,
            "Keep your arms straight.",
            Constraint.TYPE.ANGLE,
            Constraint.INEQUALITY_TYPE.EQUAL,
            Constraint.INSIGNIFICANT_DIMENSION.Z,
            180));
    // Füße schulterbreit - innen (nach außen mehr Variation erlauben, als nach innen) (squat-global, plank-global, bicep_curl-global, push_up-global)
    constraints.add(
        new Constraint(
            "left_shoulder",
            "right_shoulder",
            "left_ankle",
            "right_ankle",
            0.3,
            "Keep your feet at shoulder width.",
            Constraint.TYPE.DISTANCE,
            Constraint.INEQUALITY_TYPE.GREATER,
            Constraint.INSIGNIFICANT_DIMENSION.NONE,
            null));
    // Ellenbogen nicht nach vorne bei BicepCurl (bicep_curl-global)
    constraints.add(
            new Constraint(
                    "left_elbow,right_elbow",
                    "left_shoulder,right_shoulder",
                    "left_hip,right_hip",
                    "left_shoulder,right_shoulder",
                    45,
                    "Keep your elbows close to your body.",
                    Constraint.TYPE.ANGLE,
                    Constraint.INEQUALITY_TYPE.GREATER,
                    Constraint.INSIGNIFICANT_DIMENSION.X,
                    0));
    return constraints;
  }
}
