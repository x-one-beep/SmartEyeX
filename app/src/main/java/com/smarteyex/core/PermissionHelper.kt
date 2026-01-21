package com.smarteyex.core

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun checkAndRequest(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int
    ): Boolean {
        val need = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        return if (need.isEmpty()) true
        else {
            ActivityCompat.requestPermissions(activity, need.toTypedArray(), requestCode)
            false
        }
    }

    // tambahan fungsi requestAll agar sesuai MainActivity
    fun requestAll(activity: Activity, requestCode: Int) {
        val perms = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        checkAndRequest(activity, perms, requestCode)
    }
}
