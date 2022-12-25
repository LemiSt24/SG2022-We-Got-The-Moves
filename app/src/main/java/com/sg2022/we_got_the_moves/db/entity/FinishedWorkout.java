package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.Duration;
import java.util.Date;

@Entity(
    tableName = "FinishedWorkout",
    foreignKeys = {
      @ForeignKey(
          entity = Workout.class,
          parentColumns = "id",
          childColumns = "workoutId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE)
    },
    indices = {
      @Index(
          value = {"id"},
          unique = true),
      @Index(
          value = {"date", "workoutId"},
          unique = true),
      @Index(value = {"workoutId"})
    })
public class FinishedWorkout {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "date")
  public Date date;

  @ColumnInfo(name = "workoutId")
  public long workoutId;

  @ColumnInfo(name = "duration")
  public Duration duration;

  @Ignore
  public FinishedWorkout(long id, Date date, long workoutId, Duration duration) {
    this.id = id;
    this.date = date;
    this.workoutId = workoutId;
    this.duration = duration;
  }

  public FinishedWorkout(Date date, long workoutId, Duration duration) {
    this.date = date;
    this.workoutId = workoutId;
    this.duration = duration;
  }
}
