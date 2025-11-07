package com.example.cs501_fp.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * AuthService
 * -------------------------------------------------
 * Handles Firebase Authentication login and registration.
 */
class AuthService {

    private val auth = FirebaseAuth.getInstance()

    // ✅ 注册新用户（返回 uid）
    suspend fun register(email: String, password: String): String? {
        auth.createUserWithEmailAndPassword(email, password).await()
        return auth.currentUser?.uid
    }

    // ✅ 登录已有用户（返回 uid）
    suspend fun login(email: String, password: String): String? {
        auth.signInWithEmailAndPassword(email, password).await()
        return auth.currentUser?.uid
    }

    // ✅ 登出
    fun logout() {
        auth.signOut()
    }

    // ✅ 获取当前登录用户 UID
    fun currentUserId(): String? = auth.currentUser?.uid
}
