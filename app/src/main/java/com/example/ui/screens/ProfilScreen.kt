package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AvailabilityStatus
import com.example.data.CareerDocument
import com.example.data.DocumentCategory
import com.example.data.KarirKuFormatters
import com.example.data.UserProfile
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.InAppWebBrowserDialog
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.UserAvatarCircle
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.KarirKuDashboardState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilScreen(
    state: KarirKuDashboardState,
    userEmail: String?,
    fallbackPhotoUrl: String?,
    fallbackInitials: String,
    isSaving: Boolean,
    onUpdateIdentity: (String, String, String, AvailabilityStatus, () -> Unit) -> Unit,
    onUpdatePreferences: (List<String>, Long, Long, List<String>, () -> Unit) -> Unit,
    onUpdateSettings: (Boolean, Boolean) -> Unit,
    onSaveDocument: (String?, String, DocumentCategory, String, String, Boolean, () -> Unit) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val profile = state.profile
    val metrics = state.metrics

    var showEditIdentitySheet by remember { mutableStateOf(false) }
    var showEditPreferencesSheet by remember { mutableStateOf(false) }
    var showAddDocumentSheet by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var previewDocument by remember { mutableStateOf<CareerDocument?>(null) }

    val displayName = profile?.displayName?.ifBlank { "Pengguna KarirKu" } ?: "Pengguna KarirKu"
    val headline = profile?.headline?.ifBlank { "Profesional Karier" } ?: "Profesional Karier"
    val location = profile?.location?.ifBlank { "Indonesia" } ?: "Indonesia"
    val availability = profile?.availabilityEnum ?: AvailabilityStatus.OPEN_TO_WORK
    val photoUrl = profile?.photoUrl ?: fallbackPhotoUrl

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profil_lazy_column"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Dark Atmospheric Profile Card
        item(key = "dossier_card") {
            AtmosphericDarkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentCrimson)
                            )
                            Text(
                                text = "PROFIL KARIER • 2026",
                                style = MonoEyebrowStyle,
                                color = colors.heroOnDark
                            )
                        }
                    }

                    // Circular Avatar with Edit Pencil Badge
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .clickable { showEditIdentitySheet = true }
                            .testTag("profil_edit_identity_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarCircle(
                            photoUrl = photoUrl,
                            initials = fallbackInitials,
                            size = 94.dp
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, colors.heroDarkSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit profil",
                                tint = colors.heroDarkSurface,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = UnboundedFontFamily,
                                fontSize = 20.sp
                            ),
                            color = colors.heroOnDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.heroOnDarkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status + Location Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                PulsingStatusDot(
                                    color = colors.emeraldBright,
                                    size = 7.dp
                                )
                                Text(
                                    text = availability.label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.heroOnDark
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = colors.heroOnDarkMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.heroOnDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 3-Column Live Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatTile(
                            eyebrow = "TOTAL",
                            value = metrics.totalCount.toString(),
                            subtitle = "Dilamar",
                            valueColor = colors.heroOnDark,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            eyebrow = "RESPON",
                            value = "${metrics.positiveResponseRate}%",
                            subtitle = "Respon Positif",
                            valueColor = colors.emeraldBright,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            eyebrow = "ESTIMASI",
                            value = KarirKuFormatters.formatStatAverageMillions(metrics.averageSalaryMidpoint),
                            subtitle = "Rata-rata Gaji",
                            valueColor = colors.heroOnDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Dokumen & Portofolio Section (Opens inside In-App Browser)
        item(key = "documents_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "CV & PORTOFOLIO",
                        style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                        color = colors.accentAmber,
                        maxLines = 1
                    )
                    Text(
                        text = "Dokumen & Portofolio",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .tactileClickable { showAddDocumentSheet = true }
                        .testTag("profil_add_document_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (state.documents.isEmpty()) {
            item(key = "documents_empty") {
                TactileRaisedCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.cardSurface,
                    borderColor = colors.borderSubtle,
                    cornerRadius = 18.dp,
                    lipDepth = 2.5.dp,
                    onClick = { showAddDocumentSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.recessedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.textPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tambahkan CV, Portofolio, atau Sertifikat",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Simpan tautan Figma, Behance, GitHub, LinkedIn, atau CV untuk dibuka langsung di aplikasi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(state.documents, key = { it.id }) { doc ->
                CareerDocumentRowCard(
                    document = doc,
                    onOpen = {
                        if (doc.externalUrl.isNotBlank()) {
                            previewDocument = doc
                        }
                    },
                    onDelete = { onDeleteDocument(doc.id) }
                )
            }
        }

        // 3. Preferensi Karier Section
        item(key = "preferences_section") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "TARGET KARIER",
                            style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                            color = colors.accentAmber,
                            maxLines = 1
                        )
                        Text(
                            text = "Preferensi Karier",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { showEditPreferencesSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(colors.cardSurface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(11.dp))
                            .testTag("profil_edit_preferences_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit preferensi karier",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TactileRaisedCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.cardSurface,
                    borderColor = colors.borderSubtle,
                    cornerRadius = 22.dp,
                    lipDepth = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("TARGET POSISI", style = MonoEyebrowStyle, color = colors.textMuted)
                            val roles = profile?.targetRoles.orEmpty()
                            if (roles.isEmpty()) {
                                Text(
                                    text = "Belum diatur — ketuk ikon pensil untuk menambahkan posisi incaran.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    roles.forEach { role ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.recessedSurface,
                                            border = BorderStroke(1.dp, colors.borderSubtle)
                                        ) {
                                            Text(
                                                text = role,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("EKSPEKTASI GAJI", style = MonoEyebrowStyle, color = colors.textMuted)
                            val minSal = profile?.expectedSalaryMin ?: 0L
                            val maxSal = profile?.expectedSalaryMax ?: 0L
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = if (minSal <= 0L && maxSal <= 0L) "Belum diatur"
                                    else "${KarirKuFormatters.formatIdrFull(minSal)} - ${KarirKuFormatters.formatIdrFull(maxSal)}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = UnboundedFontFamily,
                                        fontSize = 15.sp
                                    ),
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "/ bulan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                        }

                        HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("SISTEM & LOKASI KERJA", style = MonoEyebrowStyle, color = colors.textMuted)
                            val workTypes = profile?.preferredWorkTypes.orEmpty()
                            if (workTypes.isEmpty()) {
                                Text("Belum diatur", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    workTypes.forEach { wt ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.recessedSurface,
                                            border = BorderStroke(1.dp, colors.borderSubtle)
                                        ) {
                                            Text(
                                                text = wt,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Pengaturan Section
        item(key = "settings_section") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "PENGATURAN",
                        style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                        color = colors.accentAmber,
                        maxLines = 1
                    )
                    Text(
                        text = "Pengaturan Aplikasi",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TactileRaisedCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.cardSurface,
                    borderColor = colors.borderSubtle,
                    cornerRadius = 22.dp,
                    lipDepth = 3.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        val remindersOn = profile?.followUpRemindersEnabled ?: true
                        val darkOn = profile?.darkModeEnabled ?: false

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = colors.textPrimary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengingat Follow-up",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Tampilkan penanda saat ada jadwal interview aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                            Switch(
                                checked = remindersOn,
                                onCheckedChange = { checked -> onUpdateSettings(checked, darkOn) },
                                colors = SwitchDefaults.colors(checkedTrackColor = colors.heroDarkSurface)
                            )
                        }

                        HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = colors.textPrimary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mode Gelap",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Tampilan gelap yang nyaman di mata",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                            Switch(
                                checked = darkOn,
                                onCheckedChange = { checked -> onUpdateSettings(remindersOn, checked) },
                                modifier = Modifier.testTag("profil_dark_mode_switch"),
                                colors = SwitchDefaults.colors(checkedTrackColor = colors.accentAmber)
                            )
                        }

                        if (!userEmail.isNullOrBlank()) {
                            HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Email Akun", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                Text(
                                    text = userEmail,
                                    style = MonoMetricStyle.copy(fontSize = 11.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showSignOutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profil_sign_out_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.crimsonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentCrimson)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cta_sign_out),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // In-App WebView Browser Dialog for Document / Portfolio Links
    previewDocument?.let { doc ->
        InAppWebBrowserDialog(
            title = doc.title,
            url = doc.externalUrl,
            onDismiss = { previewDocument = null }
        )
    }

    // Dialogs & Bottom Sheets
    if (showEditIdentitySheet) {
        EditIdentityBottomSheet(
            profile = profile,
            fallbackName = displayName,
            isSaving = isSaving,
            onDismiss = { showEditIdentitySheet = false },
            onSave = { name, hl, loc, avail ->
                onUpdateIdentity(name, hl, loc, avail) { showEditIdentitySheet = false }
            }
        )
    }

    if (showEditPreferencesSheet) {
        EditPreferencesBottomSheet(
            profile = profile,
            isSaving = isSaving,
            onDismiss = { showEditPreferencesSheet = false },
            onSave = { roles, minS, maxS, workTypes ->
                onUpdatePreferences(roles, minS, maxS, workTypes) { showEditPreferencesSheet = false }
            }
        )
    }

    if (showAddDocumentSheet) {
        AddDocumentBottomSheet(
            isSaving = isSaving,
            onDismiss = { showAddDocumentSheet = false },
            onSave = { title, cat, sub, url, verified ->
                onSaveDocument(null, title, cat, sub, url, verified) { showAddDocumentSheet = false }
            }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Keluar dari KarirKu?") },
            text = { Text("Data lamaranmu tetap tersimpan aman di cloud dan akan langsung muncul kembali saat kamu masuk lagi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOutClick()
                    }
                ) {
                    Text("Keluar", color = colors.accentCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ProfileStatTile(
    eyebrow: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eyebrow,
                    style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                    color = colors.heroOnDarkMuted,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = colors.heroOnDarkMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = valueColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = colors.heroOnDarkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CareerDocumentRowCard(
    document: CareerDocument,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val (icon, tintBg, tintFg) = when (document.categoryEnum) {
        DocumentCategory.CV -> Triple(Icons.Outlined.Description, colors.crimsonSoftBg, colors.accentCrimson)
        DocumentCategory.PORTFOLIO -> Triple(Icons.Outlined.Layers, colors.blueSoftBg, colors.accentBlue)
        DocumentCategory.CERTIFICATION -> Triple(Icons.Outlined.VerifiedUser, colors.emeraldSoftBg, colors.accentEmerald)
        DocumentCategory.OTHER -> Triple(Icons.Outlined.Description, colors.recessedSurface, colors.textPrimary)
    }

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profil_doc_card_${document.id}"),
        containerColor = colors.cardSurface,
        borderColor = colors.borderSubtle,
        cornerRadius = 18.dp,
        lipDepth = 2.5.dp,
        onClick = if (document.externalUrl.isNotBlank()) onOpen else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .modernColoredShadow(
                        color = tintFg.copy(alpha = 0.22f),
                        cornerRadius = 12.dp,
                        blurRadius = 10.dp,
                        offsetY = 4.dp
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(tintBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tintFg, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = document.subtitle.ifBlank { document.categoryEnum.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (document.verified) colors.accentEmerald else colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (document.externalUrl.isNotBlank()) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("profil_doc_open_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = "Buka tautan di dalam aplikasi",
                        tint = colors.accentBlue,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Hapus dokumen",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditIdentityBottomSheet(
    profile: UserProfile?,
    fallbackName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, AvailabilityStatus) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var displayName by remember { mutableStateOf(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName) }
    var headline by remember { mutableStateOf(profile?.headline ?: "") }
    var location by remember { mutableStateOf(profile?.location ?: "") }
    var availability by remember { mutableStateOf(profile?.availabilityEnum ?: AvailabilityStatus.OPEN_TO_WORK) }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("PROFIL SAYA", style = MonoEyebrowStyle, color = colors.accentAmber)
            Text("Edit Profil", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NAMA LENGKAP *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = displayName,
                    onValueChange = { displayName = it.take(100) },
                    placeholder = "Nama lengkapmu",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PROFESI / POSISI SAAT INI", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = headline,
                    onValueChange = { headline = it.take(120) },
                    placeholder = "Contoh: Senior Product & UX Designer",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DOMISILI / LOKASI", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = location,
                    onValueChange = { location = it.take(100) },
                    placeholder = "Contoh: Jakarta Selatan",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("STATUS KARIER", style = MonoEyebrowStyle, color = colors.textMuted)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvailabilityStatus.entries.forEach { st ->
                    val sel = availability == st
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { availability = st },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sel) colors.heroDarkSurface else colors.recessedSurface
                    ) {
                        Text(
                            text = st.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (sel) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            TactileRaisedButton(
                onClick = {
                    if (displayName.trim().isNotEmpty()) {
                        onSave(displayName, headline, location, availability)
                    }
                },
                enabled = !isSaving && displayName.trim().isNotEmpty(),
                isLoading = isSaving,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF06070A),
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Profil", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditPreferencesBottomSheet(
    profile: UserProfile?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<String>, Long, Long, List<String>) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var rolesCsv by remember { mutableStateOf(profile?.targetRoles?.joinToString(", ") ?: "") }
    var minSalText by remember {
        mutableStateOf(profile?.expectedSalaryMin?.takeIf { it > 0L }?.toString() ?: "")
    }
    var maxSalText by remember {
        mutableStateOf(profile?.expectedSalaryMax?.takeIf { it > 0L }?.toString() ?: "")
    }
    var workTypesCsv by remember { mutableStateOf(profile?.preferredWorkTypes?.joinToString(", ") ?: "Hybrid, Remote") }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TARGET KARIER", style = MonoEyebrowStyle, color = colors.accentAmber)
            Text("Preferensi Karier", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TARGET POSISI (PISAHKAN KOMA)", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = rolesCsv,
                    onValueChange = { rolesCsv = it },
                    placeholder = "Lead UX Designer, Senior Product Designer",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pref_roles_input")
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickRoles = listOf(
                    "Product Designer",
                    "Software Engineer",
                    "Product Manager",
                    "Data Analyst",
                    "UI/UX Researcher"
                )
                quickRoles.forEach { suggestion ->
                    val alreadyAdded = rolesCsv.split(",").any { it.trim().equals(suggestion, ignoreCase = true) }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .tactileClickable {
                                if (!alreadyAdded) {
                                    rolesCsv = if (rolesCsv.isBlank()) suggestion else "$rolesCsv, $suggestion"
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (alreadyAdded) colors.emeraldSoftBg else colors.recessedSurface,
                        border = BorderStroke(1.dp, if (alreadyAdded) colors.emeraldBorder else colors.borderSubtle)
                    ) {
                        Text(
                            text = if (alreadyAdded) "✓ $suggestion" else "+ $suggestion",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (alreadyAdded) colors.accentEmerald else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("GAJI MINIMAL", style = MonoEyebrowStyle, color = colors.textMuted, maxLines = 1)
                    KarirKuCompactTextField(
                        value = minSalText,
                        onValueChange = { minSalText = it.filter { c -> c.isDigit() }.take(10) },
                        prefixText = "Rp",
                        placeholder = "15000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pref_salary_min_input")
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("GAJI MAKSIMAL", style = MonoEyebrowStyle, color = colors.textMuted, maxLines = 1)
                    KarirKuCompactTextField(
                        value = maxSalText,
                        onValueChange = { maxSalText = it.filter { c -> c.isDigit() }.take(10) },
                        prefixText = "Rp",
                        placeholder = "25000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pref_salary_max_input")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SISTEM & LOKASI KERJA", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = workTypesCsv,
                    onValueChange = { workTypesCsv = it },
                    placeholder = "Hybrid Jakarta, Remote Global",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pref_work_types_input")
                )
            }

            TactileRaisedButton(
                onClick = {
                    val roles = rolesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(10)
                    val workTypes = workTypesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(10)
                    val minS = minSalText.toLongOrNull() ?: 0L
                    val maxS = maxSalText.toLongOrNull() ?: minS
                    onSave(roles, minS, maxOf(minS, maxS), workTypes)
                },
                enabled = !isSaving,
                isLoading = isSaving,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF06070A),
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Preferensi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddDocumentBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, DocumentCategory, String, String, Boolean) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DocumentCategory.CV) }
    var subtitle by remember { mutableStateOf("") }
    var externalUrl by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        val fileName = if (nameIdx >= 0) cursor.getString(nameIdx) else "Dokumen.pdf"
                        val sizeBytes = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L
                        if (title.isBlank()) title = fileName.substringBeforeLast(".")
                        val sizeStr = if (sizeBytes >= 1_048_576L) {
                            String.format(java.util.Locale.US, "%.1f MB", sizeBytes / 1_048_576.0)
                        } else if (sizeBytes >= 1024L) {
                            "${sizeBytes / 1024L} KB"
                        } else {
                            "Berkas Lokal"
                        }
                        subtitle = "$fileName • $sizeStr"
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("BERKAS KARIER", style = MonoEyebrowStyle, color = colors.accentAmber)
            Text("Tambah Dokumen & Portofolio", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentCategory.entries.forEach { cat ->
                    val sel = category == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { category = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sel) colors.heroDarkSurface else colors.recessedSurface
                    ) {
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (sel) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { filePicker.launch(arrayOf("application/pdf", "application/msword", "image/*")) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Pilih Berkas dari Perangkat (Opsional)")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("JUDUL DOKUMEN *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = title,
                    onValueChange = { title = it.take(120) },
                    placeholder = "Contoh: CV ATS-Friendly 2026",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_title_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("KETERANGAN / VERSI", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it.take(160) },
                    placeholder = "Contoh: Behance • 8 Proyek Unggulan",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_subtitle_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TAUTAN (FIGMA / LINKEDIN / DRIVE)", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = externalUrl,
                    onValueChange = { externalUrl = it.take(500) },
                    placeholder = "https://...",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_url_input")
                )
                Text(
                    text = "Tautan akan dibuka langsung di browser dalam aplikasi.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textMuted
                )
            }

            TactileRaisedButton(
                onClick = {
                    if (title.trim().isNotEmpty()) {
                        onSave(
                            title,
                            category,
                            subtitle.ifBlank { category.label },
                            externalUrl,
                            category == DocumentCategory.CERTIFICATION
                        )
                    }
                },
                enabled = !isSaving && title.trim().isNotEmpty(),
                isLoading = isSaving,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF06070A),
                cornerRadius = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doc_save_button")
            ) {
                Text("Simpan Dokumen", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
