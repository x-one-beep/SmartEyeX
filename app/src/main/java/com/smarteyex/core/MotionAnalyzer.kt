package com.smarteyex.core

import android.graphics.Bitmap

/**
 * MotionAnalyzer
 * Analisis gerakan dari kamera / sensor
 */
class MotionAnalyzer {

    private var frameListener: ((Bitmap) -> Unit)? = null

    fun setFrameListener(listener: (Bitmap) -> Unit) {
        frameListener = listener
    }

    fun analyzeFrame(frame: Bitmap) {
        // Placeholder: deteksi motion / gesture
        // Bisa deteksi apakah user bergerak atau posisi tertentu
        frameListener?.invoke(frame)
    }
}