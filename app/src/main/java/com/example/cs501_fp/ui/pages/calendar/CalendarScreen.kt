package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by viewModel.events.collectAsState(initial = emptyList())
    val headcounts by viewModel.headcounts.collectAsState()

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val eventsWithParsed = remember(events) {
        events.mapNotNull { e ->
            try {
                e to LocalDate.parse(e.dateText, formatter)
            } catch (_: Exception) {
                null
            }
        }
    }

    val upcomingEvents = remember(eventsWithParsed) {
        eventsWithParsed
            .filter { it.second >= today }
            .sortedBy { it.second }
            .take(6)
            .map { it.first }
    }

    LaunchedEffect(upcomingEvents) {
        if (upcomingEvents.isNotEmpty()) {
            viewModel.fetchUpcomingHeadcounts(upcomingEvents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Calendar") },
                actions = {
                    TextButton(onClick = { navController.navigate("add_event") }) {
                        Text("Add Event")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            MonthHeader(
                month = currentMonth,
                onPrev = { currentMonth = currentMonth.minusMonths(1) },
                onNext = { currentMonth = currentMonth.plusMonths(1) }
            )

            WeekdayHeader()

            MonthGrid(
                month = currentMonth,
                today = today,
                events = eventsWithParsed,
                onDayClick = { dateClicked ->
                    val eventsToday = eventsWithParsed
                        .filter { it.second == dateClicked }
                        .map { it.first }

                    when (eventsToday.size) {
                        0 -> {
                            android.widget.Toast.makeText(context, "No events on $dateClicked", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        1 -> navController.navigate("event_detail/${eventsToday[0].id}")
                        else -> {
                            navController.navigate("events_on_day/${dateClicked}")
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            Text("Upcoming Shows", style = MaterialTheme.typography.titleMedium)

            UpcomingList(
                items = upcomingEvents,
                headcounts = headcounts,
                onEventClick = { event ->
                    navController.navigate("event_detail/${event.id}")
                }
            )
        }
    }
}

/* ----------------------- Month Header ----------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onPrev) { Text("‹") }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onNext) { Text("›") }
    }
}

/* ----------------------- Weekday Header ----------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        ).forEach {
            Text(
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/* ----------------------- Month Grid ----------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    events: List<Pair<UserEvent, LocalDate>>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val startIndex = firstDay.dayOfWeek.value % 7

    val days = (0 until 42).map { firstDay.plusDays((it - startIndex).toLong()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { dateInfo ->
                    val inMonth = YearMonth.from(dateInfo) == month
                    val isToday = dateInfo == today
                    val hasEvent = events.any { it.second == dateInfo }

                    DayCell(
                        date = dateInfo,
                        enabled = inMonth,
                        isToday = isToday,
                        hasEvent = hasEvent,
                        onClick = { if (inMonth) onDayClick(dateInfo) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    date: LocalDate,
    enabled: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val textColor =
        if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            Text(
                date.dayOfMonth.toString(),
                color = textColor,
                style = if (isToday) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvent) {
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/* ----------------------- Upcoming List ----------------------- */

@Composable
private fun UpcomingList(
    items: List<UserEvent>,
    headcounts: Map<String, Long>,
    onEventClick: (UserEvent) -> Unit
) {
    if (items.isEmpty()) {
        Text("No upcoming shows yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { e ->
            val othersCount = headcounts[e.id] ?: 0L

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEventClick(e) }
            ) {
                Row(Modifier.padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(e.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${e.dateText} • ${e.timeText} • ${e.venue}", style = MaterialTheme.typography.bodySmall)

                        if (othersCount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Groups,
                                    contentDescription = "Others going",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "$othersCount others going!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text("\$${"%.0f".format(e.price)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}