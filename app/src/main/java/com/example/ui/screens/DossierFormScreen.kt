package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.ApplicationStatus
import com.example.data.CareerDocument
import com.example.data.ContractType
import com.example.data.JobApplication
import com.example.data.KarirKuFormatters
import com.example.data.WorkMode
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.CompanyMonogramBadge
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DossierFormScreen(
    existingApplication: JobApplication?,
    savedProfileDocuments: List<CareerDocument>,
    expectedSalaryBaseline: Long,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSave: (
        existingId: String?,
        existingDossierCode: String?,
        roleTitle: String,
        companyName: String,
        location: String,
        status: ApplicationStatus,
        workMode: WorkMode,
        contractType: ContractType,
        salaryMin: Long,
        salaryMax: Long,
        appliedDateMillis: Long,
        targetResponseMillis: Long,
        notes: String,
        hrContact: String,
        attachments: List<String>,
        starred: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current

    val dossierCode = remember(existingApplication) {
        existingApplication?.dossierCode?.takeIf { it.isNotBlank() }
            ?: KarirKuFormatters.generateDossierCode()
    }

    var roleTitle by remember(existingApplication) { mutableStateOf(existingApplication?.roleTitle ?: "") }
    var companyName by remember(existingApplication) { mutableStateOf(existingApplication?.companyName ?: "") }
    var location by remember(existingApplication) { mutableStateOf(existingApplication?.location ?: "") }
    var status by remember(existingApplication) {
        mutableStateOf(existingApplication?.statusEnum ?: ApplicationStatus.DILAMAR)
    }
    var workMode by remember(existingApplication) {
        mutableStateOf(existingApplication?.workModeEnum ?: WorkMode.HYBRID)
    }
    var contractType by remember(existingApplication) {
        mutableStateOf(existingApplication?.contractTypeEnum ?: ContractType.FULL_TIME)
    }
    var salaryMinText by remember(existingApplication) {
        mutableStateOf(existingApplication?.salaryMin?.takeIf { it > 0L }?.toString() ?: "")
    }
    var salaryMaxText by remember(existingApplication) {
        mutableStateOf(existingApplication?.salaryMax?.takeIf { it > 0L }?.toString() ?: "")
    }
    var appliedDateMillis by remember(existingApplication) {
        mutableLongStateOf(
            existingApplication?.appliedDateMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
    }
    var targetResponseMillis by remember(existingApplication) {
        mutableLongStateOf(
            existingApplication?.targetResponseMillis?.takeIf { it > 0L }
                ?: (System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L)
        )
    }
    var notes by remember(existingApplication) { mutableStateOf(existingApplication?.notes ?: "") }
    var hrContact by remember(existingApplication) { mutableStateOf(existingApplication?.hrContact ?: "") }
    val attachments = remember(existingApplication) {
        mutableStateListOf<String>().apply {
            addAll(existingApplication?.attachments ?: emptyList())
        }
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && attachments.size < 10) {
            val meta = resolveDocumentDisplayMeta(context, uri)
            if (meta !in attachments) {
                attachments.add(meta)
            }
        }
    }

    val parsedMin = salaryMinText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val parsedMax = salaryMaxText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val idealTarget = when {
        parsedMin > 0L && parsedMax > 0L -> (parsedMin + parsedMax) / 2L
        parsedMax > 0L -> parsedMax
        else -> parsedMin
    }

    fun submit(overrideStatus: ApplicationStatus?) {
        if (roleTitle.trim().isEmpty()) {
            validationError = "Posisi yang dilamar wajib diisi."
            return
        }
        if (companyName.trim().isEmpty()) {
            validationError = "Nama perusahaan wajib diisi."
            return
        }
        validationError = null
        val finalMin = parsedMin
        val finalMax = if (parsedMax > 0L && parsedMax < parsedMin) parsedMin else parsedMax
        onSave(
            existingApplication?.id,
            dossierCode,
            roleTitle,
            companyName,
            location,
            overrideStatus ?: status,
            workMode,
            contractType,
            finalMin,
            finalMax,
            appliedDateMillis,
            targetResponseMillis,
            notes,
            hrContact,
            attachments.toList(),
            existingApplication?.starred ?: false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Back Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .modernColoredShadow(
                        color = Color(0xFF2B2118).copy(alpha = 0.10f),
                        cornerRadius = 22.dp,
                        blurRadius = 12.dp,
                        offsetY = 5.dp
                    )
                    .clip(CircleShape)
                    .background(colors.cardSurface)
                    .border(1.dp, colors.borderSubtle, CircleShape)
                    .testTag("dossier_form_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = colors.textPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (existingApplication == null) "LAMARAN BARU" else "EDIT LAMARAN",
                    style = MonoEyebrowStyle,
                    color = colors.accentAmber
                )
                Text(
                    text = if (existingApplication == null) "Tambah Lamaran" else existingApplication.companyName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Dark Editorial Header Card (Clean non-wrapping top tags, redundant bottom bar removed)
        AtmosphericDarkCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.accentAmber.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "CATAT LAMARAN • 2026",
                        style = MonoEyebrowStyle.copy(fontSize = 10.sp),
                        color = colors.accentAmber,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "FORM LAMARAN",
                            style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                            color = colors.heroOnDarkMuted,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = dossierCode,
                            style = MonoMetricStyle.copy(fontSize = 11.sp),
                            color = colors.heroOnDark.copy(alpha = 0.85f),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = if (existingApplication == null) "Catat Lamaran\nKerja Barumu" else "Edit Detail\nLamaran Kerja",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    ),
                    color = colors.heroOnDark
                )

                Text(
                    text = "Simpan info posisi, status seleksi, estimasi gaji, dan catatan penting dalam satu tempat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.heroOnDarkMuted
                )
            }
        }

        if (!validationError.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.crimsonSoftBg,
                border = BorderStroke(1.dp, colors.crimsonBorder)
            ) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accentCrimson,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // SECTION 01: Posisi & Perusahaan
        DossierFormSectionCard(
            number = "01",
            title = "Posisi & Perusahaan",
            rightTag = "WAJIB DIISI"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "POSISI YANG DILAMAR *",
                        style = MonoEyebrowStyle,
                        color = colors.textMuted
                    )
                    KarirKuCompactTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it.take(120) },
                        placeholder = "Senior UX Researcher",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_role_title_input")
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "NAMA PERUSAHAAN *",
                        style = MonoEyebrowStyle,
                        color = colors.textMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KarirKuCompactTextField(
                            value = companyName,
                            onValueChange = { companyName = it.take(120) },
                            placeholder = "Tokopedia Indonesia",
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Business,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_company_name_input")
                        )
                        CompanyMonogramBadge(
                            companyName = companyName.ifBlank { "KRK" },
                            size = 48.dp
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "LOKASI KERJA",
                        style = MonoEyebrowStyle,
                        color = colors.textMuted
                    )
                    KarirKuCompactTextField(
                        value = location,
                        onValueChange = { location = it.take(100) },
                        placeholder = "Jakarta Selatan / Remote",
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_location_input")
                    )
                }
            }
        }

        // SECTION 02: Status Lamaran
        DossierFormSectionCard(
            number = "02",
            title = "Status Lamaran",
            rightTag = "PILIH TAHAP"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val statuses = ApplicationStatus.entries
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    statuses.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { st ->
                                val isSelected = status == st
                                val dotColor = when (st) {
                                    ApplicationStatus.DISIMPAN -> colors.textMuted
                                    ApplicationStatus.DILAMAR -> colors.accentBlue
                                    ApplicationStatus.ADMINISTRASI -> colors.accentAmber
                                    ApplicationStatus.INTERVIEW -> colors.accentAmber
                                    ApplicationStatus.OFFER -> colors.accentEmerald
                                    ApplicationStatus.DITOLAK -> colors.accentCrimson
                                }
                                val bg = when {
                                    isSelected && st == ApplicationStatus.OFFER -> colors.emeraldSoftBg
                                    isSelected -> colors.heroDarkSurface
                                    else -> colors.recessedSurface
                                }
                                val border = when {
                                    isSelected && st == ApplicationStatus.OFFER -> colors.accentEmerald
                                    isSelected -> colors.heroDarkSurface
                                    else -> colors.borderSubtle
                                }
                                val textCol = when {
                                    isSelected && st == ApplicationStatus.OFFER -> colors.accentEmerald
                                    isSelected -> colors.heroOnDark
                                    else -> colors.textPrimary
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.modernColoredShadow(
                                                    color = dotColor.copy(alpha = 0.28f),
                                                    cornerRadius = 14.dp,
                                                    blurRadius = 12.dp,
                                                    offsetY = 5.dp
                                                )
                                            } else Modifier
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .tactileClickable { status = st }
                                        .testTag("form_status_${st.name}"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = bg,
                                    border = BorderStroke(1.dp, border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Text(
                                            text = st.shortLabel,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = textCol,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))

                Text(
                    text = "MODEL KERJA & KONTRAK",
                    style = MonoEyebrowStyle,
                    color = colors.textMuted
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkMode.entries.forEach { wm ->
                        val selected = workMode == wm
                        SelectableChipPill(
                            label = wm.label,
                            selected = selected,
                            onClick = { workMode = wm }
                        )
                    }
                    ContractType.entries.forEach { ct ->
                        val selected = contractType == ct
                        SelectableChipPill(
                            label = ct.label,
                            selected = selected,
                            onClick = { contractType = ct }
                        )
                    }
                }
            }
        }

        // SECTION 03: Estimasi Gaji & Tanggal
        DossierFormSectionCard(
            number = "03",
            title = "Estimasi Gaji & Tanggal",
            rightTag = "PER BULAN"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("GAJI MINIMAL", style = MonoEyebrowStyle, color = colors.textMuted, maxLines = 1)
                        KarirKuCompactTextField(
                            value = salaryMinText,
                            onValueChange = { salaryMinText = it.filter { ch -> ch.isDigit() }.take(10) },
                            prefixText = "Rp",
                            placeholder = "15000000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_salary_min_input")
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("GAJI MAKSIMAL", style = MonoEyebrowStyle, color = colors.textMuted, maxLines = 1)
                        KarirKuCompactTextField(
                            value = salaryMaxText,
                            onValueChange = { salaryMaxText = it.filter { ch -> ch.isDigit() }.take(10) },
                            prefixText = "Rp",
                            placeholder = "25000000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_salary_max_input")
                        )
                    }
                }

                // Quick IDR Salary Presets for fast input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PILIH CEPAT (PER BULAN)",
                        style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                        color = colors.textMuted
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            "10jt – 15jt" to (10_000_000L to 15_000_000L),
                            "15jt – 22jt" to (15_000_000L to 22_000_000L),
                            "20jt – 30jt" to (20_000_000L to 30_000_000L),
                            "30jt – 45jt" to (30_000_000L to 45_000_000L)
                        )
                        presets.forEach { (label, range) ->
                            val isActive = parsedMin == range.first && parsedMax == range.second
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .tactileClickable {
                                        salaryMinText = range.first.toString()
                                        salaryMaxText = range.second.toString()
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) colors.heroDarkSurface else colors.recessedSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) colors.heroDarkSurface else colors.borderSubtle
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MonoMetricStyle.copy(fontSize = 10.5.sp),
                                    color = if (isActive) colors.heroOnDark else colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Target Ideal Visualizer
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Ideal: ${KarirKuFormatters.formatIdrFull(idealTarget)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (expectedSalaryBaseline > 0L && idealTarget > 0L) {
                            val diffPct = (((idealTarget - expectedSalaryBaseline) * 100L) / expectedSalaryBaseline).toInt()
                            val badgeText = if (diffPct >= 0) "+$diffPct%" else "$diffPct%"
                            Text(
                                text = badgeText,
                                style = MonoMetricStyle.copy(fontSize = 11.sp),
                                color = if (diffPct >= 0) colors.accentEmerald else colors.accentAmber,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Range bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.recessedSurface)
                    ) {
                        val fillFrac = when {
                            idealTarget <= 0L -> 0.15f
                            parsedMax > parsedMin && parsedMax > 0L -> 0.62f
                            else -> 0.45f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillFrac)
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.heroDarkSurface)
                        )
                    }
                }

                // Dates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateSelectorField(
                        label = "TANGGAL LAMAR",
                        dateMillis = appliedDateMillis,
                        onClick = { activeDatePickerTarget = "applied" },
                        modifier = Modifier.weight(1f)
                    )
                    DateSelectorField(
                        label = "TARGET KABAR",
                        dateMillis = targetResponseMillis,
                        onClick = { activeDatePickerTarget = "target" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // SECTION 04: Detail & Berkas
        DossierFormSectionCard(
            number = "04",
            title = "Catatan & Berkas",
            rightTag = "${attachments.size} DOKUMEN"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATATAN PENTING", style = MonoEyebrowStyle, color = colors.textMuted)
                    KarirKuCompactTextField(
                        value = notes,
                        onValueChange = { notes = it.take(3000) },
                        placeholder = "Tanggung jawab utama, poin riset, atau catatan wawancara...",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 7,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_notes_input")
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("KONTAK HRD / RECRUITER", style = MonoEyebrowStyle, color = colors.textMuted)
                    KarirKuCompactTextField(
                        value = hrContact,
                        onValueChange = { hrContact = it.take(200) },
                        placeholder = "Nama HRD, email, atau LinkedIn",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_hr_contact_input")
                    )
                }

                // Attached files list
                Text("BERKAS TERLAMPIR", style = MonoEyebrowStyle, color = colors.textMuted)

                attachments.forEachIndexed { idx, entry ->
                    val parts = entry.split("|")
                    val title = parts.getOrElse(0) { entry }
                    val subtitle = parts.getOrElse(1) { "Berkas Lampiran" }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = colors.recessedSurface,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = { attachments.removeAt(idx) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Hapus berkas",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Quick attach from Profile Dossier
                if (savedProfileDocuments.isNotEmpty() && attachments.size < 10) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        savedProfileDocuments.take(4).forEach { doc ->
                            val encoded = "${doc.title}|${doc.subtitle.ifBlank { doc.categoryEnum.label }}"
                            val alreadyAttached = encoded in attachments
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (!alreadyAttached && attachments.size < 10) {
                                            attachments.add(encoded)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (alreadyAttached) colors.emeraldSoftBg else colors.cardSurface,
                                border = BorderStroke(1.dp, if (alreadyAttached) colors.emeraldBorder else colors.borderSubtle)
                            ) {
                                Text(
                                    text = if (alreadyAttached) "✓ ${doc.title}" else "+ ${doc.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (alreadyAttached) colors.accentEmerald else colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Upload picker box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            documentPickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "image/*"
                                )
                            )
                        }
                        .testTag("form_upload_attachment_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.recessedSurface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Ketuk untuk Lampirkan Berkas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Format PDF, DOCX, atau Gambar (maks. 10 berkas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons
        TactileRaisedButton(
            onClick = { submit(overrideStatus = null) },
            enabled = !isSaving,
            isLoading = isSaving,
            containerColor = colors.heroDarkSurface,
            contentColor = colors.heroOnDark,
            lipColor = Color(0xFF06070A),
            glowColor = colors.heroDarkSurface.copy(alpha = 0.35f),
            cornerRadius = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_save_application_button")
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.cta_save_application),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        TactileRaisedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_save_draft_button"),
            containerColor = colors.cardSurface,
            borderColor = colors.borderSubtle,
            cornerRadius = 16.dp,
            lipDepth = 2.dp,
            onClick = if (!isSaving) ({ submit(overrideStatus = ApplicationStatus.DISIMPAN) }) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cta_save_draft),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (activeDatePickerTarget != null) {
        val initial = if (activeDatePickerTarget == "applied") appliedDateMillis else targetResponseMillis
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { activeDatePickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            if (activeDatePickerTarget == "applied") appliedDateMillis = picked
                            else targetResponseMillis = picked
                        }
                        activeDatePickerTarget = null
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePickerTarget = null }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DossierFormSectionCard(
    number: String,
    title: String,
    rightTag: String,
    content: @Composable () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    TactileRaisedCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.cardSurface,
        borderColor = colors.borderSubtle,
        cornerRadius = 22.dp,
        lipDepth = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = colors.amberSoftBg.copy(alpha = if (colors.isDark) 0.4f else 0.7f),
                    border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = number,
                        style = MonoMetricStyle.copy(fontSize = 11.5.sp),
                        color = colors.accentAmber,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = rightTag,
                        style = MonoEyebrowStyle.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.7f))
            content()
        }
    }
}

@Composable
private fun SelectableChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    Surface(
        modifier = Modifier
            .then(
                if (selected) {
                    Modifier.modernColoredShadow(
                        color = colors.heroDarkSurface.copy(alpha = 0.22f),
                        cornerRadius = 12.dp,
                        blurRadius = 10.dp,
                        offsetY = 4.dp
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .tactileClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.heroDarkSurface else colors.recessedSurface,
        border = BorderStroke(1.dp, if (selected) colors.heroDarkSurface else colors.borderSubtle)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = if (selected) colors.heroOnDark else colors.textSecondary,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun DateSelectorField(
    label: String,
    dateMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MonoEyebrowStyle, color = colors.textMuted, maxLines = 1)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            color = colors.recessedSurface,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = KarirKuFormatters.formatDateShort(dateMillis),
                    style = MonoMetricStyle.copy(fontSize = 12.sp),
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Pilih tanggal",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun resolveDocumentDisplayMeta(context: Context, uri: Uri): String {
    var name = "Dokumen_Lampiran.pdf"
    var sizeBytes = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
            }
        }
    } catch (_: Exception) {
        // Fallback to default label
    }
    val sizeLabel = when {
        sizeBytes >= 1_048_576L -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes / 1_048_576.0)
        sizeBytes >= 1024L -> "${sizeBytes / 1024L} KB"
        else -> "Berkas Terlampir"
    }
    return "${name.take(90)}|$sizeLabel • Siap Dikirim"
}
