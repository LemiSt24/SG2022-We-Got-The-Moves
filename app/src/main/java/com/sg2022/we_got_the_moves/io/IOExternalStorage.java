package com.sg2022.we_got_the_moves.io;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class IOExternalStorage extends IOStorage {

  public static final String TAG = "IOExternalStorage";
  private final String rootDirectory;
  private final String TARGET_DIRECTORY = "WeGotTheMoves";

  private final String[] PERMISSIONS = {
    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  public IOExternalStorage(String rootDirectory) {
    super();
    this.rootDirectory = rootDirectory;
  }

  @Override
  public String getRootDirectoryPath() {
    return Environment.getExternalStoragePublicDirectory(this.rootDirectory).getPath();
  }

  @Override
  public String getRootDirectoryName() {
    return this.rootDirectory;
  }

  @Override
  public String getDirectoryPath(Subdirectory subdirectory) {
    return this.getTargetDirectoryPath() + File.separator + subdirectory.name();
  }

  @Override
  public String getRelativeDirectoryPath(Subdirectory subdirectory) {
    return this.getRootDirectoryName()
        + File.separator
        + getTargetDirectoryName()
        + File.separator
        + subdirectory.name();
  }

  public String getTargetDirectoryPath() {
    return this.getRootDirectoryPath() + File.separator + TARGET_DIRECTORY;
  }

  public String getTargetDirectoryName() {
    return this.TARGET_DIRECTORY;
  }

  @Override
  public String[] getPermissions() {
    return this.PERMISSIONS;
  }
}
