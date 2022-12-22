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

  @ColumnInfo(name = "isCountable")
  public boolean isCountable;

  public Exercise(
      long id,
      String name,
      String instruction,
      String youtubeId,
      int imageId,
      boolean isCountable) {
    this.id = id;
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.imageId = imageId;
    this.isCountable = isCountable;
  }

  @Ignore
  public Exercise(
      String name, String instruction, String youtubeId, int imageId, boolean isCountable) {
    this(0, name, instruction, youtubeId, imageId, isCountable);
  }

  @Ignore
  public Exercise(String name, String instruction, int imageId, boolean isCountable) {
    this(name, instruction, null, imageId, isCountable);
  }

  @Ignore
  public Exercise(String name, String instruction, boolean isCountable) {
    this(name, instruction, 0, isCountable);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof Exercise)) return false;
    return this.id == ((Exercise) obj).id && Objects.equals(this.name, ((Exercise) obj).name);
  }
}
