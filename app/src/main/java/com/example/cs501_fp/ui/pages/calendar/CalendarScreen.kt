// File: app/src/main/java/com/example/cs501_fp/ui/pages/calendar/CalendarScreen.kt
// Displays the user's personal calendar, highlighting dates with events and showing upcoming shows.

package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.ui.components.OnCoreCard
import com.example.cs501_fp.ui.components.StaggeredEntry
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val events by viewModel.events.collectAsState(initial = emptyList())
    val headcounts by viewModel.headcounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Parse date strings only when events list changes
    val eventsWithParsed = remember(events) {
        events.mapNotNull { e ->
            try {
                e to LocalDate.parse(e.dateText, formatter)
            } catch (_: Exception) {
                null
            }
        }
    }

    // Filter next 6 events for the upcoming list
    val upcomingEvents = remember(eventsWithParsed) {
        eventsWithParsed
            .filter { it.second >= today }
            .sortedBy { it.second }
            .take(6)
            .map { it.first }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("My Calendar", style = MaterialTheme.typography.headlineMedium)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(28.dp))
                    }
                },
                windowInsets = WindowInsets.statusBars,
                modifier = Modifier.heightIn(max = 64.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_event") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Syncing with cloud...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                OnCoreCard {
                    Column(Modifier.padding(16.dp)) {
                        MonthHeader(
                            month = currentMonth,
                            onPrev = { currentMonth = currentMonth.minusMonths(1) },
                            onNext = { currentMonth = currentMonth.plusMonths(1) }
                        )

                        Spacer(Modifier.height(16.dp))
                        WeekdayHeader()
                        Spacer(Modifier.height(8.dp))

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
                                        Toast.makeText(context, "No events on $dateClicked", Toast.LENGTH_SHORT).show()
                                    }
                                    1 -> navController.navigate("event_detail/${eventsToday[0].id}")
                                    else -> navController.navigate("events_on_day/${dateClicked}")
                                }
                            }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Upcoming Shows",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    UpcomingList(
                        items = upcomingEvents,
                        headcounts = headcounts,
                        onEventClick = { event ->
                            navController.navigate("event_detail/${event.id}")
                        }
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}


// --- Month Header ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.primary)
        }

        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}


// --- Weekly Header ---
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
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}


// --- Month Grid ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    events: List<Pair<UserEvent, LocalDate>>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDay = month.atDay(1)
    val startIndex = firstDay.dayOfWeek.value % 7

    val days = (0 until 42).map { firstDay.plusDays((it - startIndex).toLong()) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { dateInfo ->
                    val inMonth = YearMonth.from(dateInfo) == month
                    val isToday = dateInfo == today
                    val hasEvent = events.any { it.second == dateInfo }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable(enabled = inMonth) { if (inMonth) onDayClick(dateInfo) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dateInfo.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color.White else if (inMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                            )

                            if (hasEvent) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) Color.White else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Upcoming List ---
@Composable
private fun UpcomingList(
    items: List<UserEvent>,
    headcounts: Map<String, Long>,
    onEventClick: (UserEvent) -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("No upcoming shows.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEachIndexed { index, event ->
                StaggeredEntry(index = index) {
                    OnCoreCard(
                        onClick = { onEventClick(event) }
                    ) {
                        Row(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${event.dateText} @ ${event.timeText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Social Headcount Badge
                            val count = headcounts[event.id] ?: 0
                            if (count > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Groups,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "+$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            } else {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}