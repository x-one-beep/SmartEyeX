package com.smarteyex.core.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)