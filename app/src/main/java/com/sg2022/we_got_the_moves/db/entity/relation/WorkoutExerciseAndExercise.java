package com.sg2022.we_got_the_moves.db.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

public class WorkoutExerciseAndExercise {

    @Embedded
    public WorkoutExercise workoutExercise;

    @Relation(
            entity = Exercise.class,
            parentColumn = "exerciseId",
            entityColumn = "id")
    public Exercise exercise;
}