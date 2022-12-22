package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.BasicApp;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExercises;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

import java.util.ArrayList;
import java.util.List;

public class WorkoutsViewModel extends AndroidViewModel {

  private static final String TAG = "WorkoutListViewModel";

  public final WorkoutsRepository repository;
  public MutableLiveData<List<WorkoutAndWorkoutExercises>> data;

  public WorkoutsViewModel(
      @NonNull final Application app,
      @NonNull final WorkoutsRepository repository,
      @NonNull LifecycleOwner owner) {
    super(app);
    this.repository = repository;
    this.data = new MutableLiveData<>(new ArrayList<>());
    this.repository
        .getAllWorkoutsWithExerciseAndWorkoutExercise()
        .observe(
            owner,
            list -> {
              if (list == null) return;
              if (list.isEmpty()) {
                this.data.postValue(new ArrayList<>());
                return;
              }
              this.data.postValue(list);
            });
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final Application app;
    private final WorkoutsRepository repository;
    private final LifecycleOwner owner;

    public Factory(@NonNull final Application app, @NonNull LifecycleOwner owner) {
      this.app = app;
      this.repository = ((BasicApp) app).getWorkoutsRepository();
      this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new WorkoutsViewModel(this.app, this.repository, this.owner);
    }
  }
}
