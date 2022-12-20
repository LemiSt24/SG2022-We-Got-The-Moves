package com.sg2022.we_got_the_moves;

import android.app.Application;

import com.sg2022.we_got_the_moves.repository.FinishedTrainingRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class BasicApp extends Application {

  private static final String TAG = "BasicApp";

  private AppExecutors executors;
  private AppDatabase db;

  // TODO: Add repos here
  private WorkoutsRepository workoutsRepository;
  private FinishedTrainingRepository finishedTrainingRepository;

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO: Create singletons here
    this.executors = AppExecutors.getInstance();
    this.db = AppDatabase.getInstance(this);

    // TODO: Create instances of singleton-based repositories here
    this.workoutsRepository = WorkoutsRepository.getInstance(this);
    this.finishedTrainingRepository = FinishedTrainingRepository.getInstance(this);
  }

  public AppExecutors getExecutors() {
    return executors;
  }

  public AppDatabase getDb() {
    return db;
  }

  public WorkoutsRepository getWorkoutsRepository() {
    return workoutsRepository;
  }

  public FinishedTrainingRepository getFinishedTrainingRepository() {
    return finishedTrainingRepository;
  }
}
