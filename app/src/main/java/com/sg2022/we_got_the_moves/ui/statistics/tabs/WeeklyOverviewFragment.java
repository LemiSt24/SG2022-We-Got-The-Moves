package com.sg2022.we_got_the_moves.ui.statistics.tabs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsWeeklyBinding;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;
import com.sg2022.we_got_the_moves.utils.TimeFormatUtil;

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
        new StatisticsViewModel.Factory(
            this.requireActivity().getApplication(), this.requireActivity());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    this.barDataSet = new MutableLiveData<>(new BarDataSet(new ArrayList<>(), "Data"));
    this.currentDate = new MutableLiveData<>(new Date());
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsWeeklyBinding.inflate(inflater, container, false);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    this.requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize =
        (int) (Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels) * 0.8);
    this.binding.barChartWeeklyStatistics.setMinimumWidth(minScreenSize);
    this.binding.barChartWeeklyStatistics.setMinimumHeight(minScreenSize);
    this.binding.imagebtnCalenderRightWeeklyStatistics.setOnClickListener(
        v -> currentDate.setValue(TimeFormatUtil.dateAdjustedByWeeks(currentDate.getValue(), 1)));

    this.binding.imagebtnCalenderLeftWeeklyStatistics.setOnClickListener(
        v -> currentDate.setValue(TimeFormatUtil.dateAdjustedByWeeks(currentDate.getValue(), -1)));

    this.setupBarChart();
    this.setupSeekbar();
    this.setTotalTime();
    this.currentDate.observe(
        this.requireActivity(),
        date -> {
          disableRightBtnCheck(currentDate.getValue());
          setupCW(currentDate.getValue());
          loadData(currentDate.getValue());
        });
    return binding.getRoot();
  }

  private void disableRightBtnCheck(Date date) {
    this.binding.imagebtnCalenderRightWeeklyStatistics.setEnabled(
        !TimeFormatUtil.getDayInterval(date).equals(TimeFormatUtil.getDayInterval(new Date())));
    if (!this.binding.imagebtnCalenderRightWeeklyStatistics.isEnabled()) {
      this.binding.imagebtnCalenderRightWeeklyStatistics.setVisibility(View.INVISIBLE);
    } else {
      this.binding.imagebtnCalenderRightWeeklyStatistics.setVisibility(View.VISIBLE);
    }
  }

  private void setupCW(Date date) {
    Pair<Integer, Integer> p = TimeFormatUtil.dateToYearAndCalendarWeek(date);
    this.binding.textviewCalenderWeeklyStatistics.setText(
        String.format(Locale.US, "%04d CW%02d", p.getFirst(), p.getSecond()));
  }

  private void setupSeekbar() {
    this.binding.seekbarYaxisWeeklyStatistics.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            binding
                .barChartWeeklyStatistics
                .getAxisLeft()
                .setValueFormatter(
                    new ValueFormatter() {
                      @Override
                      public String getFormattedValue(float value) {
                        return formatValue(seekBar.getProgress(), value);
                      }
                    });
            binding
                .barChartWeeklyStatistics
                .getBarData()
                .setValueFormatter(
                    new ValueFormatter() {
                      @Override
                      public String getBarLabel(BarEntry barEntry) {
                        return formatValue(seekBar.getProgress(), barEntry.getY());
                      }
                    });
            binding.barChartWeeklyStatistics.postInvalidate();
          }
        });
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

    this.model.finishedWorkoutRepository.getAllFinishedWorkoutsByDateRangeSingle(
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
                                                  triple.getSecond().getSecond())) return result;
                                          return result + e.finishedWorkout.duration.getSeconds();
                                        },
                                        Long::sum)))
                    .collect(Collectors.toList());
            //noinspection ConstantConditions
            barDataSet.getValue().clear();
            barDataSet.getValue().resetColors();
            ArrayList<Integer> textColors = new ArrayList<>();
            totalWorkoutDurationOnWeekdays.forEach(
                p -> {
                  barDataSet
                      .getValue()
                      .addEntry(
                          new BarEntry(p.getFirst().getValue() - 1, p.getSecond().floatValue()));
                  if (weekDay == p.getFirst().getValue() && isCurrentCw) {
                    barDataSet.getValue().addColor(Color.GREEN);
                    textColors.add(Color.GREEN);
                  } else if (weekDay < p.getFirst().getValue() && isCurrentCw) {
                    barDataSet.getValue().addColor(Color.RED);
                    textColors.add(Color.RED);
                  } else {
                    barDataSet.getValue().addColor(Color.BLUE);
                    textColors.add(Color.BLUE);
                  }
                });
            barDataSet.getValue().setValueTextColors(textColors);
            barDataSet.getValue().setValueTextSize(16);
            barDataSet.getValue().setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            long totalWeektime =
                totalWorkoutDurationOnWeekdays.stream()
                    .reduce(0L, (result, e) -> result + e.getSecond(), Long::sum);
            long avgWeektime = totalWeektime / weekDay;
            binding.textviewAverageValueWeeklyStatistics.setText(
                TimeFormatUtil.formatTime(avgWeektime));
            binding.barChartWeeklyStatistics.setData(new BarData(barDataSet.getValue()));
            setupBarChart();
            binding.barChartWeeklyStatistics.invalidate();
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setTotalTime() {
    this.model.finishedWorkoutRepository.getTotalDurationSingle(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull Duration duration) {
            binding.textviewValueTotalStatistics.setText(
                String.format(
                    Locale.US,
                    "%02d:%02d:%02d:%02d:%02d:%02d",
                    duration.toDays() / 360,
                    duration.toDays() / 30,
                    duration.toDays() % 30,
                    duration.toHours() % 24,
                    duration.toMinutes() % 60,
                    duration.getSeconds() % 60));
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setupBarChart() {
    this.binding.barChartWeeklyStatistics.getLegend().setEnabled(false);
    this.binding.barChartWeeklyStatistics.getDescription().setEnabled(false);
    this.binding.barChartWeeklyStatistics.setEnabled(true);
    this.binding.barChartWeeklyStatistics.setDrawGridBackground(false);
    this.binding.barChartWeeklyStatistics.setExtraTopOffset(20f);
    this.binding.barChartWeeklyStatistics.setExtraLeftOffset(20f);
    this.binding.barChartWeeklyStatistics.getAxisRight().setEnabled(false);
    this.binding
        .barChartWeeklyStatistics
        .getXAxis()
        .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    this.binding.barChartWeeklyStatistics.getXAxis().setTextSize(16);
    this.binding
        .barChartWeeklyStatistics
        .getXAxis()
        .setValueFormatter(
            new IndexAxisValueFormatter(
                EnumSet.allOf(TimeFormatUtil.DAY.class).stream()
                    .map(Enum::name)
                    .collect(Collectors.toList())));
    this.binding.barChartWeeklyStatistics.setTouchEnabled(false);
    this.binding.barChartWeeklyStatistics.setVerticalScrollBarEnabled(true);
  }

  private String formatValue(int progress, float value) {
    float result, hours, mins, secs;
    switch (progress) {
      case 0:
        return String.format(Locale.US, "%.0f", value);
      case 1:
        mins = TimeFormatUtil.secsToHhmmss((int) value).getSecond().floatValue();
        secs = TimeFormatUtil.secsToHhmmss((int) value).getThird().floatValue() / 60;
        result = mins + secs;
        break;
      default:
        hours = TimeFormatUtil.secsToHhmmss((int) value).getFirst().floatValue();
        mins = TimeFormatUtil.secsToHhmmss((int) value).getSecond().floatValue() / 60;
        secs = TimeFormatUtil.secsToHhmmss((int) value).getThird().floatValue() / 3600;
        result = hours + mins + secs;
        break;
    }
    return String.format(Locale.US, "%.2f", result);
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
