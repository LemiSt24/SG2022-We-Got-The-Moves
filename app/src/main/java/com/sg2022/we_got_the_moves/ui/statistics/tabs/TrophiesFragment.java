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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;


public class TrophiesFragment extends Fragment {
  private final String TAG = "TrophiesFragment";
  private FragmentStatisticsTrophiesBinding binding;
  private StatisticsViewModel model;
  private Toast toast;
  private HashMap<String, ACHIEVEMENT> achievements;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StatisticsViewModel.Factory factory =
        new StatisticsViewModel.Factory(this.requireActivity().getApplication());
    this.model =
        new ViewModelProvider(this.requireActivity(), factory).get(StatisticsViewModel.class);
    achievements = new HashMap<String, ACHIEVEMENT>();
    Log.println(Log.DEBUG, TAG, "onCreate");
  }

  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.binding = FragmentStatisticsTrophiesBinding.inflate(inflater, container, false);

    //setting night owl
    model.finishedWorkoutRepository.getAllFinishedWorkoutsSingle(
            new SingleObserver<List<FinishedWorkoutAndFinishedExercises>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull
                  List<FinishedWorkoutAndFinishedExercises> finishedWorkoutAndFinishedExercises) {
        int night_trainings = 0;
        int last_id = -1;
        for (FinishedWorkoutAndFinishedExercises workout: finishedWorkoutAndFinishedExercises){
          if (workout.finishedWorkout.id != last_id)
            if (workout.finishedWorkout.date.getHours() <= 5 ||
                    workout.finishedWorkout.date.getHours() >= 23)
              night_trainings++;
        }
        achievements.put("nightOwl", ACHIEVEMENT.NOT);
        if (night_trainings > 0) achievements.put("nightOwl", ACHIEVEMENT.LEVEL_ONE);
        if (night_trainings >= 10) achievements.put("nightOwl", ACHIEVEMENT.LEVEL_TWO);
        binding.cardviewNightOwl.setCardBackgroundColor(getColor(achievements.get("nightOwl")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting workout collector
    model.workoutsRepository.getWorkoutCount(new SingleObserver<List<Integer>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> list) {
        Integer workouts = list.get(0);
        achievements.put("workoutCollector", ACHIEVEMENT.NOT);
        if (workouts != null && workouts >= 10) achievements.put("workoutCollector", ACHIEVEMENT.LEVEL_ONE);
        if (workouts != null && workouts >= 20) achievements.put("workoutCollector", ACHIEVEMENT.LEVEL_TWO);
        binding.cardviewWorkoutCollector.setCardBackgroundColor(getColor(achievements.get("workoutCollector")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting minimalist
    model.finishedWorkoutRepository
            .getNumberOfFinishedWorkoutsSmallerEqualNumberOfDistinctExercises(1, new SingleObserver<List<Integer>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> integers) {
          Integer number = integers.get(0);
          if (number == null) number = 0;
          achievements.put("minimalist", ACHIEVEMENT.NOT);
          if (number >= 5) achievements.put("minimalist", ACHIEVEMENT.LEVEL_ONE);
          if (number >= 25) achievements.put("minimalist", ACHIEVEMENT.LEVEL_TWO);
          binding.cardviewMinimalist.setCardBackgroundColor(getColor(achievements.get("minimalist")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting Endurance
    model.finishedWorkoutRepository.getLongestDurationOfFinishedWorkouts(new SingleObserver<List<Duration>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Duration> durations) {
        Duration longest = durations.get(0);
        int minutes = 0;
        if (longest != null) minutes = (int) (longest.getSeconds()/60);
        achievements.put("longTraining", ACHIEVEMENT.NOT);
        if (minutes >= 30) achievements.put("longTraining", ACHIEVEMENT.LEVEL_ONE);
        if (minutes >= 60) achievements.put("longTraining", ACHIEVEMENT.LEVEL_TWO);
        binding.cardviewLongTraining.setCardBackgroundColor(getColor(achievements.get("longTraining")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting many calories (calorie goal as i don't want to calculate new calories)
    model.userRepository.getUser(new SingleObserver<User>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull User user) {
        achievements.put("calorieGoal", ACHIEVEMENT.NOT);
        if (user.calories >= 750) achievements.put("calorieGoal", ACHIEVEMENT.LEVEL_ONE);
        if (user.calories >= 1250) achievements.put("calorieGoal", ACHIEVEMENT.LEVEL_TWO);
        binding.cardviewManyCalories.setCardBackgroundColor(getColor(achievements.get("calorieGoal")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting mountain challenge
    model.finishedWorkoutRepository.getTotalReps(1L, new SingleObserver<List<Integer>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> integers) {
        Integer reps = integers.get(0);
        if (reps == null) reps = 0;
        int heightInMeters = (int) 0.3 * reps;
        achievements.put("squatToTheTop", ACHIEVEMENT.NOT);
        if (heightInMeters >= 950) achievements.put("squatToTheTop", ACHIEVEMENT.LEVEL_ONE);
        if (heightInMeters >= 2962) achievements.put("squatToTheTop", ACHIEVEMENT.LEVEL_ONE_HALF);
        if (heightInMeters >= 8848) achievements.put("squatToTheTop", ACHIEVEMENT.LEVEL_TWO);
        binding.cardviewSquat.setCardBackgroundColor(getColor(achievements.get("squatToTheTop")));
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting maximalist
    model.finishedWorkoutRepository.getNumberDistinctFinishedExercises(new SingleObserver<List<Integer>>() {
      @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
      @Override
      public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> integers) {
        Integer finishedExercise = integers.get(0);
        if (finishedExercise == null) finishedExercise = 0;
        achievements.put("maximalist", ACHIEVEMENT.NOT);
        Integer finalFinishedExercise = finishedExercise;
        model.workoutsRepository.getExerciseCount(new SingleObserver<List<Integer>>() {
          @Override public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}
          @Override
          public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Integer> integers) {
            Integer exercises = integers.get(0);
            if (exercises == null) exercises = 1;
            if ((float) finalFinishedExercise.floatValue() / (float) exercises.floatValue() > 0.5)
              achievements.put("maximalist", ACHIEVEMENT.LEVEL_ONE);
            if (finalFinishedExercise == exercises) achievements.put("maximalist", ACHIEVEMENT.LEVEL_TWO);
            binding.cardviewMaximalist.setCardBackgroundColor(getColor(achievements.get("maximalist")));
          }
          @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
        });
      }
      @Override public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
    });


    //setting the toast messages
    binding.cardviewNightOwl.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
        "The late bird gets the award!",
        "Training in the night is beautiful.",
        "You're a real night time sportsman!"};
      toast = Toast.makeText(getContext(), text[achievements.get("nightOwl").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewWorkoutCollector.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
        "You can never have too many workouts in your collection!",
        "Every Situation gets its own training.",
        "You're a collector by heart."};
      toast = Toast.makeText(getContext(), text[achievements.get("workoutCollector").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewMinimalist.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
        "Minimalism is the key to success.",
        "One Exercise to rule them all!",
        "You're a real minimalist!"};
      toast = Toast.makeText(getContext(), text[achievements.get("minimalist").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewLongTraining.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
        "Only for real workout enjoyers.",
        "Longer workouts are your thing.",
        "You are the king of workouts!"};
      toast = Toast.makeText(getContext(), text[achievements.get("longTraining").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewManyCalories.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
        "Caution, fire is hot!",
        "From great goals comes great responsibility.",
        "Your goals are fire."};
      toast = Toast.makeText(getContext(), text[achievements.get("calorieGoal").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewSquat.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
              "Every squat gets you closer to the top. First Goal Wasserkuppe(960m)",
              "Wasserkuppe mastered. Next Challenge Zugspitze(2962m).",
              "You squatted to the top of Mount Everest. Your a real high climber.",
              //Level ONE_HALF text
              "Zugspitze mastered. Next Challenge Mount Everest(8848m)."};
      toast = Toast.makeText(getContext(), text[achievements.get("squatToTheTop").getValue()], Toast.LENGTH_LONG);
      toast.show();
    });
    binding.cardviewMaximalist.setOnClickListener(v -> {
      if (toast != null) toast.cancel();
      String[] text = {
              "You have to try them all.",
              "Already tried half of all exercises.",
              "You have done them all."};
      toast = Toast.makeText(getContext(), text[achievements.get("maximalist").getValue()], Toast.LENGTH_LONG);
      toast.show();
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


  public enum ACHIEVEMENT {
    NOT(0), LEVEL_ONE(1), LEVEL_ONE_HALF(3) ,LEVEL_TWO(2);
    private final int achievementInt;
    ACHIEVEMENT(int value) {this.achievementInt = value;}
    public int getValue() {return achievementInt;}
  }

  public int getColor(ACHIEVEMENT achievement){
    if (achievement == null || achievement == ACHIEVEMENT.NOT)
      return getResources().getColor(R.color.grey);
    if (achievement == ACHIEVEMENT.LEVEL_ONE || achievement == ACHIEVEMENT.LEVEL_ONE_HALF)
      return getResources().getColor(R.color.sg_design_green);
    if (achievement == ACHIEVEMENT.LEVEL_TWO)
      return getResources().getColor(R.color.gold);
    return 0;
  }

}
