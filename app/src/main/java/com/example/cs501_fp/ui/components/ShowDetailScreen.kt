package com.example.cs501_fp.ui.components

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.ShowDetailViewModel
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

    val context = LocalContext.current

    var isAdded by remember { mutableStateOf(false) }

    var showInputDialog by remember { mutableStateOf(false) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var pendingEvent by remember { mutableStateOf<UserEvent?>(null) } // 暂存

    var inputPrice by remember { mutableStateOf("") }
    var inputSeat by remember { mutableStateOf("") }

    LaunchedEffect(showId) {
        viewModel.loadShowDetail(showId)
    }

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

    if (showConflictDialog && pendingEvent != null) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Time Conflict Warning") },
            text = {
                Text("You already have another event scheduled around this time on ${pendingEvent?.dateText}. Do you want to add this one anyway?")
            },
            confirmButton = {
                Button(onClick = {
                    pendingEvent?.let { saveEventToWallet(it) }
                }) {
                    Text("Yes, Add It")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) {
                    Text("Cancel")
                }
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
                        label = { Text("Seat") },
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
                TextButton(
                    onClick = {
                        val s = show
                        if (s != null) {
                            val finalPrice = inputPrice.toDoubleOrNull() ?: 0.0
                            val finalSeat = inputSeat.ifBlank { "General Admission" }

                            val newEvent = UserEvent(
                                id = UUID.randomUUID().toString(),
                                title = s.name ?: "Unknown Show",
                                venue = s._embedded?.venues?.firstOrNull()?.name ?: "NYC Theatre",
                                dateText = s.dates?.start?.localDate ?: "2025-01-01",
                                timeText = s.dates?.start?.localTime ?: "19:00",
                                seat = finalSeat,
                                price = finalPrice,
                                imageUri = null
                            )

                            val newTimeMin = parseTime(newEvent.timeText)
                            val hasConflict = existingEvents.any { event ->
                                if (event.dateText == newEvent.dateText) {
                                    val existingTimeMin = parseTime(event.timeText)
                                    abs(newTimeMin - existingTimeMin) < 180 // 间隔小于3小时
                                } else {
                                    false
                                }
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
                ) {
                    Text("Save Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(s.name ?: "Show Detail") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!isAdded) {
                            showInputDialog = true
                        }
                    },
                    containerColor = if (isAdded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = if (isAdded) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAdded) "Added" else "I Bought a Ticket")
                }
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val imageUrl = s.images?.firstOrNull { it.url?.contains("TABLET") == true }?.url
                    ?: s.images?.firstOrNull()?.url

                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Poster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    text = s.name ?: "Untitled Show",
                    style = MaterialTheme.typography.headlineMedium
                )

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Venue", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = s._embedded?.venues?.firstOrNull()?.name ?: "Unknown Theatre",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if(!s.info.isNullOrBlank()) {
                    Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(s.info, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Date & Time", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${s.dates?.start?.localDate ?: "TBD"} at ${s.dates?.start?.localTime ?: "TBD"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "(Venue Local Time)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!s.url.isNullOrBlank()) {
                    Text(text = "More Info: ${s.url}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
