package com.sg2022.we_got_the_moves.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sg2022.we_got_the_moves.databinding.FragmentTrainingBinding;
import com.sg2022.we_got_the_moves.ui.CustomFragmentStateAdapter;
import com.sg2022.we_got_the_moves.ui.training.tabs.overview.TrainingOverviewFragment;
import com.sg2022.we_got_the_moves.ui.training.tabs.playback.PlaybackFragment;
import com.sg2022.we_got_the_moves.ui.training.tabs.recording.RecordingFragment;

import java.util.Arrays;
import java.util.List;

public class TrainingFragment extends Fragment {

  private static final String TAG = TrainingFragment.class.getSimpleName().toUpperCase();
  private List<Class<? extends Fragment>> tabFragments;
  private List<String> tabLabels;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.tabFragments =
        Arrays.asList(
            TrainingOverviewFragment.class, PlaybackFragment.class);
    this.tabLabels = Arrays.asList("Training Overview", "Playbacks");
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FragmentTrainingBinding binding = FragmentTrainingBinding.inflate(inflater, container, false);
    binding.viewPagerTraining.setAdapter(new CustomFragmentStateAdapter(this, this.tabFragments));
    new TabLayoutMediator(
            binding.tabLayoutTraining,
            binding.viewPagerTraining,
            (tab, position) -> tab.setText(tabLabels.get(position)))
        .attach();
    return binding.getRoot();
  }
}
