package com.example.cs501_fp.ui.calendar

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.data.model.UserEvent
import java.util.*

@Composable
fun AddEventDialog(
    context: Context,
    userId: String, // ✅ 当前登录用户 ID（Firebase UID）
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
            TextButton(
                onClick = {
                    if (name.isNotBlank() && date.isNotBlank()) {
                        val newEvent = UserEvent(
                            eventId = UUID.randomUUID().toString(), // ✅ 唯一 ID
                            userId = userId,                         // ✅ 当前用户
                            name = name,
                            venue = venue,
                            date = date,
                            time = time
                        )
                        onAddEvent(newEvent)
                        onDismiss()
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        },
        title = { Text("Add New Event") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}