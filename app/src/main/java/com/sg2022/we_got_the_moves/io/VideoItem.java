package com.sg2022.we_got_the_moves.io;

import android.net.Uri;
import java.io.File;

public class VideoItem {
  public String filename;
  public Uri uri;
  public boolean mute;

  public VideoItem(File f) {
    this.filename = f.getName();
    this.uri = Uri.fromFile(f);
    this.mute = true;
  }
}