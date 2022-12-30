package com.sg2022.we_got_the_moves;

import android.annotation.SuppressLint;
import android.util.Pair;

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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DataGenerator {

  private static final String TAG = "DataGenerator";

  public static String[] exerciseNames = {
    "Squat", "Side-planks", "Mountain-climbers", "Pushup", "Sit-up", "Plank", "Biceps-curl"
  };

  public static String[] exerciseInstructions = {
    "1. Stand up with your feet shoulder-width apart.\n\n"
        + "2. Bend your knees, press your hips back and stop the movement once the hip joint is slightly lower than the knees.\n\n"
        + "3. Press your heels into the floor to return to the initial position.\n\n"
        + "4. Repeat until set is complete.",
    "1. Lie on your side with your body fully extended.\n\n"
        + "2. Lift your body off the ground and balance your weight between the forearm and the side of the foot.\n\n"
        + "3. Keep your body in a straight line and hold for as long as you can.\n\n"
        + "4. Change sides and repeat.",
    "1. Start with your body in a straight line and your hands slightly wider than shoulder-width apart. Keep your toes and balls of the feet touching the floor.\n\n"
        + "2. Bring one knee up toward the center of your stomach and then quickly alternate between legs.\n\n"
        + "3. Continue alternating until the set is complete.",
    "1. With your legs extended back, place the hands below the shoulders, slightly wider than shoulder-width apart.\n\n"
        + "2. Start bending your elbows and lower your chest until it’s just above the floor.\n\n"
        + "3. Push back to the starting position. A 1-second push, 1-second pause, 2-second down count is ideal.\n\n"
        + "4. Repeat ",
    "1. Lie down on your back, keep your knees bent, and your back and feet flat on the mat.\n\n"
        + "2. Slowly lift your torso and sit up.\n\n"
        + "3. Return to the starting position by rolling down one vertebra at a time.\n\n"
        + "4. Repeat the exercise until set is complete.",
    "1. Begin in a kneeling position with your hands directly beneath your shoulders and your knees positioned hip-width apart.\n\n"
        + "2. Slowly lower your body until your elbows are at a 90-degree angle while keeping your core engaged.\n\n"
        + "3. Hold the position for the duration of the exercise, then release and return to the starting position.\n\n"
        + "4. Remember to engage your core and keep your back straight throughout the entire movement.",
    "1. Stand straight with a dumbbell in each hand, your feet shoulder-width apart, and hands by your sides.\n\n"
        + "2. Squeeze the biceps and lift the dumbbells. Keep the elbows close to your body and the upper arms stationary, only the forearms should move.\n\n"
        + "3. Once the dumbbells are at shoulder level, slowly lower the arms to the starting position.\n\n"
        + "4. Repeat."
  };

  public static String[] youtubeIds = {
    "Zqc_lc93hak",
    "Fum_2H2cog4",
    "w2iTOneGPdU",
    "v9LABVJzv8A",
    "5bOjqyL0PGE",
    "EvNPYh3OMKw",
    "P8MNX2ocp2U"
  };

  public static int[] imageIds = {
    R.drawable.squates,
    R.drawable.side_planks,
    R.drawable.mountain_climbers,
    R.drawable.push_ups,
    R.drawable.sit_ups,
    R.drawable.plank,
    R.drawable.bicepsculs
  };

  public static Exercise.UNIT[] isCountable = {
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.DURATION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.REPETITION,
    Exercise.UNIT.DURATION,
    Exercise.UNIT.REPETITION
  };

  public static double[] minPerRep = {
    (1.0d / 20.0d),
    0.0d, // if UNIT.DURATION
    (1.0d / 40.0d),
    (1.0d / 20.0d),
    (1.0d / 20.0d),
    0.0d,
    (1.0d / 40.0d)
  };

  public static double[] metScores = {
    5.5d, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    5.0d, // https://fitnessvolt.com/side-planks-calculator
    8.0d, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    3.8d, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    8.0d, // https://middleeasy.com/sports/push-ups/#:~:text=For%20the%20calorie%20burn%20calculation,activity%20you%20performed%20in%20minutes.
    5.0d, // https://fitnessvolt.com/side-planks-calculator
    5.0d // estimated (moderate-intensive training between [3.9, 5.9])
  };

  public static String[] workoutNames = {
    "Power", "Basic", "Relaxing", "Heavy", "Endurance", "Strength", "Agility", "Training"
  };

  public static List<Exercise> getDummyExercises() {
    List<Exercise> e = new ArrayList<>();

    for (int i = 0; i < exerciseNames.length; ++i) {
      if (i == 1 || i == 2)
        ;
      else {
        e.add(
            new Exercise(
                i + 1,
                exerciseNames[i],
                exerciseInstructions[i],
                youtubeIds[i],
                imageIds[i],
                isCountable[i],
                0,
                0,
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
      for (int j = 0; j < DummyExercises.size(); ++j) {
        we.add(new WorkoutExercise(DummyWorkouts.get(i).id, DummyExercises.get(j).id, 5));
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
        fe.add(new FinishedExercise(fw.get(i).workoutId, result.get(j).exerciseId, 30, 0));
      }
    }
    return fe;
  }

  public static Pair<List<ExerciseState>, List<Constraint>> getDummyExerciseStatesAndConstraints() {
    List<Constraint> constraints = new ArrayList<>();
    List<ExerciseState> exerciseStates = new ArrayList<>();
    List<Exercise> exs = getDummyExercises();
    long count = 1;
    for (int i = 0; i < exs.size(); ++i) {
      for (int j = 0; j < ExerciseState.STATE.values().length; j++) {
        for (int k = 0; k < NormalizedLandmark.landmark_names.size(); k++) {
          String from1 = NormalizedLandmark.landmark_names.get(k);
          String to1 =
              NormalizedLandmark.landmark_names.get(
                  (k + 1) % NormalizedLandmark.landmark_names.size());
          String from2 =
              NormalizedLandmark.landmark_names.get(
                  (k + 2) % NormalizedLandmark.landmark_names.size());
          String to2 =
              NormalizedLandmark.landmark_names.get(
                  (k + 3) % NormalizedLandmark.landmark_names.size());
          double max_diff = 1.0;
          String template =
              "Message for constraint [from1: %s, to1: %s, from2: %s, to2: %s, max_diff: %f]";
          @SuppressLint("DefaultLocale")
          String msg = String.format(template, from1, to1, from2, to2, max_diff);
          Constraint c = new Constraint(count, from1, to1, from2, to2, max_diff, msg);
          ++count;
          constraints.add(c);
          ExerciseState es =
              new ExerciseState(exs.get(i).id, ExerciseState.STATE.values()[j], c.id);
          exerciseStates.add(es);
        }
      }
    }
    return new Pair<>(exerciseStates, constraints);
  }
}
