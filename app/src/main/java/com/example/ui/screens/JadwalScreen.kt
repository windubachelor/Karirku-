package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InterviewSchedule
import com.example.data.JobApplication
import com.example.data.KarirKuFormatters
import com.example.data.MeetingFormat
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.InAppWebBrowserDialog
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.modernColoredShadow
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

private data class WeekDaySlot(
    val dateKey: String,
    val dayShort: String,
    val dayNumber: String,
    val isToday: Boolean,
    val millis: Long
)

private fun toDateKey(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
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
    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<InterviewSchedule?>(null) }
    var inAppBrowserPreview by remember { mutableStateOf<Pair<String, String>?>(null) }

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

    val schedulesByDateKey = remember(state.schedules) {
        state.schedules.groupBy { toDateKey(it.scheduledAtMillis) }
    }

    val displayedSchedules = remember(state.schedules, activeFilter, selectedDateKey) {
        val base = when (activeFilter) {
            ScheduleTabFilter.UPCOMING -> state.schedules.filter { !it.completed }.sortedBy { it.scheduledAtMillis }
            ScheduleTabFilter.COMPLETED -> state.schedules.filter { it.completed }.sortedByDescending { it.scheduledAtMillis }
            ScheduleTabFilter.ALL -> state.schedules.sortedBy { it.scheduledAtMillis }
        }
        if (selectedDateKey == null) base else base.filter { toDateKey(it.scheduledAtMillis) == selectedDateKey }
    }

    fun addToGoogleCalendar(schedule: InterviewSchedule) {
        val endMillis = schedule.scheduledAtMillis + (schedule.durationMinutes.coerceAtLeast(15L) * 60_000L)
        val descParts = buildList {
            add("Posisi: ${schedule.roleTitle}")
            add("Perusahaan: ${schedule.companyName}")
            if (schedule.interviewerName.isNotBlank()) add("Pewawancara: ${schedule.interviewerName}")
            if (schedule.prepNotes.isNotBlank()) add("\nCatatan Persiapan:\n${schedule.prepNotes}")
        }
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schedule.scheduledAtMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, "${schedule.stageTitle} — ${schedule.companyName}")
            putExtra(CalendarContract.Events.DESCRIPTION, descParts.joinToString("\n"))
            if (schedule.meetingLocation.isNotBlank()) {
                putExtra(CalendarContract.Events.EVENT_LOCATION, schedule.meetingLocation)
            }
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
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
                            text = "${upcomingList.size} MENDATANG",
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
                            text = "Catat setiap sesi HR Screening, User Interview, atau Technical Test supaya tidak ada jadwal yang terlewat.",
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

        // Interactive 7-Day Week Strip Calendar
        item(key = "jadwal_week_strip") {
            WeekStripCalendarCard(
                weekOffset = weekOffset,
                selectedDateKey = selectedDateKey,
                schedulesByDateKey = schedulesByDateKey,
                onPrevWeek = { weekOffset -= 1 },
                onNextWeek = { weekOffset += 1 },
                onResetToday = {
                    weekOffset = 0
                    selectedDateKey = null
                },
                onSelectDate = { key ->
                    selectedDateKey = if (selectedDateKey == key) null else key
                }
            )
        }

        // Filter Pills
        item(key = "jadwal_filters") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                if (selectedDateKey != null) {
                    ExpressiveEmptyStateCard(
                        eyebrow = "TANGGAL KOSONG",
                        title = "Tidak Ada Jadwal di Hari Ini",
                        description = "Belum ada agenda interview pada tanggal yang dipilih. Kamu bisa tambah jadwal baru atau tampilkan semua jadwal.",
                        ctaLabel = "Tampilkan Semua Jadwal",
                        onCtaClick = { selectedDateKey = null }
                    )
                } else {
                    ExpressiveEmptyStateCard(
                        eyebrow = "BELUM ADA JADWAL",
                        title = if (activeFilter == ScheduleTabFilter.COMPLETED) "Belum Ada Sesi Selesai" else "Belum Ada Jadwal Interview",
                        description = "Tambahkan jadwal wawancara lengkap dengan link meeting, nama pewawancara, dan catatan persiapanmu.",
                        ctaLabel = "+ Buat Jadwal Interview",
                        onCtaClick = {
                            editingSchedule = null
                            showSheet = true
                        }
                    )
                }
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
                    onAddToCalendar = { addToGoogleCalendar(schedule) },
                    onOpenLink = { url ->
                        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
                        } catch (_: Exception) {
                        }
                    },
                    onOpenOnsiteMaps = { loc ->
                        val trimmed = loc.trim()
                        val mapsUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                            trimmed
                        } else {
                            "https://www.google.com/maps/search/?api=1&query=${Uri.encode(trimmed)}"
                        }
                        inAppBrowserPreview = "Lokasi ${schedule.companyName}" to mapsUrl
                    }
                )
            }
        }
    }

    inAppBrowserPreview?.let { (title, url) ->
        InAppWebBrowserDialog(
            title = title,
            url = url,
            onDismiss = { inAppBrowserPreview = null }
        )
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
private fun WeekStripCalendarCard(
    weekOffset: Int,
    selectedDateKey: String?,
    schedulesByDateKey: Map<String, List<InterviewSchedule>>,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetToday: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val todayKey = remember { toDateKey(System.currentTimeMillis()) }

    val weekDays = remember(weekOffset) {
        val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val currentDow = get(Calendar.DAY_OF_WEEK)
            val diffFromMonday = if (currentDow == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDow
            add(Calendar.DAY_OF_MONTH, diffFromMonday + (weekOffset * 7))
        }
        (0..6).map { idx ->
            val millis = cal.timeInMillis
            val key = toDateKey(millis)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
            cal.add(Calendar.DAY_OF_MONTH, 1)
            WeekDaySlot(
                dateKey = key,
                dayShort = dayLabels[idx],
                dayNumber = dayNum,
                isToday = key == todayKey,
                millis = millis
            )
        }
    }

    val monthTitle = remember(weekDays) {
        val midMillis = weekDays.getOrNull(3)?.millis ?: System.currentTimeMillis()
        SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
            .format(Date(midMillis))
            .replaceFirstChar { it.uppercase() }
    }

    val selectedSlot = remember(selectedDateKey, weekDays) {
        weekDays.firstOrNull { it.dateKey == selectedDateKey }
    }

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("jadwal_week_strip"),
        cornerRadius = 22.dp,
        lipDepth = 2.5.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with week arrows & month title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    if (weekOffset != 0 || selectedDateKey != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .tactileClickable(onClick = onResetToday)
                                .testTag("jadwal_week_today_button"),
                            shape = RoundedCornerShape(999.dp),
                            color = colors.amberSoftBg,
                            border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Hari Ini",
                                style = MonoEyebrowStyle.copy(fontSize = 9.sp, letterSpacing = 0.2.sp),
                                color = colors.amberText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.recessedSurface)
                            .border(1.dp, colors.borderSubtle, CircleShape)
                            .tactileClickable(onClick = onPrevWeek)
                            .testTag("jadwal_prev_week_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Pekan sebelumnya",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.recessedSurface)
                            .border(1.dp, colors.borderSubtle, CircleShape)
                            .tactileClickable(onClick = onNextWeek)
                            .testTag("jadwal_next_week_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Pekan berikutnya",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 7-day interactive row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                weekDays.forEach { slot ->
                    val isSelected = selectedDateKey == slot.dateKey
                    val daySchedules = schedulesByDateKey[slot.dateKey].orEmpty()
                    val hasUpcoming = daySchedules.any { !it.completed }
                    val hasAny = daySchedules.isNotEmpty()

                    val tileBg = when {
                        isSelected -> colors.heroDarkSurface
                        slot.isToday -> colors.amberSoftBg
                        else -> colors.recessedSurface.copy(alpha = 0.65f)
                    }
                    val tileBorder = when {
                        isSelected -> colors.heroDarkSurface
                        slot.isToday -> colors.accentAmber.copy(alpha = 0.55f)
                        else -> colors.borderSubtle.copy(alpha = 0.7f)
                    }
                    val dayLabelColor = when {
                        isSelected -> colors.heroOnDarkMuted
                        slot.isToday -> colors.amberText
                        else -> colors.textMuted
                    }
                    val numberColor = when {
                        isSelected -> colors.heroOnDark
                        slot.isToday -> colors.amberText
                        else -> colors.textPrimary
                    }
                    val dotColor = when {
                        hasUpcoming -> colors.accentAmber
                        hasAny -> colors.accentEmerald
                        else -> Color.Transparent
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelected) {
                                    Modifier.modernColoredShadow(
                                        color = colors.heroDarkSurface.copy(alpha = 0.28f),
                                        cornerRadius = 14.dp,
                                        blurRadius = 10.dp,
                                        offsetY = 4.dp
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .tactileClickable { onSelectDate(slot.dateKey) }
                            .testTag("jadwal_day_${slot.dateKey}"),
                        shape = RoundedCornerShape(14.dp),
                        color = tileBg,
                        border = BorderStroke(1.dp, tileBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = slot.dayShort,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = dayLabelColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = slot.dayNumber,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                ),
                                color = numberColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }

            // Active selected day indicator chip
            if (selectedDateKey != null) {
                val displayDate = selectedSlot?.let { KarirKuFormatters.formatDateEditorial(it.millis) } ?: selectedDateKey
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .tactileClickable { onSelectDate(selectedDateKey) },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.recessedSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Menampilkan jadwal $displayDate",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentAmber
                            )
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Reset filter tanggal",
                                tint = colors.accentAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleTimelineCard(
    schedule: InterviewSchedule,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit,
    onOpenLink: (String) -> Unit,
    onOpenOnsiteMaps: (String) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var showMenu by remember { mutableStateOf(false) }

    val dateObj = remember(schedule.scheduledAtMillis) { Date(schedule.scheduledAtMillis) }
    val monthStr = remember(dateObj) { SimpleDateFormat("MMM", Locale("in", "ID")).format(dateObj).uppercase() }
    val dayStr = remember(dateObj) { SimpleDateFormat("dd", Locale("in", "ID")).format(dateObj) }
    val timeStr = remember(schedule.scheduledAtMillis) { KarirKuFormatters.formatTime(schedule.scheduledAtMillis) }
    val isMeetingUrl = remember(schedule.meetingLocation) {
        val trimmed = schedule.meetingLocation.trim()
        trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            (trimmed.contains(".") && !trimmed.contains(" "))
    }
    val isOnsite = schedule.formatEnum == MeetingFormat.ONSITE
    val hasOnsiteLocation = isOnsite && schedule.meetingLocation.isNotBlank()

    val formatIcon = when (schedule.formatEnum) {
        MeetingFormat.ONLINE -> Icons.Outlined.Videocam
        MeetingFormat.ONSITE -> Icons.Outlined.LocationOn
        MeetingFormat.PHONE -> Icons.Outlined.Phone
    }

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

                // Modern Header Action Controls (Check + Minimalist MoreVert Dropdown matching Profil & Info Lowongan)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("schedule_complete_${schedule.id}")
                    ) {
                        Icon(
                            imageVector = if (schedule.completed) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (schedule.completed) "Batal selesai" else "Tandai selesai",
                            tint = if (schedule.completed) colors.accentEmerald else colors.textMuted,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("schedule_more_${schedule.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Opsi jadwal",
                                tint = colors.textMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = colors.cardSurface,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Jadwal", color = colors.textPrimary) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Hapus Jadwal",
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
                            imageVector = formatIcon,
                            contentDescription = null,
                            tint = if (isOnsite) colors.accentAmber else colors.textMuted,
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

            // Modern Direct Action Pills: Google Calendar + Google Maps (Onsite in-app browser) / Link Meeting
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasOnsiteLocation) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { onOpenOnsiteMaps(schedule.meetingLocation) }
                            .testTag("schedule_open_maps_${schedule.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.emeraldSoftBg,
                        border = BorderStroke(1.dp, colors.emeraldBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            GoogleMapsBrandIcon(size = 16.dp)
                            Text(
                                text = "Buka Google Maps",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentEmerald,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else if (isMeetingUrl) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { onOpenLink(schedule.meetingLocation.trim()) }
                            .testTag("schedule_open_meeting_${schedule.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.heroDarkSurface,
                        border = BorderStroke(1.dp, colors.heroDarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = colors.accentAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Buka Link Meeting",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.heroOnDark,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .tactileClickable(onClick = onAddToCalendar)
                        .testTag("schedule_add_calendar_${schedule.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        GoogleCalendarBrandIcon(size = 16.dp)
                        Text(
                            text = "Google Calendar",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Multi-color Google Calendar vector icon drawn cleanly on Canvas.
 */
@Composable
private fun GoogleCalendarBrandIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.16f
        val r = w * 0.18f

        // White center tile
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(stroke * 0.6f, stroke * 0.6f),
            size = Size(w - stroke * 1.2f, h - stroke * 1.2f),
            cornerRadius = CornerRadius(r, r)
        )
        // Top Blue border
        drawLine(Color(0xFF4285F4), Offset(r, stroke * 0.5f), Offset(w - r, stroke * 0.5f), strokeWidth = stroke)
        // Left Blue border
        drawLine(Color(0xFF4285F4), Offset(stroke * 0.5f, r), Offset(stroke * 0.5f, h - r), strokeWidth = stroke)
        // Right Yellow border
        drawLine(Color(0xFFFBBC04), Offset(w - stroke * 0.5f, r), Offset(w - stroke * 0.5f, h - r), strokeWidth = stroke)
        // Bottom Green border
        drawLine(Color(0xFF34A853), Offset(r, h - stroke * 0.5f), Offset(w - r, h - stroke * 0.5f), strokeWidth = stroke)
        // Bottom-right folded Red corner accent
        drawCircle(Color(0xFFEA4335), radius = stroke * 0.72f, center = Offset(w - stroke * 0.8f, h - stroke * 0.8f))
        // Center calendar bars representing "31"
        drawLine(Color(0xFF1A73E8), Offset(w * 0.34f, h * 0.42f), Offset(w * 0.52f, h * 0.42f), strokeWidth = stroke * 0.7f)
        drawLine(Color(0xFF1A73E8), Offset(w * 0.34f, h * 0.62f), Offset(w * 0.52f, h * 0.62f), strokeWidth = stroke * 0.7f)
        drawLine(Color(0xFF1A73E8), Offset(w * 0.64f, h * 0.37f), Offset(w * 0.64f, h * 0.67f), strokeWidth = stroke * 0.7f)
    }
}

/**
 * Multi-color Google Maps Pin vector icon drawn cleanly on Canvas.
 */
@Composable
private fun GoogleMapsBrandIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.38f
        val r = w * 0.34f

        // Bottom pin tail (Green)
        val pinPath = Path().apply {
            moveTo(cx - r * 0.82f, cy + r * 0.48f)
            lineTo(cx, h * 0.94f)
            lineTo(cx + r * 0.82f, cy + r * 0.48f)
            close()
        }
        drawPath(pinPath, color = Color(0xFF34A853))

        // Pin head color arcs (Red, Blue, Yellow, Green)
        val arcSize = Size(r * 2f, r * 2f)
        val arcTopLeft = Offset(cx - r, cy - r)
        drawArc(Color(0xFFEA4335), startAngle = 190f, sweepAngle = 115f, useCenter = true, topLeft = arcTopLeft, size = arcSize)
        drawArc(Color(0xFF4285F4), startAngle = 305f, sweepAngle = 95f, useCenter = true, topLeft = arcTopLeft, size = arcSize)
        drawArc(Color(0xFFFBBC04), startAngle = 115f, sweepAngle = 75f, useCenter = true, topLeft = arcTopLeft, size = arcSize)
        drawArc(Color(0xFF34A853), startAngle = 40f, sweepAngle = 75f, useCenter = true, topLeft = arcTopLeft, size = arcSize)

        // Inner white circle cutout
        drawCircle(color = Color.White, radius = r * 0.44f, center = Offset(cx, cy))
        // Subtle blue center highlight ring
        drawCircle(
            color = Color(0xFF4285F4).copy(alpha = 0.25f),
            radius = r * 0.44f,
            center = Offset(cx, cy),
            style = Stroke(width = 0.8.dp.toPx())
        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existingSchedule == null) "JADWAL BARU" else "EDIT JADWAL",
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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PERUSAHAAN *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = companyName,
                    onValueChange = { companyName = it.take(120) },
                    placeholder = "Contoh: GoTo Financial",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_company_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("POSISI YANG DILAMAR *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = roleTitle,
                    onValueChange = { roleTitle = it.take(120) },
                    placeholder = "Contoh: Senior Product Designer",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_role_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TAHAP WAWANCARA *", style = MonoEyebrowStyle, color = colors.textMuted)
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
                KarirKuCompactTextField(
                    value = stageTitle,
                    onValueChange = { stageTitle = it.take(80) },
                    placeholder = "Contoh: Interview User",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_stage_input")
                )
            }

            // Date & 30-minute step Time Selector (matched 48dp compact height)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TANGGAL & JAM", style = MonoEyebrowStyle, color = colors.textMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
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
                            .height(48.dp)
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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("LINK MEETING / ALAMAT KANTOR", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = meetingLocation,
                    onValueChange = { meetingLocation = it.take(300) },
                    placeholder = "https://meet.google.com/... atau lokasi kantor",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_location_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NAMA PEWAWANCARA (HR / USER)", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = interviewerName,
                    onValueChange = { interviewerName = it.take(120) },
                    placeholder = "Contoh: Rina (HRBP)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_interviewer_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CATATAN PERSIAPAN", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = prepNotes,
                    onValueChange = { prepNotes = it.take(2000) },
                    placeholder = "Daftar pertanyaan, bahan presentasi, atau poin penting...",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_notes_input")
                )
            }

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
