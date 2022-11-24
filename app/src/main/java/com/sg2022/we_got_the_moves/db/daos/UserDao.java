package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.User;

@Dao
public interface UserDao {

    @Insert(onConflict = REPLACE)
    void insert(User user);

    @Update(onConflict = REPLACE)
    void update(User user);

    @Query("Select * From User")
    LiveData<User> getUser();
}
