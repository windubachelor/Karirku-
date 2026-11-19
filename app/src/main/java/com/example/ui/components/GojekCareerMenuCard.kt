package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KarirKuThemeTokens

private enum class CareerMenuIllustration {
    INFO_LOWONGAN,
    CATATAN,
    INTERVIEW_PREPARE,
    AI_ASSISTANT
}

private enum class MenuBadgeStyle {
    RIBBON_EMERALD,
    RIBBON_AMBER,
    PILL_SOON
}

/**
 * Gojek / GoPay-inspired 4-column tactile menu card adapted to KarirKu's warm editorial identity.
 * Each tile pairs a vibrant multi-color custom vector Canvas illustration inside a sculpted
 * squircle container with an overlapping ribbon/pill badge and crisp centered label.
 */
@Composable
fun GojekCareerMenuCard(
    publicJobCount: Int,
    onOpenInfoLowongan: () -> Unit,
    onOpenCatatan: () -> Unit,
    onInterviewPrepareClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors

    TactileRaisedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gojek_career_menu_card"),
        containerColor = colors.cardSurface,
        borderColor = colors.borderSubtle,
        cornerRadius = 24.dp,
        lipDepth = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            CareerMenuTile(
                title = "Info\nLowongan",
                badgeText = if (publicJobCount > 0) "$publicJobCount AKTIF" else "PUBLIK",
                badgeStyle = MenuBadgeStyle.RIBBON_EMERALD,
                illustration = CareerMenuIllustration.INFO_LOWONGAN,
                tileTint = if (colors.isDark) Color(0xFF28221D) else Color(0xFFFFF6EF),
                tileGlow = colors.accentAmber.copy(alpha = if (colors.isDark) 0.18f else 0.15f),
                isComingSoon = false,
                onClick = onOpenInfoLowongan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_tile_info_lowongan")
            )

            CareerMenuTile(
                title = "Catatan",
                badgeText = "CATAT",
                badgeStyle = MenuBadgeStyle.RIBBON_AMBER,
                illustration = CareerMenuIllustration.CATATAN,
                tileTint = if (colors.isDark) Color(0xFF23252C) else Color(0xFFF4F7FD),
                tileGlow = colors.accentBlue.copy(alpha = if (colors.isDark) 0.18f else 0.14f),
                isComingSoon = false,
                onClick = onOpenCatatan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_tile_catatan")
            )

            CareerMenuTile(
                title = "Interview\nPrepare",
                badgeText = "SOON",
                badgeStyle = MenuBadgeStyle.PILL_SOON,
                illustration = CareerMenuIllustration.INTERVIEW_PREPARE,
                tileTint = if (colors.isDark) Color(0xFF232629) else Color(0xFFF6F3EE),
                tileGlow = Color(0xFF6366F1).copy(alpha = if (colors.isDark) 0.16f else 0.12f),
                isComingSoon = true,
                onClick = onInterviewPrepareClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_tile_interview_prepare")
            )

            CareerMenuTile(
                title = "AI",
                badgeText = "SOON",
                badgeStyle = MenuBadgeStyle.PILL_SOON,
                illustration = CareerMenuIllustration.AI_ASSISTANT,
                tileTint = if (colors.isDark) Color(0xFF27222D) else Color(0xFFF8F4FC),
                tileGlow = Color(0xFF8B5CF6).copy(alpha = if (colors.isDark) 0.18f else 0.14f),
                isComingSoon = true,
                onClick = onAiClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_tile_ai")
            )
        }
    }
}

