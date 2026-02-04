package com.smarteyex.core.service

import android.content.Context
import android.os.Build
import android.os.PowerManager

object ServiceController {

    fun isBatteryOptimized(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else true
    }

    fun requestBatteryExclusion(context: Context) {
        // Intent untuk user exclude battery optimization
    }
}