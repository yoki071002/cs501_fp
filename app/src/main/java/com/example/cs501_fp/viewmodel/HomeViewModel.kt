package com.example.cs501_fp.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
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
    private var currentPlayingUrl: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

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

    fun togglePlayPreview(context: Context, url: String) {
        if (url.isEmpty()) return

        if (currentPlayingUrl == url && mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                _isPlaying.value = false
                Toast.makeText(context, "Paused ⏸", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer!!.start()
                _isPlaying.value = true
                Toast.makeText(context, "Resumed ▶", Toast.LENGTH_SHORT).show()
            }
            return
        }

        startNewPreview(context, url)
    }

    private fun startNewPreview(context: Context, url: String) {
        try {
            stopPreview()
            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    currentPlayingUrl = url
                    Toast.makeText(context, "Playing 🎵", Toast.LENGTH_SHORT).show()
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                }

                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun stopPreview() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            currentPlayingUrl = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            try {
                val response = itunesRepo.getMusicalSongs("broadway musical")
                val track = response.results
                    .filter { !it.previewUrl.isNullOrEmpty() && !it.artworkUrl100.isNullOrEmpty() }
                    .randomOrNull()

                if (track == null) {
                    _dailyPick.value = null
                    return@launch
                }

                val highResImage = track.artworkUrl100?.replace("100x100", "600x600")

                _dailyPick.value = ShowSummary(
                    id = track.previewUrl ?: "",
                    title = track.trackName ?: "Unknown Song",
                    venue = track.artistName ?: "Unknown Artist",
                    dateTime = LocalDate.now(),
                    priceFrom = 0,
                    imageUrl = highResImage
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _dailyPick.value = null
            }
        }
    }

    private fun loadShowsThisWeek() {
        viewModelScope.launch {
            updatePrevWeekButtonState()
            val startOfWeekDate = _currentWeekStart.value.with(DayOfWeek.MONDAY)
            val endOfWeekDate = startOfWeekDate.plusDays(6)
            val startOfWeekWithTime = startOfWeekDate.atStartOfDay().atZone(ZoneId.systemDefault())
            val endOfWeekWithTime = endOfWeekDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault())

            val rawEvents = ticketRepo.getEvents(startOfWeekWithTime, endOfWeekWithTime)

            val mappedCandidates = rawEvents.mapNotNull { event ->
                val dateStr = event.dates?.start?.localDate
                val timeStr = event.dates?.start?.localTime ?: "00:00"

                if (dateStr != null) {
                    val summary = ShowSummary(
                        id = event.id ?: "",
                        title = event.name ?: "Untitled",
                        venue = event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre",
                        dateTime = LocalDate.parse(dateStr),
                        priceFrom = 100,
                        imageUrl = event.images?.firstOrNull()?.url
                    )
                    Triple(summary, event.name ?: "", timeStr)
                } else {
                    null
                }
            }

            val distinctEvents = mappedCandidates.distinctBy { (summary, rawTitle, timeStr) ->
                val normalizedTitle = rawTitle
                    .replace(Regex("\\(.*?\\)"), "")
                    .replace(Regex(" - .*"), "")
                    .trim()
                    .lowercase()
                "$normalizedTitle|${summary.dateTime}|$timeStr"
            }

            _showsThisWeek.value = distinctEvents
                .map { it.first }
                .groupBy { it.dateTime }
        }
    }

}
