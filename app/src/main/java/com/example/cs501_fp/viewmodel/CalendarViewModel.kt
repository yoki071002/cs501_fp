// File: app/src/main/java/com/example/cs501_fp/viewmodel/CalendarViewModel.kt
// The central ViewModel managing the User's Ticket Wallet, Cloud Sync, and Event Searching

package com.example.cs501_fp.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.model.UserProfile
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.repository.UserRepository
import com.example.cs501_fp.data.repository.FirestoreRepository
import com.example.cs501_fp.data.repository.LocalRepository
import com.example.cs501_fp.data.repository.TicketmasterRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    // --- Dependencies & Initialization ---
    private val database = AppDatabase.getInstance(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val localRepo = LocalRepository(
        userEventDao = database.userEventDao()
    )

    private val cloudRepo = FirestoreRepository()
    private val ticketRepo = TicketmasterRepository()
    private val userRepo = UserRepository()


    // --- Search Results ---
    private val _searchResults = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val searchResults: StateFlow<List<TicketmasterEvent>> = _searchResults

    fun searchEvents(query: String) {
        viewModelScope.launch {
            if (query.length > 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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


    // --- User Profile ---
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


    // --- Flow ---
    @RequiresApi(Build.VERSION_CODES.O)
    val events: StateFlow<List<UserEvent>> = localRepo.getAllEvents()
        .onEach { list ->
            val upcoming = list.filter {
                try {
                    LocalDate.parse(it.dateText) >= LocalDate.now()
                } catch (e: Exception) { false }
            }.take(6)

            if (upcoming.isNotEmpty()) {
                fetchUpcomingHeadcounts(upcoming)
            }
        }
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


    // --- CRUD Operations ---
    fun addEvent(event: UserEvent) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            var eventToSave = event

            if (currentUser != null) {
                val profile = userRepo.getUserProfile()

                eventToSave = event.copy(
                    ownerId = currentUser.uid,
                    ownerName = profile?.username?.ifBlank { currentUser.email?.substringBefore("@") } ?: "User",
                    ownerAvatarUrl = profile?.avatarUrl
                )
            }

            localRepo.addEvent(eventToSave)
            if (currentUser != null) {
                cloudRepo.uploadEvent(eventToSave)
            }
        }
    }

    fun updateEvent(event: UserEvent) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            var eventToSave = event

            if (currentUser != null && event.ownerId == currentUser.uid) {
                val profile = userRepo.getUserProfile()
                eventToSave = event.copy(
                    ownerName = profile?.username ?: event.ownerName,
                    ownerAvatarUrl = profile?.avatarUrl ?: event.ownerAvatarUrl
                )
            }

            localRepo.addEvent(eventToSave)

            if (currentUser != null) {
                cloudRepo.uploadEvent(eventToSave)
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


    // --- Sync ---
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


    // --- Social Headcounts ---
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
