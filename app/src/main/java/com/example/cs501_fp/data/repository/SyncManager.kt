package com.example.cs501_fp.data.repository

import com.example.cs501_fp.data.local.EventDao
import com.example.cs501_fp.data.model.UserEvent
import com.example.cs501_fp.data.remote.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SyncManager
 * -------------------------------------------------
 * Handles bidirectional sync between Firestore and Room.
 */
class SyncManager(
    private val eventDao: EventDao,
    private val remote: FirestoreService = FirestoreService()
) {

    // ✅ 云端 → 本地同步
    suspend fun syncFromCloud(userId: String) = withContext(Dispatchers.IO) {
        val cloudEvents = remote.fetchUserEvents(userId)
        cloudEvents.forEach { eventDao.insertEvent(it) }
    }

    // ✅ 本地 → 云端同步
    suspend fun syncToCloud(userId: String) = withContext(Dispatchers.IO) {
        val localEvents = eventDao.getAllEvents()
        localEvents.forEach { remote.uploadEvent(userId, it) }
    }

    // ✅ 实时监听（自动同步）
    fun observeCloudUpdates(userId: String) {
        remote.observeUserEvents(userId) { events ->
            CoroutineScope(Dispatchers.IO).launch {
                events.forEach { event ->
                    eventDao.insertEvent(event)
                }
            }
        }
    }
}
