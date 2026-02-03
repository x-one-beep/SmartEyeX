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
        // Simpel retention: hanya simpan 100 memory terbaru
        if (memoryList.size > 100) memoryList.removeAt(0)
    }

    fun getRecentMemory(count: Int = 10): List<MemoryItem> {
        return memoryList.takeLast(count)
    }

    fun clearMemory() {
        memoryList.clear()
    }
}