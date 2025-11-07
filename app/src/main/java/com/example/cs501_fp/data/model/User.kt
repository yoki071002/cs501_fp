package com.example.cs501_fp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User
 * -------------------------------------------------
 * Represents a user both locally (Room) and remotely (Firestore).
 * userId matches Firebase UID for cross-device sync.
 */
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = false)
    val userId: String = "",        // ✅ 与 Firebase UID 对应，全局唯一

    val username: String = "",      // 本地显示名
    val email: String? = null,      // 登录邮箱
    val password: String? = null    // 仅本地存储，上传前应置空
)
