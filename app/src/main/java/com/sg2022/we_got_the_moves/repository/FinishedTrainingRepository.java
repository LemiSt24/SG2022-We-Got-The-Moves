package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.daos.FinishedTrainingDao;
import com.sg2022.we_got_the_moves.db.entity.FinishedTraining;

import java.util.List;

public class FinishedTrainingRepository {
  private static final String TAG = "FinishedTrainingRepository";

  private static volatile FinishedTrainingRepository INSTANCE;

  private final FinishedTrainingDao Dao;
  private final AppExecutors executors;

  private FinishedTrainingRepository(@NonNull AppDatabase db) {
    this.Dao = db.FinishedTrainingDao();
    this.executors = AppExecutors.getInstance();
  }

  public static FinishedTrainingRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (FinishedTrainingRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app.getApplicationContext());
          INSTANCE = new FinishedTrainingRepository(db);
        }
      }
    }
    return INSTANCE;
  }

  public void insert(FinishedTraining finishedTraining) {
    this.executors.getPoolThread().execute(() -> this.Dao.insert(finishedTraining));
  }

  public LiveData<List<FinishedTraining>> getNLastTrainigs(int n) {
    return this.Dao.getNLastTrainings(n);
  }

  public LiveData<FinishedTraining> getLastTraining() {
    return this.Dao.getLastTraining();
  }

  public LiveData<List<Long>> getNLastDistinctWorkoutIds(int n) {
    return this.Dao.getNLastDistictWorkoutIds(n);
  }

  public LiveData<List<FinishedTraining>> getOrderedFinishedWorkouts() {
    return this.Dao.getOrderedTrainings();
  }
}
