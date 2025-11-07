package com.example.cs501_fp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.repository.TicketmasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowDetailViewModel : ViewModel() {
    private val ticketRepo = TicketmasterRepository()
    private val _showDetail = MutableStateFlow<TicketmasterEvent?>(null)
    val showDetail: StateFlow<TicketmasterEvent?> = _showDetail
    private val repo = TicketmasterRepository()

    fun loadShowDetail(showId: String) {
        viewModelScope.launch {
            val events = repo.getEvents()
            _showDetail.value = events.firstOrNull { it.id == showId }
        }
    }
}