package com.sg2022.we_got_the_moves.ui.dashboard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemInstructionBinding;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.ui.workouts.adapter.ExerciseListAdapter;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutsViewModel;

import java.util.ArrayList;
import java.util.List;

public class InstructionListAdapter extends RecyclerView.Adapter<InstructionListAdapter.InstructionListViewHolder>{

    private static final String TAG = "InstructionListAdapter";

    private List<Exercise> exerciseList;
    private final LifecycleOwner owner;
    private final DashboardViewModel model;
    private ItemInstructionBinding binding;


    public InstructionListAdapter(@NonNull LifecycleOwner owner, @NonNull DashboardViewModel model) {
        this.exerciseList = new ArrayList<Exercise>();
        this.owner = owner;
        this.model = model;
        this.model.getRepository().getAllExercises().observe(owner, exercises -> {
            if (exercises == null || exercises.isEmpty()) {
                exerciseList.clear();
            } else {
                exerciseList = exercises;
            }
            notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    public InstructionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_instruction, parent, false);
        binding.setLifecycleOwner(this.owner);
        return new InstructionListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull  InstructionListViewHolder holder, int position) {
        Exercise e =  this.exerciseList.get(position);
        holder.binding.setExercise(e);
        holder.binding.elementInstruction.setText(e.name);
        holder.binding.elementInstruction.setOnClickListener(v -> MainActivity.getInstanceActivity().openInstructionActivity(e.id));
    }

    @Override
    public long getItemId(int position) {
        return this.exerciseList.get(position).id;
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }


    protected static class InstructionListViewHolder extends RecyclerView.ViewHolder{

        public ItemInstructionBinding binding;

        InstructionListViewHolder(ItemInstructionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}



