package com.sg2022.we_got_the_moves.io;

import android.os.FileObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MutableLiveVideoItemData extends MutableLiveData<List<VideoItem>> {

  private static final String TAG = "MutableLiveFileData";
  public final IOStorage ios;
  private final int[] relevantEvents = {
    FileObserver.CREATE,
    FileObserver.DELETE,
    FileObserver.DELETE_SELF,
    FileObserver.CLOSE_WRITE,
    FileObserver.MODIFY,
    FileObserver.MOVED_FROM,
    FileObserver.MOVED_TO,
    FileObserver.MOVE_SELF,
  };
  private FileObserver fileObserver;

  public MutableLiveVideoItemData(List<VideoItem> value, IOStorage ios) {
    super(value);
    this.ios = ios;
    this.setValue(new ArrayList<>());
    this.setupFileObserver();
  }

  public MutableLiveVideoItemData(IOStorage ios) {
    this(new ArrayList<>(), ios);
  }

  private List<VideoItem> filesToItems(List<File> files) {
    return files.stream().map(VideoItem::new).collect(Collectors.toList());
  }

  private void setupFileObserver() {
    String directoryPath = ios.getDirectoryPath();
    this.fileObserver =
        new FileObserver(directoryPath) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            for (int e : relevantEvents) {
              if (e == event) {
                postValue(filesToItems(List.of(ios.readFiles())));
                break;
              }
            }
          }
        };
  }

  @Override
  public void observe(
      @NonNull LifecycleOwner owner, @NonNull Observer<? super List<VideoItem>> observer) {
    super.observe(owner, observer);
    observer.onChanged(this.filesToItems(List.of(ios.readFiles())));
  }

  @Override
  public void observeForever(@NonNull Observer<? super List<VideoItem>> observer) {
    super.observeForever(observer);
    observer.onChanged(this.filesToItems(List.of(ios.readFiles())));
  }

  @Override
  protected void onActive() {
    super.onActive();
    this.fileObserver.startWatching();
  }

  @Override
  protected void onInactive() {
    this.fileObserver.stopWatching();
  }
}
