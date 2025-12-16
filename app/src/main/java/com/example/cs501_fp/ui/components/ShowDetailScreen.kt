// File: app/src/main/java/com/example/cs501_fp/ui/components/ShowDetailScreen.kt
// Displays detailed info for a Ticketmaster event and handles the "Add to Wallet" logic with conflict detection.

package com.example.cs501_fp.ui.components

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.ShowDetailViewModel
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
import java.util.UUID
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: String,
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val show by viewModel.showDetail.collectAsState()
    val existingEvents by calendarViewModel.events.collectAsState(initial = emptyList())

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var isAdded by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var pendingEvent by remember { mutableStateOf<UserEvent?>(null) }

    var inputPrice by remember { mutableStateOf("") }
    var inputSeat by remember { mutableStateOf("") }

    LaunchedEffect(showId) {
        viewModel.loadShowDetail(showId)
    }

    // --- Helper Functions ---
    fun saveEventToWallet(event: UserEvent) {
        calendarViewModel.addEvent(event)
        isAdded = true
        Toast.makeText(context, "Ticket added to Wallet!", Toast.LENGTH_SHORT).show()
        pendingEvent = null
        showConflictDialog = false
        showInputDialog = false
    }

    fun parseTime(t: String): Int {
        return try {
            if (t.contains(":")) {
                val parts = t.split(":")
                var h = parts[0].trim().toInt()
                val m = parts[1].take(2).toInt()
                if (t.uppercase().contains("PM") && h != 12) h += 12
                if (t.uppercase().contains("AM") && h == 12) h = 0
                h * 60 + m
            } else 0
        } catch (e: Exception) { 0 }
    }


    // --- Dialogs ---
    if (showConflictDialog && pendingEvent != null) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Time Conflict Warning") },
            text = { Text("You already have another event scheduled around this time on ${pendingEvent?.dateText}. Do you want to add this one anyway?") },
            confirmButton = {
                OnCoreButton(
                    onClick = { pendingEvent?.let { saveEventToWallet(it) } }
                ) {
                    Text("Yes, Add It")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showInputDialog) {
        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            title = { Text("Add to Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Please enter your ticket details for accurate tracking:")
                    OutlinedTextField(
                        value = inputSeat,
                        onValueChange = { inputSeat = it },
                        label = { Text("Seat (Optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inputPrice,
                        onValueChange = { inputPrice = it },
                        label = { Text("Price Paid ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                OnCoreButton(
                    onClick = {
                        val s = show
                        if (s != null) {
                            val finalPrice = inputPrice.toDoubleOrNull() ?: 0.0
                            val finalSeat = inputSeat.ifBlank { "General Admission" }

                            val finalImageUrl = s.images?.firstOrNull { it.url?.contains("TABLET") == true }?.url
                                ?: s.images?.firstOrNull()?.url

                            val newEvent = UserEvent(
                                id = UUID.randomUUID().toString(),
                                title = s.name ?: "Unknown Show",
                                venue = s._embedded?.venues?.firstOrNull()?.name ?: "NYC Theatre",
                                dateText = s.dates?.start?.localDate ?: "2025-01-01",
                                timeText = s.dates?.start?.localTime ?: "19:00",
                                seat = finalSeat,
                                price = finalPrice,
                                officialImageUrl = finalImageUrl,
                                userImageUris = emptyList(),
                                ticketmasterId = s.id,
                                isPublic = false
                            )

                            val newTimeMin = parseTime(newEvent.timeText)
                            val hasConflict = existingEvents.any { event ->
                                if (event.dateText == newEvent.dateText) {
                                    val existingTimeMin = parseTime(event.timeText)
                                    abs(newTimeMin - existingTimeMin) < 180
                                } else false
                            }

                            if (hasConflict) {
                                pendingEvent = newEvent
                                showInputDialog = false
                                showConflictDialog = true
                            } else {
                                saveEventToWallet(newEvent)
                            }
                        }
                    }
                ) { Text("Save Ticket") }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) { Text("Cancel") }
            }
        )
    }


    // --- Main UI ---
    if (show == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val s = show!!
        Scaffold(
            containerColor = TicketPaperColor,
            topBar = {
                TheatricalTopBar(
                    title = "Show Detail",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TicketInkColor)
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { if (!isAdded) showInputDialog = true },
                    containerColor = if (isAdded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = if (isAdded) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(if (isAdded) Icons.Default.Check else Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isAdded) "Added to Wallet" else "I Bought a Ticket",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val imageUrl = s.images?.firstOrNull { it.url?.contains("TABLET") == true }?.url ?: s.images?.firstOrNull()?.url

                if (imageUrl != null) {
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .size(800, 800)
                                .build(),
                            contentDescription = "Poster",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 300f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                s.name ?: "Untitled Show",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(24.dp)
                    ) {
                        Text(
                            s.name ?: "Untitled Show",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OnCoreCard {
                        Column(Modifier.padding(16.dp)) {
                            // Venue
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Venue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(s._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            // Date
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Showtime", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        "${s.dates?.start?.localDate ?: "TBD"} at ${s.dates?.start?.localTime ?: "TBD"}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "(Venue Local Time)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if(!s.info.isNullOrBlank()) {
                        Text("About the Show", style = MaterialTheme.typography.titleLarge)
                        Text(
                            s.info,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    }

                    if (!s.url.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))

                        OnCoreButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(s.url)
                                } catch (e: Exception) {
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View on Ticketmaster")
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
