package com.example.cs501_fp.data.repository

import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.data.local.entity.Experience
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
}