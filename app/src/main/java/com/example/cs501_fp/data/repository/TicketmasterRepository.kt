package com.example.cs501_fp.data.repository

import android.util.Log
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.network.TicketmasterApiService
import com.example.cs501_fp.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TicketmasterRepository {
    private val api: TicketmasterApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.TICKETMASTER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(TicketmasterApiService::class.java)
    }

    suspend fun getEvents(): List<TicketmasterEvent> {
        return try {
            val response = api.getEvents(Constants.TICKETMASTER_API_KEY)
            response._embedded?.events ?: emptyList()
        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error fetching events: ${e.message}")
            emptyList()
        }
    }
}