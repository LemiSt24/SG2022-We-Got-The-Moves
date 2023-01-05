package com.sg2022.we_got_the_moves.ui.statistics.tabs;

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

import com.github.mikephil.charting.data.BarDataSet;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsExercisesBinding;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class ExercisesOverviewFragment extends Fragment {
  private final String TAG = "ExercisesOverviewFragment";

  private FragmentStatisticsExercisesBinding binding;
  private StatisticsViewModel model;
  private MutableLiveData<BarDataSet> barDataSet;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(
            this.requireActivity().getApplication(), this.requireActivity());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    this.barDataSet = new MutableLiveData<>(new BarDataSet(new ArrayList<>(), "Data"));
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsExercisesBinding.inflate(inflater, container, false);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    this.requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int minScreenSize =
        (int) (Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels) * 0.8);
    this.binding.barChartExercisesStatistics.setMinimumWidth(minScreenSize);
    this.binding.barChartExercisesStatistics.setMinimumHeight(minScreenSize);
    return binding.getRoot();
  }

  private void loadData() {

    this.model.workoutsRepository.getAllExerciseAndFinishedExercisesSingle(
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<ExerciseAndFinishedExercises> list) {}

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private void setupBarChart() {
    /*    this.binding.barChartWeeklyStatistics.getLegend().setEnabled(false);
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
    this.binding.barChartWeeklyStatistics.setVerticalScrollBarEnabled(true);*/
  }

  /*  private String formatValue(int progress, float value) {
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
  }*/

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroy() {
    this.binding = null;
    this.model = null;
    this.barDataSet = null;
    super.onDestroy();
  }
}
