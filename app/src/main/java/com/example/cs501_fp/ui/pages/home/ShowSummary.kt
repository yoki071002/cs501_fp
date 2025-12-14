// File: app/src/main/java/com/example/cs501_fp/ui/pages/home/ShowSummary.kt
// A simplified UI model representing a show/event for the Home Screen.

package com.example.cs501_fp.ui.pages.home

import java.time.LocalDate

data class ShowSummary(
    val id: String,
    val title: String,
    val venue: String,
    val dateTime: LocalDate,
    val priceFrom: Int,
    val imageUrl: String? = null
)
