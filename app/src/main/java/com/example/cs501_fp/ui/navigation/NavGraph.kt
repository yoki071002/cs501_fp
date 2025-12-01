package com.example.cs501_fp.ui.navigation

import com.google.firebase.auth.FirebaseAuth
import com.example.cs501_fp.ui.pages.auth.LoginScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs501_fp.ui.components.BottomNavBar
import com.example.cs501_fp.ui.components.ShowDetailScreen
import com.example.cs501_fp.ui.pages.calendar.*
import com.example.cs501_fp.ui.pages.home.HomeScreen
import com.example.cs501_fp.ui.pages.profile.ProfileScreen
import com.example.cs501_fp.ui.pages.tickets.TicketScreen
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.HomeViewModel

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {

    /** ----------- Bottom Navigation Items ----------- */
    val items = listOf(
        NavItem("Home", Icons.Filled.Home, "home"),
        NavItem("My Calendar", Icons.Filled.DateRange, "calendar"),
        NavItem("Tickets", Icons.Filled.Theaters, "tickets"),
        NavItem("Profile", Icons.Filled.Person, "profile")
    )

    /** ----------- ViewModels ----------- */
    val homeVM: HomeViewModel = viewModel()
    val calendarVM: CalendarViewModel = viewModel()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "home" else "login"
    val currentRoute = navController.currentDestination?.route
    val showBottomBar = currentRoute != "login"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = items,
                    currentDestination = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("home") {
                                saveState = true
                            }
                        }
                    }
                )
            }
        }
    ) { inner ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(inner)
        ) {
            /** ---------------- LOGIN ---------------- */
            composable("login") {
                LoginScreen(navController = navController)
            }

            /** ---------------- HOME ---------------- */
            composable("home") {
                HomeScreen(
                    viewModel = homeVM,
                    onShowClick = { show ->
                        navController.navigate("detail/${show.id}")
                    }
                )
            }

            composable("detail/{showId}") { backStack ->
                val showId = backStack.arguments?.getString("showId") ?: ""
                ShowDetailScreen(showId = showId, onBack = { navController.popBackStack() })
            }

            /** ---------------- CALENDAR ---------------- */
            composable("calendar") {
                CalendarScreen(
                    viewModel = calendarVM,
                    navController = navController
                )
            }

            /** Add new event */
            composable("add_event") {
                AddEventScreen(
                    onSave = { event ->
                        calendarVM.addEvent(event)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            /** Single event detail */
            composable("event_detail/{eventId}") { backStack ->
                val eventId = backStack.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    viewModel = calendarVM,
                    navController = navController
                )
            }

            /** Multiple events on same day */
            composable("events_on_day/{dateText}") { backStack ->
                val dateText = backStack.arguments?.getString("dateText") ?: ""
                DayEventListScreen(
                    dateText = dateText,
                    viewModel = calendarVM,
                    navController = navController
                )
            }

            /** ---------------- TICKETS ---------------- */
            composable("tickets") {
                TicketScreen()
            }

            /** ---------------- PROFILE ---------------- */
            composable("profile") {
                ProfileScreen(navController = navController)
            }
        }
    }
}