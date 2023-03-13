package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;

public class FinishedExerciseAndExercise {
  @Embedded public FinishedExercise finishedExercise;

  @Relation(entity = Exercise.class, parentColumn = "exerciseId", entityColumn = "id")
  public Exercise exercise;
}
