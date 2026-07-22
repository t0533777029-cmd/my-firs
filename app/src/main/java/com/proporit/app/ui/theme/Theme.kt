package com.proporit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

private val ProporitDarkColors = darkColorScheme(
    primary = Blue400,
    onPrimary = Navy950,
    secondary = Gold400,
    onSecondary = GoldDeep,
    background = Navy950,
    onBackground = Ink100,
    surface = Indigo800,
    onSurface = Ink100,
    error = Red400,
)

// The signature background gradient used across every screen —
// deep navy base with soft blue + cyan glow, matching the approved mockup.
fun proporitBackgroundBrush(): Brush = Brush.radialGradient(
    colors = listOf(Indigo700.copy(alpha = 0.55f), Navy950.copy(alpha = 0f)),
    center = Offset(0.15f, -0.1f),
    radius = 900f
)

val cardBrush: Brush
    get() = Brush.linearGradient(listOf(CardFill, CardFill))

@Composable
fun ProporitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProporitDarkColors,
        typography = Typography,
        content = content
    )
}
