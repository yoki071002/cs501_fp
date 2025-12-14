// File: app/src/main/java/com/example/cs501_fp/data/repository/LocalRepository.kt
// Acts as a clean API for ViewModels to access local database operations

package com.example.cs501_fp.data.repository

import com.example.cs501_fp.data.local.dao.UserEventDao
import com.example.cs501_fp.data.local.entity.UserEvent
import kotlinx.coroutines.flow.Flow

class LocalRepository(
    private val userEventDao: UserEventDao
) {
    // --- User Events ---
    fun getAllEvents(): Flow<List<UserEvent>> =
        userEventDao.getAllEvents()

    suspend fun addEvent(event: UserEvent) =
        userEventDao.addEvent(event)

    suspend fun deleteEvent(event: UserEvent) =
        userEventDao.deleteEvent(event)

    suspend fun deleteAllEvents() = userEventDao.deleteAllEvents()
}