package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZXDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF002029),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = CyanAccent,
    secondary = CyanVariant,
    onSecondary = Color(0xFF001E30),
    secondaryContainer = Color(0xFF0B334B),
    onSecondaryContainer = Color(0xFFBBE9FF),
    tertiary = StatusInfo,
    onTertiary = Color.White,
    background = BackgroundBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun ZXStoreTheme(
    darkTheme: Boolean = true, // Premium Dark by default as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZXDarkColorScheme,
        typography = Typography,
        content = content
    )
}
