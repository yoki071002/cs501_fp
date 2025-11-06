package com.example.cs501_fp.data.model

data class ItunesResponse(
    val results: List<ItunesTrack>
)

data class ItunesTrack(
    val trackName: String? = null,
    val artistName: String? = null,
    val previewUrl: String? = null,
    val artworkUrl100: String? = null
)