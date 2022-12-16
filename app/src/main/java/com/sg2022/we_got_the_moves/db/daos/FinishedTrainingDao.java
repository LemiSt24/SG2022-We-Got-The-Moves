package com.sg2022.we_got_the_moves.db.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sg2022.we_got_the_moves.db.entity.FinishedTraining;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Dao
public interface FinishedTrainingDao {

    @Insert(onConflict = REPLACE)
    void insert(FinishedTraining finishedTraining);

    @Query("Select * From FinishedTraining Order by date Desc limit :n")
    LiveData<List<FinishedTraining>> getNLastTrainings(int n);

    @Query("Select * From FinishedTraining order by date Desc")
    LiveData<List<FinishedTraining>> getOrderedTrainings();

    @Query("Select * From FinishedTraining Order by date Desc limit 1")
    LiveData<FinishedTraining> getLastTraining();

    @Query("Select distinct workoutId From FinishedTraining Order by date Desc limit :n")
    LiveData<List<Long>> getNLastDistictWorkoutIds(int n);

    /*
    @Query("Select * From FinishedTraining Where date >= :endDate Order by date Desc")
    List<FinishedTraining> getTrainingsAfter(LocalDate endDate);
    */
}
