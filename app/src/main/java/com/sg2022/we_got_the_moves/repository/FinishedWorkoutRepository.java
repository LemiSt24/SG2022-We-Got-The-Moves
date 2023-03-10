package com.sg2022.we_got_the_moves.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.daos.FinishedExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.daos.FinishedWorkoutDao;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class FinishedWorkoutRepository {
  private static final String TAG = "FinishedWorkoutRepository";

  private static volatile FinishedWorkoutRepository INSTANCE;

  private final FinishedWorkoutDao finishedWorkoutDao;
  private final FinishedExerciseDao finishedExerciseDao;
  private final AppExecutors executors;

  private FinishedWorkoutRepository(@NonNull AppDatabase db) {
    this.finishedWorkoutDao = db.FinishedWorkoutDao();
    this.finishedExerciseDao = db.FinishedExerciseDao();
    this.executors = AppExecutors.getInstance();
  }

  public static FinishedWorkoutRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (FinishedWorkoutRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app);
          INSTANCE = new FinishedWorkoutRepository(db);
        }
      }
    }
    return INSTANCE;
  }

  public void insert(FinishedWorkout finishedWorkout) {
    this.executors.getPoolThread().execute(() -> this.finishedWorkoutDao.insert(finishedWorkout));
  }

  public LiveData<FinishedWorkout> getLastTraining() {
    return this.finishedWorkoutDao.getLastTraining();
  }

  public LiveData<List<FinishedWorkout>> getOrderedFinishedWorkouts() {
    return this.finishedWorkoutDao.getOrderedTrainings();
  }

  public void insertFinishedExercise(List<FinishedExercise> l) {
    this.executors.getPoolThread().execute(() -> this.finishedExerciseDao.insert(l));
  }

  public void getAllFinishedWorkoutsByDateRangeSingle(
      Date begin, Date end, SingleObserver<List<FinishedWorkoutAndFinishedExercises>> observer) {
    this.finishedWorkoutDao
        .getAllFinishedWorkoutsAndExercise(begin, end)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getAllFinishedWorkoutsSingle(
      SingleObserver<List<FinishedWorkoutAndFinishedExercises>> observer) {
    this.finishedWorkoutDao
        .getAll()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getTotalDurationSingle(SingleObserver<Duration> observer) {
    this.finishedWorkoutDao
        .getTotalDuration()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getMaxDurationSingle(SingleObserver<Duration> observer) {
    this.finishedWorkoutDao
        .getMaxDuration()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getNumberOfFinishedWorkoutsSmallerEqualNumberOfDistinctExercises
          (int value, SingleObserver<List<Integer>> observer){
    this.finishedWorkoutDao
        .getNumberOfFinishedWorkoutsSmallerEqualNumberOfDistinctExercises(value)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getLongestDurationOfFinishedWorkouts(SingleObserver<List<Duration>> observer){
    this.finishedWorkoutDao
            .getLongestWorkoutDuration()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer);
  }

  public void getTotalReps(long exerciseId, SingleObserver<List<Integer>> observer){
    this.finishedExerciseDao
      .getTotalReps(exerciseId)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(observer);
  }

  public void getNumberDistinctFinishedExercises(SingleObserver<List<Integer>> observer){
    this.finishedExerciseDao
            .getNumberDistinctFinishedExercises()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer);
  }

}
