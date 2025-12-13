package com.example.cs501_fp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.model.UserProfile
import com.example.cs501_fp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repo = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun isCurrentUser(userId: String?): Boolean {
        val currentUid = auth.currentUser?.uid
        return userId == null || userId == currentUid
    }

    fun loadProfile(userId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val p = repo.getUserProfile(userId)
            if (p != null) _profile.value = p
            _isLoading.value = false
        }
    }

    fun updateProfile(username: String, bio: String, favShows: String) {
        viewModelScope.launch {
            val newProfile = _profile.value.copy(
                username = username,
                bio = bio,
                favoriteShows = favShows
            )
            repo.saveUserProfile(newProfile)
            _profile.value = newProfile
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val url = repo.uploadAvatar(uri)
            if (url != null) {
                val newProfile = _profile.value.copy(avatarUrl = url)
                repo.saveUserProfile(newProfile)
                _profile.value = newProfile
            }
            _isLoading.value = false
        }
    }
}
