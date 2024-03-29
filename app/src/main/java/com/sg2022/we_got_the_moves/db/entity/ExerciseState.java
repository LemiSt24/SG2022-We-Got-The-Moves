package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.List;

@Entity(
    tableName = "ExerciseState",
    primaryKeys = {"id", "exerciseId"},
    foreignKeys = {
      @ForeignKey(
          entity = Exercise.class,
          parentColumns = "id",
          childColumns = "exerciseId",
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.CASCADE),
    })
public class ExerciseState {
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "exerciseId", index = true)
  public long exerciseId;

  @ColumnInfo(name = "constraintIds", index = true)
  public List<Long> constraintIds;

  @ColumnInfo(name = "enterStateLandmarkStart")
  public String enterStateLandmarkStart;

  @ColumnInfo(name = "enterStateLandmarkMid")
  public String enterStateLandmarkMid;

  @ColumnInfo(name = "enterStateLandmarkEnd")
  public String enterStateLandmarkEnd;

  @ColumnInfo(name = "comparator")
  public ExerciseState.COMPARATOR comparator;

  @ColumnInfo(name = "compareAngle")
  public int compareAngle;

  @ColumnInfo(name = "insignificantDimension")
  public ExerciseState.INSIGNIFICANT_DIMENSION insignificantDimension;

  @ColumnInfo(name = "stateTime")
  public Long stateTime;

  public ExerciseState(
      long id,
      long exerciseId,
      List<Long> constraintIds,
      String enterStateLandmarkStart,
      String enterStateLandmarkMid,
      String enterStateLandmarkEnd,
      ExerciseState.COMPARATOR comparator,
      int compareAngle,
      ExerciseState.INSIGNIFICANT_DIMENSION insignificantDimension,
      Long stateTime) {
    this.id = id;
    this.exerciseId = exerciseId;
    this.constraintIds = constraintIds;
    this.enterStateLandmarkStart = enterStateLandmarkStart;
    this.enterStateLandmarkMid = enterStateLandmarkMid;
    this.enterStateLandmarkEnd = enterStateLandmarkEnd;
    this.comparator = comparator;
    this.compareAngle = compareAngle;
    this.insignificantDimension = insignificantDimension;
    this.stateTime = stateTime;
  }

  public enum COMPARATOR {
    LESS,
    GREATER
  }

  public enum INSIGNIFICANT_DIMENSION {
    X,
    Y,
    Z
  }
}
