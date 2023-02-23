package com.sg2022.we_got_the_moves.io;

import android.net.Uri;

import java.io.File;

public class VidItem {
  public String filename;
  public Uri uri;
  public boolean mute;

  public VidItem(File f) {
    this.filename = f.getName();
    this.uri = Uri.fromFile(f);
    this.mute = true;
  }
}
