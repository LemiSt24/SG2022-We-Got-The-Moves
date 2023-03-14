package com.sg2022.we_got_the_moves.ui.training;

import static android.content.Context.WINDOW_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.mediapipe.components.CameraHelper;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.Arrays;
import java.util.List;

public class Camera2Helper extends CameraHelper {
  public static final String TAG = "Camera2Helper";
  private static final int REQUEST_CAMERA_PERMISSION = 0;
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  protected CameraDevice cameraDevice;
  protected CameraCaptureSession cameraCaptureSessions;
  protected CaptureRequest.Builder captureRequestBuilder;
  private String cameraId;
  private Size imageDimension;
  private ImageReader imageReader;
  private Handler mBackgroundHandler;
  private HandlerThread mBackgroundThread;
  private SurfaceTexture outputSurface;
  private Size frameSize;
  private int frameRotation;
  private CameraHelper.CameraFacing cameraFacing;
  private final Context context;
  private final CameraDevice.StateCallback stateCallback =
      new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
          // This is called when the camera is open
          cameraDevice = camera;
          createCameraPreview();
          if (onCameraStartedListener != null) {
            onCameraStartedListener.onCameraStarted(outputSurface);
          }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
          cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
          try {
            cameraDevice.close();
            cameraDevice = null;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };

  public Camera2Helper(Context context, SurfaceTexture surfaceTexture) {
    this.context = context;
    this.outputSurface = surfaceTexture;
  }

  @Override
  public void startCamera(
      Activity context, CameraFacing cameraFacing, @Nullable SurfaceTexture surfaceTexture) {
    this.cameraFacing = cameraFacing;
    closeCamera();
    startBackgroundThread();
    openCamera();
  }

  @Override
  public Size computeDisplaySizeFromViewSize(Size viewSize) {
    if (viewSize == null || frameSize == null) {
      // Wait for all inputs before setting display size.
      Log.d(TAG, "viewSize or frameSize is null.");
      return null;
    }

    // Valid rotation values are 0, 90, 180 and 270.
    // Frames are rotated relative to the device's "natural" landscape orientation. When in portrait
    // mode, valid rotation values are 90 or 270, and the width/height should be swapped to
    // calculate aspect ratio.
    float frameAspectRatio =
        frameRotation == 90 || frameRotation == 270
            ? frameSize.getHeight() / (float) frameSize.getWidth()
            : frameSize.getWidth() / (float) frameSize.getHeight();

    float viewAspectRatio = viewSize.getWidth() / (float) viewSize.getHeight();

    // Match shortest sides together.
    int scaledWidth;
    int scaledHeight;
    if (frameAspectRatio < viewAspectRatio) {
      scaledWidth = viewSize.getWidth();
      scaledHeight = Math.round(viewSize.getWidth() / frameAspectRatio);
    } else {
      scaledHeight = viewSize.getHeight();
      scaledWidth = Math.round(viewSize.getHeight() * frameAspectRatio);
    }

    return new Size(scaledWidth, scaledHeight);
  }

  @Override
  public boolean isCameraRotated() {
    Display display =
        ((WindowManager) MediaPipeActivity.getInstanceActivity().getSystemService(WINDOW_SERVICE))
            .getDefaultDisplay();
    this.frameRotation = display.getRotation();
    return this.frameRotation % 2 == 1;
  }

  public void closeCamera() {
    try {
      stopBackgroundThread();
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (Exception e) {
      Log.e(TAG, e.toString());
    }
  }

  private void openCamera() {
    CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);


      UserRepository userRepository =
          UserRepository.getInstance(MediaPipeActivity.getInstanceActivity().getApplication());
      userRepository.getCameraBoolean(
          new SingleObserver<>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
              try {
                if (aBoolean) {
                  for (String cameraIdListElement : manager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics =
                        manager.getCameraCharacteristics(cameraIdListElement);
                    if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) == 0) {
                      cameraId = cameraIdListElement;
                      break;
                    }
                  }
                } else {
                  for (String cameraIdListElement : manager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics =
                        manager.getCameraCharacteristics(cameraIdListElement);
                    if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) == 1) {
                      cameraId = cameraIdListElement;
                      break;
                    }
                  }
                }

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                  ActivityCompat.requestPermissions(
                      (Activity) context,
                      new String[] {
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                      },
                      REQUEST_CAMERA_PERMISSION);
                  Log.d(TAG, "Permission issue");
                  return;
                }
                Log.d(TAG, "Opening camera from manager " + cameraId);
                manager.openCamera(cameraId, stateCallback, null);
                Log.println(Log.DEBUG, "test", "boolean now: " + aBoolean);

              } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
              }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
          });

    }

  // private CameraCaptureSession cameraCaptureSession;
  // @RequiresApi(api = Build.VERSION_CODES.O)
  protected void createCameraPreview() {
    try {
      outputSurface = (outputSurface == null) ? new CustomSurfaceTexture(0) : outputSurface;

      SurfaceTexture texture = outputSurface;
      texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
      frameSize = imageDimension;
      Surface surface = new Surface(texture);
      captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      captureRequestBuilder.set(
          CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
          CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
      captureRequestBuilder.addTarget(surface);
      cameraDevice.createCaptureSession(
          List.of(surface),
          new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == cameraDevice) {
                return;
              }
              // When the session is ready, we start displaying the preview.
              cameraCaptureSessions = cameraCaptureSession;
              updatePreview();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              Toast.makeText(context, "Configuration change", Toast.LENGTH_SHORT).show();
            }
          },
          null);
    } catch (CameraAccessException e) {
      e.printStackTrace();
      Log.d(TAG, e.toString());
    }
  }

  protected void updatePreview() {
    if (null == cameraDevice) {
      Log.e(TAG, "updatePreview error, return");
    }
    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    // captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(frameRotation));

    try {
      cameraCaptureSessions.setRepeatingRequest(
          captureRequestBuilder.build(), null, mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  protected void startBackgroundThread() {
    mBackgroundThread = new HandlerThread("Camera Background");
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  protected void stopBackgroundThread() {
    mBackgroundThread.quitSafely();
    try {
      mBackgroundThread.join();
      mBackgroundThread = null;
      mBackgroundHandler = null;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

class CustomSurfaceTexture extends SurfaceTexture {
  public CustomSurfaceTexture(int texName) {
    super(texName);
    init();
  }

  private void init() {
    super.detachFromGLContext();
  }
}
