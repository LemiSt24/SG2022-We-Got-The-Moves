package com.sg2022.we_got_the_moves;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sg2022.we_got_the_moves.db.entity.Constraint;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.ExerciseState;
import com.sg2022.we_got_the_moves.db.entity.FinishedExercise;
import com.sg2022.we_got_the_moves.db.entity.FinishedWorkout;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;
import com.sg2022.we_got_the_moves.db.entity.daos.ConstraintDao;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.daos.ExerciseStateDao;
import com.sg2022.we_got_the_moves.db.entity.daos.FinishedExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.daos.FinishedWorkoutDao;
import com.sg2022.we_got_the_moves.db.entity.daos.UserDao;
import com.sg2022.we_got_the_moves.db.entity.daos.WorkoutDao;
import com.sg2022.we_got_the_moves.db.entity.daos.WorkoutExerciseDao;
import com.sg2022.we_got_the_moves.ui.statistics.tabs.TrophiesFragment;

import java.util.HashMap;

// TODO: Add entity classes here
@Database(
    entities = {
      User.class,
      Exercise.class,
      Workout.class,
      WorkoutExercise.class,
      FinishedWorkout.class,
      FinishedExercise.class,
      ExerciseState.class,
      Constraint.class
    },
    version = 1,
    exportSchema = false)
@TypeConverters({com.sg2022.we_got_the_moves.db.converter.TypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
  public static final String DB_NAME = "SGWeGotTheMovesDB";
  private static final String TAG = "AppDatabase";
  private static volatile AppDatabase INSTANCE;

  public static AppDatabase getInstance(@NonNull final Application app) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          AppExecutors e = AppExecutors.getInstance();
          INSTANCE = buildDatabase(app, e);
          /*INSTANCE = Room.databaseBuilder(app.getApplicationContext(), AppDatabase.class, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .createFromAsset("database/SGWeGotTheMovesDB.db")
                        .build();*/
        }
      }
    }
    return INSTANCE;
  }

  private static AppDatabase buildDatabase(
      @NonNull final Application app, final AppExecutors executors) {
    return Room.databaseBuilder(app.getApplicationContext(), AppDatabase.class, DB_NAME)
        .fallbackToDestructiveMigration()
        .addCallback(
            new Callback() {
              @Override
              public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
              }

              @Override
              public void onCreate(@NonNull SupportSQLiteDatabase db1) {
                super.onCreate(db1);
                Log.d(TAG, "DB created");


                HashMap<String, TrophiesFragment.ACHIEVEMENT> trophies = new HashMap<>();
                executors
                    .getPoolThread()
                    .execute(
                        () -> {
                          // TODO: Init DB with Dummy Data only at creation time
                          getInstance(app)
                              .ExerciseDao()
                              .insertAll(DataGenerator.getDummyExercises());
                          getInstance(app)
                              .ExerciseStateDao()
                              .insertAll(DataGenerator.getDummyExerciseStates());
                          getInstance(app).WorkoutDao().insertAll(DataGenerator.getDummyWorkouts());
                          getInstance(app)
                              .WorkoutExerciseDao()
                              .insertAll(DataGenerator.getDummyWorkoutExercises());
                          getInstance(app)
                              .UserDao()
                              .insert(
                                  new User(
                                      "Simon Westermann",
                                      (float) 1.77,
                                      50,
                                      User.SEX.MALE,
                                      22,
                                      500,
                                      10,
                                      true,
                                      true,
                                      trophies));
                          getInstance(app)
                              .FinishedWorkoutDao()
                              .insert(DataGenerator.getDummyFinsishedWorkouts());
                          getInstance(app)
                              .FinishedExerciseDao()
                              .insert(DataGenerator.getDummyFinsishedExercise());
                          getInstance(app)
                              .ConstraintDao()
                              .insert(DataGenerator.giveMeDummyConstraints());
                        });
              }
            })
        .build();
  }

  // TODO: Add DAOs below
  public abstract ExerciseDao ExerciseDao();

  public abstract WorkoutDao WorkoutDao();

  public abstract WorkoutExerciseDao WorkoutExerciseDao();

  public abstract UserDao UserDao();

  public abstract FinishedWorkoutDao FinishedWorkoutDao();

  public abstract FinishedExerciseDao FinishedExerciseDao();

  public abstract ExerciseStateDao ExerciseStateDao();

  public abstract ConstraintDao ConstraintDao();
}
