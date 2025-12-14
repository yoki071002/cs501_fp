// File: app/src/main/java/com/example/cs501_fp/data/local/entity/Experience.kt
// Defines the Experience table schema for Room Database to store personal notes/photos linked to events

package com.example.cs501_fp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiences")
data class Experience(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val eventId: Int,
    val note: String?,
    val photoUri: String?
)