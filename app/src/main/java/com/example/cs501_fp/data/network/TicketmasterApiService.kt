// File: app/src/main/java/com/example/cs501_fp/data/network/TicketmasterApiService.kt
// Retrofit interface for fetching event data from the Ticketmaster Discovery API.

package com.example.cs501_fp.data.network

import com.example.cs501_fp.data.model.TicketmasterResponse
import com.example.cs501_fp.data.model.TicketmasterEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketmasterApiService {
    @GET("events.json")
    suspend fun getEvents(
        @Query("apikey") apiKey: String,
        @Query("city") city: String,
        @Query("startDateTime") startDateTime: String,
        @Query("endDateTime") endDateTime: String,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("classificationName") classificationName: String?,
        @Query("sort") sort: String?
    ): TicketmasterResponse

    @GET("events/{id}.json")
    suspend fun getEventById(
        @Path("id") eventId: String,
        @Query("apikey") apiKey: String
    ): TicketmasterEvent
}