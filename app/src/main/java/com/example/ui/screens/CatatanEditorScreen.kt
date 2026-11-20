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
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerNote
import com.example.data.KarirKuFormatters
import com.example.ui.components.InlineStyle
import com.example.ui.components.ListPrefixKind
import com.example.ui.components.NoteColorPalette
import com.example.ui.components.WysiwygRichTextVisualTransformation
import com.example.ui.components.deserializeRichNote
import com.example.ui.components.serializeRichNote
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily

private val EDITOR_NOTE_TAGS = listOf("Umum", "Riset", "Interview", "Gaji", "Checklist")

/**
 * Full-screen Google Keep-inspired true WYSIWYG Rich Text Note Composer & Editor.
 * - Zero visible raw markdown symbols (Bold, Italic, Underline, Strikethrough, Heading, Quote
 *   are rendered cleanly via span-based VisualTransformation).
 * - Clean uncluttered top bar with guaranteed unclipped Simpan CTA.
 * - Edge-to-edge docked rich-text toolbar that sits flush above the IME keyboard without gaps
 *   (parent Scaffold already applies WindowInsets.safeDrawing).
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
    var richState by remember(existingNote?.id) {
        mutableStateOf(deserializeRichNote(existingNote?.content ?: ""))
    }
    var selectedTag by remember(existingNote?.id) {
        mutableStateOf(existingNote?.tag?.ifBlank { "Umum" } ?: "Umum")
    }
    var selectedPalette by remember(existingNote?.id) {
        mutableStateOf(NoteColorPalette.fromKey(existingNote?.colorKey))
    }
    var pinned by remember(existingNote?.id) { mutableStateOf(existingNote?.pinned ?: false) }
    var showPaletteStrip by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val surfaceBg = selectedPalette.backgroundColor(colors.isDark)
    val surfaceBorder = selectedPalette.borderColor(colors.isDark)

    val richVisualTransformation = remember(
        richState.spans,
        richState.headingLines,
        richState.quoteLines,
        colors
    ) {
        WysiwygRichTextVisualTransformation(
            spans = richState.spans,
            headingLines = richState.headingLines,
            quoteLines = richState.quoteLines,
            accentColor = colors.accentAmber,
            emeraldColor = colors.accentEmerald,
            mutedColor = colors.textMuted,
            textColor = colors.textPrimary
        )
    }

    val wordCount = remember(richState.plainText) {
        richState.plainText.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    val canSave = (title.isNotBlank() || richState.plainText.isNotBlank()) && !isSaving
    val currentListKind = richState.currentLineListKind
    val isOnChecklistLine = currentListKind == ListPrefixKind.CHECK_UNCHECKED ||
        currentListKind == ListPrefixKind.CHECK_CHECKED

    fun performSave() {
        val effectiveTitle = title.trim().ifBlank {
            richState.plainText.lineSequence().firstOrNull { it.isNotBlank() }
                ?.removePrefix("• ")
                ?.removePrefix("☐ ")
                ?.removePrefix("☑ ")
                ?.trim()
                ?.take(60)
                ?.ifBlank { "Catatan Tanpa Judul" }
                ?: "Catatan Tanpa Judul"
        }
        val serializedContent = serializeRichNote(richState).take(9950)
        onSave(
            existingNote?.id,
            effectiveTitle,
            serializedContent,
            selectedTag,
            selectedPalette.key,
            pinned,
            onNavigateBack
        )
    }

    // Note: Parent Scaffold already applies WindowInsets.safeDrawing (status bar + nav bar + IME).
    // Do NOT add .imePadding() or .navigationBarsPadding() here, otherwise the keyboard inset is doubled!
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceBg)
            .testTag("catatan_editor_screen")
    ) {
        // 1. Clean, Roomy Top Bar (Back + Pin + More Menu + Unclipped Simpan Pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.cardSurface.copy(alpha = 0.92f))
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

            // Pin Toggle
            EditorTopIconButton(
                icon = if (pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (pinned) "Lepas sematan" else "Sematkan catatan",
                active = pinned,
                borderColor = surfaceBorder,
                onClick = { pinned = !pinned },
                modifier = Modifier.testTag("note_editor_pin")
            )

            // More Actions Menu (Warna Kartu, Hapus Catatan)
            Box {
                EditorTopIconButton(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = "Menu catatan lainnya",
                    active = showMoreMenu,
                    borderColor = surfaceBorder,
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.testTag("note_editor_more")
                )

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    containerColor = colors.cardSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Warna Catatan", color = colors.textPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = colors.accentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            showPaletteStrip = !showPaletteStrip
                        }
                    )
                    if (existingNote != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Hapus Catatan",
                                    color = colors.accentCrimson,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = colors.accentCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                showDeleteConfirm = true
                            },
                            modifier = Modifier.testTag("note_editor_delete")
                        )
                    }
                }
            }

            // Prominent Unclipped Simpan Pill
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
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            fontSize = 13.sp
                        ),
                        color = if (canSave) colors.heroOnDark else colors.textMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 2. Compact Tag Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
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
                    color = if (selected) colors.heroDarkSurface else colors.cardSurface.copy(alpha = 0.8f),
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
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 3. Scrollable Writing Canvas (fills exact remaining height above the docked toolbar)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    text = "$wordCount kata",
                    style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                    color = colors.accentAmber
                )
            }

            // Borderless Keep-Style Title Input
            BasicTextField(
                value = title,
                onValueChange = { title = it.take(120) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 25.sp,
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
                                    fontSize = 19.sp
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

            HorizontalDivider(color = surfaceBorder.copy(alpha = 0.7f))

            // True WYSIWYG Rich Text Body Field (zero raw markdown characters!)
            BasicTextField(
                value = richState.fieldValue,
                onValueChange = { richState = richState.onFieldValueChange(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.textPrimary,
                    lineHeight = 25.sp,
                    fontSize = 15.5.sp
                ),
                cursorBrush = SolidColor(colors.accentAmber),
                visualTransformation = richVisualTransformation,
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp)
                    ) {
                        if (richState.plainText.isEmpty()) {
                            Text(
                                text = "Mulai tulis catatan riset perusahaan, poin interview, checklist persiapan, atau negosiasi gaji...",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Contextual Checklist Toggle Pill (appears smoothly whenever cursor is on a checklist line)
        AnimatedVisibility(
            visible = isOnChecklistLine,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val isChecked = currentListKind == ListPrefixKind.CHECK_CHECKED
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardSurface.copy(alpha = 0.96f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isChecked) "Item checklist selesai" else "Item checklist aktif",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .tactileClickable { richState = richState.toggleCurrentChecklistChecked() }
                        .testTag("fmt_checklist_complete_toggle"),
                    shape = RoundedCornerShape(999.dp),
                    color = if (isChecked) colors.recessedSurface else colors.emeraldSoftBg,
                    border = BorderStroke(
                        1.dp,
                        if (isChecked) colors.borderSubtle else colors.emeraldBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TaskAlt,
                            contentDescription = null,
                            tint = if (isChecked) colors.textSecondary else colors.accentEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isChecked) "Tandai Belum" else "Tandai Selesai",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isChecked) colors.textSecondary else colors.accentEmerald
                        )
                    }
                }
            }
        }

        // 5. Google Keep-Style Pastel Color Picker Strip (docked right above the bottom bar)
        AnimatedVisibility(
            visible = showPaletteStrip,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardSurface)
            ) {
                HorizontalDivider(color = surfaceBorder.copy(alpha = 0.8f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        }

        // 6. Sleek Edge-to-Edge Docked Rich Text Formatting Bar (sits flush above keyboard!)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.cardSurface,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = surfaceBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormatToolbarButton(
                        icon = Icons.Rounded.Title,
                        contentDescription = "Judul (Heading)",
                        active = richState.isCurrentLineHeading,
                        onClick = { richState = richState.toggleHeading() },
                        testTag = "fmt_heading"
                    )
                    FormatToolbarButton(
                        icon = Icons.Rounded.FormatBold,
                        contentDescription = "Tebal (Bold)",
                        active = richState.isStyleActive(InlineStyle.BOLD),
                        onClick = { richState = richState.toggleInlineStyle(InlineStyle.BOLD) },
                        testTag = "fmt_bold"
                    )
                    FormatToolbarButton(
                        icon = Icons.Rounded.FormatItalic,
                        contentDescription = "Miring (Italic)",
                        active = richState.isStyleActive(InlineStyle.ITALIC),
                        onClick = { richState = richState.toggleInlineStyle(InlineStyle.ITALIC) },
                        testTag = "fmt_italic"
                    )
                    FormatToolbarButton(
                        icon = Icons.Rounded.FormatUnderlined,
                        contentDescription = "Garis bawah (Underline)",
                        active = richState.isStyleActive(InlineStyle.UNDERLINE),
                        onClick = { richState = richState.toggleInlineStyle(InlineStyle.UNDERLINE) },
                        testTag = "fmt_underline"
                    )
                    FormatToolbarButton(
                        icon = Icons.Rounded.FormatStrikethrough,
                        contentDescription = "Coret (Strikethrough)",
                        active = richState.isStyleActive(InlineStyle.STRIKETHROUGH),
                        onClick = { richState = richState.toggleInlineStyle(InlineStyle.STRIKETHROUGH) },
                        testTag = "fmt_strike"
                    )

                    ToolbarDivider()

                    FormatToolbarButton(
                        icon = Icons.Outlined.CheckBox,
                        contentDescription = "Checklist tugas",
                        active = isOnChecklistLine,
                        onClick = { richState = richState.toggleListPrefix(ListPrefixKind.CHECK_UNCHECKED) },
                        testTag = "fmt_checklist"
                    )
                    FormatToolbarButton(
                        icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                        contentDescription = "Daftar poin",
                        active = currentListKind == ListPrefixKind.BULLET,
                        onClick = { richState = richState.toggleListPrefix(ListPrefixKind.BULLET) },
                        testTag = "fmt_bullet"
                    )
                    FormatToolbarButton(
                        icon = Icons.Outlined.FormatListNumbered,
                        contentDescription = "Daftar bernomor",
                        active = currentListKind == ListPrefixKind.NUMBERED,
                        onClick = { richState = richState.toggleListPrefix(ListPrefixKind.NUMBERED) },
                        testTag = "fmt_numbered"
                    )
                    FormatToolbarButton(
                        icon = Icons.Outlined.FormatQuote,
                        contentDescription = "Kutipan",
                        active = richState.isCurrentLineQuote,
                        onClick = { richState = richState.toggleQuote() },
                        testTag = "fmt_quote"
                    )

                    ToolbarDivider()

                    FormatToolbarButton(
                        icon = Icons.Outlined.Palette,
                        contentDescription = "Pilih warna catatan",
                        active = showPaletteStrip,
                        onClick = { showPaletteStrip = !showPaletteStrip },
                        testTag = "note_editor_palette"
                    )
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
private fun ToolbarDivider() {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .width(1.dp)
            .height(22.dp)
            .background(colors.borderSubtle)
    )
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
            .background(if (active) colors.amberSoftBg else colors.cardSurface.copy(alpha = 0.92f))
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
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) colors.amberSoftBg else Color.Transparent)
            .border(
                width = if (active) 1.dp else 0.dp,
                color = if (active) colors.accentAmber.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(11.dp)
            )
            .tactileClickable(pressedScale = 0.90f, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) colors.accentAmber else colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
