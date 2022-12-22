package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExercises;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface WorkoutDao {

  @Insert(onConflict = REPLACE)
  void insert(Workout w);

  @Insert(onConflict = REPLACE)
  Single<Long> insertSingle(Workout w);

  @Insert(onConflict = REPLACE)
  void insertAll(List<Workout> ws);

  @Insert(onConflict = REPLACE)
  Single<List<Long>> insertAllSingle(List<Workout> ws);

  @Update(onConflict = REPLACE)
  void update(Workout w);

  @Delete
  void delete(Workout w);

  @Delete
  void delete(List<Workout> ws);

  @Query("SELECT * FROM Workout WHERE Workout.id = :workoutId")
  LiveData<Workout> getWorkout(long workoutId);

  @Query("SELECT * FROM Workout WHERE Workout.id = :workoutId")
  Single<Workout> getWorkoutSingle(long workoutId);

  @Query("SELECT * FROM Workout")
  LiveData<List<Workout>> getAllWorkouts();

  @Transaction
  @Query("SELECT * FROM Workout")
  LiveData<List<WorkoutAndWorkoutExercises>> getAllWorkoutsWithExerciseAndWorkoutExercise();
}
