package com.example.cs501_fp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.model.UserProfile
import com.example.cs501_fp.data.repository.UserRepository
import com.example.cs501_fp.data.repository.FirestoreRepository
import com.example.cs501_fp.data.repository.LocalRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.repository.TicketmasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class CalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    /* ---------------------------------------------------------
     *                   INIT DATABASE & REPOSITORY
     * --------------------------------------------------------- */
    private val database = AppDatabase.getInstance(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val localRepo = LocalRepository(
        userEventDao = database.userEventDao(),
        experienceDao = database.experienceDao()
    )

    private val cloudRepo = FirestoreRepository()
    private val ticketRepo = TicketmasterRepository()
    private val userRepo = UserRepository()

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
     *                   USER PROFILE STATE
     * --------------------------------------------------------- */
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile

    init {
        fetchUserProfile()
    }

    suspend fun uploadImageToCloud(eventId: String, uri: Uri): String? {
        return cloudRepo.uploadEventImage(eventId, uri)
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _currentUserProfile.value = userRepo.getUserProfile()
        }
    }

    /* ---------------------------------------------------------
     *                   FLOW: All Events
     * --------------------------------------------------------- */
    val events: StateFlow<List<UserEvent>> = localRepo.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalSpent: StateFlow<Double?> = database.userEventDao().getTotalSpent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    /* ---------------------------------------------------------
     *                   ADD / UPDATE / DELETE
     * --------------------------------------------------------- */
    fun addEvent(event: UserEvent) {
        viewModelScope.launch {
            localRepo.addEvent(event)
            FirebaseAuth.getInstance().currentUser?.let {
                cloudRepo.uploadEvent(event)
            }
        }
    }

    fun updateEvent(event: UserEvent) {
        viewModelScope.launch {
            localRepo.addEvent(event)
            FirebaseAuth.getInstance().currentUser?.let {
                cloudRepo.uploadEvent(event)
            }
        }
    }

    fun deleteEvent(event: UserEvent) {
        viewModelScope.launch {
            localRepo.deleteEvent(event)
            FirebaseAuth.getInstance().currentUser?.let {
                try {
                    cloudRepo.deleteEvent(event.id)
                } catch (e: Exception) {
                    Log.e("CalendarVM", "Cloud delete failed", e)
                }
            }
        }
    }

    /* ---------------------------------------------------------
     *              SYNC CLOUD → LOCAL
     * --------------------------------------------------------- */
    fun syncFromCloud() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            _isLoading.value = true
            try {
                val cloudEvents = cloudRepo.getAllEvents()
                if (cloudEvents.isNotEmpty()) {
                    localRepo.deleteAllEvents()
                    cloudEvents.forEach { localRepo.addEvent(it) }
                }
            } catch (e: Exception) {
                Log.e("Sync", "Error during sync", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ---------------------------------------------------------
     *                   SOCIAL HEADCOUNTS
     * --------------------------------------------------------- */
    private val _headcounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val headcounts: StateFlow<Map<String, Long>> = _headcounts

    fun fetchUpcomingHeadcounts(events: List<UserEvent>) {
        if (events.isEmpty()) return

        viewModelScope.launch {
            val newCounts = withContext(Dispatchers.IO) {
                val deferredCounts = events.map { event ->
                    async {
                        val tmId = event.ticketmasterId
                        if (!tmId.isNullOrBlank()) {
                            val count = cloudRepo.getHeadcount(tmId, event.dateText)
                            if (count > 1) event.id to (count - 1) else null
                        } else {
                            null
                        }
                    }
                }
                deferredCounts.awaitAll().filterNotNull().toMap()
            }
            _headcounts.value = newCounts
        }
    }
}
