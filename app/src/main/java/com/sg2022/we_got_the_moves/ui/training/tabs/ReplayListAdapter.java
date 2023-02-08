package com.sg2022.we_got_the_moves.ui.training.tabs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemReplayBinding;
import com.sg2022.we_got_the_moves.io.Subdirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplayListAdapter
    extends RecyclerView.Adapter<ReplayListAdapter.ReplayListViewHolder> {

  private static final String TAG = "ReplayListAdapter";
  private final Fragment fragment;
  private List<File> list;

  public ReplayListAdapter(@NonNull Fragment fragment, FileViewModel model) {
    this.fragment = fragment;
    this.list = new ArrayList<>();
    model
        .fileRepository
        .getAllReplaysInternal(Subdirectory.VIDEOS)
        .observe(
            fragment,
            items -> {
              FileListDiffUtil fileDiff =
                  items == null
                      ? new FileListDiffUtil(list, new ArrayList<>())
                      : new FileListDiffUtil(list, items);
              DiffUtil.DiffResult diff = DiffUtil.calculateDiff(fileDiff);
              diff.dispatchUpdatesTo(ReplayListAdapter.this);
              list = items;
            });
  }

  @NonNull
  @Override
  public ReplayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemReplayBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_replay, parent, false);
    return new ReplayListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ReplayListViewHolder holder, int position) {

    holder.binding.videoviewReplayItem.setVideoPath(list.get(position).getPath());
    MediaController mc = new MediaController(this.fragment.requireContext());
    mc.setAnchorView(holder.binding.videoviewReplayItem);
    mc.setMediaPlayer(holder.binding.videoviewReplayItem);

    holder.binding.videoviewReplayItem.setMediaController(mc);
  }

  @Override
  public void onViewDetachedFromWindow(@NonNull ReplayListViewHolder holder) {
    super.onViewDetachedFromWindow(holder);
  }

  @Override
  public int getItemCount() {
    return this.list.size();
  }

  private static class FileListDiffUtil extends DiffUtil.Callback {
    private final List<File> oldList;
    private final List<File> newList;

    public FileListDiffUtil(@NonNull List<File> oldList, @NonNull List<File> newList) {
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
      return this.oldList
          .get(oldItemPosition)
          .getName()
          .equals(this.newList.get(newItemPosition).getName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return false;
    }
  }

  protected static class ReplayListViewHolder extends RecyclerView.ViewHolder {

    public final ItemReplayBinding binding;

    ReplayListViewHolder(ItemReplayBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
