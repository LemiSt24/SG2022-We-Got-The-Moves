package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;

import java.util.List;

public class FinishedWorkoutAndFinishedExercises {
  @Embedded public FinishedWorkout finishedWorkout;

  @Relation(
      entity = FinishedExercise.class,
      parentColumn = "id",
      entityColumn = "finishedWorkoutId")
  public List<FinishedExerciseAndExercise> finishedExerciseAndExercises;
}
