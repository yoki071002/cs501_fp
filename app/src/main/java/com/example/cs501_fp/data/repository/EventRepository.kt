package com.example.cs501_fp.data.repository

import android.content.Context
import com.example.cs501_fp.data.local.EventDao
import com.example.cs501_fp.data.model.UserEvent
import kotlinx.coroutines.flow.Flow

/**
 * EventRepository
 * -------------------------------------------------
 * Wraps EventManager to provide clean, testable interface for ViewModel.
 */
class EventRepository(private val dao: EventDao) {

    private val manager = EventManager(dao)

    fun addEvent(event: UserEvent, context: Context) =
        manager.addEventWithReminder(event, context)

    fun updateEvent(event: UserEvent) =
        manager.updateEvent(event)

    fun deleteEvent(event: UserEvent) =
        manager.deleteEvent(event)

    fun syncFromCloud(userId: String) =
        manager.syncFromCloud(userId)

    fun getAllEvents(userId: String): Flow<List<UserEvent>> =
        manager.getAllEventsForUser(userId)

    fun getEventsByDate(userId: String, date: String): Flow<List<UserEvent>> =
        manager.getEventsByDateForUser(userId, date)
}