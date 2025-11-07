package com.example.cs501_fp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs501_fp.data.model.TicketmasterEvent
import com.example.cs501_fp.viewmodel.ShowDetailViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: String,
    viewModel: ShowDetailViewModel = viewModel()
) {
    val show by viewModel.showDetail.collectAsState()
    LaunchedEffect(showId) {
        viewModel.loadShowDetail(showId)
    }

    if (show == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val s = show!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = s.name ?: "untitled Show", style = MaterialTheme.typography.headlineMedium)
            Text(text = s._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre")
            Text(
                text = "Date: ${s.dates?.start?.localDate ?: "TBD"}  •  Time: ${s.dates?.start?.localTime ?: "TBD"}"
            )
            Text(text = "URL: ${s.url ?: "N/A"}")
            Spacer(Modifier.height(20.dp))
        }
    }
}