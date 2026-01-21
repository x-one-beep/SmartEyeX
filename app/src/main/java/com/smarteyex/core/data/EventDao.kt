package com.smarteyex.core

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: Event)

    @Query("SELECT * FROM Event ORDER BY time DESC LIMIT :limit")
    suspend fun getLastEvents(limit: Int): List<Event>
}
