package com.example.cs501_fp.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.data.model.UserEvent

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (UserEvent) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && date.isNotBlank()) {
                    onAddEvent(
                        UserEvent(
                            name = name,
                            venue = venue,
                            date = date,
                            time = time
                        )
                    )
                    onDismiss()
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        },
        title = { Text("Add New Event") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Event Name") })
                OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") })
            }
        }
    )
}
