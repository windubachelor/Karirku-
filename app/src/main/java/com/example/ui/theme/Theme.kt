package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val KarirKuLightColorScheme = lightColorScheme(
    primary = ObsidianInk,
    onPrimary = WarmOffWhite,
    primaryContainer = SoftAlabaster,
    onPrimaryContainer = ObsidianInk,
    secondary = TerracottaAmber,
    onSecondary = Color.White,
    secondaryContainer = WarmGoldContainer,
    onSecondaryContainer = WarmGoldText,
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSoftBg,
    onTertiaryContainer = EmeraldAccent,
    background = WarmIvoryCanvas,
    onBackground = ObsidianInk,
    surface = PureCardSurface,
    onSurface = ObsidianInk,
    surfaceVariant = SoftAlabaster,
    onSurfaceVariant = SlateMuted,
    outline = EditorialBorder,
    outlineVariant = Color(0xFFDAD2C6),
    error = CrimsonAccent,
    onError = Color.White,
    errorContainer = CrimsonSoftBg,
    onErrorContainer = CrimsonAccent
)

private val KarirKuDarkColorScheme = darkColorScheme(
    primary = WarmOffWhite,
    onPrimary = ObsidianInk,
    primaryContainer = DarkRecessed,
    onPrimaryContainer = WarmOffWhite,
    secondary = Color(0xFFF38956),
    onSecondary = ObsidianInk,
    secondaryContainer = Color(0xFF352414),
    onSecondaryContainer = Color(0xFFFCD34D),
    tertiary = Color(0xFF34D399),
    onTertiary = ObsidianInk,
    tertiaryContainer = EmeraldDarkBg,
    onTertiaryContainer = Color(0xFF6EE7B7),
    background = DarkCanvas,
    onBackground = WarmOffWhite,
    surface = DarkCardSurface,
    onSurface = WarmOffWhite,
    surfaceVariant = DarkRecessed,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder,
    outlineVariant = Color(0xFF3A404F),
    error = Color(0xFFF87171),
    onError = ObsidianInk,
    errorContainer = CrimsonDarkBg,
    onErrorContainer = Color(0xFFFCA5A5)
)

val KarirKuShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object KarirKuThemeTokens {
    val colors: KarirKuExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKarirKuColors.current
}

@Composable
fun KarirKuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KarirKuDarkColorScheme else KarirKuLightColorScheme
    val extendedColors = if (darkTheme) DarkKarirKuColors else LightKarirKuColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalKarirKuColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KarirKuTypography,
            shapes = KarirKuShapes,
            content = content
        )
    }
}
