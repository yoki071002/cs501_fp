package com.example.cs501_fp.data.repository

import android.content.Context
import com.example.cs501_fp.data.local.EventDao
import com.example.cs501_fp.data.model.UserEvent
import com.example.cs501_fp.data.remote.FirestoreService
import com.example.cs501_fp.util.EventReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

/**
 * EventManager
 * -------------------------------------------------
 * Handles CRUD operations and cloud synchronization for user events.
 * Combines Room (local) + Firestore (cloud) + Notification reminders.
 */
class EventManager(private val dao: EventDao) {

    private val remote = FirestoreService()

    /**
     * Convert date + time string to epoch milliseconds (for scheduling reminders)
     */
    private fun parseDateToMillis(date: String, time: String): Long {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val parsedDate = formatter.parse("$date $time")
            parsedDate?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            e.printStackTrace()
            System.currentTimeMillis()
        }
    }

    /**
     * ✅ Add new event + Cloud sync + Local reminder
     */
    fun addEventWithReminder(event: UserEvent, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val finalEvent = if (event.eventId.isEmpty()) {
                event.copy(eventId = UUID.randomUUID().toString())
            } else event

            // Save to local DB
            dao.insertEvent(finalEvent)

            // Upload to Firestore
            remote.uploadEvent(finalEvent.userId, finalEvent)

            // Schedule reminder
            val reminderManager = EventReminderManager(context)
            val triggerTime = parseDateToMillis(finalEvent.date, finalEvent.time)
            reminderManager.scheduleEventReminder(
                finalEvent.eventId.hashCode(), // 🔹 unique int for AlarmManager
                finalEvent.name,
                triggerTime
            )
        }
    }

    /**
     * ✅ Add event (without reminder) + Cloud sync
     */
    fun addEventOnly(event: UserEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val finalEvent = if (event.eventId.isEmpty()) {
                event.copy(eventId = UUID.randomUUID().toString())
            } else event

            dao.insertEvent(finalEvent)
            remote.uploadEvent(finalEvent.userId, finalEvent)
        }
    }

    /**
     * ✅ Update event (Local + Cloud)
     */
    fun updateEvent(event: UserEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateEvent(event)
            remote.uploadEvent(event.userId, event) // Overwrite existing Firestore doc
        }
    }

    /**
     * ✅ Delete event (Local + Cloud)
     */
    fun deleteEvent(event: UserEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteEvent(event)
            remote.deleteEvent(event.userId, event.eventId)
        }
    }

    /**
     * ✅ Sync all events from Firestore to local Room
     */
    fun syncFromCloud(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val cloudEvents = remote.fetchUserEvents(userId)
            cloudEvents.forEach { dao.insertEvent(it) }
        }
    }

    /**
     * ✅ Get all events for a user (Local Flow)
     */
    fun getAllEventsForUser(userId: String): Flow<List<UserEvent>> {
        return dao.getAllEventsForUser(userId)
    }

    /**
     * ✅ Get events by date (Local Flow)
     */
    fun getEventsByDateForUser(userId: String, date: String): Flow<List<UserEvent>> {
        return dao.getEventsByDateForUser(userId, date)
    }

    /**
     * ✅ Get a single event by ID (Local)
     */
    suspend fun getEventByIdForUser(userId: String, eventId: String): UserEvent? {
        return dao.getEventById(eventId, userId)
    }
}