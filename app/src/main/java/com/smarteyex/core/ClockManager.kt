package com.smarteyex.core.clock

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockManager(private val textView: TextView) {

    private val handler = Handler(Looper.getMainLooper())
    private val format = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val runnable = object : Runnable {
        override fun run() {
            textView.text = format.format(Date())
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