package com.sg2022.we_got_the_moves.ui.training.mediapipe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;
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
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;
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

public class MediaPipeActivity extends AppCompatActivity {

  public static final String WORKOUT_TITLE = "WORKOUT_TITLE";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_world_landmarks";
  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;
  public static final String WORKOUT_ID = "WORKOUT_ID";
  public static final String CAMERA_FACING_FLAG = "CAMERA_FACING_FLAG";
  public static final String TEXT_TO_SPEECH_FLAG = "TEXT_TO_SPEECH_FLAG";
  private static final String TAG = "MediaPipeActivity";

  static {
    System.loadLibrary("mediapipe_jni");
    // System.loadLibrary("opencv_java3");
  }

  private final Date startTime;
  private SurfaceTexture surfaceTexture;
  private FrameProcessor processor;
  private ExternalTextureConverter converter;
  private final Map<Long, List<Integer>> exerciseIdToAmount;
  private final HashMap<ExerciseState, List<Constraint>> currentConstraints;
  private final List<FinishedExercise> finishedExercises;
  private PoseClassifier classifier;
  private long workoutId;
  private SurfaceView surfaceView;
  private List<Exercise> exercises;
  private long timeCounter;
  private boolean timeStopped;
  private Camera2Helper cameraHelper;
  private int setPointer;
  private int exercisePointer;
  private Exercise currentExercise;
  private int lastState;
  private int reps;
  private boolean timerSet;
  private boolean paused;
  private long countableStartTime;
  private long countableEndTime;
  private long stateStartTime;
  private String finishedExerciseSummary;
  private Long timeLastCheck;
  private TextToSpeech textToSpeech;
  private boolean TextToSpeechEnabled;
  private boolean inStartPosition;
  private CameraHelper.CameraFacing cameraFacing;
  private FileRepository fileRepository;
  private WorkoutsRepository workoutsRepository;
  private FinishedWorkoutRepository finishedWorkoutRepository;
  private ConstraintRepository constraintRepository;
  private String workoutTitle;

  private MediaRecorder mediaRecorder;
  private File outputFile;
  private boolean isRecording;
  private SurfaceTexture recorderTexture;
  private Surface recorderSurface;

  public MediaPipeActivity() {
    this.exercises = new ArrayList<>();
    this.finishedExercises = new ArrayList<>();
    this.exerciseIdToAmount = new HashMap<>();
    this.currentConstraints = new HashMap<>();
    this.timeLastCheck = SystemClock.elapsedRealtime();
    this.startTime = new Date(System.currentTimeMillis());
    this.TextToSpeechEnabled = true;
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
    this.isRecording = false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.fileRepository = FileRepository.getInstance(this.getApplication());
    this.workoutsRepository = WorkoutsRepository.getInstance(this.getApplication());
    this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(getApplication());
    this.constraintRepository = ConstraintRepository.getInstance(this.getApplication());
    this.getBundleData();
    AndroidAssetUtil.initializeNativeAssetManager(this);
    PermissionHelper.checkAndRequestCameraPermissions(this);
    this.classifier = new PoseClassifier(getApplicationContext(), 20, 10, "dataset.csv");
    this.loadWorkoutData();
    this.setContentView(R.layout.activity_mediapipe);
    this.setupPreviewDisplay();
    this.setupOverlayView();
    this.setupProcessing();
    this.setupTextToSpeech("");
  }

