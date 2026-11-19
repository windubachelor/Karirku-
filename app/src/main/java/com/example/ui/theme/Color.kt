package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand & Editorial Canvas Palette
val WarmIvoryCanvas = Color(0xFFFAF7F2)
val PureCardSurface = Color(0xFFFFFFFF)
val SoftAlabaster = Color(0xFFF3EFE8)
val EditorialBorder = Color(0xFFE9E2D8)
val ObsidianInk = Color(0xFF111318)
val ObsidianElevated = Color(0xFF1A1E27)
val CharcoalSubtle = Color(0xFF282D38)
val SlateMuted = Color(0xFF716C65)
val SlateLight = Color(0xFF9C968E)

// Dark Mode Editorial Surfaces
val DarkCanvas = Color(0xFF0E1014)
val DarkCardSurface = Color(0xFF171A21)
val DarkRecessed = Color(0xFF20242E)
val DarkBorder = Color(0xFF2C313D)
val WarmOffWhite = Color(0xFFF6F3EE)
val DarkMutedText = Color(0xFFA0A5B1)

// Semantic Stage & Accent Colors
val TerracottaAmber = Color(0xFFE86F3A)
val WarmGold = Color(0xFFF59E0B)
val WarmGoldContainer = Color(0xFFFEF3C7)
val WarmGoldText = Color(0xFF92400E)

val EmeraldAccent = Color(0xFF0E7C5B)
val EmeraldBright = Color(0xFF34D399)
val EmeraldSoftBg = Color(0xFFEAF7F2)
val EmeraldSoftBorder = Color(0xFFBDE6D7)
val EmeraldDarkBg = Color(0xFF112A23)

val CobaltBlue = Color(0xFF2563EB)
val CobaltSoftBg = Color(0xFFEFF6FF)
val CobaltSoftBorder = Color(0xFFBFDBFE)
val CobaltDarkBg = Color(0xFF14213D)

val CrimsonAccent = Color(0xFFDC2626)
val CrimsonSoftBg = Color(0xFFFEF2F2)
val CrimsonSoftBorder = Color(0xFFFECACA)
val CrimsonDarkBg = Color(0xFF311518)

val SlateTagBg = Color(0xFFF1EFEA)
val SlateTagDarkBg = Color(0xFF252932)

@Immutable
data class KarirKuExtendedColors(
    val canvas: Color,
    val cardSurface: Color,
    val recessedSurface: Color,
    val borderSubtle: Color,
    val heroDarkSurface: Color,
    val heroDarkElevated: Color,
    val heroOnDark: Color,
    val heroOnDarkMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentAmber: Color,
    val amberSoftBg: Color,
    val amberText: Color,
    val accentEmerald: Color,
    val emeraldBright: Color,
    val emeraldSoftBg: Color,
    val emeraldBorder: Color,
    val accentBlue: Color,
    val blueSoftBg: Color,
    val blueBorder: Color,
    val accentCrimson: Color,
    val crimsonSoftBg: Color,
    val crimsonBorder: Color,
    val neutralChipBg: Color,
    val isDark: Boolean
)

val LightKarirKuColors = KarirKuExtendedColors(
    canvas = WarmIvoryCanvas,
    cardSurface = PureCardSurface,
    recessedSurface = SoftAlabaster,
    borderSubtle = EditorialBorder,
    heroDarkSurface = ObsidianInk,
    heroDarkElevated = ObsidianElevated,
    heroOnDark = WarmOffWhite,
    heroOnDarkMuted = Color(0xFFA8AEBC),
    textPrimary = ObsidianInk,
    textSecondary = Color(0xFF3B3E47),
    textMuted = SlateMuted,
    accentAmber = TerracottaAmber,
    amberSoftBg = WarmGoldContainer,
    amberText = WarmGoldText,
    accentEmerald = EmeraldAccent,
    emeraldBright = EmeraldBright,
    emeraldSoftBg = EmeraldSoftBg,
    emeraldBorder = EmeraldSoftBorder,
    accentBlue = CobaltBlue,
    blueSoftBg = CobaltSoftBg,
    blueBorder = CobaltSoftBorder,
    accentCrimson = CrimsonAccent,
    crimsonSoftBg = CrimsonSoftBg,
    crimsonBorder = CrimsonSoftBorder,
    neutralChipBg = SlateTagBg,
    isDark = false
)

val DarkKarirKuColors = KarirKuExtendedColors(
    canvas = DarkCanvas,
    cardSurface = DarkCardSurface,
    recessedSurface = DarkRecessed,
    borderSubtle = DarkBorder,
    heroDarkSurface = Color(0xFF141821),
    heroDarkElevated = Color(0xFF1F2533),
    heroOnDark = WarmOffWhite,
    heroOnDarkMuted = DarkMutedText,
    textPrimary = WarmOffWhite,
    textSecondary = Color(0xFFD5D8E0),
    textMuted = DarkMutedText,
    accentAmber = Color(0xFFF38956),
    amberSoftBg = Color(0xFF352414),
    amberText = Color(0xFFFCD34D),
    accentEmerald = Color(0xFF34D399),
    emeraldBright = Color(0xFF6EE7B7),
    emeraldSoftBg = EmeraldDarkBg,
    emeraldBorder = Color(0xFF1F4D3F),
    accentBlue = Color(0xFF60A5FA),
    blueSoftBg = CobaltDarkBg,
    blueBorder = Color(0xFF1E3A6D),
    accentCrimson = Color(0xFFF87171),
    crimsonSoftBg = CrimsonDarkBg,
    crimsonBorder = Color(0xFF5C232A),
    neutralChipBg = SlateTagDarkBg,
    isDark = true
)

val LocalKarirKuColors = staticCompositionLocalOf { LightKarirKuColors }
