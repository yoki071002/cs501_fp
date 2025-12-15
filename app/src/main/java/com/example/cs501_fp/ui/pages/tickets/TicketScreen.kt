// File: app/src/main/java/com/example/cs501_fp/ui/pages/tickets/TicketScreen.kt
// The main screen for the Tickets tab, managing the Ticket Wallet and Analytics Dashboard

package com.example.cs501_fp.ui.pages.tickets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import coil.request.ImageRequest
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.ui.components.OnCoreButton
import com.example.cs501_fp.util.saveBitmapToInternalStorage
import com.example.cs501_fp.util.saveUriToInternalStorage
import com.example.cs501_fp.viewmodel.AnalyticsViewModel
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.MonthlyStat
import com.example.cs501_fp.ui.theme.Gold
import com.example.cs501_fp.ui.theme.GoldDim
import java.io.File


// --- Main Screen & Toggle ---
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
                    title = {
                        Text(
                            "Ticket Wallet",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    },
                    windowInsets = WindowInsets.statusBars,
                    modifier = Modifier.heightIn(max = 64.dp)
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


// --- Analytics Dashboard ---
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
        Text("Budget Monitor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        BudgetCard(
            budget = budget,
            spent = spent,
            onEditClick = { showBudgetDialog = true }
        )

        Text("6-Month Spending Trend", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        TrendChartCard(trends = trends)

        Text("Key Metrics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                title = "Lifetime Spent",
                value = "$${"%.0f".format(lifetimeTotal)}",
                icon = Icons.Default.Savings,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Avg Ticket Price",
                value = "$${"%.0f".format(avgPrice)}",
                icon = Icons.Default.Analytics,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(60.dp))
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
            OnCoreButton(onClick = { onSave(text.toDoubleOrNull() ?: currentBudget) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


// -- Analytics Visualizations ---
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
                    "$${"%.0f".format(spent)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
                Text(
                    " / $${"%.0f".format(budget)}",
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
                    "$${"%.0f".format(remaining)} remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    "Over budget by $${"%.0f".format(-remaining)}",
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


// --- Ticket List ---
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
            items(
                items = events,
                key = { event -> event.id }
            ) { event ->
                FlipTicketCard(event, viewModel)
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}


// --- Flip & Ticket Details
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
                        items(
                            items = event.userImageUris,
                            key = { path -> path }
                        ) { path ->
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
                    OnCoreButton(onClick = { showGalleryDialog = false }, modifier = Modifier.align(Alignment.End)) { Text("Close") }
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
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bgImage)
                        .crossfade(true)
                        .size(800, 800)
                        .build(),
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

@Composable
fun TicketBack(event: UserEvent, viewModel: CalendarViewModel) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditRepoDialog by remember { mutableStateOf(false) }

    var currentPage by remember { mutableIntStateOf(0) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket") },
            text = { Text("Remove this ticket from wallet? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteEvent(event); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditRepoDialog) {
        EditRepoDialog(
            initialNotes = event.notes,
            initialPublicReview = event.publicReview,
            initialIsPublic = event.isPublic,
            onDismiss = { showEditRepoDialog = false },
            onSave = { notes, review, isPublic ->
                val updatedEvent = event.copy(
                    notes = notes,
                    publicReview = review,
                    isPublic = isPublic
                )
                viewModel.updateEvent(updatedEvent)
                if (isPublic && !event.isPublic) {
                    Toast.makeText(context, "Posted to Community!", Toast.LENGTH_SHORT).show()
                } else if (!isPublic && event.isPublic) {
                    Toast.makeText(context, "Removed from Community", Toast.LENGTH_SHORT).show()
                }
                showEditRepoDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
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
                    text = if (currentPage == 0) "Ticket Details" else "Your Repo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentPage == 1) {
                        IconButton(onClick = { showEditRepoDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                        Spacer(Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { currentPage = 0 },
                        enabled = currentPage == 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Prev",
                            tint = if (currentPage == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    IconButton(
                        onClick = { currentPage = 1 },
                        enabled = currentPage == 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = if (currentPage == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.weight(1f)) {

                if (currentPage == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) { DetailItem("Date", event.dateText) }
                            Box(Modifier.weight(1f)) { DetailItem("Time", event.timeText) }
                        }
                        DetailItem("Venue", event.venue)
                        Row(Modifier.fillMaxWidth()) {
                            Box(Modifier.weight(1f)) { DetailItem("Seat", event.seat.ifBlank { "GA" }) }
                            Box(Modifier.weight(1f)) { DetailItem("Price", "$${"%.2f".format(event.price)}") }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (event.notes.isBlank() && event.publicReview.isBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Tap edit to write a review!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            if (event.publicReview.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Public, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Public Review", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(event.publicReview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }

                            if (event.notes.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, null, Modifier.size(14.dp), tint = GoldDim)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Private Notes", style = MaterialTheme.typography.labelSmall, color = GoldDim, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(event.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            if (event.isPublic) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Posted to Community", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
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


// --- Edit Repo ---
@Composable
fun EditRepoDialog(
    initialNotes: String,
    initialPublicReview: String,
    initialIsPublic: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }
    var publicReview by remember { mutableStateOf(initialPublicReview) }
    var isPublic by remember { mutableStateOf(initialIsPublic) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Your Repo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Private Notes") },
                    placeholder = { Text("Only visible to you...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        if (notes.isNotBlank()) publicReview = notes
                    }) {
                        Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy to Review")
                    }
                }

                OutlinedTextField(
                    value = publicReview,
                    onValueChange = { publicReview = it },
                    label = { Text("Public Review") },
                    placeholder = { Text("Share with community...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Post to Community?", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (isPublic) "Visible in Stage Door" else "Private only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }
            }
        },
        confirmButton = {
            OnCoreButton(onClick = { onSave(notes, publicReview, isPublic) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
