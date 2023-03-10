package com.sg2022.we_got_the_moves.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.io.IOInternalStorage;
import com.sg2022.we_got_the_moves.io.IOStorage;
import com.sg2022.we_got_the_moves.io.MutableLiveVideoItemData;
import com.sg2022.we_got_the_moves.io.Subdirectory;
import com.sg2022.we_got_the_moves.io.VideoItem;
import java.util.List;

public class FileRepository {

  private static final String TAG = "FileRepository";
  private static volatile FileRepository INSTANCE;
  private final IOStorage defaultStorage;
  private final AppExecutors executors;

  private FileRepository(@NonNull Application app) {
    this.executors = AppExecutors.getInstance();
    this.defaultStorage = new IOInternalStorage(app);
  }

  public static FileRepository getInstance(@NonNull Application app) {
    if (INSTANCE == null) {
      synchronized (FileRepository.class) {
        if (INSTANCE == null) {
          INSTANCE = new FileRepository(app);
        }
      }
    }
    return INSTANCE;
  }

  public LiveData<List<VideoItem>> getAllVideoItemsDefault() {
    return new MutableLiveVideoItemData(this.defaultStorage, Subdirectory.Videos);
  }

  public String getDirectoryPathDefault(Subdirectory subdirectory) {
    return this.defaultStorage.createDirectory(subdirectory);
  }

  public String[] getPermissionsDefault() {
    return this.defaultStorage.getPermissions();
  }
}
