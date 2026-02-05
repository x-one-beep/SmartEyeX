package com.smarteyex.voice

import com.smarteyex.notification.*

class VoiceReplyController {

    fun onUserVoiceAnswer(answer: String) {

        val notif = PendingReplyCache.get() ?: return

        SmartVoiceEngine().speak(
            text = "oke, gue kirimin ya",
            emotion = VoiceEmotion.HAPPY,
            intent = SpeechIntent.RESPOND
        )

        WhatsAppReplySender.sendReply(notif, answer)
    }
}