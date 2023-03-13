package com.sg2022.we_got_the_moves.io;

import android.util.Log;
import java.io.File;

public abstract class IOStorage {

  public static final String TAG = "IOStorage";

  public abstract String getRootDirectoryPath();

  public abstract String getDirectoryPath();

  public abstract String[] getPermissions();

  public String createDirectory() {
    File f = new File(this.getDirectoryPath());
    if (!f.exists()) {
      if (!f.mkdirs()) Log.e(TAG, "Couldn't create folders");
    }
    return f.getPath();
  }

  public File[] readFiles() {
    File f = new File(this.createDirectory());
    return f.listFiles();
  }
}
