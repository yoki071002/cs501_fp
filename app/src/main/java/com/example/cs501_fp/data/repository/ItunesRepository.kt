package com.example.cs501_fp.data.repository

import android.util.Log
import com.example.cs501_fp.data.model.ItunesResponse
import com.example.cs501_fp.data.network.ItunesApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ItunesRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ItunesApiService::class.java)

    suspend fun getMusicalSongs(term: String): ItunesResponse {
        return try {
            api.searchSongs(term)
        } catch (e: Exception) {
            Log.e("ItunesRepository", "Error fetching songs", e)
            ItunesResponse(emptyList())
        }
    }
}
