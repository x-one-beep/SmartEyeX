package com.smarteyex.core

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class ClockManager {

    private val handler = Handler(Looper.getMainLooper())
    private var offsetMillis: Long = 0L

    private var onTick: ((time: String, date: String) -> Unit)? = null

    private val runnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis() + offsetMillis

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

            val time = timeFormat.format(Date(now))
            val date = dateFormat.format(Date(now))

            onTick?.invoke(time, date)

            handler.postDelayed(this, 1000)
        }
    }

    fun start(onTick: (time: String, date: String) -> Unit) {
        this.onTick = onTick
        stop()
        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

    fun setOffsetSeconds(seconds: Int) {
        offsetMillis = seconds * 1000L
    }

    fun resetOffset() {
        offsetMillis = 0L
    }
}
