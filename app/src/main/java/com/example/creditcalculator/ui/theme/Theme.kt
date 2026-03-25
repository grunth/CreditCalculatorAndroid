package com.example.creditcalculator.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
    secondary = SecondarySlate,
    onSecondary = OnSecondarySlate,
    secondaryContainer = SecondaryContainerSlate,
    onSecondaryContainer = OnSecondaryContainerSlate,
    tertiary = TertiaryTeal,
    onTertiary = OnTertiaryTeal,
    tertiaryContainer = TertiaryContainerTeal,
    onTertiaryContainer = OnTertiaryContainerTeal,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = ErrorRed,
    onError = OnErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue, // Можно адаптировать для темной, но пока оставим современные акценты
    secondary = SecondarySlate,
    tertiary = TertiaryTeal
)

@Composable
fun CreditCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
