package com.fretpitch.presentation.theme

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
    primary = AccentBlue,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnBlueContainer,
    secondary = AccentAmber,
    tertiary = AccentGreen,
    tertiaryContainer = GreenContainer,
    onTertiaryContainer = OnGreenContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color(0xFF003258),
    onSecondary = Color(0xFF432B00),
    onTertiary = Color(0xFF0D3900),
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = AccentRed,
    errorContainer = RedContainer,
    onError = Color(0xFF690005),
    onErrorContainer = OnRedContainer
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    primaryContainer = BlueContainer,
    onPrimaryContainer = OnBlueContainer,
    secondary = AccentAmber,
    tertiary = AccentGreen,
    tertiaryContainer = GreenContainer,
    onTertiaryContainer = OnGreenContainer,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    error = AccentRed,
    errorContainer = RedContainer,
    onError = Color(0xFFFFFFFF),
    onErrorContainer = OnRedContainer
)

@Composable
fun FretPitchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
