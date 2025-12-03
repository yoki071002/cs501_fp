package com.example.cs501_fp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    private val _publicPosts = MutableStateFlow<List<UserEvent>>(emptyList())
    val publicPosts: StateFlow<List<UserEvent>> = _publicPosts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var allPostsCache = emptyList<UserEvent>()

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            repo.getPublicEventsFlow().collect { events ->
                allPostsCache = events
                _publicPosts.value = events
                _isLoading.value = false
            }
        }
    }

    fun searchPosts(query: String) {
        if (query.isBlank()) {
            _publicPosts.value = allPostsCache
        } else {
            _publicPosts.value = allPostsCache.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.venue.contains(query, ignoreCase = true)
            }
        }
    }
}
