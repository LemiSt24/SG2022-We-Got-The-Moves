package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface FinishedExerciseDao {

  @Insert(onConflict = REPLACE)
  void insert(FinishedExercise fe);

  @Insert(onConflict = REPLACE)
  void insert(List<FinishedExercise> l);

  @Insert(onConflict = REPLACE)
  Single<Long> insertSingle(FinishedExercise fe);

  @Insert(onConflict = REPLACE)
  Single<List<Long>> insertAllSingle(List<FinishedExercise> l);

  @Update(onConflict = REPLACE)
  void update(FinishedExercise fe);

  @Delete
  void delete(FinishedExercise fe);

  @Query(
      "SELECT * FROM FinishedExercise WHERE FinishedExercise.exerciseId = :exerciseId AND FinishedExercise.finishedWorkoutId = :finishedWorkoutId")
  Single<FinishedExercise> getSingle(long finishedWorkoutId, long exerciseId);

  @Query(
      "SELECT * FROM FinishedExercise WHERE FinishedExercise.finishedWorkoutId = :finishedWorkoutId")
  Single<List<FinishedExercise>> getAllByFinishedWorkoutIdSingle(long finishedWorkoutId);

  @Query("SELECT * FROM FinishedExercise WHERE FinishedExercise.exerciseId = :exerciseId")
  Single<List<FinishedExercise>> getAllByExerciseIdSingle(long exerciseId);
}
