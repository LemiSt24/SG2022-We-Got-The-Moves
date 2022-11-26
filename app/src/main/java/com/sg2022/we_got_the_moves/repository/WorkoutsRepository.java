package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.daos.WorkoutDao;
import com.sg2022.we_got_the_moves.db.daos.WorkoutExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.MaybeObserver;
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
          AppDatabase db = AppDatabase.getInstance(app.getApplicationContext());
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

  public LiveData<List<Exercise>> getExercises(long workoutId) {
    return this.exerciseDao.getAllExercises(workoutId);
  }

  public LiveData<List<WorkoutExerciseAndExercise>> getAllWorkoutExercise(long workoutId) {
    return this.workoutExerciseDao.getAllWorkoutExercise(workoutId);
  }

  public void getAllNotContainedExercise(long workoutId, MaybeObserver<List<Exercise>> observer) {
    this.exerciseDao
        .getAllNotContainedExercisesMaybe(workoutId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getAllWorkoutExercise(long id, MaybeObserver<List<WorkoutExercise>> observer) {
    this.workoutExerciseDao
        .getAllWorkoutExerciseMaybe(id)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void insertWorkoutExercise(List<WorkoutExercise> l) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.insertAll(l));
  }

  public void insertWorkoutExercise(List<WorkoutExercise> l, MaybeObserver<List<Long>> observer) {
    this.workoutExerciseDao
        .insertAllMaybe(l)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void deleteWorkout(Workout w) {
    this.executors.getPoolThread().execute(() -> this.workoutDao.delete(w));
  }

  public void deleteWorkoutExercise(WorkoutExercise we) {
    this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.delete(we));
  }

  public LiveData<WorkoutExercise> getWorkoutExercise(long workoutId, long exerciseId) {
    return this.workoutExerciseDao.getWorkoutExercise(workoutId, exerciseId);
  }

  public LiveData<List<WorkoutAndWorkoutExerciseAndExercise>>
      getAllWorkoutsWithExerciseAndWorkoutExercise() {
    return this.workoutDao.getAllWorkoutsWithExerciseAndWorkoutExercise();
  }
}
