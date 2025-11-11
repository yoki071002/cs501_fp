package com.example.cs501_fp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.model.ItunesTrack
import com.example.cs501_fp.data.repository.ItunesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ItunesViewModel : ViewModel() {
    private val repo = ItunesRepository()
    private val _randomTrack = MutableStateFlow<ItunesTrack?>(null)
    val randomTrack: StateFlow<ItunesTrack?> = _randomTrack

    fun fetchRandomSong() {
        viewModelScope.launch {
            try {
                val response = repo.getMusicalSongs("musical")
                if (response.results.isNotEmpty()) {
                    val random = response.results[Random.nextInt(response.results.size)]
                    _randomTrack.value = random
                    Log.d("ItunesVM", "Picked random: ${random.trackName} by ${random.artistName}")
                } else {
                    Log.w("ItunesVM", "No songs found")
                }
            } catch (e: Exception) {
                Log.e("ItunesVM", "Error fetching song", e)
            }
        }
    }
}