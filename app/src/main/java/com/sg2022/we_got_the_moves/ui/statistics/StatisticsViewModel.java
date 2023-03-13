package com.sg2022.we_got_the_moves.ui.statistics;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class StatisticsViewModel extends AndroidViewModel {

  public final FinishedWorkoutRepository finishedWorkoutRepository;
  public final UserRepository userRepository;
  public final WorkoutsRepository workoutsRepository;

  public StatisticsViewModel(
      @NonNull final Application app,
      @NonNull final FinishedWorkoutRepository finishedWorkoutRepository,
      @NonNull final UserRepository userRepository,
      @NonNull final WorkoutsRepository workoutsRepository) {
    super(app);
    this.finishedWorkoutRepository = finishedWorkoutRepository;
    this.userRepository = userRepository;
    this.workoutsRepository = workoutsRepository;
  }

  public static class Factory implements ViewModelProvider.Factory {

    public final FinishedWorkoutRepository finishedWorkoutRepository;
    public final UserRepository userRepository;
    public final WorkoutsRepository workoutsRepository;
    private final Application app;

    public Factory(@NonNull final Application app) {
      this.app = app;
      this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(app);
      this.userRepository = UserRepository.getInstance(app);
      this.workoutsRepository = WorkoutsRepository.getInstance(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T)
          new StatisticsViewModel(
              app, finishedWorkoutRepository, userRepository, workoutsRepository);
    }
  }
}
