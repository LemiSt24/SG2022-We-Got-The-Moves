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

import java.util.List;

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

    public LiveData<Workout> getWorkout(long id) {
        return this.workoutDao.get(id);
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return this.workoutDao.getAll();
    }

    public LiveData<Exercise> getExercise(long id) {
        return this.exerciseDao.get(id);
    }

    public LiveData<List<Exercise>> getAllExercises() {
        return this.exerciseDao.getAll();
    }

    public LiveData<List<Exercise>> getExercisesByWorkoutId(long id) {
        return this.exerciseDao.getExercisesByWorkoutId(id);
    }

    public LiveData<List<WorkoutExercise>> getAllWorkoutExercises(){
        return this.workoutExerciseDao.getAll();
    }

    public void insertWorkout(Workout w) {
        this.executors.getPoolThread().execute(()-> this.workoutDao.insert(w));
    }

    public void insertExercise(Exercise e) {
        this.executors.getPoolThread().execute(() -> this.exerciseDao.insert(e));
    }

    public void insertWorkoutExercise(WorkoutExercise we) {
        this.executors.getPoolThread().execute(() -> this.workoutExerciseDao.insert(we));
    }

    public void deleteWorkout(Workout w){
        this.executors.getPoolThread().execute(() -> this.workoutDao.delete(w));
    }

    public LiveData<WorkoutExercise> getWorkoutExercise(long workoutId, long exerciseId){
        return this.workoutExerciseDao.get(workoutId, exerciseId);
    }
}