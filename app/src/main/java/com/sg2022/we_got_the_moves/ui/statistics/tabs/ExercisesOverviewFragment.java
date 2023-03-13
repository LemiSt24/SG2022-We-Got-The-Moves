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
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsExercisesBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class ExercisesOverviewFragment extends Fragment {
  private final String TAG = "ExercisesOverviewFragment";
  private FragmentStatisticsExercisesBinding binding;
  private StatisticsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(this.requireActivity().getApplication());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsExercisesBinding.inflate(inflater, container, false);
    this.loadData();
    return this.binding.getRoot();
  }

  private void loadData() {
    this.model.workoutsRepository.getAllExerciseAndFinishedExercisesSingle(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<ExerciseAndFinishedExercises> list) {
            //data for amount
            List<BarEntry> entriesAmount =
                IntStream.range(0, list.size())
                    .mapToObj(
                        idx -> {
                          ExerciseAndFinishedExercises efe = list.get(idx);
                          return new BarEntry(
                              idx,
                              efe.finishedExercises.stream()
                                  .reduce(0, (result, fe) -> result + fe.amount, Integer::sum),
                              efe.exercise);
                        })
                    .collect(Collectors.toList());

            //data for duration of all exercises
            List<BarEntry> entriesDuration =
                IntStream.range(0, list.size())
                    .mapToObj(
                        idx -> {
                          ExerciseAndFinishedExercises efe = list.get(idx);
                          return new BarEntry(
                              idx,
                              efe.finishedExercises.stream()
                                  .reduce(0, (result, fe) -> result + fe.duration, Integer::sum),
                              efe.exercise);
                        })
                    .collect(Collectors.toList());

          // average duration per repetition of en exercise
          // if you want to add this metric back,
          // 1. uncomment entriesAverage (Line 109) and barDataSetAverage (Line 142),
          // 2. add barDataSetAverage in BarData (Line 153) and
          // 3. set groupSpace to 0.4f (Line 155) (Label has granularity of 1, each bar has 0.2 width
          // -> numberOfBarCharts * 0.2 + groupSpace == 1 for correct chart)

          /*  List<BarEntry> entriesAverage =
                IntStream.range(0, list.size())
                    .mapToObj(
                        idx -> {
                          float amount = entriesAmount.get(idx).getY();
                          float value =
                              entriesDuration.get(idx).getY() / (amount == 0f ? 1 : amount);
                          return new BarEntry(idx, value, value);
                        })
                    .collect(Collectors.toList()); */

            BarDataSet barDataSetAmount =
                new BarDataSet(entriesAmount, getString(R.string.total_number_reps));
            barDataSetAmount.setColor(Color.BLUE);
            barDataSetAmount.setValueFormatter(
                new ValueFormatter() {
                  @Override
                  public String getFormattedValue(float value) {
                    return String.format(Locale.US, "%.0f", value);
                  }
                });

            BarDataSet barDataSetDuration =
                new BarDataSet(entriesDuration, getString(R.string.total_duration_hours));
            barDataSetDuration.setColor(Color.GREEN);
            barDataSetDuration.setValueFormatter(
                new ValueFormatter() {
                  @Override
                  public String getFormattedValue(float value) {
                    return String.format(Locale.US, "%.2f", value / 3600);
                  }
                });

          /*  BarDataSet barDataSetAverage =
                new BarDataSet(entriesAverage, getString(R.string.average_secs_repetition));
            barDataSetAverage.setColor(Color.CYAN);
            barDataSetAverage.setValueFormatter(
                new ValueFormatter() {
                  @Override
                  public String getFormattedValue(float value) {
                    return String.format(Locale.US, "%.2f", value);
                  }
                });*/

            BarData barData = new BarData(barDataSetAmount, barDataSetDuration);
            barData.setBarWidth(0.2f);
            barData.groupBars(-0.5f, 0.6f, 0f);
            barData.setValueTextSize(14f);
            barData.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            HorizontalBarChart hbc = binding.barChartStatisticsExercises;
            hbc.setData(barData);

            XAxis xAxis = hbc.getXAxis();
            xAxis.setValueFormatter(
                new IndexAxisValueFormatter(
                    entriesAmount.stream()
                        .map(e -> ((Exercise) e.getData()).name)
                        .collect(Collectors.toList())));
            // hbc.setFitBars(true);

            setupBarChart();
            hbc.invalidate();
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setupBarChart() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    this.requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels);

    HorizontalBarChart hbc = this.binding.barChartStatisticsExercises;
    hbc.setMinimumHeight((int) (minScreenSize * 1.5));
    hbc.setMinimumWidth(minScreenSize);

    hbc.setEnabled(true);
    hbc.setDrawGridBackground(false);
    hbc.setBackgroundColor(getResources().getColor(R.color.black));
    hbc.getData().setValueTextColor(getResources().getColor(R.color.white));
    hbc.setTouchEnabled(false);
    hbc.setDrawValueAboveBar(true);
    hbc.setExtraOffsets(0f, 0f, 35f, 30f);

    XAxis xAxis = hbc.getXAxis();
    xAxis.setDrawGridLines(false);
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawLabels(true);
    xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    xAxis.setTextSize(14f);
    xAxis.setGranularity(1f);
    xAxis.setSpaceMin(0.5f);
    xAxis.setSpaceMax(0.5f);
    xAxis.setAvoidFirstLastClipping(true);
    xAxis.setTextColor(getResources().getColor(R.color.white));

    YAxis yAxisR = hbc.getAxisRight();
    yAxisR.setEnabled(false);
    yAxisR.setDrawLabels(false);
    yAxisR.setDrawGridLines(false);
    yAxisR.setAxisMinimum(0f);

    YAxis yAxisL = hbc.getAxisLeft();
    yAxisL.setEnabled(false);
    yAxisL.setDrawLabels(false);
    yAxisL.setDrawGridLines(false);
    yAxisL.setAxisMinimum(0f);

    Legend legend = hbc.getLegend();
    List<LegendEntry> l = new ArrayList<>();
    for (LegendEntry e: legend.getEntries()) l.add(e);
    Collections.rotate(l, l.size() - 1);
    legend.setCustom(l);
    legend.setEnabled(true);
    legend.setDrawInside(false);
    legend.setTextSize(14f);
    legend.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
    legend.setForm(Legend.LegendForm.CIRCLE);
    legend.setYOffset(20f);
    legend.setTextColor(getResources().getColor(R.color.white));

    Description description = hbc.getDescription();
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
