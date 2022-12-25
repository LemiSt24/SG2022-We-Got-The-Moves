package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;

import java.util.List;

public class ExerciseStateAndConstraints {
  @Embedded public ExerciseState exerciseState;

  @Relation(entity = Constraint.class, parentColumn = "constraintId", entityColumn = "id")
  public List<Constraint> constraints;
}


