package com.smarteyex.core.clock

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockManager(private val context: Context, private val tvClock: TextView) {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            tvClock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            handler.postDelayed(this, 1000)
        }
    }

    fun start() {
        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }
}
