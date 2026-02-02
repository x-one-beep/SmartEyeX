package com.smarteyex.core

import java.util.concurrent.ConcurrentHashMap

object AudioMemory {

    private val voiceMap = ConcurrentHashMap<String, String>()

    fun rememberVoice(signature: String, name: String) {
        if (!voiceMap.containsKey(signature)) {
            voiceMap[signature] = name
        }
    }

    fun identifyVoice(signature: String): String? {
        return voiceMap[signature]
    }
}