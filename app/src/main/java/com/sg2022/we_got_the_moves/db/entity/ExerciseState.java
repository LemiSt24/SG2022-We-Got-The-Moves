package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.List;

@Entity(
    tableName = "ExerciseState",
    primaryKeys = {"exerciseId"},
    foreignKeys = {
      @ForeignKey(
          entity = Exercise.class,
          parentColumns = "id",
          childColumns = "exerciseId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE),
    })
public class ExerciseState {

  @ColumnInfo(name = "exerciseId", index = true)
  public long exerciseId;

  @ColumnInfo(name = "constraintIds", index = true)
  public List<Long> constraintIds;

  public ExerciseState(long exerciseId, List<Long> constraintIds) {
    this.exerciseId = exerciseId;
    this.constraintIds = constraintIds;
  }
}
