// File: app/src/main/java/com/example/cs501_fp/data/model/UserProfile.kt
// Data model representing a user's profile information (Bio, Avatar, Favorites) stored in Firestore.

package com.example.cs501_fp.data.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val favoriteShows: String = ""
)