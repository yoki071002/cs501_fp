package com.example.cs501_fp.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.EventDao
import com.example.cs501_fp.data.model.UserEvent
import com.example.cs501_fp.data.repository.EventManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * CalendarViewModel
 * -------------------------------------------------
 * Handles all calendar-related operations.
 * Connects UI (Compose) with repository (EventManager).
 */
class CalendarViewModel(
    private val dao: EventDao,
    private val userId: String  // ✅ 当前登录用户 UID
) : ViewModel() {

    private val manager = EventManager(dao)

    /**
     * ✅ 添加事件（含提醒与云同步）
     */
    fun addEvent(event: UserEvent, context: Context) {
        viewModelScope.launch {
            manager.addEventWithReminder(
                event.copy(userId = userId),  // 🔹 确保事件包含当前用户ID
                context
            )
        }
    }

    /**
     * ✅ 更新事件
     */
    fun updateEvent(event: UserEvent) {
        viewModelScope.launch {
            manager.updateEvent(event.copy(userId = userId))
        }
    }

    /**
     * ✅ 删除事件
     */
    fun deleteEvent(event: UserEvent) {
        viewModelScope.launch {
            manager.deleteEvent(event)
        }
    }

    /**
     * ✅ 获取所有事件（本地 Flow）
     */
    fun getAllEvents(): Flow<List<UserEvent>> {
        return manager.getAllEventsForUser(userId)
    }

    /**
     * ✅ 按日期获取事件（本地 Flow）
     */
    fun getEventsByDate(date: String): Flow<List<UserEvent>> {
        return manager.getEventsByDateForUser(userId, date)
    }

    /**
     * ✅ 从云端同步到本地
     */
    fun syncFromCloud() {
        viewModelScope.launch {
            manager.syncFromCloud(userId)
        }
    }
}