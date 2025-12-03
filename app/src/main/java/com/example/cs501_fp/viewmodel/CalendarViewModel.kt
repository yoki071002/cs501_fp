package com.example.cs501_fp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.repository.FirestoreRepository
import com.example.cs501_fp.data.repository.LocalRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.repository.TicketmasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val ticketRepo = TicketmasterRepository()

    /* ---------------------------------------------------------
     *                   SEARCH STATE
     * --------------------------------------------------------- */
    private val _searchResults = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val searchResults: StateFlow<List<TicketmasterEvent>> = _searchResults

    fun searchEvents(query: String) {
        viewModelScope.launch {
            if (query.length > 2) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    _searchResults.value = ticketRepo.searchEventsByKeyword(query)
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

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
     *                   ADD EVENT (Update photo)
     * --------------------------------------------------------- */
    fun updateEvent(event: UserEvent) {
        viewModelScope.launch {
            localRepo.addEvent(event)

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
                try {
                    cloudRepo.deleteEvent(event.id)
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Failed to delete event from cloud: ${event.id}", e)
                }
            }
        }
    }

    /* ---------------------------------------------------------
     *              SYNC CLOUD → LOCAL
     * --------------------------------------------------------- */
    fun syncFromCloud() {viewModelScope.launch {
        val user = FirebaseAuth.getInstance().currentUser ?: return@launch
        Log.d("Sync", "Starting sync from cloud for user ${user.uid}")

        val cloudEvents = cloudRepo.getAllEvents()
        Log.d("Sync", "Found ${cloudEvents.size} events in cloud.")

        if (cloudEvents.isNotEmpty()) {
            localRepo.deleteAllEvents()
            cloudEvents.forEach { event ->
                localRepo.addEvent(event)
            }
            Log.d("Sync", "Finished syncing to local database.")
        }
    }
    }


    /* ---------------------------------------------------------
     *                   SOCIAL HEADCOUNTS
     * --------------------------------------------------------- */
    private val _headcounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val headcounts: StateFlow<Map<String, Long>> = _headcounts

    fun fetchUpcomingHeadcounts(events: List<UserEvent>) {
        viewModelScope.launch {
            val newCounts = mutableMapOf<String, Long>()
            events.forEach { event ->
                val tmId = event.ticketmasterId
                if (!tmId.isNullOrBlank()) {
                    val count = cloudRepo.getHeadcount(tmId, event.dateText)
                    if (count > 1) {
                        newCounts[event.id] = count - 1
                    }
                }
            }
            _headcounts.value = newCounts
        }
    }
}