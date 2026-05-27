package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonCyan,
    tertiary = NeonPurple,
    background = CyberBg,
    surface = CyberCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = CyberTextLight,
    onSurface = CyberTextLight,
    onSurfaceVariant = CyberTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = SakuraPink,
    secondary = PeachCaramel,
    tertiary = SoftBlossom,
    background = WarmBlossomBg,
    surface = SoftCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextGray
)

private val MikuColorScheme = darkColorScheme(
    primary = MikuPrimary,
    secondary = MikuSecondary,
    tertiary = Color(0xFF00FFCC),
    background = MikuBackground,
    surface = MikuCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0F7FA),
    onSurface = Color(0xFFE0F7FA),
    onSurfaceVariant = Color(0xFF80DEEA)
)

private val SunshineColorScheme = darkColorScheme(
    primary = SunshinePrimary,
    secondary = SunshineSecondary,
    tertiary = Color(0xFFFFA500),
    background = SunshineBackground,
    surface = SunshineCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFFFF3E0),
    onSurface = Color(0xFFFFF3E0),
    onSurfaceVariant = Color(0xFFFFB74D)
)

private val EvaColorScheme = darkColorScheme(
    primary = EvaPrimary,
    secondary = EvaSecondary,
    tertiary = Color(0xFF8A2BE2),
    background = EvaBackground,
    surface = EvaCardBg,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF3E5F5),
    onSurface = Color(0xFFF3E5F5),
    onSurfaceVariant = Color(0xFFB39DDB)
)

@Composable
fun MyApplicationTheme(
    animeTheme: String = "TOKYO_NIGHT",
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to enforce the gorgeous hand-coded Japanese anime palettes!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (animeTheme) {
        "TOKYO_NIGHT" -> DarkColorScheme
        "SAKURA_BLOSSOM" -> LightColorScheme
        "MIKU_TEAL" -> MikuColorScheme
        "SUNSHINE_ORANGE" -> SunshineColorScheme
        "EVA_UNIT_01" -> EvaColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
