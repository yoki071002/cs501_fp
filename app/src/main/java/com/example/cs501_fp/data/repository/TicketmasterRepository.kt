// File: app/src/main/java/com/example/cs501_fp/data/repository/TicketmasterRepository.kt
// Handles network requests to the Ticketmaster Discovery API to fetch event lists and details

package com.example.cs501_fp.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.network.TicketmasterApiService
import com.example.cs501_fp.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TicketmasterRepository {
    private val api: TicketmasterApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.TICKETMASTER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(TicketmasterApiService::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getEvents(startDate: ZonedDateTime, endDate: ZonedDateTime): List<TicketmasterEvent> {
        val utcStart = startDate.withZoneSameInstant(ZoneId.of("UTC"))
        val utcEnd = endDate.withZoneSameInstant(ZoneId.of("UTC"))

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val startDateTimeStr = utcStart.format(formatter)
        val endDateTimeStr = utcEnd.format(formatter)

        val allEvents = mutableListOf<TicketmasterEvent>()
        var page = 0
        val pageSize = 100
        var keepFetching = true

        try {
            while (keepFetching) {
                val response = api.getEvents(
                    apiKey = Constants.TICKETMASTER_API_KEY,
                    city = "New York",
                    startDateTime = startDateTimeStr,
                    endDateTime = endDateTimeStr,
                    size = pageSize,
                    page = page,
                    keyword = "Broadway",
                    classificationName = "Theatre",
                    sort = "date,asc"
                )

                val events = response._embedded?.events ?: emptyList()
                allEvents.addAll(events)

                if (events.size < pageSize) {
                    keepFetching = false
                } else {
                    page++
                }
                if (page >= 5) keepFetching = false
            }

            Log.d("TicketmasterRepo", "Total fetched events: ${allEvents.size}")
            return allEvents

        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error fetching events: ${e.message}", e)
            return allEvents
        }
    }

    suspend fun getEventDetails(eventId: String): TicketmasterEvent? {
        return try {
            api.getEventById(eventId = eventId, apiKey = Constants.TICKETMASTER_API_KEY)
        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error details: ${e.message}")
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun searchEventsByKeyword(keyword: String): List<TicketmasterEvent> {
        return try {
            val now = ZonedDateTime.now(ZoneId.of("UTC"))
            val response = api.getEvents(
                apiKey = Constants.TICKETMASTER_API_KEY,
                city = "New York",
                startDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                endDateTime = now.plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                size = 20,
                page = 0,
                keyword = keyword,
                classificationName = "Theatre",
                sort = "date,asc"
            )
            response._embedded?.events ?: emptyList()
        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error searching: ${e.message}")
            emptyList()
        }
    }
}
