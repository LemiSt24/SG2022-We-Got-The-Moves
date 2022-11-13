package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.User;

@Dao
public interface UserDao {

    @Insert(onConflict = REPLACE)
    public void insert(User user);

    @Update(onConflict = REPLACE)
    public void update(User user);
}
