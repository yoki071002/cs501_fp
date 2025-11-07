package com.example.cs501_fp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.repository.TicketmasterRepository
import com.example.cs501_fp.data.model.TicketmasterEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TicketmasterViewModel : ViewModel() {

    private val repo = TicketmasterRepository()

    private val _events = MutableStateFlow<List<TicketmasterEvent>>(emptyList())
    val events: StateFlow<List<TicketmasterEvent>> = _events

    fun loadEvents() {
        viewModelScope.launch {
            val result = repo.getEvents()

            _events.value = result

            if (result.isEmpty()) {
                Log.e("TicketmasterVM", "No events received")
            } else {
                result.forEach {
                    Log.d("TicketmasterVM", "✅ ${it.name} — ${it._embedded?.venues?.firstOrNull()?.name}")
                }
            }
        }
    }
}
