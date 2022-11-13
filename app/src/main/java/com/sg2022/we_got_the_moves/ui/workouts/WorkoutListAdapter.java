package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutBinding;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutViewModel;

import java.util.List;

public class WorkoutListAdapter extends RecyclerView.Adapter<WorkoutListAdapter.WorkoutItemViewHolder>{

    private static final String TAG = "WorkoutListAdapter";

    private List<Workout> workoutsList;
    Application app;
    ItemWorkoutBinding binding;

    WorkoutListAdapter(Application app, @NonNull List<Workout> workoutsList) {
        this.workoutsList = workoutsList;
    }

    @NonNull
    @Override
    public WorkoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_workout, parent, false);
        return new WorkoutItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutItemViewHolder holder, int position) {
        WorkoutViewModel.Factory factory = new WorkoutViewModel.Factory(
                app, this.getItemId(position));
        final WorkoutViewModel child = new ViewModelProvider((ViewModelStoreOwner) app, factory).get(WorkoutViewModel.class);


        Workout w = this.workoutsList.get(position);
        holder.binding.textTitleWorkoutItem.setText(String.format("   %s", w.name));
        holder.binding.deleteBtnWorkoutItem.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click on Delete Button", Toast.LENGTH_SHORT).show());
        holder.binding.editBtnWorkoutItem.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click on Edit Button", Toast.LENGTH_SHORT).show());
        holder.binding.checkBoxWorkoutItem.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click on Check Box", Toast.LENGTH_SHORT).show());
    }

    @Override
    public long getItemId(int position) {
        return this.workoutsList.get(position).id;
    }

    @Override
    public int getItemCount() {
        return this.workoutsList.size();
    }

    public List<Workout> getWorkoutsList() {
        return workoutsList;
    }

    public void setWorkoutsList(List<Workout> workoutsList) {
        this.workoutsList = workoutsList;
    }

    protected static class WorkoutItemViewHolder extends RecyclerView.ViewHolder{

        public ItemWorkoutBinding binding;

        public WorkoutItemViewHolder(ItemWorkoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
