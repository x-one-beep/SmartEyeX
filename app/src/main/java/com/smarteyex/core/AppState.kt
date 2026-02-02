package com.smarteyex.core

object AppState {

    enum class AiMode {
        ACTIVE,
        PASSIVE_AWARE,
        SLEEP
    }

    enum class Emotion {
        NETRAL,
        SANTAI,
        CAPEK,
        KESEL,
        SENENG,
        EMPATI,
        FOKUS
    }

    enum class UserMode {
        NORMAL,
        SEKOLAH,
        GAME
    }

    @Volatile
    var aiMode: AiMode = AiMode.ACTIVE

    @Volatile
    var emotion: Emotion = Emotion.NETRAL

    @Volatile
    var userMode: UserMode = UserMode.NORMAL

    @Volatile
    var isSpeaking: Boolean = false

    @Volatile
    var isListening: Boolean = false

    @Volatile
    var lastActiveTimestamp: Long = System.currentTimeMillis()

    fun updateActivity() {
        lastActiveTimestamp = System.currentTimeMillis()
        if (aiMode != AiMode.ACTIVE) {
            aiMode = AiMode.ACTIVE
        }
    }
}