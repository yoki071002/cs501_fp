package com.example.cs501_fp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cs501_fp.ui.theme.Gold
import com.example.cs501_fp.ui.theme.TicketInkColor

@Composable
fun TheatricalTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            ArtDecoCorner(modifier = Modifier.align(Alignment.TopStart))
            ArtDecoCorner(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer(scaleX = -1f)
            )

            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                navigationIcon()
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                color = TicketInkColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }

        HorizontalDivider(
            color = Gold.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun ArtDecoCorner(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(40.dp)) {
        val strokeWidth = 1.dp.toPx()
        val color = Gold.copy(alpha = 0.3f)

        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(-10.dp.toPx(), -10.dp.toPx()),
            size = Size(size.width, size.height),
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
    }
}
