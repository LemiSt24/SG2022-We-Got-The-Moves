package com.sg2022.we_got_the_moves.ui.workouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentWorkoutsBinding;
import com.sg2022.we_got_the_moves.ui.workouts.adapter.WorkoutListAdapter;
import com.sg2022.we_got_the_moves.ui.workouts.viewmodel.WorkoutsViewModel;

public class WorkoutsFragment extends Fragment {

    private static final String TAG = "WorkoutListFragment";

    private FragmentWorkoutsBinding binding;
    private WorkoutListAdapter adapter;
    private WorkoutsViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WorkoutsViewModel.Factory factory = new WorkoutsViewModel.Factory(
                this.requireActivity().getApplication());
        this.model = new ViewModelProvider(this, factory).get(WorkoutsViewModel.class);
        this.adapter = new WorkoutListAdapter(this, this.model);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_workouts, container, false);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerviewWorkouts.setLayoutManager(layoutManager);
        this.binding.recyclerviewWorkouts.setAdapter(this.adapter);
        this.binding.floatingBtnWorkouts.setOnClickListener(v -> Toast.makeText(getContext(), "Click on Floating Action Button", Toast.LENGTH_SHORT).show());
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