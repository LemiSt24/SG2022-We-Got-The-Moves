package com.sg2022.we_got_the_moves.ui.training.tabs.playback;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.player.autoplayer.AutoPlayerManager;
import com.player.autoplayer.utils.HelperForExoPlayer;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentTrainingPlaybackBinding;
import com.sg2022.we_got_the_moves.io.VidItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaybackFragment extends Fragment {
  private static final String TAG = "PlaybackFragment";
  private final ActivityResultLauncher<String[]> permissionActivityLauncher;
  private AutoPlayerManager autoPlayerManager;
  private FileViewModel model;
  private PlaybackItemAdapter playbackItemAdapter;
  private LiveData<List<VidItem>> data;

  private boolean isMute;

  public PlaybackFragment() {
    this.isMute = false;
    this.permissionActivityLauncher =
        this.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              result.entrySet().stream()
                  .filter((Map.Entry<String, Boolean> e) -> !e.getValue())
                  .forEach(
                      (Map.Entry<String, Boolean> e) ->
                          Log.i(TAG, "Required Permission :" + e.getKey() + " is missing"));
              boolean permissionsGranted =
                  result.entrySet().parallelStream().allMatch(Map.Entry::getValue);
              if (permissionsGranted) {
                if (this.data == null) {
                  this.data = this.model.repository.getAllVideoItemsDefault();
                  this.data.observe(
                      this,
                      list -> {
                        DiffUtil.DiffResult diff =
                            DiffUtil.calculateDiff(new VidDiffUtil(playbackItemAdapter.vids, list));
                        playbackItemAdapter.vids.clear();
                        playbackItemAdapter.vids.addAll(list);
                        diff.dispatchUpdatesTo(playbackItemAdapter);
                      });
                }
              }
            });
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FileViewModel.Factory factory =
        new FileViewModel.Factory(this.requireActivity().getApplication());
    this.model = new ViewModelProvider(this.requireActivity(), factory).get(FileViewModel.class);
    this.autoPlayerManager = new AutoPlayerManager(this);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FragmentTrainingPlaybackBinding binding =
        DataBindingUtil.inflate(inflater, R.layout.fragment_training_playback, container, false);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this.requireContext());
    this.playbackItemAdapter =
        new PlaybackItemAdapter(
            this.requireContext(),
            new ArrayList<>(),
            position -> {
              isMute = !isMute;
              for (int i = 0; i < playbackItemAdapter.vids.size(); i++) {
                playbackItemAdapter.vids.get(i).mute = isMute;
                if (i != position) {
                  playbackItemAdapter.notifyItemChanged(i);
                }
              }
            });
    binding.recyclerviewReplays.setLayoutManager(layoutManager);
    binding.recyclerviewReplays.setAdapter(playbackItemAdapter);
    this.autoPlayerManager.setAutoPlayerId(R.id.autoplayer_viditem);
    this.autoPlayerManager.setUseController(true);
    this.autoPlayerManager.attachRecyclerView(binding.recyclerviewReplays);
    this.autoPlayerManager.setup();
    assert this.autoPlayerManager.getHelperForExoPlayer() != null;
    HelperForExoPlayer h = this.autoPlayerManager.getHelperForExoPlayer();
    h.makeLifeCycleAware(this);
    this.autoPlayerManager.setAutoPlayPlayer(true);
    return binding.getRoot();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    this.permissionActivityLauncher.launch(this.model.repository.getPermissionsDefault());
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  private static class VidDiffUtil extends DiffUtil.Callback {

    private final List<VidItem> oldList;
    private final List<VidItem> newList;

    public VidDiffUtil(List<VidItem> oldList, List<VidItem> newList) {
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
      return this.oldList.get(oldItemPosition).uri.equals(this.newList.get(newItemPosition).uri);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return true;
    }
  }
}