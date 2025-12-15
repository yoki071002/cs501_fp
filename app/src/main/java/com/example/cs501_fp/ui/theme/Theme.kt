// File: app/src/main/java/com/example/cs501_fp/ui/theme/Theme.kt
// Theme for the package

package com.example.cs501_fp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TheatreRed,
    onPrimary = TextWhite,
    primaryContainer = TheatreRedDark,
    onPrimaryContainer = TextWhite,

    secondary = Gold,
    onSecondary = TextBlack,
    secondaryContainer = GoldDim,

    background = MidnightBlack,
    surface = Charcoal,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = Charcoal
)

private val LightColorScheme = lightColorScheme(
    primary = TheatreRed,
    onPrimary = TextWhite,
    primaryContainer = TheatreRedLight,

    secondary = GoldDim,
    onSecondary = TextBlack,

    background = OffWhite,
    surface = PaperWhite,
    onBackground = TextBlack,
    onSurface = TextBlack,
    surfaceVariant = OffWhite
)

@Composable
fun _501_fpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
