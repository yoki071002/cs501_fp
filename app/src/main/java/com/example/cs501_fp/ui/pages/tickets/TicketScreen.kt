package com.example.cs501_fp.ui.pages.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class TicketItem(
    val id: String,
    val title: String,
    val venue: String,
    val dateText: String,   // "2025-10-08"
    val timeText: String,   // "7:00 PM"
    val seat: String,       // "Mezz C • Row D • 8"
    val price: Double
)

private val demoTickets = listOf(
    TicketItem("lk", "The Lion King", "Minskoff Theatre", "2025-09-15", "7:00 PM", "Mezz C • Row D • 8", 79.0),
    TicketItem("wk", "Wicked", "Gershwin Theatre", "2025-10-02", "8:00 PM", "Orch • Row F • 12", 85.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen() {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Tickets") }) },
        floatingActionButton = {
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Past Shows", style = MaterialTheme.typography.titleMedium)

            demoTickets.forEach { ticket ->
                TicketCard(ticket)
            }

            Text("My Experience", style = MaterialTheme.typography.titleMedium)
            ExperienceCard(title = demoTickets.first().title)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TicketCard(ticket: TicketItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(ticket.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ticket.venue, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(ticket.dateText, style = MaterialTheme.typography.bodySmall)
                Text(ticket.timeText, style = MaterialTheme.typography.bodySmall)
            }
            Text("Seat: ${ticket.seat}", style = MaterialTheme.typography.bodySmall)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Price: \$${"%.2f".format(ticket.price)}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { /* TODO */ }) { Text("View Details") }
            }
        }
    }
}

@Composable
private fun ExperienceCard(title: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("My Experience", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(title, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text("Photo", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* TODO */ }) { Text("Add Review") }
                OutlinedButton(onClick = { /* TODO */ }) { Text("Upload Photo") }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun TicketScreenPreview() { MaterialTheme { TicketScreen() } }
