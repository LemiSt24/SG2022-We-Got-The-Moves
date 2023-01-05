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

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsDailyBinding;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedExerciseAndExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;
import com.sg2022.we_got_the_moves.utils.TimeFormatUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class DailyOverviewFragment extends Fragment {
  private final String TAG = "DailyOverviewFragment";

  private FragmentStatisticsDailyBinding binding;
  private StatisticsViewModel model;
  private MutableLiveData<PieDataSet> pieDataSet;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(
            this.requireActivity().getApplication(), this.requireActivity());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    this.pieDataSet = new MutableLiveData<>(new PieDataSet(new ArrayList<>(), "Data"));
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentStatisticsDailyBinding.inflate(inflater, container, false);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    this.requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize =
        (int) (Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels) * 0.9);
    binding.pieChartDailyStatistics.setMinimumWidth(minScreenSize);
    binding.pieChartDailyStatistics.setMinimumHeight(minScreenSize);
    setupPieChart();
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
            final int avgDailyCalories = user.sex == User.SEX.MALE ? 2600 : 2200;
            binding.textviewCaloriesValueDailyStatistics.setText(
                String.format("%s", avgDailyCalories));

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
                            fee.exercise.getCalories(weight, fee.finishedExercise.duration);
                      }
                    }
                    //noinspection ConstantConditions
                    pieDataSet.getValue().resetColors();
                    pieDataSet.getValue().clear();
                    pieDataSet.getValue().addEntry(new PieEntry((int) burnedCalories, "Burned"));
                    pieDataSet
                        .getValue()
                        .addEntry(
                            new PieEntry(((int) (avgDailyCalories - burnedCalories)), "Remaining"));
                    pieDataSet.getValue().setColors(Color.RED, Color.GREEN);
                    pieDataSet.getValue().setValueTextSize(16);
                    pieDataSet
                        .getValue()
                        .setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    binding.pieChartDailyStatistics.setData(new PieData(pieDataSet.getValue()));
                    binding.pieChartDailyStatistics.invalidate();
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
    binding.pieChartDailyStatistics.setEntryLabelTextSize(16);
    binding.pieChartDailyStatistics.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

    binding.pieChartDailyStatistics.getLegend().setEnabled(false);
    binding.pieChartDailyStatistics.getDescription().setEnabled(false);
    binding.pieChartDailyStatistics.setEnabled(true);

    binding.pieChartDailyStatistics.setDrawHoleEnabled(true);
    binding.pieChartDailyStatistics.setHoleColor(
        this.requireContext().getColor(R.color.transparent));
    binding.pieChartDailyStatistics.setTouchEnabled(false);
    binding.pieChartDailyStatistics.setVerticalScrollBarEnabled(true);
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
