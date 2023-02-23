package com.sg2022.we_got_the_moves.ui.statistics.tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.databinding.FragmentStatisticsTrophiesBinding;
import com.sg2022.we_got_the_moves.db.entity.User;
import com.sg2022.we_got_the_moves.db.entity.relation.FinishedWorkoutAndFinishedExercises;
import com.sg2022.we_got_the_moves.ui.statistics.StatisticsViewModel;

import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class TrophiesFragment extends Fragment {
  private final String TAG = "TrophiesFragment";
  private FragmentStatisticsTrophiesBinding binding;
  private StatisticsViewModel model;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(this.requireActivity().getApplication());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    Log.println(Log.DEBUG, TAG, "onCreate");
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsTrophiesBinding.inflate(inflater, container, false);

    // setting night owl
    model.finishedWorkoutRepository.getAllFinishedWorkoutsSingle(
        new SingleObserver<List<FinishedWorkoutAndFinishedExercises>>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(
              @io.reactivex.rxjava3.annotations.NonNull
                  List<FinishedWorkoutAndFinishedExercises> finishedWorkoutAndFinishedExercises) {
            int night_trainings = 0;
            int last_id = -1;
            for (FinishedWorkoutAndFinishedExercises workout :
                finishedWorkoutAndFinishedExercises) {
              if (workout.finishedWorkout.id != last_id)
                if (workout.finishedWorkout.date.getHours() <= 5
                    || workout.finishedWorkout.date.getHours() >= 23) night_trainings++;
            }
            ACHIEVEMENT nightOwlAchievement = ACHIEVEMENT.NOT;
            if (night_trainings > 0) nightOwlAchievement = ACHIEVEMENT.LEVEL_ONE;
            if (night_trainings >= 10) nightOwlAchievement = ACHIEVEMENT.LEVEL_TWO;
            binding.cardviewNightOwl.setCardBackgroundColor(getColor(nightOwlAchievement));
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });

    // setting workout collector
    model.workoutsRepository.getWorkoutCount(
        new SingleObserver<List<Integer>>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> list) {
            Integer workouts = list.get(0);
            ACHIEVEMENT workoutCollectorAchievement = ACHIEVEMENT.NOT;
            if (workouts != null && workouts >= 10)
              workoutCollectorAchievement = ACHIEVEMENT.LEVEL_ONE;
            if (workouts != null && workouts >= 20)
              workoutCollectorAchievement = ACHIEVEMENT.LEVEL_TWO;
            binding.cardviewWorkoutCollector.setCardBackgroundColor(
                getColor(workoutCollectorAchievement));
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });
    /*
    model.workoutsRepository.getAllWorkouts(new SingleObserver<List<Workout>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Workout> workouts) {
        ACHIEVEMENT workoutCollectorAchievement = ACHIEVEMENT.NOT;
        if (workouts.size() >= 10) workoutCollectorAchievement = ACHIEVEMENT.LEVEL_ONE;
        if (workouts.size() >= 20) workoutCollectorAchievement = ACHIEVEMENT.LEVEL_TWO;
        binding.cardviewWorkoutCollector.setCardBackgroundColor(getColor(workoutCollectorAchievement));
      }

      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });*/

    // setting minimalist TODO add other cases
    ACHIEVEMENT minimalist = ACHIEVEMENT.NOT;
    binding.cardviewMinimalist.setCardBackgroundColor(getColor(minimalist));

    // setting Endurance
    model.finishedWorkoutRepository.getAllFinishedWorkoutsSingle(
        new SingleObserver<List<FinishedWorkoutAndFinishedExercises>>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(
              @io.reactivex.rxjava3.annotations.NonNull
                  List<FinishedWorkoutAndFinishedExercises> finishedWorkoutAndFinishedExercises) {
            long longestTraining = 0;
            int last_id = -1;
            for (FinishedWorkoutAndFinishedExercises workout :
                finishedWorkoutAndFinishedExercises) {
              if (workout.finishedWorkout.id != last_id)
                if (workout.finishedWorkout.duration.getSeconds() > longestTraining)
                  longestTraining = workout.finishedWorkout.duration.getSeconds();
            }
            ACHIEVEMENT longTrainingAchievement = ACHIEVEMENT.NOT;
            if (longestTraining / 60 >= 30) longTrainingAchievement = ACHIEVEMENT.LEVEL_ONE;
            if (longestTraining / 60 >= 60) longTrainingAchievement = ACHIEVEMENT.LEVEL_TWO;
            binding.cardviewLongTraining.setCardBackgroundColor(getColor(longTrainingAchievement));
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });

    // setting many calories (calorie goal as i don't want to calculate new calories)
    model.userRepository.getUser(
        new SingleObserver<User>() {
          @Override
          public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

          @Override
          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull User user) {
            ACHIEVEMENT calorieGoalAchievement = ACHIEVEMENT.NOT;
            if (user.calories >= 750) calorieGoalAchievement = ACHIEVEMENT.LEVEL_ONE;
            if (user.calories >= 1500) calorieGoalAchievement = ACHIEVEMENT.LEVEL_TWO;
            binding.cardviewManyCalories.setBackgroundColor(getColor(calorieGoalAchievement));
          }

          @Override
          public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });

    // setting the toast messages
    binding.cardviewNightOwl.setOnClickListener(
        v -> {
          Toast.makeText(getContext(), "The late bird gets the award", Toast.LENGTH_LONG).show();
        });
    binding.cardviewWorkoutCollector.setOnClickListener(
        v -> {
          Toast.makeText(
                  getContext(),
                  "You can never have to many workouts in your collection",
                  Toast.LENGTH_LONG)
              .show();
        });
    binding.cardviewMinimalist.setOnClickListener(
        v -> {
          Toast.makeText(getContext(), "Minimalism is the key to success", Toast.LENGTH_LONG)
              .show();
        });
    binding.cardviewLongTraining.setOnClickListener(
        v -> {
          Toast.makeText(getContext(), "Only for real workout enjoyer", Toast.LENGTH_LONG).show();
        });
    binding.cardviewManyCalories.setOnClickListener(
        v -> {
          Toast.makeText(getContext(), "Caution fire is hot", Toast.LENGTH_LONG).show();
        });

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  public int getColor(ACHIEVEMENT achievement) {
    if (achievement == ACHIEVEMENT.NOT) return getResources().getColor(R.color.grey);
    if (achievement == ACHIEVEMENT.LEVEL_ONE)
      return getResources().getColor(R.color.sg_design_green);
    if (achievement == ACHIEVEMENT.LEVEL_TWO) return getResources().getColor(R.color.gold);
    return 0;
  }

  public enum ACHIEVEMENT {
    NOT(0),
    LEVEL_ONE(1),
    LEVEL_TWO(2);
    private final int achievementInt;

    ACHIEVEMENT(int value) {
      this.achievementInt = value;
    }

    public int getValue() {
      return achievementInt;
    }
  }
}
