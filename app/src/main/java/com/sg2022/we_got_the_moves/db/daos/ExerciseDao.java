package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;

@Dao
public interface ExerciseDao {

    @Insert(onConflict = REPLACE)
    void insert(Exercise e);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Exercise> es);

    @Update(onConflict = REPLACE)
    void update(Exercise e);

    @Delete
    void delete(Exercise e);

    @Query("SELECT * FROM Exercise")
    LiveData<List<Exercise>> getAllExercises();

    @Query("SELECT Exercise.* FROM Exercise JOIN WorkoutExercise ON (Exercise.id == WorkoutExercise.exerciseId) WHERE WorkoutExercise.workoutId == :workoutId")
    LiveData<List<Exercise>> getAllExercises(long workoutId);

    @Transaction
    @Query("SELECT * FROM Exercise WHERE Exercise.id NOT IN (SELECT Exercise.id FROM Exercise JOIN WorkoutExercise ON (Exercise.id == WorkoutExercise.exerciseId) WHERE WorkoutExercise.workoutId == :workoutId)")
    Maybe<List<Exercise>> getAllNotContainedExercisesMaybe(long workoutId);


    @Query("SELECT * FROM Exercise WHERE Exercise.id = :exerciseId")
    LiveData<Exercise> getExercise(long exerciseId);
}
