package com.smarteyex.core.service

import android.content.Context

class ServiceController(private val context: Context) {

    // Fungsi untuk manage semua service modular
    fun startAllServices() {
        SmartEyeXService.startService(context)
        // Start lainnya jika perlu
    }

    fun stopAllServices() {
        SmartEyeXService.stopService(context)
    }

    // Fungsi untuk check status service
    fun isServiceRunning(): Boolean {
        // Implementasi check
        return true