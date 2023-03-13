package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.sg2022.we_got_the_moves.R;
import java.util.List;
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

  @ColumnInfo(name = "youtubeId", defaultValue = "" + R.string.youtube_video_placeholder)
  public String youtubeId;

  @ColumnInfo(name = "imageId", defaultValue = "" + R.drawable.placeholder)
  public int imageId;

  @ColumnInfo(name = "unit")
  public Exercise.UNIT unit;

  @ColumnInfo(name = "met")
  public float met; // (1 MET = 3.5 ml·kg^-1·min^-1 = 1 kcal·kg^-1·hour^-1)

  @ColumnInfo(name = "exerciseStates")
  public List<ExerciseState> exerciseStates;

  public Exercise(
      long id,
      String name,
      String instruction,
      String youtubeId,
      int imageId,
      Exercise.UNIT unit,
      float met,
      List<ExerciseState> exerciseStates) {
    this.id = id;
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.imageId = imageId;
    this.unit = unit;
    this.met = met;
    this.exerciseStates = exerciseStates;
  }

  @Ignore
  public Exercise(
      String name,
      String instruction,
      String youtubeId,
      Exercise.UNIT unit,
      float met,
      List<ExerciseState> exerciseStates) {
    this.name = name;
    this.instruction = instruction;
    this.youtubeId = youtubeId;
    this.unit = unit;
    this.met = met;
    this.exerciseStates = exerciseStates;
  }

  public boolean isCountable() {
    return this.unit == UNIT.REPETITION;
  }

  public float getKCal(
      float weight, // [kg]
      float duration) // [secs]
      {
    return this.met * duration / 3600 * weight;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof Exercise)) return false;
    return this.id == ((Exercise) obj).id && Objects.equals(this.name, ((Exercise) obj).name);
  }

  public enum UNIT {
    REPETITION,
    DURATION
  }
}
