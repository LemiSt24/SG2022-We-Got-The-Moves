package com.sg2022.we_got_the_moves.ui.workouts.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class WorkoutsViewModel extends AndroidViewModel {

    private static final String TAG = "WorkoutListViewModel";

    private final WorkoutsRepository repository;

    public WorkoutsViewModel(@NonNull final Application app, @NonNull final WorkoutsRepository repository) {
        super(app);
        this.repository = repository;
    }

    public WorkoutsRepository getRepository() {
        return repository;
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
            return (T) new WorkoutsViewModel(app, repository);
        }
    }
}