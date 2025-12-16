// File: app/src/main/java/com/example/cs501_fp/ui/pages/profile/ProfileScreen.kt
// The Settings/Profile Hub screen allowing users to toggle themes, clear cache, and sign out

package com.example.cs501_fp.ui.pages.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.cs501_fp.ui.components.OnCoreButton
import com.example.cs501_fp.ui.components.OnCoreCard
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
import com.example.cs501_fp.viewmodel.ProfileViewModel
import com.example.cs501_fp.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    viewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    fun clearImageCache() {
        val imageLoader = context.imageLoader
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
        Toast.makeText(context, "Image cache cleared!", Toast.LENGTH_SHORT).show()
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About OnCore") },
            text = {
                Column {
                    Text("Version: 1.0.0")
                    Spacer(Modifier.height(8.dp))
                    Text("Developed for CS501 Final Project.")
                    Text("By: Yoki, Sophie, Nana")
                }
            },
            confirmButton = {
                OnCoreButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Last updated: Dec 2025\n\n" +
                                "1. Data Collection\n" +
                                "We collect your email and username to provide account services. Your ticket data is stored locally and synced to the cloud for backup.\n\n" +
                                "2. Usage\n" +
                                "We do not sell your personal data to third parties. Your data is used solely for the functionality of the OnCore app.\n\n" +
                                "3. Security\n" +
                                "We take reasonable measures to protect your information, but cannot guarantee absolute security.\n\n" +
                                "4. Contact\n" +
                                "If you have any questions, please contact the development team.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                OnCoreButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }

    // Settings list layout
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TheatricalTopBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TicketInkColor)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OnCoreCard(
                onClick = { navController.navigate("user_profile") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profile.avatarUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(profile.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.username.take(1).uppercase().ifBlank { "?" },
                                fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            profile.username.ifBlank { "User" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "View and edit your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SettingsSection(title = "Preferences") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { themeViewModel.toggleTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Data & Storage") {
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear Image Cache",
                    onClick = { clearImageCache() }
                )
            }

            SettingsSection(title = "Support") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About OnCore",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    onClick = {
                        showPrivacyDialog = true
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.clearLocalData()
                    onSignOut()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}


// --- Helper Components ---
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        OnCoreCard {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = {
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = Color.Gray
        )
    },
) {
    ListItem(
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        trailingContent = trailing
    )
}
