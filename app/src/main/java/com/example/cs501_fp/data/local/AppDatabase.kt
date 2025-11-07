package com.example.cs501_fp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cs501_fp.data.model.User
import com.example.cs501_fp.data.model.UserEvent

/**
 * AppDatabase
 * -------------------------------------------------
 * Room database holding user and event data.
 * Each user has their own local database file.
 */

@Database(
    entities = [User::class, UserEvent::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Build or return an existing instance of AppDatabase.
         * Each user has their own local DB file based on userId.
         */
        fun getInstance(context: Context, userId: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "broadway_calendar_db_$userId" // ✅ user-specific database
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear instance when user logs out (so a new user can load a new DB)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
