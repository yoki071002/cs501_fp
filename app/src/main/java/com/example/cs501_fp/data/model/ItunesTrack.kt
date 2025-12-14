// File: app/src/main/java/com/example/cs501_fp/data/model/ItunesTrack.kt
// Data models used to parse JSON responses from the iTunes Search API (for musical song previews).

package com.example.cs501_fp.data.model

data class ItunesResponse(
    val results: List<ItunesTrack>
)

data class ItunesTrack(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val previewUrl: String? = null,
    val artworkUrl100: String? = null
)