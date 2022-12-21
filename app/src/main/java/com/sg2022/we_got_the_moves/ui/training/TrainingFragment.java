package com.sg2022.we_got_the_moves.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.databinding.FragmentTrainingBinding;

public class TrainingFragment extends Fragment {

  private static final String TAG = "TrainingsFragment";
  private TrainingViewModel model;
  private FragmentTrainingBinding binding;
  private AllWorkoutsAdapter adapterAllWorkouts;
  private LastWorkoutsAdapter adapterLastWorkouts;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TrainingViewModel.Factory factory =
        new TrainingViewModel.Factory(
            this.requireActivity().getApplication(), this.requireActivity());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(TrainingViewModel.class);
    this.adapterLastWorkouts = new LastWorkoutsAdapter(this, this.model);
    this.adapterAllWorkouts = new AllWorkoutsAdapter(this, this.model);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentTrainingBinding.inflate(inflater, container, false);

    binding.titleLastUsed.setText("Last Workouts");

    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    this.binding.lastWorkoutsRecycler.setLayoutManager(layoutManager);
    this.binding.lastWorkoutsRecycler.setAdapter(this.adapterLastWorkouts);

    binding.titleAllWorkouts.setText("All Workouts");

    layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    this.binding.allWorkoutsRecycler.setLayoutManager(layoutManager);
    this.binding.allWorkoutsRecycler.setAdapter(this.adapterAllWorkouts);

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }
}
