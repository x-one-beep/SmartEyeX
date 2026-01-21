package com.smarteyex.core

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val type: String,
    val data: String
)
