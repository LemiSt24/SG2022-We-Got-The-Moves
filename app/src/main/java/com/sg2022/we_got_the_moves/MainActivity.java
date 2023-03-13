package com.sg2022.we_got_the_moves;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.sg2022.we_got_the_moves.databinding.ActivityMainBinding;
import com.sg2022.we_got_the_moves.ui.training.MediaPipeActivity;
import com.sg2022.we_got_the_moves.ui.tutorial.TutorialActivity;
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
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    weakMainActivity = new WeakReference<>(MainActivity.this);
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
    NavController navController =
        Navigation.findNavController(weakMainActivity.get(), R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(
        weakMainActivity.get(), navController, appBarConfiguration);
    NavigationUI.setupWithNavController(binding.bottomNavBarMain, navController);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    boolean isAccessed = prefs.getBoolean(getString(R.string.is_accessed), false);
    if (!isAccessed) {
      SharedPreferences.Editor edit = prefs.edit();
      edit.putBoolean(getString(R.string.is_accessed), Boolean.TRUE);
      edit.apply();
      openTutorialActivity();
    }
  }

  public void openMediapipeActivity(long id, boolean recordingBoolean) {
    Intent intent = new Intent(this, MediaPipeActivity.class);
    intent.putExtra("RECORDING_BOOLEAN", recordingBoolean);
    intent.putExtra("WORKOUT_ID", id);
    startActivity(intent);
  }
  public void openTutorialActivity() {
    Intent intent = new Intent(this, TutorialActivity.class);
    startActivity(intent);
  }
}
