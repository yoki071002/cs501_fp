package com.example.cs501_fp.ui.pages.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cs501_fp.data.firebase.FirebaseAuthManager

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { FirebaseAuthManager() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onAuthSuccess() {
        isLoading = false
        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
        navController.navigate("home") {
            popUpTo("login") { inclusive = true }
        }
    }

    fun onAuthError(msg: String?) {
        isLoading = false
        errorMessage = msg ?: "Authentication failed"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MusicNY", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Sign in to sync your tickets", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        authManager.login(email, password) { success, msg ->
                            if (success) onAuthSuccess() else onAuthError(msg)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(Modifier.height(16.dp))

            // Register Button
            OutlinedButton(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        authManager.register(email, password) { success, msg ->
                            if (success) onAuthSuccess() else onAuthError(msg)
                        }
                    } else {
                        errorMessage = "Please enter email & password to register"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register / Sign Up")
            }
        }
    }
}
