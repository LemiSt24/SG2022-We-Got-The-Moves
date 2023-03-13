package com.sg2022.we_got_the_moves.ui.training.tabs.playback;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.sg2022.we_got_the_moves.repository.FileRepository;

public class FileViewModel extends AndroidViewModel {

  private static final String TAG = "FileViewModel";

  public final FileRepository repository;

  public FileViewModel(@NonNull final Application app, @NonNull final FileRepository repository) {
    super(app);
    this.repository = repository;
  }

  public static class Factory implements ViewModelProvider.Factory {
    private final Application app;
    private final FileRepository repository;

    public Factory(@NonNull final Application app) {
      this.app = app;
      this.repository = FileRepository.getInstance(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new FileViewModel(this.app, this.repository);
    }
  }
}
