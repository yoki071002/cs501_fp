// File: app/src/main/java/com/example/cs501_fp/data/network/ItunesApiService.kt
// Retrofit interface defining the endpoint for searching songs via iTunes API.

package com.example.cs501_fp.data.network

import com.example.cs501_fp.data.model.ItunesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): ItunesResponse
}