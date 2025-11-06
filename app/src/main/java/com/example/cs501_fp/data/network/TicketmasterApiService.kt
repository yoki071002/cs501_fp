package com.example.cs501_fp.data.network

import com.example.cs501_fp.data.model.TicketmasterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketmasterApiService {
    @GET("events.json")
    suspend fun getEvents(
        @Query("apikey") apiKey: String,
        @Query("keyword") keyword: String = "Broadway",
        @Query("city") city: String = "New York",
        @Query("countryCode") countryCode: String = "US",
        @Query("genreId") genreId: String? = null,
        @Query("size") size: Int = 10
    ): TicketmasterResponse
}