// File: app/src/main/java/com/example/cs501_fp/data/local/dao/UserEventDao.kt
// Data Access Object (DAO) for the UserEvent entity, providing CRUD operations on the local Room database

package com.example.cs501_fp.data.local.dao

import androidx.room.*
import com.example.cs501_fp.data.local.entity.UserEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEventDao {

    @Query("SELECT * FROM user_events ORDER BY dateText ASC")
    fun getAllEvents(): Flow<List<UserEvent>>

    @Query("SELECT SUM(price) FROM user_events")
    fun getTotalSpent(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: UserEvent)

    @Delete
    suspend fun deleteEvent(event: UserEvent)

    @Query("DELETE FROM user_events")
    suspend fun deleteAllEvents()
}

