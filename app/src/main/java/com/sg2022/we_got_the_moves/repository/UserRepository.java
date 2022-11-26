package com.sg2022.we_got_the_moves.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.sg2022.we_got_the_moves.AppDatabase;
import com.sg2022.we_got_the_moves.AppExecutors;
import com.sg2022.we_got_the_moves.db.daos.UserDao;
import com.sg2022.we_got_the_moves.db.entity.User;

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
          AppDatabase db = AppDatabase.getInstance(app.getApplicationContext());
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

  public void update(User u) {
    this.executors.getPoolThread().execute(() -> this.userDao.update(u));
  }
}
