package com.sg2022.we_got_the_moves.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionsHelper {

  private static final String TAG = "PermissionsHelper";

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

  public static String[] missingPermissions(Context context, String[] permissions) {
    List<String> result = new ArrayList<>();
    for (String p : permissions) {
      if (!checkPermission(context, p)) result.add(p);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return result.toArray(String[]::new);
    } else {
      return result.toArray(new String[0]);
    }
  }
}
