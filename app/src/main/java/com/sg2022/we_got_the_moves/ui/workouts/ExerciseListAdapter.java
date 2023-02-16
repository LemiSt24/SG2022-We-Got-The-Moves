package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.DialogInstructionBinding;
import com.sg2022.we_got_the_moves.databinding.DialogNumberInputBinding;
import com.sg2022.we_got_the_moves.databinding.DialogTimeInputBinding;
import com.sg2022.we_got_the_moves.databinding.ItemExerciseBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.ui.TimeFormatUtil;

import java.util.List;

import kotlin.Triple;

public class ExerciseListAdapter
    extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseItemViewHolder> {

  private static final String TAG = "ExerciseListAdapter";

  private final Fragment fragment;
  private final WorkoutsViewModel model;
  private final List<WorkoutExerciseAndExercise> list;

  public ExerciseListAdapter(
      @NonNull Fragment fragment,
      @NonNull WorkoutsViewModel model,
      @NonNull List<WorkoutExerciseAndExercise> list) {
    this.fragment = fragment;
    this.model = model;
    this.list = list;
    //for (WorkoutExerciseAndExercise wee : list) Log.println(Log.DEBUG, "test","orderNum :" +  wee.workoutExercise.orderNum);
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
    for (int i = 0; i < wee.workoutExercise.amount.size(); i++){
        addAmountButton(holder, position);
    }
    holder.binding.imagebuttonInfoExerciseItem.setOnClickListener(
        v -> showInstructionDialog(wee.exercise));
    holder.binding.imagebuttonAddSetExerciseItem.setOnClickListener(
        v -> addAmountButton(holder, position)
    );
    holder.binding.imagebuttonRemoveSetExerciseItem.setOnClickListener(
        v -> deleteLastAmountButton(holder, position)
    );
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private void addAmountButton(@NonNull ExerciseItemViewHolder holder, int position){
      Log.println(Log.DEBUG, "test", list.get(position).exercise.name);
      WorkoutExerciseAndExercise wee = list.get(position);
      Button btn = new Button(this.fragment.getContext());
      int buttonNumber = holder.binding.linearLayoutButtonsAmountExerciseItem.getChildCount();
      if (wee.workoutExercise.amount.size() <= buttonNumber){
          wee.workoutExercise.amount.add(5);
      }

      btn.getBackground().setColorFilter(btn.getContext().getResources().
              getColor(R.color.sg_design_green), PorterDuff.Mode.MULTIPLY);
      btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
      btn.setTypeface(Typeface.DEFAULT_BOLD);
      btn.setId(buttonNumber);
      if (wee.exercise.isCountable()){
          btn.setText(String.valueOf(wee.workoutExercise.amount.get(buttonNumber)));
      }
      else btn.setText(TimeFormatUtil.formatTimeHhmmss(wee.workoutExercise.amount.get(buttonNumber)));

      btn.setOnClickListener(v -> showAmountDialog(holder, wee, v.getId()));
      holder.binding.linearLayoutButtonsAmountExerciseItem.addView(btn);
  }

  private void deleteLastAmountButton(@NonNull ExerciseItemViewHolder holder, int position){
      if (holder.binding.linearLayoutButtonsAmountExerciseItem.getChildCount() <= 1) return;
      WorkoutExerciseAndExercise wee = list.get(position);
      int lastButtonPos = holder.binding.linearLayoutButtonsAmountExerciseItem.getChildCount() - 1;
      holder.binding.linearLayoutButtonsAmountExerciseItem.removeView(
              holder.binding.linearLayoutButtonsAmountExerciseItem.findViewById(lastButtonPos)
      );
      wee.workoutExercise.amount.remove(lastButtonPos);
  }

  private void showAmountDialog(@NonNull ExerciseItemViewHolder holder, WorkoutExerciseAndExercise ewe, int set) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.fragment.getContext());
    ViewDataBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.fragment.getContext()),
            ewe.exercise.unit == Exercise.UNIT.REPETITION
                ? R.layout.dialog_number_input
                : R.layout.dialog_time_input,
            null,
            false);
    if (binding instanceof DialogNumberInputBinding) {
      DialogNumberInputBinding b = (DialogNumberInputBinding) binding;
      b.numberPickerNumberDialog.setMinValue(1);
      b.numberPickerNumberDialog.setMaxValue(100);
      b.numberPickerNumberDialog.setWrapSelectorWheel(true);
      b.numberPickerNumberDialog.setValue(ewe.workoutExercise.amount.get(set));
      builder
          .setView(binding.getRoot())
          .setTitle(
              String.format(
                  this.fragment.getString(R.string.set_exercise_amount), ewe.exercise.name))
          .setPositiveButton(
              R.string.yes,
              (dialog, id) -> {
                int amount = b.numberPickerNumberDialog.getValue();
                if (amount == ewe.workoutExercise.amount.get(set)){
                    dialog.dismiss();
                }
                else if (amount == 0) {
                  model.repository.deleteWorkoutExercise(ewe.workoutExercise);
                } else {
                  ewe.workoutExercise.amount.set(set, amount);
                  ((Button)holder.binding.linearLayoutButtonsAmountExerciseItem.
                          findViewById(set)).setText(String.valueOf(amount));
                }
                dialog.dismiss();
              })
          .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
          .create()
          .show();
    } else {
      DialogTimeInputBinding b = (DialogTimeInputBinding) binding;
      Triple<Integer, Integer, Integer> triple =
          TimeFormatUtil.secsToHhmmss(ewe.workoutExercise.amount.get(set));
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
              String.format(
                  this.fragment.getString(R.string.set_exercise_amount), ewe.exercise.name))
          .setPositiveButton(
              R.string.yes,
              (dialog, id) -> {
                int amount =
                    TimeFormatUtil.hhmmssToSecs(
                        new Triple<>(
                            b.numberPickerHoursDialog.getValue(),
                            b.numberPickerMinutesDialog.getValue(),
                            b.numberPickerSecondsDialog.getValue()));
                if (amount == ewe.workoutExercise.amount.get(set)) dialog.dismiss();
                if (amount == 0) {
                  model.repository.deleteWorkoutExercise(ewe.workoutExercise);
                } else {
                  ewe.workoutExercise.amount.set(set, amount);
                  ((Button)holder.binding.linearLayoutButtonsAmountExerciseItem.findViewById(set)).
                          setText(TimeFormatUtil.formatTimeHhmmss(amount));
                }
                dialog.dismiss();
              })
          .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
          .create()
          .show();
    }
  }

  private void showInstructionDialog(@NonNull Exercise e) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.fragment.getContext());
    DialogInstructionBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.fragment.requireContext()),
            R.layout.dialog_instruction,
            null,
            false);
    binding.setExercise(e);
    this.fragment.getLifecycle().addObserver(binding.youtubePlayerViewInstructionDialog);
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
        .setTitle(String.format(this.fragment.getString(R.string.instruction_title), e.name))
        .setNeutralButton(R.string.ok, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  public static class ExerciseItemViewHolder extends RecyclerView.ViewHolder {

    public final ItemExerciseBinding binding;

    public ExerciseItemViewHolder(@NonNull ItemExerciseBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
