package com.sg2022.we_got_the_moves.ui.workouts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutBinding;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutsViewModel;

import java.util.ArrayList;
import java.util.List;

public class WorkoutListAdapter extends RecyclerView.Adapter<WorkoutListAdapter.WorkoutItemViewHolder> {

    private static final String TAG = "WorkoutListAdapter";

    private List<Workout> workoutsList;
    private final LifecycleOwner owner;
    private final WorkoutsViewModel model;
    private ItemWorkoutBinding binding;

    public WorkoutListAdapter(@NonNull LifecycleOwner owner, @NonNull WorkoutsViewModel model) {
        this.workoutsList = new ArrayList<Workout>();
        this.owner = owner;
        this.model = model;
        this.model.getRepository().getAllWorkouts().observe(owner, new Observer<List<Workout>>() {
            @Override
            public void onChanged(List<Workout> workouts) {
                if (workouts == null || workouts.isEmpty()) {
                    workoutsList.clear();
                } else {
                    workoutsList = workouts;
                }
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public WorkoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_workout, parent, false);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerviewExercises.setLayoutManager(layoutManager);
        binding.setLifecycleOwner(this.owner);
        return new WorkoutItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutItemViewHolder holder, int position) {
        Workout w = this.workoutsList.get(position);
        holder.binding.setWorkout(w);
        holder.binding.deleteBtnWorkoutItem.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.getRepository().deleteWorkout(w);
                    }
                }
        );
        holder.binding.editBtnWorkoutItem.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click on Edit Button", Toast.LENGTH_SHORT).show());
        holder.binding.checkBoxWorkoutItem.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click on Check Box", Toast.LENGTH_SHORT).show());
        ExerciseListAdapter adapter = new ExerciseListAdapter(this.owner, this.model, w.id);
        holder.binding.recyclerviewExercises.setAdapter(adapter);
    }

    @Override
    public long getItemId(int position) {
        return this.workoutsList.get(position).id;
    }

    @Override
    public int getItemCount() {
        return this.workoutsList.size();
    }

    protected static class WorkoutItemViewHolder extends RecyclerView.ViewHolder {

        public ItemWorkoutBinding binding;

        public WorkoutItemViewHolder(ItemWorkoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
