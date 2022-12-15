package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

import javax.annotation.Nullable;

@Entity(tableName = "Exercise")
public class Exercise {

  @PrimaryKey(autoGenerate = true)
  public long id;

  @ColumnInfo(name = "name")
  public String name;

  private String textInstruction;
  private String videoInstruction;
  private Boolean isCountable; //true if repetions can be counted -> non static exercise

  public Exercise(long id, String name, String textInstruction, String videoInstruction, Boolean isCountable) {
    this.id = id;
    this.name = name;
    this.textInstruction = textInstruction;
    this.videoInstruction = videoInstruction;
    this.isCountable = isCountable;
  }

  @Nullable
  public String getTextInstruction() {
    return textInstruction;
  }

  public void setTextInstruction(@Nullable String textInstruction) {
    this.textInstruction = textInstruction;
  }

  @Nullable
  public String getVideoInstruction() {
    return videoInstruction;
  }

  public void setVideoInstruction(@Nullable String videoInstruction) {
    this.videoInstruction = videoInstruction;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof Exercise)) return false;
    return this.id == ((Exercise) obj).id && Objects.equals(this.name, ((Exercise) obj).name);
  }

  public Boolean getCountable() {
    return isCountable;
  }
}
