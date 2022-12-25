package com.sg2022.we_got_the_moves.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.BasicApp;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;

public class StatisticsViewModel extends AndroidViewModel {

  public final FinishedWorkoutRepository finishedWorkoutRepository;
  public final UserRepository userRepository;
  public final LifecycleOwner owner;

  public StatisticsViewModel(
      @NonNull final Application app,
      @NonNull final FinishedWorkoutRepository finishedWorkoutRepository,
      @NonNull final UserRepository userRepository,
      @NonNull LifecycleOwner owner) {
    super(app);
    this.finishedWorkoutRepository = finishedWorkoutRepository;
    this.userRepository = userRepository;
    this.owner = owner;
  }

  public static class Factory implements ViewModelProvider.Factory {

    public final FinishedWorkoutRepository finishedWorkoutRepository;
    public final UserRepository userRepository;
    public final LifecycleOwner owner;
    private final Application app;

    public Factory(@NonNull final Application app, @NonNull LifecycleOwner owner) {
      this.app = app;
      this.finishedWorkoutRepository = ((BasicApp) app).getFinishedTrainingRepository();
      this.userRepository = ((BasicApp) app).getUserRepository();
      this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new StatisticsViewModel(app, finishedWorkoutRepository, userRepository, owner);
    }
  }
}
