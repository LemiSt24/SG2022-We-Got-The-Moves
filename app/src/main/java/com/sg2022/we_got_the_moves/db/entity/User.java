package com.sg2022.we_got_the_moves.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  public int id;

  @ColumnInfo(name = "name")
  public String name;

  @ColumnInfo(name = "height")
  public float height; // [m]

  @ColumnInfo(name = "weight")
  public float weight; // [kg]

  @ColumnInfo(name = "sex")
  public SEX sex;

  @ColumnInfo(name = "age")
  public int age;

  @ColumnInfo(name = "calories")
  public int calories;

  @ColumnInfo(name = "frontCamera")
  public boolean frontCamera;

  public User(String name, float height, float weight, SEX sex, int age, int calories, boolean frontCamera) {
    this.id = 1;
    this.name = name;
    this.height = height;
    this.weight = weight;
    this.sex = sex;
    this.age = age;
    this.calories = calories;
    this.frontCamera = frontCamera;
  }

  public double getBMI() {
    return this.weight / Math.pow(this.height, 2);
  }

  public float kgToLbs(float kg) {
    return kg * (float) 2.20462;
  }

  public int[] mToFtIn(float m) {
    int feet = (int) Math.floor(m / 0.3048);
    float rest = (float) (m % 0.3048);
    int inch = (int) Math.floor(rest / 0.0254);
    return new int[] {feet, inch};
  }

  public enum SEX {
    MALE,
    FEMALE
  }
}
