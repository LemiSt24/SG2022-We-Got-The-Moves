package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "WorkoutExercise",
        primaryKeys = {"workoutId", "exerciseId"},
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = "id",
                        childColumns = "workoutId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index(value = {"workoutId", "exerciseId"}, unique = true)})
public class WorkoutExercise {

    @ColumnInfo(name = "workoutId")
    public long workoutId;

    @ColumnInfo(name = "exerciseId")
    public long exerciseId;

    @ColumnInfo(name = "amount")
    public int amount;

    public WorkoutExercise() {
    }

    @Ignore
    public WorkoutExercise(long workoutId, long exerciseId, int amount) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.amount = amount;
    }
}
