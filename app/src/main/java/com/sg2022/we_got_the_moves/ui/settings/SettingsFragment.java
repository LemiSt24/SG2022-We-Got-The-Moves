package com.sg2022.we_got_the_moves.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

  private FragmentSettingsBinding binding;
  private SettingsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstance) {
    super.onCreate(savedInstance);
    SettingsViewModel.Factory factory =
        new SettingsViewModel.Factory(this.requireActivity().getApplication());
    SettingsViewModel model = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
    this.model = model;
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentSettingsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    binding.editBtnUserDataChange.setOnClickListener(
        v -> {
          MainActivity.getInstanceActivity().openUserDataChangeActivity();
        });

    this.model
        .getDBUser()
        .observe(
            getViewLifecycleOwner(),
            userData -> {
              // Setting the User Parameters to the text Views
              binding.textViewName.setText("Name: " + userData.name);
              binding.textViewAge.setText("Age: " + (String.valueOf(userData.age)));
              binding.textViewGender.setText("Gender: " + (userData.isMale ? "Male" : "Female"));
              binding.textViewHeight.setText(
                  "Height: " + (String.valueOf(userData.hightInMeters)) + "m");
              binding.textViewWeightInKg.setText(
                  "Weight: " + (String.valueOf(userData.weigthInKg)) + "kg");
            });

    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
