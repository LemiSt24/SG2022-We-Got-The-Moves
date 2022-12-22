package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Workout")
public class Workout {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public long id;

  @ColumnInfo(name = "name")
  public String name;

  public Workout(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Ignore
  public Workout(String name) {
    this(0, name);
  }
}
