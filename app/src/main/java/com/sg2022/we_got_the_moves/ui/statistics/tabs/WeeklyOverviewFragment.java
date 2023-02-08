package com.sg2022.we_got_the_moves.ui.statistics.tabs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsWeeklyBinding;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.TimeFormatUtil;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import kotlin.Pair;

public class WeeklyOverviewFragment extends Fragment {
  private final String TAG = "WeeklyOverviewFragment";

  private FragmentStatisticsWeeklyBinding binding;
  private StatisticsViewModel model;
  private MutableLiveData<BarDataSet> barDataSet;
  private MutableLiveData<Date> currentDate;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(this.requireActivity().getApplication());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    this.barDataSet = new MutableLiveData<>(new BarDataSet(new ArrayList<>(), "Data"));
    this.currentDate = new MutableLiveData<>(new Date());
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsWeeklyBinding.inflate(inflater, container, false);
    this.binding.imagebtnCalenderRightStatisticsWeekly.setOnClickListener(
        v -> currentDate.setValue(TimeFormatUtil.dateAdjustedByWeeks(currentDate.getValue(), 1)));

    this.binding.imagebtnCalenderLeftStatisticsWeekly.setOnClickListener(
        v -> currentDate.setValue(TimeFormatUtil.dateAdjustedByWeeks(currentDate.getValue(), -1)));

