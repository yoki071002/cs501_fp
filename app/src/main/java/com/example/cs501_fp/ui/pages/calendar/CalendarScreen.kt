//opAppBar：标题“My Calendar” + “Add Event” 按钮
//
//月视图：7 列网格（Sun–Sat），某天有演出显示小标记/价格
//
//Spending Overview（可选）：本月花费柱状/统计
//
//Upcoming Shows：近期条目列表（名称/日期/时间/剧院/标签：booked/past）


package com.example.cs501_fp.ui.pages.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** 数据模型：一次演出/事件 */
data class ShowEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val venue: String,
    val price: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    initialMonth: YearMonth = YearMonth.now(),
    events: List<ShowEvent> = emptyList(),
    onAddEvent: () -> Unit = {},
    onSelectDay: (LocalDate) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(initialMonth) }
    val today = LocalDate.now()

    val monthEvents = remember(currentMonth, events) {
        events.filter { YearMonth.from(it.date) == currentMonth }
    }
    val monthSpend = monthEvents.sumOf { it.price }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Calendar") },
                actions = {
                    TextButton(onClick = onAddEvent) { Text("Add Event") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 月份标题 + 左右切换
            MonthHeader(
                month = currentMonth,
                onPrev = { currentMonth = currentMonth.minusMonths(1) },
                onNext = { currentMonth = currentMonth.plusMonths(1) }
            )

            // 星期标题
            WeekdayHeader()

            // 月视图网格
            MonthGrid(
                month = currentMonth,
                today = today,
                events = events,
                onDayClick = { onSelectDay(it) }
            )

            // 本月花费统计
            SpendingOverviewCard(
                monthTotal = monthSpend,
                count = monthEvents.size
            )

            // 即将到来的演出
            Text("Upcoming Shows", style = MaterialTheme.typography.titleMedium)
            UpcomingList(
                items = events.filter { it.date >= today }.sortedBy { it.date }.take(6)
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        // 以周日为一周起始
        val days = listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        )
        days.forEach {
            Text(
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    events: List<ShowEvent>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val firstWeekdayIndex = ((firstOfMonth.dayOfWeek.value) % 7) // 将周日置为0
    val daysInMonth = month.lengthOfMonth()

    // 生成 6 周 × 7 天 = 42 个格子，包含跨月补齐
    val cells = (0 until 42).map { index ->
        val dayOffset = index - firstWeekdayIndex
        firstOfMonth.plusDays(dayOffset.toLong())
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.forEach { date ->
                    val inThisMonth = YearMonth.from(date) == month
                    val isToday = date == today
                    val hasEvent = events.any { it.date == date }

                    DayCell(
                        date = date,
                        enabled = inThisMonth,
                        isToday = isToday,
                        hasEvent = hasEvent,
                        onClick = { if (inThisMonth) onDayClick(date) },
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
    modifier: Modifier = Modifier
) {
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // 日期号
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = if (isToday) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
            )
            // 演出打点
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

@Composable
private fun SpendingOverviewCard(monthTotal: Double, count: Int) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("This Month", style = MaterialTheme.typography.labelMedium)
                Text(
                    "Total \$${"%.2f".format(monthTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Shows", style = MaterialTheme.typography.labelMedium)
                Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun UpcomingList(items: List<ShowEvent>) {
    if (items.isEmpty()) {
        Text("No upcoming shows yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { s ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("${s.date} • ${s.venue}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("\$${"%.0f".format(s.price)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/* ------------------------- Preview & 假数据 ------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
private val previewEvents = listOf(
    ShowEvent("1", "Hamilton", LocalDate.now().withDayOfMonth(8), "Richard Rodgers Theatre", 125.0),
    ShowEvent("2", "Wicked", LocalDate.now().withDayOfMonth(7), "Gershwin Theatre", 85.0),
    ShowEvent("3", "The Lion King", LocalDate.now().plusDays(10), "Minskoff Theatre", 79.0)
)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun CalendarScreenPreview() {
    MaterialTheme {
        CalendarScreen(
            events = previewEvents,
            onAddEvent = {},
            onSelectDay = {}
        )
    }
}
