package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.daos.ConstraintDao;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseStateDao;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class ConstraintRepository {

  private static final String TAG = "ConstraintRepository";

  private static volatile ConstraintRepository INSTANCE;

  private final ExerciseStateDao exerciseStateDao;
  private final ConstraintDao constraintDao;
  private final AppExecutors executors;

  private ConstraintRepository(@NonNull AppDatabase db) {
    this.exerciseStateDao = db.ExerciseStateDao();
    this.constraintDao = db.ConstraintDao();
    this.executors = AppExecutors.getInstance();
  }

  public static ConstraintRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (ConstraintRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app);
          INSTANCE = new ConstraintRepository(db);
        }
      }
    }
    return INSTANCE;
  }

  public void insertExerciseState(ExerciseState es) {
    this.executors.getPoolThread().execute(() -> this.exerciseStateDao.insert(es));
  }

  public void insertExerciseStates(List<ExerciseState> l) {
    this.executors.getPoolThread().execute(() -> this.exerciseStateDao.insert(l));
  }

  public void insertConstraint(Constraint c) {
    this.executors.getPoolThread().execute(() -> this.constraintDao.insert(c));
  }

  public void insertConstraints(List<Constraint> l) {
    this.executors.getPoolThread().execute(() -> this.constraintDao.insert(l));
  }

  public Single<Constraint> getConstraint(Long id) {
    return this.constraintDao.getSingle(id);
  }
}
