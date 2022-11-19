package com.sg2022.we_got_the_moves.ui.workouts.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemExerciseBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutsViewModel;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseItemViewHolder> {

    private static final String TAG = "ExerciseListAdapter";

    private List<Exercise> exercisesList;
    private final LifecycleOwner owner;
    private final WorkoutsViewModel model;
    private ItemExerciseBinding binding;
    private final long workoutId;

    public ExerciseListAdapter(@NonNull LifecycleOwner owner, @NonNull WorkoutsViewModel model, long workoutId) {
        this.exercisesList = new ArrayList<Exercise>();
        this.owner = owner;
        this.model = model;
        this.workoutId = workoutId;
        this.model.getRepository().getExercisesByWorkoutId(workoutId).observe(owner, new Observer<List<Exercise>>() {
            @Override
            public void onChanged(List<Exercise> exercises) {
                if (exercises == null || exercises.isEmpty()) {
                    exercisesList.clear();
                } else {
                    exercisesList = exercises;
                }
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public ExerciseItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_exercise, parent, false);
        binding.setLifecycleOwner(this.owner);
        return new ExerciseItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseItemViewHolder holder, int position) {
        Exercise e = this.exercisesList.get(position);
        holder.binding.setExercise(e);
        this.model.getRepository().getWorkoutExercise(this.workoutId, e.id).observe(this.owner, new Observer<WorkoutExercise>() {
            @Override
            public void onChanged(WorkoutExercise workoutExercise) {
                if (workoutExercise == null)
                    return;
                holder.binding.textCounterExerciseItem.setText(String.valueOf(workoutExercise.amount));
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return this.exercisesList.get(position).id;
    }

    @Override
    public int getItemCount() {
        return this.exercisesList.size();
    }

    protected static class ExerciseItemViewHolder extends RecyclerView.ViewHolder {

        public ItemExerciseBinding binding;

        public ExerciseItemViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
