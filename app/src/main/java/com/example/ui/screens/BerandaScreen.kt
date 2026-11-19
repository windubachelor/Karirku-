package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ApplicationStatus
import com.example.data.JobApplication
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.JobApplicationCard
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.BigStatNumeralStyle
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.KarirKuDashboardState
import com.example.viewmodel.PipelineMetrics
import com.example.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BerandaScreen(
    state: KarirKuDashboardState,
    selectedFilter: ApplicationStatus?,
    sortOrder: SortOrder,
    onSelectFilter: (ApplicationStatus?) -> Unit,
    onCycleSort: () -> Unit,
    onOpenApplicationDetail: (JobApplication) -> Unit,
    onToggleStar: (JobApplication) -> Unit,
    onAddApplicationClick: () -> Unit,
    onOpenScheduleTab: () -> Unit,
    onOpenProfileTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val metrics = state.metrics

    val firstName = remember(state.profile?.displayName) {
        state.profile?.displayName
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Pejuang Karier"
    }

    val filteredApps = remember(state.applications, selectedFilter, sortOrder) {
        val base = if (selectedFilter == null) state.applications
        else state.applications.filter { it.statusEnum == selectedFilter }
        when (sortOrder) {
            SortOrder.NEWEST -> base
            SortOrder.HIGHEST_SALARY -> base.sortedByDescending { maxOf(it.salaryMin, it.salaryMax) }
            SortOrder.STARRED_FIRST -> base.sortedByDescending { it.starred }
        }
    }

    val now = remember { Date() }
    val monthTag = remember(now) {
        SimpleDateFormat("MMM", Locale("in", "ID")).format(now).uppercase(Locale.US)
    }
    val dayNum = remember(now) {
        SimpleDateFormat("dd", Locale("in", "ID")).format(now)
    }
    val dayName = remember(now) {
        SimpleDateFormat("EEEE", Locale("in", "ID")).format(now)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("beranda_lazy_column"),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Atmospheric Dark Hero Greeting Card with 3D depth & pulsing live indicator
            item(key = "hero_card") {
                AtmosphericDarkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("beranda_hero_card"),
                    cornerIllustrationRes = R.drawable.img_header_beranda_1795098523162,
                    illustrationAlignment = Alignment.BottomEnd,
                    illustrationSize = 158.dp,
                    illustrationAlpha = 0.36f,
                    illustrationOffsetX = 12.dp,
                    illustrationOffsetY = 12.dp,
                    onClick = onOpenScheduleTab
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PulsingStatusDot(color = colors.accentAmber, size = 6.dp)
                                        Text(
                                            text = "Minggu Produktif",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.2.sp
                                            ),
                                            color = colors.heroOnDark
                                        )
                                    }
                                }

                                Text(
                                    text = "Halo, $firstName",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontFamily = UnboundedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 23.sp
                                    ),
                                    color = colors.heroOnDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Raised Glass Calendar Tile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.18f),
                                                Color.White.copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f))
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = monthTag,
                                        style = MonoEyebrowStyle,
                                        color = colors.accentAmber
                                    )
                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = UnboundedFontFamily,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = colors.heroOnDark
                                    )
                                    Text(
                                        text = dayName.take(6),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.heroOnDarkMuted
                                    )
                                }
                            }
                        }

                        val agendaCopy = when {
                            metrics.upcomingInterviewsThisWeek > 0 ->
                                "Ada ${metrics.upcomingInterviewsThisWeek} agenda interview penting pekan ini. Siapkan portofolio & catatan risetmu!"
                            metrics.interviewCount > 0 ->
                                "${metrics.interviewCount} lamaran sedang berada di tahap Interview aktif. Pantau jadwal dan follow-up secara berkala."
                            else ->
                                "Mulai catat lamaran kerja incaranmu dan pantau progres seleksi secara real-time."
                        }
                        Text(
                            text = agendaCopy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.heroOnDarkMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIAP MELANGKAH",
                                style = MonoEyebrowStyle,
                                color = colors.heroOnDarkMuted.copy(alpha = 0.75f)
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Lihat Jadwal",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                        color = colors.heroOnDark
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Buka jadwal",
                                        tint = colors.accentAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1B. Gojek-style Super-App Raised Quick Action Dock
            item(key = "quick_actions_dock") {
                TactileRaisedCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    lipDepth = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionTile(
                            label = "Catat Baru",
                            icon = Icons.Rounded.AddTask,
                            iconColor = colors.accentAmber,
                            bgColor = colors.amberSoftBg,
                            onClick = onAddApplicationClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            label = "Jadwal",
                            icon = Icons.Rounded.CalendarMonth,
                            iconColor = colors.accentBlue,
                            bgColor = colors.blueSoftBg,
                            onClick = onOpenScheduleTab,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            label = "Offer (${metrics.offerCount})",
                            icon = Icons.Rounded.WorkspacePremium,
                            iconColor = colors.accentEmerald,
                            bgColor = colors.emeraldSoftBg,
                            onClick = { onSelectFilter(ApplicationStatus.OFFER) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionTile(
                            label = "Portofolio",
                            icon = Icons.Rounded.FolderSpecial,
                            iconColor = colors.textPrimary,
                            bgColor = colors.recessedSurface,
                            onClick = onOpenProfileTab,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Pipeline Seleksi Header (Non-colliding two-tier editorial layout)
            item(key = "pipeline_header") {
                val offerPct = String.format(Locale.US, "%.1f%%", metrics.offerRatePercent)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentAmber)
                                )
                                Text(
                                    text = "RINGKASAN PROGRES",
                                    style = MonoEyebrowStyle,
                                    color = colors.accentAmber,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "Progres Lamaran",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = UnboundedFontFamily,
                                    fontSize = 20.sp
                                ),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(1.dp, colors.borderSubtle),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentEmerald)
                                )
                                Text(
                                    text = "${metrics.totalCount} Lamaran",
                                    style = MonoMetricStyle.copy(fontSize = 11.sp),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pantau tahap lamaranmu sekarang",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.amberSoftBg.copy(alpha = if (colors.isDark) 0.45f else 0.75f),
                            border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "↗ $offerPct ke Offer",
                                style = MonoEyebrowStyle.copy(fontSize = 9.5.sp, letterSpacing = 0.2.sp),
                                color = colors.accentAmber,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 3. Funnel Progress Bar Card (Raised 3D)
            item(key = "funnel_bar") {
                FunnelRatioCard(metrics = metrics)
            }

            // 4. Asymmetric 2-Column Hero Metric Bento (Interview Dark 3D + Offer Mint 3D)
            item(key = "hero_bento_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Obsidian Krusial Interview Card
                    AtmosphericDarkCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("bento_interview_card"),
                        cornerRadius = 22.dp,
                        onClick = { onSelectFilter(ApplicationStatus.INTERVIEW) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(15.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.accentAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentAmber)
                                    )
                                    Text(
                                        text = "KRUSIAL",
                                        style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                                        color = colors.accentAmber,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = metrics.interviewCount.toString().padStart(2, '0'),
                                    style = BigStatNumeralStyle,
                                    color = colors.heroOnDark
                                )
                                Text(
                                    text = "AGENDA",
                                    style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                                    color = colors.heroOnDarkMuted,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Column {
                                Text(
                                    text = "Interview HR & User",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.heroOnDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tahap penentu teknikal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.heroOnDarkMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = metrics.interviewCompaniesPreview,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.heroOnDark.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = colors.accentAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Right: Raised 3D Soft Emerald Offer Card with Colored Shadow & Emerald Lip
                    TactileRaisedCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("bento_offer_card"),
                        containerColor = colors.emeraldSoftBg,
                        borderColor = colors.emeraldBorder,
                        shadowColor = colors.accentEmerald.copy(alpha = 0.24f),
                        lipColor = colors.emeraldBorder,
                        cornerRadius = 22.dp,
                        lipDepth = 3.dp,
                        onClick = { onSelectFilter(ApplicationStatus.OFFER) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(15.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = colors.cardSurface.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, colors.emeraldBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.WorkspacePremium,
                                            contentDescription = null,
                                            tint = colors.accentEmerald,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "OFFER",
                                            style = MonoEyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 0.3.sp),
                                            color = colors.accentEmerald,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Text(
                                    text = "${metrics.winRatePercent}% Win",
                                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 0.1.sp),
                                    color = colors.accentEmerald,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = metrics.offerCount.toString().padStart(2, '0'),
                                    style = BigStatNumeralStyle,
                                    color = colors.accentEmerald
                                )
                                Text(
                                    text = "PAKET",
                                    style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Penawaran (Offer)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Negosiasi benefit & gaji",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = metrics.offerCompaniesPreview,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.accentEmerald,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = colors.accentEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Three-Column Compact Stage Strip (Uniform height, non-wrapping percent pill)
            item(key = "compact_stage_strip") {
                val safeTotal = metrics.totalCount.coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactStageCard(
                        label = "TERKIRIM",
                        count = metrics.appliedCount,
                        percent = (metrics.appliedCount * 100) / safeTotal,
                        subtitle = "Menunggu review",
                        stripeColor = colors.textMuted,
                        onClick = { onSelectFilter(ApplicationStatus.DILAMAR) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    CompactStageCard(
                        label = "SCREENING",
                        count = metrics.screeningCount,
                        percent = (metrics.screeningCount * 100) / safeTotal,
                        subtitle = "Lolos admin",
                        stripeColor = colors.accentBlue,
                        onClick = { onSelectFilter(ApplicationStatus.ADMINISTRASI) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    CompactStageCard(
                        label = "WISHLIST",
                        count = metrics.savedCount,
                        percent = (metrics.savedCount * 100) / safeTotal,
                        subtitle = "Simpan dulu",
                        stripeColor = colors.accentAmber,
                        onClick = { onSelectFilter(ApplicationStatus.DISIMPAN) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // 6. Horizontal Expressive Filter Chips
            item(key = "filter_pills") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterStagePill(
                            label = "Semua (${metrics.totalCount})",
                            dotColor = null,
                            selected = selectedFilter == null,
                            onClick = { onSelectFilter(null) }
                        )
                    }
                    val stageFilters = listOf(
                        Triple(ApplicationStatus.INTERVIEW, "Interview (${metrics.interviewCount})", colors.accentAmber),
                        Triple(ApplicationStatus.OFFER, "Offer (${metrics.offerCount})", colors.accentEmerald),
                        Triple(ApplicationStatus.ADMINISTRASI, "Screening (${metrics.screeningCount})", colors.accentBlue),
                        Triple(ApplicationStatus.DILAMAR, "Terkirim (${metrics.appliedCount})", colors.textMuted),
                        Triple(ApplicationStatus.DISIMPAN, "Disimpan (${metrics.savedCount})", colors.accentAmber),
                        Triple(ApplicationStatus.DITOLAK, "Ditolak (${metrics.rejectedCount})", colors.accentCrimson)
                    )
                    items(stageFilters) { (status, text, dot) ->
                        FilterStagePill(
                            label = text,
                            dotColor = dot,
                            selected = selectedFilter == status,
                            onClick = {
                                onSelectFilter(if (selectedFilter == status) null else status)
                            }
                        )
                    }
                }
            }

            // 7. Aktivitas Lamaran Terbaru Header
            item(key = "activity_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lamaran Terbaru",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Urutan: ${sortOrder.label} • ${filteredApps.size} ditampilkan",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .modernColoredShadow(
                                color = if (colors.isDark) Color.Black.copy(alpha = 0.4f) else Color(0xFF2B2118).copy(alpha = 0.12f),
                                cornerRadius = 22.dp,
                                blurRadius = 10.dp,
                                offsetY = 4.dp
                            )
                            .clip(CircleShape)
                            .background(colors.cardSurface)
                            .border(1.dp, colors.borderSubtle, CircleShape)
                            .tactileClickable(onClick = onCycleSort)
                            .testTag("beranda_sort_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = "Ubah urutan lamaran",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 8. Job Application Feed or Empty State
            if (filteredApps.isEmpty()) {
                item(key = "empty_feed") {
                    ExpressiveEmptyStateCard(
                        eyebrow = if (selectedFilter == null) "BELUM ADA LAMARAN" else "TIDAK ADA DI TAHAP INI",
                        title = if (selectedFilter == null) "Mulai Catat Lamaran Pertamamu" else "Belum Ada Lamaran ${selectedFilter.shortLabel}",
                        description = if (selectedFilter == null)
                            "Catat lowongan incaranmu lengkap dengan estimasi gaji, status seleksi, dan kontak HRD di satu tempat."
                        else
                            "Ketuk tombol di bawah untuk menambah lamaran baru atau reset filter untuk melihat semua lamaran.",
                        ctaLabel = if (selectedFilter == null) "+ Tambah Lamaran Pertama" else "Lihat Semua Lamaran",
                        onCtaClick = {
                            if (selectedFilter == null) onAddApplicationClick() else onSelectFilter(null)
                        }
                    )
                }
            } else {
                items(filteredApps, key = { it.id }) { app ->
                    JobApplicationCard(
                        application = app,
                        onClick = { onOpenApplicationDetail(app) },
                        onToggleStar = { onToggleStar(app) }
                    )
                }
            }
        }

        // Raised 3D Floating Super-App CTA (+ TAMBAH LAMARAN) — sleek elongated single-line pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            TactileRaisedButton(
                onClick = onAddApplicationClick,
                fillMaxWidth = false,
                modifier = Modifier.testTag("fab_catat_lamaran"),
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF040507),
                glowColor = colors.accentAmber.copy(alpha = 0.32f),
                cornerRadius = 999.dp,
                contentPadding = PaddingValues(start = 10.dp, end = 22.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFF59E0B), colors.accentAmber)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "TAMBAH LAMARAN",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.4.sp
                    ),
                    color = colors.heroOnDark,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .tactileClickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .modernColoredShadow(
                    color = iconColor.copy(alpha = 0.22f),
                    cornerRadius = 15.dp,
                    blurRadius = 10.dp,
                    offsetY = 4.dp
                )
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgColor, bgColor.copy(alpha = 0.72f))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = if (colors.isDark) 0.16f else 0.9f), iconColor.copy(alpha = 0.22f))
                    ),
                    shape = RoundedCornerShape(15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            ),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FunnelRatioCard(metrics: PipelineMetrics) {
    val colors = KarirKuThemeTokens.colors
    val activeFunnelTotal = (metrics.appliedCount + metrics.screeningCount + metrics.interviewCount + metrics.offerCount)
        .coerceAtLeast(1)

    val appliedFrac by animateFloatAsState(
        targetValue = metrics.appliedCount.toFloat() / activeFunnelTotal,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "appliedFrac"
    )
    val screenFrac by animateFloatAsState(
        targetValue = metrics.screeningCount.toFloat() / activeFunnelTotal,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "screenFrac"
    )
    val interviewFrac by animateFloatAsState(
        targetValue = metrics.interviewCount.toFloat() / activeFunnelTotal,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "interviewFrac"
    )
    val offerFrac by animateFloatAsState(
        targetValue = metrics.offerCount.toFloat() / activeFunnelTotal,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 280f),
        label = "offerFrac"
    )

    TactileRaisedCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        lipDepth = 2.5.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sebaran Tahap Lamaran",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Text(
                    text = "Otomatis",
                    style = MonoEyebrowStyle.copy(fontSize = 9.sp, letterSpacing = 0.3.sp),
                    color = colors.textMuted
                )
            }

            val trackBg = colors.recessedSurface
            val appliedColor = colors.textMuted.copy(alpha = 0.7f)
            val screenColor = colors.accentBlue
            val intervColor = colors.accentAmber
            val offerColor = colors.accentEmerald
            val hasData = metrics.appliedCount + metrics.screeningCount + metrics.interviewCount + metrics.offerCount > 0

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                val r = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(color = trackBg, size = size, cornerRadius = r)
                if (hasData && size.width > 0f) {
                    val gap = 4.dp.toPx()
                    val segments = listOf(
                        appliedFrac to appliedColor,
                        screenFrac to screenColor,
                        interviewFrac to intervColor,
                        offerFrac to offerColor
                    ).filter { it.first > 0.001f }

                    val totalGaps = gap * (segments.size - 1).coerceAtLeast(0)
                    val usableWidth = (size.width - totalGaps).coerceAtLeast(0f)
                    var startX = 0f
                    segments.forEach { (frac, col) ->
                        val remaining = (size.width - startX).coerceAtLeast(0f)
                        if (remaining > 0f) {
                            val segW = (usableWidth * frac).coerceAtLeast(6.dp.toPx()).coerceAtMost(remaining)
                            drawRoundRect(
                                color = col,
                                topLeft = Offset(startX, 0f),
                                size = Size(segW, size.height),
                                cornerRadius = r
                            )
                            startX += segW + gap
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FunnelLegendItem("Kirim (${metrics.appliedCount})", appliedColor)
                FunnelLegendItem("Screen (${metrics.screeningCount})", screenColor)
                FunnelLegendItem("Interv (${metrics.interviewCount})", intervColor)
                FunnelLegendItem("Offer (${metrics.offerCount})", offerColor)
            }
        }
    }
}

@Composable
private fun FunnelLegendItem(label: String, color: Color) {
    val colors = KarirKuThemeTokens.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = colors.textMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun CompactStageCard(
    label: String,
    count: Int,
    percent: Int,
    subtitle: String,
    stripeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    TactileRaisedCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        lipDepth = 2.5.dp,
        shadowColor = stripeColor.copy(alpha = if (colors.isDark) 0.2f else 0.14f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .heightIn(min = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 0.4.sp),
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = count.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = UnboundedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = stripeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$percent%",
                            style = MonoEyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 0.sp),
                            color = stripeColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FilterStagePill(
    label: String,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    Surface(
        modifier = Modifier
            .modernColoredShadow(
                color = if (selected) colors.heroDarkSurface.copy(alpha = 0.25f) else Color.Transparent,
                cornerRadius = 999.dp,
                blurRadius = 10.dp,
                offsetY = 4.dp
            )
            .clip(RoundedCornerShape(999.dp))
            .tactileClickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colors.heroDarkSurface else colors.cardSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) colors.heroDarkSurface else colors.borderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = if (selected) colors.heroOnDark else colors.textSecondary
            )
        }
    }
}
