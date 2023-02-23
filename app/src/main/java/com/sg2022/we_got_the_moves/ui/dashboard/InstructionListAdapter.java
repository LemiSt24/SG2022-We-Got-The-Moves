package com.sg2022.we_got_the_moves.ui.dashboard;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.DialogInstructionBinding;
import com.sg2022.we_got_the_moves.databinding.ItemInstructionBinding;
import com.sg2022.we_got_the_moves.db.entity.Exercise;

import java.util.ArrayList;
import java.util.List;

public class InstructionListAdapter
    extends RecyclerView.Adapter<InstructionListAdapter.ExerciseInstructionListViewHolder> {

  private static final String TAG = "InstructionListAdapter";
  private final Fragment fragment;
  private List<Exercise> list;

  public InstructionListAdapter(@NonNull Fragment fragment, @NonNull DashboardViewModel model) {
    this.list = new ArrayList<>();
    this.fragment = fragment;
    this.list = new ArrayList<>();
    model
        .repository
        .getAllExercises()
        .observe(
            this.fragment,
            items -> {
              InstructionListDiffUtil instructionDiff =
                  items == null
                      ? new InstructionListDiffUtil(list, new ArrayList<>())
                      : new InstructionListDiffUtil(list, items);
              DiffUtil.DiffResult diff = DiffUtil.calculateDiff(instructionDiff);
              list = items;
              diff.dispatchUpdatesTo(InstructionListAdapter.this);
            });
  }

  @NonNull
  @Override
  public ExerciseInstructionListViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    ItemInstructionBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_instruction, parent, false);
    return new ExerciseInstructionListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ExerciseInstructionListViewHolder holder, int position) {
    Exercise e = this.list.get(position);
    holder.binding.setExercise(e);
    Glide.with(this.fragment)
        .load(e.imageId)
        .placeholder(R.drawable.placeholder)
        .into(holder.binding.imageviewExerciseInstructionItem);
    holder.binding.imageviewExerciseInstructionItem.setOnClickListener(
        v -> showInstructionDialog(e));
  }

  @Override
  public long getItemId(int position) {
    return this.list.get(position).id;
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  private void showInstructionDialog(@NonNull Exercise e) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this.fragment.getContext());
    DialogInstructionBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(this.fragment.requireContext()),
            R.layout.dialog_instruction,
            null,
            false);
    binding.setExercise(e);
    // this.fragment.getLifecycle().addObserver(binding.youtubePlayerViewInstructionDialog);
    binding.youtubePlayerViewInstructionDialog.addYouTubePlayerListener(
        new AbstractYouTubePlayerListener() {
          @Override
          public void onReady(@NonNull YouTubePlayer youTubePlayer) {
            youTubePlayer.loadVideo(e.youtubeId, 0);
          }

          @Override
          public void onError(
              @NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
            super.onError(youTubePlayer, error);
            Log.e(TAG, error.toString());
            binding.youtubePlayerViewInstructionDialog.release();
          }
        });
    builder
        .setOnDismissListener(dialog -> binding.youtubePlayerViewInstructionDialog.release())
        .setView(binding.getRoot())
        .setTitle(String.format(this.fragment.getString(R.string.instruction_title), e.name))
        .setNegativeButton(R.string.ok, (dialog, id) -> dialog.dismiss())
        .create()
        .show();
  }

  private static class InstructionListDiffUtil extends DiffUtil.Callback {

    private final List<Exercise> oldList;
    private final List<Exercise> newList;

    public InstructionListDiffUtil(
        @NonNull List<Exercise> oldList, @NonNull List<Exercise> newList) {
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
      return this.oldList.get(oldItemPosition).id == this.newList.get(newItemPosition).id;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return true;
    }
  }

  protected static class ExerciseInstructionListViewHolder extends RecyclerView.ViewHolder {

    public final ItemInstructionBinding binding;

    ExerciseInstructionListViewHolder(ItemInstructionBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
