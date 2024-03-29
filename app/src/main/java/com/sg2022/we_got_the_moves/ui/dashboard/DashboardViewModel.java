package com.sg2022.we_got_the_moves.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class DashboardViewModel extends AndroidViewModel {

  private static final String TAG = "DashboardListViewModel";

  public final WorkoutsRepository repository;

  public DashboardViewModel(
      @NonNull final Application app, @NonNull final WorkoutsRepository repository) {
    super(app);
    this.repository = repository;
  }

  public WorkoutsRepository getRepository() {
    return repository;
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Application app;
    @NonNull private final WorkoutsRepository repository;

    public Factory(@NonNull final Application app) {
      this.app = app;
      this.repository = WorkoutsRepository.getInstance(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new DashboardViewModel(this.app, this.repository);
    }
  }
}
