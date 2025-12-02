package com.example.cs501_fp.ui.pages.tickets

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.util.saveBitmapToInternalStorage
import com.example.cs501_fp.util.saveUriToInternalStorage
import com.example.cs501_fp.viewmodel.CalendarViewModel
import java.io.File

/** ------------------------ Ticket Main Screen ------------------------ */
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
        topBar = { CenterAlignedTopAppBar(title = { Text("Ticket Wallet") }) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SpendingSummaryCard(total = totalSpent ?: 0.0) }
            item { Text("My Tickets (Tap to Flip)", style = MaterialTheme.typography.titleMedium) }

            if (events.isEmpty()) {
                item { Text("No tickets yet. Add events from Calendar or Home!", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(events) { event ->
                    FlipTicketCard(event, viewModel)
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

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

/** ------------------------ Flip Card Container ------------------------ */
@Composable
fun FlipTicketCard(event: UserEvent, viewModel: CalendarViewModel) {
    var rotated by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "FlipAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { rotated = !rotated }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            TicketFront(event, viewModel)
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                TicketBack(event, viewModel)
            }
        }
    }
}

/** ------------------------ Front Side (Poster & Visuals) ------------------------ */
@Composable
fun TicketFront(event: UserEvent, viewModel: CalendarViewModel) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showGalleryDialog by remember { mutableStateOf(false) }

    val bgImage = event.officialImageUrl ?: event.userImageUris.firstOrNull()

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            saveBitmapToInternalStorage(context, bitmap)?.let { path ->
                val newList = event.userImageUris + path
                viewModel.updateEvent(event.copy(userImageUris = newList))
                Toast.makeText(context, "Photo Added!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            saveUriToInternalStorage(context, uri)?.let { path ->
                val newList = event.userImageUris + path
                viewModel.updateEvent(event.copy(userImageUris = newList))
                Toast.makeText(context, "Photo Added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Photo / Stub") },
            text = { Text("Choose source:") },
            confirmButton = {},
            dismissButton = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { showImageSourceDialog = false; cameraLauncher.launch(null) }) { Text("Camera") }
                    TextButton(onClick = { showImageSourceDialog = false; galleryLauncher.launch("image/*") }) { Text("Gallery") }
                    TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    if (showGalleryDialog && event.userImageUris.isNotEmpty()) {
        Dialog(onDismissRequest = { showGalleryDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("My Stubs & Memories", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(event.userImageUris) { path ->
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { showGalleryDialog = false }, modifier = Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bgImage != null) {
                AsyncImage(
                    model = if (bgImage.startsWith("/")) File(bgImage) else bgImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))
            } else {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
            }

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).padding(end = 48.dp)) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${event.dateText} @ ${event.timeText}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Text(event.venue, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }

            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showImageSourceDialog = true }) {
                    Icon(Icons.Default.AddAPhoto, "Add Photo", tint = Color.White)
                }
                if (event.userImageUris.isNotEmpty()) {
                    IconButton(onClick = { showGalleryDialog = true }) {
                        BadgedBox(badge = { Badge { Text(event.userImageUris.size.toString()) } }) {
                            Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/** ------------------------ Back Side (Details & Notes) ------------------------ */
@Composable
fun TicketBack(event: UserEvent, viewModel: CalendarViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket") },
            text = { Text("Remove this ticket from wallet?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(event); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditNotesDialog) {
        EditNotesDialog(
            initialNotes = event.notes,
            onDismiss = { showEditNotesDialog = false },
            onSave = { newNotes ->
                viewModel.updateEvent(event.copy(notes = newNotes))
                showEditNotesDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    DetailItem(label = "Date", value = event.dateText)
                    DetailItem(label = "Time", value = event.timeText)
                }
                Column(Modifier.weight(1f)) {
                    DetailItem(label = "Seat", value = event.seat)
                    DetailItem(label = "Price", value = "\$${"%.2f".format(event.price)}")
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Repo / Notes", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = { showEditNotesDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Notes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .clickable { showEditNotesDialog = true }
                    .padding(8.dp)
            ) {
                if (event.notes.isBlank()) {
                    Text(
                        text = "Tap to write your review...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** ------------------------ Edit Notes Dialog ------------------------ */
@Composable
fun EditNotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    "Edit Repo / Notes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    placeholder = { Text("How was the show? Write your memories here...") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(notes) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
