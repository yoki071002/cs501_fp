package com.example.cs501_fp.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.repository.ItunesRepository
import com.example.cs501_fp.data.repository.TicketmasterRepository
import com.example.cs501_fp.ui.pages.home.ShowSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel : ViewModel() {
    private val ticketRepo = TicketmasterRepository()
    private val itunesRepo = ItunesRepository()
    private var mediaPlayer: MediaPlayer? = null

    private val _dailyPick = MutableStateFlow<ShowSummary?>(null)
    val dailyPick: StateFlow<ShowSummary?> = _dailyPick

    private val _showsThisWeek = MutableStateFlow<Map<LocalDate, List<ShowSummary>>>(emptyMap())
    val showsThisWeek: StateFlow<Map<LocalDate, List<ShowSummary>>> = _showsThisWeek

    private val startOfCurrentActualWeek: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    private val _isPrevWeekEnabled = MutableStateFlow(false)
    val isPrevWeekEnabled: StateFlow<Boolean> = _isPrevWeekEnabled

    private val _currentWeekStart = MutableStateFlow(startOfCurrentActualWeek)
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart

    init {
        loadData()
    }

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
        loadShowsThisWeek()
    }

    fun prevWeek() {
        if (_isPrevWeekEnabled.value) {
            _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
            loadShowsThisWeek()
        }
    }

    fun playPreview(context: Context, id: String) {
        val url = _dailyPick.value?.id

        if (url.isNullOrEmpty() || !url.startsWith("http")) {
            android.widget.Toast.makeText(context, "No preview audio available", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        try {
            stopPreview()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    android.widget.Toast.makeText(context, "Playing preview...", android.widget.Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    android.widget.Toast.makeText(context, "Error playing audio", android.widget.Toast.LENGTH_SHORT).show()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Playback failed", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun stopPreview() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }

    fun loadData() {
        loadDailyPick()
        loadShowsThisWeek()
    }

    private fun updatePrevWeekButtonState() {
        _isPrevWeekEnabled.value = _currentWeekStart.value.isAfter(startOfCurrentActualWeek)
    }

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

    private fun loadShowsThisWeek() {
        viewModelScope.launch {
            updatePrevWeekButtonState()

            val startOfWeekDate = _currentWeekStart.value.with(DayOfWeek.MONDAY)
            val endOfWeekDate = startOfWeekDate.plusDays(6)

            val startOfWeekWithTime = startOfWeekDate.atStartOfDay().atZone(ZoneId.systemDefault())
            val endOfWeekWithTime = endOfWeekDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())

            val events = ticketRepo.getEvents(startOfWeekWithTime, endOfWeekWithTime)

            _showsThisWeek.value = events
                .mapNotNull { event ->
                    val dateStr = event.dates?.start?.localDate
                    if (dateStr != null) {
                        ShowSummary(
                            id = event.id ?: "",
                            title = event.name ?: "Untitled",
                            venue = event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre",
                            dateTime = LocalDate.parse(dateStr),
                            priceFrom = 100,
                            imageUrl = event.images?.firstOrNull()?.url
                        )
                    } else {
                        null
                    }
                }
                .groupBy { it.dateTime }
        }
    }
}
