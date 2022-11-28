package com.sg2022.we_got_the_moves.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

  private static final String TAG = "InstructionListFragment";

  private FragmentDashboardBinding binding;
  private InstructionListAdapter adapter;
  private DashboardViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DashboardViewModel.Factory factory =
        new DashboardViewModel.Factory(this.requireActivity().getApplication());
    this.model = new ViewModelProvider(this, factory).get(DashboardViewModel.class);
    this.adapter = new InstructionListAdapter(this, this.model);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    this.binding.recyclerviewInstructions.setLayoutManager(layoutManager);
    this.binding.recyclerviewInstructions.setAdapter(this.adapter);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }
}
