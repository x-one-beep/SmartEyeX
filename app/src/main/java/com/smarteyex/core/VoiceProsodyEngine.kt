package com.smarteyex.voice

class VoiceProsodyEngine {

    fun build(
        emotion: VoiceEmotion,
        intent: SpeechIntent
    ): ProsodyProfile {

        return when (emotion) {

            VoiceEmotion.SAD -> ProsodyProfile(
                pitch = 0.85f,
                speed = 0.8f,
                pauseMs = 600,
                warmth = 1.0f
            )

            VoiceEmotion.HAPPY -> ProsodyProfile(
                pitch = 1.1f,
                speed = 1.05f,
                pauseMs = 250,
                warmth = 0.8f
            )

            VoiceEmotion.TIRED -> ProsodyProfile(
                pitch = 0.9f,
                speed = 0.75f,
                pauseMs = 700,
                warmth = 0.9f
            )

            VoiceEmotion.ANGRY -> ProsodyProfile(
                pitch = 1.0f,
                speed = 1.15f,
                pauseMs = 200,
                warmth = 0.2f
            )

            else -> ProsodyProfile(
                pitch = 1.0f,
                speed = 1.0f,
                pauseMs = 350,
                warmth = 0.6f
            )
        }
    }
}