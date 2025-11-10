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
            val events = ticketRepo.getEvents()
            val startOfWeek = _currentWeekStart.value
            val endOfWeek = startOfWeek.plusDays(6)

            _showsThisWeek.value = events.mapNotNull {
                val dateStr = it.dates?.start?.localDate ?: return@mapNotNull null
                val date = LocalDate.parse(dateStr)
                if (date in startOfWeek..endOfWeek) {
                    ShowSummary(
                        id = it.id ?: "",
                        title = it.name ?: "Untitled",
                        venue = it._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre",
                        dateTime = LocalDate.parse(it.dates?.start?.localDate ?: LocalDate.now().toString()),
                        priceFrom = 100,
                        imageUrl = it.images?.firstOrNull()?.url
                    )
                } else null
            }
        }
    }
}
