package com.example.cs501_fp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * UserEvent
 * -------------------------------------------------
 * Represents a calendar event linked to a specific user.
 * Supports both local Room storage and Firestore synchronization.
 */
@Entity(
    tableName = "user_event",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],     // ✅ 对应 User.kt 中的 userId:String
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEvent(
    @PrimaryKey(autoGenerate = false)
    val eventId: String = "",             // ✅ 全局唯一（UUID），同步用

    val userId: String,                   // ✅ 与 Firebase UID 对齐
    val name: String,
    val venue: String,
    val date: String,
    val time: String,
    val price: Double? = null,
    val note: String? = null
)