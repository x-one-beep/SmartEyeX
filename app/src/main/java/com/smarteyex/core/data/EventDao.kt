package com.smarteyex.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: Event)

    @Query(
        "SELECT * FROM events " +
        "ORDER BY time DESC " +
        "LIMIT :limit"
    )
    fun getLastEvents(limit: Int): List<Event>
}
