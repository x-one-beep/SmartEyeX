package com.smarteyex.voice

data class ProsodyProfile(
    val pitch: Float,        // nada
    val speed: Float,        // tempo
    val pauseMs: Long,       // jeda antar kalimat
    val warmth: Float        // kesan hangat
)