package com.sg2022.we_got_the_moves.ui.training.tabs.recording;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentTrainingRecordingBinding;
import com.sg2022.we_got_the_moves.io.Subdirectory;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.ui.PermissionUtil;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Map;

public class RecordingFragment extends Fragment implements HBRecorderListener {

  private static final String TAG = "RecordingFragment";

  private final String[] permissions = {
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private HBRecorder hbRecorder;
  private Intent screenCaptureIntent;
  private Context context;
  private ActivityResultLauncher<Intent> intentActivityResultLauncher;
  private ActivityResultLauncher<String[]> permissionActivityLauncher;
  private FileRepository fileRepository;
  private FragmentTrainingRecordingBinding binding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.context = this.requireContext();
    this.fileRepository = FileRepository.getInstance(this.requireActivity().getApplication());
    this.hbRecorder = new HBRecorder(this.context, this);
    MediaProjectionManager mediaProjectionManager =
        (MediaProjectionManager) this.context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    this.screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
    this.intentActivityResultLauncher =
        this.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              boolean permissionsGranted =
                  PermissionUtil.checkPermissions(this.context, this.permissions);
              if (result.getResultCode() == Activity.RESULT_OK && permissionsGranted) {
                this.prepareRecording();
                this.binding.btnRecording.setText(R.string.stop);
                this.hbRecorder.startScreenRecording(result.getData(), result.getResultCode());
              }
            });
    this.permissionActivityLauncher =
        this.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result ->
                result.entrySet().stream()
                    .filter((Map.Entry<String, Boolean> e) -> !e.getValue())
                    .forEach(
                        (Map.Entry<String, Boolean> e) ->
                            Log.i(TAG, "Required Permission :" + e.getKey() + " is missing")));
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding =
        DataBindingUtil.inflate(inflater, R.layout.fragment_training_recording, container, false);
    binding.btnRecording.setText(R.string.start);
    binding.btnRecording.setOnClickListener(
        v -> {
          if (hbRecorder.isBusyRecording()) {
            hbRecorder.stopScreenRecording();
            binding.btnRecording.setText(R.string.start);
          } else {
            boolean permissionsGranted =
                PermissionUtil.checkPermissions(this.context, this.permissions);
            if (!permissionsGranted) {
              this.permissionActivityLauncher.launch(this.permissions);
            } else {
              this.intentActivityResultLauncher.launch(this.screenCaptureIntent);
            }
          }
        });
    return binding.getRoot();
  }

  private void prepareRecording() {
    final String filename = String.valueOf(System.currentTimeMillis());
    final String extension = Subdirectory.Videos.getSupportedFormats()[0];
    final String directoryPath = this.fileRepository.getDirectoryPathDefault(Subdirectory.Videos);
    final Uri uri =
        Uri.fromFile(
            new File(
                directoryPath
                    + File.separator
                    + filename
                    + FilenameUtils.EXTENSION_SEPARATOR
                    + extension));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      this.hbRecorder.setOutputUri(uri);
    } else {
      this.hbRecorder.setOutputPath(directoryPath);
      this.hbRecorder.setFileName(filename + FilenameUtils.EXTENSION_SEPARATOR + extension);
    }
  }

  @Override
  public void HBRecorderOnStart() {
    Toast.makeText(this.context, "VideoRecording has started", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void HBRecorderOnComplete() {
    Toast.makeText(this.context, "VideoRecording has finished", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void HBRecorderOnError(int errorCode, String reason) {
    Toast.makeText(this.context, "Error: " + errorCode, Toast.LENGTH_SHORT).show();
    Log.d(TAG, reason);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (this.hbRecorder.isRecordingPaused()) this.hbRecorder.resumeScreenRecording();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (this.hbRecorder.isBusyRecording()) this.hbRecorder.pauseScreenRecording();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (this.hbRecorder.isRecordingPaused()) this.hbRecorder.stopScreenRecording();
  }
}
