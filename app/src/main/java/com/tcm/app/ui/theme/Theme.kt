package com.tcm.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TcmPrimary,
    onPrimary = TcmOnPrimary,
    primaryContainer = TcmPrimaryContainer,
    onPrimaryContainer = TcmOnPrimaryContainer,
    secondary = TcmSecondary,
    onSecondary = TcmOnSecondary,
    secondaryContainer = TcmSecondaryContainer,
    onSecondaryContainer = TcmOnSecondaryContainer,
    tertiary = TcmTertiary,
    onTertiary = TcmOnTertiary,
    tertiaryContainer = TcmTertiaryContainer,
    onTertiaryContainer = TcmOnTertiaryContainer,
    error = TcmError,
    onError = TcmOnError,
    errorContainer = TcmErrorContainer,
    onErrorContainer = TcmOnErrorContainer,
    background = TcmBackground,
    onBackground = TcmOnBackground,
    surface = TcmSurface,
    onSurface = TcmOnSurface,
    surfaceVariant = TcmSurfaceVariant,
    onSurfaceVariant = TcmOnSurfaceVariant,
    outline = TcmOutline,
    outlineVariant = TcmOutlineVariant,
    inverseSurface = TcmInverseSurface,
    inverseOnSurface = TcmInverseOnSurface,
    inversePrimary = TcmInversePrimary,
    scrim = TcmScrim,
    surfaceTint = TcmPrimary,
    surfaceBright = Color(0xFFFDFCF7),
    surfaceContainer = Color(0xFFF4F2EB),
    surfaceContainerHigh = Color(0xFFEEEBE4),
    surfaceContainerHighest = Color(0xFFE8E5DE),
    surfaceContainerLow = Color(0xFFFAF8F1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDDDAD3)
)

private val DarkColorScheme = darkColorScheme(
    primary = TcmInversePrimary,
    onPrimary = TcmPrimaryContainer,
    primaryContainer = TcmPrimary,
    onPrimaryContainer = TcmPrimaryContainer,
    secondary = Color(0xFFE5C39C),
    onSecondary = TcmSecondaryContainer,
    secondaryContainer = TcmSecondary,
    onSecondaryContainer = TcmSecondaryContainer,
    tertiary = Color(0xFFA8C8C8),
    onTertiary = TcmTertiaryContainer,
    tertiaryContainer = TcmTertiary,
    onTertiaryContainer = TcmTertiaryContainer,
    error = Color(0xFFFFB4AB),
    onError = TcmErrorContainer,
    errorContainer = TcmError,
    onErrorContainer = TcmErrorContainer,
    background = Color(0xFF1C1B18),
    onBackground = Color(0xFFE5E2DB),
    surface = Color(0xFF1C1B18),
    onSurface = Color(0xFFE5E2DB),
    surfaceVariant = Color(0xFF474640),
    onSurfaceVariant = Color(0xFFC7C5BE),
    outline = Color(0xFF918F89),
    outlineVariant = Color(0xFF474640),
    inverseSurface = Color(0xFFE5E2DB),
    inverseOnSurface = Color(0xFF31302C),
    inversePrimary = TcmPrimary,
    scrim = TcmScrim,
    surfaceTint = TcmInversePrimary,
    surfaceBright = Color(0xFF3A3835),
    surfaceContainer = Color(0xFF272622),
    surfaceContainerHigh = Color(0xFF31302C),
    surfaceContainerHighest = Color(0xFF3C3A37),
    surfaceContainerLow = Color(0xFF1C1B18),
    surfaceContainerLowest = Color(0xFF11100E),
    surfaceDim = Color(0xFF1C1B18)
)

@Composable
fun TcmOcrAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent TCM theming
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
