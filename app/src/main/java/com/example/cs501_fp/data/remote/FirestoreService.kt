package com.example.cs501_fp.data.remote

import com.example.cs501_fp.data.model.User
import com.example.cs501_fp.data.model.UserEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * FirestoreService
 * -------------------------------------------------
 * Handles Firebase Auth & Firestore operations for users and events.
 */
class FirestoreService {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ✅ 用户注册：Auth + Firestore profile
    suspend fun registerUser(user: User): String? {
        auth.createUserWithEmailAndPassword(user.email!!, user.password!!).await()
        val uid = auth.currentUser?.uid ?: return null

        val userData = user.copy(
            userId = uid,
            password = null // ✅ 不存明文密码
        )

        db.collection("users").document(uid).set(userData).await()
        return uid
    }

    // ✅ 用户登录（返回 UID）
    suspend fun login(email: String, password: String): String? {
        auth.signInWithEmailAndPassword(email, password).await()
        return auth.currentUser?.uid
    }

    // ✅ 登出
    fun logout() {
        auth.signOut()
    }

    // ✅ 上传/同步事件到 Firestore
    suspend fun uploadEvent(userId: String, event: UserEvent) {
        val eventId = event.eventId ?: UUID.randomUUID().toString()
        val syncedEvent = event.copy(eventId = eventId)
        db.collection("users").document(userId)
            .collection("events")
            .document(eventId)
            .set(syncedEvent)
            .await()
    }

    // ✅ 更新事件（部分字段）
    suspend fun updateEvent(userId: String, eventId: String, updatedFields: Map<String, Any>) {
        db.collection("users").document(userId)
            .collection("events")
            .document(eventId)
            .update(updatedFields)
            .await()
    }

    // ✅ 获取用户所有事件（一次性获取）
    suspend fun fetchUserEvents(userId: String): List<UserEvent> {
        val snapshot = db.collection("users").document(userId)
            .collection("events")
            .get().await()
        return snapshot.toObjects(UserEvent::class.java)
    }

    // ✅ 删除事件
    suspend fun deleteEvent(userId: String, eventId: String) {
        db.collection("users").document(userId)
            .collection("events")
            .document(eventId)
            .delete()
            .await()
    }

    // ✅ 实时监听事件变化（可用于自动同步）
    fun observeUserEvents(userId: String, onUpdate: (List<UserEvent>) -> Unit) {
        db.collection("users").document(userId)
            .collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val events = snapshot.toObjects(UserEvent::class.java)
                    onUpdate(events)
                }
            }
    }
}