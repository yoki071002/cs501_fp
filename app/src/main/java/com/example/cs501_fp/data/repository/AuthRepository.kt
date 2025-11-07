package com.example.cs501_fp.data.repository

import com.example.cs501_fp.data.local.UserDao
import com.example.cs501_fp.data.model.User
import com.example.cs501_fp.data.remote.AuthService
import com.example.cs501_fp.data.remote.FirestoreService

/**
 * AuthRepository
 * -------------------------------------------------
 * Middle layer between UI and FirebaseAuth/Firestore.
 */
class AuthRepository(private val userDao: UserDao) {

    private val authService = AuthService()
    private val remote = FirestoreService()

    // ✅ 注册并保存用户到 Firestore + 本地
    suspend fun registerUser(username: String, email: String, password: String): String? {
        val uid = authService.register(email, password) ?: return null
        val user = User(userId = uid, username = username, email = email)
        remote.registerUser(user)
        userDao.insertUser(user)
        return uid
    }

    // ✅ 登录用户（返回 uid）
    suspend fun loginUser(email: String, password: String): String? {
        val uid = authService.login(email, password)
        return uid
    }

    // ✅ 登出
    fun logoutUser() {
        authService.logout()
    }

    // ✅ 获取当前用户 UID
    fun currentUserId(): String? = authService.currentUserId()
}
