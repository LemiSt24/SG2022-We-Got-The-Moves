package com.sg2022.we_got_the_moves.io;

import android.os.Environment;

import com.sg2022.we_got_the_moves.BuildConfig;

import java.io.File;

public class IOExternalStorage extends IOStorage {

  public static final String TAG = "IOExternalStorage";
  public static String PUBLIC_DIRECTORY = Environment.DIRECTORY_DOWNLOADS;
  public static String TARGET_DIRECTORY = BuildConfig.APPLICATION_ID;
  private static volatile IOExternalStorage INSTANCE;

  public static IOExternalStorage getInstance() {
    if (INSTANCE == null) {
      synchronized (IOExternalStorage.class) {
        if (INSTANCE == null) {
          INSTANCE = new IOExternalStorage();
        }
      }
    }
    return INSTANCE;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean hasExternalStorageReadWriteAccess() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  public String getRootDirectoryPath() {
    return Environment.getExternalStoragePublicDirectory(PUBLIC_DIRECTORY).getPath();
  }

  public String getTargetDirectoryPath() {
    return this.getRootDirectoryPath() + File.separator + TARGET_DIRECTORY;
  }

  public String getDirectoryPath(Subdirectory sub) {
    return this.getTargetDirectoryPath() + File.separator + sub.name();
  }

  public void createDirectories() throws Exception {
    if (!hasExternalStorageReadWriteAccess()) {
      throw new Exception();
    }
    for (Subdirectory sub : Subdirectory.values()) {
      File f = new File(getDirectoryPath(sub));
      if (!f.exists()) {
        //noinspection ResultOfMethodCallIgnored
        f.mkdirs();
      }
    }
  }

  public void createDirectory(Subdirectory sub) {
    if (!hasExternalStorageReadWriteAccess()) {
      try {
        throw new Exception();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    File f = new File(getDirectoryPath(sub));
    if (!f.exists()) {
      //noinspection ResultOfMethodCallIgnored
      f.mkdirs();
    }
  }
}
