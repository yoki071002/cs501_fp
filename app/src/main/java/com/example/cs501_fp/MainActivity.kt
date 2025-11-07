package com.example.cs501_fp

import CalendarScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.room.Room
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.ui.calendar.CalendarViewModel
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.cs501_fp.ui.calendar.EventReminderManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reminderManager = EventReminderManager(this)
        reminderManager.requestNotificationPermission()
        reminderManager.createNotificationChannel()

        val db = Room.inMemoryDatabaseBuilder(
            applicationContext,
            AppDatabase::class.java
        ).build()

        val dao = db.eventDao()
        val viewModel = CalendarViewModel(dao)

        setContent {
            MaterialTheme {
                CalendarScreen(viewModel)
            }
        }

    }
}