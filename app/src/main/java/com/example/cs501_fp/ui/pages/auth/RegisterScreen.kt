// File: app/src/main/java/com/example/cs501_fp/ui/pages/auth/RegisterScreen.kt
// The registration screen for creating new accounts and initializing user profiles

package com.example.cs501_fp.ui.pages.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.firebase.FirebaseAuthManager
import com.example.cs501_fp.ui.components.OnCoreButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authManager = remember { FirebaseAuthManager() }
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // --- Header ---
            Text(
                text = "Join the Cast",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Create your profile to start tracking shows.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // --- Inputs ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Stage Name (Username)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

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

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Action Buttons ---
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                OnCoreButton(
                    onClick = {
                        if (username.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMsg = "Please fill in all fields"
                            return@OnCoreButton
                        }
                        isLoading = true
                        authManager.register(email, password) { success, message ->
                            if (success) {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                if (uid != null) {
                                    val userMap = hashMapOf(
                                        "username" to username,
                                        "email" to email,
                                        "bio" to "New to the stage!",
                                        "avatarUrl" to null
                                    )
                                    db.collection("users").document(uid).set(userMap)
                                }

                                Toast.makeText(context, "Welcome, $username!", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                isLoading = false
                                errorMsg = message
                            }
                        }
                    }
                ) {
                    Text("Sign Up")
                }

                // Back to Login
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        "Already have a role? Log In",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
