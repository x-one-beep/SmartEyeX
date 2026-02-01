package com.smarteyex.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockManager(private val context: Context, private val clockTextView: TextView) {

    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var isRunning = false

    // Fungsi untuk start jam real-time
    fun startClock() {
        isRunning = true
        updateClock()
    }

    // Fungsi untuk stop jam
    fun stopClock() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateClock() {
        if (isRunning) {
            clockTextView.text = dateFormat.format(Date())
            handler.postDelayed({ updateClock() }, 1000)
        }
    }

    // Fungsi untuk set alarm (placeholder)
    fun setAlarm(hour: Int, minute: Int) {
        // Implementasi alarm sederhana
    }
}