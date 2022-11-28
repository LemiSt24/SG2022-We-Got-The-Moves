package com.sg2022.we_got_the_moves.ui.dashboard;

import static java.util.function.Predicate.not;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.ui.settings.UserDataChangeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class InstructionActivity extends AppCompatActivity {

    //private static WeakReference<InstructionActivity> weakInstructionActivity;

    private ImageButton backBtn;
    private long exerciseId;
    private Exercise exercise;
    private DashboardViewModel model;
    private String text;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        //weakInstructionActivity = new WeakReference<>(InstructionActivity.this);

        DashboardViewModel.Factory factory = new  DashboardViewModel.Factory(getApplication());
        this.model = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        backBtn = (ImageButton) findViewById(R.id.backButtonInstructionActivity);
        backBtn.setOnClickListener(v -> {
            youTubePlayerView.release();
            finish();
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        exerciseId = extras.getLong("EXERCISE_ID");

        TextView exerciseTitle = findViewById(R.id.TitleExerciseName);
        TextView textInstruction = findViewById(R.id.TextInstruction);

        this.model.getRepository().getExercise(exerciseId).observe(this, e -> {
            if (e == null){
            }
            else{
                exercise = e;
                exerciseTitle.setText(exercise.name);
                textInstruction.setText(exercise.getTextInstruction());
                videoId = exercise.getVideoInstruction();
                /*if  (exercise.getVideoInstruction() != null){
                    videoId = exercise.getVideoInstruction();
                    Log.d("d", "if");
                }*/
                /*else{
                    videoId = "S0Q4gqBUs7c";
                    Log.d("d", "else");
                }*/

                youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {

                        youTubePlayer.loadVideo(videoId, 0);
                    }
                });
            }
        });
    }
}