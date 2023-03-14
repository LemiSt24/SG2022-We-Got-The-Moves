package com.sg2022.we_got_the_moves.ui.training.tabs.playback;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.player.autoplayer.utils.PlayerListener;
import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemVideoitemBinding;
import com.sg2022.we_got_the_moves.databinding.ItemVideoitemBindingImpl;
import com.sg2022.we_got_the_moves.databinding.ItemViditemBinding;
import com.sg2022.we_got_the_moves.io.VideoItem;

import java.util.List;

public class PlaybackItemAdapter
    extends RecyclerView.Adapter<PlaybackItemAdapter.PlaybackViewHolder> {

  private static final String TAG = "PlaybackItemAdapter";
  public final List<VideoItem> videoItems;
  private final Context context;
  private final MuteListener muteListener;

  public PlaybackItemAdapter(
      @NonNull Context context, List<VideoItem> videos, MuteListener muteListener) {
    this.videoItems = videos;
    this.context = context;
    this.muteListener = muteListener;
  }

  @NonNull
  @Override
  public PlaybackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemVideoitemBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_videoitem, parent, false);
    return new PlaybackViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull PlaybackViewHolder holder, int position) {
    String[] filenameArray = videoItems.get(position).filename.
            replace("_", " ").split("\\.");
    holder.binding.videoItemName.setText(filenameArray[0] + "\n" + filenameArray[1]);
    holder.binding.videoItemName.setOnClickListener(v-> {
      MainActivity.getInstanceActivity().openReplayActivity(videoItems.get(position).filename);}
    );
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  @Override
  public int getItemCount() {
    return this.videoItems.size();
  }

  public static class PlaybackViewHolder extends RecyclerView.ViewHolder {
    public final ItemVideoitemBinding binding;

    public PlaybackViewHolder(@NonNull ItemVideoitemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
