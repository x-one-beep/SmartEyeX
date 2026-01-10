
package com.smarteyex.core.data

import androidx.room.Dao

import androidx.room.Insert

import androidx.room.Query

@Dao

interface EventDao {

@Insert

suspend fun insert(event: Event)

만

@Query("SELECT * FROM events ORDER BY time DESC LIMIT :limit")
suspend fun getLastEvents(limit: Int): List<Event>
