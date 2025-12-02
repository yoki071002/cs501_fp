package com.example.cs501_fp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_events")
data class UserEvent(

    @PrimaryKey
    val id: String,   // Firestore documentId 或 UUID

    val title: String,
    val venue: String,
    val dateText: String,   // 2025-11-25
    val timeText: String,      // "7:00 PM"
    val seat: String,          // not nullable
    val price: Double,
    val officialImageUrl: String? = null,
    val userImageUris: List<String> = emptyList(),
    val notes: String = "",

    val ticketmasterId: String? = null,
    val isPublic: Boolean = false
)