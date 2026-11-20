package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobListingCategory
import com.example.data.KarirKuFormatters
import com.example.data.PublicJobListing
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.InAppWebBrowserDialog
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.UiState
import kotlin.math.ceil

private const val THREE_DAYS_MILLIS = 3L * 24L * 60L * 60L * 1000L
private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L

@Composable
fun InfoLowonganScreen(
    listingsState: UiState<List<PublicJobListing>>,
    currentUserId: String,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSaveListing: (
        existingId: String?,
        title: String,
        companyName: String,
        category: JobListingCategory,
        location: String,
        subtitle: String,
        externalUrl: String,
        expiresAtMillis: Long,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteListing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val colors = KarirKuThemeTokens.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<JobListingCategory?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var previewListing by remember { mutableStateOf<PublicJobListing?>(null) }
    var pendingDeleteListing by remember { mutableStateOf<PublicJobListing?>(null) }

    val listings = (listingsState as? UiState.Success)?.data.orEmpty()
    val filteredListings = remember(listings, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        listings.filter { item ->
            val matchesCat = selectedCategory == null || item.categoryEnum == selectedCategory
            val matchesQuery = q.isBlank() ||
                item.title.lowercase().contains(q) ||
                item.companyName.lowercase().contains(q) ||
                item.location.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.authorName.lowercase().contains(q)
            matchesCat && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("info_lowongan_screen")
    ) {
        // Editorial Top Bar with Tactile Back + Add Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.cardSurface)
                    .border(1.dp, colors.borderSubtle, CircleShape)
                    .tactileClickable(onClick = onNavigateBack)
                    .testTag("info_lowongan_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali ke Beranda",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "KOMUNITAS PUBLIK",
                    style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                    color = colors.accentEmerald,
                    maxLines = 1
                )
                Text(
                    text = "Info Lowongan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .tactileClickable { showAddSheet = true }
                    .testTag("info_lowongan_add_button"),
                shape = RoundedCornerShape(14.dp),
                color = colors.heroDarkSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = colors.accentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tambah",
                        style = MonoMetricStyle.copy(fontSize = 11.5.sp),
                        color = colors.heroOnDark,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("info_lowongan_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Atmospheric Public Board Banner
            item(key = "public_banner") {
                AtmosphericDarkCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.emeraldBright.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, colors.emeraldBright.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PulsingStatusDot(color = colors.emeraldBright, size = 6.dp)
                                    Text(
                                        text = "TERBUKA UNTUK SEMUA",
                                        style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                                        color = colors.emeraldBright
                                    )
                                }
                            }

                            Text(
                                text = "${listings.size} Lowongan",
                                style = MonoMetricStyle.copy(fontSize = 11.5.sp),
                                color = colors.heroOnDark
                            )
                        }

                        Text(
                            text = "Papan Lowongan Komunitas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = UnboundedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = colors.heroOnDark
                        )

                        Text(
                            text = "Siapa pun pengguna KarirKu bisa membagikan info lowongan terbaru di sini. Ketuk kartu untuk melihat tautan langsung di dalam aplikasi.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = colors.heroOnDarkMuted
                        )
                    }
                }
            }

            // 2. Compact 48dp Search Bar
            item(key = "search_bar") {
                KarirKuCompactTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Cari posisi, perusahaan, lokasi...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.testTag("info_lowongan_search")
                )
            }

            // 3. Category Filter Pills
            item(key = "category_chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        CategoryFilterChip(
                            label = "Semua (${listings.size})",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(JobListingCategory.entries) { cat ->
                        val count = listings.count { it.categoryEnum == cat }
                        CategoryFilterChip(
                            label = if (count > 0) "${cat.label} ($count)" else cat.label,
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = if (selectedCategory == cat) null else cat
                            }
                        )
                    }
                }
            }

            // 4. Error / Loading / Feed
            when (listingsState) {
                is UiState.Loading -> {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.accentAmber)
                        }
                    }
                }

                is UiState.Error -> {
                    item(key = "error") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.crimsonSoftBg,
                            border = BorderStroke(1.dp, colors.crimsonBorder)
                        ) {
                            Text(
                                text = listingsState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accentCrimson,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    if (filteredListings.isEmpty()) {
                        item(key = "empty_listings") {
                            ExpressiveEmptyStateCard(
                                eyebrow = if (listings.isEmpty()) "BELUM ADA LOWONGAN" else "TIDAK DITEMUKAN",
                                title = if (listings.isEmpty()) "Jadilah yang Pertama Berbagi!" else "Tidak Ada Lowongan yang Cocok",
                                description = if (listings.isEmpty())
                                    "Punya info lowongan menarik dari LinkedIn, Glints, atau situs perusahaan? Bagikan agar bermanfaat untuk pengguna KarirKu lainnya."
                                else
                                    "Coba ubah kata kunci pencarian atau pilih kategori lain untuk melihat lowongan tersedia.",
                                ctaLabel = if (listings.isEmpty()) "+ Bagikan Lowongan Pertama" else "Reset Filter",
                                onCtaClick = {
                                    if (listings.isEmpty()) {
                                        showAddSheet = true
                                    } else {
                                        searchQuery = ""
                                        selectedCategory = null
                                    }
                                }
                            )
                        }
                    } else {
                        items(filteredListings, key = { it.id }) { listing ->
                            PublicJobListingRowCard(
                                listing = listing,
                                isAuthor = listing.authorUid == currentUserId,
                                onOpen = {
                                    if (listing.externalUrl.isNotBlank()) {
                                        previewListing = listing
                                    }
                                },
                                onDelete = { pendingDeleteListing = listing }
                            )
                        }
                    }
                }
            }
        }
    }

    // Open Job Link directly inside KarirKu's Modern In-App Browser
    previewListing?.let { job ->
        InAppWebBrowserDialog(
            title = "${job.title} — ${job.companyName}",
            url = job.externalUrl,
            onDismiss = { previewListing = null }
        )
    }

    // Add Public Job Listing Bottom Sheet
    if (showAddSheet) {
        AddPublicJobBottomSheet(
            isSaving = isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { title, company, category, location, subtitle, url, expiresAtMillis ->
                onSaveListing(null, title, company, category, location, subtitle, url, expiresAtMillis) {
                    showAddSheet = false
                }
            }
        )
    }

    // Delete Confirmation Dialog (only for author)
    pendingDeleteListing?.let { job ->
        AlertDialog(
            onDismissRequest = { pendingDeleteListing = null },
            title = { Text("Hapus Info Lowongan?") },
            text = { Text("Lowongan \"${job.title}\" di ${job.companyName} akan dihapus dari papan lowongan publik.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = job.id
                        pendingDeleteListing = null
                        onDeleteListing(id)
                    }
                ) {
                    Text("Hapus", color = colors.accentCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteListing = null }) { Text("Batal") }
            }
        )
    }
}

