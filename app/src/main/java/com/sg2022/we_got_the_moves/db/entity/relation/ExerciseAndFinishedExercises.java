package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import java.util.List;

public class ExerciseAndFinishedExercises {
  @Embedded public Exercise exercise;

  @Relation(entity = FinishedExercise.class, parentColumn = "id", entityColumn = "exerciseId")
  public List<FinishedExercise> finishedExercises;
}
