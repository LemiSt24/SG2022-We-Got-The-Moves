package com.sg2022.we_got_the_moves.io;

import android.graphics.Bitmap;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class IOStorage {

  public abstract String getRootDirectoryPath();

  public abstract String getRootDirectoryName();

  public abstract String getDirectoryPath(Subdirectory subdirectory);

  public abstract String getRelativeDirectoryPath(Subdirectory subdirectory);

  public abstract String[] getPermissions();

  public String createDirectory(Subdirectory subdirectory) {
    File f = new File(this.getDirectoryPath(subdirectory));
    if (!f.exists()) {
      //noinspection ResultOfMethodCallIgnored
      f.mkdirs();
    }
    return f.getPath();
  }

  public File[] readFiles(Subdirectory subdirectory) {
    File f = new File(this.createDirectory(subdirectory));
    return f.listFiles();
  }

  public File[] filterFiles(Subdirectory subdirectory) {
    File f = new File(this.createDirectory(subdirectory));
    return f.listFiles(
        file -> {
          for (String format : subdirectory.getSupportedFormats()) {
            if (FilenameUtils.getExtension(file.getName()).equals(format)) return true;
          }
          return false;
        });
  }

  public void writeImage(Bitmap bmp, String filename, Subdirectory subdirectory) {
    createDirectory(subdirectory);
    FileOutputStream fos;
    try {
      File f = new File(getDirectoryPath(subdirectory), filename);
      fos = new FileOutputStream(f);
      bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
