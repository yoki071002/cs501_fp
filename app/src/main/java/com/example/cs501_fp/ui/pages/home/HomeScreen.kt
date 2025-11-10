//TopAppBar：标题“MusicNY”、搜索按钮
//
//Daily Pick Banner：专辑图 + 歌名 + “Listen” 按钮
//
//本周演出（Shows This Week）：左右切换周
//
//演出列表：卡片（海报 | 名称 | 时间 | 剧院 | 价格 | “Book”/“Wishlist”）


package com.example.cs501_fp.ui.pages.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.cs501_fp.viewmodel.HomeViewModel

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Simple model for a show item on Home */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onListenClick: (ShowSummary) -> Unit = {},
    onShowClick: (ShowSummary) -> Unit = {},
    onPrevWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {}
) {
    val dailyPick = viewModel.dailyPick.collectAsState().value
    val shows = viewModel.showsThisWeek.collectAsState().value

    LaunchedEffect(Unit) {
        if (dailyPick == null && shows.isEmpty()) {
            viewModel.loadData()
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("MusicNY") }) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val context = LocalContext.current
                dailyPick?.let { pick ->
                    DailyPickBanner(
                        pick = pick,
                        onListenClick = {
                            viewModel.playPreview(context, pick.id)
                        }
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Shows This Week",
                    onPrev = { viewModel.prevWeek() },
                    onNext = { viewModel.nextWeek() }
                )
            }

            items(shows) { show ->
                ShowCard(show = show, onClick = { onShowClick(show) })
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ----------------------------- Components ----------------------------- */

@Composable
private fun DailyPickBanner(
    pick: ShowSummary,
    onListenClick: (ShowSummary) -> Unit
) {
    // Gradient background; swap to an Image if you prefer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .clickable { onListenClick(pick) },
        contentAlignment = Alignment.CenterStart
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Daily Pick", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            Text(pick.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            AssistChip(onClick = { onListenClick(pick) }, label = { Text("Listen") })
        }
    }
}

@Composable
private fun SectionHeader(title: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onPrev) { Text("‹") }
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
            // Left thumbnail (placeholder block if no image lib)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(show.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(show.venue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    show.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "  •  From \$${show.priceFrom}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.width(8.dp))
            AssistChip(onClick = onClick, label = { Text("Book") })
        }
    }
}

/* ----------------------------- Preview ----------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
private val demoShows = listOf(
    ShowSummary("1", "Hamilton", "Richard Rodgers Theatre", LocalDate.now().plusDays(1), 89),
    ShowSummary("2", "Wicked", "Gershwin Theatre", LocalDate.now().plusDays(2), 85),
    ShowSummary("3", "The Lion King", "Minskoff Theatre", LocalDate.now().plusDays(3), 79)
)
