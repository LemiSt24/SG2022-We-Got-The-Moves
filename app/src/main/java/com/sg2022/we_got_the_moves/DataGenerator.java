package com.sg2022.we_got_the_moves;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.util.ArrayList;
import java.util.List;

public class DataGenerator {

    public static final String TAG = "DataGenerator";

    public static String[] exerciseNames = {
            "Fly Steps",
            "Climbers",
            "Flutter Kicks",
            "High Knees",
            "Shoulder Taps",
            "Scissors",
            "Sprinter Lunges",
            "Arm Raises",
            "Side Leg Raises",
            "Side Chops",
            "Alt Arm / Leg Raises",
            "Climber Taps",
            "Plank Arm Raises",
            "Plank Jacks",
            "Plank Leg Raises",
            "Plank Rolls",
            "Up & Down Planks",
            "Sitting Twists",
            "Plank Jump-Ins",
            "Jumping Lunges",
            "Plank Into Lunge",
            "Squats",
            "Elbow Strikes",
            "Double Side Kicks",
            "Backfists",
            "Punches / Straight",
            "Knee Strikes",
            "Hooks",
            "Turning Kicks / Side-to-Side",
            "Turning Kicks",
            "Side Kicks / Side-to-Side",
            "Hook Kicks",
            "Uppercut",
            "Backfist + Side Kick",
            "Jab + Cross + Squat",
            "Squat + Side Kick",
            "Squat + Turning Kick",
            "Overhead Punches",
            "Squat + Front Kick",
            "Jab + Cross + Jab + Turning Kick"
    };

    public static String[] workoutNames = {
            "POWER",
            "Basic",
            "Relaxing",
            "Heavy",
            "Endurance",
            "Strength",
            "Agility",
            "Training"
    };

    public static List<Exercise> getDummyExercises() {
        List<Exercise> e = new ArrayList<>();
        for (int i = 0; i < exerciseNames.length; ++i) {
            e.add(new Exercise(i + 1, exerciseNames[i]));
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
}
