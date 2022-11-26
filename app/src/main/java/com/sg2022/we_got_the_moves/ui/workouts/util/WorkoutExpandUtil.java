package com.sg2022.we_got_the_moves.ui.workouts.util;

import android.view.View;

import com.sg2022.we_got_the_moves.R;

public class WorkoutExpandUtil {

  public boolean isVisible;

  public WorkoutExpandUtil() {
    this.isVisible = false;
  }

  public void switchValue() {
    this.isVisible = !this.isVisible;
  }

  public int getVisibility() {
    return this.isVisible ? View.VISIBLE : View.GONE;
  }

  public int getIcon() {
    return this.isVisible ? R.drawable.ic_arrow_up_black_24dp : R.drawable.ic_arrow_down_black_24dp;
  }
}
