// File: app/src/main/java/com/example/cs501_fp/data/model/Comment.kt
// Data model representing a user comment on a Community post.

package com.example.cs501_fp.data.model

data class Comment(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)