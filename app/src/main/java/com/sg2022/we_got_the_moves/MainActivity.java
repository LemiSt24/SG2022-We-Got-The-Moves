package com.sg2022.we_got_the_moves;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sg2022.we_got_the_moves.databinding.ActivityMainBinding;
import com.sg2022.we_got_the_moves.ui.dashboard.InstructionActivity;
import com.sg2022.we_got_the_moves.ui.settings.SettingsViewModel;
import com.sg2022.we_got_the_moves.ui.settings.UserDataChangeActivity;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private static WeakReference<MainActivity> weakMainActivity;

    public static MainActivity getInstanceActivity(){
        return weakMainActivity.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids (maximal 5) because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                //R.id.navigation_home,
                R.id.navigation_dashboard, R.id.navigation_training, R.id.navigation_workouts, R.id.navigation_statistics, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        weakMainActivity = new WeakReference<>(MainActivity.this);
    }

    public void openUserDataChangeActivity(){
        Intent intent = new Intent(this, UserDataChangeActivity.class);
        startActivity(intent);
    }

    public void openInstructionActivity(long id){
        Intent intent = new Intent(this, InstructionActivity.class);
        intent.putExtra("EXERCISE_ID", id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}