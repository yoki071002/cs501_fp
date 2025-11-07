package com.example.cs501_fp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.ui.auth.LoginScreen
import com.example.cs501_fp.ui.auth.LoginViewModel
import com.example.cs501_fp.ui.calendar.CalendarScreen
import com.example.cs501_fp.ui.calendar.CalendarViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val db = AppDatabase.getInstance(navController.context, "temp")
            val viewModel = LoginViewModel(db.userDao())
            LoginScreen(navController, viewModel)
        }

        composable("calendar/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val db = AppDatabase.getInstance(navController.context, userId)
            val calendarViewModel = CalendarViewModel(db.eventDao(), userId)
            CalendarScreen(userId, calendarViewModel)
        }
    }
}
