package com.rianixia.settings.overlay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = XinyaDarkPrimary,
    onPrimary = XinyaDarkOnPrimary,
    secondary = XinyaDarkSecondary,
    onSecondary = XinyaDarkText,
    background = XinyaDarkBackground,
    onBackground = XinyaDarkText,
    surface = XinyaDarkSurface,
    onSurface = XinyaDarkText,
    surfaceVariant = XinyaDarkSurfaceVariant,
    onSurfaceVariant = XinyaDarkSubText,
    outline = XinyaDarkDivider,
    error = XinyaDarkError,
    onError = XinyaDarkBackground,
    primaryContainer = XinyaDarkSurfaceVariant,
    onPrimaryContainer = XinyaDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = XinyaLightPrimary,
    onPrimary = XinyaLightOnPrimary,
    secondary = XinyaLightSecondary,
    onSecondary = XinyaLightOnPrimary,
    background = XinyaLightBackground,
    onBackground = XinyaLightText,
    surface = XinyaLightSurface,
    onSurface = XinyaLightText,
    surfaceVariant = XinyaLightSurfaceVariant,
    onSurfaceVariant = XinyaLightSubText,
    outline = XinyaLightDivider,
    error = XinyaLightError,
    onError = XinyaLightSurface,
    primaryContainer = XinyaLightSurfaceVariant,
    onPrimaryContainer = XinyaLightText
)

@Composable
fun XinyaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default for consistent styling
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}