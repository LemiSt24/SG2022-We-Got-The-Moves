package com.sg2022.we_got_the_moves.db.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.TrophiesFragment;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TypeConverters {

  @TypeConverter
  public static Date fromTimestamp(Long value) {
    return value == null ? null : new Date(value);
  }

  @TypeConverter
  public static Long dateToTimestamp(Date date) {
    return date == null ? null : date.getTime();
  }

  @TypeConverter
  public static Duration longToDuration(Long durationInSeconds) {
    return durationInSeconds == null ? null : java.time.Duration.ofSeconds(durationInSeconds);
  }

  @TypeConverter
  public static long duratioToLong(Duration duration) {
    return duration == null ? -1 : duration.getSeconds();
  }

  //brauch man vermutlich nicht mehr
  @TypeConverter
  public String fromExerciseStatesList(List<ExerciseState> ExerciseStates) {
    if (ExerciseStates == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<ExerciseState>>() {}.getType();
    return gson.toJson(ExerciseStates, type);
  }

  @TypeConverter
  public List<ExerciseState> toExerciseStatesList(String ExerciseStateString) {
    if (ExerciseStateString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<ExerciseState>>() {}.getType();
    return gson.fromJson(ExerciseStateString, type);
  }

  @TypeConverter
  public String fromConstraintIdsList(List<Long> ConstraintIds) {
    if (ConstraintIds == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Long>>() {}.getType();
    return gson.toJson(ConstraintIds, type);
  }

  @TypeConverter
  public List<Long> toConstraintIdsList(String ConstraintIdsString) {
    if (ConstraintIdsString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Long>>() {}.getType();
    return gson.fromJson(ConstraintIdsString, type);
  }

  @TypeConverter
  public String fromAmountList(List<Integer> amounts) {
    if (amounts == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Integer>>() {}.getType();
    return gson.toJson(amounts, type);
  }

  @TypeConverter
  public List<Integer> toAmountList(String amountsString) {
    if (amountsString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Integer>>() {}.getType();
    return gson.fromJson(amountsString, type);
  }

  @TypeConverter
  public static HashMap<String, TrophiesFragment.ACHIEVEMENT> fromString(String value) {
    Type mapType = new TypeToken<HashMap<String, TrophiesFragment.ACHIEVEMENT>>() {
    }.getType();
    return new Gson().fromJson(value, mapType);
  }

  @TypeConverter
  public static String fromStringMap(HashMap<String, TrophiesFragment.ACHIEVEMENT> map) {
    Gson gson = new Gson();
    return gson.toJson(map);
  }

}
