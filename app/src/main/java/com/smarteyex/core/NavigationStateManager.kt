package com.smarteyex.core.navigation

import android.content.Context
import android.content.Intent
import com.smarteyex.core.MainActivity
import com.smarteyex.core.camera.CameraFragment

class NavigationStateManager(private val context: Context) {

    fun openCamera() {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("open", "camera")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openAI() {
        // Panel AI nanti bisa fragment / activity sendiri
    }

    fun openMemory() {
        // Halaman memory viewer
    }

    fun openWA() {
        // Status WA listener
    }

    fun openSetting() {
        // Setting SmartEyeX
    }
}