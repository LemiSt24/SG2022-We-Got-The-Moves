package com.sg2022.we_got_the_moves.ui.workouts.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.BasicApp;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class WorkoutViewModel extends AndroidViewModel {

    private static final String TAG = "WorkoutViewModel";

    private final WorkoutsRepository repository;
    private final MutableLiveData<Workout> workout;

    public WorkoutViewModel(@NonNull final Application app, final long workoutId) {
        super(app);
        this.repository = ((BasicApp) app).getWorkoutsRepository();
        this.workout = new MutableLiveData<Workout>(this.repository.getWorkout(workoutId).getValue());
    }

    public LiveData<Workout> getWorkout() {
        return workout;
    }

    public void setWorkout(LiveData<Workout> workout) {
        this.workout.setValue(workout.getValue());
    }


    public static class Factory implements ViewModelProvider.Factory {
        @NonNull
        private final Application app;
        private final long workoutId;

        public Factory(@NonNull Application app, long workoutId) {
            this.app = app;
            this.workoutId = workoutId;
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new WorkoutViewModel(app, workoutId);
        }
    }
}