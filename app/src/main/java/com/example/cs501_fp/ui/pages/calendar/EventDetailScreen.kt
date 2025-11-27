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
fun EventDetailScreen(
    eventId: String,
    viewModel: CalendarViewModel,
    navController: NavHostController
) {
    val events by viewModel.events.collectAsState(initial = emptyList())

    // 找到对应的事件
    val event = events.firstOrNull { it.id == eventId }

    if (event == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Event Detail") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .padding(20.dp)
            ) {
                Text("Event not found.", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            /* ---------- Event Title ---------- */
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            /* ---------- Venue ---------- */
            Text("Venue: ${event.venue}", style = MaterialTheme.typography.bodyLarge)

            /* ---------- Date & Time ---------- */
            Text("Date: ${event.dateText}", style = MaterialTheme.typography.bodyLarge)
            Text("Time: ${event.timeText}", style = MaterialTheme.typography.bodyLarge)

            /* ---------- Seat ---------- */
            Text("Seat: ${event.seat}", style = MaterialTheme.typography.bodyLarge)

            /* ---------- Price ---------- */
            Text(
                "Price: $${"%.2f".format(event.price)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(40.dp))

            /* ---------- Delete Button ---------- */
            Button(
                onClick = {
                    viewModel.deleteEvent(event)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Event")
            }
        }
    }
}