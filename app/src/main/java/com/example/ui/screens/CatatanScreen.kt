package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerNote
import com.example.data.KarirKuFormatters
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.NoteColorPalette
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.buildCleanRichTextAnnotatedString
import com.example.ui.components.extractPlainNoteText
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.UiState

private val KEEP_NOTE_TAGS = listOf("Umum", "Riset", "Interview", "Gaji", "Checklist")

/**
 * Google Keep-inspired Career Notes workspace featuring a floating pill search header,
 * 2-column staggered masonry / 1-column list toggle, pastel note cards with rich-text preview,
 * Pinned vs Others sections, and a tactile compose FAB opening the full-screen Rich Text Editor.
 */
@Composable
fun CatatanScreen(
    notesState: UiState<List<CareerNote>>,
    onNavigateBack: () -> Unit,
    onCreateNewNote: () -> Unit,
    onOpenNote: (CareerNote) -> Unit,
    onTogglePin: (CareerNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val colors = KarirKuThemeTokens.colors
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var isGridMode by rememberSaveable { mutableStateOf(true) }
    var pendingDeleteNote by remember { mutableStateOf<CareerNote?>(null) }

    val notes = (notesState as? UiState.Success)?.data.orEmpty()
    val filteredNotes = remember(notes, searchQuery, selectedTag) {
        val q = searchQuery.trim().lowercase()
        notes.filter { note ->
            val matchesTag = selectedTag == null || note.tag.equals(selectedTag, ignoreCase = true)
            val matchesQuery = q.isBlank() ||
                note.title.lowercase().contains(q) ||
                extractPlainNoteText(note.content).lowercase().contains(q) ||
                note.tag.lowercase().contains(q)
            matchesTag && matchesQuery
        }
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.pinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filter { !it.pinned } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("catatan_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Google Keep-Style Floating Pill Search Bar with Back & Grid/List Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .modernColoredShadow(
                            color = if (colors.isDark) Color.Black.copy(alpha = 0.4f) else Color(0xFF2B2118).copy(alpha = 0.10f),
                            cornerRadius = 26.dp,
                            blurRadius = 16.dp,
                            offsetY = 5.dp
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("catatan_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(colors.accentAmber),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari catatan kariermu...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    inner()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("catatan_search_input")
                        )

                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Grid / List Layout Toggle (Google Keep Signature)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.recessedSurface)
                                .tactileClickable { isGridMode = !isGridMode }
                                .testTag("catatan_layout_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGridMode) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                contentDescription = if (isGridMode) "Tampilan daftar" else "Tampilan grid",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // 2. Tag Filter Chips Strip
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    KeepTagChip(
                        label = "Semua (${notes.size})",
                        selected = selectedTag == null,
                        onClick = { selectedTag = null }
                    )
                }
                items(KEEP_NOTE_TAGS) { tag ->
                    val count = notes.count { it.tag.equals(tag, ignoreCase = true) }
                    KeepTagChip(
                        label = if (count > 0) "$tag ($count)" else tag,
                        selected = selectedTag == tag,
                        onClick = { selectedTag = if (selectedTag == tag) null else tag }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Google Keep Masonry Staggered Grid / List
            when (notesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.accentAmber)
                    }
                }

                is UiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
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

                is UiState.Success -> {
                    if (filteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            ExpressiveEmptyStateCard(
                                eyebrow = "CATATAN ALA KEEP",
                                title = if (notes.isEmpty()) "Mulai Catat Ide & Risetmu" else "Catatan Tidak Ditemukan",
                                description = if (notes.isEmpty())
                                    "Tulis catatan interview, checklist persiapan, riset perusahaan, atau poin negosiasi gaji dengan editor rich text modern."
                                else
                                    "Coba kata kunci atau label lain untuk melihat catatanmu.",
                                ctaLabel = if (notes.isEmpty()) "+ Buat Catatan Baru" else "Reset Filter",
                                onCtaClick = {
                                    if (notes.isEmpty()) {
                                        onCreateNewNote()
                                    } else {
                                        searchQuery = ""
                                        selectedTag = null
                                    }
                                }
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(if (isGridMode) 2 else 1),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("catatan_grid"),
                            contentPadding = PaddingValues(
                                start = 18.dp,
                                end = 18.dp,
                                top = 4.dp,
                                bottom = 104.dp
                            ),
                            verticalItemSpacing = 12.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (pinnedNotes.isNotEmpty()) {
                                item(key = "header_pinned", span = StaggeredGridItemSpan.FullLine) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PushPin,
                                            contentDescription = null,
                                            tint = colors.accentAmber,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "DISEMATKAN",
                                            style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                                            color = colors.accentAmber
                                        )
                                    }
                                }

                                items(pinnedNotes, key = { "pin_${it.id}" }) { note ->
                                    KeepNoteCard(
                                        note = note,
                                        isCompactGrid = isGridMode,
                                        onClick = { onOpenNote(note) },
                                        onTogglePin = { onTogglePin(note) },
                                        onDelete = { pendingDeleteNote = note }
                                    )
                                }
                            }

                            if (otherNotes.isNotEmpty()) {
                                if (pinnedNotes.isNotEmpty()) {
                                    item(key = "header_others", span = StaggeredGridItemSpan.FullLine) {
                                        Text(
                                            text = "LAINNYA",
                                            style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                                            color = colors.textMuted,
                                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                                        )
                                    }
                                }

                                items(otherNotes, key = { it.id }) { note ->
                                    KeepNoteCard(
                                        note = note,
                                        isCompactGrid = isGridMode,
                                        onClick = { onOpenNote(note) },
                                        onTogglePin = { onTogglePin(note) },
                                        onDelete = { pendingDeleteNote = note }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Floating Tactile Google Keep-Style Compose FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            TactileRaisedButton(
                onClick = onCreateNewNote,
                fillMaxWidth = false,
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF040507),
                glowColor = colors.accentAmber.copy(alpha = 0.35f),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(start = 12.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                modifier = Modifier.testTag("catatan_fab_compose")
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
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
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "Catatan Baru",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = UnboundedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    ),
                    color = colors.heroOnDark,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
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
private fun KeepNoteCard(
    note: CareerNote,
    isCompactGrid: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    var showMenu by remember { mutableStateOf(false) }

    val palette = remember(note.colorKey) { NoteColorPalette.fromKey(note.colorKey) }
    val cardBg = palette.backgroundColor(colors.isDark)
    val cardBorder = if (note.pinned) colors.accentAmber.copy(alpha = 0.55f) else palette.borderColor(colors.isDark)

    val richPreview = remember(note.content, colors) {
        buildCleanRichTextAnnotatedString(
            raw = note.content,
            accentColor = colors.accentAmber,
            emeraldColor = colors.accentEmerald,
            mutedColor = colors.textMuted,
            textColor = colors.textPrimary
        )
    }

    TactileRaisedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("catatan_card_${note.id}"),
        containerColor = cardBg,
        borderColor = cardBorder,
        lipColor = cardBorder,
        cornerRadius = 18.dp,
        lipDepth = 2.5.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 13.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 19.sp
                    ),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp, end = 4.dp)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Opsi catatan",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = colors.cardSurface,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Buka & Edit", color = colors.textPrimary) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(17.dp))
                            },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (note.pinned) "Lepas Sematan" else "Sematkan",
                                    color = colors.textPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (note.pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    tint = colors.accentAmber,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onTogglePin()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Hapus Catatan",
                                    color = colors.accentCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.accentCrimson, modifier = Modifier.size(17.dp))
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Text(
                    text = richPreview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp,
                        fontSize = 12.5.sp
                    ),
                    color = colors.textSecondary,
                    maxLines = if (isCompactGrid) 7 else 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom tag + timestamp row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = colors.cardSurface.copy(alpha = if (colors.isDark) 0.55f else 0.85f),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Text(
                        text = note.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textSecondary,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = KarirKuFormatters.formatRelativeTime(note.updatedAt ?: note.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun KeepTagChip(
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
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
        )
    }
}
