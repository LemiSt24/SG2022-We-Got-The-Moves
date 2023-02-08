package com.sg2022.we_got_the_moves;

import android.app.Application;

import com.sg2022.we_got_the_moves.io.IOExternalStorage;
import com.sg2022.we_got_the_moves.io.IOInternalStorage;
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class BasicApp extends Application {

  private static final String TAG = "BasicApp";
  private AppDatabase db;
  private AppExecutors executors;
  private IOExternalStorage externalStorage;
  private IOInternalStorage internalStorage;

  // TODO: Add repos here
  private WorkoutsRepository workoutsRepository;
  private FinishedWorkoutRepository finishedWorkoutRepository;
  private ConstraintRepository constraintRepository;
  private UserRepository userRepository;
  private FileRepository fileRepository;

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO: Create singletons here
    this.executors = AppExecutors.getInstance();
    this.db = AppDatabase.getInstance(this);
    this.externalStorage = IOExternalStorage.getInstance();
    this.internalStorage = IOInternalStorage.getInstance(this);

    // TODO: Create instances of singleton-based repositories here
    this.workoutsRepository = WorkoutsRepository.getInstance(this);
    this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(this);
    this.constraintRepository = ConstraintRepository.getInstance(this);
    this.userRepository = UserRepository.getInstance(this);
    this.fileRepository = FileRepository.getInstance(this);
  }
}
