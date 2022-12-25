package com.sg2022.we_got_the_moves.util;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import kotlin.Triple;

public class TimeFormatUtil {

  public static Triple<Integer, Integer, Integer> secsToHhmmss(int totalSecs) {
    int hours = totalSecs / 3600;
    int minutes = (totalSecs % 3600) / 60;
    int seconds = totalSecs % 60;
    return new Triple<>(hours, minutes, seconds);
  }

  public static int hhmmssToSecs(Triple<Integer, Integer, Integer> triple) {
    return triple.getFirst() * 3600 + triple.getSecond() * 60 + triple.getThird();
  }

  @SuppressLint("DefaultLocale")
  public static String formatTime(int totalSecs) {
    Triple<Integer, Integer, Integer> t = secsToHhmmss(totalSecs);
    return String.format("%02d:%02d:%02d", t.getFirst(), t.getSecond(), t.getThird());
  }

  private static LocalDateTime dateToLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  private static Date localDateTimeToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Date atStartOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
    return localDateTimeToDate(startOfDay);
  }

  public static Date atEndOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
    return localDateTimeToDate(endOfDay);
  }
}
