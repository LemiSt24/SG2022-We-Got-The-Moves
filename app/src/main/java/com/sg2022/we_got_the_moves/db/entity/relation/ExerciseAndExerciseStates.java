package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import java.util.List;

public class ExerciseAndExerciseStates {
  @Embedded public Exercise exercise;

  @Relation(entity = ExerciseState.class, parentColumn = "id", entityColumn = "exerciseId")
  public List<ExerciseStateAndConstraints> exerciseStatesAndConstraints;
}
