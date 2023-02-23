package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface FinishedWorkoutDao {

  @Insert(onConflict = REPLACE)
  void insert(FinishedWorkout finishedWorkout);

  @Insert(onConflict = REPLACE)
  void insert(List<FinishedWorkout> l);

  @Insert(onConflict = REPLACE)
  Single<Long> insertSingle(FinishedWorkout finishedWorkout);

  @Transaction
  @Insert(onConflict = REPLACE)
  Single<List<Long>> insertAllSingle(List<FinishedWorkout> l);

  @Query("Select * From FinishedWorkout Order by date Desc limit :n")
  LiveData<List<FinishedWorkout>> getNLastTrainings(int n);

  @Query("Select * From FinishedWorkout order by date Desc")
  LiveData<List<FinishedWorkout>> getOrderedTrainings();

  @Query("Select * From FinishedWorkout Order by date Desc limit 1")
  LiveData<FinishedWorkout> getLastTraining();

  @Query("Select distinct workoutId From FinishedWorkout Order by date Desc limit :n")
  LiveData<List<Long>> getNLastDistictWorkoutIds(int n);

  @Transaction
  @Query("Select * From FinishedWorkout")
  Single<List<FinishedWorkoutAndFinishedExercises>> getAll();

  @Transaction
  @Query("Select * From FinishedWorkout WHERE FinishedWorkout.id = :finishWorkoutId")
  Single<FinishedWorkoutAndFinishedExercises> get(long finishWorkoutId);

  @Transaction
  @Query(
      "Select * From FinishedWorkout WHERE FinishedWorkout.date >= :begin AND FinishedWorkout.date <= :end")
  Single<List<FinishedWorkoutAndFinishedExercises>> getAllFinishedWorkoutsAndExercise(
      Date begin, Date end);

  @Transaction
  @Query("Select MAX(FinishedWorkout.duration) From FinishedWorkout")
  Single<Duration> getMaxDuration();

  @Transaction
  @Query("Select SUM(FinishedWorkout.duration) From FinishedWorkout")
  Single<Duration> getTotalDuration();

  @Transaction
  @Query(
      "Select AVG(FinishedWorkout.duration) From FinishedWorkout WHERE FinishedWorkout.date >= :begin AND FinishedWorkout.date <= :end")
  Single<Duration> getAvgDurationByRange(Date begin, Date end);

  @Query("SELECT COUNT(*) FROM (" +
    "SELECT DISTINCT(fw.id) FROM FinishedWorkout fw, FinishedExercise fe " +
    "WHERE fw.id == fe.finishedWorkoutId AND fe.duration != 0 " +
    "GROUP BY fw.id HAVING COUNT(DISTINCT fe.exerciseId) <= :value)dt")
  Single<List<Integer>> getNumberOfFinishedWorkoutsSmallerEqualNumberOfDistinctExercises(int value);

  @Query("SELECT w.duration FROM FinishedWorkout w ORDER BY w.duration DESC limit 1")
  Single<List<Duration>> getLongestWorkoutDuration();
}
