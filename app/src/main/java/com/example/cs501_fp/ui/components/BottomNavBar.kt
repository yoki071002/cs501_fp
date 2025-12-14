// File: app/src/main/java/com/example/cs501_fp/ui/components/BottomNavBar.kt
// A reusable UI component for the bottom navigation bar

package com.example.cs501_fp.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.cs501_fp.ui.navigation.NavItem

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    currentDestination: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = false
            )
        }
    }
}
