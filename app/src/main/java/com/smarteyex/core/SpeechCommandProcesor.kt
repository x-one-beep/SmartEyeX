package com.smarteyex.core

object SpeechCommandProcessor {

    private var activeTopic: String? = null

    fun process(input: String) {

        // MODE SWITCH (VOICE)
        when {
            input.contains("mode sekolah") -> {
                AppState.userMode = AppState.UserMode.SEKOLAH
                AppSpeak.say("Oke, gue diem. Fokus sekolah dulu.")
                return
            }

            input.contains("mode game") -> {
                AppState.userMode = AppState.UserMode.GAME
                AppSpeak.say("Gas. Gue nongkrong di background aja.")
                return
            }

            input.contains("bung smart aktif") -> {
                AppState.aiMode = AppState.AiMode.ACTIVE
                AppSpeak.say("Hadir, bos.")
                return
            }

            input.contains("bung smart off") -> {
                AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
                AppSpeak.say("Yaudah, gue minggir dulu.")
                return
            }
        }

        // TANPA KEYWORD = DIANGGAP ISI / OBROLAN
        if (NavigationStateManager.getState()
            == NavigationStateManager.State.WAITING_USER_RESPONSE
        ) {
            WaReplyManager.sendDirect(input)
            AppSpeak.say("Udah gue kirim.")
            return
        }

        // NGOBROL BEBAS
        activeTopic = inferTopic(input)

        val response =
            GroqAiEngine.chat(
                input,
                activeTopic,
                AppState.emotion
            )

        AppSpeak.say(response)
    }

    private fun inferTopic(text: String): String {
        return when {
            text.contains("cewek") || text.contains("dia") -> "relationship"
            text.contains("capek") || text.contains("stress") -> "emotion"
            text.contains("game") -> "game"
            else -> "random"
        }
    }
}