package com.smarteyex.core.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun checkAndRequest(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
        val list = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        return if (list.isEmpty()) true
        else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), requestCode)
            false
        }
    }
}
