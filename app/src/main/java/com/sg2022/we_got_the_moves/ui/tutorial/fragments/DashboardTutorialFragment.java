package com.sg2022.we_got_the_moves.ui.tutorial.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentDashboardBinding;
import com.sg2022.we_got_the_moves.databinding.FragmentTutorialDashboardBinding;
import com.sg2022.we_got_the_moves.ui.tutorial.OnSwipeTouchListener;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;

import java.util.ArrayList;
import java.util.List;


public class DashboardTutorialFragment extends Fragment {

    private List<Integer> images;
    private int position;

    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.images = new ArrayList<>();
        images.add(R.drawable.dashboard_tutorial_1);
        images.add(R.drawable.dashboard_tutorial_2);
        position = 0;
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTutorialDashboardBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_dashboard, container, false);
        binding.imagebuttonDashboardEndTutorial.setOnClickListener(v -> TutorialActivity.getInstanceActivity().finish());
        binding.textviewDashboardPageInfo.setText(position + 1 + "/" + images.size());
        binding.tutorialDashboardImageview.setOnTouchListener(new OnSwipeTouchListener(TutorialActivity.getInstanceActivity()){
            public void onSwipeRight() {
                if (position != 0){
                    position -= 1;
                    binding.tutorialDashboardImageview.setImageResource(images.get(position));
                    binding.textviewDashboardPageInfo.setText(position + 1 + "/" + images.size());
                };
            }
            public void onSwipeLeft() {
                if (position != images.size() - 1){
                    position += 1;
                    binding.tutorialDashboardImageview.setImageResource(images.get(position));
                    binding.textviewDashboardPageInfo.setText(position + 1 + "/" + images.size());
                };
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
    }
}

