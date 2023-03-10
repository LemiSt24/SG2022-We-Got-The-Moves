package com.sg2022.we_got_the_moves.util;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PermissionsChecker {

  private static final String TAG = "PermissionsChecker";

  public static boolean checkPermission(Context context, String permission) {
    return ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED;
  }

  public static boolean checkPermissions(Context context, String[] permissions) {
    for (String p : permissions) {
      if (!checkPermission(context, p)) return false;
    }
    return true;
  }

  public static boolean checkPermissions(Context context, String[]... permissions) {
    return checkPermissions(context, joinPermissions(permissions));
  }

  public static String[] joinPermissions(String[]... permissions) {
    Set<String> set = new HashSet<>();
    for (String[] p : permissions) {
      set.addAll(Arrays.asList(p));
    }
    String[] result = new String[set.size()];
    return set.toArray(result);
  }
}
