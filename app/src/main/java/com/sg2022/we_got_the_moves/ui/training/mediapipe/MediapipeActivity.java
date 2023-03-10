package com.sg2022.we_got_the_moves.ui.training.mediapipe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.sg2022.we_got_the_moves.NormalizedLandmark;
import com.sg2022.we_got_the_moves.PoseClassifier;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.DialogBetweenExerciseScreenBinding;
import com.sg2022.we_got_the_moves.databinding.DialogFinishedTrainingScreenBinding;
import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.relation.WorkoutExerciseAndExercise;
import com.sg2022.we_got_the_moves.io.Subdirectory;
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;
import com.sg2022.we_got_the_moves.ui.training.tabs.overview.TrainingOverviewFragment;
import com.sg2022.we_got_the_moves.ui.workouts.WorkoutListAdapter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;

public class MediapipeActivity extends AppCompatActivity implements HBRecorderListener {

  private static final String TAG = "MediapipeActivity";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_world_landmarks";
  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;

  static {
    System.loadLibrary("mediapipe_jni");
    System.loadLibrary("opencv_java3");
  }

  private SurfaceTexture previewFrameTexture;
  private SurfaceView previewDisplayView;
  private EglManager eglManager;
  private FrameProcessor processor;
  private ExternalTextureConverter converter;
  private final Map<Long, List<Integer>> exerciseIdToAmount;
  private final HashMap<ExerciseState, List<Constraint>> currentConstraints;
  private final List<FinishedExercise> finishedExercises;
  private PoseClassifier classifier;
  private long workoutId;
  private CameraXPreviewHelper cameraHelper;
  private List<Exercise> exercises;
  private long timeCounter;
  private boolean timeStopped;
  private boolean recording_enabled;
  private int setPointer;
  private int exercisePointer;
  private Exercise currentExercise;
  private int lastState;
  private int reps;
  private Date startTime;
  private boolean timerSet;
  private boolean paused;
  private long countableStartTime;
  private long countableEndTime;
  private long stateStartTime;
  private String finishedExerciseSummary;
  private Long timeLastCheck;
  private TextToSpeech tts;
  private boolean ttsBoolean;
  private boolean inStartPosition;
  private FileRepository fileRepository;
  private HBRecorder hbRecorder;
  private ActivityResultLauncher<Intent> intentActivityResultLauncher;
  private Intent screenCaptureIntent;
  private UserRepository userRepository;
  private WorkoutsRepository workoutsRepository;
  private CameraHelper.CameraFacing cameraFacing;
  private FinishedWorkoutRepository finishedWorkoutRepository;
  private String workoutTitle;
  private ConstraintRepository constraintRepository;
  private Handler mMainHandler;
  private Handler mWorkerHandler;
  private ViewRecorder videoRecorder;
  private ViewGroup viewGroup;
  private boolean isRecording;
  private FrameProcessor recorderProcessor;
  private EglManager eglRecorderManager;

