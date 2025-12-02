package com.example.cs501_fp.ui.pages.home

import android.os.Build
import androidx.annotation.RequiresApi
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cs501_fp.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onShowClick: (ShowSummary) -> Unit = {},
    onProfileClick: () -> Unit
) {
    val dailyPick by viewModel.dailyPick.collectAsState()
    val showsByDay by viewModel.showsThisWeek.collectAsState()
    val isPrevWeekEnabled by viewModel.isPrevWeekEnabled.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val sortedDays = showsByDay.keys.sorted()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MusicNY") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, "Profile")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                val context = LocalContext.current
                dailyPick?.let { pick ->
                    DailyPickBanner(
                        pick = pick,
                        isPlaying = isPlaying,
                        onListenClick = {
                            viewModel.togglePlayPreview(context, pick.id)
                        }
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Shows This Week",
                    isPrevEnabled = isPrevWeekEnabled,
                    onPrev = { viewModel.prevWeek() },
                    onNext = { viewModel.nextWeek() }
                )
            }

            if (showsByDay.isEmpty()) {
                item {
                    Text(
                        "No shows found for this week.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                sortedDays.forEach { date ->
                    val showsOnDate = showsByDay[date] ?: emptyList()
                    item {
                        DailyShowsItem(
                            date = date,
                            shows = showsOnDate,
                            onShowClick = onShowClick
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "* All show times are displayed in Venue Local Time (ET).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(60.dp))
                }
            }
        }
    }
}

/* ----------------------------- Components ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DailyShowsItem(
    date: LocalDate,
    shows: List<ShowSummary>,
    onShowClick: (ShowSummary) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Card(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand or collapse"
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                shows.forEach { show ->
                    ShowCard(show = show, onClick = { onShowClick(show) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyPickBanner(
    pick: ShowSummary,
    isPlaying: Boolean,
    onListenClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onListenClick() },
        contentAlignment = Alignment.BottomStart
    ) {
        if (pick.imageUrl != null) {
            AsyncImage(
                model = pick.imageUrl,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(Modifier.padding(16.dp)) {
            Text("Daily Pick", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            Text(
                pick.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                pick.venue,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onListenClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isPlaying) "Pause" else "Listen")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, isPrevEnabled: Boolean, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onPrev, enabled = isPrevEnabled) { Text("‹") }
        TextButton(onClick = onNext) { Text("›") }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ShowCard(show: ShowSummary, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (show.imageUrl != null) {
                AsyncImage(
                    model = show.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(show.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(show.venue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    show.dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
