import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.ui.calendar.AddEventDialog
import com.example.cs501_fp.ui.calendar.CalendarViewModel

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val events by viewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "My Calendar Events (${events.size})",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (events.isEmpty()) {
                Text("No events yet. Tap + to add one.")
            } else {
                events.forEach { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = event.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${event.venue} · ${event.date} ${event.time}")
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddEventDialog(
                onDismiss = { showDialog = false },
                onAddEvent = { newEvent ->
                    viewModel.addEvent(newEvent)
                    showDialog = false
                }
            )
        }
    }
}