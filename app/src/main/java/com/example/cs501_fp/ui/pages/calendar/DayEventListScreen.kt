package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayEventListScreen(
    dateText: String,
    viewModel: CalendarViewModel,
    navController: NavHostController
) {
    // 所有事件
    val events by viewModel.events.collectAsState(initial = emptyList())

    // 过滤当天
    val dayEvents = remember(events, dateText) {
        events.filter { it.dateText == dateText }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events on $dateText") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (dayEvents.isEmpty()) {
                Text(
                    "No events on this day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            DayEventList(
                events = dayEvents,
                onEventClick = { event ->
                    navController.navigate("event_detail/${event.id}")
                }
            )
        }
    }
}

@Composable
private fun DayEventList(
    events: List<UserEvent>,
    onEventClick: (UserEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        events.forEach { e ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEventClick(e) }
            ) {
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            e.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${e.timeText} • ${e.venue}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (e.seat.isNotBlank()) {
                            Text(
                                "Seat: ${e.seat}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        "\$${"%.2f".format(e.price)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
