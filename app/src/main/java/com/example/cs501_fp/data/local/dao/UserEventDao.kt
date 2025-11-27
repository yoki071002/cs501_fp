package com.example.cs501_fp.data.local.dao

import androidx.room.*
import com.example.cs501_fp.data.local.entity.UserEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEventDao {

    @Query("SELECT * FROM user_events ORDER BY dateText ASC")
    fun getAllEvents(): Flow<List<UserEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: UserEvent)

    @Delete
    suspend fun deleteEvent(event: UserEvent)
}