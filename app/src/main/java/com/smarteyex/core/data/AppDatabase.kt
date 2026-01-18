package com.smarteyex.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Event::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        private const val DB_NAME = "smartey_db"
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
