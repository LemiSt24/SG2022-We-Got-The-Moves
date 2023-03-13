package com.sg2022.we_got_the_moves.ui.training.mediapipe;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraHelper.CameraFacing;
import com.google.mediapipe.glutil.EglManager;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGLSurface;

public class MyCameraXPreviewHelper extends CameraHelper {
  private static final String TAG = "CameraXPreviewHelper";
  private static final Size FRONT_TARGET_SIZE = new Size(1280, 720);
  private static final Size BACK_TARGET_SIZE = new Size(1920, 1080);
  private static final double ASPECT_TOLERANCE = 0.25;
  private static final double ASPECT_PENALTY = 10000.0;
  private static final int CLOCK_OFFSET_CALIBRATION_ATTEMPTS = 3;
  private final SingleThreadHandlerExecutor renderExecutor =
      new SingleThreadHandlerExecutor("RenderThread", 0);
  private ProcessCameraProvider cameraProvider;
  private Preview preview;
  private ImageCapture imageCapture;
  private ImageCapture.Builder imageCaptureBuilder;
  private ExecutorService imageCaptureExecutorService;
  private Camera camera;
  private int[] textures = null;
  private Size frameSize;
  private int frameRotation;
  private boolean isImageCaptureEnabled = false;
  @Nullable private CameraCharacteristics cameraCharacteristics = null;
  private float focalLengthPixels = Float.MIN_VALUE;
  private int cameraTimestampSource = 0;

  public MyCameraXPreviewHelper() {}

  private static long getOffsetFromUnknownTimestampSource() {
    return 0L;
  }

  private static long getOffsetFromRealtimeTimestampSource() {
    long offset = Long.MAX_VALUE;
    long lowestGap = Long.MAX_VALUE;

    for (int i = 0; i < 3; ++i) {
      long startMonoTs = System.nanoTime();
      long realTs = SystemClock.elapsedRealtimeNanos();
      long endMonoTs = System.nanoTime();
      long gapMonoTs = endMonoTs - startMonoTs;
      if (gapMonoTs < lowestGap) {
        lowestGap = gapMonoTs;
        offset = (startMonoTs + endMonoTs) / 2L - realTs;
      }
    }

    return offset;
  }

  @Nullable
  private static CameraCharacteristics getCameraCharacteristics(
      Context context, Integer lensFacing) {
    CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

    try {
      String[] cameraList = cameraManager.getCameraIdList();

      for (String availableCameraId : cameraList) {
        CameraCharacteristics availableCameraCharacteristics =
            cameraManager.getCameraCharacteristics(availableCameraId);
        Integer availableLensFacing =
            (Integer) availableCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
        if (availableLensFacing != null && availableLensFacing.equals(lensFacing)) {
          return availableCameraCharacteristics;
        }
      }
    } catch (CameraAccessException var8) {
      Log.e("CameraXPreviewHelper", "Accessing camera ID info got error: " + var8);
    }

    return null;
  }

  public void startCamera(
      Activity activity,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable SurfaceTexture surfaceTexture) {
    this.startCamera(
        (Context) activity,
        (LifecycleOwner) ((LifecycleOwner) activity),
        cameraFacing,
        surfaceTexture,
        getResolution(cameraFacing));
  }

  public void startCamera(
      Activity activity,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable SurfaceTexture surfaceTexture,
      @Nullable Size targetSize) {
    this.startCamera(
        (Context) activity,
        (LifecycleOwner) ((LifecycleOwner) activity),
        cameraFacing,
        surfaceTexture,
        targetSize);
  }

  public void startCamera(
      Activity activity,
      @Nonnull ImageCapture.Builder imageCaptureBuilder,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable Size targetSize) {
    this.imageCaptureBuilder = imageCaptureBuilder;
    this.startCamera(
        (Context) activity,
        (LifecycleOwner) ((LifecycleOwner) activity),
        (CameraHelper.CameraFacing) cameraFacing,
        targetSize);
  }

  public void startCamera(
      Activity activity,
      @Nonnull ImageCapture.Builder imageCaptureBuilder,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable SurfaceTexture surfaceTexture,
      @Nullable Size targetSize) {
    this.imageCaptureBuilder = imageCaptureBuilder;
    this.startCamera(
        (Context) activity,
        (LifecycleOwner) ((LifecycleOwner) activity),
        cameraFacing,
        surfaceTexture,
        targetSize);
  }

