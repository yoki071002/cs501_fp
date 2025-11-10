package com.example.cs501_fp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.data.repository.TicketmasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShowDetailViewModel : ViewModel() {
    private val _showDetail = MutableStateFlow<TicketmasterEvent?>(null)
    val showDetail: StateFlow<TicketmasterEvent?> = _showDetail
    private val repo = TicketmasterRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadShowDetail(showId: String) {
        viewModelScope.launch {
            _showDetail.value = repo.getEventDetails(showId)
        }
    }
}