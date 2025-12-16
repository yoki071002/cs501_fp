// File: app/src/main/java/com/example/cs501_fp/ui/pages/auth/LoginScreen.kt
// The initial authentication screen allowing users to sign in via Firebase

package com.example.cs501_fp.ui.pages.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.firebase.FirebaseAuthManager
import com.example.cs501_fp.ui.components.OnCoreButton
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { FirebaseAuthManager() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showWelcomeAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(showWelcomeAnimation) {
        if (showWelcomeAnimation) {
            delay(1500)
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    fun handleLogin() {
        if (email.isNotBlank() && password.isNotBlank()) {
            isLoading = true
            errorMessage = null

            authManager.login(email, password) { success, msg ->
                isLoading = false
                if (success) {
                    showWelcomeAnimation = true
                } else {
                    errorMessage = msg ?: "Authentication failed"
                }
            }
        } else {
            errorMessage = "Please enter email and password"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // --- Brand Logo ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OnCore",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 64.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your Front Row Seat",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- Input Fields ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Action Buttons ---
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                OnCoreButton(onClick = { handleLogin() }) {
                    Text("Enter the Theatre")
                }

                // Register Link
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        "New here? Join the Cast",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showWelcomeAnimation,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF5A0000)
                            ),
                            radius = 1200f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "The show is about to start...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
