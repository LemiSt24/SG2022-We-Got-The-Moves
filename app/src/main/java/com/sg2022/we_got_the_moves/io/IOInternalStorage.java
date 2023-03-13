package com.sg2022.we_got_the_moves.io;

import android.app.Application;

public class IOInternalStorage extends IOStorage {

  public static final String TAG = "IOInternalStorage";

  private final Application app;

  public IOInternalStorage(Application app) {
    super();
    this.app = app;
  }

  @Override
  public String getRootDirectoryPath() {
    return this.app.getFilesDir().getPath();
  }

  public String getDirectoryPath() {
    return this.getRootDirectoryPath();
  }

  @Override
  public String[] getPermissions() {
    return new String[] {};
  }
}