/**
 * Mirrors ProfilScreen's modern `CareerDocumentRowCard` aesthetic and adds:
 * - Seamless integrated rounded top-right corner ribbon "Baru ditambahkan" (auto-hides after 3 days)
 * - Contributor account attribution (avatar initials + name)
 * - Date added timestamp & expiration date / countdown badge
 */
@Composable
private fun PublicJobListingRowCard(
    listing: PublicJobListing,
    isAuthor: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var showMenu by remember { mutableStateOf(false) }

    val (icon, tintBg, tintFg) = when (listing.categoryEnum) {
        JobListingCategory.FULL_TIME -> Triple(Icons.Outlined.BusinessCenter, colors.amberSoftBg, colors.accentAmber)
        JobListingCategory.REMOTE -> Triple(Icons.Outlined.LaptopMac, colors.blueSoftBg, colors.accentBlue)
        JobListingCategory.MAGANG -> Triple(Icons.Outlined.School, colors.emeraldSoftBg, colors.accentEmerald)
        JobListingCategory.FREELANCE -> Triple(Icons.Outlined.WorkOutline, colors.crimsonSoftBg, colors.accentCrimson)
        JobListingCategory.LAINNYA -> Triple(Icons.Outlined.Public, colors.recessedSurface, colors.textPrimary)
    }

    val hasLink = listing.externalUrl.isNotBlank()
    val hostPreview = remember(listing.externalUrl) {
        if (!hasLink) "" else try {
            val normalized = if (listing.externalUrl.startsWith("http://") || listing.externalUrl.startsWith("https://")) {
                listing.externalUrl
            } else {
                "https://${listing.externalUrl}"
            }
            Uri.parse(normalized).host?.removePrefix("www.") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    val companyLocationLine = remember(listing.companyName, listing.location) {
        if (listing.location.isNotBlank()) "${listing.companyName} • ${listing.location}"
        else listing.companyName
    }

    val nowMillis = remember { System.currentTimeMillis() }
    val createdMillis = listing.createdAt?.toDate()?.time
    // Automatically visible when added within the last 3 days (72 hours), hidden after 3 days
    val isNewlyAdded = createdMillis == null || (nowMillis - createdMillis) in 0L..THREE_DAYS_MILLIS

    val authorInitials = remember(listing.authorName) {
        listing.authorName.trim()
            .split(Regex("\\s+"))
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "KK" }
    }

    val addedDateLabel = remember(createdMillis) {
        if (createdMillis != null) {
            "${KarirKuFormatters.formatDateShort(createdMillis)} • ${KarirKuFormatters.formatRelativeTime(listing.createdAt)}"
        } else {
            "Baru saja"
        }
    }

    val isExpired = listing.expiresAtMillis > 0L && listing.expiresAtMillis < nowMillis
    val daysUntilExpiry = if (listing.expiresAtMillis > nowMillis) {
        ceil((listing.expiresAtMillis - nowMillis).toDouble() / ONE_DAY_MILLIS.toDouble()).toLong()
    } else 0L

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("public_job_card_${listing.id}"),
        containerColor = colors.cardSurface,
        borderColor = if (isNewlyAdded) colors.emeraldBorder else colors.borderSubtle,
        cornerRadius = 20.dp,
        lipDepth = 2.5.dp,
        onClick = if (hasLink) onOpen else null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Integrated Rounded Corner Ribbon ("Baru ditambahkan") seamlessly fused to TopEnd
            if (isNewlyAdded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                            )
                        )
                        .padding(horizontal = 11.dp, vertical = 4.dp)
                        .testTag("public_job_new_badge_${listing.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "Baru ditambahkan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = if (isNewlyAdded) 28.dp else 14.dp,
                        bottom = 12.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main Row matching Profil -> Dokumen & Portofolio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .modernColoredShadow(
                                color = tintFg.copy(alpha = 0.20f),
                                cornerRadius = 14.dp,
                                blurRadius = 10.dp,
                                offsetY = 4.dp
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(tintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = tintFg, modifier = Modifier.size(22.dp))
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tintBg
                            ) {
                                Text(
                                    text = listing.categoryEnum.label.uppercase(),
                                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp, letterSpacing = 0.3.sp),
                                    color = tintFg,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (hostPreview.isNotBlank()) {
                                Text(
                                    text = hostPreview,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = companyLocationLine,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val extraInfo = listing.subtitle.trim()
                        if (extraInfo.isNotBlank()) {
                            Text(
                                text = extraInfo,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Modern action pill + minimalist overflow menu matching Dokumen & Portofolio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (hasLink) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .tactileClickable(onClick = onOpen)
                                    .testTag("public_job_open_${listing.id}"),
                                shape = RoundedCornerShape(999.dp),
                                color = colors.recessedSurface,
                                border = BorderStroke(1.dp, colors.borderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Lihat",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = "Buka tautan lowongan",
                                        tint = colors.accentAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        if (isAuthor) {
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("public_job_more_${listing.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "Opsi lowongan",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    containerColor = colors.cardSurface,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (hasLink) {
                                        DropdownMenuItem(
                                            text = { Text("Buka Lowongan", color = colors.textPrimary) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.OpenInNew,
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                onOpen()
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Hapus Lowongan",
                                                color = colors.accentCrimson,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteOutline,
                                                contentDescription = null,
                                                tint = colors.accentCrimson,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onDelete()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.6f))

                // Footer Metadata Strip: Author Account + Added Date + Expiration Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(colors.heroDarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = authorInitials,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = colors.heroOnDark
                                )
                            }
                            Text(
                                text = if (isAuthor) "${listing.authorName} (Kamu)" else listing.authorName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Ditambah $addedDateLabel",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = colors.textMuted,
                                maxLines = 1
                            )
                        }
                    }

                    // Expiry Status Chip
                    val (expiryBg, expiryFg, expiryText) = when {
                        isExpired -> Triple(
                            colors.crimsonSoftBg,
                            colors.accentCrimson,
                            "Masa berlaku habis (${KarirKuFormatters.formatDateShort(listing.expiresAtMillis)})"
                        )

                        listing.expiresAtMillis > 0L && daysUntilExpiry <= 3L -> Triple(
                            colors.amberSoftBg,
                            colors.amberText,
                            "Berakhir s/d ${KarirKuFormatters.formatDateShort(listing.expiresAtMillis)} • Sisa $daysUntilExpiry hari"
                        )

                        listing.expiresAtMillis > 0L -> Triple(
                            colors.emeraldSoftBg,
                            colors.accentEmerald,
                            "Berlaku s/d ${KarirKuFormatters.formatDateShort(listing.expiresAtMillis)} • Sisa $daysUntilExpiry hari"
                        )

                        else -> Triple(
                            colors.recessedSurface,
                            colors.textMuted,
                            "Berlaku hingga lowongan ditutup"
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = expiryBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpired) Icons.Outlined.EventBusy else Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = expiryFg,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = expiryText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = expiryFg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .tactileClickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) colors.heroDarkSurface else colors.cardSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) colors.heroDarkSurface else colors.borderSubtle
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = if (selected) colors.heroOnDark else colors.textSecondary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddPublicJobBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, JobListingCategory, String, String, String, Long) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(JobListingCategory.FULL_TIME) }
    var location by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    // Default 14-day validity period so users see clear expiration date immediately
    val initialExpiry = remember { System.currentTimeMillis() + 14L * ONE_DAY_MILLIS }
    var expiresAtMillis by remember { mutableLongStateOf(initialExpiry) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }

    val canSubmit = title.isNotBlank() && company.isNotBlank() && url.isNotBlank() && !isSaving

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "BAGIKAN KE KOMUNITAS",
                    style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                    color = colors.accentEmerald
                )
                Text(
                    text = "Tambah Info Lowongan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.textPrimary
                )
            }

            // Public visibility info strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.emeraldSoftBg,
                border = BorderStroke(1.dp, colors.emeraldBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = colors.accentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Lowongan yang kamu tambahkan bersifat publik dan bisa dilihat oleh pengguna KarirKu lainnya.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = colors.textPrimary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("POSISI / JUDUL LOWONGAN *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Contoh: UI/UX Designer / Staff Admin",
                    modifier = Modifier.testTag("add_public_job_title")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("NAMA PERUSAHAAN *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = company,
                    onValueChange = { company = it },
                    placeholder = "Contoh: PT GoTo Gojek Tokopedia",
                    modifier = Modifier.testTag("add_public_job_company")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("KATEGORI", style = MonoEyebrowStyle, color = colors.textMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JobListingCategory.entries.forEach { cat ->
                        val selected = category == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tactileClickable { category = cat },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.heroDarkSurface else colors.recessedSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.heroDarkSurface else colors.borderSubtle
                            )
                        ) {
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) colors.heroOnDark else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("LOKASI / SISTEM KERJA", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "Contoh: Jakarta Selatan • Hybrid",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Masa Berlaku / Expired Lowongan
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MASA BERLAKU (EXPIRED)", style = MonoEyebrowStyle, color = colors.textMuted)
                    Text(
                        text = if (expiresAtMillis > 0L) "s/d ${KarirKuFormatters.formatDateShort(expiresAtMillis)}" else "Tanpa batas",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.accentEmerald
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    val now = System.currentTimeMillis()
                    listOf(
                        "7 Hari" to (now + 7L * ONE_DAY_MILLIS),
                        "14 Hari" to (now + 14L * ONE_DAY_MILLIS),
                        "30 Hari" to (now + 30L * ONE_DAY_MILLIS),
                        "Tanpa Batas" to 0L
                    ).forEach { (label, targetMs) ->
                        val isPicked = when {
                            targetMs == 0L -> expiresAtMillis == 0L
                            expiresAtMillis == 0L -> false
                            else -> kotlin.math.abs(expiresAtMillis - targetMs) < ONE_DAY_MILLIS / 2
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .tactileClickable { expiresAtMillis = targetMs },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPicked) colors.heroDarkSurface else colors.recessedSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isPicked) colors.heroDarkSurface else colors.borderSubtle
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isPicked) colors.heroOnDark else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .tactileClickable { showExpiryDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = colors.amberSoftBg,
                        border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = colors.amberText,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Pilih Tanggal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.amberText
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("CATATAN SINGKAT (OPSIONAL)", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    placeholder = "Contoh: Min. 1 thn pengalaman, kisaran 7-10jt"
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("TAUTAN LOWONGAN (URL) *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "https://glints.com/... atau linkedin.com/jobs/...",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = colors.accentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("add_public_job_url")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TactileRaisedButton(
                onClick = { onSave(title, company, category, location, subtitle, url, expiresAtMillis) },
                enabled = canSubmit,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF050608),
                glowColor = colors.accentEmerald.copy(alpha = 0.25f),
                modifier = Modifier.testTag("add_public_job_submit")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = colors.heroOnDark,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = if (isSaving) "Membagikan..." else "Bagikan Lowongan",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }

    if (showExpiryDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (expiresAtMillis > 0L) expiresAtMillis else System.currentTimeMillis() + 14L * ONE_DAY_MILLIS
        )
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { picked ->
                            expiresAtMillis = picked
                        }
                        showExpiryDatePicker = false
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
