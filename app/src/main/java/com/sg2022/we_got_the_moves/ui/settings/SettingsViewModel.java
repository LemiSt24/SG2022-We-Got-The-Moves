package com.sg2022.we_got_the_moves.ui.settings;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.sg2022.we_got_the_moves.repository.UserRepository;

public class SettingsViewModel extends ViewModel {

  public final UserRepository userRepository;

  public SettingsViewModel(@NonNull final Application app, @NonNull UserRepository repository) {
    this.userRepository = repository;
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Application app;
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
}
