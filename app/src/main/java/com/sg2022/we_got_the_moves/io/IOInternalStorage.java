package com.sg2022.we_got_the_moves.io;

import android.app.Application;

import androidx.annotation.NonNull;

import java.io.File;

public class IOInternalStorage extends IOStorage {

  public static final String TAG = "IOInternalStorage";
  private static volatile IOInternalStorage INSTANCE;
  private final Application app;

  public IOInternalStorage(@NonNull Application app) {
    this.app = app;
    createDirectories();
  }

  public static IOInternalStorage getInstance(@NonNull Application app) {
    if (INSTANCE == null) {
      synchronized (IOInternalStorage.class) {
        if (INSTANCE == null) {
          INSTANCE = new IOInternalStorage(app);
        }
      }
    }
    return INSTANCE;
  }

  public String getRootDirectoryPath() {
    return this.app.getFilesDir().getPath();
  }

  public String getDirectoryPath(Subdirectory sub) {
    return this.getRootDirectoryPath() + File.separator + sub.name();
  }

  public void createDirectory(Subdirectory sub) {
    File f = new File(getDirectoryPath(sub));
    if (!f.exists()) {
      //noinspection ResultOfMethodCallIgnored
      f.mkdirs();
    }
  }

  public void createDirectories() {
    for (Subdirectory sub : Subdirectory.values()) {
      File f = new File(getDirectoryPath(sub));
      if (!f.exists()) {
        //noinspection ResultOfMethodCallIgnored
        f.mkdirs();
      }
    }
  }
}
