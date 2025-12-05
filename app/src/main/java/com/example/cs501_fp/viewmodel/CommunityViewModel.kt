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

class CommunityViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String get() = auth.currentUser?.uid ?: ""

    // 搜索词
    private val _searchQuery = MutableStateFlow("")

    val publicPosts: StateFlow<List<UserEvent>> = combine(
        repo.getPublicEventsFlow(),
        _searchQuery
    ) { events, query ->
        if (query.isBlank()) {
            events
        } else {
            events.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.venue.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchPosts(query: String) {
        _searchQuery.value = query
    }

    fun toggleLike(event: UserEvent) {
        viewModelScope.launch {
            repo.toggleLike(event.id, currentUserId)
        }
    }

    val currentUserName: String get() = auth.currentUser?.email?.substringBefore("@") ?: "User"

    fun sendComment(eventId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val comment = Comment(
                eventId = eventId,
                userId = currentUserId,
                username = currentUserName,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repo.addComment(comment)
        }
    }

    fun getComments(eventId: String): Flow<List<Comment>> {
        return repo.getCommentsFlow(eventId)
    }
}
