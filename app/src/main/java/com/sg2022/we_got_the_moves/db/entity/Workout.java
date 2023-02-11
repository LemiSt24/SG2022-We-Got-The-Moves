package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "Workout")
public class Workout {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "name")
  public String name;

  @ColumnInfo(name = "workoutExercises")
  public List<WorkoutExercise> workoutExercises;

  public Workout(long id, String name, List<WorkoutExercise> workoutExercises) {
    this.id = id;
    this.name = name;
    this.workoutExercises = workoutExercises;
  }

  @Ignore
  public Workout(String name) {
    this.name = name;
  }
}
