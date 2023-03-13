package com.sg2022.we_got_the_moves.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentSettingsBinding;
import com.sg2022.we_got_the_moves.db.entity.User;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class SettingsFragment extends Fragment {

  public static final String TAG = "SettingsFragment";

  private SettingsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstance) {
    super.onCreate(savedInstance);
    SettingsViewModel.Factory factory =
        new SettingsViewModel.Factory(this.requireActivity().getApplication());
    this.model = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    FragmentSettingsBinding binding =
        DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
    this.model
        .userRepository
        .getUser()
        .observe(
            this.getViewLifecycleOwner(),
            user -> {
              binding.setUser(user);
              binding.edittextUserNameSettings.setEnabled(false);
              binding.edittextUserHeightSettings.setEnabled(false);
              binding.edittextUserWeightSettings.setEnabled(false);
              binding.edittextUserAgeSettings.setEnabled(false);
              binding.radiobtnMaleSettings.setEnabled(false);
              binding.radiobtnFemaleSettings.setEnabled(false);
              binding.edittextUserCaloriesSettings.setEnabled(false);
              binding.imagebtnUserProfileSettings.setEnabled(true);
              binding.btnUserProfileSave.setEnabled(false);
              binding.btnUserProfileCancel.setEnabled(false);
              binding.radiobtnBackcameraSettings.setEnabled((false));
              binding.radiobtnFrontcameraSettings.setEnabled(false);
              binding.radiobtnTtsOnSettings.setEnabled(false);
              binding.radiobtnTtsOffSettings.setEnabled(false);

              binding.edittextUserNameSettings.addTextChangedListener(
                  new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                      binding.getUser().name = s.toString();
                    }
                  });
              binding.edittextUserHeightSettings.addTextChangedListener(
                  new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                      if (s.toString().isBlank()) s.append("0");
                      binding.getUser().height = Float.parseFloat(s.toString());
                    }
                  });
              binding.edittextUserWeightSettings.addTextChangedListener(
                  new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                      if (s.toString().isBlank()) s.append("0");
                      binding.getUser().weight = Float.parseFloat(s.toString());
                    }
                  });
              binding.edittextUserAgeSettings.addTextChangedListener(
                  new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                      if (s.toString().isBlank()) s.append("0");
                      binding.getUser().age = Integer.parseInt(s.toString());
                    }
                  });
              binding.radiobtnMaleSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnFemaleSettings.setChecked(false);
                    binding.getUser().sex = User.SEX.MALE;
                  });
              binding.radiobtnFemaleSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnMaleSettings.setChecked(false);
                    binding.getUser().sex = User.SEX.FEMALE;
                  });
              binding.edittextUserCaloriesSettings.addTextChangedListener(
                  new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                      if (s.toString().isBlank()) s.append("0");
                      binding.getUser().calories = Integer.parseInt(s.toString());
                    }
                  });
              binding.radiobtnFrontcameraSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnBackcameraSettings.setChecked(false);
                    binding.getUser().frontCamera = true;
                  });
              binding.radiobtnBackcameraSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnFrontcameraSettings.setChecked(false);
                    binding.getUser().frontCamera = false;
                  });
              binding.radiobtnTtsOnSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnTtsOffSettings.setChecked(false);
                    binding.getUser().tts = true;
                  });
              binding.radiobtnTtsOffSettings.setOnClickListener(
                  v -> {
                    binding.radiobtnTtsOnSettings.setChecked(false);
                    binding.getUser().tts = false;
                  });
            });

    binding.imagebtnUserProfileSettings.setOnClickListener(
        v -> {
          binding.imagebtnUserProfileSettings.setEnabled(false);
          binding.edittextUserNameSettings.setEnabled(true);
          binding.edittextUserHeightSettings.setEnabled(true);
          binding.edittextUserWeightSettings.setEnabled(true);
          binding.edittextUserAgeSettings.setEnabled(true);
          binding.radiobtnMaleSettings.setEnabled(true);
          binding.radiobtnFemaleSettings.setEnabled(true);
          binding.edittextUserCaloriesSettings.setEnabled(true);
          binding.edittextUserNameSettings.setEnabled(true);
          binding.btnUserProfileSave.setEnabled(true);
          binding.btnUserProfileCancel.setEnabled(true);
          binding.radiobtnBackcameraSettings.setEnabled((true));
          binding.radiobtnFrontcameraSettings.setEnabled(true);
          binding.radiobtnTtsOnSettings.setEnabled(true);
          binding.radiobtnTtsOffSettings.setEnabled(true);
        });

    binding.btnUserProfileSave.setOnClickListener(
        v -> model.userRepository.update(binding.getUser()));

    binding.btnUserProfileCancel.setOnClickListener(
        v ->
            model.userRepository.getUser(
                new SingleObserver<>() {
                  @Override
                  public void onSubscribe(@NonNull Disposable d) {}

                  @Override
                  public void onSuccess(@NonNull User user) {
                    binding.setUser(user);
                    binding.edittextUserNameSettings.setEnabled(false);
                    binding.edittextUserHeightSettings.setEnabled(false);
                    binding.edittextUserWeightSettings.setEnabled(false);
                    binding.edittextUserAgeSettings.setEnabled(false);
                    binding.radiobtnMaleSettings.setEnabled(false);
                    binding.radiobtnFemaleSettings.setEnabled(false);
                    binding.edittextUserCaloriesSettings.setEnabled(false);
                    binding.imagebtnUserProfileSettings.setEnabled(true);
                    binding.btnUserProfileSave.setEnabled(false);
                    binding.btnUserProfileCancel.setEnabled(false);
                    binding.radiobtnBackcameraSettings.setEnabled((false));
                    binding.radiobtnFrontcameraSettings.setEnabled(false);
                    binding.radiobtnTtsOnSettings.setEnabled(false);
                    binding.radiobtnTtsOffSettings.setEnabled(false);
                  }

                  @Override
                  public void onError(@NonNull Throwable e) {
                    Log.e(TAG, e.toString());
                  }
                }));

    return binding.getRoot();
  }
}
