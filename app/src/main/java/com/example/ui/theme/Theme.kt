package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==================== 1. MIDNIGHT BASS ====================
private val MidnightBassColorScheme = darkColorScheme(
    primary = MidnightBassPrimary,
    secondary = MidnightBassSecondary,
    tertiary = MidnightBassTertiary,
    background = MidnightBassBg,
    surface = MidnightBassCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = MidnightBassTextLight,
    onSurface = MidnightBassTextLight,
    onSurfaceVariant = MidnightBassTextMuted
)

// ==================== 2. COZY CAFE ====================
private val CozyCafeColorScheme = lightColorScheme(
    primary = CozyCafePrimary,
    secondary = CozyCafeSecondary,
    tertiary = CozyCafeTertiary,
    background = CozyCafeBg,
    surface = CozyCafeCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = CozyCafeTextDark,
    onSurface = CozyCafeTextDark,
    onSurfaceVariant = CozyCafeTextMuted
)

// ==================== 3. RAINY DAY ====================
private val RainyDayColorScheme = lightColorScheme(
    primary = RainyDayPrimary,
    secondary = RainyDaySecondary,
    tertiary = RainyDayTertiary,
    background = RainyDayBg,
    surface = RainyDayCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = RainyDayTextDark,
    onSurface = RainyDayTextDark,
    onSurfaceVariant = RainyDayTextMuted
)

// ==================== 4. RETRO VINYL ====================
private val RetroVinylColorScheme = darkColorScheme(
    primary = RetroVinylPrimary,
    secondary = RetroVinylSecondary,
    tertiary = RetroVinylTertiary,
    background = RetroVinylBg,
    surface = RetroVinylCardBg,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = RetroVinylTextLight,
    onSurface = RetroVinylTextLight,
    onSurfaceVariant = RetroVinylTextMuted
)

// ==================== 5. MATCHA LATTE ====================
private val MatchaLatteColorScheme = lightColorScheme(
    primary = MatchaLattePrimary,
    secondary = MatchaLatteSecondary,
    tertiary = MatchaLatteTertiary,
    background = MatchaLatteBg,
    surface = MatchaLatteCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = MatchaLatteTextDark,
    onSurface = MatchaLatteTextDark,
    onSurfaceVariant = MatchaLatteTextMuted
)

@Composable
fun MyApplicationTheme(
    animeTheme: String = "MIDNIGHT_BASS",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (animeTheme) {
        "MIDNIGHT_BASS" -> MidnightBassColorScheme
        "COZY_CAFE" -> CozyCafeColorScheme
        "RAINY_DAY" -> RainyDayColorScheme
        "RETRO_VINYL" -> RetroVinylColorScheme
        "MATCHA_LATTE" -> MatchaLatteColorScheme
        else -> if (darkTheme) MidnightBassColorScheme else CozyCafeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
