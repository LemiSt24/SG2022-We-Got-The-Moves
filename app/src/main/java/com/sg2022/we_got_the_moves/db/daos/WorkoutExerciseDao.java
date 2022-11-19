package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.util.List;

@Dao
public interface WorkoutExerciseDao {

    @Insert(onConflict = REPLACE)
    void insert(WorkoutExercise e);

    @Insert(onConflict = REPLACE)
    void insertAll(List<WorkoutExercise> ws);

    @Update(onConflict = REPLACE)
    void update(WorkoutExercise e);

    @Delete
    void delete(WorkoutExercise e);

    @Query("SELECT * FROM WorkoutExercise")
    LiveData<List<WorkoutExercise>> getAll();

    @Query("SELECT * FROM WorkoutExercise WHERE WorkoutExercise.workoutId = :workoutId AND WorkoutExercise.exerciseId = :exerciseId")
    LiveData<WorkoutExercise> get(long workoutId, long exerciseId);
}
