package com.smarteyex.core

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat

object PermissionGate {

    fun requestAll(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            1001
        )
    }
}