    this.setTotalTime();
    this.currentDate.observe(
        this.requireActivity(),
        date -> {
          disableRightBtnCheck(currentDate.getValue());
          setupCW(currentDate.getValue());
          loadData(currentDate.getValue());
          setTotalTime();
        });
    return this.binding.getRoot();
  }

  private void loadData(Date date) {
    final List<Pair<DayOfWeek, Pair<Date, Date>>> intervals =
        TimeFormatUtil.weekOfDayIntervals(date);
    final int weekDay = TimeFormatUtil.dateToWeekDay(date);
    final Date startDayOfWeekBegin = intervals.get(0).getSecond().getFirst();
    final Date finalDayOfWeekEnd = intervals.get(6).getSecond().getSecond();
    Pair<Integer, Integer> cwDate = TimeFormatUtil.dateToYearAndCalendarWeek(date);
    Pair<Integer, Integer> cwNow = TimeFormatUtil.dateToYearAndCalendarWeek(new Date());
    boolean isCurrentCw =
        Objects.equals(cwDate.getFirst(), cwNow.getFirst())
            && Objects.equals(cwDate.getSecond(), cwNow.getSecond());

    this.model.finishedWorkoutRepository.getMaxDurationSingle(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull Duration maxDuration) {

            model.finishedWorkoutRepository.getAllFinishedWorkoutsByDateRangeSingle(
                startDayOfWeekBegin,
                finalDayOfWeekEnd,
                new SingleObserver<>() {
                  @Override
                  public void onSubscribe(@NonNull Disposable d) {}

                  @Override
                  public void onSuccess(@NonNull List<FinishedWorkoutAndFinishedExercises> list) {
                    List<Pair<DayOfWeek, Long>> totalWorkoutDurationOnWeekdays =
                        intervals.stream()
                            .map(
                                triple ->
                                    new Pair<>(
                                        triple.getFirst(),
                                        list.stream()
                                            .reduce(
                                                0L,
                                                (result, e) -> {
                                                  if (e.finishedWorkout.date.before(
                                                          triple.getSecond().getFirst())
                                                      || e.finishedWorkout.date.after(
                                                          triple.getSecond().getSecond()))
                                                    return result;
                                                  return result
                                                      + e.finishedWorkout.duration.getSeconds();
                                                },
                                                Long::sum)))
                            .collect(Collectors.toList());
                    //noinspection ConstantConditions
                    barDataSet.getValue().clear();
                    barDataSet.getValue().resetColors();
                    ArrayList<Integer> colors = new ArrayList<>();
                    MutableLiveData<Float> maxDuration = new MutableLiveData<>(0f);
                    totalWorkoutDurationOnWeekdays.forEach(
                        p -> {
                          barDataSet
                              .getValue()
                              .addEntry(
                                  new BarEntry(
                                      p.getFirst().getValue() - 1, p.getSecond().floatValue()));
                          if (weekDay == p.getFirst().getValue() && isCurrentCw) {
                            barDataSet.getValue().addColor(Color.GREEN);
                            colors.add(Color.GREEN);
                          } else if (weekDay < p.getFirst().getValue() && isCurrentCw) {
                            barDataSet.getValue().addColor(Color.RED);
                            colors.add(Color.RED);
                          } else {
                            barDataSet.getValue().addColor(Color.BLUE);
                            colors.add(Color.BLUE);
                          }
                          //noinspection ConstantConditions
                          maxDuration.setValue(
                              Math.max(p.getSecond().floatValue(), maxDuration.getValue()));
                        });
                    barDataSet.getValue().setValueTextColors(colors);
                    barDataSet.getValue().setValueTextSize(14f);
                    barDataSet
                        .getValue()
                        .setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    long totalWeektime =
                        totalWorkoutDurationOnWeekdays.stream()
                            .reduce(0L, (result, e) -> result + e.getSecond(), Long::sum);
                    long avgWeektime = totalWeektime / weekDay;

                    binding.textviewValueTotalWeekStatisticsWeekly.setText(
                        TimeFormatUtil.formatTimeDdhhmmss((int) totalWeektime));
                    binding.textviewValueAverageStatisticsWeekly.setText(
                        TimeFormatUtil.formatTimeHhmmss((int) avgWeektime));

                    BarData barData = new BarData(barDataSet.getValue());
                    barData.setValueFormatter(
                        new ValueFormatter() {
                          @Override
                          public String getBarLabel(BarEntry barEntry) {
                            return barEntry.getY() == 0f
                                ? ""
                                : TimeFormatUtil.formatTimeHhmmss((int) barEntry.getY());
                          }
                        });
                    BarChart bc = binding.barChartStatisticsWeekly;
                    bc.setData(barData);
                    YAxis yAxisL = bc.getAxisLeft();
                    yAxisL.setAxisMinimum(0f);
                    //noinspection ConstantConditions
                    yAxisL.setAxisMaximum(
                        Math.max(
                            3600f, maxDuration.getValue() + 3600f - maxDuration.getValue() % 3600));
                    setupBarChart();
                    bc.invalidate();
                  }

                  @Override
                  public void onError(@NonNull Throwable e) {
                    Log.e(TAG, e.toString());
                  }
                });
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void disableRightBtnCheck(Date date) {
    this.binding.imagebtnCalenderRightStatisticsWeekly.setEnabled(
        !TimeFormatUtil.getDayInterval(date).equals(TimeFormatUtil.getDayInterval(new Date())));
    if (!this.binding.imagebtnCalenderRightStatisticsWeekly.isEnabled()) {
      this.binding.imagebtnCalenderRightStatisticsWeekly.setVisibility(View.INVISIBLE);
    } else {
      this.binding.imagebtnCalenderRightStatisticsWeekly.setVisibility(View.VISIBLE);
    }
  }

  private void setupCW(Date date) {
    Pair<Integer, Integer> p = TimeFormatUtil.dateToYearAndCalendarWeek(date);
    this.binding.textviewCalenderStatisticsWeekly.setText(
        String.format(Locale.US, "%04d CW%02d", p.getFirst(), p.getSecond()));
  }

  private void setTotalTime() {
    this.model.finishedWorkoutRepository.getTotalDurationSingle(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull Duration duration) {
            binding.textviewValueTotalStatistics.setText(
                TimeFormatUtil.formatTimeYyMMddhhmmss((int) duration.getSeconds()));
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setupBarChart() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels);

    BarChart bc = binding.barChartStatisticsWeekly;
    bc.setMinimumWidth(minScreenSize);
    bc.setMinimumHeight(minScreenSize);
    bc.setEnabled(true);
    bc.setDrawGridBackground(false);
    bc.setExtraTopOffset(20f);
    bc.setExtraLeftOffset(20f);
    bc.setTouchEnabled(false);
    bc.setVerticalScrollBarEnabled(true);

    YAxis yAxisR = bc.getAxisRight();
    yAxisR.setEnabled(false);

    XAxis xAxis = bc.getXAxis();
    xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    xAxis.setTextSize(14f);
    xAxis.setSpaceMax(1f);
    xAxis.setValueFormatter(
        new IndexAxisValueFormatter(
            EnumSet.allOf(TimeFormatUtil.DAY.class).stream()
                .map(Enum::name)
                .collect(Collectors.toList())));

    YAxis yAxisL = bc.getAxisLeft();
    yAxisL.setValueFormatter(
        new ValueFormatter() {
          @Override
          public String getFormattedValue(float value) {
            return TimeFormatUtil.formatTimeHhmmss((int) value);
          }
        });
    yAxisL.setLabelCount(5, true);

    Legend legend = bc.getLegend();
    legend.setEnabled(false);

    Description description = bc.getDescription();
    description.setEnabled(false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
