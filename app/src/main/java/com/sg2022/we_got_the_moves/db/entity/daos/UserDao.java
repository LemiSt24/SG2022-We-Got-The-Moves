package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.TrophiesFragment;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {

  @Insert(onConflict = REPLACE)
  void insert(User user);

  @Update(onConflict = REPLACE)
  void update(User user);

  @Query("SELECT * FROM User LIMIT 1")
  LiveData<User> getUser();

  @Query("Select * From User LIMIT 1")
  Single<User> getUserSingle();

  @Query("Select frontCamera From User LIMIT 1")
  Single<Boolean> getCameraBoolean();

  @Query("Select tts From User LIMIT 1")
  Single<Boolean> getTTSBoolean();

  /*
  @Query("Select trophies FROM User LIMIT 1")
  Single<Map<String, TrophiesFragment.ACHIEVEMENT>> getTrophiesMap(); */

  @Query("UPDATE User set trophies = :trophies")
  void updateTrophies(HashMap<String, TrophiesFragment.ACHIEVEMENT> trophies);
}
