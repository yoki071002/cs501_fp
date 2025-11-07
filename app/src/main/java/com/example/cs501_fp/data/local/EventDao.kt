package com.example.cs501_fp.data.local

import androidx.room.*
import com.example.cs501_fp.data.model.UserEvent
import kotlinx.coroutines.flow.Flow

/**
 * EventDao
 * -------------------------------------------------
 * Defines CRUD operations for user events stored locally.
 * Each event is associated with a specific userId (String).
 */

@Dao
interface EventDao {

    // ✅ 插入新事件（若存在则覆盖）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UserEvent)

    // ✅ 更新事件
    @Update
    suspend fun updateEvent(event: UserEvent)

    // ✅ 删除特定事件
    @Delete
    suspend fun deleteEvent(event: UserEvent)

    // ✅ 查询：某用户的所有事件（按日期排序）
    @Query("SELECT * FROM user_event WHERE userId = :userId ORDER BY date ASC")
    fun getAllEventsForUser(userId: String): Flow<List<UserEvent>>

    // ✅ 查询：某用户在指定日期的事件
    @Query("SELECT * FROM user_event WHERE userId = :userId AND date = :date")
    fun getEventsByDateForUser(userId: String, date: String): Flow<List<UserEvent>>

    // ✅ 查询：单个事件（根据 id）
    @Query("SELECT * FROM user_event WHERE eventId = :eventId AND userId = :userId LIMIT 1")
    suspend fun getEventById(eventId: String, userId: String): UserEvent?

    // ✅ 查询：所有事件（调试/同步用）
    @Query("SELECT * FROM user_event")
    suspend fun getAllEvents(): List<UserEvent>

    // ✅ 清空指定用户的事件（登出或重新同步时）
    @Query("DELETE FROM user_event WHERE userId = :userId")
    suspend fun deleteAllEventsForUser(userId: String)
}