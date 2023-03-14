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
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ItemViditemBinding;
import com.sg2022.we_got_the_moves.io.VideoItem;
import java.util.List;

public class PlaybackItemAdapterBackup
    extends RecyclerView.Adapter<PlaybackItemAdapterBackup.PlaybackViewHolder> {

  private static final String TAG = "PlaybackItemAdapter";
  public final List<VideoItem> videoItems;
  private final Context context;
  private final MuteListener muteListener;

  public PlaybackItemAdapterBackup(
      @NonNull Context context, List<VideoItem> vids, MuteListener muteListener) {
    this.videoItems = vids;
    this.context = context;
    this.muteListener = muteListener;
  }

  @NonNull
  @Override
  public PlaybackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemViditemBinding binding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()), R.layout.item_viditem, parent, false);
    return new PlaybackViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull PlaybackViewHolder holder, int position) {

    Glide.with(this.context)
        .load(videoItems.get(position).uri)
        .centerCrop()
        .placeholder(R.drawable.placeholder)
        .into(holder.binding.imageviewPlaceholderViditem);
    holder.binding.autoplayerViditem.setUrl(videoItems.get(position).uri.toString());
    holder.binding.autoplayerViditem.setAnimationTime(500);
    holder.binding.autoplayerViditem.setPlaceholderView(holder.binding.imageviewPlaceholderViditem);
    Log.println(Log.DEBUG, "test", "filename: " + videoItems.get(position).filename);
    String[] filenameArray = videoItems.get(position).filename.
            replace("_", " ").split("\\.");
    holder.binding.textviewInfoViditem.setText(filenameArray[0] + "\n" + filenameArray[1]);
    if (videoItems.get(position).mute) {
      holder.binding.imageviewVolumeViditem.setImageResource(R.drawable.ic_volume_off_white_24dp);
    } else {
      holder.binding.imageviewVolumeViditem.setImageResource(R.drawable.ic_volume_on_white_24dp);
    }
    holder.binding.imageviewVolumeViditem.setOnClickListener(
        v -> {
          holder.binding.autoplayerViditem.setMute(!holder.binding.autoplayerViditem.isMute());
          if (holder.binding.autoplayerViditem.isMute()) {
            holder.binding.imageviewVolumeViditem.setImageResource(
                R.drawable.ic_volume_off_white_24dp);
          } else {
            holder.binding.imageviewVolumeViditem.setImageResource(
                R.drawable.ic_volume_on_white_24dp);
          }
          muteListener.onMute(holder.getBindingAdapterPosition());
        });
    holder.binding.autoplayerViditem.setPlayerListener(
        new PlayerListener() {
          @Override
          public void onPlayerError(@Nullable ExoPlaybackException e) {}

          @Override
          public void onPlayerReady() {}

          @Override
          public void onPlayerStart() {
            holder.binding.imageviewVolumeViditem.setImageResource(
                R.drawable.ic_volume_on_white_24dp);
          }

          @Override
          public void onPlayerStop() {
            holder.binding.imageviewVolumeViditem.setImageResource(
                R.drawable.ic_volume_off_white_24dp);
          }

          @Override
          public void onPlayerProgress(long l) {}

          @Override
          public void onPlayerBuffering(boolean b) {}

          @Override
          public void onPlayerToggleControllerVisible(boolean b) {}
        });
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
    public final ItemViditemBinding binding;

    public PlaybackViewHolder(@NonNull ItemViditemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
