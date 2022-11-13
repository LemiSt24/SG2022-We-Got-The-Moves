package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Workout;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert(onConflict = REPLACE)
    public void insert(Workout e);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Workout> ws);

    @Update(onConflict = REPLACE)
    public void update(Workout e);

    @Delete
    public void delete(Workout e);

    @Query("SELECT * FROM Workout WHERE Workout.id = :id")
    public LiveData<Workout> get(long id);

    @Transaction
    @Query("SELECT * FROM Workout")
    public LiveData<List<Workout>> getAll();
}
