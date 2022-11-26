package com.sg2022.we_got_the_moves.ui.workouts.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.InputDialogNumberBinding;
import com.sg2022.we_got_the_moves.databinding.ItemExerciseBinding;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.ui.workouts.WorkoutsViewModel;

import java.util.List;

public class ExerciseListAdapter
    extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseItemViewHolder> {

  public static final String TAG = "ExerciseListAdapter";

  private final Fragment owner;
  private final WorkoutsViewModel model;
  private final List<WorkoutExerciseAndExercise> list;

  public ExerciseListAdapter(
      @NonNull Fragment owner,
      @NonNull WorkoutsViewModel model,
      List<WorkoutExerciseAndExercise> list) {
    this.owner = owner;
    this.model = model;
    this.list = list;
  }

  @NonNull
  @Override
  public ExerciseItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemExerciseBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_exercise, parent, false);
    return new ExerciseItemViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ExerciseItemViewHolder holder, int position) {
    WorkoutExerciseAndExercise wee = this.list.get(position);
    holder.binding.setWorkoutExerciseAndExercise(wee);
    holder.binding.buttonCounterExerciseItem.setOnClickListener(
        v -> showAmountDialog(wee, position));
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private void showAmountDialog(WorkoutExerciseAndExercise ewe, int position) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    InputDialogNumberBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.owner.getContext()),
            R.layout.input_dialog_number,
            null,
            false);
    binding.numberPickerNumberDialog.setMinValue(0);
    binding.numberPickerNumberDialog.setMaxValue(100);
    binding.numberPickerNumberDialog.setWrapSelectorWheel(true);
    binding.numberPickerNumberDialog.setValue(ewe.workoutExercise.amount);
    builder.setView(binding.getRoot());
    builder
        .setTitle(
            String.format(this.owner.getString(R.string.set_exercise_amount), ewe.exercise.name))
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              int amount = binding.numberPickerNumberDialog.getValue();
              if (amount == ewe.workoutExercise.amount) dialog.dismiss();
              if (amount == 0) {
                model.repository.deleteWorkoutExercise(ewe.workoutExercise);
                // notifyItemRemoved(position);
              } else {
                ewe.workoutExercise.amount = amount;
                model.repository.updateWorkoutExercise(ewe.workoutExercise);
                // notifyItemChanged(position);
              }
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  public static class ExerciseItemViewHolder extends RecyclerView.ViewHolder {

    public ItemExerciseBinding binding;

    public ExerciseItemViewHolder(ItemExerciseBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
