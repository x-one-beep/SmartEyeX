package com.smarteyex.core

object ModeManager {

    fun setSchoolMode() {
        AppState.userMode = AppState.UserMode.SEKOLAH
        AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
        AppSpeak.say("Mode sekolah aktif. Gue kalem, gak nyeletuk.")
    }

    fun setGameMode() {
        AppState.userMode = AppState.UserMode.GAME
        AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
        AppSpeak.say("Mode game nyala. Panggil aja kalo perlu.")
    }

    fun setNormalMode() {
        AppState.userMode = AppState.UserMode.NORMAL
        AppState.aiMode = AppState.AiMode.ACTIVE
        AppSpeak.say("Balik normal. Gue standby.")
    }
}
