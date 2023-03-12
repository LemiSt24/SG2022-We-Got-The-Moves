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
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialWorkoutsBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class WorkoutsTutorialFragment extends Fragment {

    private List<Integer> images;
    private List<String> instructions;
    private int position;
    private FragmentTutorialWorkoutsBinding binding;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        this.instructions = new ArrayList<>();
        images.add(R.drawable.tutorial_workouts_01);
        images.add(R.drawable.tutorial_workouts_02);
        images.add(R.drawable.tutorial_workouts_03);
        images.add(R.drawable.tutorial_workouts_04);
        images.add(R.drawable.tutorial_workouts_05);
        images.add(R.drawable.tutorial_workouts_06);
        images.add(R.drawable.tutorial_workouts_07);
        images.add(R.drawable.tutorial_workouts_08);
        images.add(R.drawable.tutorial_workouts_09);
        images.add(R.drawable.tutorial_workouts_10);
        images.add(R.drawable.tutorial_workouts_11);
        images.add(R.drawable.tutorial_workouts_12);

        instructions.add("In the Workoutplanner you can create your own workouts and customize the " +
                "predefined workouts.");
        instructions.add("By clicking on the pencil icon you can rename workouts.");
        instructions.add("By clicking on the copy icon you can create a copy of a workout.");
        instructions.add("By clicking on the trashcan icon you delete a workout.");
        instructions.add("If you want to create a new workout you have to click on the floating " +
                "bottom in the lower right corner and choose a name for the workout.");
        instructions.add("The new workout will be added to the end of the list.");
        instructions.add("To select the exercises in a workout, you need to click the button on the " +
                "the right side of each workout and select the desired exercises.");
        instructions.add("To edit the exercises of a workout you have to click on the button on " +
                "the left of each workout to open it.");
        instructions.add("Then you can add and remove sets with the plus and minus icons " +
                "and change the repetitions and duration for each set.");
        instructions.add("You can also see the exercise instructions as in the dashboard by " +
                "clicking on the info icon.");
        instructions.add("The order of the exercises can be changed per drag and drop.");
        instructions.add("When you have completed your adjustments, you can save the workout " +
                "by clicking on the save button.");
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_workouts, container, false);
        position = 0;
        binding.imagebuttonWorkoutsEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewTutorialWorkoutsPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialWorkoutsImageview.setImageResource(images.get(position));
        binding.textviewTutorialWorkoutsInstruction.setText(instructions.get(position));
        binding.tutorialWorkoutsImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialWorkoutsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialWorkoutsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialWorkoutsPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialWorkoutsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialWorkoutsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialWorkoutsPageInfo.setText(position + 1 + "/" + images.size());
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
        binding.tutorialWorkoutsImageview.setOnTouchListener(null);
    }
}

