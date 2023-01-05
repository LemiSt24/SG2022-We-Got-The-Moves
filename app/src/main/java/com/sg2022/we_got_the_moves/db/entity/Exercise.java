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

  @ColumnInfo(name = "unit")
  public Exercise.UNIT unit;

  @ColumnInfo(name = "totalAmount", defaultValue = "0")
  public int totalAmount;

  @ColumnInfo(name = "totalTime", defaultValue = "0")
  public int totalTime;

  @ColumnInfo(name = "met")
  public double met;

  public Exercise(
      long id,
      String name,
      String instruction,
      String youtubeId,
      int imageId,
      Exercise.UNIT unit,
      int totalAmount,
      int totalTime,
      double met) {
    this.id = id;
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.imageId = imageId;
    this.unit = unit;
    this.totalAmount = totalAmount;
    this.totalTime = totalTime;
    this.met = met; // (1 MET = 3.5 ml·kg^-1·min^-1)
  }

  @Ignore
  public Exercise(
      String name,
      String instruction,
      String youtubeId,
      String imageId,
      Exercise.UNIT unit,
      double met) {
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.unit = unit;
    this.met = met;
  }

  public boolean isCountable() {
    return this.unit == UNIT.REPETITION;
  }

  public double getCalories(
      double weight, // weight [kg]
      double duration) // [secs]
      {

    return duration / 60 * (this.met * 3.5f) * (weight / 200);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof Exercise)) return false;
    return this.id == ((Exercise) obj).id && Objects.equals(this.name, ((Exercise) obj).name);
  }

  public enum UNIT {
    REPETITION,
    DURATION // [secs]
  }
}
