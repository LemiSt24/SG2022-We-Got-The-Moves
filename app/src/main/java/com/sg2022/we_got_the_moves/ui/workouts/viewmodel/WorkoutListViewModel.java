package com.sg2022.we_got_the_moves.ui.workouts.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

import java.util.List;

public class WorkoutListViewModel extends AndroidViewModel {

    private static final String TAG = "WorkoutListViewModel";

    private final WorkoutsRepository repository;
    private final MutableLiveData<List<Workout>> workouts;

    public WorkoutListViewModel(@NonNull final Application app, @NonNull final WorkoutsRepository repository) {
        super(app);
        this.repository = repository;
        this.workouts = new MutableLiveData<List<Workout>>();
    }

    public MutableLiveData<List<Workout>> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(LiveData<List<Workout>> workouts) {
        this.workouts.setValue(workouts.getValue());
    }

    public void postWorkouts(LiveData<List<Workout>> workouts) {
        this.workouts.postValue(workouts.getValue());
    }

    public void loadAllWorkouts(){
        List<Workout> l = this.repository.getAllWorkouts().getValue();
        this.workouts.setValue(l);
    }

    public static class Factory implements ViewModelProvider.Factory {
        @NonNull
        private final Application app;
        private final WorkoutsRepository repository;

        public Factory(@NonNull final Application app) {
            this.app = app;
            this.repository = WorkoutsRepository.getInstance(app);
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new WorkoutListViewModel(app, repository);
        }
    }
}