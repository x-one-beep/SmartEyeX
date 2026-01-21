package com.smarteyex.core

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class ClockManager {

    private val handler = Handler(Looper.getMainLooper())
    private var listener: ((String, String) -> Unit)? = null

    private val tick = object : Runnable {
        override fun run() {
            val now = Date()
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)
            listener?.invoke(time, date)
            handler.postDelayed(this, 1000)
        }
    }

    fun start(onTick: (String, String) -> Unit) {
        listener = onTick
        handler.post(tick)
    }

    fun stop() {
        handler.removeCallbacks(tick)
    }
}
