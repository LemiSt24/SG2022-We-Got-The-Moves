package com.sg2022.we_got_the_moves.ui.training.tabs.overview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.ui.training.mediapipe.MediaPipeActivity;
import com.sg2022.we_got_the_moves.ui.workouts.WorkoutListAdapter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

public class RecentWorkoutsAdapter
    extends RecyclerView.Adapter<RecentWorkoutsAdapter.LastWorkoutsListViewHolder> {

  private static final String TAG = "LastWorkoutAdapter";
  private final TrainingOverviewFragment fragment;
  private final TrainingViewModel model;
  private List<Workout> workoutList;
  private List<Long> workoutIds;
  private final List<FinishedWorkout> finishedWorkouts;

  @SuppressLint("NotifyDataSetChanged")
  public RecentWorkoutsAdapter(
      @NonNull TrainingOverviewFragment fragment, @NonNull TrainingViewModel model) {
    this.fragment = fragment;
    this.model = model;
    this.workoutList = new ArrayList<>();
    this.finishedWorkouts = new ArrayList<>();
    this.workoutIds = new ArrayList<>();
    this.model
        .finishedWorkoutRepository
        .getOrderedFinishedWorkouts()
        .observe(
            fragment,
            finishedTraining -> {
              workoutIds = new ArrayList<>();
              workoutList = new ArrayList<>();
              Log.println(Log.DEBUG, TAG, finishedTraining.toString());
              this.finishedWorkouts.clear();
              this.finishedWorkouts.addAll(finishedTraining);
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
    ItemWorkoutNoEditBinding binding =
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
    AlertDialog.Builder builder = new AlertDialog.Builder(this.fragment.getContext());
    builder
        .setTitle(w.name)
        .setPositiveButton(
            "Start",
            (dialog, id) -> {
              this.model.userRepository.getUser(
                  new SingleObserver<>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}

                    @Override
                    public void onSuccess(@NonNull User user) {
                      Intent intent = new Intent(fragment.getContext(), MediaPipeActivity.class);
                      intent.putExtra(MediaPipeActivity.WORKOUT_ID, w.id);
                      intent.putExtra(MediaPipeActivity.CAMERA_FACING_FLAG, user.frontCamera);
                      intent.putExtra(MediaPipeActivity.TEXT_TO_SPEECH_FLAG, user.tts);
                      fragment.launchMediaPipe(intent);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                      Log.e(TAG, "Couln't retrieve user configuration");
                      e.printStackTrace();
                    }
                  });
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
    model.workoutsRepository.getAllWorkoutExerciseAndExerciseSingle(
        w.id,
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<WorkoutExerciseAndExercise> result) {
            if (result.isEmpty()) {
              Toast.makeText(
                      fragment.getContext(),
                      "Workout doesn't contain any Exercises",
                      Toast.LENGTH_SHORT)
                  .show();
              return;
            }
            List<WorkoutExerciseAndExercise> wees = new ArrayList<>(result);
            wees.sort(new WorkoutListAdapter.WorkoutExerciseComparator());
            List<String> sets = new ArrayList<>();
            wees.forEach(
                wee ->
                    wee.workoutExercise.amount.forEach(
                        amount ->
                            sets.add(
                                amount
                                    + (wee.exercise.isCountable() ? " x " : " s ")
                                    + wee.exercise.name)));
            String[] items = new String[sets.size()];
            items = sets.toArray(items);
            builder.setItems(items, null).create().show();
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, "Error when reading WorkoutExercises");
          }
        });
  }

  @Override
  public long getItemId(int position) {
    return this.workoutList.get(position).id;
  }

  @Override
  public int getItemCount() {
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
