package com.example.cs501_fp.ui.pages.calendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.math.abs

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEventScreen(
    viewModel: CalendarViewModel = viewModel(),
    onSave: (UserEvent) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var seat by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    var showConflictDialog by remember { mutableStateOf(false) }
    val existingEvents by viewModel.events.collectAsState(initial = emptyList())

    fun doSave() {
        onSave(
            UserEvent(
                id = UUID.randomUUID().toString(),
                title = title,
                venue = venue,
                dateText = dateText,
                timeText = timeText,
                seat = seat,
                price = price.toDoubleOrNull() ?: 0.0
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
            } else {
                false
            }
        }

        if (hasConflict) {
            showConflictDialog = true
        } else {
            doSave()
        }
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Time Conflict Warning") },
            text = { Text("You already have an event scheduled around this time on $dateText. Do you want to add this one anyway?") },
            confirmButton = {
                Button(onClick = {
                    showConflictDialog = false
                    doSave()
                }) { Text("Yes, Add It") }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        dateText = localDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    timeText = String.format("%02d:%02d", hour, minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimeInput(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Event") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
                actions = {
                    TextButton(
                        onClick = { attemptSave() },
                        enabled = title.isNotBlank() && venue.isNotBlank() && timeText.isNotBlank() && dateText.isNotBlank() && seat.isNotBlank() && price.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(
                value = dateText, onValueChange = { }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth(), readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                interactionSource = remember { MutableInteractionSource() }.also { src ->
                    LaunchedEffect(src) { src.interactions.collect { if (it is PressInteraction.Release) showDatePicker = true } }
                }
            )

            OutlinedTextField(
                value = timeText, onValueChange = { }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth(), readOnly = true,
                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                interactionSource = remember { MutableInteractionSource() }.also { src ->
                    LaunchedEffect(src) { src.interactions.collect { if (it is PressInteraction.Release) showTimePicker = true } }
                }
            )

            OutlinedTextField(value = seat, onValueChange = { seat = it }, label = { Text("Seat") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(
                value = price, onValueChange = { price = it; priceError = false },
                label = { Text("Price (number)") }, isError = priceError, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            if (priceError) Text("Price must be a valid number", color = MaterialTheme.colorScheme.error)
        }
    }
}
