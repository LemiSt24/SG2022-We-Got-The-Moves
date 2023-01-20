package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.daos.ConstraintDao;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseStateDao;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseStateAndConstraints;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConstraintRepository {

  private static final String TAG = "ConstraintRepository";

  private static volatile ConstraintRepository INSTANCE;

  private final ExerciseDao exerciseDao;
  private final ExerciseStateDao exerciseStateDao;
  private final ConstraintDao constraintDao;
  private final AppExecutors executors;

  private ConstraintRepository(@NonNull AppDatabase db) {
    this.exerciseDao = db.ExerciseDao();
    this.exerciseStateDao = db.ExerciseStateDao();
    this.constraintDao = db.ConstraintDao();
    this.executors = AppExecutors.getInstance();
  }

  public static ConstraintRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (ConstraintRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app.getApplicationContext());
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

  public Single<Constraint> getConstraint(Long id){
     return this.constraintDao.getSingle(id);
  }

  /*
  public void getAllExerciseStates(
      int exerciseId, SingleObserver<List<ExerciseState.STATE>> observer) {
    this.exerciseStateDao
        .getAllStatesSingle(exerciseId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public Single<List<ExerciseState.STATE>> getAllStatesSingle(int exerciseId) {
    return this.exerciseStateDao.getAllStatesSingle(exerciseId);
  }

  public Single<List<ExerciseStateAndConstraints>> getStateAndConstraintsSingle(
      int exerciseId, ExerciseState.STATE state) {
    return this.exerciseStateDao.getStateAndConstraintsSingle(exerciseId, state);
  }*/
}
