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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

  public void getConstraint(Long id, SingleObserver<Constraint> observer) {
    this.constraintDao
            .getSingle(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer);
  }
}
