package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import javax.annotation.Nullable;

@Entity(
    tableName = "FinishedExercise",
    primaryKeys = {"finishedWorkoutId", "exerciseId"},
    foreignKeys = {
      @ForeignKey(
          entity = FinishedWorkout.class,
          parentColumns = "id",
          childColumns = "finishedWorkoutId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE),
      @ForeignKey(
          entity = Exercise.class,
          parentColumns = "id",
          childColumns = "exerciseId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE)
    },
    indices = {
      @Index(
          value = {"finishedWorkoutId", "exerciseId"},
          unique = true),
      @Index(value = {"exerciseId"})
    })
public class FinishedExercise {


  @ColumnInfo(name = "finishedWorkoutId")
  public long finishedWorkoutId;

  @ColumnInfo(name = "exerciseId")
  public long exerciseId;

  @ColumnInfo(name = "duration")
  public int duration; // in [secs]

  @ColumnInfo(name = "amount")
  public int amount; // 0 for time-based

  public FinishedExercise(long finishedWorkoutId, long exerciseId, int duration, int amount) {
    this.finishedWorkoutId = finishedWorkoutId;
    this.exerciseId = exerciseId;
    this.duration = duration;
    this.amount = amount;
  }

  public void setFinishedWorkoutId(long finishedWorkoutId) {
    this.finishedWorkoutId = finishedWorkoutId;
  }
}
