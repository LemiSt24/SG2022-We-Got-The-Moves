package com.sg2022.we_got_the_moves.ui.training.tabs.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentTrainingOverviewBinding;

public class TrainingOverviewFragment extends Fragment {

  private static final String TAG = "TrainingOverviewFragment";
  private AllWorkoutsAdapter adapterAllWorkouts;
  private LastWorkoutsAdapter adapterLastWorkouts;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TrainingViewModel.Factory factory =
        new TrainingViewModel.Factory(this.requireActivity().getApplication());
    TrainingViewModel model =
        new ViewModelProvider(this.requireActivity(), factory).get(TrainingViewModel.class);
    this.adapterLastWorkouts = new LastWorkoutsAdapter(this, model);
    this.adapterAllWorkouts = new AllWorkoutsAdapter(this, model);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FragmentTrainingOverviewBinding binding =
        FragmentTrainingOverviewBinding.inflate(inflater, container, false);
    binding.textviewRecentlyTrainingsOverview.setText(R.string.last_workouts);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    binding.recyclerviewTrainingsOverview.setLayoutManager(layoutManager);
    binding.recyclerviewTrainingsOverview.setAdapter(this.adapterLastWorkouts);
    binding.textviewAllTrainingsOverview.setText(R.string.all_workouts);
    layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    binding.recyclerviewAllTrainingsOverview.setLayoutManager(layoutManager);
    binding.recyclerviewAllTrainingsOverview.setAdapter(this.adapterAllWorkouts);
    return binding.getRoot();
  }
}
