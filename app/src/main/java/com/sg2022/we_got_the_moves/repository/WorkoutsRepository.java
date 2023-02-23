package com.sg2022.we_got_the_moves.repository;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.daos.WorkoutDao;
import com.sg2022.we_got_the_moves.db.entity.daos.WorkoutExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseAndFinishedExercises;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExercises;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WorkoutsRepository {

  private static final String TAG = "WorkoutsRepository";

  private static volatile WorkoutsRepository INSTANCE;

  private final WorkoutDao workoutDao;
  private final ExerciseDao exerciseDao;
  private final WorkoutExerciseDao workoutExerciseDao;
  private final AppExecutors executors;

  private WorkoutsRepository(@NonNull AppDatabase db) {
    this.workoutDao = db.WorkoutDao();
    this.exerciseDao = db.ExerciseDao();
    this.workoutExerciseDao = db.WorkoutExerciseDao();
    this.executors = AppExecutors.getInstance();
  }

  public static WorkoutsRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (WorkoutsRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app);
          INSTANCE = new WorkoutsRepository(db);
        }
      }
    }
    return INSTANCE;
  }

  public void insertWorkout(Workout w, SingleObserver<Long> observer) {
    this.workoutDao
        .insertSingle(w)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void insertWorkout(Workout w) {
    this.executors.getPoolThread().execute(() -> this.workoutDao.insert(w));
  }

  public void updateWorkout(Workout w) {
    this.executors.getPoolThread().execute(() -> this.workoutDao.update(w));
  }

  public void updateWorkoutExercise(WorkoutExercise we) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.update(we));
  }

  public LiveData<List<Workout>> getAllWorkouts() {
    return this.workoutDao.getAllWorkouts();
  }

  public void getAllWorkouts(SingleObserver<List<Workout>> observer) {
    this.workoutDao
        .getAllWorkoutsSingle()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public LiveData<Workout> getWorkout(long id) {
    return this.workoutDao.getWorkout(id);
  }

  public LiveData<List<Exercise>> getAllExercises(long workoutId) {
    return this.exerciseDao.getAllExercises(workoutId);
  }

  public void getAllExercises(int workoutId, SingleObserver<List<Exercise>> observer) {
    this.exerciseDao
        .getAllExercisesSingle(workoutId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public LiveData<List<Exercise>> getAllExercises() {
    return this.exerciseDao.getAllExercises();
  }

  public void getAllExercises(SingleObserver<List<Exercise>> observer) {
    this.exerciseDao
        .getAllExercisesSingle()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public LiveData<Exercise> getExercise(long exerciseId) {
    return this.exerciseDao.getExercise(exerciseId);
  }

  public LiveData<List<WorkoutExerciseAndExercise>> getAllWorkoutExerciseAndExercise(
      long workoutId) {
    return this.workoutExerciseDao.getAllWorkoutExerciseAndExercise(workoutId);
  }

  public void getAllWorkoutExerciseSingle(
      long workoutId, SingleObserver<List<WorkoutExercise>> observer) {
    this.workoutExerciseDao
        .getAllWorkoutExerciseSingle(workoutId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getAllWorkoutExerciseAndExerciseSingle(
      long workoutId, SingleObserver<List<WorkoutExerciseAndExercise>> observer) {
    this.workoutExerciseDao
        .getAllWorkoutExerciseAndExerciseSingle(workoutId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getAllExerciseAndFinishedExercisesSingle(
      SingleObserver<List<ExerciseAndFinishedExercises>> observer) {
    this.exerciseDao
        .getAllFinishedExercisesSingle()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void insertWorkoutExercise(List<WorkoutExercise> l) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.insertAll(l));
  }

  public void insertOrDeleteWorkoutExercises(List<Pair<WorkoutExercise, Boolean>> wes) {
    this.executors
        .getPoolThread()
        .execute(
            () -> {
              this.workoutExerciseDao.insertAll(
                  wes.stream()
                      .filter(e -> e.second)
                      .map(e -> e.first)
                      .collect(Collectors.toList()));
              this.workoutExerciseDao.deleteAll(
                  wes.stream()
                      .filter(e -> !e.second)
                      .map(e -> e.first)
                      .collect(Collectors.toList()));
            });
  }

  public void insertWorkoutExercise(List<WorkoutExercise> l, SingleObserver<List<Long>> observer) {
    this.workoutExerciseDao
        .insertAllSingle(l)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void deleteWorkout(Workout w) {
    this.executors.getPoolThread().execute(() -> this.workoutDao.delete(w));
  }

  public void deleteWorkouts(List<Workout> ws) {
    this.executors.getPoolThread().execute(() -> this.workoutDao.delete(ws));
  }

  public void deleteWorkoutExercise(WorkoutExercise we) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.delete(we));
  }

  public void deleteWorkoutExercises(List<WorkoutExercise> wes) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.deleteAll(wes));
  }

  public LiveData<WorkoutExercise> getWorkoutExercise(long workoutId, long exerciseId) {
    return this.workoutExerciseDao.getWorkoutExercise(workoutId, exerciseId);
  }

  public LiveData<List<WorkoutAndWorkoutExercises>> getAllWorkoutsWithExerciseAndWorkoutExercise() {
    return this.workoutDao.getAllWorkoutsWithExerciseAndWorkoutExercise();
  }

  public void getWorkoutCount(SingleObserver<List<Integer>> singleObserver) {
    this.workoutDao
        .getWorkoutCount()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(singleObserver);
  }
}
