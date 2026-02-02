package com.smarteyex.core

object SpeechCommandProcessor {

    private var activeTopic: String? = null
    private var waitingWaReply = false

    /**
     * ENTRY POINT dari VoiceEngine
     */
    fun process(inputRaw: String) {
        val input = inputRaw.trim()
        if (input.isEmpty()) return

        // Bangunin AI kalo lagi capek / passive
        AppState.wakeUp()
        AppState.registerInteraction()

        // 1️⃣ MODE CHECK (sekolah / game / normal)
        if (ModeManager.detectAndApply(input)) return

        // 2️⃣ KALO LAGI NUNGGU BALAS WA
        if (waitingWaReply) {
            WaReplyManager.sendDirect(input)
            AppSpeak.say("Udah gue kirim.")
            waitingWaReply = false
            return
        }

        // 3️⃣ SYSTEM INTENT (jam, tanggal, cuaca)
        if (SystemIntentRouter.handle(input)) return

        // 4️⃣ WA INTENT (jawab / baca)
        if (WaIntentRouter.handle(input) {
                waitingWaReply = it
            }
        ) return

        // 5️⃣ OBROLAN / AI
        activeTopic = inferTopic(input)
        respondWithAi(input)
    }

    /**
     * RESPON AI (Groq)
     */
    private fun respondWithAi(text: String) {
        AppSpeak.say("Bentar ya…")

        Async.run {
            val response = GroqAiEngine.chat(
                userInput = text,
                topic = activeTopic,
                emotion = AppState.emotion
            )
            AppSpeak.say(response)
        }
    }

    /**
     * TOPIC TRACKING (RINGAN, MANUSIAWI)
     */
    private fun inferTopic(text: String): String =
        when {
            text.contains("cewek", true) ||
            text.contains("dia", true) -> "relationship"

            text.contains("capek", true) ||
            text.contains("stress", true) -> "emotion"

            text.contains("game", true) -> "game"

            text.contains("kerja", true) ||
            text.contains("kantor", true) -> "work"

            else -> "random"
        }

    /**
     * DIPANGGIL DARI WA SYSTEM
     */
    fun setWaitingWaReply(wait: Boolean) {
        waitingWaReply = wait
    }
}