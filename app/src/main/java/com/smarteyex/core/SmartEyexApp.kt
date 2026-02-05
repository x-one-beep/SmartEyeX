package com.smarteyex.core

import android.app.Application
import android.content.Intent

class SmartEyexApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AppState.init(this)

        // Nyalakan CoreService sejak app hidup
        val serviceIntent = Intent(this, CoreService::class.java)
        startForegroundService(serviceIntent)
    }
}