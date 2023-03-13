package com.sg2022.we_got_the_moves.ui.training.tabs.overview;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentTrainingOverviewBinding;
import com.sg2022.we_got_the_moves.util.PermissionsChecker;

public class TrainingOverviewFragment extends Fragment {

  private static final String TAG = "TrainingOverviewFragment";
  private final String[] recorderPermissions = {
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private final String[] mediaPipePermissions = {
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private AllWorkoutsAdapter adapterAllWorkouts;
  private RecentWorkoutsAdapter adapterLastWorkouts;
  private ActivityResultLauncher<String[]> permissionActivityLauncher;
  private FragmentTrainingOverviewBinding binding;
  private Intent intent;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TrainingViewModel.Factory factory =
        new TrainingViewModel.Factory(this.requireActivity().getApplication());
    TrainingViewModel model =
        new ViewModelProvider(this.requireActivity(), factory).get(TrainingViewModel.class);
    this.intent = null;
    this.permissionActivityLauncher =
        this.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              if (result.entrySet().stream().anyMatch(p -> !p.getValue())) {
                Snackbar.make(
                        this.binding.getRoot(),
                        "Please enabled required permissions. If permissions have been denied twice go to your device's system app settings",
                        Snackbar.LENGTH_LONG)
                    .show();
              } else {
                this.requireActivity().startActivity(this.intent);
              }
            });
    this.adapterLastWorkouts = new RecentWorkoutsAdapter(this, model);
    this.adapterAllWorkouts = new AllWorkoutsAdapter(this, model);
  }

  public void launchMediaPipe(Intent intent) {
    this.intent = intent;
    this.permissionActivityLauncher.launch(
        PermissionsChecker.joinPermissions(this.mediaPipePermissions, this.recorderPermissions));
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentTrainingOverviewBinding.inflate(inflater, container, false);
    this.binding.textviewRecentlyTrainingsOverview.setText(R.string.last_workouts);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    this.binding.recyclerviewTrainingsOverview.setLayoutManager(layoutManager);
    this.binding.recyclerviewTrainingsOverview.setAdapter(this.adapterLastWorkouts);
    this.binding.textviewAllTrainingsOverview.setText(R.string.all_workouts);
    layoutManager =
        new LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false);
    this.binding.recyclerviewAllTrainingsOverview.setLayoutManager(layoutManager);
    this.binding.recyclerviewAllTrainingsOverview.setAdapter(this.adapterAllWorkouts);
    return this.binding.getRoot();
  }
}
