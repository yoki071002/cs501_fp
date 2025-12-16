// File: app/src/main/java/com/example/cs501_fp/ui/pages/profile/UserProfileScreen.kt
// Displays a public or private user profile with editing capabilities (Bio, Avatar, Favorites)

package com.example.cs501_fp.ui.pages.profile

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cs501_fp.ui.components.OnCoreButton
import com.example.cs501_fp.ui.components.OnCoreCard
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.ProfileViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    userId: String? = null,
    viewModel: ProfileViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isCurrentUser = viewModel.isCurrentUser(userId)
    val myEvents by calendarViewModel.events.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }

    var isEditing by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editFavs by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            editUsername = profile.username
            editBio = profile.bio
            editFavs = profile.favoriteShows
        }
    }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.updateAvatar(uri)
    }

    Scaffold(
        containerColor = TicketPaperColor,
        topBar = {
            TheatricalTopBar(
                title = profile.username.ifBlank { "Profile" },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TicketInkColor
                        )
                    }
                }
            )
        }
    ) { inner ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
            ) {
                // --- Profile Header ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (profile.avatarUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profile.avatarUrl)
                                    .crossfade(true)
                                    .size(400, 400)
                                    .build(),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.username.take(1).uppercase().ifBlank { "?" },
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isCurrentUser && !isEditing) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 4.dp, y = 4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { avatarLauncher.launch("image/*") }
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isEditing) {
                        // Editing Form
                        OutlinedTextField(
                            value = editUsername, onValueChange = { editUsername = it },
                            label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editBio, onValueChange = { editBio = it },
                            label = { Text("Bio") }, minLines = 2, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editFavs, onValueChange = { editFavs = it },
                            label = { Text("Favorites (comma separated)") }, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                            OnCoreButton(
                                onClick = {
                                    viewModel.updateProfile(editUsername, editBio, editFavs)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                        }
                    } else {
                        // Display Info
                        Text(
                            text = profile.username.ifBlank { "User" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (profile.bio.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = profile.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        // Favorites Tags
                        if (profile.favoriteShows.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                profile.favoriteShows.split(",").forEach {
                                    if (it.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                text = it.trim(),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        if (isCurrentUser) {
                            Button(
                                onClick = { isEditing = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Edit Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- Tabs ---
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.List, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Reviews")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GridOn, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Stage")
                            }
                        }
                    )
                }

                // --- Content Area ---
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        val reviews = myEvents.filter { it.publicReview.isNotBlank() || it.notes.isNotBlank() }

                        if (reviews.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No reviews written yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(reviews) { event ->
                                    OnCoreCard {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(event.venue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.height(8.dp))

                                            if (event.publicReview.isNotBlank()) {
                                                Text(event.publicReview, style = MaterialTheme.typography.bodyMedium)
                                            } else {
                                                Text(event.notes, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            }

                                            Spacer(Modifier.height(8.dp))
                                            Text(event.dateText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val eventsWithImages = myEvents.filter { !it.officialImageUrl.isNullOrBlank() || it.userImageUris.isNotEmpty() }

                        if (eventsWithImages.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No posters to display.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(eventsWithImages) { event ->
                                    val img = event.officialImageUrl ?: event.userImageUris.firstOrNull()
                                    if (img != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(img)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = event.title,
                                            modifier = Modifier
                                                .aspectRatio(0.7f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
