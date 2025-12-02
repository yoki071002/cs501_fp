package com.example.cs501_fp.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.network.TicketmasterApiService
import com.example.cs501_fp.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val startDateTimeStr = startDate.format(formatter)
        val endDateTimeStr = endDate.format(formatter)

        return try {
            val response = api.getEvents(
                apiKey = Constants.TICKETMASTER_API_KEY,
                city = "New York",
                startDateTime = startDateTimeStr,
                endDateTime = endDateTimeStr,
                size = 200,
                keyword = "Broadway",
                classificationName = "Theatre",
                sort = "date,asc"
            )

            response._embedded?.events ?: emptyList()
        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error fetching events: ${e.message}")
            emptyList()
        }
    }

    suspend fun getEventDetails(eventId: String): TicketmasterEvent? {
        return try {
            api.getEventById(eventId = eventId, apiKey = Constants.TICKETMASTER_API_KEY)
        } catch (e: Exception) {
            Log.e("TicketmasterRepo", "Error fetching event details for ID $eventId: ${e.message}")
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun searchEventsByKeyword(keyword: String): List<TicketmasterEvent> {
        return try {
            val now = ZonedDateTime.now()
            val response = api.getEvents(
                apiKey = Constants.TICKETMASTER_API_KEY,
                city = "New York",
                startDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                endDateTime = now.plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                size = 10,
                keyword = keyword,
                classificationName = "Theatre",
                sort = "date,asc"
            )
            response._embedded?.events ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
