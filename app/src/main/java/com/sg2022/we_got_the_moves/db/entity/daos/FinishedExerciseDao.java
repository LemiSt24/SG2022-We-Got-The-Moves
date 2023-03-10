package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

@Dao
public interface FinishedExerciseDao {

  @Insert(onConflict = REPLACE)
  void insert(FinishedExercise fe);

  @Insert(onConflict = REPLACE)
  void insert(List<FinishedExercise> l);

  @Insert(onConflict = REPLACE)
  Single<Long> insertSingle(FinishedExercise fe);

  @Insert(onConflict = REPLACE)
  Single<List<Long>> insertAllSingle(List<FinishedExercise> l);

  @Update(onConflict = REPLACE)
  void update(FinishedExercise fe);

  @Delete
  void delete(FinishedExercise fe);

  @Query("SELECT SUM(fe.amount) FROM FinishedExercise fe WHERE fe.exerciseId = :exerciseId")
  Single<List<Integer>> getTotalReps(long exerciseId);

  @Query("SELECT COUNT(*) FROM (SELECT fe.exerciseId FROM FinishedExercise fe WHERE fe.duration > 0 GROUP BY fe.exerciseId)")
  Single<List<Integer>> getNumberDistinctFinishedExercises();
}
