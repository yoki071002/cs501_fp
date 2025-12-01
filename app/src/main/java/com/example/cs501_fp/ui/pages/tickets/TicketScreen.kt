package com.example.cs501_fp.ui.pages.tickets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // 确保引入这个
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel

/** ------------------------ Ticket main screen ------------------------ */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    viewModel: CalendarViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState(initial = emptyList())
    val totalSpent by viewModel.totalSpent.collectAsState(initial = 0.0)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Ticket Wallent") }) }
    ) {
        inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SpendingSummaryCard(total = totalSpent ?: 0.0)
            }

            item {
                Text("My Tickets", style = MaterialTheme.typography.titleMedium)
            }

            if (events.isEmpty()) {
                item {
                    Text(
                        "No tickets yet. Add events from Calendar or Home!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(events) { event ->
                    TicketCard(event)
                }
            }

            item {
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}


/** ------------------------ Monetary feature card ------------------------ */
@Composable
fun SpendingSummaryCard(total: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Total Spent on Theater Shows", style = MaterialTheme.typography.labelMedium)
            Text(
                text = "\$${"%.2f".format(total)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/** ------------------------  Single Ticket Card ------------------------ */
@Composable
private fun TicketCard(event: UserEvent) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Venue
            Text(
                event.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(event.dateText, style = MaterialTheme.typography.bodySmall)
                Text(event.timeText, style = MaterialTheme.typography.bodySmall)
            }

            // Seat
            if (event.seat.isNotBlank()) {
                Text("Seat: ${event.seat}", style = MaterialTheme.typography.bodySmall)
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // Price + Action
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Price: \$${"%.2f".format(event.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 🟢 这里未来可以加拍照按钮 (Phase 3)
                // OutlinedButton(onClick = { /* TODO: Upload Photo */ }) { Text("Upload Stub") }
            }
        }
    }
}