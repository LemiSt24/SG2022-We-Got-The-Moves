package com.sg2022.we_got_the_moves.ui.training;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
import static android.os.Environment.DIRECTORY_MOVIES;

import androidx.core.app.NotificationManagerCompat;

import com.sg2022.we_got_the_moves.R;

/**
 * RecordService Class
 *
 * Background service for recording the device screen.
 * Listens for commands to stop or start recording by the user
 * and by screen locked/unlock events. Notification in the
 * notification center informs user about the running recording
 * service.
 */
public final class RecordService extends Service {

    private ServiceHandler mServiceHandler;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private int resultCode;
    private Intent data;
    private BroadcastReceiver mScreenStateReceiver;

    private static final String TAG = "RECORDERSERVICE";
    private static final String EXTRA_RESULT_CODE = "resultcode";
    private static final String EXTRA_DATA = "data";
    private static final int ONGOING_NOTIFICATION_ID = 23;

    /*
     *
     */
    static Intent newIntent(Context context, int resultCode, Intent data) {
        Log.v("test", "newIntent service");
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "in broadcast receiver");
            switch(action){
                case Intent.ACTION_SCREEN_ON:
                    startRecording(resultCode, data);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    stopRecording();
                    break;
                case Intent.ACTION_CONFIGURATION_CHANGED:
                    Log.v(TAG, "broadcast receiver third case");
                    stopRecording();
                    startRecording(resultCode, data);
                    break;
            }
        }
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.v("test", "handleMessage service");
            if (resultCode == RESULT_OK) {
                Log.v("test", "handle message result code ok service");
                startRecording(resultCode, data);
            }else{
            }
        }
    }

    @Override
    public void onCreate() {
        // run this service as foreground service to prevent it from getting killed
        // when the main app is being closed
        Log.v("test", "on create service");

        // register receiver to check if the phone screen is on or off
        mScreenStateReceiver = new MyBroadcastReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("test", "on Startcommand service");
        Toast.makeText(this, "Starting recording service", Toast.LENGTH_SHORT).show();

        Intent notificationIntent =  new Intent(this, RecordService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        String channelID = "com.sg2022.we_got_the_moves.ui.training.tabs.recording.RecorderService";
        String channelName = "RecordService Notification";
        /*NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        ((NotificationManager)this.getApplication().getSystemService(Context.NOTIFICATION_SERVICE)).
                createNotificationChannel(channel);*/

        NotificationChannel chan = new NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManagerCompat service = NotificationManagerCompat.from(RecordService.this);
        //NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);

        Notification notification =
                new Notification.Builder(this, "channelID")
                        .setContentTitle("DataRecorder")
                        .setContentText("Your screen is being recorded and saved to your phone.")
                        .setSmallIcon(R.mipmap.ic_app_logo)
                        .setContentIntent(pendingIntent)
                        .setTicker("Tickertext")
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);

        resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        data = intent.getParcelableExtra(EXTRA_DATA);

        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return Service.START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    private void startRecording(int resultCode, Intent data) {
        Log.v("test", "startRecording service");

        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService (Context.MEDIA_PROJECTION_SERVICE);
        mMediaRecorder = new MediaRecorder();
        Log.v("test", "startRecording Service lin 162");
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);

        int mScreenDensity = metrics.densityDpi;
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(8 * 1000 * 1000);
        mMediaRecorder.setVideoFrameRate(15);
        mMediaRecorder.setVideoSize(displayWidth, displayHeight);

        String videoDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath();
        Long timestamp = System.currentTimeMillis();

        String orientation = "portrait";
        if( displayWidth > displayHeight ) {
            orientation = "landscape";
        }

        String videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss")
                .format(new Date())).append(".mp4").toString();
        //String filePathAndName = videoDir + "/time_" + timestamp.toString() + "_mode_" + orientation + ".mp4";

        mMediaRecorder.setOutputFile( videoUri );

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        Surface surface = mMediaRecorder.getSurface();
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("RecorderActivity",
                displayWidth, displayHeight, mScreenDensity, VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                surface, null, null);
        mMediaRecorder.start();

        Log.v(TAG, "Started recording");
    }

    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaProjection.stop();
        mMediaRecorder.release();
        mVirtualDisplay.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v("test", "onDestroy service");
     /*   stopRecording();
        unregisterReceiver(mScreenStateReceiver);
        stopSelf();*/
        Toast.makeText(this, "Recorder service stopped", Toast.LENGTH_SHORT).show();
    }
}