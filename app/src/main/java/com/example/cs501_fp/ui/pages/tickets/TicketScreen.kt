package com.example.cs501_fp.ui.pages.tickets

import androidx.activity.compose.rememberLauncherForActivityResult
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel

/** ------------------------ Ticket main screen ------------------------ */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    viewModel: CalendarViewModel = viewModel()
) {
    val rawEvents by viewModel.events.collectAsState(initial = emptyList())
    val events = remember(rawEvents) {
        rawEvents.sortedByDescending { it.dateText }
    }
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
private fun TicketCard(event: UserEvent, viewModel: CalendarViewModel = viewModel()) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket") },
            text = { Text("Are you sure you want to remove this ticket from your wallet?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(event)
                        showDeleteDialog = false
                        Toast.makeText(context, "Ticket removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uriStr = saveBitmapToInternalStorage(context, bitmap, event.id)
            if (uriStr != null) {
                val updatedEvent = event.copy(imageUri = uriStr as String?)
                viewModel.updateEvent(updatedEvent)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission required to upload stub", Toast.LENGTH_SHORT).show()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Ticket",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }

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

                if (event.imageUri == null) {
                    OutlinedButton(onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Upload Stub")
                    }
                } else {
                    Text("Stub Saved ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }

            if (event.imageUri != null) {
                Spacer(Modifier.height(8.dp))
                Text("My Ticket Stub:", style = MaterialTheme.typography.labelSmall)
                AsyncImage(
                    model = event.imageUri,
                    contentDescription = "Ticket Stub",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


/** ------------------------ helper function: save bitmap local ------------------------ */
private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, eventId: String): String? {
    return try {
        val filename = "stub_$eventId.jpg"
        val fos: FileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()
        File(context.filesDir, filename).absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}