package com.sg2022.we_got_the_moves.ui.training.tabs;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.DialogStartWorkoutBinding;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.Workout;

import java.util.ArrayList;
import java.util.List;

public class LastWorkoutsAdapter
    extends RecyclerView.Adapter<LastWorkoutsAdapter.LastWorkoutsListViewHolder> {

  private static final String TAG = "LastWorkoutAdapter";
  private final Fragment fragment;
  private final TrainingViewModel model;
  private List<Workout> workoutList;
  private List<Long> workoutIds;
  private List<FinishedWorkout> finishedWorkouts;
  private String exercisesString;

  public LastWorkoutsAdapter(@NonNull Fragment fragment, @NonNull TrainingViewModel model) {
    this.fragment = fragment;
    this.model = model;

    finishedWorkouts = new ArrayList<>();

    this.model
        .finishedWorkoutRepository
        .getOrderedFinishedWorkouts()
        .observe(
            fragment,
            finishedTraining -> {
              workoutIds = new ArrayList<>();
              workoutList = new ArrayList<>();
              Log.println(Log.DEBUG, TAG, finishedTraining.toString());
              finishedWorkouts = finishedTraining;
              for (int i = 0; i < finishedWorkouts.size() && workoutIds.size() < 3; i++) {
                if (!workoutIds.contains(finishedWorkouts.get(i).workoutId)) {
                  workoutIds.add(finishedWorkouts.get(i).workoutId);
                }
              }

              this.model
                  .workoutsRepository
                  .getAllWorkouts()
                  .observe(
                      fragment,
                      workout -> {
                        for (int i = 0; i < workoutIds.size(); i++) {
                          for (int j = 0; j < workout.size(); j++) {
                            if (workout.get(j).id == workoutIds.get(i)) {
                              workoutList.add(workout.get(j));
                              if (workoutList.size() == workoutIds.size()) break;
                            }
                          }
                        }
                        notifyDataSetChanged();
                      });

              notifyDataSetChanged();
            });
  }

  @NonNull
  @Override
  public LastWorkoutsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_workout_no_edit, parent, false);
    binding.setLifecycleOwner(this.fragment);
    return new LastWorkoutsListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull LastWorkoutsListViewHolder holder, int position) {
    Workout w = this.workoutList.get(position);
    holder.binding.setWorkout(w);
    holder.binding.workoutName.setText(w.name);
    holder.binding.workoutName.setOnClickListener(v -> showWorkoutDialog(w));
  }

  private void showWorkoutDialog(@NonNull Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstanceActivity());
    DialogStartWorkoutBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(MainActivity.getInstanceActivity()),
            R.layout.dialog_start_workout,
            null,
            false);
    builder
        .setView(binding.getRoot())
        .setPositiveButton(
            "Start",
            (dialog, id) -> {
              MainActivity.getInstanceActivity().openMediapipeActivity(w.id);
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
    binding.textviewStartWorkoutWorkout.setText(w.name);
    exercisesString = "";
    model
        .workoutsRepository
        .getAllExercises(w.id)
        .observe(
            fragment,
            exercises -> {
              for (Exercise e : exercises) {
                exercisesString += e.name + "\n";
              }
              binding.textviewStartWorkoutExercises.setText(exercisesString);
              notifyDataSetChanged();
            });
  }

  @Override
  public long getItemId(int position) {
    return this.workoutList.get(position).id;
  }

  @Override
  public int getItemCount() {
    if (workoutList == null) return 0;
    return workoutList.size();
  }

  protected static class LastWorkoutsListViewHolder extends RecyclerView.ViewHolder {
    public ItemWorkoutNoEditBinding binding;

    LastWorkoutsListViewHolder(ItemWorkoutNoEditBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
