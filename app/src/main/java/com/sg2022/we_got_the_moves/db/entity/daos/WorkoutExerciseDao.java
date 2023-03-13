package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface WorkoutExerciseDao {

  @Insert(onConflict = REPLACE)
  void insert(WorkoutExercise e);

  @Transaction
  @Insert(onConflict = REPLACE)
  void insertAll(List<WorkoutExercise> ws);


  @Update(onConflict = REPLACE)
  void update(WorkoutExercise e);

  @Delete
  void delete(WorkoutExercise e);

  @Transaction
  @Delete
  void deleteAll(List<WorkoutExercise> wes);

  @Query("SELECT * FROM WorkoutExercise WHERE WorkoutExercise.workoutId = :workoutId order by orderNum asc")
  Single<List<WorkoutExercise>> getAllWorkoutExerciseSingle(long workoutId);

  @Transaction
  @Query("SELECT * FROM WorkoutExercise WHERE WorkoutExercise.workoutId == :workoutId order by orderNum asc")
  LiveData<List<WorkoutExerciseAndExercise>> getAllWorkoutExerciseAndExercise(long workoutId);

  @Transaction
  @Query("SELECT * FROM WorkoutExercise WHERE WorkoutExercise.workoutId == :workoutId order by orderNum asc")
  Single<List<WorkoutExerciseAndExercise>> getAllWorkoutExerciseAndExerciseSingle(long workoutId);
}
