package com.example.cs501_fp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.UserDao
import com.example.cs501_fp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    private val authRepo = AuthRepository(userDao)
    var currentUserId: String? = null
        private set

    fun login(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val uid = authRepo.loginUser(email, password)
            if (uid != null) {
                currentUserId = uid
                onSuccess(uid)
            } else {
                onError("Login failed. Please check credentials.")
            }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val uid = authRepo.registerUser(username, email, password)
            if (uid != null) {
                currentUserId = uid
                onSuccess(uid)
            } else {
                onError("Registration failed. Try again.")
            }
        }
    }
}
