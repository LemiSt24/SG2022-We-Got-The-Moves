package com.sg2022.we_got_the_moves.io;

import android.os.FileObserver;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MutableLiveFileData extends MutableLiveData<List<File>> {

  private static final String TAG = "MutableLiveFileData";
  private static final List<Integer> FILE_EVENTS =
      Arrays.asList(
          FileObserver.CREATE,
          FileObserver.DELETE,
          FileObserver.MODIFY,
          FileObserver.MOVED_TO,
          FileObserver.MOVED_FROM,
          FileObserver.ATTRIB,
          FileObserver.MOVE_SELF,
          FileObserver.DELETE_SELF);
  private final FileObserver fo;

  public <T extends IOStorage> MutableLiveFileData(List<File> value, T ios, Subdirectory sub) {
    super(value);
    this.fo =
        new FileObserver(ios.getDirectoryPath(sub)) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            if (FILE_EVENTS.contains(event)) {
              postValue(List.of(ios.readFilesFromSubdirectory(sub)));
            }
          }
        };
    postValue(List.of(ios.readFilesFromSubdirectory(sub)));
  }

  public MutableLiveFileData(IOStorage ios, Subdirectory sub) {
    this(new ArrayList<>(), ios, sub);
  }

  @Override
  protected void onActive() {
    super.onActive();
    this.fo.startWatching();
  }

  @Override
  protected void onInactive() {
    super.onInactive();
    this.fo.stopWatching();
  }
}
