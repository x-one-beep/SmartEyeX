package com.smarteyex.memory

data class ShortMemory(
    val topic: String,
    val intent: String,
    val emotion: String,
    val timestamp: Long
)

object ShortMemoryStore {

    private val memories = mutableListOf<ShortMemory>()

    fun add(topic: String, intent: String, emotion: String) {
        memories.add(
            ShortMemory(topic, intent, emotion, System.currentTimeMillis())
        )

        if (memories.size > 10) memories.removeAt(0)
    }

    fun recent(): List<ShortMemory> = memories
}