package com.sg2022.we_got_the_moves.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.db.daos.UserDao;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutsViewModel;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    public UserRepository repository;

    public SettingsViewModel(@NonNull final Application app, @NonNull UserRepository repository) {
        mText = new MutableLiveData<>();


        this.repository = repository;
        //mText.setValue("This is notifications fragment");


    }

    public static class Factory implements ViewModelProvider.Factory {
        @NonNull
        private final Application app;
        private final UserRepository repository;

        public Factory(@NonNull final Application app) {
            this.app = app;
            this.repository = UserRepository.getInstance(app);
        }
        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SettingsViewModel(app, repository);
        }
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<User> getDBUser() {
        return repository.getUser();
    }
}