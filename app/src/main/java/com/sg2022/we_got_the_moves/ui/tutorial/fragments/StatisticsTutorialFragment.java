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
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialStatisticsBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class StatisticsTutorialFragment extends Fragment {

    private List<Integer> images;
    private List<String> instructions;
    private int position;
    private FragmentTutorialStatisticsBinding binding;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        this.instructions = new ArrayList<>();
        images.add(R.drawable.tutorial_statistics_01);
        images.add(R.drawable.tutorial_statistics_02);
        images.add(R.drawable.tutorial_statistics_03);
        images.add(R.drawable.tutorial_statistics_04);
        images.add(R.drawable.tutorial_statistics_05);
        instructions.add("The Statistics consists of 4 Features." +
                "\n" + "The first is a donut chart that shows you how many calories you burned " +
                "during workout and how close you are to your calorie goal.");
        instructions.add("The second feature provides you with information about your training " +
                "time in total, on average and per day");
        instructions.add("If you scroll down, you can change the calendar week to which the chart " +
                "refers by clicking the arrow icon.");
        instructions.add("Through this table you will get an overview of the number of repetitions " +
                "and the duration you have performed the different exercises");
        instructions.add("There are many trophies you can unlock by training with the app. " +
                "You will get a hint how to unlock the trophy when you click on it");
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_statistics, container, false);
        position = 0;
        binding.imagebuttonStatisticsEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewTutorialStatisticsPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialStatisticsImageview.setImageResource(images.get(position));
        binding.textviewTutorialStatisticsInstruction.setText(instructions.get(position));
        binding.tutorialStatisticsImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialStatisticsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialStatisticsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialStatisticsPageInfo.setText(position + 1 + "/" + images.size());
                }
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialStatisticsImageview.setImageResource(images.get(position));
                    binding.textviewTutorialStatisticsInstruction.setText(instructions.get(position));
                    binding.textviewTutorialStatisticsPageInfo.setText(position + 1 + "/" + images.size());
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
        binding.tutorialStatisticsImageview.setOnTouchListener(null);
    }
}

