package com.sg2022.we_got_the_moves.io;

public enum Subdirectory {
  TEXT,
  VIDEOS,
  THUMBNAILS;

  public static final String[] TEXT_FORMATS = {".txt", ".csv"};
  public static final String[] VIDEO_FORMATS = {".mp4"};
  public static final String[] THUMBNAIL_FORMATS = {".bmp", ".png"};

  public String[] getSupportedFormats() {
    switch (this.ordinal()) {
      case 0:
        return TEXT_FORMATS;
      case 1:
        return VIDEO_FORMATS;
      default:
        return THUMBNAIL_FORMATS;
    }
  }
}
