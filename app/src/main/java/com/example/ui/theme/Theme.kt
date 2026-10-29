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
    primary = TealPrimaryLight,
    onPrimary = NavySurfaceDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealContainer,
    secondary = MedicalCyan,
    onSecondary = Color.White,
    secondaryContainer = NavyCardDark,
    onSecondaryContainer = MedicalCyanContainer,
    background = NavySurfaceDark,
    onBackground = Slate100,
    surface = NavyCardDark,
    onSurface = Slate100,
    surfaceVariant = NavyBorder,
    onSurfaceVariant = Slate200,
    outline = Slate600,
    error = SeverityCriticalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = MedicalCyan,
    onSecondary = Color.White,
    secondaryContainer = MedicalCyanContainer,
    onSecondaryContainer = OnMedicalCyanContainer,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = SeverityCriticalRed,
    onError = Color.White
)

@Composable
fun RxSafetyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep medical brand colors consistent
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
