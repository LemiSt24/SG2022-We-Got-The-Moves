package com.sg2022.we_got_the_moves.ui.workouts.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class ExerciseViewModel extends AndroidViewModel {

    private static final String TAG = "ExerciseViewModel";

    private final WorkoutsRepository repository;
    private final MutableLiveData<Exercise> exercise;

    public ExerciseViewModel(final Application app, WorkoutsRepository repository, final long exerciseId) {
        super(app);
        this.repository = repository;
        this.exercise = new MutableLiveData<>(this.repository.getExercise(exerciseId).getValue());
    }

    public WorkoutsRepository getRepository() {
        return repository;
    }

    public LiveData<Exercise> getExercise() {
        return exercise;
    }

    public void setExercise(LiveData<Exercise> exercise) {
        this.exercise.setValue(exercise.getValue());
    }

    public static class Factory implements ViewModelProvider.Factory {
        @NonNull
        private final Application app;
        private final WorkoutsRepository repository;
        private final long exerciseId;

        public Factory(@NonNull Application app, long exerciseId) {
            this.app = app;
            this.exerciseId = exerciseId;
            this.repository = WorkoutsRepository.getInstance(app);
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ExerciseViewModel(app, repository, exerciseId);
        }
    }
}