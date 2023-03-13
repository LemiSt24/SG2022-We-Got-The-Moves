/*package com.sg2022.we_got_the_moves.ui.training.recording;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;


import com.sg2022.we_got_the_moves.R;


public class RecorderActivity extends AppCompatActivity {

    private static final String TAG = "DATARECORDER";
    private static final int PERMISSION_CODE = 1;
    private MediaProjectionManager mProjectionManager;
    private ToggleButton mToggleButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder2);
        mProjectionManager = (MediaProjectionManager) getSystemService (Context.MEDIA_PROJECTION_SERVICE);
        mToggleButton = (ToggleButton) findViewById(R.id.toggle);

        boolean isRecording = isServiceRunning(RecordService.class);
        if(isRecording){
            mToggleButton.setChecked(true);
        }

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleScreenShare(v);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.v("test", "startRecordingService from Activity");
            startRecordingService(resultCode, data);
        } else {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
    }

    public void onToggleScreenShare(View view) {
        if ( ((ToggleButton)view).isChecked() ) {
            // ask for permission to capture screen and act on result after
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            Log.v("test", "onToggleScreenShare");
        } else {
            Log.v("test", "onToggleScreenShare: Recording Stopped");
            stopRecordingService();
        }
    }

    private void startRecordingService(int resultCode, Intent data){
        Intent intent = RecordService.newIntent(this, resultCode, data);
        /*PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0,intent, 0);*/
/*        startService(intent);
    }

    private void stopRecordingService(){
        Intent intent = new Intent(this, RecordService.class);
        stopService(intent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}*/