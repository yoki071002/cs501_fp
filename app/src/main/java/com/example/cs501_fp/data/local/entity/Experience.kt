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
    val photoUri: String?     // 本地或 Firebase Storage 图片 URL
)