package com.smarteyex.core

import android.os.Handler
import android.os.Looper

class DelayManager {

    private val handler = Handler(Looper.getMainLooper())

    // Fungsi untuk debounce aksi (e.g., delay notifikasi)
    fun debounce(delayMillis: Long, action: () -> Unit) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(action, delayMillis)
    }

    // Fungsi untuk schedule aksi
    fun schedule(delayMillis: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMillis)
    }
}