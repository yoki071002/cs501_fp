package com.example.cs501_fp.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.EventDao
import com.example.cs501_fp.data.model.UserEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

fun parseDateToMillis(date: String, time: String): Long {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val combined = "$date $time"
        val parsedDate = formatter.parse(combined)
        parsedDate?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        e.printStackTrace()
        System.currentTimeMillis() // 出错时返回当前时间
    }
}
class CalendarViewModel(private val dao: EventDao) : ViewModel() {

    private val _events = MutableStateFlow<List<UserEvent>>(emptyList())
    val events: StateFlow<List<UserEvent>> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllEvents().collect { _events.value = it }
        }
    }

    fun addEvent(event: UserEvent) {
        viewModelScope.launch {
            dao.insertEvent(event)
        }
    }

    fun updateEvent(event: UserEvent) {
        viewModelScope.launch { dao.updateEvent(event) }
    }

    fun deleteEvent(event: UserEvent) {
        viewModelScope.launch { dao.deleteEvent(event) }
    }

    fun addEventWithReminder(event: UserEvent, context: Context) {
        viewModelScope.launch {
            dao.insertEvent(event)
            // 设置提醒
            val reminderManager = EventReminderManager(context)
            val triggerTime = parseDateToMillis(event.date, event.time)
            reminderManager.scheduleEventReminder(event.id, event.name, triggerTime)
        }
    }

}
