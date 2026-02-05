package com.smarteyex.social

import kotlinx.coroutines.*
import kotlin.math.abs

/* =========================
   SOCIAL STATES
========================= */

enum class SocialMode {
    SILENT,        // diem total
    LISTENING,     // denger doang
    READY,         // boleh nimbrung
    INTERRUPT_OK   // boleh nyela sopan
}

data class SocialSignal(
    val speakerCount: Int,
    val avgSpeechSpeed: Float,
    val emotionLevel: Int,     // 1–10
    val keywordTrigger: Boolean,
    val userMentionedAI: Boolean
)

/* =========================
   ENGINE
========================= */

class SocialContextEngine {

    private var lastSpeakTime = 0L
    private val speakCooldown = 12_000L // 12 detik biar gak rese

    /* === ANALISIS KONTEKS SOSIAL === */
    fun evaluate(signal: SocialSignal): SocialMode {

        // 🔕 rame banget → DIAM
        if (signal.speakerCount >= 3 && signal.emotionLevel >= 7) {
            return SocialMode.SILENT
        }

        // 😡 emosi tinggi → jangan nyela
        if (signal.emotionLevel >= 8) {
            return SocialMode.LISTENING
        }

        // 🗣 user nyebut AI
        if (signal.userMentionedAI) {
            return SocialMode.INTERRUPT_OK
        }

        // 🎯 topik relevan
        if (signal.keywordTrigger && canSpeak()) {
            return SocialMode.READY
        }

        return SocialMode.LISTENING
    }

    /* === CEK ETIKA WAKTU BICARA === */
    private fun canSpeak(): Boolean {
        val now = System.currentTimeMillis()
        return abs(now - lastSpeakTime) > speakCooldown
    }

    fun markSpoken() {
        lastSpeakTime = System.currentTimeMillis()
    }
}