  public MediapipeActivity() {
    this.exercises = new ArrayList<>();
    this.finishedExercises = new ArrayList<>();
    this.exerciseIdToAmount = new HashMap<>();
    this.currentConstraints = new HashMap<>();
    this.ttsBoolean = true;
    this.inStartPosition = false;
    this.timerSet = false;
    this.paused = true;
    this.finishedExerciseSummary = "";
    this.setPointer = 0;
    this.exercisePointer = 0;
    this.lastState = 0;
    this.reps = 0;
    this.timeCounter = 0;
    this.timeStopped = false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.userRepository = UserRepository.getInstance(this.getApplication());
    this.fileRepository = FileRepository.getInstance(this.getApplication());
    this.workoutsRepository = WorkoutsRepository.getInstance(this.getApplication());
    this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(getApplication());
    this.constraintRepository = ConstraintRepository.getInstance(this.getApplication());
    this.timeLastCheck = SystemClock.elapsedRealtime();
    this.startTime = new Date(System.currentTimeMillis());
    this.videoRecorder = new ViewRecorder();
    this.hbRecorder = new HBRecorder(this.getApplicationContext(), this);
    MediaProjectionManager mediaProjectionManager =
        (MediaProjectionManager)
            this.getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    // this.screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
    /*    this.intentActivityResultLauncher =
    this.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            this.prepareRecording();
            this.hbRecorder.startScreenRecording(result.getData(), result.getResultCode());
          }
        });*/
    this.getBundleData();
    AndroidAssetUtil.initializeNativeAssetManager(this);
    PermissionHelper.checkAndRequestCameraPermissions(this);
    this.classifier = new PoseClassifier(getApplicationContext(), 20, 10, "dataset.csv");
    this.loadWorkoutData();
    this.setContentView(R.layout.activity_mediapipe);
    this.setupPreviewDisplayView();
    this.setupView();
    this.setupProcessing();
    this.performTextToSpeech("");
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

  private void startRecord() {
    try {
      this.videoRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
      this.videoRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
      this.videoRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
      this.videoRecorder.setOutputFile(
          getFilesDir().getPath() + File.separator + System.currentTimeMillis() + ".mp4");
      this.videoRecorder.setOnErrorListener(
          (mr, what, extra) -> {
            Log.e(TAG, "MediaRecorder error: type = " + what + ", code = " + extra);
            this.videoRecorder.reset();
            this.videoRecorder.release();
          });
      this.videoRecorder.setRecordedView(this.previewDisplayView);
      this.videoRecorder.prepare();
      this.videoRecorder.start();
    } catch (IOException e) {
      Log.e(TAG, "startRecord failed", e);
      e.printStackTrace();
    }
    isRecording = true;
  }

  private void pauseRecord() {
    if (this.isRecording) {
      try {
        this.videoRecorder.pause();
      } catch (Exception e) {
        Log.e(TAG, "pauseRecord failed!");
        e.printStackTrace();
      }
      this.isRecording = false;
    }
  }

  private void resumeRecord() {
    if (!this.isRecording && this.recording_enabled) {
      try {
        this.videoRecorder.resume();
      } catch (Exception e) {
        Log.e(TAG, "resumeRecord failed");
        e.printStackTrace();
      }
      this.isRecording = true;
    }
  }

  private void stopRecord() {
    try {
      this.videoRecorder.stop();
      this.videoRecorder.reset();
      this.videoRecorder.release();
    } catch (Exception e) {
      Log.d(TAG, "stopRecord failed!");
      e.printStackTrace();
    }
    isRecording = false;
  }

  private void setupView() {
    ImageButton stop_but = findViewById(R.id.mediapipe_stop_button);
    CardView stop_card = findViewById(R.id.mediapipe_stop_card);
    Button continue_but = findViewById(R.id.mediapipe_continue_button);
    Button skip_but = findViewById(R.id.mediapipe_skip_exercise_button);
    Button finish_but = findViewById(R.id.mediapipe_finish_button);
    stop_but.setOnClickListener(v -> showPauseCard());
    continue_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - timeCounter);
          performTextToSpeech("continue");
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
          paused = false;
        });
    skip_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - timeCounter);
          nextExercise(false);
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
        });
    finish_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - timeCounter);
          countableEndTime = SystemClock.elapsedRealtime();
          addFinishedExercise(currentExercise, false);
          showEndScreenAndSave();
        });
  }

  private void loadWorkoutData() {
    this.workoutsRepository.getAllWorkoutExerciseAndExerciseSingle(
        this.workoutId,
        new SingleObserver<>() {
          @Override
          public void onSubscribe(@NonNull Disposable d) {}

          @Override
          public void onSuccess(@NonNull List<WorkoutExerciseAndExercise> wees) {
            wees.sort(new WorkoutListAdapter.WorkoutExerciseComparator());
            currentExercise = wees.get(0).exercise;
            setExerciseName(currentExercise.name);
            if (currentExercise.isCountable()) setRepetition("0");
            wees.forEach(
                wee -> exerciseIdToAmount.put(wee.exercise.id, wee.workoutExercise.amount));
            exercises = wees.stream().map(wee -> wee.exercise).collect(Collectors.toList());
            showNextExerciseSetDialog(
                currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), 5);
          }

          @Override
          public void onError(@NonNull Throwable e) {
            e.printStackTrace();
          }
        });
  }

  @SuppressLint("CheckResult")
  private void loadConstraintsForExercise() {
    for (ExerciseState state : this.currentExercise.exerciseStates) {
      List<Constraint> tmpConstraints = new ArrayList<>();
      for (long constraintId : state.constraintIds) {
        this.constraintRepository
            .getConstraint(constraintId)
            .subscribeOn(Schedulers.io())
            .subscribe(constraint -> tmpConstraints.add(constraint));
      }
      this.currentConstraints.put(state, tmpConstraints);
    }
  }

  private void setupProcessing() {
    this.eglManager = new EglManager(null);
    this.processor =
        new FrameProcessor(
            this,
            this.eglManager.getNativeContext(),
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME);
    this.processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
    this.processor.addPacketCallback(
        OUTPUT_LANDMARKS_STREAM_NAME,
        (packet) -> {
          byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
          try {
            LandmarkProto.NormalizedLandmarkList landmarks =
                LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
            if (landmarks == null) {
              Log.d(TAG, "[TS:" + packet.getTimestamp() + "] No landmarks.");
              return;
            }
            List<LandmarkProto.NormalizedLandmark> landmarkList = landmarks.getLandmarkList();
            for (int i = 11; i < landmarkList.size(); i++) {
              if (landmarkList.get(i).getPresence() < 0.5) {
                Log.d(
                    TAG,
                    "landmark not visible: "
                        + NormalizedLandmark.landmark_names.get(i)
                        + " "
                        + landmarkList.get(i));
                setExerciseX(NormalizedLandmark.landmark_names.get(i) + " is not visible");
              }
            }
            this.classifier.classify(landmarks);
            if (this.exercises.size() != 0) {
              if (!this.inStartPosition) {
                for (Constraint constraint :
                    Objects.requireNonNull(
                        this.currentConstraints.get(
                            this.currentExercise.exerciseStates.get(this.lastState)))) {
                  if (!classifier.judge_constraint(constraint)) {
                    setExerciseX(constraint.message);
                    return;
                  }
                  if (this.currentExercise.isCountable())
                    this.countableStartTime = SystemClock.elapsedRealtime();
                  this.stateStartTime = SystemClock.elapsedRealtime();
                  this.inStartPosition = true;
                }
              }
              if (!this.paused && !this.currentExercise.isCountable()) {
                if (!this.timerSet) {
                  this.setTimeCounter(
                      Objects.requireNonNull(this.exerciseIdToAmount.get(this.currentExercise.id))
                          .get(this.setPointer));
                  this.timerSet = true;
                }

                if (SystemClock.elapsedRealtime() - this.timeLastCheck > 5000L) {
                  boolean changed = false;

                  for (Constraint constraint :
                      Objects.requireNonNull(
                          this.currentConstraints.get(
                              this.currentExercise.exerciseStates.get(this.lastState)))) {
                    if (!this.classifier.judge_constraint(constraint)) {
                      this.setExerciseX(constraint.message);
                      this.performTextToSpeech(constraint.message);
                      changed = true;
                      break;
                    }
                  }
                  if (!changed) {
                    this.setExerciseCheck();
                  }
                  this.timeLastCheck = SystemClock.elapsedRealtime();
                }
              } else if (!paused) {
                if (this.checkExerciseState()) { // TODO Change for toggleable Classifier
                  boolean changed = false;
                  for (Constraint constraint :
                      Objects.requireNonNull(
                          this.currentConstraints.get(
                              currentExercise.exerciseStates.get(lastState)))) {
                    if (!classifier.judge_constraint(constraint)) {
                      this.setExerciseX(constraint.message);
                      this.performTextToSpeech(constraint.message);
                      changed = true;
                      break;
                    }
                  }
                  if (!changed) {
                    this.setExerciseCheck();
                  }
                }
                if (this.lastState == 0) {
                  if (this.reps
                      >= Objects.requireNonNull(
                              this.exerciseIdToAmount.get(this.currentExercise.id))
                          .get(this.setPointer)) {
                    this.nextExerciseSet();
                  }
                }
              }
            }

          } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "Couldn't Exception received - " + e);
            e.printStackTrace();
          }
        });
    this.converter = new ExternalTextureConverter(this.eglManager.getContext(), 2);
    this.converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    this.converter.setConsumer(this.processor);
  }

  private void getBundleData() {
    Bundle bundle = this.getIntent().getExtras();
    this.workoutId = bundle.getLong(TrainingOverviewFragment.WORKOUT_ID, 0);
    this.workoutTitle = bundle.getString(TrainingOverviewFragment.WORKOUT_TITLE, "Workout");
    this.recording_enabled = bundle.getBoolean(TrainingOverviewFragment.RECORDING_FLAG, false);
    this.cameraFacing =
        bundle.getBoolean(TrainingOverviewFragment.CAMERA_FACING_FLAG, true)
            ? CameraHelper.CameraFacing.FRONT
            : CameraHelper.CameraFacing.BACK;
    this.ttsBoolean = bundle.getBoolean(TrainingOverviewFragment.TEXT_TO_SPEECH_FLAG, true);
  }

  @Override
  protected void onStart() {
    super.onStart();
    // if (this.recording_enabled)
    // this.intentActivityResultLauncher.launch(this.screenCaptureIntent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (PermissionHelper.cameraPermissionsGranted(this)) {
      this.startCamera();
    }
    // if (this.hbRecorder.isRecordingPaused()) this.hbRecorder.resumeScreenRecording();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // if (this.hbRecorder.isBusyRecording()) this.hbRecorder.pauseScreenRecording();
    this.pauseRecord();
    this.converter.close();
    previewDisplayView.setVisibility(View.GONE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.stopRecord();
    // if (this.hbRecorder.isRecordingPaused()) this.hbRecorder.stopScreenRecording();
  }

  public void startCamera() {
    this.cameraHelper = new CameraXPreviewHelper();
    this.cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          this.previewFrameTexture = surfaceTexture;
          this.previewDisplayView.setVisibility(View.VISIBLE);

          this.startRecord();
        });
    cameraHelper.startCamera(this, this.cameraFacing, null);
  }

  protected void onPreviewDisplaySurfaceChanged(
      SurfaceHolder holder, int format, int width, int height) {
    Size viewSize = new Size(width, height);
    Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
    boolean isCameraRotated = cameraHelper.isCameraRotated();
    this.converter.setSurfaceTextureAndAttachToGLContext(
        previewFrameTexture,
        !isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
        !isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    this.converter.setRotation(display.getRotation());
    this.videoRecorder.setInputSurface(holder.getSurface());
  }

  private void setupPreviewDisplayView() {
    this.previewDisplayView = new SurfaceView(this);
    this.previewDisplayView.setVisibility(View.GONE);
    viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(previewDisplayView);
    this.previewDisplayView
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

  public boolean checkExerciseState() {
    int nextState = lastState + 1;
    if (nextState >= currentExercise.exerciseStates.size()) {
      nextState = 0;
    }
    if (classifier.judgeEnterState(currentExercise.exerciseStates.get(nextState))) {
      if (currentExercise.exerciseStates.get(lastState).stateTime
          > SystemClock.elapsedRealtime() - stateStartTime) {
        this.setExerciseX("Slower your execution speed");
        performTextToSpeech("Slower your execution speed");
      }
      this.lastState = nextState;
      this.stateStartTime = SystemClock.elapsedRealtime();
      if (lastState == 0) countRepUp();
      return true;
    }
    return false;
  }

  public void setExerciseCheck() {
    ImageView check_x_mark = findViewById(R.id.mediapipe_check_x_mark);
    TextView evaluation_text = findViewById(R.id.mediapipe_evaluation_text);
    runOnUiThread(
        () -> {
          check_x_mark.setImageResource(R.drawable.ic_check_green_24dp);
          evaluation_text.setText(R.string.all_correct);
        });
  }

  public void setExerciseX(String reason) {
    ImageView check_x_mark = findViewById(R.id.mediapipe_check_x_mark);
    TextView evaluation_text = findViewById(R.id.mediapipe_evaluation_text);

    runOnUiThread(
        () -> {
          check_x_mark.setImageResource(R.drawable.ic_x_red_24dp);
          evaluation_text.setText(reason);
        });
  }

  public void setTimeCounter(long seconds) {
    timeStopped = false;
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);

    runOnUiThread(
        () -> {
          time_counter.setVisibility(View.VISIBLE);
          repetition_counter.setVisibility(View.GONE);
          time_counter.setBase(SystemClock.elapsedRealtime() + 1000 * seconds);
          time_counter.start();
          time_counter.setOnChronometerTickListener(
              chronometer -> {
                long base = time_counter.getBase();
                if (base < SystemClock.elapsedRealtime()) {
                  time_counter.stop();
                  timerSet = false;
                  nextExerciseSet();
                }
              });
        });
  }

  public void stopTimeCounter() {
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    timeCounter = SystemClock.elapsedRealtime();
    time_counter.stop();
    timeStopped = true;
  }

  public void startTimeCounter() {
    if (timeStopped) {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      time_counter.setBase(time_counter.getBase() + SystemClock.elapsedRealtime() - timeCounter);
      time_counter.start();
      timeStopped = false;
    }
  }

  public void setExerciseName(String name) {
    TextView exerciseText = findViewById(R.id.mediapipe_exercise_name);
    runOnUiThread(() -> exerciseText.setText(name));
  }

  public void setRepetition(String Rep) {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    runOnUiThread(
        () -> {
          repetition_counter.setVisibility(View.VISIBLE);
          time_counter.setVisibility(View.GONE);
          repetition_counter.setText(Rep);
        });
  }

  public void countRepUp() {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    reps = reps + 1;
    runOnUiThread(() -> repetition_counter.setText(String.valueOf(reps)));
  }

  public void addFinishedExercise(Exercise exercise, boolean finished) {
    int duration;
    int amount = 0;
    if (exercise.isCountable()) {
      if (reps == 0) return;
      amount = reps;
      duration = (int) ((countableEndTime - countableStartTime) / 1000L);
    } else {
      if (finished) {
        duration = exerciseIdToAmount.get(exercise.id).get(setPointer);
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        duration =
            exerciseIdToAmount.get(exercise.id).get(setPointer)
                - ((int) ((time_counter.getBase() - timeCounter) / 1000L));
      }
    }
    finishedExercises.add(new FinishedExercise(0, exercise.id, duration, amount));
  }

  public void nextExerciseSet() {
    if (setPointer >= exerciseIdToAmount.get(currentExercise.id).size() - 1) {
      nextExercise(true);
    } else {
      inStartPosition = false;
      paused = true;
      countableEndTime = SystemClock.elapsedRealtime();
      addFinishedExercise(currentExercise, true);
      setPointer++;
      reps = 0;
      setRepetition(String.valueOf(0));
      showNextExerciseSetDialog(
          currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), 5);
    }
  }

  public void nextExercise(boolean finishedNormal) {
    inStartPosition = false;
    if (currentExercise.isCountable()) {
      countableEndTime = SystemClock.elapsedRealtime();
      addFinishedExercise(currentExercise, finishedNormal);
      setPointer = 0;
      exercisePointer++;

      // -> training finished
      if (exercisePointer >= exercises.size()) {
        paused = true;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this::showEndScreenAndSave);
      }

      // next exercise is loaded
      else {
        paused = true;
        currentExercise = exercises.get(exercisePointer);
        showNextExerciseSetDialog(
            currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), 5);
        setExerciseName(currentExercise.name);
        reps = 0;
        setRepetition(String.valueOf(0));
        setPointer = 0;
      }
    }
    // current is time based
    else {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      runOnUiThread(
          () -> {
            addFinishedExercise(currentExercise, finishedNormal);
            setPointer = 0;
            exercisePointer++;
            // no more exercises
            if (exercisePointer >= exercises.size()) {
              Log.println(Log.DEBUG, TAG, "workout finished");
              paused = true;
              Handler handler = new Handler(Looper.getMainLooper());
              handler.post(this::showEndScreenAndSave);
              time_counter.stop();
            }
            // load new exercise
            else {
              paused = true;
              currentExercise = exercises.get(exercisePointer);
              setExerciseName(currentExercise.name);
              showNextExerciseSetDialog(
                  currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), 5);
              timerSet = false;
              reps = 0;
              setRepetition(String.valueOf(0));
              time_counter.stop();
              setPointer = 0;
            }
          });
    }
  }

  private void showNextExerciseSetDialog(@NonNull Exercise e, int amount, int seconds) {
    performTextToSpeech("Next Exercise " + amount + e.name);
    runOnUiThread(
        () -> {
          AlertDialog.Builder builder = new AlertDialog.Builder(MediapipeActivity.this);
          DialogBetweenExerciseScreenBinding binding =
              DataBindingUtil.inflate(
                  LayoutInflater.from(MediapipeActivity.this),
                  R.layout.dialog_between_exercise_screen,
                  null,
                  false);
          binding.setExercise(e);
          builder.setView(binding.getRoot());
          AlertDialog dialog = builder.create();
          dialog.setCanceledOnTouchOutside(false);
          dialog.setCancelable(false);
          dialog.show();

          Chronometer pause_countdown = dialog.findViewById(R.id.pause_countdown);
          pause_countdown.setBase(SystemClock.elapsedRealtime() + 1000L * seconds);
          pause_countdown.start();

          TextView amountView = dialog.findViewById(R.id.pause_screen_excercise_amount);
          String text = String.valueOf(amount);
          if (e.isCountable()) text += " x ";
          else text += " s ";
          text += e.name;
          amountView.setText(text);

          String filename = e.name.toLowerCase() + ".csv";
          Log.println(Log.DEBUG, "Test", filename);
          classifier = new PoseClassifier(getApplicationContext(), 20, 10, filename);
          loadConstraintsForExercise();

          pause_countdown.setOnChronometerTickListener(
              chronometer -> {
                long base = pause_countdown.getBase();
                if (base < SystemClock.elapsedRealtime()) {
                  dialog.dismiss();
                  paused = false;
                }
              });
        });
  }

  private void showEndScreenAndSave() {
    long endTime = System.currentTimeMillis();
    Duration timeSpent = Duration.of(endTime - startTime.getTime(), ChronoUnit.MILLIS);
    FinishedWorkout finishedWorkout = new FinishedWorkout(startTime, workoutId, timeSpent);
    this.finishedWorkoutRepository.insert(finishedWorkout);
    this.finishedWorkoutRepository
        .getLastTraining()
        .observe(
            this,
            lastTraining -> {
              for (FinishedExercise finishedExercise : finishedExercises) {
                finishedExercise.setFinishedWorkoutId(lastTraining.id);
              }
              finishedWorkoutRepository.insertFinishedExercise(finishedExercises);
            });

    runOnUiThread(
        () -> {
          AlertDialog.Builder builder = new AlertDialog.Builder(MediapipeActivity.this);
          DialogFinishedTrainingScreenBinding binding =
              DataBindingUtil.inflate(
                  LayoutInflater.from(MediapipeActivity.this),
                  R.layout.dialog_finished_training_screen,
                  null,
                  false);
          builder
              .setView(binding.getRoot())
              .setNeutralButton(
                  "Finish",
                  (dialog, id) -> {
                    performTextToSpeech("Training finished");
                    this.stopRecord();
                    this.converter.close();
                    dialog.dismiss();
                    finish();
                  });
          AlertDialog dialog = builder.create();
          dialog.setCanceledOnTouchOutside(false);
          dialog.setCancelable(false);
          dialog.show();

          TextView titel = dialog.findViewById(R.id.finished_trainings_screen_titel);
          TextView exerciseList = dialog.findViewById(R.id.finished_trainings_screen_exercise_list);
          TextView duration = dialog.findViewById(R.id.finished_trainings_screen_duration);

          titel.setText(this.workoutTitle);

          String durationString = "Duration: " + timeSpent.toMinutes() + ":";
          if (timeSpent.getSeconds() % 60 < 10) {
            durationString += "0" + (timeSpent.getSeconds() % 60);
          } else {
            durationString += String.valueOf(timeSpent.getSeconds() % 60);
          }
          duration.setText(durationString);

          this.workoutsRepository
              .getAllExercises(workoutId)
              .observe(
                  MediapipeActivity.this,
                  exercises -> {
                    for (FinishedExercise finishedExercise : finishedExercises) {
                      for (Exercise exercise : exercises) {
                        if (exercise.id == finishedExercise.exerciseId) {
                          if (exercise.isCountable()) {

                            finishedExerciseSummary +=
                                finishedExercise.amount + " x " + exercise.name + "\n";
                          } else {
                            finishedExerciseSummary +=
                                finishedExercise.duration + " s " + exercise.name + "\n";
                          }
                          break;
                        }
                      }
                    }
                    exerciseList.setText(finishedExerciseSummary);
                  });
        });
  }

  public void showPauseCard() {
    performTextToSpeech("Pause");
    runOnUiThread(
        () -> {
          findViewById(R.id.mediapipe_stop_card).setVisibility(View.VISIBLE);
          findViewById(R.id.mediapipe_continue_button).setClickable(true);
          findViewById(R.id.mediapipe_skip_exercise_button).setClickable(true);
          findViewById(R.id.mediapipe_finish_button).setClickable(true);
        });
    stopTimeCounter();
    paused = true;
  }

  @Override
  public void onBackPressed() {
    showPauseCard();
  }

  public void performTextToSpeech(String text) {
    if (!ttsBoolean) return;
    tts =
        new TextToSpeech(
            getApplicationContext(),
            status -> {
              if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.UK);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1");
              }
            });
  }

  @Override
  public void HBRecorderOnStart() {
    Toast.makeText(this.getApplicationContext(), "VideoRecording has started", Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void HBRecorderOnComplete() {
    Toast.makeText(this.getApplicationContext(), "VideoRecording has finished", Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void HBRecorderOnError(int errorCode, String reason) {
    Toast.makeText(this.getApplicationContext(), "Error: " + errorCode, Toast.LENGTH_SHORT).show();
    Log.d(TAG, reason);
  }
}
