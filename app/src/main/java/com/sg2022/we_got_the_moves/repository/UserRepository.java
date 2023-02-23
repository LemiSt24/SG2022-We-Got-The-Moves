package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.daos.UserDao;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.TrophiesFragment;

import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {
  private static final String TAG = "UserRepository";

  private static volatile UserRepository INSTANCE;

  private final UserDao userDao;
  private final AppExecutors executors;

  private UserRepository(@NonNull AppDatabase db) {
    this.userDao = db.UserDao();
    this.executors = AppExecutors.getInstance();
  }

  public static UserRepository getInstance(Application app) {
    if (INSTANCE == null) {
      synchronized (WorkoutsRepository.class) {
        if (INSTANCE == null) {
          AppDatabase db = AppDatabase.getInstance(app);
          INSTANCE = new UserRepository(db);
        }
      }
    }
    return INSTANCE;
  }

  public void insertUser(User u) {
    this.executors.getPoolThread().execute(() -> this.userDao.insert(u));
  }

  public LiveData<User> getUser() {
    return this.userDao.getUser();
  }

  public void getUser(SingleObserver<User> observer) {
    this.userDao
        .getUserSingle()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void update(User u) {
    this.executors.getPoolThread().execute(() -> this.userDao.update(u));
  }

  public void getCameraBoolean(SingleObserver<Boolean> observer) {
    this.userDao
        .getCameraBoolean()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getTTSBoolean(SingleObserver<Boolean> observer) {
    this.userDao
        .getTTSBoolean()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  /*
  public void getTrophies(SingleObserver<Map<String, TrophiesFragment.ACHIEVEMENT>> observer){
    this.userDao
        .getTrophiesMap()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }*/

  public void updateTrophies(HashMap<String, TrophiesFragment.ACHIEVEMENT> trophies){
    this.executors.getPoolThread().execute(() -> this.userDao.updateTrophies(trophies));
  }
}
