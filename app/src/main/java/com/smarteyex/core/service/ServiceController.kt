package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.os.Build
import com.smarteyex.app.R

object ServiceController {

    fun start(context: Context) {
        val intent = Intent(context, SmartEyeXService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop(context: Context) {
        val intent = Intent(context, SmartEyeXService::class.java)
        context.stopService(intent)
    }
}
