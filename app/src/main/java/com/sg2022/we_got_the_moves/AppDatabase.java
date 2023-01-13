package com.sg2022.we_got_the_moves;

import android.content.Context;
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
import com.sg2022.we_got_the_moves.utils.DataGenerator;

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

  public static AppDatabase getInstance(final Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          Context c = context.getApplicationContext();
          AppExecutors e = AppExecutors.getInstance();
          INSTANCE = buildDatabase(c, e);
        }
      }
    }
    return INSTANCE;
  }

  private static AppDatabase buildDatabase(final Context appContext, final AppExecutors executors) {
    return Room.databaseBuilder(appContext.getApplicationContext(), AppDatabase.class, DB_NAME)
        .fallbackToDestructiveMigration()
        .addCallback(
            new Callback() {
              @Override
              public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
                Log.d(TAG, "DB opened");
              }

              @Override
              public void onCreate(@NonNull SupportSQLiteDatabase db1) {
                super.onCreate(db1);
                Log.d(TAG, "DB created");
                executors
                    .getPoolThread()
                    .execute(
                        () -> {
                          // TODO: Init DB with Dummy Data only at creation time
                          getInstance(appContext)
                              .ExerciseDao()
                              .insertAll(DataGenerator.getDummyExercises());
                          getInstance(appContext)
                              .WorkoutDao()
                              .insertAll(DataGenerator.getDummyWorkouts());
                          getInstance(appContext)
                              .WorkoutExerciseDao()
                              .insertAll(DataGenerator.getDummyWorkoutExercises());
                          getInstance(appContext)
                              .UserDao()
                              .insert(
                                  new User(
                                      "Simon Westermann",
                                      (float) 1.77,
                                      50,
                                      User.SEX.MALE,
                                      22,
                                      500));
                          getInstance(appContext)
                              .FinishedWorkoutDao()
                              .insert(DataGenerator.getDummyFinsishedWorkouts());
                          getInstance(appContext)
                              .FinishedExerciseDao()
                              .insert(DataGenerator.getDummyFinsishedExercise());
                          getInstance(appContext)
                              .ConstraintDao()
                              .insert(DataGenerator.getDummyExerciseStatesAndConstraints().second);
                          getInstance(appContext)
                              .ExerciseStateDao()
                              .insert(DataGenerator.getDummyExerciseStatesAndConstraints().first);
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
