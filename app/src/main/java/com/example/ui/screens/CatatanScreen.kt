package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerNote
import com.example.data.KarirKuFormatters
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.UiState

private val DEFAULT_NOTE_TAGS = listOf("Umum", "Riset", "Interview", "Gaji", "Checklist")

@Composable
fun CatatanScreen(
    notesState: UiState<List<CareerNote>>,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSaveNote: (String?, String, String, String, Boolean, () -> Unit) -> Unit,
    onTogglePin: (CareerNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val colors = KarirKuThemeTokens.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var showEditorSheet by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<CareerNote?>(null) }
    var pendingDeleteNote by remember { mutableStateOf<CareerNote?>(null) }

    val notes = (notesState as? UiState.Success)?.data.orEmpty()
    val filteredNotes = remember(notes, searchQuery, selectedTag) {
        val q = searchQuery.trim().lowercase()
        notes.filter { note ->
            val matchesTag = selectedTag == null || note.tag.equals(selectedTag, ignoreCase = true)
            val matchesQuery = q.isBlank() ||
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q) ||
                note.tag.lowercase().contains(q)
            matchesTag && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("catatan_screen")
    ) {
        // Top Header
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
                    .testTag("catatan_back_button"),
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
                    text = "CATATAN PRIBADI",
                    style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                    color = colors.accentAmber,
                    maxLines = 1
                )
                Text(
                    text = "Catatan Karier",
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
                    .tactileClickable {
                        editingNote = null
                        showEditorSheet = true
                    }
                    .testTag("catatan_add_button"),
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
                        text = "Catat",
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
                .testTag("catatan_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "notes_search") {
                KarirKuCompactTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Cari catatan interview, riset, atau gaji...",
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
                    } else null
                )
            }

            item(key = "notes_tags") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        NoteTagFilterChip(
                            label = "Semua (${notes.size})",
                            selected = selectedTag == null,
                            onClick = { selectedTag = null }
                        )
                    }
                    items(DEFAULT_NOTE_TAGS) { tag ->
                        NoteTagFilterChip(
                            label = tag,
                            selected = selectedTag == tag,
                            onClick = { selectedTag = if (selectedTag == tag) null else tag }
                        )
                    }
                }
            }

            when (notesState) {
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
                                text = notesState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accentCrimson,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    if (filteredNotes.isEmpty()) {
                        item(key = "empty_notes") {
                            ExpressiveEmptyStateCard(
                                eyebrow = "RUANG CATATAN",
                                title = if (notes.isEmpty()) "Belum Ada Catatan Karier" else "Catatan Tidak Ditemukan",
                                description = if (notes.isEmpty())
                                    "Simpan poin penting seperti daftar pertanyaan ke HR, catatan riset perusahaan, atau target negosiasi gajimu di sini."
                                else
                                    "Coba kata kunci atau kategori lain untuk melihat catatanmu.",
                                ctaLabel = if (notes.isEmpty()) "+ Buat Catatan Pertama" else "Reset Filter",
                                onCtaClick = {
                                    if (notes.isEmpty()) {
                                        editingNote = null
                                        showEditorSheet = true
                                    } else {
                                        searchQuery = ""
                                        selectedTag = null
                                    }
                                }
                            )
                        }
                    } else {
                        items(filteredNotes, key = { it.id }) { note ->
                            CareerNoteCard(
                                note = note,
                                onClick = {
                                    editingNote = note
                                    showEditorSheet = true
                                },
                                onTogglePin = { onTogglePin(note) },
                                onDelete = { pendingDeleteNote = note }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditorSheet) {
        NoteEditorBottomSheet(
            existingNote = editingNote,
            isSaving = isSaving,
            onDismiss = {
                showEditorSheet = false
                editingNote = null
            },
            onSave = { id, title, content, tag, pinned ->
                onSaveNote(id, title, content, tag, pinned) {
                    showEditorSheet = false
                    editingNote = null
                }
            }
        )
    }

    pendingDeleteNote?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingDeleteNote = null },
            title = { Text("Hapus Catatan Ini?") },
            text = { Text("Catatan \"${note.title}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = note.id
                        pendingDeleteNote = null
                        onDeleteNote(id)
                    }
                ) {
                    Text("Hapus", color = colors.accentCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteNote = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun CareerNoteCard(
    note: CareerNote,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val (tagBg, tagFg) = when (note.tag) {
        "Interview" -> colors.amberSoftBg to colors.accentAmber
        "Riset" -> colors.blueSoftBg to colors.accentBlue
        "Gaji" -> colors.emeraldSoftBg to colors.accentEmerald
        "Checklist" -> colors.crimsonSoftBg to colors.accentCrimson
        else -> colors.recessedSurface to colors.textSecondary
    }

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("catatan_card_${note.id}"),
        containerColor = colors.cardSurface,
        borderColor = if (note.pinned) colors.accentAmber.copy(alpha = 0.45f) else colors.borderSubtle,
        cornerRadius = 20.dp,
        lipDepth = 2.5.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = tagBg
                    ) {
                        Text(
                            text = note.tag.uppercase(),
                            style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                            color = tagFg,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = KarirKuFormatters.formatRelativeTime(note.updatedAt ?: note.createdAt),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (note.pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.pinned) "Lepas sematan" else "Sematkan catatan",
                            tint = if (note.pinned) colors.accentAmber else colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Hapus catatan",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = colors.textSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NoteTagFilterChip(
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
            1.dp,
            if (selected) colors.heroDarkSurface else colors.borderSubtle
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = if (selected) colors.heroOnDark else colors.textSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorBottomSheet(
    existingNote: CareerNote?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, String, Boolean) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }
    var tag by remember(existingNote) { mutableStateOf(existingNote?.tag?.ifBlank { "Umum" } ?: "Umum") }
    var pinned by remember(existingNote) { mutableStateOf(existingNote?.pinned ?: false) }

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
                    text = if (existingNote == null) "CATATAN BARU" else "EDIT CATATAN",
                    style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                    color = colors.accentAmber
                )
                Text(
                    text = if (existingNote == null) "Tulis Catatan Karier" else "Perbarui Catatan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.textPrimary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("JUDUL CATATAN *", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Contoh: Daftar Pertanyaan untuk User Interview",
                    modifier = Modifier.testTag("note_title_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("LABEL", style = MonoEyebrowStyle, color = colors.textMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_NOTE_TAGS.forEach { option ->
                        val selected = tag == option
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .tactileClickable { tag = option },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.heroDarkSurface else colors.recessedSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.heroDarkSurface else colors.borderSubtle
                            )
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) colors.heroOnDark else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("ISI CATATAN", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = "Tulis poin riset perusahaan, kisaran gaji, atau rangkuman persiapanmu...",
                    singleLine = false,
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.testTag("note_content_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sematkan di Atas",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tampilkan catatan penting ini paling atas",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = pinned,
                    onCheckedChange = { pinned = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = colors.accentAmber)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TactileRaisedButton(
                onClick = { onSave(existingNote?.id, title, content, tag, pinned) },
                enabled = title.isNotBlank() && !isSaving,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF050608),
                glowColor = colors.accentAmber.copy(alpha = 0.28f),
                modifier = Modifier.testTag("note_save_button")
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
                    text = if (isSaving) "Menyimpan..." else "Simpan Catatan",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
