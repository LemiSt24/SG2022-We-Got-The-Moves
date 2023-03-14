package com.sg2022.we_got_the_moves.ui.training.tabs.overview;


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
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.ui.workouts.WorkoutListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class AllWorkoutsAdapter
    extends RecyclerView.Adapter<AllWorkoutsAdapter.AllWorkoutsListViewHolder> {

  private static final String TAG = "AllWorkoutAdapter";
  private final Fragment fragment;
  private final TrainingViewModel model;
  private List<Workout> workoutList;
  private String exercisesString;

  public AllWorkoutsAdapter(@NonNull Fragment fragment, @NonNull TrainingViewModel model) {
    this.fragment = fragment;
    this.model = model;

    //getting all workouts for displaying the names
    this.model.workoutsRepository.getAllWorkoutsSingle(new SingleObserver<List<Workout>>() {
      @Override
      public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Workout> workouts) {
        workoutList = workouts;
        notifyDataSetChanged();
      }

      @Override
      public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
        workoutList = new ArrayList<>();
      }
    });

  }

  @NonNull
  @Override
  public AllWorkoutsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_workout_no_edit, parent, false);
    binding.setLifecycleOwner(this.fragment);
    return new AllWorkoutsListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull AllWorkoutsListViewHolder holder, int position) {
    Workout w = this.workoutList.get(position);
    holder.binding.setWorkout(w);
    holder.binding.workoutName.setText(w.name);
    holder.binding.workoutName.setOnClickListener(v -> showWorkoutDialog(w));
  }

  /**
   * Method for displaying the summary of the workout and
   * starting the MediaPipe Activity using this Workout
   * @param w Workout to be started
   */
  private void showWorkoutDialog(@NonNull Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstanceActivity());
    DialogStartWorkoutBinding binding =
            DataBindingUtil.inflate(
                    LayoutInflater.from(MainActivity.getInstanceActivity()),
                    R.layout.dialog_start_workout,
                    null,
                    false);

    //building the dialog box for summary and starting the Workout
    builder
            .setView(binding.getRoot())
            .setPositiveButton(
                    "Start",
                    (dialog, id) -> {
                      MainActivity.getInstanceActivity().openMediapipeActivity(w.id,
                              binding.checkboxActiveRecording.isChecked());
                      dialog.dismiss();
                    })
            .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
            .create()
            .show();
    binding.textviewStartWorkoutWorkout.setText(w.name);

    //getting all Exercises of the Workout for displaying the summary
    exercisesString = "";
    model
            .workoutsRepository
            .getAllWorkoutExerciseAndExercise(w.id)
            .observe(
                    fragment,
                    wee -> {
                      wee.sort(new WorkoutListAdapter.WorkoutExerciseComparator());
                      for (WorkoutExerciseAndExercise e : wee) {

                        //differentiation between countable and time bases exercises
                        if (e.exercise.isCountable()) {
                          for (int amount : e.workoutExercise.amount) {
                            exercisesString += amount + " x " + e.exercise.name + "\n";
                          }
                        } else
                          for (int amount : e.workoutExercise.amount) {
                            exercisesString += amount + " s " + e.exercise.name + "\n";
                          }
                      }
                      if (exercisesString == "")
                        exercisesString = fragment.getString(R.string.empty_workout_error);
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

  protected static class AllWorkoutsListViewHolder extends RecyclerView.ViewHolder {
    public final ItemWorkoutNoEditBinding binding;

    AllWorkoutsListViewHolder(ItemWorkoutNoEditBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
