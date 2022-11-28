package com.sg2022.we_got_the_moves.ui.training;

import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.pm.ApplicationInfo;
import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import com.sg2022.we_got_the_moves.databinding.FragmentTrainingBinding;

import java.util.HashMap;
import java.util.Map;

public class TrainingFragment extends Fragment {

  private FragmentTrainingBinding binding;

    private static final String TAG = "TrainingFragment";

    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";

    private static final int TARGET_CAMERA_WIDTH = 960;
    private static final int TARGET_CAMERA_HEIGHT = 1280;

    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.BACK;
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TrainingViewModel TrainingViewModel =
                new ViewModelProvider(this).get(TrainingViewModel.class);

    binding = FragmentTrainingBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    previewDisplayView = binding.surfaceView;
    setupPreviewDisplayView();

      // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
      // binary graphs.
      AndroidAssetUtil.initializeNativeAssetManager(this.getActivity());
      eglManager = new EglManager(null);
      processor =
              new FrameProcessor(
                      this.getActivity(),
                      eglManager.getNativeContext(),
                      BINARY_GRAPH_NAME,
                      INPUT_VIDEO_STREAM_NAME,
                      OUTPUT_VIDEO_STREAM_NAME);
      processor
              .getVideoSurfaceOutput()
              .setFlipY(FLIP_FRAMES_VERTICALLY);

      PermissionHelper.checkAndRequestCameraPermissions(this.getActivity());
      AndroidPacketCreator packetCreator = processor.getPacketCreator();
      Map<String, Packet> inputSidePackets = new HashMap<>();
//        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
//        processor.setInputSidePackets(inputSidePackets);

      // To show verbose logging, run:
      // adb shell setprop log.tag.MainActivity VERBOSE
//        if (Log.isLoggable(TAG, Log.VERBOSE)) {
      processor.addPacketCallback(
              OUTPUT_LANDMARKS_STREAM_NAME,
              (packet) -> {
                Log.v(TAG, "Received multi-hand landmarks packet.");

                Log.v(TAG, packet.toString());
                byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                try {
                  NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                  if (landmarks == null) {
                    Log.v(TAG, "[TS:" + packet.getTimestamp() + "] No iris landmarks.");
                    return;
                  }
                  // Note: If eye_presence is false, these landmarks are useless.
                  Log.v(
                          TAG,
                          "[TS:"
                                  + packet.getTimestamp()
                                  + "] #Landmarks for iris: "
                                  + landmarks.getLandmarkCount());
                  Log.v(TAG, getLandmarksDebugString(landmarks));
                } catch (InvalidProtocolBufferException e) {
                  Log.e(TAG, "Couldn't Exception received - " + e);
                  return;
                }
              });

    //final TextView textView = binding.textTraining;
    //TrainingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    return root;
  }


  private static String getLandmarksDebugString(NormalizedLandmarkList landmarks) {
    int landmarkIndex = 0;
    String landmarksString = "";
    for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
      landmarksString +=
              "\t\tLandmark["
                      + landmarkIndex
                      + "]: ("
                      + landmark.getX()
                      + ", "
                      + landmark.getY()
                      + ", "
                      + landmark.getZ()
                      + ")\n";
      ++landmarkIndex;
    }
    return landmarksString;
  }


  @Override
  public void onResume() {
    super.onResume();
    converter =
            new ExternalTextureConverter(
                    eglManager.getContext(), 2);
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);
    if (PermissionHelper.cameraPermissionsGranted(this.getActivity())) {
      startCamera();
      Log.v(TAG, "Permissions granted, starting camera...");
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    converter.close();

    // Hide preview display until we re-open the camera again.
    previewDisplayView.setVisibility(View.GONE);
  }

  protected void onCameraStarted(SurfaceTexture surfaceTexture) {
    previewFrameTexture = surfaceTexture;
    // Make the display view visible to start showing the preview. This triggers the
    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
    previewDisplayView.setVisibility(View.VISIBLE);
  }

  protected Size cameraTargetResolution() {
    return new Size(TARGET_CAMERA_WIDTH, TARGET_CAMERA_HEIGHT);
  }

  public void startCamera() {
    cameraHelper = new CameraXPreviewHelper();
    cameraHelper.setOnCameraStartedListener(
            this::onCameraStarted);
    cameraHelper.startCamera(
            this.getActivity(), CAMERA_FACING, /*unusedSurfaceTexture=*/ null, cameraTargetResolution());
  }

  protected void onPreviewDisplaySurfaceChanged(
          SurfaceHolder holder, int format, int width, int height) {
    if(cameraHelper == null)
    {
      startCamera();
    }
    // (Re-)Compute the ideal size of the camera-preview display (the area that the
    // camera-preview frames get rendered onto, potentially with scaling and rotation)
    // based on the size of the SurfaceView that contains the display.
    Size viewSize = new Size(width, height);
    Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
    boolean isCameraRotated = cameraHelper.isCameraRotated();

    // Connect the converter to the camera-preview frames as its input (via
    // previewFrameTexture), and configure the output width and height as the computed
    // display size.
    converter.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
            isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
  }

  private void setupPreviewDisplayView() {
    //previewDisplayView.setVisibility(View.GONE);
    //ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    //viewGroup.addView(previewDisplayView);

    previewDisplayView
            .getHolder()
            .addCallback(
                    new SurfaceHolder.Callback() {
                      @Override
                      public void surfaceCreated(SurfaceHolder holder) {
                        processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                      }

                      @Override
                      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        onPreviewDisplaySurfaceChanged(holder, format, width, height);
                      }

                      @Override
                      public void surfaceDestroyed(SurfaceHolder holder) {
                        processor.getVideoSurfaceOutput().setSurface(null);
                      }
                    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
