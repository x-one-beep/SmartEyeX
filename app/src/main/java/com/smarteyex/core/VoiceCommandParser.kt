package com.smarteyex.core

class VoiceCommandParser {

    fun parse(command: String): String {
        val c = command.lowercase()

        return when {
            "kamera" in c -> "OPEN_CAMERA"
            "jam" in c -> "SHOW_TIME"
            "ai" in c -> "ASK_AI"
            "diam" in c -> "STOP_VOICE"
            else -> "UNKNOWN"
        }
    }
}
