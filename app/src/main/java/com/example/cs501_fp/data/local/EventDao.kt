package com.example.cs501_fp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cs501_fp.data.model.UserEvent
import kotlinx.coroutines.flow.Flow

/**
 * EventDao
 * -------------------------------------------------
 * Defines CRUD operations for user events stored locally.
 */

@Dao
interface EventDao {

    // Insert a new event
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEvent(event: UserEvent)

    // Delete a specific event
    @Delete
    suspend fun deleteEvent(event: UserEvent)

    // Retrieve all events as a Flow
    @Query("SELECT * FROM user_event ORDER BY date ASC")
    fun getAllEvents(): Flow<List<UserEvent>>

    // Retrieve events for a specific date
    @Query("SELECT * FROM user_event WHERE date = :date")
    fun getEventsByDate(date: String): Flow<List<UserEvent>>

    @Update
    suspend fun updateEvent(event: UserEvent)

    @Query("SELECT * FROM user_event WHERE id = :id")
    suspend fun getEventById(id: Int): UserEvent?
}