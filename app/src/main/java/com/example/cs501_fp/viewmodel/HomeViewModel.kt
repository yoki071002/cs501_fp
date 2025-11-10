package com.example.cs501_fp.viewmodel

import android.os.Build
import android.media.MediaPlayer
import android.content.Context
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.repository.TicketmasterRepository
import com.example.cs501_fp.data.repository.ItunesRepository
import com.example.cs501_fp.ui.pages.home.ShowSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.LocalDate

class HomeViewModel : ViewModel() {
    private val ticketRepo = TicketmasterRepository()
    private val itunesRepo = ItunesRepository()
    private var mediaPlayer: MediaPlayer? = null

    private val _dailyPick = MutableStateFlow<ShowSummary?>(null)
    val dailyPick: StateFlow<ShowSummary?> = _dailyPick

    private val _showsThisWeek = MutableStateFlow<List<ShowSummary>>(emptyList())
    val showsThisWeek: StateFlow<List<ShowSummary>> = _showsThisWeek

    fun playPreview(context: Context, id: String) {
        val url = _dailyPick.value?.id ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPreview() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentWeekStart = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        loadShowsThisWeek()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun prevWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
        loadShowsThisWeek()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData() {
        loadDailyPick()
        loadShowsThisWeek()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDailyPick() {
        viewModelScope.launch {
            val response = itunesRepo.getMusicalSongs("broadway musical")
            val track = response.results.randomOrNull()

            if (track == null) {
                _dailyPick.value = null
                return@launch
            }

            _dailyPick.value = ShowSummary(
                id = track.previewUrl ?: "unknown-id",
                title = track.trackName ?: "Unknown Song",
                venue = track.artistName ?: "Unknown Artist",
                dateTime = LocalDate.now(),
                priceFrom = 0,
                imageUrl = track.artworkUrl100
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadShowsThisWeek() {
        viewModelScope.launch {
            val startOfWeekDate = _currentWeekStart.value.with(DayOfWeek.MONDAY)
            val endOfWeekDate = startOfWeekDate.plusDays(6)

            val startOfWeekWithTime = startOfWeekDate.atStartOfDay().atZone(ZoneId.systemDefault())
            val endOfWeekWithTime = endOfWeekDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())

            val events = ticketRepo.getEvents(startOfWeekWithTime, endOfWeekWithTime)

            _showsThisWeek.value = events.map { event ->
                ShowSummary(
                    id = event.id ?: "",
                    title = event.name ?: "Untitled",
                    venue = event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre",
                    dateTime = LocalDate.parse(event.dates?.start?.localDate),
                    priceFrom = 100,
                    imageUrl = event.images?.firstOrNull()?.url
                )
            }
        }
    }
}
