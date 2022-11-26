package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {

  @PrimaryKey(autoGenerate = true)
  public int id;

  public String name;
  public float hightInMeters;
  public float weigthInKg;
  public boolean isMale;
  public int age;

  public User(String name, float hightInMeters, float weigthInKg, boolean isMale, int age) {
    this.id = 1;
    this.name = name;
    this.hightInMeters = hightInMeters;
    this.weigthInKg = weigthInKg;
    this.isMale = isMale;
    this.age = age;
  }

  public float getBMI() {
    return weigthInKg / (hightInMeters * hightInMeters);
  }
}
