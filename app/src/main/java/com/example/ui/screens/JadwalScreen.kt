package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InterviewSchedule
import com.example.data.JobApplication
import com.example.data.KarirKuFormatters
import com.example.data.MeetingFormat
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.KarirKuDashboardState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ScheduleTabFilter(val label: String) {
    UPCOMING("Akan Datang"),
    COMPLETED("Selesai"),
    ALL("Semua")
}

@Composable
fun JadwalScreen(
    state: KarirKuDashboardState,
    preselectedApplicationForNewSchedule: JobApplication?,
    onClearPreselectedApplication: () -> Unit,
    isSaving: Boolean,
    onSaveSchedule: (
        existingId: String?,
        applicationId: String,
        companyName: String,
        roleTitle: String,
        stageTitle: String,
        scheduledAtMillis: Long,
        durationMinutes: Long,
        meetingFormat: MeetingFormat,
        meetingLocation: String,
        interviewerName: String,
        prepNotes: String,
        completed: Boolean,
        onSuccess: () -> Unit
    ) -> Unit,
    onToggleCompleted: (InterviewSchedule) -> Unit,
    onDeleteSchedule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current

    var activeFilter by remember { mutableStateOf(ScheduleTabFilter.UPCOMING) }
    var showSheet by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<InterviewSchedule?>(null) }

    // Safe side-effect trigger when jumping from a job detail sheet
    LaunchedEffect(preselectedApplicationForNewSchedule) {
        if (preselectedApplicationForNewSchedule != null) {
            editingSchedule = null
            showSheet = true
        }
    }

    val upcomingList = remember(state.schedules) {
        state.schedules.filter { !it.completed }.sortedBy { it.scheduledAtMillis }
    }
    val nextInterview = upcomingList.firstOrNull()

    val displayedSchedules = remember(state.schedules, activeFilter) {
        when (activeFilter) {
            ScheduleTabFilter.UPCOMING -> state.schedules.filter { !it.completed }.sortedBy { it.scheduledAtMillis }
            ScheduleTabFilter.COMPLETED -> state.schedules.filter { it.completed }.sortedByDescending { it.scheduledAtMillis }
            ScheduleTabFilter.ALL -> state.schedules.sortedBy { it.scheduledAtMillis }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("jadwal_lazy_column"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dark Atmospheric Agenda Card
        item(key = "jadwal_hero") {
            AtmosphericDarkCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.accentAmber.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "AGENDA INTERVIEW",
                                style = MonoEyebrowStyle,
                                color = colors.accentAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = "${upcomingList.size} AKTIF",
                            style = MonoMetricStyle,
                            color = colors.heroOnDarkMuted
                        )
                    }

                    if (nextInterview != null) {
                        Text(
                            text = nextInterview.stageTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = UnboundedFontFamily
                            ),
                            color = colors.heroOnDark
                        )
                        Text(
                            text = "${nextInterview.companyName} • ${nextInterview.roleTitle}",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.heroOnDarkMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${KarirKuFormatters.formatDateEditorial(nextInterview.scheduledAtMillis)} • ${KarirKuFormatters.formatTime(nextInterview.scheduledAtMillis)} WIB",
                                style = MonoMetricStyle.copy(fontSize = 11.sp),
                                color = colors.heroOnDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Jadwal Wawancara\n& Follow-up",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = UnboundedFontFamily
                            ),
                            color = colors.heroOnDark
                        )
                        Text(
                            text = "Catat setiap sesi HR Screening, User Interview, atau Technical Test agar tidak ada momentum terlewat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.heroOnDarkMuted
                        )
                    }

                    TactileRaisedButton(
                        onClick = {
                            editingSchedule = null
                            showSheet = true
                        },
                        containerColor = colors.accentAmber,
                        contentColor = Color.White,
                        lipColor = Color(0xFFA84619),
                        glowColor = colors.accentAmber.copy(alpha = 0.4f),
                        cornerRadius = 15.dp,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("jadwal_add_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tambah Jadwal Baru", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Filter Pills
        item(key = "jadwal_filters") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleTabFilter.entries.forEach { tab ->
                    val selected = activeFilter == tab
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { activeFilter = tab },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) colors.heroDarkSurface else colors.cardSurface,
                        border = BorderStroke(1.dp, if (selected) colors.heroDarkSurface else colors.borderSubtle)
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (selected) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        // Schedule List or Empty State
        if (displayedSchedules.isEmpty()) {
            item(key = "jadwal_empty") {
                ExpressiveEmptyStateCard(
                    eyebrow = "TIMELINE KOSONG",
                    title = if (activeFilter == ScheduleTabFilter.COMPLETED) "Belum Ada Sesi Selesai" else "Belum Ada Jadwal Terdaftar",
                    description = "Tambahkan agenda wawancara beserta link meeting, nama pewawancara, dan catatan persiapanmu.",
                    ctaLabel = "+ Buat Jadwal Interview",
                    onCtaClick = {
                        editingSchedule = null
                        showSheet = true
                    }
                )
            }
        } else {
            items(displayedSchedules, key = { it.id }) { schedule ->
                ScheduleTimelineCard(
                    schedule = schedule,
                    onToggleComplete = { onToggleCompleted(schedule) },
                    onEdit = {
                        editingSchedule = schedule
                        showSheet = true
                    },
                    onDelete = { onDeleteSchedule(schedule.id) },
                    onOpenLink = { url ->
                        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
                        } catch (_: Exception) {
                        }
                    }
                )
            }
        }
    }

    if (showSheet) {
        ScheduleEditorBottomSheet(
            existingSchedule = editingSchedule,
            preselectedApplication = preselectedApplicationForNewSchedule,
            availableApplications = state.applications,
            isSaving = isSaving,
            onDismiss = {
                showSheet = false
                editingSchedule = null
                onClearPreselectedApplication()
            },
            onSave = { appId, company, role, stage, timeMs, durMin, format, loc, interviewer, prep, completed ->
                onSaveSchedule(
                    editingSchedule?.id,
                    appId,
                    company,
                    role,
                    stage,
                    timeMs,
                    durMin,
                    format,
                    loc,
                    interviewer,
                    prep,
                    completed
                ) {
                    showSheet = false
                    editingSchedule = null
                    onClearPreselectedApplication()
                }
            }
        )
    }
}

