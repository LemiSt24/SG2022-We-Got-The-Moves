package com.sg2022.we_got_the_moves.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsBinding;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedExerciseAndExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.util.TimeFormatUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class StatisticsFragment extends Fragment {

  private final String TAG = "StatisticsFragment";

  private FragmentStatisticsBinding binding;
  private StatisticsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(
            this.requireActivity().getApplication(), this.requireActivity());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentStatisticsBinding.inflate(inflater, container, false);
    this.model.userRepository.getUser(
        new SingleObserver<User>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull User user) {
            final float weight = user.weight;
            final float avgDailyCalories = user.gender == User.SEX.MALE ? 2600.0f : 2200.0f;
            final Date today_begin = TimeFormatUtil.atStartOfDay(new Date());
            final Date today_end = TimeFormatUtil.atEndOfDay(new Date());

            model.finishedWorkoutRepository.getAllFinishedWorkoutsByDateRangeSingle(
                today_begin,
                today_end,
                new SingleObserver<List<FinishedWorkoutAndFinishedExercises>>() {
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
                            fee.exercise.getCalories(weight, fee.finishedExercise.units);
                      }
                    }
                    PieDataSet pieDataSet = new PieDataSet(new ArrayList<>(), "Data");
                    pieDataSet.addEntry(new PieEntry((float) burnedCalories, "Burned"));
                    pieDataSet.addEntry(
                        new PieEntry(((float) (avgDailyCalories - burnedCalories)), "Remaining"));
                    pieDataSet.setColors(Color.RED, Color.GREEN);
                    PieData pieData = new PieData();
                    pieData.setDataSet(pieDataSet);
                    binding.pieChartStatistics.getLegend().setEnabled(false);
                    binding.pieChartStatistics.getDescription().setEnabled(false);
                    binding.pieChartStatistics.setData(pieData);
                    binding.pieChartStatistics.setEnabled(true);
                    binding.textviewStatistics.setText(R.string.statistiscs_titel_calorie_burn);
                    binding.pieChartStatistics.invalidate();
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
    // binding.pieChartStatistics.setHoleColor(Color.WHITE);
    binding.pieChartStatistics.setDrawHoleEnabled(true);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }
}
