package com.sg2022.we_got_the_moves;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.sg2022.we_got_the_moves.databinding.ActivityMainBinding;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

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
  }
}
