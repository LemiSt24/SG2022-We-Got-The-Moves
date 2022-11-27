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

  public float getBMI() {return weigthInKg / (hightInMeters * hightInMeters);}

  public float kgToLbs(float kg) { return kg * (float)2.20462;}

  public int[] mToFtIn(float m) {
    int feet = (int) Math.floor(m / 0.3048);
    float rest = (float) (m % 0.3048);
    int inch = (int) Math.floor(rest / 0.0254);
    return new int[] {feet, inch};
  }

}
