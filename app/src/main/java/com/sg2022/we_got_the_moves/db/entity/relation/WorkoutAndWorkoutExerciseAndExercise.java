package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.util.List;

public class WorkoutAndWorkoutExerciseAndExercise {

  @Embedded public Workout workout;

  @Relation(entity = WorkoutExercise.class, parentColumn = "id", entityColumn = "workoutId")
  public List<WorkoutExerciseAndExercise> workoutAndExercises;
}
