package com.example.cs501_fp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CalendarToday
//import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cs501_fp.ui.pages.calendar.CalendarScreen
import com.example.cs501_fp.ui.pages.home.HomeScreen

import com.example.cs501_fp.ui.components.BottomNavBar
import com.example.cs501_fp.ui.components.ShowDetailScreen
import com.example.cs501_fp.ui.pages.profile.ProfileScreen
import com.example.cs501_fp.ui.pages.tickets.TicketScreen
import com.example.cs501_fp.viewmodel.HomeViewModel

private val filled: Any
    get() {
        TODO()
    }

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val items = listOf(
        NavItem("Home",        Icons.Filled.Home,               "home"),
        NavItem("My Calendar", Icons.Filled.Home,  "calendar"),   // TEMP use Home
        NavItem("Tickets",   Icons.Filled.Person, "tickets"),     // TEMP use Person
        NavItem("Profile",     Icons.Filled.Person,             "profile")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = items,
                currentDestination = currentRoute,
                onItemClick = { selectedRoute: String ->
                    if (selectedRoute != currentRoute) {
                        navController.navigate(selectedRoute) {
                            launchSingleTop = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(inner)
        ) {
            composable("home")     {
                val homeVM: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeVM,
                    onShowClick = { show ->
                        navController.navigate("detail/${show.id}")
                    },
                    onListenClick = { pick ->
                        val mediaPlayer = android.media.MediaPlayer().apply {
                            setDataSource(pick.id)
                            prepare()
                            start()
                        }
                    }
                )
            }
            composable("calendar") { CalendarScreen() }
            composable("tickets")  { TicketScreen() }
            composable("profile")  { ProfileScreen() }

            composable("detail/{showId}") { backStack ->
                val id = backStack.arguments?.getString("showId") ?: ""
                ShowDetailScreen(showId = id)
            }
        }
    }
}
