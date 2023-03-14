package com.sg2022.we_got_the_moves.ui.training.tabs.playback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

import com.sg2022.we_got_the_moves.R;
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

    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        // Check if the permission has been granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission already granted, proceed to access the video file
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            this.filename = extras.getString("FILENAME");
            String directoryPath = FileRepository.getInstance(
                    this.getApplication()).getDirectoryPathDefault();
            File file = new File(directoryPath, filename);
            file.setReadable(true);
            Uri filename = Uri.parse(directoryPath + File.separator + this.filename);

            VideoView videoView = (VideoView)findViewById(R.id.videoView_playbackActivity);
            MediaController mediaController= new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(filename);
            videoView.requestFocus();
            videoView.start();
        }


    }

}