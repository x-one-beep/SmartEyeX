package com.smarteyex.core

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun checkAndRequest(
        activity: Activity,
        permissions: Array<String>,
        code: Int
    ): Boolean {
        val need = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it)
                    != PackageManager.PERMISSION_GRANTED
        }
        if (need.isEmpty()) return true
        ActivityCompat.requestPermissions(activity, need.toTypedArray(), code)
        return false
    }
}
