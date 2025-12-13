package com.example.cs501_fp.data.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val favoriteShows: String = ""
)