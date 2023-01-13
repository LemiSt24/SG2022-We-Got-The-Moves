package com.sg2022.we_got_the_moves;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.sg2022.we_got_the_moves.databinding.ActivityMainBinding;
import com.sg2022.we_got_the_moves.ui.training.MediapipeActivity;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private static WeakReference<MainActivity> weakMainActivity;

  public static MainActivity getInstanceActivity() {
    return weakMainActivity.get();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    List<Integer> fragments =
        Arrays.asList(
            R.id.navigation_dashboard,
            R.id.navigation_training,
            R.id.navigation_workouts,
            R.id.navigation_statistics,
            R.id.navigation_settings);
    AppBarConfiguration appBarConfiguration =
        new AppBarConfiguration.Builder(new HashSet<>(fragments)).build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(binding.bottomNavBarMain, navController);

    weakMainActivity = new WeakReference<>(MainActivity.this);
  }

  public void openMediapipeActivity(long id) {
    Intent intent = new Intent(this, MediapipeActivity.class);
    intent.putExtra("WORKOUT_ID", id);
    startActivity(intent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
