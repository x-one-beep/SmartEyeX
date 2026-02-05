package com.smarteyex.vision

import android.graphics.RectF
import kotlin.math.abs

/* =========================
   DATA MODEL
========================= */

enum class VisionMode {
    OBSERVE,        // cuma liat
    GUIDE,          // ngarahin
    WARNING,        // bahaya / salah
    CONFIRM         // validasi aksi user
}

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val box: RectF
)

data class HandMotion(
    val speed: Float,
    val direction: String
)

data class VisionContext(
    val objects: List<DetectedObject>,
    val handMotion: HandMotion?,
    val focusedObject: DetectedObject?
)

/* =========================
   ENGINE
========================= */

class VisionInstructorEngine {

    private var lastGuidanceTime = 0L
    private val guidanceCooldown = 800L // ms, real-time tapi gak cerewet

    fun analyze(context: VisionContext): VisionMode {

        // ❗ tombol emergency / bahaya
        context.focusedObject?.let {
            if (it.label.contains("emergency", true)) {
                return VisionMode.WARNING
            }
        }

        // 🖐 gerakan terlalu cepat
        context.handMotion?.let {
            if (it.speed > 1.4f) {
                return VisionMode.WARNING
            }
        }

        // 🎯 objek dikenal & aman
        if (context.focusedObject != null && canGuide()) {
            return VisionMode.GUIDE
        }

        return VisionMode.OBSERVE
    }

    private fun canGuide(): Boolean {
        val now = System.currentTimeMillis()
        return abs(now - lastGuidanceTime) > guidanceCooldown
    }

    fun markGuided() {
        lastGuidanceTime = System.currentTimeMillis()
    }
}