package com.sg2022.we_got_the_moves.ui.workouts;

import android.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.primitives.Booleans;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.InputDialogTextBinding;
import com.sg2022.we_got_the_moves.databinding.ItemWorkoutBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutAndWorkoutExercises;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class WorkoutListAdapter
    extends RecyclerView.Adapter<WorkoutListAdapter.WorkoutItemViewHolder> {

  private static final String TAG = "WorkoutListAdapter";

  private final Fragment owner;
  private final WorkoutsViewModel model;
  private List<WorkoutAndWorkoutExercises> list;

  public WorkoutListAdapter(@NonNull Fragment owner, @NonNull WorkoutsViewModel model) {
    this.owner = owner;
    this.model = model;
    this.list = new ArrayList<>();
    this.model.data.observe(
        owner,
        list -> {
          if (list == null) {
            list = new ArrayList<>();
          }
          if (this.list == list) return;

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
    List<WorkoutExerciseAndExercise> we = this.list.get(position).workoutAndExercises;
    holder.binding.setWorkout(w);
    holder.binding.setVisible(false);

    holder.binding.editBtnWorkoutItem.setOnClickListener(v -> showEditDialog(w));
    holder.binding.copyBtnWorkoutItem.setOnClickListener(v -> showCopyDialog(w));
    holder.binding.addBtnWorkoutItem.setOnClickListener(v -> showAddDialog(w));
    holder.binding.deleteBtnWorkoutItem.setOnClickListener(v -> showDeleteDialog(w));
    holder.binding.expandBtnWorkoutItem.setOnClickListener(
        v -> holder.binding.setVisible(!holder.binding.getVisible()));
    ExerciseListAdapter adapter = new ExerciseListAdapter(this.owner, this.model, we);
    holder.binding.recyclerviewExercises.setAdapter(adapter);
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private void showDeleteDialog(@NonNull Workout w) {
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

  private void showCopyDialog(@NonNull Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    builder
        .setTitle(String.format(this.owner.getString(R.string.copy_workout_titel), w.name))
        .setMessage(R.string.copy_workout_message)
        .setPositiveButton(
            R.string.yes,
            (dialog, id) -> {
              Workout copy = new Workout(w.name);
              this.model.repository.insertWorkout(
                  copy,
                  new SingleObserver<>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}

                    @Override
                    public void onSuccess(@NonNull Long aLong) {
                      model.repository.getAllWorkoutExerciseSingle(
                          w.id,
                          new SingleObserver<>() {

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

  private void showEditDialog(@NonNull Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    InputDialogTextBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.owner.getContext()), R.layout.input_dialog_text, null, false);
    binding.setText(w.name);
    builder
        .setView(binding.getRoot())
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

  private void showAddDialog(@NonNull Workout w) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.owner.getContext());
    this.model.repository.getAllExercises(
        new SingleObserver<>() {

          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<Exercise> total) {
            if (total.isEmpty()) {
              builder
                  .setTitle(String.format(owner.getString(R.string.select_exercises), w.name))
                  .setMessage(R.string.no_items)
                  .setPositiveButton(R.string.yes, (dialog, id) -> dialog.dismiss())
                  .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                  .create()
                  .show();
            } else {
              model.repository.getAllWorkoutExerciseAndExerciseSingle(
                  w.id,
                  new SingleObserver<>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {}

                    @Override
                    public void onSuccess(@NonNull List<WorkoutExerciseAndExercise> found) {
                      List<Exercise> tmp =
                          found.stream().map(e -> e.exercise).collect(Collectors.toList());
                      String[] items = total.stream().map(e -> e.name).toArray(String[]::new);
                      List<Boolean> b =
                          total.stream().map(tmp::contains).collect(Collectors.toList());
                      boolean[] checkedList = Booleans.toArray(b);
                      builder
                          .setMultiChoiceItems(
                              items,
                              checkedList,
                              (dialog, which, isChecked) -> checkedList[which] = isChecked)
                          .setTitle(
                              String.format(owner.getString(R.string.select_exercises), w.name))
                          .setPositiveButton(
                              R.string.yes,
                              (dialog, id) -> {
                                List<Pair<WorkoutExercise, Boolean>> result = new ArrayList<>();
                                for (int i = 0; i < checkedList.length; i++) {
                                  result.add(
                                      new Pair<>(
                                          new WorkoutExercise(w.id, total.get(i).id, 5),
                                          checkedList[i]));
                                }
                                if (result.size() > 0) {
                                  model.repository.insertOrDeleteWorkoutExercises(result);
                                }

                                dialog.dismiss();
                              })
                          .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                          .create()
                          .show();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                      Log.e(TAG, e.toString());
                    }
                  });
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {
            Log.e(TAG, e.toString());
          }
        });
  }

  private static class WorkoutListDiffUtil extends DiffUtil.Callback {

    private final List<WorkoutAndWorkoutExercises> oldList;
    private final List<WorkoutAndWorkoutExercises> newList;

    public WorkoutListDiffUtil(
        @NonNull List<WorkoutAndWorkoutExercises> oldList,
        @NonNull List<WorkoutAndWorkoutExercises> newList) {
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
      return this.oldList.get(oldItemPosition).workout.id
          == this.newList.get(newItemPosition).workout.id;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return Objects.equals(
              this.oldList.get(oldItemPosition).workout.name,
              this.newList.get(newItemPosition).workout.name)
          && areListsTheSame(
              this.oldList.get(oldItemPosition).workoutAndExercises,
              this.newList.get(newItemPosition).workoutAndExercises);
    }

    private boolean areListsTheSame(
        List<WorkoutExerciseAndExercise> l1, List<WorkoutExerciseAndExercise> l2) {
      if (l1.size() != l2.size()) return false;
      for (int i = 0; i < l1.size(); i++) {
        if (!l1.get(i).equals(l2.get(i))) return false;
      }
      return true;
    }
  }

  public static class WorkoutItemViewHolder extends RecyclerView.ViewHolder {

    public ItemWorkoutBinding binding;

    public WorkoutItemViewHolder(@NonNull ItemWorkoutBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
