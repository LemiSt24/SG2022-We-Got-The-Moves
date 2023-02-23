package com.sg2022.we_got_the_moves.ui.training.tabs.overview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class TrainingViewModel extends AndroidViewModel {

  public final FinishedWorkoutRepository finishedWorkoutRepository;
  public final WorkoutsRepository workoutsRepository;
  public final ConstraintRepository constraintRepository;

  public TrainingViewModel(
      @NonNull final Application app,
      @NonNull final FinishedWorkoutRepository finishedWorkoutRepository,
      @NonNull final WorkoutsRepository workoutsRepository,
      @NonNull final ConstraintRepository constraintRepository) {
    super(app);
    this.finishedWorkoutRepository = finishedWorkoutRepository;
    this.workoutsRepository = workoutsRepository;
    this.constraintRepository = constraintRepository;
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final Application app;
    private final FinishedWorkoutRepository finishedWorkoutRepository;
    private final WorkoutsRepository workoutsRepository;
    private final ConstraintRepository constraintRepository;

    public Factory(@NonNull final Application app) {
      this.app = app;
      this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(app);
      this.workoutsRepository = WorkoutsRepository.getInstance(app);
      this.constraintRepository = ConstraintRepository.getInstance(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T)
          new TrainingViewModel(
              app, finishedWorkoutRepository, workoutsRepository, constraintRepository);
    }
  }
}
