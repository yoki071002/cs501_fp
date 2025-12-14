// File: app/src/main/java/com/example/cs501_fp/viewmodel/CommunityViewModel.kt
// Manages the social feed, handling search, sorting, likes, and comments

package com.example.cs501_fp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.model.Comment
import com.example.cs501_fp.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

enum class SortOption {
    NEWEST,
    TRENDING,
    MINE
}

class CommunityViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String get() = auth.currentUser?.uid ?: ""
    val currentUserName: String get() = auth.currentUser?.email?.substringBefore("@") ?: "User"

    private val _searchQuery = MutableStateFlow("")

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption

    val publicPosts: StateFlow<List<UserEvent>> = combine(
        repo.getPublicEventsFlow(),
        _searchQuery,
        _sortOption
    ) { events, query, sort ->
        val filtered = if (query.isBlank()) {
            events
        } else {
            events.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.venue.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.dateText }
            SortOption.TRENDING -> filtered.sortedByDescending { it.likedBy.size }
            SortOption.MINE -> filtered.filter { it.ownerId == currentUserId }
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchPosts(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleLike(event: UserEvent) {
        viewModelScope.launch {
            repo.toggleLike(event.id, currentUserId)
        }
    }

    fun sendComment(eventId: String, postOwnerId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val user = auth.currentUser
            val comment = Comment(
                eventId = eventId,
                userId = currentUserId,
                username = currentUserName,
                avatarUrl = user?.photoUrl?.toString(),
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repo.addComment(comment, postOwnerId)
        }
    }

    fun deleteComment(comment: Comment, postOwnerId: String) {
        viewModelScope.launch {
            repo.deleteComment(
                commentId = comment.id,
                eventId = comment.eventId,
                postOwnerId = postOwnerId
            )
        }
    }

    fun getComments(eventId: String): Flow<List<Comment>> {
        return repo.getCommentsFlow(eventId)
    }
}
