package com.iptvhero.pro.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val HeroRed = Color(0xFFE62429)
val HeroBlue = Color(0xFF0476F2)
val DarkBackground = Color(0xFF0D0F14)
val DarkSurface = Color(0xFF161A23)
val DarkSurfaceVariant = Color(0xFF212736)
val TextPrimary = Color(0xFFF4F6FA)
val TextSecondary = Color(0xFFA0A8B8)
val StatusGreen = Color(0xFF22C55E)
val StatusAmber = Color(0xFFF59E0B)

private val HeroDarkColorScheme = darkColorScheme(
    primary = HeroRed,
    secondary = HeroBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun IPTVHeroProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeroDarkColorScheme,
        content = content
    )
}
