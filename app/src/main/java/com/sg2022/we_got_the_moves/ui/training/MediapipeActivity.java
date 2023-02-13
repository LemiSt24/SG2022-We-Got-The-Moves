package com.sg2022.we_got_the_moves.ui.training;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
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
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MediapipeActivity extends AppCompatActivity {

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
  private static final String TAG = "MediapipeActivity";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";
  private static final int STATE_CHANGE_VALUE = 10;
  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;
  private static CameraHelper.CameraFacing CAMERA_FACING;
  private static WeakReference<MediapipeActivity> weakMediapipeActivity;

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
  private ApplicationInfo applicationInfo;
  // Handles camera access via the {@link CameraX} Jetpack support library.
  private Camera2Helper cameraHelper;
  // Saves the current time in counter at stopTimeCounter to use this at startTimeCounter
  private long time_counter_time = 0;
  // Sets true if time gets stopped and true if time gets started again. Can only start time if
  // time_stopped = true
  private boolean time_stopped = false;
  private PoseClassifier classifier;
  private long workoutId;
  private List<Exercise> exercises;
  private Map<Long, Integer> exerciseIdToAmount;
  private int ExercisePointer = 0;
  private boolean lastStateWasTop = true;
  private int lastState = 0;
  private int Reps = 0;
  private Exercise currentExercise;
  private HashMap<ExerciseState, List<Constraint>> currentConstraints;
  private boolean timerSet = false;
  private Date startTime;
  private boolean noPause = false;
  private boolean firstTimeShowDialog = true;
  private List<FinishedExercise> finishedExercises;
  private long countableStartTime;
  private long countableEndTime;
  private String finishedExerciseSummary = "";
  private boolean timeUp;
  private Long timeLastCheck = SystemClock.elapsedRealtime();
  private TextToSpeech tts;
  private boolean ttsBoolean = true;

  public static MediapipeActivity getInstanceActivity() {
    return weakMediapipeActivity.get();
  }

  private static String getClassificationDebugString(Map<String, Integer> classification) {
    String classificationString = "";
    for (Map.Entry<String, Integer> entry : classification.entrySet()) {
      classificationString += entry.getKey() + ": " + entry.getValue() + "\n";
    }
    return classificationString;
  }

  private static String getLandmarksDebugString(LandmarkProto.NormalizedLandmarkList landmarks) {
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

  // loads all constraints from db which are related to the supplied exercise and saves them in
  // class variables
  private void loadConstraintsForExercise() {
    ConstraintRepository constraintRepository =
        ConstraintRepository.getInstance(this.getApplication());

    currentConstraints = new HashMap<ExerciseState, List<Constraint>>();

    for (ExerciseState state : currentExercise.exerciseStates) {
      List<Constraint> tmpConstraints = new ArrayList<Constraint>();
      for (Long constraintId : state.constraintIds) {
        Log.println(Log.DEBUG, TAG, constraintId.toString());
        constraintRepository
            .getConstraint(constraintId)
            .subscribeOn(Schedulers.io())
            .subscribe(
                constraint -> {
                  tmpConstraints.add(constraint);
                });
      }
      currentConstraints.put(state, tmpConstraints);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    weakMediapipeActivity = new WeakReference<>(MediapipeActivity.this);
    timeLastCheck = SystemClock.elapsedRealtime();

    UserRepository userRepository = UserRepository.getInstance(this.getApplication());
    userRepository.getCameraBoolean(
        new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
            if (aBoolean) CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
            else CAMERA_FACING = CameraHelper.CameraFacing.BACK;
            Log.println(Log.DEBUG, "test", "boolean now: " + aBoolean);
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });

    userRepository.getTTSBoolean(
        new SingleObserver<Boolean>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
            ttsBoolean = aBoolean;
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });
    tts("");
    setContentView(getContentViewLayoutResId());

    startTime = new Date(System.currentTimeMillis());

    /*
    // wird für nichts benutzt?, keine metadata in manifest?
    try {
      applicationInfo =
          getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Cannot find application info: " + e);
    }*/

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

    Log.println(Log.DEBUG, "workoutID", String.valueOf(workoutId));

    // loading of the workout form db, with exercises and amounts
    WorkoutsRepository workoutsRepository = WorkoutsRepository.getInstance(this.getApplication());
    finishedExercises = new ArrayList<FinishedExercise>();
    exercises = new ArrayList<Exercise>();
    exerciseIdToAmount = new HashMap<>();
    workoutsRepository
        .getAllExercises(workoutId)
        .observe(
            this,
            e -> {
              exercises = e;
              currentExercise = e.get(0);
              setExerciseName(currentExercise.name);
              if (currentExercise.isCountable()) setRepetition("0");

              for (int i = 0; i < exercises.size(); i++) {
                workoutsRepository
                    .getWorkoutExercise(workoutId, exercises.get(i).id)
                    .observe(
                        this,
                        workoutExercise -> {
                          exerciseIdToAmount.put(
                              workoutExercise.exerciseId, workoutExercise.amount.get(0)); //finished exercises dann auch anpassen
                          if (firstTimeShowDialog) {
                            showNextExerciseDialog(currentExercise, workoutExercise.amount.get(0), 5);
                            firstTimeShowDialog = false;
                          }
                        });
              }
            });

    AndroidPacketCreator packetCreator = processor.getPacketCreator();
    Map<String, Packet> inputSidePackets = new HashMap<>();

    //        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME,
    // packetCreator.createInt32(NUM_HANDS));
    //        processor.setInputSidePackets(inputSidePackets);

    // To show verbose logging, run:
    // adb shell setprop log.tag.MainActivity VERBOSE
    //        if (Log.isLoggable(TAG, Log.VERBOSE)) {
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
                return;
              }
            }

            // Klassifizierung durchführen.
            // Der Classifier speichert das Ergebnis, es lässt sich aus get_result abrufen und für
            // weitere Untersuchungen weiterverwenden
            classifier.classify(landmarks);

            /*  print all landmarks with name
            for (int i = 0; i < landmarkList.size(); i++) {
              Log.v(
                      TAG,
                      "name: " + landmark_names.get(i) +" "
                      + landmarkList.get(i));
            }*/

            if (exercises.size() != 0) {

              // exercises time based
              if (noPause && !currentExercise.isCountable()) {

                if (!timerSet) {
                  setTimeCounter(exerciseIdToAmount.get(currentExercise.id));
                  timerSet = true;
                  timeUp = false;
                }

                if (SystemClock.elapsedRealtime() - timeLastCheck > 5000L) {
                  boolean changed = false;

                  for (Constraint constraint :
                      currentConstraints.get(currentExercise.exerciseStates.get(lastState))) {
                    // Log.println(Log.DEBUG, "test",
                    // String.valueOf(classifier.judge_constraint(constraint)));
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
              else if (noPause) {
                if (checkExerciseState()) { // TODO Change for toggleable Classifier
                  boolean changed = false;
                  for (Constraint constraint :
                      currentConstraints.get(currentExercise.exerciseStates.get(lastState))) {
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
                  if (Reps >= exerciseIdToAmount.get(currentExercise.id)) {
                    nextExercise(true);
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
        });
    continue_but.setOnClickListener(
        v -> {
          tts("continue");
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
          noPause = true;
        });
    skip_but.setOnClickListener(
        v -> {
          nextExercise(false);
          stop_card.setVisibility(View.GONE);
          continue_but.setClickable(false);
          skip_but.setClickable(false);
          finish_but.setClickable(false);
          startTimeCounter();
          noPause = true;
        }
    );
    finish_but.setOnClickListener(
        v -> {
          finishedExercises.add(createFinishedExercise(currentExercise, false));
          showEndScreenAndSave();
        });
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

  /* protected void onCameraStarted(SurfaceTexture surfaceTexture) {
    previewFrameTexture = surfaceTexture;
    // Make the display view visible to start showing the preview. This triggers the
    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
    previewDisplayView.setVisibility(View.VISIBLE);
  }*/

  @Override
  protected void onPause() {
    super.onPause();
    converter.close();

    // Hide preview display until we re-open the camera again.
    previewDisplayView.setVisibility(View.GONE);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  protected Size cameraTargetResolution() {
    return null; // No preference and let the camera (helper) decide.
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
    if (currentExercise.isCountable()) {
      countableEndTime = SystemClock.elapsedRealtime();
      finishedExercises.add(createFinishedExercise(currentExercise, finishedNormal));
      ExercisePointer++;

      // -> training finished
      if (ExercisePointer >= exercises.size()) {
        noPause = false;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
          new Runnable() {
            public void run() {
              showEndScreenAndSave();
            }
          });
      }

      // next exercise is loaded
      else {
        currentExercise = exercises.get(ExercisePointer);
        noPause = false;
        showNextExerciseDialog(
                currentExercise, exerciseIdToAmount.get(currentExercise.id), 5);
        setExerciseName(currentExercise.name);
        Reps = 0;
        setRepetition(String.valueOf(0));
      }
    }
    //current is time based
    else {
      Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
      runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            finishedExercises.add(createFinishedExercise(currentExercise, finishedNormal));
            ExercisePointer++;
            //no more exercises
            if (ExercisePointer >= exercises.size()) {
              Log.println(Log.DEBUG, TAG, "workout finished");
              noPause = false;
              timeUp = true;
              Handler handler = new Handler(Looper.getMainLooper());
              handler.post(
                      new Runnable() {
                        public void run() {
                          showEndScreenAndSave();
                        }
                      });
              time_counter.stop();
            }
            //load new exercise
            else {
              currentExercise = exercises.get(ExercisePointer);
              setExerciseName(currentExercise.name);
              noPause = false;
              showNextExerciseDialog(
                      currentExercise, exerciseIdToAmount.get(currentExercise.id), 5);
              timerSet = false;
              Reps = 0;
              setRepetition(String.valueOf(0));
              time_counter.stop();
              timeUp = true;
            }
          }
        });
    }
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
    Log.println(Log.DEBUG, "test", "cameraroatation" + String.valueOf(isCameraRotated));

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
      if (nextState >= currentExercise.exerciseStates.size()) {
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
    if (nextState >= currentExercise.exerciseStates.size()) {
      nextState = 0;
    }
    if (classifier.judgeEnterState(currentExercise.exerciseStates.get(nextState))) {
      lastState = nextState;
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
        new Runnable() {
          @Override
          public void run() {
            check_x_mark.setImageResource(R.drawable.ic_check_green_24dp);
            evaluation_text.setText("all correct");
          }
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
        new Runnable() {
          @Override
          public void run() {
            check_x_mark.setImageResource(R.drawable.ic_x_red_24dp);
            evaluation_text.setText(reason);
          }
        });
  }

  public void setTimeCounter(long seconds) {
    time_stopped = false;
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);

    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {

            time_counter.setVisibility(View.VISIBLE);
            repetition_counter.setVisibility(View.GONE);
            time_counter.setBase(SystemClock.elapsedRealtime() + 1000 * seconds);
            time_counter.start();
            time_counter.setOnChronometerTickListener(
                new Chronometer.OnChronometerTickListener() {
                  @Override
                  public void onChronometerTick(Chronometer chronometer) {
                    long base = time_counter.getBase();
                    if (base < SystemClock.elapsedRealtime()) {
                      nextExercise(true);
                    }
                  }
                });
          }
        });
  }

  public void stopTimeCounter() {
    Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
    time_counter_time = SystemClock.elapsedRealtime();
    countableEndTime = time_counter_time;
    time_counter.stop();
    time_stopped = true;
  }

  public void startTimeCounter() {
    if (time_stopped) {
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
        new Runnable() {
          @Override
          public void run() {
            exerciseText.setText(name);
          }
        });
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
        new Runnable() {
          @Override
          public void run() {
            repetition_counter.setVisibility(View.VISIBLE);
            time_counter.setVisibility(View.GONE);
            repetition_counter.setText(Rep);
          }
        });
  }

  /** Increments the rep-counter by one running on separate UIThread */
  public void countRepUp() {
    TextView repetition_counter = findViewById(R.id.mediapipe_repetition_counter);
    Reps = Reps + 1;
    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            repetition_counter.setText(String.valueOf(Reps));
          }
        });
  }

  /**
   * Creates and returns a finishedExercise-Object
   *
   * @param exercise the exercise that should get saved
   * @param finished bool for time based exercises, if they have finished (for correct duration
   *     time)
   * @return the finishedExercise
   */
  public FinishedExercise createFinishedExercise(Exercise exercise, boolean finished) {
    int duration;
    int amount = 0;
    if (exercise.isCountable()) {
      amount = Reps;
      duration = (int) ((countableEndTime - countableStartTime) / 1000);
    } else {
      if (finished) {
        duration = exerciseIdToAmount.get(exercise.id);
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        duration =
            exerciseIdToAmount.get(exercise.id)
                - ((int) ((time_counter.getBase() - time_counter_time) / 1000));
      }
    }
    return new FinishedExercise(0, exercise.id, duration, amount);
  }

  private void showNextExerciseDialog(
      @NonNull Exercise e, @NonNull int amount, @NonNull int seconds) {
    tts("Next Exercise " + amount + e.name);
    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
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
            pause_countdown.setBase(SystemClock.elapsedRealtime() + 1000 * seconds);
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
                new Chronometer.OnChronometerTickListener() {
                  @Override
                  public void onChronometerTick(Chronometer chronometer) {
                    long base = pause_countdown.getBase();
                    if (base < SystemClock.elapsedRealtime()) {
                      dialog.dismiss();
                      noPause = true;
                      if (currentExercise.isCountable())
                        countableStartTime = SystemClock.elapsedRealtime();
                    }
                  }
                });
          }
        });
  }

  private void showEndScreenAndSave() {
    Long endTime = System.currentTimeMillis();

    Duration timeSpent = Duration.of(endTime - startTime.getTime(), ChronoUnit.MILLIS);
    FinishedWorkout training = new FinishedWorkout(startTime, workoutId, timeSpent);

    FinishedWorkoutRepository finishedWorkoutRepository =
        FinishedWorkoutRepository.getInstance(getApplication());
    finishedWorkoutRepository.insert(training);

    finishedWorkoutRepository
        .getLastTraining()
        .observe(
            this,
            lastTraining -> {
              for (int i = 0; i < finishedExercises.size(); i++) {
                Log.println(Log.DEBUG, "test", String.valueOf(lastTraining.id));
                finishedExercises.get(i).setFinishedWorkoutId(lastTraining.id);
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
              .observe(
                  MediapipeActivity.this,
                  x -> {
                    titel.setText(x.name);
                  });

          // Setting the duration
          String durationString = "Duration: " + String.valueOf(timeSpent.toMinutes()) + ":";
          if (timeSpent.getSeconds() % 60 < 10) {
            durationString += "0" + String.valueOf(timeSpent.getSeconds() % 60);
          } else {
            durationString += String.valueOf(timeSpent.getSeconds() % 60);
          }
          duration.setText(durationString);

          // Setting the exercise List

          workoutsRepository
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
    tts("Pause");
    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            findViewById(R.id.mediapipe_stop_card).setVisibility(View.VISIBLE);
            findViewById(R.id.mediapipe_continue_button).setClickable(true);
            findViewById(R.id.mediapipe_skip_exercise_button).setClickable(true);
            findViewById(R.id.mediapipe_finish_button).setClickable(true);
          }
        });
    stopTimeCounter();
    noPause = false;
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
    tts =
        new TextToSpeech(
            getApplicationContext(),
            new TextToSpeech.OnInitListener() {
              public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                  tts.setLanguage(Locale.UK);
                  if (!tts.isSpeaking())
                    ;
                  tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "1");
                }
              }
            });
  }
}
