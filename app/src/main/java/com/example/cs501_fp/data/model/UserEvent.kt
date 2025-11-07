package com.example.cs501_fp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * UserEvent
 * -------------------------------------------------
 * Represents a performance the user plans to attend.
 */
@Entity(tableName = "user_event")
data class UserEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val venue: String,
    val date: String,
    val time: String,
    val price: Double? = null,
    val note: String? = null
)
