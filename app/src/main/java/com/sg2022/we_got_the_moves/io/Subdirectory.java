package com.sg2022.we_got_the_moves.io;

public enum Subdirectory {
  Videos,
  Images;
  private static final String[] VIDEO_EXTENSIONS = {"mp4"};
  private static final String[] IMAGES_EXTENSIONS = {"png"};

  public String[] getSupportedFormats() {
    if (this.ordinal() == 0) {
      return VIDEO_EXTENSIONS;
    }
    return IMAGES_EXTENSIONS;
  }
}
