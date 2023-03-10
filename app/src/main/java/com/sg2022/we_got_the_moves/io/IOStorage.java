package com.sg2022.we_got_the_moves.io;

import android.util.Log;
import java.io.File;

public abstract class IOStorage {

  public static final String TAG = "IOStorage";

  public abstract String getRootDirectoryPath();

  public abstract String getDirectoryPath(Subdirectory subdirectory);

  public abstract String[] getPermissions();

  public String createDirectory(Subdirectory subdirectory) {
    File f = new File(this.getDirectoryPath(subdirectory));
    if (!f.exists()) {
      if (!f.mkdirs()) Log.e(TAG, "Couldn't create folders");
    }
    return f.getPath();
  }

  public File[] readFiles(Subdirectory subdirectory) {
    File f = new File(this.createDirectory(subdirectory));
    return f.listFiles();
  }
}
