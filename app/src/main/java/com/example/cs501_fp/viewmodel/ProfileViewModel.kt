package com.example.cs501_fp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.model.UserProfile
import com.example.cs501_fp.data.repository.LocalRepository
import com.example.cs501_fp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val db = AppDatabase.getInstance(application)
    private val localRepo = LocalRepository(db.userEventDao(), db.experienceDao())

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
            val p = userRepo.getUserProfile(userId)
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
            userRepo.saveUserProfile(newProfile)
            _profile.value = newProfile
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val url = userRepo.uploadAvatar(uri)
            if (url != null) {
                val newProfile = _profile.value.copy(avatarUrl = url)
                userRepo.saveUserProfile(newProfile)
                _profile.value = newProfile
            }
            _isLoading.value = false
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            localRepo.deleteAllEvents()
        }
    }
}
