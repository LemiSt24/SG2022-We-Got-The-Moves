package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Duration;
import java.util.Date;

@Entity(tableName = "FinishedTraining")
public class FinishedTraining {

  @PrimaryKey() public Date date;

  public long workoutId;
  public Duration duration;

  public FinishedTraining(Date date, long workoutId, Duration duration) {
    this.date = date;
    this.workoutId = workoutId;
    this.duration = duration;
  }
}
