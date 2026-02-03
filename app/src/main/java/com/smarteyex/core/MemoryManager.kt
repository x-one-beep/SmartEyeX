package com.smarteyex.core.memory

import com.smarteyex.core.state.AppState

data class MemoryItem(
    val id: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1 // 1–5, 5 paling penting
)

class MemoryManager {

    private val memory = mutableListOf<MemoryItem>()

    /**
     * Simpan memory secara selektif
     */
    fun addMemory(item: MemoryItem) {
        memory.add(item)
        trimMemory()
    }

    /**
     * Ambil memory terbaru / penting
     */
    fun getRecentMemory(count: Int = 5): List<MemoryItem> {
        return memory.sortedByDescending { it.timestamp }.take(count)
    }

    /**
     * Hemat memori → simpan yang penting, buang paling lama
     */
    private fun trimMemory(maxSize: Int = 50) {
        if (memory.size > maxSize) {
            memory.sortBy { it.importance } // buang yang paling tidak penting
            while (memory.size > maxSize) {
                memory.removeAt(0)
            }
        }
    }
}