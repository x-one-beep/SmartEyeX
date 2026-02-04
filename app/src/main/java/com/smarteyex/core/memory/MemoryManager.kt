package com.smarteyex.core.memory

data class MemoryItem(
    val id: String,
    val text: String,
    val importance: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryManager {

    private val memoryList = mutableListOf<MemoryItem>()

    fun addMemory(item: MemoryItem) {
        memoryList.add(item)
        if (memoryList.size > 200) memoryList.removeAt(0) // max memory cap
    }

    fun getRecentMemory(limit: Int = 10): List<MemoryItem> {
        return memoryList.takeLast(limit).reversed()
    }

    fun clearMemory() {
        memoryList.clear()
    }
}
