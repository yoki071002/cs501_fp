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
import com.example.cs501_fp.data.local.entity.UserEvent
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEventScreen(
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        dateText = localDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },confirmButton = {
                TextButton(onClick = {
                    // 格式化时间 HH:mm
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    timeText = String.format("%02d:%02d", hour, minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimeInput(state = timePickerState)
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Event") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
                    TextButton(
                        onClick = {
                            priceError = price.toDoubleOrNull() == null

                            if (!priceError) {
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
                        },
                        enabled = title.isNotBlank()
                                && venue.isNotBlank()
                                && timeText.isNotBlank()
                                && dateText.isNotBlank()
                                && seat.isNotBlank()
                                && price.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- Title ---------- */
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /* ---------- Venue ---------- */
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("Venue") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /* ---------- Date Picker Field ---------- */
            OutlinedTextField(
                value = dateText,
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }
                    }
            )

            /* ---------- Time Picker Field ---------- */
            OutlinedTextField(
                value = timeText,
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showTimePicker = true
                                }
                            }
                        }
                    }
            )

            /* ---------- Seat ---------- */
            OutlinedTextField(
                value = seat,
                onValueChange = { seat = it },
                label = { Text("Seat") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /* ---------- Price ---------- */
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it
                    priceError = false
                },
                label = { Text("Price (number)") },
                isError = priceError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal) // 🟢 优化键盘
            )
            if (priceError) {
                Text(
                    text = "Price must be a valid number",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
