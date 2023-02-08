package com.sg2022.we_got_the_moves.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CustomFragmentStateAdapter extends FragmentStateAdapter {

  public static final String TAG = "CustomFragmentStateAdapter";
  private final List<Class<? extends Fragment>> fragments;

  public CustomFragmentStateAdapter(
      @NonNull Fragment fragment, @NonNull final List<Class<? extends Fragment>> fragments) {
    super(fragment);
    this.fragments = fragments;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    try {
      return this.fragments.get(position).newInstance();
    } catch (IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }
    //noinspection ConstantConditions
    return null;
  }

  @Override
  public int getItemCount() {
    return this.fragments.size();
  }
}
