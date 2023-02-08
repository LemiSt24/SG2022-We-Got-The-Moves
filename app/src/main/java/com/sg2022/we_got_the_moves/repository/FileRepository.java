package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;

import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.io.IOExternalStorage;
import com.sg2022.we_got_the_moves.io.IOInternalStorage;
import com.sg2022.we_got_the_moves.io.MutableLiveFileData;
import com.sg2022.we_got_the_moves.io.Subdirectory;

public class FileRepository {

  private static final String TAG = "FileRepository";
  private static volatile FileRepository INSTANCE;
  private final AppExecutors executors;
  private final IOInternalStorage internalStorage;
  private final IOExternalStorage externalStorage;

  private FileRepository(@NonNull Application app) {
    this.executors = AppExecutors.getInstance();
    this.internalStorage = IOInternalStorage.getInstance(app);
    this.externalStorage = IOExternalStorage.getInstance();
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

  public MutableLiveFileData getAllReplaysInternal(Subdirectory sub) {
    return new MutableLiveFileData(this.internalStorage, sub);
  }

  public MutableLiveFileData getAllReplaysExternal(Subdirectory sub) {
    return new MutableLiveFileData(this.externalStorage, sub);
  }
}
