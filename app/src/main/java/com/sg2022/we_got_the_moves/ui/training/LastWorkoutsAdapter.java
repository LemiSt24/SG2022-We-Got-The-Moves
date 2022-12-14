package com.sg2022.we_got_the_moves.ui.training;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutNoEditBinding;
import com.sg2022.we_got_the_moves.db.entity.FinishedTraining;
import com.sg2022.we_got_the_moves.db.entity.Workout;

import java.util.ArrayList;
import java.util.List;


public class LastWorkoutsAdapter
        extends RecyclerView.Adapter<LastWorkoutsAdapter.LastWorkoutsListViewHolder> {

    private static final String TAG = "LastWorkoutAdapter";
    private final LifecycleOwner owner;
    private final TrainingViewModel model;
    private List<Workout> workoutList;
    private ItemWorkoutNoEditBinding binding;
    private List<Long> workoutIds;

    public LastWorkoutsAdapter(@NonNull LifecycleOwner owner, @NonNull TrainingViewModel model) {
        this.owner = owner;
        this.model = model;

        workoutIds = new ArrayList<>();
        workoutList = new ArrayList<>();
        this.model.repository.getNLastDistinctWorkoutIds(3).observe(
                owner, finishedTraining -> {
                    Log.println(Log.DEBUG, TAG, finishedTraining.toString());
                    workoutIds = finishedTraining;
                    for (int i = 0; i < workoutIds.size(); i++) {
                        this.model.workoutsRepository.getWorkout(workoutIds.get(i)).observe(
                                owner, workout -> {
                                    workoutList.add(workout);
                                    notifyDataSetChanged();
                                }
                        );
                    }
                    notifyDataSetChanged();
                }
        );

    }

    @NonNull
    @Override
    public LastWorkoutsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.println(Log.DEBUG, TAG, "on Create Viewholder");
        this.binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_workout_no_edit, parent, false);
        binding.setLifecycleOwner(this.owner);
        return new LastWorkoutsListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LastWorkoutsListViewHolder holder, int position) {
        Workout w = this.workoutList.get(position);
        holder.binding.setWorkout(w);
        holder.binding.workoutName.setText(w.name);
        holder.binding.workoutName.setOnClickListener(v -> MainActivity.getInstanceActivity().openMediapipeActivity(w.id));
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

    protected static  class LastWorkoutsListViewHolder extends RecyclerView.ViewHolder {
        public ItemWorkoutNoEditBinding binding;

        LastWorkoutsListViewHolder(ItemWorkoutNoEditBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
