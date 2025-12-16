// File: app/src/main/java/com/example/cs501_fp/ui/components/TheatricalTopBar.kt

package com.example.cs501_fp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.ui.theme.Gold
import com.example.cs501_fp.ui.theme.TheatreRedDark

@Composable
fun TheatricalTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val velvetGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF750000),
            TheatreRedDark
        )
    )

    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0xFF3E0000).copy(alpha = 0.6f),
                ambientColor = Color(0xFF3E0000).copy(alpha = 0.6f)
            )
            .background(velvetGradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            SpotlightDecoration(modifier = Modifier.fillMaxSize())

            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                CompositionLocalProvider(LocalContentColor provides Gold) {
                    navigationIcon()
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                color = Gold,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides Gold) {
                    actions()
                }
            }
        }

        HorizontalDivider(
            color = Gold,
            thickness = 2.dp
        )
    }
}

@Composable
private fun SpotlightDecoration(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Gold.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(0f, 0f),
                radius = 120.dp.toPx()
            ),
            center = Offset(0f, 0f),
            radius = 120.dp.toPx()
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Gold.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(width, 0f),
                radius = 120.dp.toPx()
            ),
            center = Offset(width, 0f),
            radius = 120.dp.toPx()
        )
    }
}
