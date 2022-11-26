package com.sg2022.we_got_the_moves.ui.workouts.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutBinding;
import com.sg2022.we_got_the_moves.databinding.TextInputDialogBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.ui.workouts.WorkoutsViewModel;
import com.sg2022.we_got_the_moves.ui.workouts.util.WorkoutExpandUtil;
import com.sg2022.we_got_the_moves.ui.workouts.util.WorkoutListDiffUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class WorkoutListAdapter
    extends RecyclerView.Adapter<WorkoutListAdapter.WorkoutItemViewHolder> {

  public static final String TAG = "WorkoutListAdapter";
  private final Fragment owner;
  private final WorkoutsViewModel model;
  private List<WorkoutAndWorkoutExerciseAndExercise> list;

  public WorkoutListAdapter(@NonNull Fragment owner, @NonNull WorkoutsViewModel model) {
    this.owner = owner;
    this.model = model;
    this.list = new ArrayList<>();
    this.model.data.observe(
        owner,
        list -> {
          if (list == null) list = new ArrayList<>();
          WorkoutListDiffUtil workoutDiff = new WorkoutListDiffUtil(this.list, list);
          DiffUtil.DiffResult diff = DiffUtil.calculateDiff(workoutDiff);
          this.list = list;
          diff.dispatchUpdatesTo(this);
        });
  }

  @NonNull
  @Override
  public WorkoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemWorkoutBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_workout, parent, false);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false);
    binding.recyclerviewExercises.setLayoutManager(layoutManager);
    return new WorkoutItemViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull WorkoutItemViewHolder holder, int position) {
    Workout w = this.list.get(position).workout;
    WorkoutExpandUtil util = new WorkoutExpandUtil();
    List<WorkoutExerciseAndExercise> we = this.list.get(position).workoutAndExercises;
    holder.binding.setWorkout(w);
    holder.binding.setUtil(util);

    holder.binding.editBtnWorkoutItem.setOnClickListener(v -> showEditDialog(w));
    holder.binding.copyBtnWorkoutItem.setOnClickListener(v -> showCopyDialog(w));

    ExerciseListAdapter adapter = new ExerciseListAdapter(this.owner, this.model, we);
    holder.binding.addBtnWorkoutItem.setOnClickListener(v -> showAddDialog(w));
    holder.binding.deleteBtnWorkoutItem.setOnClickListener(v -> showDeleteDialog(w));
    holder.binding.expandBtnWorkoutItem.setOnClickListener(
        v -> {
          util.switchValue();
          holder.binding.recyclerviewExercises.setVisibility(util.getVisibility());
          holder.binding.expandBtnWorkoutItem.setImageResource(util.getIcon());
        });
    holder.binding.recyclerviewExercises.setVisibility(util.getVisibility());
    holder.binding.recyclerviewExercises.setAdapter(adapter);
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private void showDeleteDialog(Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    builder
        .setTitle(String.format(this.owner.getString(R.string.delete_workout_titel), w.name))
        .setMessage(R.string.delete_workout_message)
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              this.model.repository.deleteWorkout(w);
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  private void showCopyDialog(Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    builder
        .setTitle(String.format(this.owner.getString(R.string.copy_workout_titel), w.name))
        .setMessage(R.string.copy_workout_message)
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              Workout copy = new Workout(0, w.name);
              this.model.repository.insertWorkout(
                  copy,
                  new SingleObserver<Long>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}

                    @Override
                    public void onSuccess(@NonNull Long aLong) {
                      model.repository.getAllWorkoutExercise(
                          w.id,
                          new MaybeObserver<List<WorkoutExercise>>() {

                            @Override
                            public void onSubscribe(@NonNull Disposable d) {}

                            @Override
                            public void onSuccess(@NonNull List<WorkoutExercise> workoutExercises) {
                              if (workoutExercises.isEmpty()) return;
                              for (WorkoutExercise we : workoutExercises) {
                                we.workoutId = aLong;
                              }
                              model.repository.insertWorkoutExercise(workoutExercises);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {}

                            @Override
                            public void onComplete() {}
                          });
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {}
                  });
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  private void showEditDialog(Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    TextInputDialogBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.owner.getContext()), R.layout.text_input_dialog, null, false);
    binding.setWorkout(w);
    builder.setView(binding.getRoot());
    builder
        .setTitle(String.format(this.owner.getString(R.string.set_workout_title), w.name))
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              String text = binding.textViewTextDialog.getText().toString();
              if (!text.equals(w.name) && !text.isEmpty()) {
                w.name = text;
                this.model.repository.updateWorkout(w);
              }
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  private void showAddDialog(Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    this.model.repository.getAllNotContainedExercise(
        w.id,
        new MaybeObserver<List<Exercise>>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<Exercise> list) {
            if (list.isEmpty()) {
              builder.setTitle(String.format(owner.getString(R.string.select_exercises), w.name));
              builder.setMessage(R.string.no_items);
              builder
                  .setPositiveButton(R.string.yes, (dialog, id) -> dialog.dismiss())
                  .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                  .create()
                  .show();
            } else {
              String[] items = new String[list.size()];
              for (int i = 0; i < items.length; i++) {
                items[i] = list.get(i).name;
              }
              boolean[] checkedList = new boolean[list.size()];
              builder.setMultiChoiceItems(
                  items, checkedList, (dialog, which, isChecked) -> checkedList[which] = isChecked);
              builder.setTitle(String.format(owner.getString(R.string.select_exercises), w.name));
              builder
                  .setPositiveButton(
                      R.string.yes,
                      (dialog, id) -> {
                        List<WorkoutExercise> result = new ArrayList<>();
                        for (int i = 0; i < checkedList.length; i++) {
                          if (checkedList[i]) {
                            result.add(new WorkoutExercise(w.id, list.get(i).id, 5));
                          }
                        }
                        if (result.size() > 0) {
                          model.repository.insertWorkoutExercise(result);
                        }
                        dialog.dismiss();
                      })
                  .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                  .create()
                  .show();
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {}

          @Override
          public void onComplete() {}
        });
  }

  public static class WorkoutItemViewHolder extends RecyclerView.ViewHolder {

    public ItemWorkoutBinding binding;

    public WorkoutItemViewHolder(ItemWorkoutBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
