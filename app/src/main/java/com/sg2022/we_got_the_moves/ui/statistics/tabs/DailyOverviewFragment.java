package com.sg2022.we_got_the_moves.ui.statistics.tabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsDailyBinding;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedExerciseAndExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.TimeFormatUtil;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class DailyOverviewFragment extends Fragment {
  private final String TAG = "DailyOverviewFragment";
  private FragmentStatisticsDailyBinding binding;
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
    this.binding = FragmentStatisticsDailyBinding.inflate(inflater, container, false);
    loadData();
    return binding.getRoot();
  }

  private void loadData() {
    this.model.userRepository.getUser(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull User user) {
            final float weight = user.weight;
            binding.textviewCaloriesValueDailyStatistics.setText(
                String.format("%s kcal", user.calories));

            final Date today_begin = TimeFormatUtil.atStartOfDay(new Date());
            final Date today_end = TimeFormatUtil.atEndOfDay(new Date());

            model.finishedWorkoutRepository.getAllFinishedWorkoutsByDateRangeSingle(
                today_begin,
                today_end,
                new SingleObserver<>() {
                  @Override
                  public void onSubscribe(@NonNull Disposable d) {}

                  @Override
                  public void onSuccess(
                      @NonNull
                          List<FinishedWorkoutAndFinishedExercises>
                              finishedWorkoutAndFinishedExercises) {

                    double burnedCalories = 0.0f;
                    for (FinishedWorkoutAndFinishedExercises fwfe :
                        finishedWorkoutAndFinishedExercises) {
                      for (FinishedExerciseAndExercise fee : fwfe.finishedExerciseAndExercises) {
                        burnedCalories +=
                            fee.exercise.getKCal(weight, fee.finishedExercise.duration);
                      }
                    }
                    PieDataSet pieDataSet = new PieDataSet(new ArrayList<>(), "Data");
                    pieDataSet.addEntry(new PieEntry((int) burnedCalories, "Burned"));
                    pieDataSet.addEntry(
                        new PieEntry((int) (user.calories - (int)burnedCalories) < 0 ?
                            0 : ((int) (user.calories - (int)burnedCalories)), "Remaining"));
                    pieDataSet.setColors(Color.RED, Color.GREEN);
                    pieDataSet.setValueTextSize(16f);
                    pieDataSet.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    PieChart pc = binding.piechartStatisticsDaily;
                    pc.setData(new PieData(pieDataSet));
                    pc.setRenderer(new CustomPieChartRenderer(binding.piechartStatisticsDaily));

                    setupPieChart();
                    pc.invalidate();
                  }

                  @Override
                  public void onError(@NonNull Throwable e) {
                    Log.e(TAG, e.toString());
                  }
                });
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setupPieChart() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels);
    PieChart pc = binding.piechartStatisticsDaily;
    pc.setMinimumWidth(minScreenSize);
    pc.setMinimumHeight(minScreenSize);
    pc.setEntryLabelTextSize(16);
    pc.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    pc.setDrawHoleEnabled(true);
    pc.setHoleColor(this.requireContext().getColor(R.color.transparent));
    pc.setTouchEnabled(false);
    pc.setVerticalScrollBarEnabled(true);
    pc.setEnabled(true);

    Legend legend = pc.getLegend();
    legend.setEnabled(false);

    Description description = pc.getDescription();
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

  public static class CustomPieChartRenderer extends PieChartRenderer {
    private final Context context;

    public CustomPieChartRenderer(PieChart chart) {
      this(chart, chart.getAnimator(), chart.getViewPortHandler());
    }

    public CustomPieChartRenderer(
        PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
      super(chart, animator, viewPortHandler);
      context = chart.getContext();
    }

    @Override
    public void drawExtras(Canvas c) {
      super.drawExtras(c);
      drawImage(c);
    }

    private void drawImage(Canvas c) {
      MPPointF center = mChart.getCenterCircleBox();

      Drawable d =
          ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_flame_black_24dp, null);

      if (d != null) {
        d.setTint(Color.RED);
        float halfWidth = mChart.getHoleRadius() / 100 * mChart.getRadius() / 2;
        float halfHeight = mChart.getHoleRadius() / 100 * mChart.getRadius() / 2;

        d.setBounds(
            (int) (center.x - halfWidth),
            (int) (center.y - halfHeight),
            (int) (center.x + halfWidth),
            (int) (center.y + halfHeight));
        d.draw(c);
      }
    }
  }
}
