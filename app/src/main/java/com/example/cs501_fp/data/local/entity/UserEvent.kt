package com.example.cs501_fp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_events")
data class UserEvent(

    @PrimaryKey
    val id: String = "",   // Firestore documentId 或 UUID

    val title: String = "",
    val venue: String = "",
    val dateText: String = "",   // 2025-11-25
    val timeText: String = "",      // "7:00 PM"
    val seat: String = "",          // not nullable
    val price: Double = 0.0,

    val officialImageUrl: String? = null,
    val userImageUris: List<String> = emptyList(),
    val publicImageUrls: List<String> = emptyList(),
    val notes: String = "",
    val ticketmasterId: String? = null,

    @get:PropertyName("public")
    @set:PropertyName("public")
    var isPublic: Boolean = false,

    val likedBy: List<String> = emptyList(),

    val ownerId: String = "",
    val ownerName: String = "",
    val ownerAvatarUrl: String? = null
)