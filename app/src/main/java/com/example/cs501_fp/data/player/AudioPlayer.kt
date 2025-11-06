package com.example.cs501_fp.data.player

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore
import android.util.Log

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(url: String) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Playback error: $what / $extra")
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio", e)
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }
}