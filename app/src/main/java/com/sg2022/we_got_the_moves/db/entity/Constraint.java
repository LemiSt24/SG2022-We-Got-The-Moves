package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import javax.annotation.Nullable;

@Entity(tableName = "Constraint")
public class Constraint {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "from1")
  public String from1;

  @ColumnInfo(name = "to1")
  public String to1;

  @ColumnInfo(name = "from2")
  public String from2;

  @ColumnInfo(name = "to2")
  public String to2;

  @ColumnInfo(name = "maxDiff")
  public double maxDiff;

  @ColumnInfo(name = "message", defaultValue = "")
  public String message;

  /* Flag for only max/min
  Constraint with angle dazu 3 Punkte notwendig, theoretisch möglich bei to1&2 gleicher wert als mitte von winkel
  (judgeAngle muss geschrieben werden)
  Flag das contraint angle/distance
  evtl relevante Dimensionen. am wenigsten mit nicht relevanter dimesion*/
  @ColumnInfo(name = "type")
  public Constraint.TYPE type;

  @ColumnInfo(name = "inequalityType")
  public Constraint.INEQUALITY_TYPE inequalityType;

  @ColumnInfo(name = "insignificantDimension")
  public Constraint.INSIGNIFICANT_DIMENSION insignificantDimension;

  @Nullable
  @ColumnInfo(name = "compareAngle")
  public Integer compareAngle;

  public Constraint(
      long id,
      String from1,
      String to1,
      String from2,
      String to2,
      double maxDiff,
      String message,
      Constraint.TYPE type,
      Constraint.INEQUALITY_TYPE inequalityType,
      Constraint.INSIGNIFICANT_DIMENSION insignificantDimension,
      Integer compareAngle) {
    this.id = id;
    this.from1 = from1;
    this.to1 = to1;
    this.from2 = from2;
    this.to2 = to2;
    this.maxDiff = maxDiff;
    this.message = message;
    // neu
    this.type = type;
    this.inequalityType = inequalityType;
    this.insignificantDimension = insignificantDimension;
    this.compareAngle = compareAngle;
  }

  @Ignore
  public Constraint(
      String from1,
      String to1,
      String from2,
      String to2,
      double maxDiff,
      String message,
      Constraint.TYPE type,
      Constraint.INEQUALITY_TYPE inequalityType,
      Constraint.INSIGNIFICANT_DIMENSION insignificantDimension,
      Integer compareAngle) {
    this.from1 = from1;
    this.to1 = to1;
    this.from2 = from2;
    this.to2 = to2;
    this.maxDiff = maxDiff;
    this.message = message;
    // neu
    this.type = type;
    this.inequalityType = inequalityType;
    this.insignificantDimension = insignificantDimension;
    this.compareAngle = compareAngle;
  }

  public enum TYPE {
    ANGLE,
    DISTANCE,
    FLOOR_DISTANCE, // Distanz mit fester Zahl vergleichen
    UPRIGHT // Sonderfall: Steht der Nutzer aufrecht?
  }

  public enum INEQUALITY_TYPE {
    LESS,
    GREATER,
    EQUAL
  }

  public enum INSIGNIFICANT_DIMENSION {
    NONE,
    X,
    Y,
    Z
  }
}
