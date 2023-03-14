package com.sg2022.we_got_the_moves.ui.training.tabs.playback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.os.Bundle;
import android.widget.VideoView;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.ActivityPlaybackBinding;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.ui.PermissionsHelper;


import java.io.File;
import java.util.Map;

public class PlaybackActivity extends AppCompatActivity {

    private String filename;
    private final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_VIDEO
    };
    private ActivityResultLauncher<String[]> permissionActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        this.permissionActivityLauncher =
                this.registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result ->
                                result.entrySet().stream()
                                        .filter((Map.Entry<String, Boolean> e) -> !e.getValue())
                                        .forEach(
                                                (Map.Entry<String, Boolean> e) ->
                                                        Log.i("test", "Required Permission :" + e.getKey() + " is missing")));
        boolean permissionsGranted =
                PermissionsHelper.checkPermissions(this, this.permissions);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        this.filename = extras.getString("FILENAME");
        String directoryPath = FileRepository.getInstance(
                this.getApplication()).getDirectoryPathDefault();
        Uri file = Uri.parse(directoryPath + File.separator + filename);
        Log.println(Log.DEBUG, "test", file.toString());

        if (permissionsGranted){
            VideoView videoView =(VideoView)findViewById(R.id.videoView_playbackActivity);
            MediaController mediaController= new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(file);
            videoView.requestFocus();
            videoView.start();
        }
    }
}