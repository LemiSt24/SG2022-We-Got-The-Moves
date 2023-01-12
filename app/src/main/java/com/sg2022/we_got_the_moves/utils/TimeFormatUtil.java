package com.sg2022.we_got_the_moves.utils;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Sextet;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlin.Pair;
import kotlin.Triple;

public class TimeFormatUtil {

  public static final Integer DAYS_PER_MONTH = 30;
  public static final Integer DAYS_PER_YEAR = 360;

  public static Triple<Integer, Integer, Integer> secsToHhmmss(int totalSecs) {
    int hours = totalSecs / 3600;
    int minutes = (totalSecs / 60) % 60;
    int seconds = totalSecs % 60;
    return new Triple<>(hours, minutes, seconds);
  }

  public static Quartet<Integer, Integer, Integer, Integer> secsToDdhhmmss(int secs) {
    Triple<Integer, Integer, Integer> t = secsToHhmmss(secs);
    return new Quartet<>(t.getFirst() / 24, t.getFirst() % 24, t.getSecond(), t.getThird());
  }

  public static Quintet<Integer, Integer, Integer, Integer, Integer> secsToMMddhhmmss(int secs) {
    Quartet<Integer, Integer, Integer, Integer> q = secsToDdhhmmss(secs);
    return new Quintet<>(
        q.getValue0() / DAYS_PER_MONTH,
        q.getValue0() % DAYS_PER_MONTH,
        q.getValue1(),
        q.getValue2(),
        q.getValue3());
  }

  public static Sextet<Integer, Integer, Integer, Integer, Integer, Integer> secsToYyMMhhddhhmmss(
      int secs) {
    Quintet<Integer, Integer, Integer, Integer, Integer> q = secsToMMddhhmmss(secs);
    return new Sextet<>(
        q.getValue0() / DAYS_PER_YEAR,
        q.getValue0() % DAYS_PER_YEAR,
        q.getValue1(),
        q.getValue2(),
        q.getValue3(),
        q.getValue4());
  }

  public static int hhmmssToSecs(Triple<Integer, Integer, Integer> triple) {
    return triple.getFirst() * 3600 + triple.getSecond() * 60 + triple.getThird();
  }

  public static String formatTimeHhmmss(int totalSecs) {
    Triple<Integer, Integer, Integer> t = secsToHhmmss(totalSecs);
    return String.format(Locale.US, "%02d:%02d:%02d", t.getFirst(), t.getSecond(), t.getThird());
  }

  public static String formatTimeDdhhmmss(int totalSecs) {
    Quartet<Integer, Integer, Integer, Integer> q = secsToDdhhmmss(totalSecs);
    return String.format(
        Locale.US,
        "%02d:%02d:%02d:%02d",
        q.getValue0(),
        q.getValue1(),
        q.getValue2(),
        q.getValue3());
  }

  public static String formatTimeYyMMddhhmmss(int totalSecs) {
    Sextet<Integer, Integer, Integer, Integer, Integer, Integer> s =
        secsToYyMMhhddhhmmss(totalSecs);
    return String.format(
        Locale.US,
        "%02d:%02d:%02d:%02d:%02d:%02d",
        s.getValue0(),
        s.getValue1(),
        s.getValue2(),
        s.getValue3(),
        s.getValue4(),
        s.getValue5());
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

  public static Pair<Date, Date> getDayInterval(Date date) {
    return new Pair<>(atStartOfDay(date), atEndOfDay(date));
  }

  public static List<Pair<DayOfWeek, Pair<Date, Date>>> weekOfDayIntervals(Date date) {
    List<Pair<DayOfWeek, Pair<Date, Date>>> result = new ArrayList<>();
    int weekDay = dateToWeekDay(date);
    for (int i = 1 - weekDay; i < 8 - weekDay; i++) {
      LocalDateTime dayOfWeek = dateToLocalDateTime(date).plusDays(i);
      Pair<Date, Date> dayOfWeekInterval = getDayInterval(localDateTimeToDate(dayOfWeek));
      result.add(new Pair<>(dayOfWeek.getDayOfWeek(), dayOfWeekInterval));
    }
    return result;
  }

  public static int dateToWeekDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    return localDateTime.getDayOfWeek().getValue();
  }

  public static DAY dateToWeekDayEnum(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    return DAY.values()[localDateTime.getDayOfWeek().getValue() - 1];
  }

  public static Date dateAdjustedByWeeks(Date date, long weeks) {
    LocalDateTime lt = dateToLocalDateTime(date);
    lt = lt.plusWeeks(weeks);
    return localDateTimeToDate(lt);
  }

  public static Pair<Integer, Integer> dateToYearAndCalendarWeek(Date date) {
    Calendar cl = Calendar.getInstance();
    cl.setTime(date);
    return new Pair<>(cl.get(Calendar.YEAR), cl.get(Calendar.WEEK_OF_YEAR));
  }

  public enum DAY {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN
  }
}
