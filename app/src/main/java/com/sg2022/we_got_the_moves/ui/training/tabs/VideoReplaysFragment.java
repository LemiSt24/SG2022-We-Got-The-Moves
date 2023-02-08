package com.sg2022.we_got_the_moves.ui.training.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.databinding.FragmentTrainingReplaysBinding;

public class VideoReplaysFragment extends Fragment {

  private static final String TAG = "VideoReplaysFragment";
  private FileViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FileViewModel.Factory factory =
        new FileViewModel.Factory(this.requireActivity().getApplication());
    this.model = new ViewModelProvider(this.requireActivity(), factory).get(FileViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    FragmentTrainingReplaysBinding binding =
        FragmentTrainingReplaysBinding.inflate(inflater, container, false);
    binding.recyclerviewReplays.setLayoutManager(new LinearLayoutManager(this.requireContext()));
    ReplayListAdapter adapter = new ReplayListAdapter(this, this.model);
    binding.recyclerviewReplays.setAdapter(adapter);
    return binding.getRoot();
  }
}
