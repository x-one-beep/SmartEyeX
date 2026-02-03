package com.smarteyex.core.memory

data class SpeakerProfile(
    val speakerId: String,
    val name: String,
    val voiceSignature: String // hash / fingerprint suara
)

class SpeakerMemory {

    private val speakers = mutableMapOf<String, SpeakerProfile>()

    fun addSpeaker(profile: SpeakerProfile) {
        speakers[profile.speakerId] = profile
    }

    fun getSpeaker(id: String): SpeakerProfile? {
        return speakers[id]
    }

    fun recognizeSpeaker(voiceSample: String): SpeakerProfile? {
        // Placeholder → nanti pakai ML / audio fingerprinting
        return speakers.values.find { it.voiceSignature == voiceSample }
    }
}