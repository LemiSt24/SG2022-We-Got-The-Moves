package com.sg2022.we_got_the_moves.db.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
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
  public static Duration longToDuration(Long durationInSeconds){
    return durationInSeconds == null ? null : java.time.Duration.ofSeconds(durationInSeconds);
  }
  @TypeConverter
  public static long duratioToLong(Duration duration){
    return duration == null ? null : duration.getSeconds();
  }
}