@Composable
private fun ScheduleTimelineCard(
    schedule: InterviewSchedule,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val dateObj = remember(schedule.scheduledAtMillis) { Date(schedule.scheduledAtMillis) }
    val monthStr = remember(dateObj) { SimpleDateFormat("MMM", Locale("in", "ID")).format(dateObj).uppercase() }
    val dayStr = remember(dateObj) { SimpleDateFormat("dd", Locale("in", "ID")).format(dateObj) }
    val timeStr = remember(schedule.scheduledAtMillis) { KarirKuFormatters.formatTime(schedule.scheduledAtMillis) }

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}"),
        borderColor = if (schedule.completed) colors.emeraldBorder else colors.borderSubtle,
        lipColor = if (schedule.completed) colors.emeraldBorder else null,
        cornerRadius = 22.dp,
        lipDepth = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (schedule.completed) colors.emeraldSoftBg else colors.heroDarkSurface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = monthStr,
                            style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                            color = if (schedule.completed) colors.accentEmerald else colors.accentAmber
                        )
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = UnboundedFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (schedule.completed) colors.accentEmerald else colors.heroOnDark
                        )
                        Text(
                            text = timeStr,
                            style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                            color = if (schedule.completed) colors.textMuted else colors.heroOnDarkMuted
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (schedule.completed) colors.emeraldSoftBg else colors.amberSoftBg
                    ) {
                        Text(
                            text = if (schedule.completed) "SELESAI" else schedule.stageTitle.uppercase(),
                            style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                            color = if (schedule.completed) colors.accentEmerald else colors.amberText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = schedule.roleTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = if (schedule.completed) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${schedule.companyName} • ${schedule.durationMinutes} mnt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }

                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.testTag("schedule_complete_${schedule.id}")
                ) {
                    Icon(
                        imageVector = if (schedule.completed) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (schedule.completed) "Batal selesai" else "Tandai selesai",
                        tint = if (schedule.completed) colors.accentEmerald else colors.textMuted
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.recessedSurface
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${schedule.formatEnum.label}: ${schedule.meetingLocation.ifBlank { "Belum diisi" }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val isUrl = schedule.meetingLocation.contains(".") && !schedule.meetingLocation.contains(" ")
                        if (isUrl) {
                            IconButton(
                                onClick = { onOpenLink(schedule.meetingLocation) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = "Buka tautan meeting",
                                    tint = colors.accentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (schedule.interviewerName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = schedule.interviewerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (schedule.prepNotes.isNotBlank()) {
                        HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.6f))
                        Text(
                            text = schedule.prepNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.accentCrimson)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ScheduleEditorBottomSheet(
    existingSchedule: InterviewSchedule?,
    preselectedApplication: JobApplication?,
    availableApplications: List<JobApplication>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        applicationId: String,
        companyName: String,
        roleTitle: String,
        stageTitle: String,
        scheduledAtMillis: Long,
        durationMinutes: Long,
        meetingFormat: MeetingFormat,
        meetingLocation: String,
        interviewerName: String,
        prepNotes: String,
        completed: Boolean
    ) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAppId by remember {
        mutableStateOf(existingSchedule?.applicationId ?: preselectedApplication?.id ?: "")
    }
    var companyName by remember {
        mutableStateOf(existingSchedule?.companyName ?: preselectedApplication?.companyName ?: "")
    }
    var roleTitle by remember {
        mutableStateOf(existingSchedule?.roleTitle ?: preselectedApplication?.roleTitle ?: "")
    }
    var stageTitle by remember {
        mutableStateOf(existingSchedule?.stageTitle ?: "Interview HR & User")
    }
    var meetingFormat by remember {
        mutableStateOf(existingSchedule?.formatEnum ?: MeetingFormat.ONLINE)
    }
    var meetingLocation by remember { mutableStateOf(existingSchedule?.meetingLocation ?: "") }
    var interviewerName by remember { mutableStateOf(existingSchedule?.interviewerName ?: "") }
    var prepNotes by remember { mutableStateOf(existingSchedule?.prepNotes ?: "") }
    var durationMinutes by remember { mutableLongStateOf(existingSchedule?.durationMinutes ?: 60L) }

    val initialCal = remember(existingSchedule) {
        Calendar.getInstance().apply {
            if (existingSchedule != null && existingSchedule.scheduledAtMillis > 0L) {
                timeInMillis = existingSchedule.scheduledAtMillis
            } else {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
            }
        }
    }
    var dateMillis by remember { mutableLongStateOf(initialCal.timeInMillis) }
    var selectedHour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (existingSchedule == null) "TAMBAH AGENDA" else "EDIT AGENDA",
                style = MonoEyebrowStyle,
                color = colors.accentAmber
            )
            Text(
                text = "Jadwal Wawancara",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )

            if (availableApplications.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("HUBUNGKAN KE LAMARAN (OPSIONAL)", style = MonoEyebrowStyle, color = colors.textMuted)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableApplications.take(6).forEach { app ->
                            val picked = selectedAppId == app.id
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .tactileClickable {
                                        selectedAppId = app.id
                                        companyName = app.companyName
                                        roleTitle = app.roleTitle
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (picked) colors.heroDarkSurface else colors.recessedSurface,
                                border = BorderStroke(1.dp, if (picked) colors.heroDarkSurface else colors.borderSubtle)
                            ) {
                                Text(
                                    text = "${app.companyName} • ${app.roleTitle}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (picked) colors.heroOnDark else colors.textSecondary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!errorMsg.isNullOrBlank()) {
                Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = colors.accentCrimson)
            }

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it.take(120) },
                label = { Text("Perusahaan *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = roleTitle,
                onValueChange = { roleTitle = it.take(120) },
                label = { Text("Posisi / Peran *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("HR Screening", "Interview User", "Technical Test", "Final Offer").forEach { preset ->
                    val sel = stageTitle.equals(preset, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .tactileClickable { stageTitle = preset },
                        shape = RoundedCornerShape(10.dp),
                        color = if (sel) colors.heroDarkSurface else colors.recessedSurface
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (sel) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = stageTitle,
                onValueChange = { stageTitle = it.take(80) },
                label = { Text("Nama Tahap *") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Date & 30-minute step Time Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .tactileClickable { showDatePicker = true },
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
                            style = MonoMetricStyle,
                            color = colors.textPrimary
                        )
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .tactileClickable {
                            if (selectedMinute == 0) {
                                selectedMinute = 30
                            } else {
                                selectedMinute = 0
                                selectedHour = if (selectedHour >= 19) 8 else selectedHour + 1
                            }
                        },
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
                            text = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')} WIB",
                            style = MonoMetricStyle,
                            color = colors.textPrimary
                        )
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Duration Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30L, 45L, 60L, 90L).forEach { mins ->
                    val sel = durationMinutes == mins
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .tactileClickable { durationMinutes = mins },
                        shape = RoundedCornerShape(10.dp),
                        color = if (sel) colors.heroDarkSurface else colors.recessedSurface
                    ) {
                        Text(
                            text = "$mins mnt",
                            style = MonoMetricStyle.copy(fontSize = 11.sp),
                            color = if (sel) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeetingFormat.entries.forEach { fmt ->
                    val sel = meetingFormat == fmt
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { meetingFormat = fmt },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sel) colors.heroDarkSurface else colors.recessedSurface
                    ) {
                        Text(
                            text = fmt.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (sel) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = meetingLocation,
                onValueChange = { meetingLocation = it.take(300) },
                label = { Text("Link Meeting / Alamat Kantor") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interviewerName,
                onValueChange = { interviewerName = it.take(120) },
                label = { Text("Nama Pewawancara (HR / User)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = prepNotes,
                onValueChange = { prepNotes = it.take(2000) },
                label = { Text("Catatan Persiapan") },
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            TactileRaisedButton(
                onClick = {
                    if (companyName.trim().isEmpty() || roleTitle.trim().isEmpty() || stageTitle.trim().isEmpty()) {
                        errorMsg = "Perusahaan, posisi, dan tahap wawancara wajib diisi."
                        return@TactileRaisedButton
                    }
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = dateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }
                    onSave(
                        selectedAppId,
                        companyName,
                        roleTitle,
                        stageTitle,
                        cal.timeInMillis,
                        durationMinutes,
                        meetingFormat,
                        meetingLocation,
                        interviewerName,
                        prepNotes,
                        existingSchedule?.completed ?: false
                    )
                },
                isLoading = isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("schedule_save_button")
            ) {
                Text("Simpan Jadwal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
