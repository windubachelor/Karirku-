package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ApplicationStatus
import com.example.data.JobApplication
import com.example.data.KarirKuFormatters
import com.example.data.StageHistoryEvent
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.ObsidianInk
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.UnboundedFontFamily
import com.example.ui.theme.WarmOffWhite

@Composable
fun KarirKuBrandBadge(
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val corner = size * 0.30f
    Box(
        modifier = modifier
            .size(size)
            .modernColoredShadow(
                color = ObsidianInk.copy(alpha = 0.28f),
                cornerRadius = corner,
                blurRadius = 10.dp,
                offsetY = 4.dp
            )
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1F232D), ObsidianInk)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "K",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.44f).sp
                ),
                color = WarmOffWhite
            )
            Box(
                modifier = Modifier
                    .padding(bottom = (size.value * 0.14f).dp, start = 1.dp)
                    .size((size.value * 0.15f).dp)
                    .clip(CircleShape)
                    .background(TerracottaAmber)
            )
        }
    }
}

@Composable
fun KarirKuTopBar(
    subtitleTag: String,
    userPhotoUrl: String?,
    userInitials: String,
    hasUpcomingAlert: Boolean,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KarirKuBrandBadge(size = 40.dp)
            Column {
                Text(
                    text = "KarirKu",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = subtitleTag,
                    style = MonoEyebrowStyle,
                    color = colors.accentAmber
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .modernColoredShadow(
                        color = if (colors.isDark) Color.Black.copy(alpha = 0.4f) else Color(0xFF2B2118).copy(alpha = 0.12f),
                        cornerRadius = 22.dp,
                        blurRadius = 12.dp,
                        offsetY = 4.dp
                    )
                    .clip(CircleShape)
                    .background(colors.cardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = if (colors.isDark) 0.14f else 0.9f), colors.borderSubtle)
                        ),
                        shape = CircleShape
                    )
                    .tactileClickable(onClick = onNotificationClick)
                    .testTag("top_bar_notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Agenda dan pengingat",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (hasUpcomingAlert) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        PulsingStatusDot(color = colors.accentAmber, size = 6.dp)
                    }
                }
            }

            UserAvatarCircle(
                photoUrl = userPhotoUrl,
                initials = userInitials,
                size = 44.dp,
                onClick = onProfileClick,
                modifier = Modifier.testTag("top_bar_avatar_button")
            )
        }
    }
}

@Composable
fun UserAvatarCircle(
    photoUrl: String?,
    initials: String,
    size: Dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val clickMod = if (onClick != null) Modifier.tactileClickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .size(size)
            .modernColoredShadow(
                color = ObsidianInk.copy(alpha = 0.22f),
                cornerRadius = size / 2f,
                blurRadius = 12.dp,
                offsetY = 5.dp
            )
            .clip(CircleShape)
            .background(colors.heroDarkElevated)
            .border(2.dp, Color.White.copy(alpha = if (colors.isDark) 0.2f else 0.85f), CircleShape)
            .then(clickMod),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto profil pengguna",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.32f).sp
                ),
                color = colors.heroOnDark
            )
        }
    }
}

@Composable
fun AtmosphericDarkCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val shape = RoundedCornerShape(cornerRadius)
    val clickMod = if (onClick != null) Modifier.tactileClickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .padding(bottom = 3.dp)
            .modernColoredShadow(
                color = ObsidianInk.copy(alpha = if (colors.isDark) 0.65f else 0.34f),
                cornerRadius = cornerRadius,
                blurRadius = 24.dp,
                offsetY = 12.dp
            )
    ) {
        // Subtle 3D bottom depth lip
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(shape)
                .background(Color(0xFF050608))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF191D26), colors.heroDarkSurface, Color(0xFF0C0E13))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = shape
                )
                .then(clickMod)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Warm terracotta glow top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TerracottaAmber.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.12f),
                        radius = size.width * 0.68f
                    ),
                    radius = size.width * 0.68f,
                    center = Offset(size.width * 0.88f, size.height * 0.12f)
                )
                // Cool cobalt glow bottom-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3B82F6).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.92f),
                        radius = size.width * 0.56f
                    ),
                    radius = size.width * 0.56f,
                    center = Offset(size.width * 0.1f, size.height * 0.92f)
                )
                // Emerald micro-aura bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.82f, size.height * 0.92f),
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.82f, size.height * 0.92f)
                )
                // Crisp subtle architectural dossier grid
                val step = 44.dp.toPx()
                var x = step
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }
            content()
        }
    }
}

