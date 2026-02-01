package com.smarteyex.core

class WaReplyManager {

    // Fungsi untuk manage auto reply WA
    fun autoReply(message: String) {
        // Panggil AI untuk generate reply berdasarkan konteks
        GroqAiEngine().chatWithAI("Generate reply for WA: $message") { aiReply ->
            WaSender().sendMessage(aiReply)
        }
    }

    // Fungsi untuk reply random
    fun randomReply() {
        GroqAiEngine().generateRandomResponse { response ->
            WaSender().sendMessage(response)
        }
    }
}