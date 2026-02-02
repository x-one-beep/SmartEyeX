package com.smarteyex.core

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.util.concurrent.atomic.AtomicBoolean

class MotionAnalyzer(
    private val onFrame: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    private val running = AtomicBoolean(true)

    override fun analyze(image: ImageProxy) {
        if (!running.get()) {
            image.close()
            return
        }

        onFrame(image)
        image.close()
    }

    fun stop() {
        running.set(false)
    }
}