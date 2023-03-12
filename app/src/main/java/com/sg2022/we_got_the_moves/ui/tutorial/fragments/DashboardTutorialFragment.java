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
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialDashboardBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class DashboardTutorialFragment extends Fragment {

    private List<Integer> images;
    private List<String> instructions;
    private int position;
    private FragmentTutorialDashboardBinding binding;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        this.instructions = new ArrayList<>();
        images.add(R.drawable.tutorial_dashboard_01);
        images.add(R.drawable.tutorial_dashboard_01);
        images.add(R.drawable.tutorial_dashboard_01);
        images.add(R.drawable.tutorial_dashboard_02);
        instructions.add("Welcome to We GOT THE MOVES" + "\n" +
                "This app will help you structure your workout and increase your training progress." +
                "\n"+ "(swipe for next tutorial page)");
        instructions.add("You are currently in the tutorial where you will learn how the app works. "+
                "The tutorial consists of 5 parts, which can be accessed by clicking " +
                "on the navigation bar at the bottom.");
        instructions.add("In the Dashboard you get an overview of all exercises supported by the app."
                + "\n" + "When you click on an exercise, the instructions are displayed. " );
        instructions.add("The exercise instruction consists of 2 parts:"
                + "\n" + "1. video tutorial with correct execution"
                + "\n" + "2. step by step written instructions");
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_dashboard, container, false);
        position = 0;
        binding.imagebuttonDashboardEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewTutorialDashboardPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialDashboardImageview.setImageResource(images.get(position));
        binding.textviewTutorialDashboardInstruction.setText(instructions.get(position));
        binding.tutorialDashboardImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialDashboardImageview.setImageResource(images.get(position));
                    binding.textviewTutorialDashboardInstruction.setText(instructions.get(position));
                    binding.textviewTutorialDashboardPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialDashboardImageview.setImageResource(images.get(position));
                    binding.textviewTutorialDashboardInstruction.setText(instructions.get(position));
                    binding.textviewTutorialDashboardPageInfo.setText(position + 1 + "/" + images.size());
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
        binding.tutorialDashboardImageview.setOnTouchListener(null);
    }
}

