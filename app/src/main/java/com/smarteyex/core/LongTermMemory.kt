package com.smarteyex.memory

data class LifeEvent(
    val summary: String,
    val people: List<String>,
    val date: String?,
    val importance: Int
)

object LongTermMemory {

    private val events = mutableListOf<LifeEvent>()

    fun store(event: LifeEvent) {
        events.add(event)
    }

    fun getAll(): List<LifeEvent> = events
}