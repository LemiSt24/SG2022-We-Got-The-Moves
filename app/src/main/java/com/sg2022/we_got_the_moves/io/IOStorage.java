package com.sg2022.we_got_the_moves.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class IOStorage {

  public abstract String getRootDirectoryPath();

  public abstract void createDirectory(Subdirectory sub);

  public abstract void createDirectories() throws Exception;

  public abstract String getDirectoryPath(Subdirectory sub);

  public File[] readFilesFromSubdirectory(Subdirectory sub) {
    createDirectory(sub);
    File f = new File(this.getDirectoryPath(sub));
    return f.listFiles(
        file -> {
          for (String format : sub.getSupportedFormats()) {
            if (file.getName().endsWith(format)) return true;
          }
          return false;
        });
  }

  public void writeFileToDirectory(Subdirectory sub, byte[] data, String filename) {
    try {
      createDirectory(sub);
      File f = new File(this.getDirectoryPath(sub), filename);
      FileOutputStream fileOutputStream = null;
      try {
        fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(data);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (fileOutputStream != null) {
          try {
            fileOutputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
