package com.sg2022.we_got_the_moves;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sg2022.we_got_the_moves.db.daos.ExerciseDao;
import com.sg2022.we_got_the_moves.db.daos.UserDao;
import com.sg2022.we_got_the_moves.db.daos.WorkoutDao;
import com.sg2022.we_got_the_moves.db.daos.WorkoutExerciseDao;
import com.sg2022.we_got_the_moves.db.entity.Exercise;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.Workout;
import com.sg2022.we_got_the_moves.db.entity.WorkoutExercise;

// TODO: Add entity classes here
@Database(
    entities = {User.class, Exercise.class, Workout.class, WorkoutExercise.class},
    version = 1,
    exportSchema = false)
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
                              .insert(new User("Simon Westermann", (float) 1.77, 50, false, 22));
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
}
