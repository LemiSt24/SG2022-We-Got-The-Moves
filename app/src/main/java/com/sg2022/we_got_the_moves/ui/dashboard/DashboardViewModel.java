package com.sg2022.we_got_the_moves.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

  private static final String TAG = "DashboardListViewModel";

  public final WorkoutsRepository repository;
  public MutableLiveData<List<Exercise>> data;

  public DashboardViewModel(
      @NonNull final Application app,
      @NonNull final WorkoutsRepository repository,
      @NonNull final LifecycleOwner owner) {
    super(app);
    this.repository = repository;
    this.data = new MutableLiveData<>(new ArrayList<>());
    this.repository
        .getAllExercises()
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

  public WorkoutsRepository getRepository() {
    return repository;
  }

  public static class Factory implements ViewModelProvider.Factory {
    @NonNull private final Application app;
    @NonNull private final WorkoutsRepository repository;
    @NonNull private final LifecycleOwner owner;

    public Factory(@NonNull final Application app, @NonNull LifecycleOwner owner) {
      this.app = app;
      this.repository = WorkoutsRepository.getInstance(app);
      this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new DashboardViewModel(this.app, this.repository, this.owner);
    }
  }
}