@Composable
fun CompanyMonogramBadge(
    companyName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val palette = if (colors.isDark) {
        listOf(
            Color(0xFF15342B) to Color(0xFF6EE7B7),
            Color(0xFF172B4A) to Color(0xFF93C5FD),
            Color(0xFF3A2418) to Color(0xFFFDBA74),
            Color(0xFF38192A) to Color(0xFFF9A8D4),
            Color(0xFF2B2342) to Color(0xFFC4B5FD)
        )
    } else {
        listOf(
            Color(0xFFE3F7EE) to Color(0xFF0E7C5B),
            Color(0xFFE5F0FF) to Color(0xFF1D4ED8),
            Color(0xFFFFEDE0) to Color(0xFFC2410C),
            Color(0xFFFCE6F0) to Color(0xFFBE185D),
            Color(0xFFEEE9FF) to Color(0xFF6D28D9)
        )
    }
    val hash = kotlin.math.abs(companyName.hashCode()) % palette.size
    val (bg, fg) = palette[hash]
    val mono = KarirKuFormatters.companyMonogram(companyName)

    Box(
        modifier = modifier
            .size(size)
            .modernColoredShadow(
                color = fg.copy(alpha = 0.22f),
                cornerRadius = 15.dp,
                blurRadius = 10.dp,
                offsetY = 4.dp
            )
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(bg, bg.copy(alpha = 0.78f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = if (colors.isDark) 0.16f else 0.85f), fg.copy(alpha = 0.22f))
                ),
                shape = RoundedCornerShape(15.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mono,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = UnboundedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (mono.length >= 4) 10.sp else 12.sp,
                letterSpacing = (-0.3).sp
            ),
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
fun StatusBadgePill(
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val (bg, border, fg) = when (status) {
        ApplicationStatus.INTERVIEW -> Triple(colors.amberSoftBg, colors.accentAmber.copy(alpha = 0.4f), colors.amberText)
        ApplicationStatus.OFFER -> Triple(colors.emeraldSoftBg, colors.emeraldBorder, colors.accentEmerald)
        ApplicationStatus.ADMINISTRASI -> Triple(colors.blueSoftBg, colors.blueBorder, colors.accentBlue)
        ApplicationStatus.DITOLAK -> Triple(colors.crimsonSoftBg, colors.crimsonBorder, colors.accentCrimson)
        ApplicationStatus.DILAMAR -> Triple(colors.neutralChipBg, colors.borderSubtle, colors.textSecondary)
        ApplicationStatus.DISIMPAN -> Triple(colors.recessedSurface, colors.borderSubtle, colors.textMuted)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (status == ApplicationStatus.OFFER) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(fg)
                )
            }
            Text(
                text = status.badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp
                ),
                color = fg,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobApplicationCard(
    application: JobApplication,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val starScale by animateFloatAsState(
        targetValue = if (application.starred) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 450f),
        label = "star_scale"
    )

    TactileRaisedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_card_${application.id}"),
        cornerRadius = 22.dp,
        lipDepth = 3.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompanyMonogramBadge(companyName = application.companyName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.roleTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val locationPart = if (application.location.isNotBlank()) " • ${application.location}" else ""
                    Text(
                        text = "${application.companyName}$locationPart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("star_button_${application.id}")
                ) {
                    val isWishlist = application.statusEnum == ApplicationStatus.DISIMPAN
                    Icon(
                        imageVector = when {
                            isWishlist && application.starred -> Icons.Rounded.Bookmark
                            isWishlist -> Icons.Outlined.BookmarkBorder
                            application.starred -> Icons.Rounded.Star
                            else -> Icons.Outlined.StarBorder
                        },
                        contentDescription = if (application.starred) "Hapus prioritas" else "Tandai prioritas",
                        tint = if (application.starred) colors.accentAmber else colors.textMuted,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                scaleX = starScale
                                scaleY = starScale
                            }
                    )
                }
            }

            if (application.notes.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.recessedSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = application.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Compensation & Work Mode Chips (FlowRow prevents truncation on narrow/scaled displays)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.amberSoftBg.copy(alpha = if (colors.isDark) 0.5f else 0.55f),
                    border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            tint = colors.accentAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = KarirKuFormatters.formatIdrCompactRange(
                                application.salaryMin,
                                application.salaryMax
                            ),
                            style = MonoMetricStyle.copy(fontSize = 11.sp),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.recessedSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WorkOutline,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${application.workModeEnum.label} • ${application.contractTypeEnum.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadgePill(status = application.statusEnum)
                Text(
                    text = KarirKuFormatters.formatRelativeTime(application.updatedAt ?: application.createdAt),
                    style = MonoEyebrowStyle.copy(fontSize = 10.sp, letterSpacing = 0.2.sp),
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
fun ExpressiveEmptyStateCard(
    eyebrow: String,
    title: String,
    description: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    TactileRaisedCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        lipDepth = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MonoEyebrowStyle,
                color = colors.accentAmber
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            TactileRaisedButton(
                onClick = onCtaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_state_cta_button"),
                cornerRadius = 15.dp,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = ctaLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DossierDetailBottomSheet(
    application: JobApplication,
    onDismiss: () -> Unit,
    onStatusChange: (ApplicationStatus) -> Unit,
    onEditClick: () -> Unit,
    onCreateInterviewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        val timelineEvents = remember(application.stageHistory, application.status, application.updatedAt) {
            application.parsedTimeline()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompanyMonogramBadge(companyName = application.companyName, size = 50.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.dossierCode.ifBlank { "#LAMARAN" },
                        style = MonoEyebrowStyle,
                        color = colors.accentAmber
                    )
                    Text(
                        text = application.roleTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = listOf(application.companyName, application.location)
                            .filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }

            // Quick stage progression pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "UBAH STATUS LAMARAN",
                    style = MonoEyebrowStyle,
                    color = colors.textMuted
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ApplicationStatus.entries.forEach { st ->
                        val selected = application.statusEnum == st
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tactileClickable { onStatusChange(st) }
                                .testTag("quick_status_${st.name}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.heroDarkSurface else colors.recessedSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.heroDarkSurface else colors.borderSubtle
                            )
                        ) {
                            Text(
                                text = st.shortLabel,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                color = if (selected) colors.heroOnDark else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Compensation & Dates summary (clean breathable rows, zero date collision)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.recessedSurface,
                border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimasi Gaji",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                        Text(
                            text = KarirKuFormatters.formatIdrCompactRange(application.salaryMin, application.salaryMax),
                            style = MonoMetricStyle.copy(fontSize = 12.sp),
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sistem Kerja",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                        Text(
                            text = "${application.workModeEnum.label} • ${application.contractTypeEnum.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "TANGGAL LAMAR",
                                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                                    color = colors.textMuted,
                                    maxLines = 1
                                )
                                Text(
                                    text = KarirKuFormatters.formatDateShort(application.appliedDateMillis),
                                    style = MonoMetricStyle.copy(fontSize = 11.5.sp),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "TARGET KABAR",
                                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                                    color = colors.accentAmber,
                                    maxLines = 1
                                )
                                Text(
                                    text = KarirKuFormatters.formatDateShort(application.targetResponseMillis),
                                    style = MonoMetricStyle.copy(fontSize = 11.5.sp),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    if (application.hrContact.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kontak HRD",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                            Text(
                                text = application.hrContact,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Timeline Riwayat Section (Status Progression History)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("application_timeline_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = colors.accentAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "TIMELINE RIWAYAT",
                        style = MonoEyebrowStyle,
                        color = colors.textMuted
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.recessedSurface.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        timelineEvents.forEachIndexed { index, event ->
                            TimelineHistoryRow(
                                event = event,
                                isLatest = index == 0,
                                isLast = index == timelineEvents.lastIndex
                            )
                        }
                    }
                }
            }

            if (application.notes.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CATATAN LAMARAN", style = MonoEyebrowStyle, color = colors.textMuted)
                    Text(
                        text = application.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            if (application.attachments.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BERKAS TERLAMPIR (${application.attachments.size})",
                        style = MonoEyebrowStyle,
                        color = colors.textMuted
                    )
                    application.attachments.forEach { raw ->
                        val parts = raw.split("|")
                        val fileName = parts.firstOrNull() ?: raw
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AttachFile,
                                contentDescription = null,
                                tint = colors.accentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TactileRaisedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_edit_application_button"),
                    cornerRadius = 15.dp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Edit Lamaran",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onCreateInterviewClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("sheet_add_interview_button"),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Jadwalkan",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sheet_delete_application_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, colors.crimsonBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentCrimson)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Hapus Lamaran",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun TimelineHistoryRow(
    event: StageHistoryEvent,
    isLatest: Boolean,
    isLast: Boolean
) {
    val colors = KarirKuThemeTokens.colors
    val dotColor = when (event.status) {
        ApplicationStatus.INTERVIEW -> colors.accentAmber
        ApplicationStatus.OFFER -> colors.accentEmerald
        ApplicationStatus.ADMINISTRASI -> colors.accentBlue
        ApplicationStatus.DITOLAK -> colors.accentCrimson
        ApplicationStatus.DILAMAR -> colors.textPrimary
        ApplicationStatus.DISIMPAN -> colors.textMuted
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            if (isLatest) {
                PulsingStatusDot(color = dotColor, size = 9.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.65f))
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(1.5.dp)
                        .height(32.dp)
                        .background(colors.borderSubtle)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.status.shortLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isLatest) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = if (isLatest) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = KarirKuFormatters.formatDateTimeShort(event.timestampMillis),
                    style = MonoEyebrowStyle.copy(fontSize = 9.sp, letterSpacing = 0.1.sp),
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Text(
                text = event.note,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Sleek, compact, single-line or multiline input field with crisp focus border animation
 * and guaranteed single-line placeholder ellipsis (eliminates Material OutlinedTextField height bloat).
 */
@Composable
fun KarirKuCompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    prefixText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    cornerRadius: Dp = 14.dp,
    containerColor: Color = KarirKuThemeTokens.colors.recessedSurface,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    elevatedShadow: Boolean = false
) {
    val colors = KarirKuThemeTokens.colors
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.textPrimary else colors.borderSubtle,
        label = "compactFieldBorder"
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .then(
                if (elevatedShadow) {
                    Modifier.modernColoredShadow(
                        color = if (colors.isDark) Color.Black.copy(alpha = 0.36f) else Color(0xFF2B2118).copy(alpha = 0.08f),
                        cornerRadius = cornerRadius,
                        blurRadius = 12.dp,
                        offsetY = 4.dp
                    )
                } else Modifier
            )
            .then(
                if (singleLine) Modifier.height(48.dp) else Modifier.heightIn(min = 92.dp)
            )
            .clip(shape)
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.6f))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .padding(
                horizontal = 14.dp,
                vertical = if (singleLine) 0.dp else 12.dp
            ),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            if (!prefixText.isNullOrEmpty()) {
                Text(
                    text = prefixText,
                    style = MonoMetricStyle.copy(fontSize = 13.sp),
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = colors.textMuted.copy(alpha = 0.8f),
                        maxLines = if (singleLine) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) colors.textPrimary else colors.textMuted
                    ),
                    cursorBrush = SolidColor(colors.accentAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                )
            }
            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}
