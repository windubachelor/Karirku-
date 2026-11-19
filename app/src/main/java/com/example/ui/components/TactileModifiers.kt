package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KarirKuThemeTokens

/**
 * Draws a modern, diffused colored ambient shadow beneath rounded surfaces
 * (similar to elevated super-app cards like Gojek / Apple Wallet).
 */
fun Modifier.modernColoredShadow(
    color: Color,
    cornerRadius: Dp = 22.dp,
    blurRadius: Dp = 18.dp,
    offsetY: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    spread: Dp = (-2).dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val spreadPx = spread.toPx()
    val offsetYPx = offsetY.toPx()
    val offsetXPx = offsetX.toPx()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.isAntiAlias = true
            frameworkPaint.color = android.graphics.Color.TRANSPARENT
            frameworkPaint.setShadowLayer(
                blurRadius.toPx().coerceAtLeast(1f),
                offsetXPx,
                offsetYPx,
                color.toArgb()
            )
            canvas.drawRoundRect(
                left = -spreadPx,
                top = -spreadPx,
                right = size.width + spreadPx,
                bottom = size.height + spreadPx,
                radiusX = cornerPx,
                radiusY = cornerPx,
                paint = paint
            )
        }
    } else {
        // Multi-step soft fallback for API 24-27
        val layers = 3
        for (i in layers downTo 1) {
            val expand = (blurRadius.toPx() / layers) * i * 0.45f
            val alpha = (color.alpha / (i + 1.2f)).coerceIn(0f, 1f)
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = androidx.compose.ui.geometry.Offset(offsetXPx - expand, offsetYPx - expand * 0.3f),
                size = androidx.compose.ui.geometry.Size(
                    (size.width + expand * 2f).coerceAtLeast(0f),
                    (size.height + expand * 1.4f).coerceAtLeast(0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx + expand * 0.4f)
            )
        }
    }
}

/**
 * Adds tactile spring bounce press physics and subtle haptic feedback on click.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactile_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
}

/**
 * Raised 3D-inspired tactile card with diffused ambient shadow, subtle bottom depth lip,
 * and crisp top inner highlight — avoiding flat/boring cards.
 */
@Composable
fun TactileRaisedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = KarirKuThemeTokens.colors.cardSurface,
    borderColor: Color = KarirKuThemeTokens.colors.borderSubtle,
    shadowColor: Color? = null,
    lipColor: Color? = null,
    cornerRadius: Dp = 22.dp,
    lipDepth: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val resolvedShadow = shadowColor ?: if (colors.isDark) {
        Color.Black.copy(alpha = 0.48f)
    } else {
        Color(0xFF2B2118).copy(alpha = 0.11f)
    }
    val resolvedLip = lipColor ?: if (colors.isDark) {
        Color.Black.copy(alpha = 0.55f)
    } else {
        Color(0xFFDCD3C6)
    }
    val topGloss = if (colors.isDark) {
        Color.White.copy(alpha = 0.09f)
    } else {
        Color.White.copy(alpha = 0.95f)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 420f),
        label = "card_press_scale"
    )
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && onClick != null) lipDepth * 0.6f else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "card_press_offset"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .modernColoredShadow(
                color = resolvedShadow,
                cornerRadius = cornerRadius,
                blurRadius = 18.dp,
                offsetY = 8.dp
            )
    ) {
        // 3D Bottom Lip Layer
        if (lipDepth > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = lipDepth)
                    .clip(shape)
                    .background(resolvedLip)
            )
        }

        // Foreground Tactile Surface with subtle top specular gloss
        val clickMod = if (onClick != null) {
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
        } else Modifier

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = pressOffsetY)
                .clip(shape)
                .background(containerColor)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            topGloss.copy(alpha = if (colors.isDark) 0.05f else 0.35f),
                            Color.Transparent
                        ),
                        endY = 80f
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(topGloss, borderColor)
                    ),
                    shape = shape
                )
                .then(clickMod),
            content = content
        )
    }
}

/**
 * Raised 3D super-app style CTA button with colored ambient glow, bottom tactile bevel lip,
 * top glossy highlight, and spring press-down physics.
 */
@Composable
fun TactileRaisedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    fillMaxWidth: Boolean = true,
    containerColor: Color = KarirKuThemeTokens.colors.heroDarkSurface,
    contentColor: Color = KarirKuThemeTokens.colors.heroOnDark,
    lipColor: Color = Color(0xFF06070A),
    glowColor: Color = containerColor.copy(alpha = 0.34f),
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
    content: @Composable RowScope.() -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val effectiveEnabled = enabled && !isLoading
    val showRaisedStyle = enabled || isLoading

    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && effectiveEnabled) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "btn_press_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && effectiveEnabled) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val activeBg = if (showRaisedStyle) containerColor else colors.recessedSurface
    val resolvedContentColor = if (showRaisedStyle) contentColor else colors.textMuted

    Box(
        modifier = modifier
            .padding(bottom = if (showRaisedStyle) 4.dp else 0.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .modernColoredShadow(
                color = if (showRaisedStyle) glowColor else Color.Transparent,
                cornerRadius = cornerRadius,
                blurRadius = 20.dp,
                offsetY = 9.dp
            )
    ) {
        // 3D Bottom Lip (only when active/loading to prevent muddy overlap when disabled)
        if (showRaisedStyle) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 4.dp)
                    .clip(shape)
                    .background(lipColor)
            )
        }

        // Button Face
        val widthMod = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
        Box(
            modifier = widthMod
                .heightIn(min = 50.dp)
                .offset(y = if (showRaisedStyle) pressOffsetY else 0.dp)
                .clip(shape)
                .background(activeBg)
                .then(
                    if (showRaisedStyle) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    ),
                                    endY = 75f
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.24f),
                                        Color.Transparent
                                    )
                                ),
                                shape = shape
                            )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = colors.borderSubtle,
                            shape = shape
                        )
                    }
                )
                .clickable(
                    enabled = effectiveEnabled,
                    interactionSource = interactionSource,
                    indication = ripple(color = resolvedContentColor),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = resolvedContentColor,
                            strokeWidth = 2.4.dp
                        )
                    } else {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Live breathing status dot with soft ambient halo ring.
 */
@Composable
fun PulsingStatusDot(
    color: Color,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse_dot")
    val haloScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_scale"
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = modifier.size(size * 2f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                    alpha = haloAlpha
                }
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
