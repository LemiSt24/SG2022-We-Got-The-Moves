package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import java.util.List;

@Dao
public interface ExerciseStateDao {

  @Insert(onConflict = REPLACE)
  void insert(ExerciseState es);

  @Insert(onConflict = REPLACE)
  void insert(List<ExerciseState> l);

  @Update(onConflict = REPLACE)
  void update(ExerciseState es);

  @Delete
  void delete(ExerciseState es);
}
