package com.example.cs501_fp.data.local

import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cs501_fp.data.model.UserEvent

/**
 * AppDatabase
 * -------------------------------------------------
 * The Room database that holds user-added events.
 * Each table represents a user entity such as events or tickets.
 */

@Database(
    entities = [UserEvent::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "broadway_calendar_db"
    }
}