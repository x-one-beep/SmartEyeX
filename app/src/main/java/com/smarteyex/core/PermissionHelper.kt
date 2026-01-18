package com.yourpackage.smarteyex.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.yourpackage.smarteyex.utils.PermissionHelper;
public class PermissionHelper {

    public static final int PERMISSION_CODE = 130809;

    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
if (!PermissionHelper.hasAllPermissions(this)) {
    PermissionHelper.requestAllPermissions(this);
}

PermissionHelper.requestIgnoreBatteryOptimization(this);
    public static boolean hasAllPermissions(Activity activity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestAllPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                REQUIRED_PERMISSIONS,
                PERMISSION_CODE
        );
    }

    // ==========================
    // BATTERY OPTIMIZATION BYPASS
    // ==========================
    public static void requestIgnoreBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
            String packageName = activity.getPackageName();

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + packageName)
                );
                activity.startActivity(intent);
            }
        }
    }

    // ==========================
    // NOTIFICATION LISTENER
    // ==========================
    public static void openNotificationListenerSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        activity.startActivity(intent);
    }
}
