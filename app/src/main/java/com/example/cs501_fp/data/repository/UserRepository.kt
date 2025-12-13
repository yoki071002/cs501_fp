package com.example.cs501_fp.data.repository

import android.net.Uri
import android.util.Log
import com.example.cs501_fp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUid get() = auth.currentUser?.uid

    suspend fun getUserProfile(userId: String? = null): UserProfile? {
        val targetUid = userId ?: currentUid ?: return null
        return try {
            val snapshot = db.collection("users").document(targetUid).get().await()
            snapshot.toObject(UserProfile::class.java)?.copy(uid = targetUid)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = currentUid ?: return
        db.collection("users").document(uid).set(profile).await()
    }

    suspend fun uploadAvatar(uri: Uri): String? {
        val uid = currentUid ?: return null
        val ref = storage.reference.child("avatars/$uid.jpg")
        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("UserRepo", "Avatar upload failed", e)
            null
        }
    }
}