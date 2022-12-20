package com.sg2022.we_got_the_moves.ui.training;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.BasicApp;
import com.sg2022.we_got_the_moves.repository.FinishedTrainingRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class TrainingViewModel extends AndroidViewModel {

  public final FinishedTrainingRepository repository;
  public final WorkoutsRepository workoutsRepository;

  public TrainingViewModel(
      @NonNull final Application app,
      @NonNull final FinishedTrainingRepository repository,
      @NonNull final WorkoutsRepository workoutsRepository,
      @NonNull LifecycleOwner owner) {
    super(app);
    this.repository = repository;
    this.workoutsRepository = workoutsRepository;
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final Application app;
    private final FinishedTrainingRepository repository;
    private final WorkoutsRepository workoutsRepository;
    private final LifecycleOwner owner;

    public Factory(@NonNull final Application app, @NonNull LifecycleOwner owner) {
      this.app = app;
      this.repository = ((BasicApp) app).getFinishedTrainingRepository();
      this.workoutsRepository = ((BasicApp) app).getWorkoutsRepository();
      this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new TrainingViewModel(app, repository, workoutsRepository, owner);
    }
  }
}