  private void setupOverlayView() {
    ImageButton stop_but = findViewById(R.id.mediapipe_stop_button);
    CardView stop_card = findViewById(R.id.mediapipe_stop_card);
    Button continue_but = findViewById(R.id.mediapipe_continue_button);
    Button skip_but = findViewById(R.id.mediapipe_skip_exercise_button);
    Button finish_but = findViewById(R.id.mediapipe_finish_button);
    ImageButton recoding_but = findViewById(R.id.mediapipe_recording_button);
    stop_but.setOnClickListener(v -> showPauseCard());
    continue_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - timeCounter);
          setupTextToSpeech("continue");
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
    recoding_but.setOnClickListener(
        v -> {
          if (isRecording) {
            try {
              mediaRecorder.stop();
            } catch (Exception e) {
              Log.e(TAG, "Recording btn click-listener error", e);
            }
            releaseRecorder();
            recoding_but.setImageResource(R.drawable.ic_videocam_off_red_24dp);
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
            isRecording = false;
          } else if (setupRecorder()) {
            try {
              mediaRecorder.start();
            } catch (Exception e) {
              isRecording = false;
              Log.e(TAG, "Error when starting recording");
              return;
            }
            recoding_but.setImageResource(R.drawable.ic_videocam_on_red_24dp);
            Toast.makeText(this, "Started recording", Toast.LENGTH_SHORT).show();
            isRecording = true;
          } else {
            releaseRecorder();
            Toast.makeText(getApplicationContext(), "Couldn't do recording", Toast.LENGTH_SHORT)
                .show();
            recoding_but.setImageResource(R.drawable.ic_videocam_off_red_24dp);
            isRecording = false;
          }
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
                currentExercise,
                Objects.requireNonNull(exerciseIdToAmount.get(currentExercise.id)).get(setPointer),
                5);
          }

