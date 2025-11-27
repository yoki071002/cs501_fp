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

/** ---------------- Ticket 数据模型（纯 UI 用） ---------------- */
data class TicketItem(
    val id: String,
    val title: String,
    val venue: String,
    val dateText: String,   // "2025-10-08"
    val timeText: String,   // "7:00 PM"
    val seat: String,       // "Mezz C • Row D • 8"
    val price: Double
)

/** ---------------- 假数据（你以后可改为从 DB 读取） ---------------- */
private val demoTickets = listOf(
    TicketItem(
        id = "lk",
        title = "The Lion King",
        venue = "Minskoff Theatre",
        dateText = "2025-09-15",
        timeText = "7:00 PM",
        seat = "Mezz C • Row D • 8",
        price = 79.0
    ),
    TicketItem(
        id = "wk",
        title = "Wicked",
        venue = "Gershwin Theatre",
        dateText = "2025-10-02",
        timeText = "8:00 PM",
        seat = "Orch • Row F • 12",
        price = 85.0
    )
)

/** ------------------------ Ticket 主页面 ------------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen() {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Tickets") }) }
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

            ExperienceCard(
                title = demoTickets.first().title
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/** ------------------------ 单张 Ticket 卡片 ------------------------ */
@Composable
private fun TicketCard(ticket: TicketItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Title
            Text(
                ticket.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Venue
            Text(
                ticket.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(ticket.dateText, style = MaterialTheme.typography.bodySmall)
                Text(ticket.timeText, style = MaterialTheme.typography.bodySmall)
            }

            // Seat
            Text("Seat: ${ticket.seat}", style = MaterialTheme.typography.bodySmall)

            // Price + Detail Button
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Price: \$${"%.2f".format(ticket.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(onClick = { /* TODO: View Ticket Detail */ }) {
                    Text("View Details")
                }
            }
        }
    }
}

/** ------------------------ Experience Card ------------------------ */
@Composable
private fun ExperienceCard(title: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "My Experience",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* TODO: Add Review */ }) { Text("Add Review") }
                OutlinedButton(onClick = { /* TODO: Upload Photo */ }) { Text("Upload Photo") }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun TicketScreenPreview() {
    MaterialTheme { TicketScreen() }
}