  public void startCamera(
      Context context,
      LifecycleOwner lifecycleOwner,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable Size targetSize) {
    this.startCamera(
        (Context) context,
        (LifecycleOwner) lifecycleOwner,
        cameraFacing,
        (SurfaceTexture) null,
        targetSize);
  }

  private void startCamera(
      Context context,
      LifecycleOwner lifecycleOwner,
      CameraHelper.CameraFacing cameraFacing,
      @Nullable SurfaceTexture surfaceTexture,
      @Nullable Size targetSize) {
    Executor mainThreadExecutor = ContextCompat.getMainExecutor(context);
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
        ProcessCameraProvider.getInstance(context);
    boolean isSurfaceTextureProvided = surfaceTexture != null;
    Integer selectedLensFacing = cameraFacing == CameraFacing.FRONT ? 0 : 1;
    this.cameraCharacteristics = getCameraCharacteristics(context, selectedLensFacing);
    targetSize = this.getOptimalViewSize(targetSize);
    if (targetSize == null) {
      targetSize = getResolution(cameraFacing);
    }
    Size rotatedSize = new Size(targetSize.getHeight(), targetSize.getWidth());
    cameraProviderFuture.addListener(
        () -> {
          try {
            this.cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
          } catch (Exception var9) {
            if (var9 instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }

            Log.e("CameraXPreviewHelper", "Unable to get ProcessCameraProvider: ", var9);
            return;
          }

          this.preview = (new Preview.Builder()).setTargetResolution(rotatedSize).build();
          CameraSelector cameraSelector =
              cameraFacing == CameraFacing.FRONT
                  ? CameraSelector.DEFAULT_FRONT_CAMERA
                  : CameraSelector.DEFAULT_BACK_CAMERA;
          this.preview.setSurfaceProvider(
              this.renderExecutor,
              (request) -> {
                this.frameSize = request.getResolution();
                Log.d(
                    "CameraXPreviewHelper",
                    String.format(
                        "Received surface request for resolution %dx%d",
                        this.frameSize.getWidth(), this.frameSize.getHeight()));
                SurfaceTexture previewFrameTexture =
                    isSurfaceTextureProvided ? surfaceTexture : this.createSurfaceTexture();
                previewFrameTexture.setDefaultBufferSize(
                    this.frameSize.getWidth(), this.frameSize.getHeight());
                request.setTransformationInfoListener(
                    this.renderExecutor,
                    (transformationInfo) -> {
                      this.frameRotation = transformationInfo.getRotationDegrees();
                      this.updateCameraCharacteristics();
                      if (!isSurfaceTextureProvided) {
                        previewFrameTexture.detachFromGLContext();
                      }

                      CameraHelper.OnCameraStartedListener listener = this.onCameraStartedListener;
                      if (listener != null) {
                        ContextCompat.getMainExecutor(context)
                            .execute(
                                () -> {
                                  listener.onCameraStarted(previewFrameTexture);
                                });
                      }
                    });
                Surface surface = new Surface(previewFrameTexture);
                Log.d("CameraXPreviewHelper", "Providing surface");
                request.provideSurface(
                    surface,
                    this.renderExecutor,
                    (result) -> {
                      Log.d("CameraXPreviewHelper", "Surface request result: " + result);
                      if (this.textures != null) {
                        GLES20.glDeleteTextures(1, this.textures, 0);
                      }

                      if (!isSurfaceTextureProvided) {
                        previewFrameTexture.release();
                      }

                      surface.release();
                    });
              });
          this.cameraProvider.unbindAll();
          if (this.imageCaptureBuilder != null) {
            this.imageCapture = this.imageCaptureBuilder.build();
            this.camera =
                this.cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    new UseCase[] {this.preview, this.imageCapture});
            this.imageCaptureExecutorService = Executors.newSingleThreadExecutor();
            this.isImageCaptureEnabled = true;
          } else {
            this.camera =
                this.cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, new UseCase[] {this.preview});
          }
        },
        mainThreadExecutor);
  }
  
  public void takePicture(File outputFile, ImageCapture.OnImageSavedCallback onImageSavedCallback) {
    if (this.isImageCaptureEnabled) {
      ImageCapture.OutputFileOptions outputFileOptions =
          (new ImageCapture.OutputFileOptions.Builder(outputFile)).build();
      this.imageCapture.takePicture(
          outputFileOptions, this.imageCaptureExecutorService, onImageSavedCallback);
    }
  }

  public Size getResolution(CameraFacing cameraFacing) {
    if (cameraFacing == CameraFacing.BACK) return BACK_TARGET_SIZE;
    return FRONT_TARGET_SIZE;
  }

  @Nullable
  public CameraCharacteristics getCameraCharacteristics() {
    return cameraCharacteristics;
  }

  public boolean isCameraRotated() {
    return this.frameRotation % 180 == 90;
  }

  public Size computeDisplaySizeFromViewSize(Size viewSize) {
    return this.frameSize;
  }

  @Nullable
  private Size getOptimalViewSize(@Nullable Size targetSize) {
    if (targetSize != null && this.cameraCharacteristics != null) {
      StreamConfigurationMap map =
          (StreamConfigurationMap)
              this.cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
      Size optimalSize = null;
      double targetRatio = (double) targetSize.getWidth() / (double) targetSize.getHeight();
      Log.d(
          "CameraXPreviewHelper",
          String.format(
              "Camera target size ratio: %f width: %d", targetRatio, targetSize.getWidth()));
      double minCost = Double.MAX_VALUE;
      Size[] var9 = outputSizes;
      int var10 = outputSizes.length;

      for (int var11 = 0; var11 < var10; ++var11) {
        Size size = var9[var11];
        double aspectRatio = (double) size.getWidth() / (double) size.getHeight();
        double ratioDiff = Math.abs(aspectRatio - targetRatio);
        double cost =
            (ratioDiff > 0.25 ? 10000.0 + ratioDiff * (double) targetSize.getHeight() : 0.0)
                + (double) Math.abs(size.getWidth() - targetSize.getWidth());
        Log.d(
            "CameraXPreviewHelper",
            String.format(
                "Camera size candidate width: %d height: %d ratio: %f cost: %f",
                size.getWidth(), size.getHeight(), aspectRatio, cost));
        if (cost < minCost) {
          optimalSize = size;
          minCost = cost;
        }
      }

      if (optimalSize != null) {
        Log.d(
            "CameraXPreviewHelper",
            String.format(
                "Optimal camera size width: %d height: %d",
                optimalSize.getWidth(), optimalSize.getHeight()));
      }

      return optimalSize;
    } else {
      return null;
    }
  }

  public long getTimeOffsetToMonoClockNanos() {
    return this.cameraTimestampSource == 1
        ? getOffsetFromRealtimeTimestampSource()
        : getOffsetFromUnknownTimestampSource();
  }

  public float getFocalLengthPixels() {
    return this.focalLengthPixels;
  }

  public Size getFrameSize() {
    return this.frameSize;
  }

  private void updateCameraCharacteristics() {
    if (this.cameraCharacteristics != null) {
      this.cameraTimestampSource =
          (Integer)
              this.cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE);
      this.focalLengthPixels = this.calculateFocalLengthInPixels();
    }
  }

  private float calculateFocalLengthInPixels() {
    float focalLengthMm =
        ((float[])
                this.cameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))
            [0];
    float sensorWidthMm =
        ((SizeF) this.cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE))
            .getWidth();
    return (float) this.frameSize.getWidth() * focalLengthMm / sensorWidthMm;
  }

  private SurfaceTexture createSurfaceTexture() {
    EglManager eglManager = new EglManager((Object) null);
    EGLSurface tempEglSurface = eglManager.createOffscreenSurface(1, 1);
    eglManager.makeCurrent(tempEglSurface, tempEglSurface);
    this.textures = new int[1];
    GLES20.glGenTextures(1, this.textures, 0);
    SurfaceTexture previewFrameTexture = new SurfaceTexture(this.textures[0]);
    return previewFrameTexture;
  }

  private static final class SingleThreadHandlerExecutor implements Executor {
    private final HandlerThread handlerThread;
    private final Handler handler;

    SingleThreadHandlerExecutor(String threadName, int priority) {
      this.handlerThread = new HandlerThread(threadName, priority);
      this.handlerThread.start();
      this.handler = new Handler(this.handlerThread.getLooper());
    }

    public void execute(Runnable command) {
      if (!this.handler.post(command)) {
        throw new RejectedExecutionException(this.handlerThread.getName() + " is shutting down.");
      }
    }

    public boolean shutdown() {
      return this.handlerThread.quitSafely();
    }
  }
}
