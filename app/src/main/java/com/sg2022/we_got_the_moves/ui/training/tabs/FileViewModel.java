package com.sg2022.we_got_the_moves.ui.training.tabs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.repository.FileRepository;

public class FileViewModel extends AndroidViewModel {

  public final FileRepository fileRepository;

  public FileViewModel(
      @NonNull final Application app, @NonNull final FileRepository fileRepository) {
    super(app);
    this.fileRepository = fileRepository;
  }

  public static class Factory implements ViewModelProvider.Factory {
    private final Application app;
    private final FileRepository fileRepository;

    public Factory(@NonNull final Application app) {
      this.app = app;
      this.fileRepository = FileRepository.getInstance(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new FileViewModel(app, fileRepository);
    }
  }
}
