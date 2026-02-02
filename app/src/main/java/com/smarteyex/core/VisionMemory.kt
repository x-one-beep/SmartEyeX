package com.smarteyex.core

import androidx.camera.core.ImageProxy
import java.util.concurrent.ConcurrentHashMap

object VisionMemory {

    private val faceMemory = ConcurrentHashMap<String, String>()

    fun processFrame(frame: ImageProxy) {
        // Placeholder vision hook:
        // Di sini nanti dipasang ML Kit / custom vision
    }

    fun rememberFace(tag: String, name: String) {
        if (!faceMemory.containsKey(tag)) {
            faceMemory[tag] = name
        }
    }

    fun identifyFace(tag: String): String? {
        return faceMemory[tag]
    }
}