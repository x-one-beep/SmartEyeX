package com.smarteyex.core

import java.time.LocalDateTime

object SpeechCommandProcessor {

    private var activeTopic: String? = null

    fun process(input: String) {

        when {
            input.contains("mode sekolah") -> {
                ModeManager.setSchoolMode(); return
            }
            input.contains("mode game") -> {
                ModeManager.setGameMode(); return
            }
            input.contains("mode normal") -> {
                ModeManager.setNormalMode(); return
            }
            input.contains("putar lagu") || input.contains("setelin musik") -> {
                MusicContext.playForMood(AppState.emotion); return
            }
            input.contains("ingatkan") && input.contains("fix") -> {
                // contoh parsing sederhana
                SmartReminder.scheduleIfConfirmed(
                    title = "janjian",
                    dateTime = LocalDateTime.now().plusHours(2)
                )
                AppSpeak.say("Siap. Gue ingetin.")
                return
            }
        }

        if (NavigationStateManager.getState()
            == NavigationStateManager.State.WAITING_USER_RESPONSE
        ) {
            WaReplyManager.sendDirect(input)
            AppSpeak.say("Udah gue kirim.")
            return
        }

        activeTopic = inferTopic(input)
        val response = GroqAiEngine.chat(
            input, activeTopic, AppState.emotion
        )
        AppSpeak.say(response)
    }

    private fun inferTopic(text: String): String =
        when {
            text.contains("cewek") || text.contains("dia") -> "relationship"
            text.contains("capek") || text.contains("stress") -> "emotion"
            text.contains("game") -> "game"
            else -> "random"
        }
}