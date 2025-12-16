// File: app/src/main/java/com/example/cs501_fp/ui/pages/calendar/EventDetailScreen.kt
// Displays details of a saved event from the local database and allows deletion

package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cs501_fp.ui.components.OnCoreCard
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
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
    val event = events.firstOrNull { it.id == eventId }

    if (event == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TheatricalTopBar(
                    title = "Event Detail",
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TicketInkColor)
                        }
                    }
                )
            }
        ) { inner ->
            Box(
                Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found.", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Event Detail",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                windowInsets = WindowInsets.statusBars,
                modifier = Modifier.heightIn(max = 64.dp)
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!event.officialImageUrl.isNullOrEmpty()) {
                Box(Modifier.fillMaxWidth().height(250.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(event.officialImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Event Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 300f
                                )
                            )
                    )
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(24.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OnCoreCard {
                    Column(Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.LocationOn, "Venue", event.venue)
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        DetailRow(Icons.Default.Event, "Date & Time", "${event.dateText} at ${event.timeText}")
                    }
                }

                OnCoreCard {
                    Column(Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.EventSeat, "Seat", if (event.seat.isNotBlank()) event.seat else "General Admission")
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        DetailRow(Icons.Default.AttachMoney, "Price Paid", "$${"%.2f".format(event.price)}")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.deleteEvent(event)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Event", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}