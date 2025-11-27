package com.example.cs501_fp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cs501_fp.data.local.dao.UserEventDao
import com.example.cs501_fp.data.local.dao.ExperienceDao
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.local.entity.Experience

@Database(
    entities = [
        UserEvent::class,
        Experience::class
    ],
    version = 3,           // ← 推荐升级版本号（你改字段结构了）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userEventDao(): UserEventDao
    abstract fun experienceDao(): ExperienceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app.db"
                )
                    .fallbackToDestructiveMigration()   // 结构变动时自动重建 DB（避免 crash）
                    .build()
                    .also { INSTANCE = it }
            }
    }
}