// File: app/src/main/java/com/example/cs501_fp/data/local/entity/UserEvent.kt
// The core data model representing a Event. Used for both Room (Local DB) and Firestore (Cloud).

package com.example.cs501_fp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_events")
data class UserEvent(

    // --- Identity ---
    @PrimaryKey
    val id: String = "",

    // --- Basic Show Info ---
    val title: String = "",
    val venue: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val ticketmasterId: String? = null,

    // --- Ticket Details ---
    val seat: String = "",
    val price: Double = 0.0,

    // --- Media Resources ---
    val officialImageUrl: String? = null,
    val userImageUris: List<String> = emptyList(),
    val publicImageUrls: List<String> = emptyList(),

    // --- Content & Review ---
    val notes: String = "",
    val publicReview: String = "",

    // --- Community Features ---
    @get:PropertyName("public")
    @set:PropertyName("public")
    var isPublic: Boolean = false,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0,

    // --- Owner Metadata ---
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerAvatarUrl: String? = null
)