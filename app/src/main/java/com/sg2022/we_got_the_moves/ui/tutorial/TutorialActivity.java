package com.sg2022.we_got_the_moves.ui.tutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.sg2022.we_got_the_moves.MainActivity;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ActivityTutorialBinding;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {


    private static final String TAG = "TutorialActivity";
    private static WeakReference<TutorialActivity> weakTutorialActivity;

    public static TutorialActivity getInstanceActivity() {
        return weakTutorialActivity.get();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weakTutorialActivity = new WeakReference<>(TutorialActivity.this);

        ActivityTutorialBinding binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        List<Integer> fragments =
            Arrays.asList(
                R.id.navigation_tutorial_dashboard,
                R.id.navigation_tutorial_training,
                R.id.navigation_tutorial_workouts,
                R.id.navigation_tutorial_statistics,
                R.id.navigation_tutorial_settings);
            AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(new HashSet<>(fragments)).build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_tutorial);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.bottomNavBarTutorial, navController);
    }
}