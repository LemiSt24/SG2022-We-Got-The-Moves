package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.InputDialogInstructionBinding;
import com.sg2022.we_got_the_moves.databinding.InputDialogNumberBinding;
import com.sg2022.we_got_the_moves.databinding.InputDialogTimeBinding;
import com.sg2022.we_got_the_moves.databinding.ItemExerciseBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.util.TimeFormatUtil;

import java.util.List;

import kotlin.Triple;

public class ExerciseListAdapter
    extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseItemViewHolder> {

  private static final String TAG = "ExerciseListAdapter";

  private final Fragment owner;
  private final WorkoutsViewModel model;
  private final List<WorkoutExerciseAndExercise> list;

  public ExerciseListAdapter(
      @NonNull Fragment owner,
      @NonNull WorkoutsViewModel model,
      @NonNull List<WorkoutExerciseAndExercise> list) {
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
    holder.binding.setWee(wee);
    holder.binding.buttonAmountExerciseItem.setOnClickListener(v -> showAmountDialog(wee));
    holder.binding.imagebuttonInfoExerciseItem.setOnClickListener(
        v -> showInstructionDialog(wee.exercise));
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private void showAmountDialog(WorkoutExerciseAndExercise ewe) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    ViewDataBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.owner.getContext()),
            ewe.exercise.unit == Exercise.UNIT.REPETITION
                ? R.layout.input_dialog_number
                : R.layout.input_dialog_time,
            null,
            false);
    if (binding instanceof InputDialogNumberBinding) {
      InputDialogNumberBinding b = (InputDialogNumberBinding) binding;
      b.numberPickerNumberDialog.setMinValue(0);
      b.numberPickerNumberDialog.setMaxValue(100);
      b.numberPickerNumberDialog.setWrapSelectorWheel(true);
      b.numberPickerNumberDialog.setValue(ewe.workoutExercise.amount);
      builder
          .setView(binding.getRoot())
          .setTitle(
              String.format(this.owner.getString(R.string.set_exercise_amount), ewe.exercise.name))
          .setPositiveButton(
              R.string.yes,
              (dialog, id) -> {
                int amount = b.numberPickerNumberDialog.getValue();
                if (amount == ewe.workoutExercise.amount) dialog.dismiss();
                if (amount == 0) {
                  model.repository.deleteWorkoutExercise(ewe.workoutExercise);
                } else {
                  ewe.workoutExercise.amount = amount;
                  model.repository.updateWorkoutExercise(ewe.workoutExercise);
                }
                dialog.dismiss();
              })
          .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
          .create()
          .show();
    } else {
      InputDialogTimeBinding b = (InputDialogTimeBinding) binding;
      Triple<Integer, Integer, Integer> triple =
          TimeFormatUtil.secsToHhmmss(ewe.workoutExercise.amount);
      b.numberPickerHoursDialog.setMinValue(0);
      b.numberPickerHoursDialog.setMaxValue(23);
      b.numberPickerHoursDialog.setValue(triple.getFirst());
      b.numberPickerHoursDialog.setWrapSelectorWheel(true);

      b.numberPickerMinutesDialog.setMinValue(0);
      b.numberPickerMinutesDialog.setMaxValue(59);
      b.numberPickerMinutesDialog.setValue(triple.getSecond());
      b.numberPickerMinutesDialog.setWrapSelectorWheel(true);

      b.numberPickerSecondsDialog.setMinValue(0);
      b.numberPickerSecondsDialog.setMaxValue(59);
      b.numberPickerSecondsDialog.setValue(triple.getThird());
      b.numberPickerSecondsDialog.setWrapSelectorWheel(true);
      builder
          .setView(binding.getRoot())
          .setTitle(
              String.format(this.owner.getString(R.string.set_exercise_amount), ewe.exercise.name))
          .setPositiveButton(
              R.string.yes,
              (dialog, id) -> {
                int amount =
                    TimeFormatUtil.hhmmssToSecs(
                        new Triple<>(
                            b.numberPickerHoursDialog.getValue(),
                            b.numberPickerMinutesDialog.getValue(),
                            b.numberPickerSecondsDialog.getValue()));
                if (amount == ewe.workoutExercise.amount) dialog.dismiss();
                if (amount == 0) {
                  model.repository.deleteWorkoutExercise(ewe.workoutExercise);
                } else {
                  ewe.workoutExercise.amount = amount;
                  model.repository.updateWorkoutExercise(ewe.workoutExercise);
                }
                dialog.dismiss();
              })
          .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
          .create()
          .show();
    }
  }

  private void showInstructionDialog(@NonNull Exercise e) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    InputDialogInstructionBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.owner.requireContext()),
            R.layout.input_dialog_instruction,
            null,
            false);
    binding.setExercise(e);
    this.owner.getLifecycle().addObserver(binding.youtubePlayerViewInstructionDialog);
    binding.youtubePlayerViewInstructionDialog.addYouTubePlayerListener(
        new AbstractYouTubePlayerListener() {
          @Override
          public void onReady(@NonNull YouTubePlayer youTubePlayer) {
            youTubePlayer.loadVideo(e.youtubeId, 0);
          }

          @Override
          public void onError(
              @NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
            super.onError(youTubePlayer, error);
            Log.e(TAG, error.toString());
            binding.youtubePlayerViewInstructionDialog.release();
          }
        });
    builder
        .setOnDismissListener(dialog -> binding.youtubePlayerViewInstructionDialog.release())
        .setView(binding.getRoot())
        .setTitle(String.format(this.owner.getString(R.string.instruction_title), e.name))
        .setNeutralButton(R.string.ok, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  public static class ExerciseItemViewHolder extends RecyclerView.ViewHolder {

    public ItemExerciseBinding binding;

    public ExerciseItemViewHolder(@NonNull ItemExerciseBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
