package com.example.cs501_fp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.repository.FirestoreRepository
import com.example.cs501_fp.data.repository.LocalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    /* ---------------------------------------------------------
     *                   INIT DATABASE & REPOSITORY
     * --------------------------------------------------------- */
    private val database = AppDatabase.getInstance(application)

    private val localRepo = LocalRepository(
        userEventDao = database.userEventDao(),
        experienceDao = database.experienceDao()
    )

    private val cloudRepo = FirestoreRepository()

    /* ---------------------------------------------------------
     *                   FLOW: All Events
     * --------------------------------------------------------- */
    val events = localRepo.getAllEvents()
    val totalSpent = database.userEventDao().getTotalSpent()

    /* ---------------------------------------------------------
     *                   ADD EVENT (local + cloud)
     * --------------------------------------------------------- */
    fun addEvent(event: UserEvent) {
        viewModelScope.launch {
            // 1. Local DB
            localRepo.addEvent(event)

            // 2. Upload to Firestore (if user logged in)
            FirebaseAuth.getInstance().currentUser?.let {
                cloudRepo.uploadEvent(event)
            }
        }
    }

    /* ---------------------------------------------------------
     *                   DELETE EVENT (local + cloud)
     * --------------------------------------------------------- */
    fun deleteEvent(event: UserEvent) {
        viewModelScope.launch {
            // 1. Local
            localRepo.deleteEvent(event)

            // 2. Cloud
            FirebaseAuth.getInstance().currentUser?.let {
                cloudRepo.deleteEvent(event.id)
            }
        }
    }

    /* ---------------------------------------------------------
     *              SYNC CLOUD → LOCAL (optional)
     * --------------------------------------------------------- */
    fun syncFromCloud() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch

            // 获取 Firestore 所有事件
            val cloudEvents = cloudRepo.getAllEvents()

            // 覆盖本地（先删再加，或者你也可以更 smart diff）
            cloudEvents.forEach { e ->
                localRepo.addEvent(e)
            }
        }
    }
}