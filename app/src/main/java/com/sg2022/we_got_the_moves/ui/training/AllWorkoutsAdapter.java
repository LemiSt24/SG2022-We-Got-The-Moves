package com.sg2022.we_got_the_moves.ui.training;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding;
import com.sg2022.we_got_the_moves.db.entity.Workout;

import java.util.List;

public class AllWorkoutsAdapter
    extends RecyclerView.Adapter<AllWorkoutsAdapter.AllWorkoutsListViewHolder> {

  private static final String TAG = "AllWorkoutAdapter";
  private final LifecycleOwner owner;
  private final TrainingViewModel model;
  private List<Workout> workoutList;
  private ItemWorkoutNoEditBinding binding;

  public AllWorkoutsAdapter(@NonNull LifecycleOwner owner, @NonNull TrainingViewModel model) {
    this.owner = owner;
    this.model = model;

    this.model
        .workoutsRepository
        .getAllWorkouts()
        .observe(
            owner,
            workouts -> {
              if (workouts == null || workouts.isEmpty()) workoutList.clear();
              else workoutList = workouts;
              notifyDataSetChanged();
            });
  }

  @NonNull
  @Override
  public AllWorkoutsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    this.binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_workout_no_edit, parent, false);
    binding.setLifecycleOwner(this.owner);
    return new AllWorkoutsListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull AllWorkoutsListViewHolder holder, int position) {
    Workout w = this.workoutList.get(position);
    holder.binding.setWorkout(w);
    holder.binding.workoutName.setText(w.name);
    holder.binding.workoutName.setOnClickListener(
        v -> MainActivity.getInstanceActivity().openMediapipeActivity(w.id));
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
    public ItemWorkoutNoEditBinding binding;

    AllWorkoutsListViewHolder(ItemWorkoutNoEditBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
