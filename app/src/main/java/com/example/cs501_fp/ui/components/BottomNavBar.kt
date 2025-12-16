// File: app/src/main/java/com/example/cs501_fp/ui/components/BottomNavBar.kt

package com.example.cs501_fp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.ui.navigation.NavItem
import com.example.cs501_fp.ui.theme.Gold
import com.example.cs501_fp.ui.theme.TheatreRedDark

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    currentDestination: String?,
    onItemClick: (String) -> Unit,
) {
    val footerGradient = Brush.verticalGradient(
        colors = listOf(
            TheatreRedDark,
            Color(0xFF2A0000)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(footerGradient)
    ) {
        HorizontalDivider(
            color = Gold,
            thickness = 2.dp
        )

        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Gold,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentDestination == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) Gold else Gold.copy(alpha = 0.4f)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Gold else Gold.copy(alpha = 0.4f)
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        unselectedIconColor = Gold.copy(alpha = 0.4f),
                        unselectedTextColor = Gold.copy(alpha = 0.4f)
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}
