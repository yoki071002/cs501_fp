// File: app/src/main/java/com/example/cs501_fp/data/repository/FirestoreRepository.kt
// Handles all interactions with Firebase Firestore (Database) and Storage (Images).

package com.example.cs501_fp.data.repository

import android.util.Log
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.local.entity.Experience
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.example.cs501_fp.data.model.Comment
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun userDoc(): DocumentReference? {
        val uid = auth.currentUser?.uid
        return uid?.let { db.collection("users").document(it) }
    }


    // --- Event CRUD ---
    suspend fun uploadEvent(event: UserEvent) {
        val doc = userDoc() ?: return
        doc.collection("events")
            .document(event.id)
            .set(event)
            .await()
    }

    suspend fun deleteEvent(eventId: String) {
        val doc = userDoc() ?: return
        doc.collection("events")
            .document(eventId)
            .delete()
            .await()
    }

    suspend fun getAllEvents(): List<UserEvent> {
        val doc = userDoc() ?: return emptyList()
        return doc.collection("events")
            .get()
            .await()
            .toObjects(UserEvent::class.java)
    }


    // --- Community Features ---
    suspend fun togglePublicStatus(eventId: String, isPublic: Boolean) {
        val doc = userDoc() ?: return
        doc.collection("events")
            .document(eventId)
            .update("public", isPublic)
    }

    fun getPublicEventsFlow(): Flow<List<UserEvent>> = callbackFlow {
        val query = db.collectionGroup("events")
            .whereEqualTo("public", true)
            .orderBy("dateText", Query.Direction.DESCENDING)
            .limit(50)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val events = snapshot.toObjects(UserEvent::class.java)
                trySend(events)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getHeadcount(tmId: String, dateText: String): Long {
        if (tmId.isBlank()) return 0
        return try {
            val snapshot = db.collectionGroup("events")
                .whereEqualTo("ticketmasterId", tmId)
                .whereEqualTo("dateText", dateText)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
            snapshot.count
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error counting heads", e)
            0
        }
    }


    // --- Experience CRUD ---
    suspend fun uploadExperience(exp: Experience) {
        val doc = userDoc() ?: return
        doc.collection("experience")
            .document(exp.id.toString())
            .set(exp)
            .await()
    }

    suspend fun deleteExperience(expId: String) {
        val doc = userDoc() ?: return
        doc.collection("experience")
            .document(expId)
            .delete()
            .await()
    }

    suspend fun getAllExperience(): List<Experience> {
        val doc = userDoc() ?: return emptyList()
        return doc.collection("experience")
            .get()
            .await()
            .toObjects(Experience::class.java)
    }


    // --- Image Upload ---
    suspend fun uploadEventImage(eventId: String, uri: Uri): String? {
        val uid = auth.currentUser?.uid ?: return null
        val filename = "${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("event_images/$uid/$eventId/$filename")

        return try {
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Image upload failed", e)
            null
        }
    }


    // --- Kudos & Comments ---
    suspend fun toggleLike(eventId: String, currentUserId: String) {
        val docRef = db.collectionGroup("events").whereEqualTo("id", eventId).get().await().documents.firstOrNull()?.reference ?: return

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

            val newLikedBy = if (likedBy.contains(currentUserId)) {
                likedBy - currentUserId
            } else {
                likedBy + currentUserId
            }
            transaction.update(docRef, "likedBy", newLikedBy)
        }.await()
    }

    suspend fun addComment(comment: Comment, postOwnerId: String) {
        val newDocRef = db.collection("comments").document()
        val commentWithId = comment.copy(id = newDocRef.id)
        newDocRef.set(commentWithId).await()
        if (postOwnerId.isNotBlank()) {
            try {
                db.collection("users")
                    .document(postOwnerId)
                    .collection("events")
                    .document(comment.eventId)
                    .update("commentCount", FieldValue.increment(1))
                    .await()
            } catch (e: Exception) {
                Log.e("FirestoreRepo", "Failed to increment comment count", e)
            }
        }
    }

    suspend fun deleteComment(commentId: String, eventId: String, postOwnerId: String) {
        if (commentId.isBlank()) return
        try {
            db.collection("comments").document(commentId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to delete comment doc", e)
            return
        }

        if (postOwnerId.isNotBlank() && eventId.isNotBlank()) {
            try {
                db.collection("users")
                    .document(postOwnerId)
                    .collection("events")
                    .document(eventId)
                    .update("commentCount", FieldValue.increment(-1))
                    .await()
            } catch (e: Exception) {
                Log.e("FirestoreRepo", "Failed to decrement comment count", e)
            }
        }
    }

    fun getCommentsFlow(eventId: String): Flow<List<Comment>> = callbackFlow {
        val query = db.collection("comments")
            .whereEqualTo("eventId", eventId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreRepo", "Listen failed: $error")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val comments = snapshot.toObjects(Comment::class.java)
                val sortedComments = comments.sortedBy { it.timestamp }
                trySend(sortedComments)
            }
        }
        awaitClose { listener.remove() }
    }
}