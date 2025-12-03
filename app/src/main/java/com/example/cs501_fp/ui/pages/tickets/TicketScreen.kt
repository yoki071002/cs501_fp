package com.example.cs501_fp.ui.pages.tickets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.util.saveBitmapToInternalStorage
import com.example.cs501_fp.util.saveUriToInternalStorage
import com.example.cs501_fp.viewmodel.AnalyticsViewModel
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.MonthlyStat
import java.io.File

/** ------------------------ Ticket Main Screen ------------------------ */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    calendarViewModel: CalendarViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    onProfileClick: () -> Unit
) {
    // 0 = ticket wallet, 1 = Analytics
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Ticket Wallet") },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
                TicketToggle(
                    selectedIndex = selectedTabIndex,
                    onSelect = { selectedTabIndex = it }
                )
                HorizontalDivider()
            }
        }
    ) { inner ->
        Box(modifier = Modifier.padding(inner)) {
            if (selectedTabIndex == 0) {
                TicketListContent(calendarViewModel)
            } else {
                AnalyticsContent(analyticsViewModel)
            }
        }
    }
}

/** ------------------------ Segmented Toggle ------------------------ */
@Composable
fun TicketToggle(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleOption(text = "My Tickets", isSelected = selectedIndex == 0, modifier = Modifier.weight(1f), onClick = { onSelect(0) })
        ToggleOption(text = "Analytics", isSelected = selectedIndex == 1, modifier = Modifier.weight(1f), onClick = { onSelect(1) })
    }
}

