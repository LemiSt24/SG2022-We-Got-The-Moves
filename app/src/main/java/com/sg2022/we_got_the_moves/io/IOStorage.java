package com.sg2022.we_got_the_moves.io;



import java.io.File;

public abstract class IOStorage {

  public abstract String getRootDirectoryPath();

  public abstract String getRootDirectoryName();

  public abstract String getDirectoryPath();

  public abstract String getRelativeDirectoryPath();

  public abstract String[] getPermissions();

  public String createDirectory() {
    File f = new File(this.getDirectoryPath());
    if (!f.exists()) {
      //noinspection ResultOfMethodCallIgnored
      f.mkdirs();
    }
    return f.getPath();
  }

  public File[] readFiles() {
    File f = new File(this.createDirectory());
    return f.listFiles();
  }
}
