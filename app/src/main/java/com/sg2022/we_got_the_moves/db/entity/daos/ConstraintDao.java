package com.sg2022.we_got_the_moves.db.entity.daos;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.sg2022.we_got_the_moves.db.entity.Constraint;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

@Dao
public interface ConstraintDao {

  @Insert(onConflict = REPLACE)
  void insert(Constraint c);

  @Insert(onConflict = REPLACE)
  void insert(List<Constraint> l);

  @Update(onConflict = REPLACE)
  void update(Constraint c);

  @Delete
  void delete(Constraint c);

  @Query("SELECT * FROM [Constraint] WHERE [Constraint].id == :constraintId")
  Single<Constraint> getSingle(Long constraintId);
}
