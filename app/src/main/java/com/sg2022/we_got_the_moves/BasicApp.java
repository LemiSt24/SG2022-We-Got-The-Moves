package com.sg2022.we_got_the_moves;

import android.app.Application;
import com.sg2022.we_got_the_moves.repository.ConstraintRepository;
import com.sg2022.we_got_the_moves.repository.FileRepository;
import com.sg2022.we_got_the_moves.repository.FinishedWorkoutRepository;
import com.sg2022.we_got_the_moves.repository.UserRepository;
import com.sg2022.we_got_the_moves.repository.WorkoutsRepository;

public class BasicApp extends Application {

  private static final String TAG = "BasicApp";

  @Override
  public void onCreate() {
    super.onCreate();

    // TODO: Create singletons here
    AppExecutors.getInstance();
    AppDatabase.getInstance(this);

    // TODO: Create instances of singleton-based repositories here; Add repos here
    WorkoutsRepository.getInstance(this);
    FinishedWorkoutRepository.getInstance(this);
    ConstraintRepository.getInstance(this);
    UserRepository.getInstance(this);
    FileRepository.getInstance(this);
  }
}
