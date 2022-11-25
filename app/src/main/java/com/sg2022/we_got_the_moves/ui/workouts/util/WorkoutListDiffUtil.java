package com.sg2022.we_got_the_moves.ui.workouts.util;

import androidx.recyclerview.widget.DiffUtil;

import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExerciseAndExercise;

import java.util.List;
import java.util.Objects;

public class WorkoutListDiffUtil extends DiffUtil.Callback {

    private final List<WorkoutAndWorkoutExerciseAndExercise> oldList;
    private final List<WorkoutAndWorkoutExerciseAndExercise> newList;

    public WorkoutListDiffUtil(List<WorkoutAndWorkoutExerciseAndExercise> oldList, List<WorkoutAndWorkoutExerciseAndExercise> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return this.oldList.size();
    }

    @Override
    public int getNewListSize() {
        return this.newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return this.oldList.get(oldItemPosition).workout == this.newList.get(newItemPosition).workout;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return this.oldList.get(oldItemPosition).workoutAndExercises == this.newList.get(newItemPosition).workoutAndExercises;
    }
}
