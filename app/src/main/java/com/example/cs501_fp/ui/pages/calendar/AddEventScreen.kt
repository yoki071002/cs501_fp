// File: app/src/main/java/com/example/cs501_fp/ui/pages/calendar/AddEventScreen.kt
// A form screen for manually adding an event, supporting Image Capture and Ticketmaster Search.

package com.example.cs501_fp.ui.pages.calendar

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.Coil
import coil.request.ImageRequest
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.util.saveBitmapToInternalStorage
import com.example.cs501_fp.util.saveUriToInternalStorage
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.ui.components.OnCoreButton
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.io.File
import kotlin.math.abs

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEventScreen(
    viewModel: CalendarViewModel = viewModel(),
    onSave: (UserEvent) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current

    // Form State
    var title by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var seat by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // Metadata from Search
    var officialImageUrl by remember { mutableStateOf<String?>(null) }
    var tmId by remember { mutableStateOf<String?>(null) }

    // Image Handling State
    val tempPhotoUris = remember { mutableStateListOf<Uri>() }
    val tempBitmaps = remember { mutableStateListOf<Bitmap>() }

    // Temporary file for camera output
    val tempCameraUri = remember {
        val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Search & Validation State
    val searchResults by viewModel.searchResults.collectAsState()
    var priceError by remember { mutableStateOf(false) }

    // Dialogs & Pickers
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var showConflictDialog by remember { mutableStateOf(false) }
    val existingEvents by viewModel.events.collectAsState(initial = emptyList())


    // --- Permission & Activity Launcher ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUris.add(tempCameraUri)
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempCameraUri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) tempPhotoUris.add(uri)
    }


    // --- Helper Functions ---
    fun doSave() {
        val savedPaths = mutableListOf<String>()
        tempPhotoUris.forEach { uri ->
            saveUriToInternalStorage(context, uri)?.let { savedPaths.add(it) }
        }
        tempBitmaps.forEach { bmp ->
            saveBitmapToInternalStorage(context, bmp)?.let { savedPaths.add(it) }
        }

        onSave(
            UserEvent(
                id = UUID.randomUUID().toString(),
                title = title,
                venue = venue,
                dateText = dateText,
                timeText = timeText,
                seat = seat,
                price = price.toDoubleOrNull() ?: 0.0,
                officialImageUrl = officialImageUrl,
                userImageUris = savedPaths,
                ticketmasterId = tmId,
                isPublic = false
            )
        )
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

    fun attemptSave() {
        priceError = price.toDoubleOrNull() == null
        if (priceError) return
        val newTimeMin = parseTime(timeText)
        val hasConflict = existingEvents.any { event ->
            if (event.dateText == dateText) {
                val existingTimeMin = parseTime(event.timeText)
                abs(newTimeMin - existingTimeMin) < 180
            } else false
        }
        if (hasConflict) showConflictDialog = true else doSave()
    }


    // --- Dialogs UI ---
    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Time Conflict Warning") },
            text = { Text("You already have an event scheduled around this time on $dateText. Add anyway?") },
            confirmButton = {
                OnCoreButton(onClick = { showConflictDialog = false; doSave() }) {
                    Text("Yes, Add It")
                }
            },
            dismissButton = { TextButton(onClick = { showConflictDialog = false }) { Text("Cancel") } }
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    dateText = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate().toString()
                }
                showDatePicker = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = {
                timeText = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimeInput(state = timePickerState) }
        )
    }


    // --- Main UI Layout ---
    Scaffold(
        containerColor = TicketPaperColor,
        topBar = {
            TheatricalTopBar(
                title = "Add Event",
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = TicketInkColor)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { attemptSave() },
                        enabled = title.isNotBlank() && venue.isNotBlank() && timeText.isNotBlank() && dateText.isNotBlank()
                    ) {
                        Text("Save", color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input (with auto complete search)
            Box(modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; viewModel.searchEvents(it) },
                    label = { Text("Show Title (Search)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Dropdown for search results
                if (searchResults.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier
                            .padding(top = 60.dp)
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        LazyColumn {
                            items(searchResults) { event ->
                                ListItem(
                                    headlineContent = { Text(event.name ?: "") },
                                    supportingContent = { Text(event._embedded?.venues?.firstOrNull()?.name ?: "") },
                                    modifier = Modifier.clickable {
                                        title = event.name ?: ""
                                        venue = event._embedded?.venues?.firstOrNull()?.name ?: ""
                                        officialImageUrl = event.images?.firstOrNull()?.url
                                        tmId = event.id
                                        viewModel.clearSearchResults()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())

            // Date & Time Pickers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateText, onValueChange = {}, label = { Text("Date") }, modifier = Modifier.weight(1f), readOnly = true,
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    interactionSource = remember { MutableInteractionSource() }.also { src ->
                        LaunchedEffect(src) { src.interactions.collect { if (it is PressInteraction.Release) showDatePicker = true } }
                    }
                )
                OutlinedTextField(
                    value = timeText, onValueChange = {}, label = { Text("Time") }, modifier = Modifier.weight(1f), readOnly = true,
                    trailingIcon = { Icon(Icons.Default.Schedule, null) },
                    interactionSource = remember { MutableInteractionSource() }.also { src ->
                        LaunchedEffect(src) { src.interactions.collect { if (it is PressInteraction.Release) showTimePicker = true } }
                    }
                )
            }

            // Seat & Price
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = seat, onValueChange = { seat = it }, label = { Text("Seat") }, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = price, onValueChange = { price = it; priceError = false }, label = { Text("Price") },
                    isError = priceError, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Media Attachment Section
            Text("Add Photos / Ticket Stubs", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // Camera Button
                OutlinedButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(tempCameraUri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Camera")
                }

                // Gallery Button
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null); Spacer(Modifier.width(8.dp)); Text("Gallery")
                }
            }

            // Preview selected images
            if (tempPhotoUris.isNotEmpty() || tempBitmaps.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tempPhotoUris) { uri ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .size(200, 200)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                    items(tempBitmaps) { bmp ->
                        AsyncImage(
                            model = bmp,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // Clean up temporary files when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("MEMORY_CLEANUP", "AddEventScreen is being disposed. Clearing temp images")
            tempBitmaps.clear()
            tempPhotoUris.clear()
            Coil.imageLoader(context).memoryCache?.clear()
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("camera_photo_")) {
                    file.delete()
                }
            }
        }
    }
}
