package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/*(
    ,
    primaryKeys = {"workoutId", "exerciseId"},
    foreignKeys = {
      @ForeignKey(
          entity = Workout.class,
          parentColumns = "id",
          childColumns = "workoutId",
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
          value = {"workoutId", "exerciseId"},
          unique = true),
      @Index(value = {"exerciseId"}),
      @Index(value = {"workoutId"})
    })*/
@Entity (tableName = "WorkoutExercise",
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = "id",
                        childColumns = "workoutId",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        })
public class WorkoutExercise {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "workoutId", index = true)
  public long workoutId;

  @ColumnInfo(name = "exerciseId", index = true)
  public long exerciseId;

  @ColumnInfo(name = "amount")
  public int amount; // in [sec] or [#]

  public WorkoutExercise(long workoutId, long exerciseId, int amount) {
    this.workoutId = workoutId;
    this.exerciseId = exerciseId;
    this.amount = amount;
  }
}
