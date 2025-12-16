// File: app/src/main/java/com/example/cs501_fp/ui/navigation/NavGraph.kt
// The central Navigation Graph defining all screens, routes, and bottom bar logic

package com.example.cs501_fp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cs501_fp.ui.components.BottomNavBar
import com.example.cs501_fp.ui.components.ShowDetailScreen
import com.example.cs501_fp.ui.pages.auth.LoginScreen
import com.example.cs501_fp.ui.pages.auth.RegisterScreen
import com.example.cs501_fp.ui.pages.calendar.*
import com.example.cs501_fp.ui.pages.community.CommunityScreen
import com.example.cs501_fp.ui.pages.home.HomeScreen
import com.example.cs501_fp.ui.pages.profile.UserProfileScreen
import com.example.cs501_fp.ui.pages.profile.ProfileScreen
import com.example.cs501_fp.ui.pages.tickets.TicketScreen
import com.example.cs501_fp.viewmodel.CalendarViewModel
import com.example.cs501_fp.viewmodel.HomeViewModel
import com.example.cs501_fp.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    themeViewModel: ThemeViewModel
) {
    // --- Bottom Navigation Items ---
    val items = listOf(
        NavItem("Home", Icons.Filled.Home, "home"),
        NavItem("Calendar", Icons.Filled.DateRange, "calendar"),
        NavItem("Community", Icons.Filled.Forum, "community"),
        NavItem("Tickets", Icons.Filled.Theaters, "tickets")
    )

    // --- ViewModels ---
    val homeVM: HomeViewModel = viewModel()
    val calendarVM: CalendarViewModel = viewModel()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "home" else "login"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login" && currentRoute != "register"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = items,
                    currentDestination = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo("home") {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
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
            // --- Login & Register ---
            composable("login") {
                LoginScreen(navController = navController)
            }

            composable("register") {
                RegisterScreen(navController = navController)
            }


            // --- Home ---
            composable("home") {
                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) calendarVM.syncFromCloud()
                }
                HomeScreen(
                    viewModel = homeVM,
                    onShowClick = { show -> navController.navigate("detail/${show.id}") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            composable("detail/{showId}") { backStack ->
                val showId = backStack.arguments?.getString("showId") ?: ""
                ShowDetailScreen(showId = showId, onBack = { navController.popBackStack() })
            }


            // --- Calendar ---
            composable("calendar") {
                CalendarScreen(
                    viewModel = calendarVM,
                    navController = navController
                )
            }

            composable("add_event") {
                AddEventScreen(
                    onSave = { event ->
                        calendarVM.addEvent(event)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable("event_detail/{eventId}") { backStack ->
                val eventId = backStack.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    viewModel = calendarVM,
                    navController = navController
                )
            }

            composable("events_on_day/{dateText}") { backStack ->
                val dateText = backStack.arguments?.getString("dateText") ?: ""
                DayEventListScreen(
                    dateText = dateText,
                    viewModel = calendarVM,
                    navController = navController
                )
            }


            // --- Tickets ---
            composable("tickets") {
                TicketScreen(onProfileClick = { navController.navigate("profile") })
            }


            // --- Community & Personal Profile ---
            composable("community") {
                CommunityScreen(
                    onProfileClick = { navController.navigate("profile") },
                    onUserClick = { userId ->
                        navController.navigate("user_profile?userId=$userId")
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    onSignOut = {
                        homeVM.stopPreview()
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }


            // --- Public Profile ---
            composable(
                route = "user_profile?userId={userId}",
                arguments = listOf(
                    androidx.navigation.navArgument("userId") {
                        nullable = true
                        defaultValue = null
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                UserProfileScreen(
                    navController = navController,
                    userId = userId
                )
            }
        }
    }
}