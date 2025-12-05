package com.example.cs501_fp.data.repository

import android.util.Log
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.local.entity.Experience
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
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

    /** 获取当前用户 DocumentReference，如果未登录返回 null */
    private fun userDoc(): DocumentReference? {
        val uid = auth.currentUser?.uid
        return uid?.let { db.collection("users").document(it) }
    }

    /* --------------------------------------------------------
     *                     EVENT CRUD
     * -------------------------------------------------------- */

    /** 上传或更新一个 Event */
    suspend fun uploadEvent(event: UserEvent) {
        val doc = userDoc() ?: return   // 未登录 → 不同步，不崩溃
        doc.collection("events")
            .document(event.id)        // event.id 是 String
            .set(event)
            .await()
    }

    /** 删除一个 Event */
    suspend fun deleteEvent(eventId: String) {
        val doc = userDoc() ?: return
        doc.collection("events")
            .document(eventId)
            .delete()
            .await()
    }

    /** 读取用户所有 Events（给同步用） */
    suspend fun getAllEvents(): List<UserEvent> {
        val doc = userDoc() ?: return emptyList()
        return doc.collection("events")
            .get()
            .await()
            .toObjects(UserEvent::class.java)
    }


    /* --------------------------------------------------------
     *                     COMMUNITY FEATURES
     * -------------------------------------------------------- */

    /** 更新公开状态 */
    suspend fun togglePublicStatus(eventId: String, isPublic: Boolean) {
        val doc = userDoc() ?: return
        doc.collection("events")
            .document(eventId)
            .update("public", isPublic)
    }

    /** 获取社区所有公开帖子 */
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

    /**
     * 查询某场演出（特定 ID + 特定日期）有多少人要去。
     */
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

    /* --------------------------------------------------------
     *                     EXPERIENCE CRUD
     * -------------------------------------------------------- */

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


    /* --------------------------------------------------------
     *                     IMAGE UPLOAD (for community)
     * -------------------------------------------------------- */
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

    /* --------------------------------------------------------
     *                     KUDOS & COMMENTS
     * -------------------------------------------------------- */
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

    suspend fun addComment(comment: Comment) {
        db.collection("comments").add(comment).await()
    }

    fun getCommentsFlow(eventId: String): kotlinx.coroutines.flow.Flow<List<Comment>> = kotlinx.coroutines.flow.callbackFlow {
        val query = db.collection("comments")
            .whereEqualTo("eventId", eventId)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(Comment::class.java))
            }
        }
        awaitClose { listener.remove() }
    }

}