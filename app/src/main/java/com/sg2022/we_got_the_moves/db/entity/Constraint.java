package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Constraint")
public class Constraint {

  @PrimaryKey (autoGenerate = true)
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

  public Constraint(
      long id, String from1, String to1, String from2, String to2, double maxDiff, String message) {
    this.id = id;
    this.from1 = from1;
    this.to1 = to1;
    this.from2 = from2;
    this.to2 = to2;
    this.maxDiff = maxDiff;
    this.message = message;
  }

  @Ignore
  public Constraint(
      String from1, String to1, String from2, String to2, double maxDiff, String message) {
    this.from1 = from1;
    this.to1 = to1;
    this.from2 = from2;
    this.to2 = to2;
    this.maxDiff = maxDiff;
    this.message = message;
  }

  @Ignore
  public Constraint(String from1, String to1, String from2, String to2, double maxDiff) {
    this(from1, to1, from2, to2, maxDiff, null);
  }
}
