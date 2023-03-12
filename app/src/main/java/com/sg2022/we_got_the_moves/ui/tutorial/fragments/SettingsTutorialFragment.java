package com.sg2022.we_got_the_moves.ui.tutorial.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialSettingsBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class SettingsTutorialFragment extends Fragment {

    private List<Integer> images;
    private List<String> instructions;
    private int position;
    private FragmentTutorialSettingsBinding binding;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        this.instructions = new ArrayList<>();
        images.add(R.drawable.tutorial_settings_01);
        images.add(R.drawable.tutorial_settings_01);
        images.add(R.drawable.tutorial_settings_01);
        images.add(R.drawable.tutorial_settings_02);
        images.add(R.drawable.tutorial_settings_03);
        images.add(R.drawable.tutorial_settings_04);
        images.add(R.drawable.tutorial_settings_04);
        instructions.add("In the Settings you can further personalize the app and its functions.");
        instructions.add("With your demagrophic data and calorie target, " +
                "your calorie consumption can be calculated more accurately " +
                "and can be displayed in the first tab of the statistics.");
        instructions.add("With the last 3 fields the training functionalities can be edited. " +
                "So you can customize your rest time, the camera you use and " +
                "turn the vocal feedback on and off.");
        instructions.add("By clicking on the floating button in the lower right corner the fields " +
                "are activated and can be edited.");
        instructions.add("Fill in your personal data and your preferences.");
        instructions.add("By clicking on the save button your changes will be saved." +
                "\n" + "You can reaccess the tutorial by clicking the button " +
                "at the bottom of the settings.");
        instructions.add("By clicking on the End Tutorial Button in the upper left corner " +
                        "you can end the tutorial and start your training.");

    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_settings, container, false);
        position = 0;
        binding.imagebuttonSettingsEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewTutorialSettingsPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialSettingsImageview.setImageResource(images.get(position));
        binding.textviewTutorialSettingsInstruction.setText(instructions.get(position));
        binding.tutorialSettingsImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialSettingsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialSettingsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialSettingsPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialSettingsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialSettingsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialSettingsPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        binding.tutorialSettingsImageview.setOnTouchListener(null);
    }
}

