package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.relation.ExerciseAndExerciseStates;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface ExerciseStateDao {

  @Insert(onConflict = REPLACE)
  void insert(ExerciseState es);

  @Insert(onConflict = REPLACE)
  void insert(List<ExerciseState> l);

  @Transaction
  @Insert(onConflict = REPLACE)
  void insertAll(List<ExerciseState> es);

  @Update(onConflict = REPLACE)
  void update(ExerciseState es);

  @Delete
  void delete(ExerciseState es);

  @Transaction
  @Query("SELECT * FROM Exercise")
  Single<List<ExerciseAndExerciseStates>> getAllSingle();

  @Transaction
  @Query("SELECT * FROM ExerciseState WHERE ExerciseState.exerciseId == :exerciseId")
  Single<List<ExerciseState>> getAllSingle(long exerciseId);

}
