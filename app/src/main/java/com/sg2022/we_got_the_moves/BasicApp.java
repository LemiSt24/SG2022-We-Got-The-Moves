package com.sg2022.we_got_the_moves;

import android.app.Application;

import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class BasicApp extends Application {

  private static final String TAG = "BasicApp";

  private AppExecutors executors;
  private AppDatabase db;

  // TODO: Add repos here
  private WorkoutsRepository workoutsRepository;
  private FinishedWorkoutRepository finishedWorkoutRepository;
  private ConstraintRepository constraintRepository;
  private UserRepository userRepository;

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO: Create singletons here
    this.executors = AppExecutors.getInstance();
    this.db = AppDatabase.getInstance(this);

    // TODO: Create instances of singleton-based repositories here
    this.workoutsRepository = WorkoutsRepository.getInstance(this);
    this.finishedWorkoutRepository = FinishedWorkoutRepository.getInstance(this);
    this.constraintRepository = ConstraintRepository.getInstance(this);
    this.userRepository = UserRepository.getInstance(this);
  }

  public AppExecutors getExecutors() {
    return executors;
  }

  public AppDatabase getDb() {
    return db;
  }

  public WorkoutsRepository getWorkoutsRepository() {
    return this.workoutsRepository;
  }

  public FinishedWorkoutRepository getFinishedTrainingRepository() {
    return this.finishedWorkoutRepository;
  }

  public ConstraintRepository getConstraintRepository() {
    return this.constraintRepository;
  }

  public UserRepository getUserRepository() {
    return this.userRepository;
  }

}
