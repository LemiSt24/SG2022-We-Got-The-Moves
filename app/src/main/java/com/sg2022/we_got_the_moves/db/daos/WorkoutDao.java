package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Workout;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert(onConflict = REPLACE)
    void insert(Workout w);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Workout> ws);

    @Update(onConflict = REPLACE)
    void update(Workout w);

    @Delete
    void delete(Workout w);

    @Query("SELECT * FROM Workout WHERE Workout.id = :id")
    LiveData<Workout> get(long id);

    @Transaction
    @Query("SELECT * FROM Workout")
    LiveData<List<Workout>> getAll();
}
