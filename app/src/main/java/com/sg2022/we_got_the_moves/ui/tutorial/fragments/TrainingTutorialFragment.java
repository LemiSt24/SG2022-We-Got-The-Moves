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
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialTrainingBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class TrainingTutorialFragment extends Fragment {

    private List<Integer> images;
    private List<String> instructions;
    private int position;
    private FragmentTutorialTrainingBinding binding;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        this.instructions = new ArrayList<>();
        images.add(R.drawable.tutorial_training_01);
        images.add(R.drawable.tutorial_training_02);
        images.add(R.drawable.tutorial_training_03);
        images.add(R.drawable.tutorial_training_04);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_05);
        images.add(R.drawable.tutorial_training_06);
        images.add(R.drawable.tutorial_training_07);
        images.add(R.drawable.tutorial_training_08);
        images.add(R.drawable.tutorial_training_09);

        instructions.add("In this view you can select the workout you want to perform. " +
                "You can select from the most recently completed workouts or " +
                "from the entire list of all your workouts.");
        instructions.add("When you select a workout by clicking on it, a box with the training " +
                 "schedule will appear. "+ "\n" + "To start the workout, click on \"Start\".");
        instructions.add("The first time you train with the app, you need to give it permission " +
                "to access your camera.");
        instructions.add("At the beginning and between each set a pause is displayed with " +
                "the duration of the pause time, which you can edit in the settings");
        instructions.add("The app launches your preferred camera (can also be edited in the settings).");
        instructions.add("IMPORTANT:" + "\n" +
                "The Tracking only works if your room is sufficiently illuminated and you " +
                "are completely in the camera image. ");
        instructions.add("IMPORTANT:" + "\n" +
                "The camera should best film you from the front diagonally. " +
                "Make sure the conditions will be met for every exercise you perform.");
        instructions.add("If everything works, you will see a check sign " +
                "and the text \"all correct\" on the bottom of your screen " +
                "and you can start to do the exercises.");
        instructions.add("The app will start to count the reps / start the time counter if you " +
                "are in the correct starting position for the exercise.");
        instructions.add("The display in the upper right corner shows the current exercise and " +
                "the number of repetitions for rep-based exercises or the time to " +
                "stay in position for time-based exercises.");
        instructions.add("If you do an incorrect execution of an exercise the app will give you " +
                "feedback with the card at the bottom and vocal feedback " +
                "(if tts activated in the settings).");
        instructions.add("By clicking on the pause button in the upper left corner you can pause " +
                "the workout and choose if you want to continue, skip the current exercise or " +
                "finish the workout.");
        instructions.add("At the end of the workout the app will give you a summary of your " +
                "performed exercises and the total time of your training." +
                "\n" + "Click on \"Finish\" to end to workout.");
        instructions.add("to be done");
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_training, container, false);
        position = 0;
        binding.imagebuttonTrainingEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewTutorialTrainingPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialTrainingImageview.setImageResource(images.get(position));
        binding.textviewTutorialTrainingInstruction.setText(instructions.get(position));
        binding.tutorialTrainingImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialTrainingImageview.setImageResource(images.get(position));
                    binding.textviewTutorialTrainingInstruction.setText(instructions.get(position));
                    binding.textviewTutorialTrainingPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialTrainingImageview.setImageResource(images.get(position));
                    binding.textviewTutorialTrainingInstruction.setText(instructions.get(position));
                    binding.textviewTutorialTrainingPageInfo.setText(position + 1 + "/" + images.size());
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
        binding.tutorialTrainingImageview.setOnTouchListener(null);
    }
}