@Composable
fun ToggleOption(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/** ------------------------ ANALYTICS CONTENT ------------------------ */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalyticsContent(viewModel: AnalyticsViewModel) {
    val budget by viewModel.monthlyBudget.collectAsState()
    val spent by viewModel.currentMonthSpending.collectAsState()
    val trends by viewModel.monthlyTrends.collectAsState()
    val lifetimeTotal by viewModel.lifetimeSpending.collectAsState()
    val avgPrice by viewModel.averageTicketPrice.collectAsState()
    var showBudgetDialog by remember { mutableStateOf(false) }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = budget,
            onDismiss = { showBudgetDialog = false },
            onSave = { viewModel.updateBudget(it); showBudgetDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Budget Monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        BudgetCard(
            budget = budget,
            spent = spent,
            onEditClick = { showBudgetDialog = true }
        )

        Text("6-Month Spending Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TrendChartCard(trends = trends)

        Text("Key Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                title = "Lifetime Spent",
                value = "\$${"%.0f".format(lifetimeTotal)}",
                icon = Icons.Default.Savings,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Avg Ticket Price",
                value = "\$${"%.0f".format(avgPrice)}",
                icon = Icons.Default.Analytics,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(60.dp))
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BudgetCard(budget: Double, spent: Double, onEditClick: () -> Unit) {
    val ratio = (spent / budget).toFloat().coerceIn(0f, 1.5f)
    val animatedRatio by animateFloatAsState(targetValue = ratio, label = "Progress")

    val progressColor = getDynamicProgressColor(animatedRatio)
    val animatedColor by animateColorAsState(targetValue = progressColor, label = "Color")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("This Month", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Numbers
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "\$${"%.0f".format(spent)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
                Text(
                    " / \$${"%.0f".format(budget)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRatio.coerceAtMost(1f))
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF81C784),
                                    animatedColor
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            val remaining = budget - spent
            if (remaining >= 0) {
                Text(
                    "\$${"%.0f".format(remaining)} remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    "Over budget by \$${"%.0f".format(-remaining)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getDynamicProgressColor(ratio: Float): Color {
    return when {
        ratio < 0.5f -> {
            androidx.compose.ui.graphics.lerp(
                Color(0xFF66BB6A),
                Color(0xFFFFCA28),
                ratio * 2
            )
        }
        ratio < 1.0f -> {
            androidx.compose.ui.graphics.lerp(
                Color(0xFFFFCA28),
                Color(0xFFEF5350),
                (ratio - 0.5f) * 2
            )
        }
        else -> Color(0xFFB71C1C)
    }
}

@Composable
fun TrendChartCard(trends: List<MonthlyStat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        if (trends.isEmpty() || trends.all { it.totalSpent == 0.0 }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No spending data yet.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Column(Modifier.padding(24.dp)) {
                val maxVal = trends.maxOfOrNull { it.totalSpent } ?: 1.0
                val colorTop = MaterialTheme.colorScheme.primary
                val colorBottom = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (trends.size * 2.5f)
                    val space = size.width / trends.size
                    val chartHeight = size.height * 0.85f

                    trends.forEachIndexed { index, stat ->
                        val barHeight = (stat.totalSpent / maxVal * chartHeight).toFloat()
                        val x = index * space + (space - barWidth) / 2
                        val y = chartHeight - barHeight

                        if (stat.totalSpent > 0) {
                            val gradientBrush = Brush.verticalGradient(
                                colors = listOf(colorTop, colorBottom),
                                startY = y,
                                endY = chartHeight
                            )

                            drawRoundRect(
                                brush = gradientBrush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )

                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    "${stat.totalSpent.toInt()}",
                                    x + barWidth / 2,
                                    y - 15f,
                                    android.graphics.Paint().apply {
                                        color = textColor
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = 32f
                                        isFakeBoldText = true
                                    }
                                )
                            }
                        }

                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                stat.monthLabel,
                                x + barWidth / 2,
                                size.height,
                                android.graphics.Paint().apply {
                                    color = textColor
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    textSize = 34f
                                    alpha = 160
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetBudgetDialog(currentBudget: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var text by remember { mutableStateOf(currentBudget.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text.toDoubleOrNull() ?: currentBudget) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** ------------------------ TICKET LIST CONTENT ------------------------ */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketListContent(viewModel: CalendarViewModel) {
    val rawEvents by viewModel.events.collectAsState(initial = emptyList())
    val events = remember(rawEvents) { rawEvents.sortedByDescending { it.dateText } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Tap a ticket to flip for details & notes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (events.isEmpty()) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No tickets yet. Add shows from the Calendar!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(events) { event -> FlipTicketCard(event, viewModel) }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

/** ------------------------ FLIP CARD COMPONENTS ------------------------ */
@Composable
fun FlipTicketCard(event: UserEvent, viewModel: CalendarViewModel) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "FlipAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { rotated = !rotated }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            TicketFront(event, viewModel)
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                TicketBack(event, viewModel)
            }
        }
    }
}

@Composable
fun TicketFront(event: UserEvent, viewModel: CalendarViewModel) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showGalleryDialog by remember { mutableStateOf(false) }
    val bgImage = event.officialImageUrl ?: event.userImageUris.firstOrNull()

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            saveBitmapToInternalStorage(context, bitmap)?.let { path ->
                val newList = event.userImageUris + path
                viewModel.updateEvent(event.copy(userImageUris = newList))
                Toast.makeText(context, "Photo Added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            saveUriToInternalStorage(context, uri)?.let { path ->
                val newList = event.userImageUris + path
                viewModel.updateEvent(event.copy(userImageUris = newList))
                Toast.makeText(context, "Photo Added!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Photo / Stub") },
            text = { Text("Choose source:") },
            confirmButton = {},
            dismissButton = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) { Text("Camera") }

                    TextButton(onClick = { showImageSourceDialog = false; galleryLauncher.launch("image/*") }) { Text("Gallery") }
                    TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel") }
                }
            }
        )
    }
    if (showGalleryDialog && event.userImageUris.isNotEmpty()) {
        Dialog(onDismissRequest = { showGalleryDialog = false }) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("My Stubs & Memories", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(event.userImageUris) { path ->
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { showGalleryDialog = false }, modifier = Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bgImage != null) {
                AsyncImage(
                    model = if (bgImage.startsWith("/")) File(bgImage) else bgImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    ))
            } else {
                Box(Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary))
            }
            Column(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(end = 48.dp)) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${event.dateText} @ ${event.timeText}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Text(event.venue, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            Row(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showImageSourceDialog = true }) { Icon(Icons.Default.AddAPhoto, "Add Photo", tint = Color.White) }
                if (event.userImageUris.isNotEmpty()) {
                    IconButton(onClick = { showGalleryDialog = true }) {
                        BadgedBox(badge = { Badge { Text(event.userImageUris.size.toString()) } }) { Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White) }
                    }
                }
            }
        }
    }
}

/** ------------------------ Back Side (Details & Notes) ------------------------ */
@Composable
fun TicketBack(event: UserEvent, viewModel: CalendarViewModel) {
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket") },
            text = { Text("Remove this ticket from wallet?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(event); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditNotesDialog) {
        EditNotesDialog(
            initialNotes = event.notes,
            onDismiss = { showEditNotesDialog = false },
            onSave = { newNotes ->
                viewModel.updateEvent(event.copy(notes = newNotes))
                showEditNotesDialog = false
            }
        )
    }

    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text(if (event.isPublic) "Make Private?" else "Post to Community?") },
            text = {
                Text(if (event.isPublic)
                    "This will remove the review from the public feed."
                else "This will share your ticket and notes to the Community feed. Your exact seat number will be hidden.")
            },
            confirmButton = {
                Button(onClick = {
                    val newStatus = !event.isPublic
                    viewModel.updateEvent(event.copy(isPublic = newStatus))
                    showPublishDialog = false
                    Toast.makeText(context, if(newStatus) "Posted!" else "Hidden!", Toast.LENGTH_SHORT).show()
                }) {
                    Text(if (event.isPublic) "Make Private" else "Post")
                }
            },
            dismissButton = { TextButton(onClick = { showPublishDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    DetailItem(label = "Date", value = event.dateText)
                    DetailItem(label = "Time", value = event.timeText)
                }
                Column(Modifier.weight(1f)) {
                    DetailItem(label = "Seat", value = event.seat)
                    DetailItem(label = "Price", value = "\$${"%.2f".format(event.price)}")
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Repo / Notes", style = MaterialTheme.typography.labelLarge)

                Row {
                    IconButton(onClick = { showPublishDialog = true }) {
                        Icon(
                            imageVector = if (event.isPublic) Icons.Default.Public else Icons.Default.PublicOff,
                            contentDescription = "Post to Community",
                            tint = if (event.isPublic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showEditNotesDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .clickable { showEditNotesDialog = true }
                    .padding(8.dp)
            ) {
                if (event.notes.isBlank()) {
                    Text(
                        text = "Tap to write your review...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** ------------------------ Edit Notes Dialog ------------------------ */
@Composable
fun EditNotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    "Edit Repo / Notes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    placeholder = { Text("How was the show? Write your memories here...") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(notes) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
