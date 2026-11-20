package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerNote
import com.example.data.KarirKuFormatters
import com.example.ui.components.NoteColorPalette
import com.example.ui.components.RichTextLiveVisualTransformation
import com.example.ui.components.applyRichLinePrefix
import com.example.ui.components.applyRichWrapFormat
import com.example.ui.components.buildCleanRichTextAnnotatedString
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily

private val EDITOR_NOTE_TAGS = listOf("Umum", "Riset", "Interview", "Gaji", "Checklist")

/**
 * Full-screen Google Keep-inspired Rich Text Note Composer & Editor.
 * Supports live rich-text styling (Heading, Bold, Italic, Underline, Strikethrough,
 * Interactive Checklists, Bullets, Numbered Lists, Blockquotes), pastel note colors,
 * label tags, pinning, Read/Preview mode, Save, and Delete.
 */
@Composable
fun CatatanEditorScreen(
    existingNote: CareerNote?,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        content: String,
        tag: String,
        colorKey: String,
        pinned: Boolean,
        onSuccess: () -> Unit
    ) -> Unit,
    onDelete: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val colors = KarirKuThemeTokens.colors
    var title by remember(existingNote?.id) { mutableStateOf(existingNote?.title ?: "") }
    var contentValue by remember(existingNote?.id) {
        mutableStateOf(TextFieldValue(existingNote?.content ?: ""))
    }
    var selectedTag by remember(existingNote?.id) {
        mutableStateOf(existingNote?.tag?.ifBlank { "Umum" } ?: "Umum")
    }
    var selectedPalette by remember(existingNote?.id) {
        mutableStateOf(NoteColorPalette.fromKey(existingNote?.colorKey))
    }
    var pinned by remember(existingNote?.id) { mutableStateOf(existingNote?.pinned ?: false) }
    var showPaletteStrip by remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val surfaceBg = selectedPalette.backgroundColor(colors.isDark)
    val surfaceBorder = selectedPalette.borderColor(colors.isDark)

    val richVisualTransformation = remember(colors) {
        RichTextLiveVisualTransformation(
            accentColor = colors.accentAmber,
            emeraldColor = colors.accentEmerald,
            mutedColor = colors.textMuted,
            textColor = colors.textPrimary
        )
    }

    val wordCount = remember(contentValue.text) {
        contentValue.text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    val canSave = (title.isNotBlank() || contentValue.text.isNotBlank()) && !isSaving

    fun performSave() {
        val effectiveTitle = title.trim().ifBlank {
            contentValue.text.lineSequence().firstOrNull { it.isNotBlank() }
                ?.replace(Regex("""^[#>\-\[\]xX\s*•]+"""), "")
                ?.trim()
                ?.take(60)
                ?.ifBlank { "Catatan Tanpa Judul" }
                ?: "Catatan Tanpa Judul"
        }
        onSave(
            existingNote?.id,
            effectiveTitle,
            contentValue.text,
            selectedTag,
            selectedPalette.key,
            pinned,
            onNavigateBack
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceBg)
            .imePadding()
            .navigationBarsPadding()
            .testTag("catatan_editor_screen")
    ) {
        // 1. Top Editorial Action Bar (Back, Pin, Preview, Palette, Delete, Simpan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.cardSurface.copy(alpha = 0.9f))
                    .border(1.dp, surfaceBorder, CircleShape)
                    .tactileClickable(onClick = onNavigateBack)
                    .testTag("note_editor_back"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali ke daftar catatan",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Toggle Preview / Edit Mode
            EditorTopIconButton(
                icon = if (isPreviewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
                contentDescription = if (isPreviewMode) "Mode Edit" else "Pratinjau Rich Text",
                active = isPreviewMode,
                borderColor = surfaceBorder,
                onClick = { isPreviewMode = !isPreviewMode },
                modifier = Modifier.testTag("note_editor_preview_toggle")
            )

            // Pin Toggle
            EditorTopIconButton(
                icon = if (pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (pinned) "Lepas sematan" else "Sematkan catatan",
                active = pinned,
                borderColor = surfaceBorder,
                onClick = { pinned = !pinned },
                modifier = Modifier.testTag("note_editor_pin")
            )

            // Color Palette Toggle
            EditorTopIconButton(
                icon = Icons.Outlined.Palette,
                contentDescription = "Pilih warna catatan",
                active = showPaletteStrip,
                borderColor = surfaceBorder,
                onClick = { showPaletteStrip = !showPaletteStrip },
                modifier = Modifier.testTag("note_editor_palette")
            )

            // Delete Button (when editing existing note)
            if (existingNote != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.crimsonSoftBg.copy(alpha = 0.85f))
                        .border(1.dp, colors.crimsonBorder, CircleShape)
                        .tactileClickable { showDeleteConfirm = true }
                        .testTag("note_editor_delete"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus catatan",
                        tint = colors.accentCrimson,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Prominent Tactile Simpan Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .tactileClickable(enabled = canSave, onClick = ::performSave)
                    .testTag("note_editor_save"),
                shape = RoundedCornerShape(14.dp),
                color = if (canSave) colors.heroDarkSurface else colors.recessedSurface,
                border = BorderStroke(
                    1.dp,
                    if (canSave) colors.heroDarkSurface else surfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = colors.accentAmber,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (canSave) colors.accentAmber else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Simpan",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = if (canSave) colors.heroOnDark else colors.textMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Optional Keep-Style Pastel Color Palette Strip
        AnimatedVisibility(
            visible = showPaletteStrip,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NoteColorPalette.entries.forEach { palette ->
                    val isSelected = selectedPalette == palette
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(palette.swatchColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) colors.accentAmber else colors.borderSubtle,
                                shape = CircleShape
                            )
                            .clickable { selectedPalette = palette }
                            .testTag("note_color_${palette.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = palette.label,
                                tint = Color(0xFF1A1B1E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Tag Selector + Metadata Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EDITOR_NOTE_TAGS.forEach { tag ->
                val selected = selectedTag.equals(tag, ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .tactileClickable { selectedTag = tag },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) colors.heroDarkSurface else colors.cardSurface.copy(alpha = 0.75f),
                    border = BorderStroke(
                        1.dp,
                        if (selected) colors.heroDarkSurface else surfaceBorder
                    )
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (selected) colors.heroOnDark else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 3. Scrollable Writing / Reading Canvas
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subtle Timestamp + Word Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingNote != null) {
                        "DIEDIT ${KarirKuFormatters.formatRelativeTime(existingNote.updatedAt ?: existingNote.createdAt).uppercase()}"
                    } else {
                        "CATATAN BARU"
                    },
                    style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                    color = colors.textMuted
                )
                Text(
                    text = "$wordCount kata • ${if (isPreviewMode) "Pratinjau" else "Rich Text"}",
                    style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                    color = colors.accentAmber
                )
            }

            // Borderless Keep-Style Title Input
            BasicTextField(
                value = title,
                onValueChange = { title = it.take(120) },
                enabled = !isPreviewMode,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textPrimary
                ),
                cursorBrush = SolidColor(colors.accentAmber),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "Judul catatan...",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = UnboundedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = colors.textMuted.copy(alpha = 0.55f)
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_editor_title")
            )

            HorizontalDivider(color = surfaceBorder.copy(alpha = 0.65f))

            if (isPreviewMode) {
                // Interactive Clean Rich-Text Reader (tapping checklists toggles [ ] <-> [x] live!)
                InteractiveRichTextReader(
                    rawContent = contentValue.text,
                    onToggleChecklistLine = { lineIdx ->
                        val lines = contentValue.text.split("\n").toMutableList()
                        if (lineIdx in lines.indices) {
                            val line = lines[lineIdx]
                            lines[lineIdx] = when {
                                line.startsWith("[ ] ") -> "[x] " + line.drop(4)
                                line.startsWith("[x] ") || line.startsWith("[X] ") -> "[ ] " + line.drop(4)
                                else -> line
                            }
                            contentValue = contentValue.copy(text = lines.joinToString("\n"))
                        }
                    }
                )
            } else {
                // Live Rich-Text Editor Field
                BasicTextField(
                    value = contentValue,
                    onValueChange = { if (it.text.length <= 10000) contentValue = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.textPrimary,
                        lineHeight = 24.sp,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(colors.accentAmber),
                    visualTransformation = richVisualTransformation,
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 260.dp)
                        ) {
                            if (contentValue.text.isEmpty()) {
                                Text(
                                    text = "Mulai tulis catatan riset perusahaan, poin interview, checklist persiapan, atau negosiasi gaji...\n\nGunakan bilah format di bawah untuk Tebal, Miring, Judul, Checklist, atau Daftar.",
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = colors.textMuted.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_editor_body")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Modern Floating Rich Text Formatting Toolbar (Docked above IME Keyboard)
        if (!isPreviewMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .modernColoredShadow(
                            color = if (colors.isDark) Color.Black.copy(alpha = 0.5f) else Color(0xFF2B2118).copy(alpha = 0.12f),
                            cornerRadius = 18.dp,
                            blurRadius = 16.dp,
                            offsetY = 6.dp
                        ),
                    shape = RoundedCornerShape(18.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, surfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatToolbarButton(
                            icon = Icons.Rounded.Title,
                            contentDescription = "Judul (Heading)",
                            onClick = { contentValue = applyRichLinePrefix(contentValue, "# ") },
                            testTag = "fmt_heading"
                        )
                        FormatToolbarButton(
                            icon = Icons.Rounded.FormatBold,
                            contentDescription = "Tebal (Bold)",
                            onClick = { contentValue = applyRichWrapFormat(contentValue, "**", "teks tebal") },
                            testTag = "fmt_bold"
                        )
                        FormatToolbarButton(
                            icon = Icons.Rounded.FormatItalic,
                            contentDescription = "Miring (Italic)",
                            onClick = { contentValue = applyRichWrapFormat(contentValue, "*", "teks miring") },
                            testTag = "fmt_italic"
                        )
                        FormatToolbarButton(
                            icon = Icons.Rounded.FormatUnderlined,
                            contentDescription = "Garis bawah (Underline)",
                            onClick = { contentValue = applyRichWrapFormat(contentValue, "__", "garis bawah") },
                            testTag = "fmt_underline"
                        )
                        FormatToolbarButton(
                            icon = Icons.Rounded.FormatStrikethrough,
                            contentDescription = "Coret (Strikethrough)",
                            onClick = { contentValue = applyRichWrapFormat(contentValue, "~~", "teks coret") },
                            testTag = "fmt_strike"
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(colors.borderSubtle)
                        )
                        FormatToolbarButton(
                            icon = Icons.Outlined.CheckBox,
                            contentDescription = "Checklist tugas",
                            onClick = { contentValue = applyRichLinePrefix(contentValue, "[ ] ") },
                            testTag = "fmt_checklist"
                        )
                        FormatToolbarButton(
                            icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                            contentDescription = "Daftar poin",
                            onClick = { contentValue = applyRichLinePrefix(contentValue, "- ") },
                            testTag = "fmt_bullet"
                        )
                        FormatToolbarButton(
                            icon = Icons.Outlined.FormatListNumbered,
                            contentDescription = "Daftar bernomor",
                            onClick = { contentValue = applyRichLinePrefix(contentValue, "1. ") },
                            testTag = "fmt_numbered"
                        )
                        FormatToolbarButton(
                            icon = Icons.Outlined.FormatQuote,
                            contentDescription = "Kutipan",
                            onClick = { contentValue = applyRichLinePrefix(contentValue, "> ") },
                            testTag = "fmt_quote"
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && existingNote != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Catatan Ini?") },
            text = { Text("Catatan \"${existingNote.title}\" akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(existingNote.id, onNavigateBack)
                    }
                ) {
                    Text("Hapus", color = colors.accentCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun InteractiveRichTextReader(
    rawContent: String,
    onToggleChecklistLine: (Int) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    if (rawContent.isBlank()) {
        Text(
            text = "Catatan masih kosong.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted
        )
        return
    }

    val lines = remember(rawContent) { rawContent.split("\n") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEachIndexed { idx, line ->
            val isUnchecked = line.startsWith("[ ] ")
            val isChecked = line.startsWith("[x] ") || line.startsWith("[X] ")

            if (isUnchecked || isChecked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleChecklistLine(idx) }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChecked) colors.accentEmerald else Color.Transparent)
                            .border(
                                width = 1.6.dp,
                                color = if (isChecked) colors.accentEmerald else colors.accentAmber,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = buildCleanRichTextAnnotatedString(
                            raw = line.drop(4),
                            accentColor = colors.accentAmber,
                            emeraldColor = colors.accentEmerald,
                            mutedColor = colors.textMuted,
                            textColor = colors.textPrimary
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isChecked) colors.textMuted else colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = buildCleanRichTextAnnotatedString(
                        raw = line,
                        accentColor = colors.accentAmber,
                        emeraldColor = colors.accentEmerald,
                        mutedColor = colors.textMuted,
                        textColor = colors.textPrimary
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 23.sp),
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun EditorTopIconButton(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (active) colors.amberSoftBg else colors.cardSurface.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = if (active) colors.accentAmber.copy(alpha = 0.55f) else borderColor,
                shape = CircleShape
            )
            .tactileClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) colors.accentAmber else colors.textPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun FormatToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .tactileClickable(pressedScale = 0.90f, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
