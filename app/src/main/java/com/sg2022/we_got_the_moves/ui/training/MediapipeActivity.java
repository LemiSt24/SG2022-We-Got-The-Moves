package com.sg2022.we_got_the_moves.ui.training;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import com.google.mediapipe.components.CameraXPreviewHelper;
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
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseStateAndConstraints;
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MediapipeActivity extends AppCompatActivity {

  private static final String TAG = "MediapipeActivity";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";

  private static final int STATE_CHANGE_VALUE = 7;

  // private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
  private static CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
  // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
  // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
  // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
  // corner, whereas MediaPipe in general assumes the image origin is at top-left.
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
  // ApplicationInfo for retrieving metadata defined in the manifest.
  private ApplicationInfo applicationInfo;
  // Handles camera access via the {@link CameraX} Jetpack support library.
  private CameraXPreviewHelper cameraHelper;

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
  private HashMap<ExerciseState,List<Constraint>> currentConstraints;
  private boolean timerSet = false;

  private Date startTime;
  private boolean noPause = false;
  private boolean firstTimeShowDialog = true;
  private List<FinishedExercise> finishedExercises;

  private long countableStartTime;
  private long countableEndTime;
  private String finishedExerciseSummary = "";

  private boolean timeUp;

  private TextToSpeech tts;

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

    for (ExerciseState state: currentExercise.exerciseStates){
      List<Constraint> tmpConstraints = new ArrayList<Constraint>();
      for (Long constraintId : state.constraintIds){
        Log.println(Log.DEBUG, TAG, constraintId.toString());
        constraintRepository.getConstraint(constraintId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        constraint -> {
                          tmpConstraints.add(constraint);
                        }
                );
      }
      currentConstraints.put(state, tmpConstraints);
    }

    /*
    constraintRepository
        .getAllStatesSingle((int) currentExercise.id)
        .subscribeOn(Schedulers.io())
        .subscribe(
            states -> {
              for (ExerciseState.STATE state : states) {
                Log.println(Log.DEBUG, "constraintFrom", "moinsen");
                constraintRepository
                    .getStateAndConstraintsSingle((int) currentExercise.id, state)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        state_and_constraints -> {
                          for (ExerciseStateAndConstraints stateAndConstraints :
                              state_and_constraints) {
                            if (stateAndConstraints.exerciseState.exerciseState
                                == ExerciseState.STATE.BOTTOM) {
                              bottom_constraints.addAll(stateAndConstraints.constraints);
                            } else if (stateAndConstraints.exerciseState.exerciseState
                                == ExerciseState.STATE.TOP) {
                              top_constraints.addAll(stateAndConstraints.constraints);
                            } else if (stateAndConstraints.exerciseState.exerciseState
                                == ExerciseState.STATE.GLOBAL) {
                              global_constraints.addAll(stateAndConstraints.constraints);
                            }
                          }
                        });
              }
            }); */
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UserRepository userRepository = UserRepository.getInstance(this.getApplication());
    userRepository.getCameraBoolean(new SingleObserver<Boolean>() {
      @Override
      public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
        if (!aBoolean){CAMERA_FACING = CameraHelper.CameraFacing.BACK;}
      }
      @Override
      public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
      }
    );
    tts("");
    setContentView(getContentViewLayoutResId());

    startTime = new Date(System.currentTimeMillis());

    // wird für nichts benutzt?, keine metadata in manifest?
    try {
      applicationInfo =
          getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Cannot find application info: " + e);
    }

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
              // Log.println(Log.DEBUG,"workoutID", String.valueOf(exercises.get(0).name));
              // Exercise firstE = exercises.get(0);
              // rep or time
              // classifier(e.get(i))
              //
              currentExercise = e.get(0);
              setExcerciseName(currentExercise.name);
              if (currentExercise.isCountable()) setRepetition("0");

              for (int i = 0; i < exercises.size(); i++) {
                workoutsRepository
                    .getWorkoutExercise(workoutId, exercises.get(i).id)
                    .observe(
                        this,
                        workoutExercise -> {
                          exerciseIdToAmount.put(
                              workoutExercise.exerciseId, workoutExercise.amount);
                          if (firstTimeShowDialog) {
                            showNextExerciseDialog(currentExercise, workoutExercise.amount, 5);
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
          /*    Log.println(Log.DEBUG,"test", "drüber");
          Log.println(Log.DEBUG,"test", packet.toString());
          Log.println(Log.DEBUG,"test", "Received multi-hand landmarks packet.");*/
          byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
          try {
            LandmarkProto.NormalizedLandmarkList landmarks =
                LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
            if (landmarks == null) {
              Log.v(TAG, "[TS:" + packet.getTimestamp() + "] No iris landmarks.");
              return;
            }

            // Klassifizierung durchführen.
            // Der Classifier speichert das Ergebnis, es lässt sich aus get_result abrufen und für
            // weitere Untersuchungen weiterverwenden
            classifier.classify(landmarks);

            // Beispielhafte Analyse von Rahmenbedingungen
            /*Log.v(
            TAG,
            "Schultern: "
                + classifier.get_distance("left_shoulder", "right_shoulder")
                + ", Füße: "
                + classifier.get_distance("left_ankle", "right_ankle")); */
            // Note: If eye_presence is false, these landmarks are useless.
            Log.v(
                TAG,
                "[TS:"
                    + packet.getTimestamp()
                    + "] #Landmarks for iris: "
                    + landmarks.getLandmarkCount());
            // Log.v(TAG, getLandmarksDebugString(landmarks));
            //      Log.println(Log.DEBUG,"test", String.valueOf(exercises.size()));
            if (exercises.size() != 0) {
              /*         Log.println(Log.DEBUG,"test", classification.toString());


              Log.println(Log.DEBUG, TAG, "Exercises there " + exercises.get(0).name);
              Log.println(Log.DEBUG, TAG, exterciseIDToAmount.get(exercises.get(0).id).toString());*/

              // exercises time based
              if (noPause && !currentExercise.isCountable()) {
                if (!timerSet) {
                  setTimeCounter(exerciseIdToAmount.get(currentExercise.id));
                  timerSet = true;
                  timeUp = false;
                  while (!timeUp){
                    boolean changed = false;
                    for (Constraint constraint:
                            currentConstraints.get(currentExercise.exerciseStates.get(lastState))){
                      if (!classifier.judge_constraint(constraint)){
                        setExerciseX(constraint.message);
                        tts(constraint.message);
                        changed = true;
                        break;
                      }
                    }
                    if (!changed) {
                      setExerciseCheck();
                    }
                    try {
                      TimeUnit.SECONDS.sleep(5L);
                    } catch (InterruptedException e){
                      Log.println(Log.DEBUG, TAG, e.getMessage());
                    }

                  }
                }

              }

              // exercises rep based
              else if (noPause){
                int nextState = checkExerciseState(classifier.get_result(),
                        lastState);

                if (nextState == lastState + 1) {
                  lastState = nextState;
                  boolean changed = false;
                  for (Constraint constraint:
                          currentConstraints.get(currentExercise.exerciseStates.get(lastState))){
                      if (!classifier.judge_constraint(constraint)){
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
                else if (nextState == 0 && lastState != 0) {
                  countRepUp();
                  lastState = nextState;
                  //     Log.println(Log.DEBUG, TAG, "Reps: "+ String.valueOf(Reps));
                  //     Log.println(Log.DEBUG, TAG, "Amount" +
                  // String.valueOf(exerciseIdToAmount.get(currentExercise.id)));
                  if (Reps >= exerciseIdToAmount.get(currentExercise.id)) {
                    // TODO next Exercise
                    countableEndTime = SystemClock.elapsedRealtime();
                    finishedExercises.add(createFinishedExercise(currentExercise, true));
                    ExercisePointer++;
                    //   Log.println(Log.DEBUG, TAG, "exercisePointer: "+
                    // String.valueOf(ExercisePointer));
                    //   Log.println(Log.DEBUG, TAG, "exercises.size()"+
                    // String.valueOf(ExercisePointer));
                    if (ExercisePointer >= exercises.size()) {
                      noPause = false;
                      Handler handler = new Handler(Looper.getMainLooper());
                      handler.post(
                              new Runnable() {
                                public void run() {
                                  showEndScreenAndSave();
                                }
                              });

                    } else {
                      currentExercise = exercises.get(ExercisePointer);
                      noPause = false;
                      showNextExerciseDialog(
                              currentExercise, exerciseIdToAmount.get(currentExercise.id), 5);
                      setExcerciseName(currentExercise.name);
                      Reps = 0;
                      setRepetition(String.valueOf(0));
                    }
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
          finish_but.setClickable(false);
          startTimeCounter();
          noPause = true;
        });
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

  protected void onCameraStarted(SurfaceTexture surfaceTexture) {
    previewFrameTexture = surfaceTexture;
    // Make the display view visible to start showing the preview. This triggers the
    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
    previewDisplayView.setVisibility(View.VISIBLE);
  }

  protected Size cameraTargetResolution() {
    return null; // No preference and let the camera (helper) decide.
  }

  public void startCamera() {
    cameraHelper = new CameraXPreviewHelper();
    cameraHelper.setOnCameraStartedListener(
        surfaceTexture -> {
          onCameraStarted(surfaceTexture);
        });
    CameraHelper.CameraFacing cameraFacing = CAMERA_FACING;
    cameraHelper.startCamera(
        this, cameraFacing, /*unusedSurfaceTexture=*/ null, cameraTargetResolution());
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
        isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
        isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
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

  public int checkExerciseState(
      Map<String, Integer> classifierOutput, int lastState) {
    Log.println(Log.DEBUG, "classifier", classifierOutput.toString());
    if (classifierOutput != null) {
      int nextState = lastState + 1;
      if (nextState >= currentExercise.exerciseStates.size()){
        nextState = 0;
      } 
      if (classifierOutput.containsKey(currentExercise.name.toLowerCase() + "_" + nextState)
              && classifierOutput.get(currentExercise.name.toLowerCase() + "_" + nextState)
              >= STATE_CHANGE_VALUE) {
        return nextState;
      }
    }
    return lastState;
  }

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
                      finishedExercises.add(createFinishedExercise(currentExercise, true));
                      ExercisePointer++;
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
                      } else {
                        currentExercise = exercises.get(ExercisePointer);
                        setExcerciseName(currentExercise.name);
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

  public void setExcerciseName(String name) {
    TextView exerciseText = findViewById(R.id.mediapipe_exercise_name);
    runOnUiThread(
        new Runnable() {

          @Override
          public void run() {

            exerciseText.setText(name);
          }
        });
  }

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

  public String exerciseToString(Exercise e, boolean finished) {
    String exerciseString = "";
    int amount;
    if (finished) {
      amount = exerciseIdToAmount.get(e.id);
    } else {
      if (e.isCountable()) {
        amount = Reps;
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        amount =
            exerciseIdToAmount.get(e.id)
                - ((int) ((time_counter.getBase() + SystemClock.elapsedRealtime()) / 1000));
        // time_counter.getBase() + SystemClock.elapsedRealtime() - time_counter_time
      }
    }
    if (amount == 0) return "";

    if (e.isCountable()) {
      exerciseString = amount + " x " + e.name;
    } else {
      exerciseString = amount + " s " + e.name;
    }
    return exerciseString;
  }

  public FinishedExercise createFinishedExercise(Exercise e, boolean finished) {
    String exerciseString = "";
    int duration = 0;
    int amount = 0;
    if (e.isCountable()) {
      amount = Reps;
      duration = (int) ((countableEndTime - countableStartTime) / 1000);
    } else {
      if (finished) {
        duration = exerciseIdToAmount.get(e.id);
      } else {
        Chronometer time_counter = findViewById(R.id.mediapipe_time_counter);
        duration =
            exerciseIdToAmount.get(e.id)
                - ((int) ((time_counter.getBase() - time_counter_time) / 1000));
      }
    }

    FinishedExercise finishedExercise = new FinishedExercise(0, e.id, duration, amount);
    return finishedExercise;
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

  public void tts(String text) {
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