          @Override
          public void onError(@NonNull Throwable e) {
            e.printStackTrace();
          }
        });
  }

  @SuppressLint("CheckResult")
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void loadConstraints() {
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
    EglManager eglManager = new EglManager(null);
    this.processor =
        new FrameProcessor(
            this,
            eglManager.getNativeContext(),
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
            List<LandmarkProto.NormalizedLandmark> landmarkList = landmarks.getLandmarkList();
            for (int i = 11; i < landmarkList.size(); i++) {
              if (landmarkList.get(i).getPresence() < 0.5) {
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
                      this.setupTextToSpeech(constraint.message);
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
                      this.setupTextToSpeech(constraint.message);
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
            e.printStackTrace();
          }
        });
    this.converter = new ExternalTextureConverter(eglManager.getContext(), 2);
    this.converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    this.converter.addConsumer(this.processor);
  }

  private void getBundleData() {
    Bundle bundle = this.getIntent().getExtras();
    this.workoutId = bundle.getLong(WORKOUT_ID, 0);
    this.workoutTitle = bundle.getString(WORKOUT_TITLE, "Workout");
    this.cameraFacing =
        bundle.getBoolean(CAMERA_FACING_FLAG, true)
            ? CameraHelper.CameraFacing.FRONT
            : CameraHelper.CameraFacing.BACK;
    this.TextToSpeechEnabled = bundle.getBoolean(TEXT_TO_SPEECH_FLAG, true);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    this.startCamera();
  }

  @Override
  protected void onPause() {
    super.onPause();
    this.releaseRecorder();
    this.converter.close();
    surfaceView.setVisibility(View.GONE);
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.converter.close();
  }

  private void createVideoOutputFile() throws IOException {
    final String filename = String.valueOf(System.currentTimeMillis());
    final String directoryPath = this.fileRepository.getDirectoryPathDefault();
    final Uri uri =
        Uri.fromFile(
            new File(
                directoryPath
                    + File.separator
                    + filename
                    + FilenameUtils.EXTENSION_SEPARATOR
                    + "mp4"));
    outputFile = new File(uri.getPath());
    // outputFile.createNewFile();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void startCamera() {
    this.cameraHelper = new Camera2Helper(this, surfaceTexture);
    // this.cameraHelper = new CameraXPreviewHelper();
    this.cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          this.surfaceTexture = surfaceTexture;
          this.surfaceView.setVisibility(View.VISIBLE);
        });
    cameraHelper.startCamera(this, this.cameraFacing, null);
  }

  private void setupPreviewDisplay() {
    this.surfaceView = new SurfaceView(this);
    this.surfaceView.setVisibility(View.GONE);
    ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(surfaceView);
    this.surfaceView
        .getHolder()
        .addCallback(
            new SurfaceHolder.Callback() {
              @Override
              public void surfaceCreated(SurfaceHolder holder) {
                Surface surface = holder.getSurface();
                processor.getVideoSurfaceOutput().setSurface(surface);
              }

              @Override
              public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Size viewSize = new Size(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                boolean isCameraRotated = cameraHelper.isCameraRotated();
                converter.setSurfaceTextureAndAttachToGLContext(
                    surfaceTexture,
                    !isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                    !isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
                Display display =
                    ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                converter.setRotation(display.getRotation());
              }

              @Override
              public void surfaceDestroyed(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(null);
              }
            });
  }

  private boolean checkExerciseState() {
    int nextState = lastState + 1;
    if (nextState >= currentExercise.exerciseStates.size()) {
      nextState = 0;
    }
    if (classifier.judgeEnterState(currentExercise.exerciseStates.get(nextState))) {
      if (currentExercise.exerciseStates.get(lastState).stateTime
          > SystemClock.elapsedRealtime() - stateStartTime) {
        this.setExerciseX("Slower your execution speed");
        setupTextToSpeech("Slower your execution speed");
      }
      this.lastState = nextState;
      this.stateStartTime = SystemClock.elapsedRealtime();
      if (lastState == 0) countRepUp();
      return true;
    }
    return false;
  }

  private void setExerciseCheck() {
    ImageView check_x_mark = findViewById(R.id.mediapipe_check_x_mark);
    TextView evaluation_text = findViewById(R.id.mediapipe_evaluation_text);
    runOnUiThread(
        () -> {
          check_x_mark.setImageResource(R.drawable.ic_check_green_24dp);
          evaluation_text.setText(R.string.all_correct);
        });
  }

  private void setExerciseX(String reason) {
    ImageView check_x_mark = findViewById(R.id.mediapipe_check_x_mark);
    TextView evaluation_text = findViewById(R.id.mediapipe_evaluation_text);

    runOnUiThread(
        () -> {
          check_x_mark.setImageResource(R.drawable.ic_x_red_24dp);
          evaluation_text.setText(reason);
        });
  }

  private void setTimeCounter(long seconds) {
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

  private void stopTimeCounter() {
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    timeCounter = SystemClock.elapsedRealtime();
    time_counter.stop();
    timeStopped = true;
  }

  private void startTimeCounter() {
    if (timeStopped) {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      time_counter.setBase(time_counter.getBase() + SystemClock.elapsedRealtime() - timeCounter);
      time_counter.start();
      timeStopped = false;
    }
  }

  private void setExerciseName(String name) {
    TextView exerciseText = findViewById(R.id.mediapipe_exercise_name);
    runOnUiThread(() -> exerciseText.setText(name));
  }

  private void setRepetition(String Rep) {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    runOnUiThread(
        () -> {
          repetition_counter.setVisibility(View.VISIBLE);
          time_counter.setVisibility(View.GONE);
          repetition_counter.setText(Rep);
        });
  }

  private void countRepUp() {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    ++reps;
    runOnUiThread(() -> repetition_counter.setText(String.valueOf(reps)));
  }

  private void addFinishedExercise(Exercise exercise, boolean finished) {
    int duration;
    int amount = 0;
    if (exercise.isCountable()) {
      if (reps == 0) return;
      amount = reps;
      duration = (int) ((countableEndTime - countableStartTime) / 1000L);
    } else {
      if (finished) {
        duration = Objects.requireNonNull(exerciseIdToAmount.get(exercise.id)).get(setPointer);
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        duration =
            Objects.requireNonNull(exerciseIdToAmount.get(exercise.id)).get(setPointer)
                - ((int) ((time_counter.getBase() - timeCounter) / 1000L));
      }
    }
    finishedExercises.add(new FinishedExercise(0, exercise.id, duration, amount));
  }

  private void nextExerciseSet() {
    if (setPointer
        >= Objects.requireNonNull(exerciseIdToAmount.get(currentExercise.id)).size() - 1) {
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
          currentExercise,
          Objects.requireNonNull(exerciseIdToAmount.get(currentExercise.id)).get(setPointer),
          5);
    }
  }

  private void nextExercise(boolean finishedNormal) {
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
            currentExercise,
            Objects.requireNonNull(exerciseIdToAmount.get(currentExercise.id)).get(setPointer),
            5);
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
            if (exercisePointer >= exercises.size()) {
              Log.println(Log.DEBUG, TAG, "workout finished");
              paused = true;
              Handler handler = new Handler(Looper.getMainLooper());
              handler.post(this::showEndScreenAndSave);
              time_counter.stop();
            } else {
              paused = true;
              currentExercise = exercises.get(exercisePointer);
              setExerciseName(currentExercise.name);
              showNextExerciseSetDialog(
                  currentExercise,
                  Objects.requireNonNull(exerciseIdToAmount.get(currentExercise.id))
                      .get(setPointer),
                  5);
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
    setupTextToSpeech("Next Exercise " + amount + e.name);
    runOnUiThread(
        () -> {
          AlertDialog.Builder builder = new AlertDialog.Builder(MediaPipeActivity.this);
          DialogBetweenExerciseScreenBinding binding =
              DataBindingUtil.inflate(
                  LayoutInflater.from(MediaPipeActivity.this),
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
          loadConstraints();

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
          AlertDialog.Builder builder = new AlertDialog.Builder(MediaPipeActivity.this);
          DialogFinishedTrainingScreenBinding binding =
              DataBindingUtil.inflate(
                  LayoutInflater.from(MediaPipeActivity.this),
                  R.layout.dialog_finished_training_screen,
                  null,
                  false);
          builder
              .setView(binding.getRoot())
              .setPositiveButton(
                  "Finish",
                  (dialog, id) -> {
                    setupTextToSpeech("Training finished");
                    this.cameraHelper.closeCamera();
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
                  MediaPipeActivity.this,
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

  private void showPauseCard() {
    setupTextToSpeech("Pause");
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

  private void setupTextToSpeech(String text) {
    if (!TextToSpeechEnabled) return;
    textToSpeech =
        new TextToSpeech(
            getApplicationContext(),
            status -> {
              if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.UK);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1");
              }
            });
  }

  private void releaseRecorder() {
    if (mediaRecorder != null) {
      mediaRecorder.reset();
      mediaRecorder.release();
      recorderSurface.release();
      recorderTexture.release();
      mediaRecorder = null;
    }
  }

  private boolean setupRecorder() {
    CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
    mediaRecorder = new MediaRecorder();
    FrameProcessor processor2 =
        new FrameProcessor(
            this,
            new EglManager(null).getNativeContext(),
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME);
    processor2.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
    recorderTexture = new SurfaceTexture(0);
    recorderSurface = new Surface(recorderTexture);
    Surface sf = MediaCodec.createPersistentInputSurface();
    processor2.getVideoSurfaceOutput().setSurface(sf);
    mediaRecorder.setInputSurface(sf);
    mediaRecorder.setPreviewDisplay(recorderSurface);
    mediaRecorder.setOnErrorListener(
        (mr, what, extra) -> Log.e(TAG, "MediaRecorderError [what: " + what + " extra: " + extra));
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    mediaRecorder.setProfile(profile);
    try {
      createVideoOutputFile();
    } catch (IOException e) {
      Toast.makeText(
              this.getApplicationContext(), "Error: Couldn't setup recording", Toast.LENGTH_SHORT)
          .show();
      Log.d(TAG, "Outputfile couldn't be created: " + e.getMessage());
      return false;
    }
    mediaRecorder.setOutputFile(this.outputFile.getPath());
    try {
      mediaRecorder.prepare();
    } catch (IllegalStateException e) {
      Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
      releaseRecorder();
      return false;
    } catch (IOException e) {
      Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
      releaseRecorder();
      return false;
    }
    return true;
  }
}
