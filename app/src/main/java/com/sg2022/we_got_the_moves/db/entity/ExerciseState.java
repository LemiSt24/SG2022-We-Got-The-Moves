package com.sg2022.we_got_the_moves.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
    tableName = "ExerciseState",
    primaryKeys = {"exerciseId", "exerciseState", "constraintId"},
    foreignKeys = {
      @ForeignKey(
          entity = Exercise.class,
          parentColumns = "id",
          childColumns = "exerciseId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE),
      @ForeignKey(
          entity = Constraint.class,
          parentColumns = "id",
          childColumns = "constraintId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE)
    })
public class ExerciseState {

  @ColumnInfo(name = "exerciseId", index = true)
  public long exerciseId;

  @ColumnInfo(name = "exerciseState", index = true)
  @NonNull
  public STATE exerciseState;

  @ColumnInfo(name = "constraintId", index = true)
  public long constraintId;

  public ExerciseState(long exerciseId, @NonNull STATE exerciseState, long constraintId) {
    this.exerciseId = exerciseId;
    this.exerciseState = exerciseState;
    this.constraintId = constraintId;
  }

  public enum STATE {
    GLOBAL,
    BOTTOM,
    TO
  }
}
