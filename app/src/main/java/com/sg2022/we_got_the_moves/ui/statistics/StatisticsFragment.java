package com.sg2022.we_got_the_moves.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsBinding;
import com.sg2022.we_got_the_moves.ui.CustomFragmentStateAdapter;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.DailyOverviewFragment;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.ExercisesOverviewFragment;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.WeeklyOverviewFragment;

import java.util.Arrays;
import java.util.List;

public class StatisticsFragment extends Fragment {

  private final String TAG = "StatisticsFragment";
  private List<Class<? extends Fragment>> tabFragments;
  private List<String> tabLabels;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.tabFragments =
        Arrays.asList(
            DailyOverviewFragment.class,
            WeeklyOverviewFragment.class,
            ExercisesOverviewFragment.class);
    this.tabLabels = Arrays.asList("Calories", "Weekly Overview", "Exercise Analysis");
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FragmentStatisticsBinding binding =
        FragmentStatisticsBinding.inflate(inflater, container, false);
    binding.viewPagerStatistics.setAdapter(new CustomFragmentStateAdapter(this, this.tabFragments));
    new TabLayoutMediator(
            binding.tabLayoutStatistics,
            binding.viewPagerStatistics,
            (tab, position) -> tab.setText(this.tabLabels.get(position)))
        .attach();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }
}
