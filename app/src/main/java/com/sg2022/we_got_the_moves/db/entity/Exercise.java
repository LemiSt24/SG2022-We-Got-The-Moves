package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.sg2022.we_got_the_moves.R;

import java.util.Objects;

import javax.annotation.Nullable;

@Entity(tableName = "Exercise")
public class Exercise {

  @ColumnInfo(name = "count")
  public COUNT count;

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "name")
  public String name;

  @ColumnInfo(name = "instruction")
  public String instruction;

  @ColumnInfo(name = "youtubeId", defaultValue = "" + R.string.test_video)
  public String youtubeId;

  @ColumnInfo(name = "imageId", defaultValue = "" + R.drawable.no_image)
  public int imageId;

  public Exercise(
      long id,
      String name,
      String instruction,
      String youtubeId,
      int imageId,
      Exercise.COUNT count) {
    this.id = id;
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.imageId = imageId;
    this.count = count;
  }

  @Ignore
  public Exercise(String name, String instruction, String youtubeId, int imageId, COUNT count) {
    this(0, name, instruction, youtubeId, imageId, count);
  }

  @Ignore
  public Exercise(String name, String instruction, int imageId, COUNT count) {
    this(name, instruction, null, imageId, count);
  }

  @Ignore
  public Exercise(String name, String instruction, COUNT count) {
    this(name, instruction, R.drawable.no_image, count);
  }

  public boolean isCountable() {
    return this.count == COUNT.REPETITION;
  }

  public enum COUNT {
    REPETITION,
    DURATION
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof Exercise)) return false;
    return this.id == ((Exercise) obj).id && Objects.equals(this.name, ((Exercise) obj).name);
  }
}
