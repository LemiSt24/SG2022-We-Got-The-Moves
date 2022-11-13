package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert(onConflict = REPLACE)
    public void insert(Exercise e);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Exercise> es);

    @Update(onConflict = REPLACE)
    public void update(Exercise e);

    @Delete
    public void delete(Exercise e);

    @Query("SELECT * FROM Exercise")
    public LiveData<List<Exercise>> getAll();

    @Query("SELECT Exercise.* FROM Exercise JOIN WorkoutExercise ON (Exercise.id == WorkoutExercise.exerciseId) WHERE WorkoutExercise.workoutId == :id")
    public LiveData<List<Exercise>>getExercisesByWorkoutId(long id);

    @Query("SELECT * FROM Exercise WHERE Exercise.id = :id")
    public LiveData<Exercise> get(long id);
}
