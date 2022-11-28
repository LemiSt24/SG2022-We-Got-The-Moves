package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentWorkoutsBinding;
import com.sg2022.we_got_the_moves.databinding.InputDialogTextBinding;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.ui.workouts.adapter.WorkoutListAdapter;

public class WorkoutsFragment extends Fragment {

  private static final String TAG = "WorkoutListFragment";

  private WorkoutListAdapter adapter;
  private WorkoutsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WorkoutsViewModel.Factory factory =
        new WorkoutsViewModel.Factory(this.requireActivity().getApplication(), this);
    this.model = new ViewModelProvider(this, factory).get(WorkoutsViewModel.class);
    this.adapter = new WorkoutListAdapter(this, this.model);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FragmentWorkoutsBinding binding =
        DataBindingUtil.inflate(inflater, R.layout.fragment_workouts, container, false);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    binding.recyclerviewWorkouts.setLayoutManager(layoutManager);
    binding.recyclerviewWorkouts.setAdapter(this.adapter);
    binding.floatingBtnWorkouts.setOnClickListener(v -> this.showNewDialog());
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  private void showNewDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
    InputDialogTextBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.getContext()), R.layout.input_dialog_text, null, false);
    Workout newItem = new Workout(0, getString(R.string.untitled));
    binding.setText(newItem.name);
    builder
        .setView(binding.getRoot())
        .setTitle(getString(R.string.new_workout))
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              String text = binding.textViewTextDialog.getText().toString();
              if (!text.isEmpty()) {
                newItem.name = text;
                this.model.repository.insertWorkout(newItem);
              }
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }
}
