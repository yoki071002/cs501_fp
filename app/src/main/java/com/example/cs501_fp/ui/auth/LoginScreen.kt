package com.example.cs501_fp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.login(email, password,
                    onSuccess = { uid ->
                        navController.navigate("calendar/$uid") { popUpTo("login") { inclusive = true } }
                    },
                    onError = { msg -> println(msg) }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}