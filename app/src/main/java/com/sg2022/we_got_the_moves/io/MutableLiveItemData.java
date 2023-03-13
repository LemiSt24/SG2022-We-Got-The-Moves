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
import java.util.Objects;
import java.util.stream.Collectors;

public class MutableLiveItemData extends MutableLiveData<List<VideoItem>> {

  private static final String TAG = "MutableLiveFileData";
  public final IOStorage ios;
  private final int[] relevantEvents = {
    FileObserver.CREATE,
    FileObserver.DELETE,
    FileObserver.DELETE_SELF,
    // FileObserver.MODIFY,
    FileObserver.MOVED_FROM,
    FileObserver.MOVED_TO,
    FileObserver.MOVE_SELF,
  };
  private final Subdirectory subdirectory;
  private FileObserver fileObserver;

  public MutableLiveItemData(List<VideoItem> value, IOStorage ios, Subdirectory subdirectory) {
    super(value);
    this.ios = ios;
    this.subdirectory = subdirectory;
    this.setValue(new ArrayList<>());
    this.setupFileObserver();
  }

  public MutableLiveItemData(IOStorage ios, Subdirectory subdirectory) {
    this(new ArrayList<>(), ios, subdirectory);
  }

  private List<VideoItem> filesToItems(List<File> files) {
    return files.stream().map(VideoItem::new).collect(Collectors.toList());
  }

  private void setupFileObserver() {
    String directoryPath = ios.getDirectoryPath(this.subdirectory);
    this.fileObserver =
        new FileObserver(directoryPath) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            if (!Objects.equals(path, subdirectory.name())) {
              for (int e : relevantEvents) {
                if (e == event) {
                  postValue(filesToItems(List.of(ios.readFiles(subdirectory))));
                  break;
                }
              }
            }
          }
        };
  }

  @Override
  public void observe(
      @NonNull LifecycleOwner owner, @NonNull Observer<? super List<VideoItem>> observer) {
    super.observe(owner, observer);
    observer.onChanged(this.filesToItems(List.of(ios.readFiles(subdirectory))));
  }

  @Override
  public void observeForever(@NonNull Observer<? super List<VideoItem>> observer) {
    super.observeForever(observer);
    observer.onChanged(this.filesToItems(List.of(ios.readFiles(subdirectory))));
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
