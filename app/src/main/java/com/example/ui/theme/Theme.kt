package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SoftPurpleDark,
    secondary = SecondaryTealDark,
    tertiary = AccentCoralDark,
    background = CharcoalStoneBg,
    surface = GroundStoneSurface,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE2E1D9),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E1D9)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLinenPurple,
    secondary = SecondaryForestGreen,
    tertiary = AccentClayTerracotta,
    background = NaturalLinenBg,
    surface = FrostedWhiteSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1E293B), // slate-800
    onSurface = androidx.compose.ui.graphics.Color(0xFF334155)    // slate-700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our beautiful hand-crafted branding!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
