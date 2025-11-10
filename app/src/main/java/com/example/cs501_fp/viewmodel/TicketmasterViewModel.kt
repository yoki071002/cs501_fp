package com.example.cs501_fp.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.repository.TicketmasterRepository
import com.example.cs501_fp.data.model.TicketmasterEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class TicketmasterViewModel : ViewModel() {

    private val repo = TicketmasterRepository()

    private val _events = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val events: StateFlow<List<TicketmasterEvent>> = _events

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadEvents() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfWeek = today.with(DayOfWeek.MONDAY)
            val endOfWeek = startOfWeek.plusDays(6)

            val startDateTime = startOfWeek.atStartOfDay(ZoneId.systemDefault())
            val endDateTime = endOfWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault())

            val result = repo.getEvents(startDateTime, endDateTime)

            _events.value = result

            if (result.isEmpty()) {
                Log.e("TicketmasterVM", "No events received")
            } else {
                result.forEach {
                    Log.d("TicketmasterVM", "${it.name} — ${it._embedded?.venues?.firstOrNull()?.name}")
                }
            }
        }
    }
}
