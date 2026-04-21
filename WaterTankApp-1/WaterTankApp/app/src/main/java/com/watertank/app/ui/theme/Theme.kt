package com.watertank.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColors = darkColorScheme(
    primary = WaterMid,
    onPrimary = BgDeep,
    primaryContainer = WaterDeep,
    onPrimaryContainer = TextPrimary,
    secondary = PipeBlue,
    onSecondary = TextPrimary,
    tertiary = GasYellow,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = TextPrimary,
    outline = StrokeSubtle
)

@Composable
fun WaterTankTheme(
    darkTheme: Boolean = true, // always dark - industrial feel
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDeep.toArgb()
            window.navigationBarColor = BgDeep.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        content = content
    )
}
