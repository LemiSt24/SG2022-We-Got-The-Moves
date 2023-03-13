package com.sg2022.we_got_the_moves.ui.training;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.SurfaceTexture;
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
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MediaPipeActivity extends AppCompatActivity {

  public static final List<String> landmark_names =
      Arrays.asList(
          "nose",
          "left_eye_inner",
          "left_eye",
          "left_eye_outer",
          "right_eye_inner",
          "right_eye",
          "right_eye_outer",
          "left_ear",
          "right_ear",
          "mouth_left",
          "mouth_right",
          "left_shoulder",
          "right_shoulder",
          "left_elbow",
          "right_elbow",
          "left_wrist",
          "right_wrist",
          "left_pinky_1",
          "right_pinky_1",
          "left_index_1",
          "right_index_1",
          "left_thumb_2",
          "right_thumb_2",
          "left_hip",
          "right_hip",
          "left_knee",
          "right_knee",
          "left_ankle",
          "right_ankle",
          "left_heel",
          "right_heel",
          "left_foot_index",
          "right_foot_index");
  private static final String TAG = "MediaPipeActivity";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_world_landmarks";
  private static final int STATE_CHANGE_VALUE = 10;
  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;
  private static CameraHelper.CameraFacing CAMERA_FACING;
  private static WeakReference<MediaPipeActivity> weakMediapipeActivity;

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
  // ApplicationInfo for retrieving metadata defined in the manifest.
  private Camera2Helper cameraHelper;
  // Saves the current time in counter at stopTimeCounter to use this at startTimeCounter
  private long time_counter_time = 0;
  // Sets true if time gets stopped and true if time gets started again. Can only start time if
  // time_stopped = true
  private boolean time_stopped = false;
  private PoseClassifier classifier;
  private long workoutId;
  private List<Exercise> exercises;
  private Map<Long, List<Integer>> exerciseIdToAmount;

  private int setPointer = 0;
  private int ExercisePointer = 0;
  private int lastState = 0;
  private int Reps = 0;
  private Exercise currentExercise;
  private HashMap<Exercise, List<ExerciseState>> exerciseToExerciseStates;
  private HashMap<ExerciseState, List<Constraint>> currentConstraints;
  private boolean timerSet = false;
  private Date startTime;
  private boolean Pause = true;
  private boolean firstTimeShowDialog = true;
  private List<FinishedExercise> finishedExercises;
  private long countableStartTime;
  private long countableEndTime;

  private long stateStartTime;
  private String finishedExerciseSummary = "";
  private Long timeLastCheck = SystemClock.elapsedRealtime();
  private TextToSpeech tts;
  private boolean ttsBoolean = true;
  private int timeBetweenExercises = 5;

  private boolean inStartPosition = false;

  private WorkoutsRepository workoutsRepository;

  public static MediaPipeActivity getInstanceActivity() {
    return weakMediapipeActivity.get();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    weakMediapipeActivity = new WeakReference<>(MediaPipeActivity.this);
    timeLastCheck = SystemClock.elapsedRealtime();

    UserRepository userRepository = UserRepository.getInstance(this.getApplication());
    userRepository.getCameraBoolean(
            new SingleObserver<>() {
              @Override
              public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
              }

              @Override
              public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                if (aBoolean) CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
                else CAMERA_FACING = CameraHelper.CameraFacing.BACK;
              }

              @Override
              public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
              }
            });

    userRepository.getTTSBoolean(
            new SingleObserver<>() {
              @Override
              public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

              @Override
              public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                ttsBoolean = aBoolean;
                tts("");
              }

              @Override
              public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
            });

    userRepository.getTimeBetweenExercise(new SingleObserver<>() {
        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

        @Override
        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
            timeBetweenExercises = integer;
        }

        @Override
        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });
    setContentView(getContentViewLayoutResId());

    startTime = new Date(System.currentTimeMillis());

    classifier = new PoseClassifier(getApplicationContext(), 20, 10, "dataset.csv");

    previewDisplayView = new SurfaceView(this);
    setupPreviewDisplayView();

    // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
    // binary graphs.
    AndroidAssetUtil.initializeNativeAssetManager(this);
    eglManager = new EglManager(null);
    processor =
        new FrameProcessor(
            this,
            eglManager.getNativeContext(),
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME);
    processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
    PermissionHelper.checkAndRequestCameraPermissions(this);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    workoutId = extras.getLong("WORKOUT_ID");

    Log.println(Log.DEBUG, TAG, "workoutId" + workoutId);

    // loading of the workout form db, with exercises and amounts
    workoutsRepository = WorkoutsRepository.getInstance(this.getApplication());
    finishedExercises = new ArrayList<>();
    exercises = new ArrayList<>();
    exerciseIdToAmount = new HashMap<>();
    exerciseToExerciseStates = new HashMap<>();
    workoutsRepository
        .getAllWorkoutExerciseAndExercise(workoutId)
        .observe(
            this,
            workoutExerciseAndExercises -> {
              for (WorkoutExerciseAndExercise wee : workoutExerciseAndExercises){
                  exercises.add(wee.exercise);
                  exerciseIdToAmount.put(wee.exercise.id, wee.workoutExercise.amount);
              }
              currentExercise = exercises.get(0);
              setExerciseName(currentExercise.name);
              if (currentExercise.isCountable()) setRepetition("0");

              //showing the first dialog only five seconds so that longer pause time affect training start
              showNextExerciseSetDialog(
                      currentExercise,
                      exerciseIdToAmount.get(currentExercise.id).get(0),
                      5);

              for (int i = 0; i < exercises.size(); i++) {
                  Exercise exercise = exercises.get(i);
                  workoutsRepository.getAllExerciseStates(exercise.id,
                      new SingleObserver<>() {
                          @Override
                          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                          }
                          @Override
                          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<ExerciseState> exerciseStates) {
                              exerciseStates.sort(new ExerciseStateComparator());
                              for (ExerciseState exerciseState: exerciseStates) Log.println(Log.DEBUG, "test", "exerciseState: " + exerciseState.id);
                              exerciseToExerciseStates.put(exercise, exerciseStates);
                          }
                          @Override
                          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                          }
                      }
                  );
              }
            });

    processor.addPacketCallback(
        OUTPUT_LANDMARKS_STREAM_NAME,
        (packet) -> {
          byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
          try {
            LandmarkProto.NormalizedLandmarkList landmarks =
                LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
            if (landmarks == null) {
              Log.v(TAG, "[TS:" + packet.getTimestamp() + "] No landmarks.");
              return;
            }
            List<LandmarkProto.NormalizedLandmark> landmarkList = landmarks.getLandmarkList();
            for (int i = 11; i < landmarkList.size(); i++) {
              if (landmarkList.get(i).getPresence() < 0.5) {
                Log.v(
                    TAG,
                    "landmark not visible: " + landmark_names.get(i) + " " + landmarkList.get(i));
                setExerciseX(landmark_names.get(i) + " is not visible");
              }
            }

            // Perform classification.
            // The classifier stores the result, it can be retrieved from get_result and used for
            // further analysis
            classifier.classify(landmarks);

            if (exercises.size() != 0) {

              if(!inStartPosition && !Pause){
                for (Constraint constraint :
                        currentConstraints.get((exerciseToExerciseStates.get(currentExercise)).get(lastState))) {
                  if (!classifier.judge_constraint(constraint)) {
                    setExerciseX(constraint.message);
                    return;
                  }
                  if (currentExercise.isCountable())
                    countableStartTime = SystemClock.elapsedRealtime();
                  stateStartTime = SystemClock.elapsedRealtime();
                  inStartPosition = true;
                }
              }

              // exercises time based
              if (!Pause && !currentExercise.isCountable()) {

                if (!timerSet) {
                  setTimeCounter(exerciseIdToAmount.get(currentExercise.id).get(setPointer));
                  timerSet = true;
                }

                if (SystemClock.elapsedRealtime() - timeLastCheck > 5000L) {
                  boolean changed = false;

                  for (Constraint constraint : 
                      currentConstraints.get(exerciseToExerciseStates.get(currentExercise).get(lastState))) {
                    if (!classifier.judge_constraint(constraint)) {
                      setExerciseX(constraint.message);
                      tts(constraint.message);
                      changed = true;
                      break;
                    }
                  }
                  if (!changed) {
                    setExerciseCheck();
                  }
                  timeLastCheck = SystemClock.elapsedRealtime();
                }
              }

              // exercises rep based
              else if (!Pause) {
                if (checkExerciseState()) { // TODO Change for toggleable Classifier
                  boolean changed = false;
                  for (Constraint constraint :
                      currentConstraints.get(exerciseToExerciseStates.get(currentExercise).get(lastState))) {
                    if (!classifier.judge_constraint(constraint)) {
                      setExerciseX(constraint.message);
                      tts(constraint.message);
                      changed = true;
                      break;
                    }
                  }
                  if (!changed) {
                    setExerciseCheck();
                  }
                }
                if (lastState == 0) {
                  // -> next Exercise
                  if (Reps >= exerciseIdToAmount.get(currentExercise.id).get(setPointer)) {
                    nextExerciseSet();
                  }
                }
              }
            }

          } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "Couldn't Exception received - " + e);
            return;
          }
        });

    ImageButton stop_but = findViewById(R.id.mediapipe_stop_button);
    CardView stop_card = findViewById(R.id.mediapipe_stop_card);
    Button continue_but = findViewById(R.id.mediapipe_continue_button);
    Button skip_but = findViewById(R.id.mediapipe_skip_exercise_button);
    Button finish_but = findViewById(R.id.mediapipe_finish_button);
    stop_but.setOnClickListener(
        v -> {
            showPauseCard();
        }
    );
    continue_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - time_counter_time);
          tts("continue");
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
          Pause = false;
        }
    );
    skip_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - time_counter_time);
          nextExercise(false);
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
        }
    );
    finish_but.setOnClickListener(
        v -> {
          countableStartTime += (SystemClock.elapsedRealtime() - time_counter_time);
          countableEndTime = SystemClock.elapsedRealtime();
          addFinishedExercise(currentExercise, false);
          showEndScreenAndSave();
        }
    );
  }

  // Used to obtain the content view for this application. If you are extending this class, and
  // have a custom layout, override this method and return the custom layout.
  protected int getContentViewLayoutResId() {
    return R.layout.activity_mediapipe;
  }

  @Override
  protected void onResume() {
    super.onResume();
    converter = new ExternalTextureConverter(eglManager.getContext(), 2);
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);
    if (PermissionHelper.cameraPermissionsGranted(this)) {
      startCamera();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    converter.close();

    // Hide preview display until we re-open the camera again.
    previewDisplayView.setVisibility(View.GONE);
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  public void startCamera() {
    int textureName = 65;
    cameraHelper = new Camera2Helper(this, new CustomSurfaceTexture(textureName));
    cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          previewFrameTexture = surfaceTexture;
          // Make the display view visible to start showing the preview. This triggers the
          // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
          previewDisplayView.setVisibility(View.VISIBLE);
        });
    // CameraHelper.CameraFacing cameraFacing = CAMERA_FACING;
    cameraHelper.startCamera(this, CAMERA_FACING, /*unusedSurfaceTexture=*/ null);
  }

  protected Size computeViewSize(int width, int height) {
    return new Size(width, height);
  }

  protected void onPreviewDisplaySurfaceChanged(
      SurfaceHolder holder, int format, int width, int height) {
    // (Re-)Compute the ideal size of the camera-preview display (the area that the
    // camera-preview frames get rendered onto, potentially with scaling and rotation)
    // based on the size of the SurfaceView that contains the display.
    Size viewSize = computeViewSize(width, height);
    Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
    boolean isCameraRotated = cameraHelper.isCameraRotated();

    // Connect the converter to the camera-preview frames as its input (via
    // previewFrameTexture), and configure the output width and height as the computed
    // display size.
    converter.setSurfaceTextureAndAttachToGLContext(
        previewFrameTexture,
        !isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
        !isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    converter.setRotation(display.getRotation());
  }

  private void setupPreviewDisplayView() {
    previewDisplayView.setVisibility(View.GONE);
    ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(previewDisplayView);

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

  /**
   * checks if the state of the user has changes to the expected next state
   *
   * @param classifierOutput output of the classifier that gets checked
   * @return true if the next (expected) state is recognised, otherwise false
   */
  public boolean checkExerciseState(Map<String, Integer> classifierOutput) {
    Log.println(Log.DEBUG, "classifier", classifierOutput.toString());
    if (classifierOutput != null) {
      int nextState = lastState + 1;
      if (nextState >= exerciseToExerciseStates.get(currentExercise).size()) {
        nextState = 0;
      }
      if (classifierOutput.containsKey(currentExercise.name.toLowerCase() + "_" + nextState)
          && classifierOutput.get(currentExercise.name.toLowerCase() + "_" + nextState)
              >= STATE_CHANGE_VALUE) {
        lastState = nextState;
        if (lastState == 0) countRepUp();
        return true;
      }
    }
    return false;
  }

  public boolean checkExerciseState() {
    int nextState = lastState + 1;
    if (nextState >= exerciseToExerciseStates.get(currentExercise).size()) {
      nextState = 0;
    }
    if (classifier.judgeEnterState(exerciseToExerciseStates.get(currentExercise).get(nextState))) {
      if (exerciseToExerciseStates.get(currentExercise).get(lastState).stateTime >
          SystemClock.elapsedRealtime() - stateStartTime){
          setExerciseX("Slower your execution speed");
          tts("Slower your execution speed");
      }
      lastState = nextState;
      stateStartTime = SystemClock.elapsedRealtime();
      if (lastState == 0) countRepUp();
      return true;
    }
    return false;
  }

  /**
   * Sets the check mark in the Constraints card-view -> is called when the user is doing everything
   * right running on separate UIThread
   */
  public void setExerciseCheck() {
    ImageView check_x_mark = findViewById(R.id.mediapipe_check_x_mark);
    TextView evaluation_text = findViewById(R.id.mediapipe_evaluation_text);

    runOnUiThread(
            () -> {
              check_x_mark.setImageResource(R.drawable.ic_check_green_24dp);
              evaluation_text.setText("all correct");
            });
  }

  /**
   * Sets a cross in the Constraints card-view -> user is doing something wrong running on separate
   * UIThread
   *
   * @param reason the text that describes what the user should change
   */
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
    time_stopped = false;
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
                          return;
                        }
                      });
            });
  }

  public void stopTimeCounter() {
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    time_counter_time = SystemClock.elapsedRealtime();
    time_counter.stop();
    time_stopped = true;
  }

  public void startTimeCounter() {
    if (time_stopped && !currentExercise.isCountable()) {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      time_counter.setBase(
          time_counter.getBase() + SystemClock.elapsedRealtime() - time_counter_time);
      time_counter.start();
      time_stopped = false;
    }
  }

  /**
   * Sets the exercise-name in the card-view
   *
   * @param name the name to be shown
   */
  public void setExerciseName(String name) {
    TextView exerciseText = findViewById(R.id.mediapipe_exercise_name);
    runOnUiThread(
            () -> exerciseText.setText(name));
  }

  /**
   * Sets the rep-counter to a specified value
   *
   * @param Rep the count to be set
   */
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

  /** Increments the rep-counter by one running on separate UIThread */
  public void countRepUp() {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    Reps = Reps + 1;
    runOnUiThread(
            () -> repetition_counter.setText(String.valueOf(Reps)));
  }

    // loads all constraints from db which are related to the supplied exercise and saves them in
    // class variables
    private void loadConstraintsForExercise() {
        ConstraintRepository constraintRepository =
                ConstraintRepository.getInstance(this.getApplication());

        currentConstraints = new HashMap<>();

        for (ExerciseState state : exerciseToExerciseStates.get(currentExercise)) {
            List<Constraint> constraints = new ArrayList<>();
            for (Long constraintId : state.constraintIds) {
                constraintRepository.getConstraint(constraintId, new SingleObserver<Constraint>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Constraint constraint) {
                        constraints.add(constraint);
                    }
                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
                });
            }
            currentConstraints.put(state, constraints);
        }
    }

  /**
   * Creates a finishedExercise-Object and adds it to finishedExercises
   *
   * @param exercise the exercise that should get saved
   * @param finished bool for time based exercises, if they have finished (for correct duration
   *     time)
   */
  public void addFinishedExercise(Exercise exercise, boolean finished) {
    int duration;
    int amount = 0;
    if (exercise.isCountable()) {
        if (Reps == 0) return;
      amount = Reps;
      duration = (int) ((countableEndTime - countableStartTime) / 1000L);
    } else {
      if (finished) {
        duration = exerciseIdToAmount.get(exercise.id).get(setPointer);
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        duration =
            exerciseIdToAmount.get(exercise.id).get(setPointer)
                - ((int) ((time_counter.getBase() - time_counter_time) / 1000L));
      }
    }
    finishedExercises.add(new FinishedExercise(0, exercise.id, duration, amount));
  }

  public void nextExerciseSet(){
    if (setPointer >= exerciseIdToAmount.get(currentExercise.id).size() - 1){
      nextExercise(true);
    }
    else{
      inStartPosition = false;
      Pause = true;
      countableEndTime = SystemClock.elapsedRealtime();
      addFinishedExercise(currentExercise, true);
      setPointer ++;
      Reps = 0;
      setRepetition(String.valueOf(0));
      showNextExerciseSetDialog(currentExercise,
              exerciseIdToAmount.get(currentExercise.id).get(setPointer), timeBetweenExercises);
    }
  }

  /**
   * Always called when an exercise is finished (all reps done, time up)
   * or when the skip button is pressed
   * @param finishedNormal <br>
   * - true if all proposed reps done / time completely up <br>
   * - false if called before exercise is regular finished (e.g. skip button pressed)
   */
  public void nextExercise(boolean finishedNormal){
    //current is cont based
    inStartPosition = false;
    if (currentExercise.isCountable()) {
      countableEndTime = SystemClock.elapsedRealtime();
      addFinishedExercise(currentExercise, finishedNormal);
      setPointer = 0;
      ExercisePointer++;

      // -> training finished
      if (ExercisePointer >= exercises.size()) {
        Pause = true;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
                this::showEndScreenAndSave);
      }

      // next exercise is loaded
      else {
        Pause = true;
        currentExercise = exercises.get(ExercisePointer);
        showNextExerciseSetDialog(
                currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), timeBetweenExercises);
        setExerciseName(currentExercise.name);
        Reps = 0;
        setRepetition(String.valueOf(0));
        setPointer = 0;
      }
    }
    //current is time based
    else {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      runOnUiThread(
              () -> {
                addFinishedExercise(currentExercise, finishedNormal);
                setPointer = 0;
                ExercisePointer++;
                //no more exercises
                if (ExercisePointer >= exercises.size()) {
                  Log.println(Log.DEBUG, TAG, "workout finished");
                  Pause = true;
                  Handler handler = new Handler(Looper.getMainLooper());
                  handler.post(
                          this::showEndScreenAndSave);
                  time_counter.stop();
                }
                //load new exercise
                else {
                  Pause = true;
                  currentExercise = exercises.get(ExercisePointer);
                  setExerciseName(currentExercise.name);
                  showNextExerciseSetDialog(
                          currentExercise, exerciseIdToAmount.get(currentExercise.id).get(setPointer), timeBetweenExercises);
                  timerSet = false;
                  Reps = 0;
                  setRepetition(String.valueOf(0));
                  time_counter.stop();
                  setPointer = 0;
                }
              });
    }
  }

  private void showNextExerciseSetDialog(
      @NonNull Exercise e, int amount, int seconds) {
    tts("Next Exercise " + amount + e.name);
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
          pause_countdown.setBase(SystemClock.elapsedRealtime() + 1000L * (long) seconds);
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

          pause_countdown.setOnChronometerTickListener(
              chronometer -> {
                long base = pause_countdown.getBase();
                if (base < SystemClock.elapsedRealtime()) {
                  loadConstraintsForExercise();
                  dialog.dismiss();
                  Pause = false;
                }
              });
        });
  }

  private void showEndScreenAndSave() {
    long endTime = System.currentTimeMillis();

    Duration timeSpent = Duration.of(endTime - startTime.getTime(), ChronoUnit.MILLIS);
    FinishedWorkout finishedWorkout = new FinishedWorkout(startTime, workoutId, timeSpent);

    FinishedWorkoutRepository finishedWorkoutRepository =
        FinishedWorkoutRepository.getInstance(getApplication());
    finishedWorkoutRepository.insert(finishedWorkout);

    finishedWorkoutRepository.getLastWorkoutSingle(new SingleObserver<FinishedWorkout>() {
        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
        @Override
        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull FinishedWorkout finishedWorkout) {
            for (FinishedExercise finishedExercise: finishedExercises) {
                finishedExercise.setFinishedWorkoutId(finishedWorkout.id);
            }
            finishedWorkoutRepository.insertFinishedExercise(finishedExercises);
        }
        @Override
        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
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
              .setNeutralButton(
                  "Finish",
                  (dialog, id) -> {
                    tts("Training finished");
                    cameraHelper.closeCamera();
                    finish();
                    dialog.dismiss();
                  });
          AlertDialog dialog = builder.create();
          dialog.setCanceledOnTouchOutside(false);
          dialog.setCancelable(false);
          dialog.show();

          // Getting the textViews
          TextView titel = dialog.findViewById(R.id.finished_trainings_screen_titel);
          TextView exerciseList = dialog.findViewById(R.id.finished_trainings_screen_exercise_list);
          TextView duration = dialog.findViewById(R.id.finished_trainings_screen_duration);

          // Setting the Workout Name
          WorkoutsRepository workoutsRepository = WorkoutsRepository.getInstance(getApplication());
          workoutsRepository
              .getWorkout(workoutId)
              .observe(MediaPipeActivity.this, x -> titel.setText(x.name));

          // Setting the duration
          String durationString = "Duration: " + timeSpent.toMinutes() + ":";
          if (timeSpent.getSeconds() % 60 < 10) {
            durationString += "0" + (timeSpent.getSeconds() % 60);
          } else {
            durationString += String.valueOf(timeSpent.getSeconds() % 60);
          }
          duration.setText(durationString);

          // Setting the exercise List

          workoutsRepository
              .getAllExercises(workoutId)
              .observe(
                  MediaPipeActivity.this,
                  exercises -> {
                    for (FinishedExercise finishedExercise : finishedExercises) {
                      for (Exercise exercise : exercises) {
                        if (exercise.id == finishedExercise.exerciseId) {
                          if (exercise.isCountable()) {
                            //
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
    if (Pause) return;
    tts("Pause");
    runOnUiThread(
            () -> {
              findViewById(R.id.mediapipe_stop_card).setVisibility(View.VISIBLE);
              findViewById(R.id.mediapipe_continue_button).setClickable(true);
              findViewById(R.id.mediapipe_skip_exercise_button).setClickable(true);
              findViewById(R.id.mediapipe_finish_button).setClickable(true);
            });
    stopTimeCounter();
    Pause = true;
  }

  @Override
  public void onBackPressed() {
    showPauseCard();
  }

  /**
   * Function for calling the Text-To-Speech model if user has turned off tts this method returns
   * immediately after calling
   *
   * @param text the text that gets read out
   */
  public void tts(String text) {
    if (!ttsBoolean) return;
    if (tts == null) {
    tts =
        new TextToSpeech(
            getApplicationContext(),
                status -> {
                  if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                    //if (!tts.isSpeaking()) ;
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1");
                  }
                });
    }
    else {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1");
    }

  }

    public static class ExerciseStateComparator implements Comparator<ExerciseState> {
        @Override
        public int compare(ExerciseState o1, ExerciseState o2) {
            return Integer.compare((int) o1.id,(int) o2.id);
        }
    }
}