@Composable
private fun CareerMenuTile(
    title: String,
    badgeText: String,
    badgeStyle: MenuBadgeStyle,
    illustration: CareerMenuIllustration,
    tileTint: Color,
    tileGlow: Color,
    isComingSoon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .tactileClickable(pressedScale = 0.93f, onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon Squircle + Overlapping GoPay-style Ribbon/Pill Badge
        Box(
            modifier = Modifier
                .size(width = 66.dp, height = 64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .modernColoredShadow(
                        color = tileGlow,
                        cornerRadius = 18.dp,
                        blurRadius = 12.dp,
                        offsetY = 5.dp
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                tileTint,
                                if (colors.isDark) tileTint.copy(alpha = 0.85f) else colors.cardSurface
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (colors.isDark) 0.16f else 0.95f),
                                colors.borderSubtle.copy(alpha = if (colors.isDark) 0.7f else 0.9f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CareerMenuVectorArtwork(
                    illustration = illustration,
                    modifier = Modifier
                        .size(40.dp)
                        .offset(y = (-2).dp)
                )
            }

            // Overlapping Tag Badge at Bottom Edge of Squircle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 1.dp)
            ) {
                when (badgeStyle) {
                    MenuBadgeStyle.RIBBON_EMERALD -> {
                        GoPayTagRibbon(
                            text = badgeText,
                            tagColor = Color(0xFF10B981),
                            textColor = Color.White
                        )
                    }

                    MenuBadgeStyle.RIBBON_AMBER -> {
                        GoPayTagRibbon(
                            text = badgeText,
                            tagColor = colors.accentAmber,
                            textColor = Color.White
                        )
                    }

                    MenuBadgeStyle.PILL_SOON -> {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (colors.isDark) Color(0xFFF5C542) else Color(0xFF1A1B1E),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (colors.isDark) Color(0xFF151618) else Color(0xFFFFD166),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                lineHeight = 14.sp,
                letterSpacing = (-0.1).sp
            ),
            color = if (isComingSoon) colors.textSecondary else colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Crisp GoPay-inspired pointed price-tag ribbon badge with subtle pinhole dot on the left edge.
 */
@Composable
private fun GoPayTagRibbon(
    text: String,
    tagColor: Color,
    textColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.height(17.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val notch = h * 0.34f
            val r = 3.dp.toPx()

            val tagPath = Path().apply {
                moveTo(notch, 0f)
                lineTo(w - r, 0f)
                quadraticTo(w, 0f, w, r)
                lineTo(w, h - r)
                quadraticTo(w, h, w - r, h)
                lineTo(notch, h)
                lineTo(0f, h * 0.5f)
                close()
            }

            // Soft micro shadow under ribbon
            drawPath(
                path = tagPath,
                color = Color.Black.copy(alpha = 0.14f)
            )
            drawPath(
                path = tagPath,
                color = tagColor
            )
            // Tiny tag punch hole
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = 1.6.dp.toPx(),
                center = Offset(notch * 0.72f, h * 0.5f)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp,
                letterSpacing = 0.3.sp
            ),
            color = textColor,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(start = 10.dp, end = 6.dp)
        )
    }
}

/**
 * Vibrant, flat-3D multi-color Canvas vector illustrations crafted specifically for KarirKu's
 * 4 Super-App menu items.
 */
@Composable
private fun CareerMenuVectorArtwork(
    illustration: CareerMenuIllustration,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (illustration) {
            CareerMenuIllustration.INFO_LOWONGAN -> {
                // Briefcase + Protruding Job Listing Sheet + Emerald Star Sparkle
                // Back protruding paper sheet
                drawRoundRect(
                    color = Color(0xFFFFFDF9),
                    topLeft = Offset(w * 0.22f, h * 0.08f),
                    size = Size(w * 0.52f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
                drawRoundRect(
                    color = Color(0xFFE5DDD2),
                    topLeft = Offset(w * 0.22f, h * 0.08f),
                    size = Size(w * 0.52f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = Stroke(width = 1.dp.toPx())
                )
                // Paper lines
                drawLine(
                    color = Color(0xFF3B82F6),
                    start = Offset(w * 0.31f, h * 0.19f),
                    end = Offset(w * 0.58f, h * 0.19f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF93C5FD),
                    start = Offset(w * 0.31f, h * 0.28f),
                    end = Offset(w * 0.65f, h * 0.28f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Briefcase handle
                drawRoundRect(
                    color = Color(0xFFB84514),
                    topLeft = Offset(w * 0.34f, h * 0.26f),
                    size = Size(w * 0.32f, h * 0.16f),
                    cornerRadius = CornerRadius(w * 0.07f, w * 0.07f),
                    style = Stroke(width = 2.6.dp.toPx())
                )

                // Briefcase main body (warm terracotta gradient)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEF6C35), Color(0xFFC84B1C))
                    ),
                    topLeft = Offset(w * 0.11f, h * 0.35f),
                    size = Size(w * 0.78f, h * 0.50f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                )

                // Top flap
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF864E), Color(0xFFDF5824))
                    ),
                    topLeft = Offset(w * 0.11f, h * 0.35f),
                    size = Size(w * 0.78f, h * 0.24f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                )

                // Golden clasp
                drawRoundRect(
                    color = Color(0xFFFFD166),
                    topLeft = Offset(w * 0.42f, h * 0.51f),
                    size = Size(w * 0.16f, h * 0.14f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                )

                // Emerald sparkle badge top-right
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = w * 0.13f,
                    center = Offset(w * 0.80f, h * 0.24f)
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.75f, h * 0.24f),
                    end = Offset(w * 0.85f, h * 0.24f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.80f, h * 0.19f),
                    end = Offset(w * 0.80f, h * 0.29f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            CareerMenuIllustration.CATATAN -> {
                // Warm Cream/Yellow Notepad with Sky-Blue Top Binding + Tilted Terracotta Pencil
                // Notepad shadow/back card
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(w * 0.16f, h * 0.14f),
                    size = Size(w * 0.62f, h * 0.72f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
                )
                // Inner crisp paper
                drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(w * 0.14f, h * 0.12f),
                    size = Size(w * 0.60f, h * 0.70f),
                    cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
                )
                // Top binding strip
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                    ),
                    topLeft = Offset(w * 0.14f, h * 0.12f),
                    size = Size(w * 0.60f, h * 0.16f),
                    cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
                )
                // Binding rings
                listOf(0.26f, 0.44f, 0.62f).forEach { rx ->
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.032f,
                        center = Offset(w * rx, h * 0.20f)
                    )
                }
                // Ruled note lines
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(w * 0.24f, h * 0.40f),
                    end = Offset(w * 0.56f, h * 0.40f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(w * 0.24f, h * 0.53f),
                    end = Offset(w * 0.62f, h * 0.53f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(w * 0.24f, h * 0.66f),
                    end = Offset(w * 0.48f, h * 0.66f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Tilted Vibrant Pencil on right side
                rotate(degrees = 32f, pivot = Offset(w * 0.74f, h * 0.54f)) {
                    // Pencil body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
                        ),
                        topLeft = Offset(w * 0.68f, h * 0.26f),
                        size = Size(w * 0.14f, h * 0.46f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    // Eraser cap
                    drawRoundRect(
                        color = Color(0xFFFB7185),
                        topLeft = Offset(w * 0.68f, h * 0.22f),
                        size = Size(w * 0.14f, h * 0.10f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    // Pencil tip cone
                    val tip = Path().apply {
                        moveTo(w * 0.68f, h * 0.70f)
                        lineTo(w * 0.82f, h * 0.70f)
                        lineTo(w * 0.75f, h * 0.84f)
                        close()
                    }
                    drawPath(tip, color = Color(0xFFFDE68A))
                    val lead = Path().apply {
                        moveTo(w * 0.725f, h * 0.78f)
                        lineTo(w * 0.775f, h * 0.78f)
                        lineTo(w * 0.75f, h * 0.84f)
                        close()
                    }
                    drawPath(lead, color = Color(0xFF1E293B))
                }
            }

            CareerMenuIllustration.INTERVIEW_PREPARE -> {
                // Studio Interview Mic + Coral Speech Bubble
                // Speech bubble in back-right
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFB923C), Color(0xFFEA580C))
                    ),
                    topLeft = Offset(w * 0.46f, h * 0.12f),
                    size = Size(w * 0.44f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
                )
                // Speech bubble tail
                val tail = Path().apply {
                    moveTo(w * 0.56f, h * 0.43f)
                    lineTo(w * 0.48f, h * 0.53f)
                    lineTo(w * 0.65f, h * 0.44f)
                    close()
                }
                drawPath(tail, color = Color(0xFFEA580C))
                // 3 chat dots
                listOf(0.58f, 0.68f, 0.78f).forEach { dx ->
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.032f,
                        center = Offset(w * dx, h * 0.29f)
                    )
                }

                // Microphone capsule (indigo-blue gradient)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                    ),
                    topLeft = Offset(w * 0.21f, h * 0.18f),
                    size = Size(w * 0.28f, h * 0.42f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
                )
                // Mic grille lines
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(w * 0.28f, h * 0.31f),
                    end = Offset(w * 0.42f, h * 0.31f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(w * 0.28f, h * 0.41f),
                    end = Offset(w * 0.42f, h * 0.41f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Mic cradle U-arc
                drawArc(
                    color = Color(0xFF1E3A8A),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.13f, h * 0.36f),
                    size = Size(w * 0.44f, h * 0.34f),
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                )
                // Mic stem & base
                drawLine(
                    color = Color(0xFF1E3A8A),
                    start = Offset(w * 0.35f, h * 0.70f),
                    end = Offset(w * 0.35f, h * 0.84f),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF1E3A8A),
                    start = Offset(w * 0.22f, h * 0.84f),
                    end = Offset(w * 0.48f, h * 0.84f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            CareerMenuIllustration.AI_ASSISTANT -> {
                // Cute Modern AI Spark Bot Head + Golden Four-Point Star
                // Antenna
                drawLine(
                    color = Color(0xFF7C3AED),
                    start = Offset(w * 0.46f, h * 0.28f),
                    end = Offset(w * 0.46f, h * 0.16f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = w * 0.065f,
                    center = Offset(w * 0.46f, h * 0.14f)
                )

                // Ear pods
                drawRoundRect(
                    color = Color(0xFF6D28D9),
                    topLeft = Offset(w * 0.08f, h * 0.45f),
                    size = Size(w * 0.10f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                )
                drawRoundRect(
                    color = Color(0xFF6D28D9),
                    topLeft = Offset(w * 0.74f, h * 0.45f),
                    size = Size(w * 0.10f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                )

                // Bot head shell (vibrant violet-indigo gradient)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFA78BFA), Color(0xFF6D28D9))
                    ),
                    topLeft = Offset(w * 0.15f, h * 0.28f),
                    size = Size(w * 0.62f, h * 0.52f),
                    cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
                )

                // Dark glossy visor
                drawRoundRect(
                    color = Color(0xFF161326),
                    topLeft = Offset(w * 0.23f, h * 0.39f),
                    size = Size(w * 0.46f, h * 0.29f),
                    cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
                )

                // Cute glowing cyan eyes
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = w * 0.055f,
                    center = Offset(w * 0.36f, h * 0.52f)
                )
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = w * 0.055f,
                    center = Offset(w * 0.56f, h * 0.52f)
                )
                // Friendly smile curve
                drawArc(
                    color = Color(0xFFFDE047),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.38f, h * 0.52f),
                    size = Size(w * 0.16f, h * 0.10f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 4-point Golden AI Sparkle Top-Right
                val cx = w * 0.82f
                val cy = h * 0.23f
                val rOuter = w * 0.15f
                val rInner = w * 0.045f
                val star = Path().apply {
                    moveTo(cx, cy - rOuter)
                    quadraticTo(cx + rInner, cy - rInner, cx + rOuter, cy)
                    quadraticTo(cx + rInner, cy + rInner, cx, cy + rOuter)
                    quadraticTo(cx - rInner, cy + rInner, cx - rOuter, cy)
                    quadraticTo(cx - rInner, cy - rInner, cx, cy - rOuter)
                    close()
                }
                drawPath(
                    path = star,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                    )
                )
            }
        }
    }
}
