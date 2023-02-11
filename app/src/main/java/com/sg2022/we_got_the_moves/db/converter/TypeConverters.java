package com.sg2022.we_got_the_moves.db.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Date;
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
    return duration == null ? null : duration.getSeconds();
  }

  @TypeConverter
  public String fromExerciseStatesList(List<ExerciseState> ExerciseStates) {
    if (ExerciseStates == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<ExerciseState>>() {}.getType();
    String json = gson.toJson(ExerciseStates, type);
    return json;
  }

  @TypeConverter
  public List<ExerciseState> toExerciseStatesList(String ExerciseStateString) {
    if (ExerciseStateString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<ExerciseState>>() {}.getType();
    List<ExerciseState> ExerciseStates = gson.fromJson(ExerciseStateString, type);
    return ExerciseStates;
  }

  @TypeConverter
  public String fromConstraintIdsList(List<Long> ConstraintIds) {
    if (ConstraintIds == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Long>>() {}.getType();
    String json = gson.toJson(ConstraintIds, type);
    return json;
  }

  @TypeConverter
  public List<Long> toConstraintIdsList(String ConstraintIdsString) {
    if (ConstraintIdsString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<Long>>() {}.getType();
    List<Long> ConstraintIds = gson.fromJson(ConstraintIdsString, type);
    return ConstraintIds;
  }

  @TypeConverter
  public String fromWorkoutExerciseList(List<WorkoutExercise> workoutExercises) {
    if (workoutExercises == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<WorkoutExercise>>() {}.getType();
    String json = gson.toJson(workoutExercises, type);
    return json;
  }

  @TypeConverter
  public List<WorkoutExercise> toWorkoutExerciseList(String workoutExercisesString) {
    if (workoutExercisesString == null) {
      return (null);
    }
    Gson gson = new Gson();
    Type type = new TypeToken<List<WorkoutExercise>>() {}.getType();
    List<WorkoutExercise> workoutExercises = gson.fromJson(workoutExercisesString, type);
    return workoutExercises;
  }
}
