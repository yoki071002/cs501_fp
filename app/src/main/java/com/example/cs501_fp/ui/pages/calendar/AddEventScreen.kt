package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.data.local.entity.UserEvent
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddEventScreen(
    onSave: (UserEvent) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }         // YYYY-MM-DD
    var timeText by remember { mutableStateOf("") }         // "7:00 PM"
    var seat by remember { mutableStateOf("") }             // not nullable
    var price by remember { mutableStateOf("") }

    var dateError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

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
                            // ----- 校验日期格式 -----
                            dateError = !isValidDate(dateText)
                            priceError = price.toDoubleOrNull() == null

                            if (!dateError && !priceError) {
                                onSave(
                                    UserEvent(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        venue = venue,
                                        dateText = dateText,
                                        timeText = timeText,
                                        seat = seat,
                                        price = price.toDouble()
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

            /* ---------- Date (YYYY-MM-DD) ---------- */
            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    dateText = it
                    dateError = false
                },
                label = { Text("Date (YYYY-MM-DD)") },
                isError = dateError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (dateError) {
                Text(
                    text = "Invalid date format. Please use YYYY-MM-DD.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            /* ---------- Time ---------- */
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("Time (e.g. 7:00 PM)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /* ---------- Seat ---------- */
            OutlinedTextField(
                value = seat,
                onValueChange = { seat = it },
                label = { Text("Seat (e.g. Mezz C • Row D • 8)") },
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
                singleLine = true
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

/* ---------------------------------------------------------
 * Helper: Check date format YYYY-MM-DD
 * --------------------------------------------------------- */
private fun isValidDate(text: String): Boolean {
    return Regex("""\d{4}-\d{2}-\d{2}""").matches(text)
}