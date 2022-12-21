package com.sg2022.we_got_the_moves;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.util.ArrayList;
import java.util.List;

public class DataGenerator {

  private static final String TAG = "DataGenerator";

  public static String[] exerciseNames = {
    "Squats", "Side-Planks", "Mountain-Climbers", "Push-Ups", "Sit-Ups", "Plank", "Biceps-Curls"
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
    "Zqc_lc93hak", "Fum_2H2cog4", "w2iTOneGPdU", "v9LABVJzv8A", "5bOjqyL0PGE" , "EvNPYh3OMKw", "P8MNX2ocp2U"
  };

  public static int[] imageIds = {
    R.drawable.squates,
    R.drawable.sideplanks,
    R.drawable.mountainclimbers,
    R.drawable.pushups,
    R.drawable.situps,
    R.drawable.plank,
    R.drawable.bicepsculs
  };

  public static boolean[] isCountable = {
    true, false, /* set to false for test purposes */ true, true, true, false, true
  };

  public static String[] workoutNames = {
    "Power", "Basic", "Relaxing", "Heavy", "Endurance", "Strength", "Agility", "Training"
  };

  public static List<Exercise> getDummyExercises() {
    List<Exercise> e = new ArrayList<>();

    for (int i = 0; i < exerciseNames.length; ++i) {
      if (i == 1 || i == 2);
      else {
      e.add(
          new Exercise(
              i + 1,
              exerciseNames[i],
              exerciseInstructions[i],
              youtubeIds[i],
              imageIds[i],
              isCountable[i]));
    }}
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
}
