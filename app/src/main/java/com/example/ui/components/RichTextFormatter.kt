package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private const val RTF_V2_PREFIX = "<!--RTF2-->"

enum class NoteColorPalette(
    val key: String,
    val label: String,
    val lightBg: Color,
    val lightBorder: Color,
    val darkBg: Color,
    val darkBorder: Color,
    val swatchColor: Color
) {
    DEFAULT(
        key = "DEFAULT",
        label = "Krem",
        lightBg = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFE7DFD3),
        darkBg = Color(0xFF1D1F23),
        darkBorder = Color(0xFF2D3138),
        swatchColor = Color(0xFFF6F1E9)
    ),
    AMBER(
        key = "AMBER",
        label = "Aprikot",
        lightBg = Color(0xFFFFF5E6),
        lightBorder = Color(0xFFF2D4A8),
        darkBg = Color(0xFF2B2219),
        darkBorder = Color(0xFF4D3824),
        swatchColor = Color(0xFFFBD399)
    ),
    MINT(
        key = "MINT",
        label = "Mint",
        lightBg = Color(0xFFEEF9F3),
        lightBorder = Color(0xFFBFE5D0),
        darkBg = Color(0xFF192822),
        darkBorder = Color(0xFF264739),
        swatchColor = Color(0xFFA8E6C5)
    ),
    SKY(
        key = "SKY",
        label = "Langit",
        lightBg = Color(0xFFEFF6FF),
        lightBorder = Color(0xFFC2DAFB),
        darkBg = Color(0xFF182230),
        darkBorder = Color(0xFF283C57),
        swatchColor = Color(0xFFAECDFB)
    ),
    ROSE(
        key = "ROSE",
        label = "Mawar",
        lightBg = Color(0xFFFFF1F3),
        lightBorder = Color(0xFFF7C9D1),
        darkBg = Color(0xFF2B1C20),
        darkBorder = Color(0xFF4F2B34),
        swatchColor = Color(0xFFF6B7C3)
    ),
    LAVENDER(
        key = "LAVENDER",
        label = "Lavender",
        lightBg = Color(0xFFF5F1FF),
        lightBorder = Color(0xFFD8C9FA),
        darkBg = Color(0xFF231D30),
        darkBorder = Color(0xFF3E3159),
        swatchColor = Color(0xFFCCB8FA)
    );

    fun backgroundColor(isDark: Boolean): Color = if (isDark) darkBg else lightBg
    fun borderColor(isDark: Boolean): Color = if (isDark) darkBorder else lightBorder

    companion object {
        fun fromKey(key: String?): NoteColorPalette =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
    }
}

enum class InlineStyle(val code: String) {
    BOLD("B"),
    ITALIC("I"),
    UNDERLINE("U"),
    STRIKETHROUGH("S");

    companion object {
        fun fromCode(code: String): InlineStyle? = entries.firstOrNull { it.code == code }
    }
}

data class StyleSpan(
    val start: Int,
    val end: Int,
    val style: InlineStyle
)

enum class ListPrefixKind {
    NONE,
    BULLET,
    NUMBERED,
    CHECK_UNCHECKED,
    CHECK_CHECKED
}

/**
 * Pure WYSIWYG rich-text editor state.
 * Keeps clean human-readable text in [fieldValue] (zero raw markdown characters like **, __, ~~, #)
 * while storing inline [spans], [headingLines], [quoteLines], and [activeStyles] separately.
 */
data class RichTextEditorState(
    val fieldValue: TextFieldValue = TextFieldValue(""),
    val spans: List<StyleSpan> = emptyList(),
    val headingLines: Set<Int> = emptySet(),
    val quoteLines: Set<Int> = emptySet(),
    val activeStyles: Set<InlineStyle> = emptySet()
) {
    val plainText: String get() = fieldValue.text

    val currentLineIndex: Int
        get() {
            val cursor = fieldValue.selection.start.coerceIn(0, plainText.length)
            var count = 0
            for (i in 0 until cursor) {
                if (plainText[i] == '\n') count++
            }
            return count
        }

    val currentLineRange: IntRange
        get() {
            val cursor = fieldValue.selection.start.coerceIn(0, plainText.length)
            val start = plainText.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
                .let { if (it < 0) 0 else it + 1 }
            val end = plainText.indexOf('\n', cursor)
                .let { if (it < 0) plainText.length else it }
            return start..end
        }

    val isCurrentLineHeading: Boolean get() = currentLineIndex in headingLines
    val isCurrentLineQuote: Boolean get() = currentLineIndex in quoteLines

    val currentLineListKind: ListPrefixKind
        get() {
            val range = currentLineRange
            val line = plainText.substring(range.first, range.last)
            return detectListPrefix(line).first
        }

    fun isStyleActive(style: InlineStyle): Boolean {
        val selStart = fieldValue.selection.min.coerceIn(0, plainText.length)
        val selEnd = fieldValue.selection.max.coerceIn(0, plainText.length)
        return if (selStart == selEnd) {
            style in activeStyles
        } else {
            isRangeFullyCovered(spans, selStart, selEnd, style)
        }
    }

    /**
     * Handles live text edits and cursor movement from BasicTextField.
     * Diffs old vs new text, adjusts spans & line sets accurately, applies [activeStyles] to
     * newly typed characters, and auto-continues bullet / numbered / checklist lines on Enter.
     */
    fun onFieldValueChange(rawNewValue: TextFieldValue, maxLength: Int = 8500): RichTextEditorState {
        val clampedText = if (rawNewValue.text.length > maxLength) {
            rawNewValue.text.take(maxLength)
        } else {
            rawNewValue.text
        }
        val clampedSelection = TextRange(
            rawNewValue.selection.start.coerceIn(0, clampedText.length),
            rawNewValue.selection.end.coerceIn(0, clampedText.length)
        )
        val newValue = rawNewValue.copy(text = clampedText, selection = clampedSelection)
        val oldText = fieldValue.text
        val newText = newValue.text

        // 1. Selection / composition change only (no text change)
        if (oldText == newText) {
            val updatedActive = if (newValue.selection != fieldValue.selection) {
                computeActiveStylesForSelection(spans, newValue.selection, newText.length)
            } else {
                activeStyles
            }
            return copy(fieldValue = newValue, activeStyles = updatedActive)
        }

        // 2. Compute exact minimal diff (prefix, deletedEnd, insertedLen)
        var prefix = 0
        val minLen = minOf(oldText.length, newText.length)
        while (prefix < minLen && oldText[prefix] == newText[prefix]) {
            prefix++
        }
        var oldSuffix = oldText.length
        var newSuffix = newText.length
        while (oldSuffix > prefix && newSuffix > prefix && oldText[oldSuffix - 1] == newText[newSuffix - 1]) {
            oldSuffix--
            newSuffix--
        }

        val deletedEnd = oldSuffix
        val deletedLen = deletedEnd - prefix
        val insertedLen = newSuffix - prefix

        // 3. Smart Enter auto-continuation for Bullet, Checklist, Numbered lists
        if (deletedLen == 0 && insertedLen == 1 && newText[prefix] == '\n') {
            val prevLineStart = oldText.lastIndexOf('\n', (prefix - 1).coerceAtLeast(0))
                .let { if (it < 0) 0 else it + 1 }
            val prevLine = oldText.substring(prevLineStart, prefix)
            val (kind, prefixLen) = detectListPrefix(prevLine)
            if (kind != ListPrefixKind.NONE) {
                val contentAfterPrefix = prevLine.drop(prefixLen)
                if (contentAfterPrefix.isBlank()) {
                    // Empty list line + Enter -> exit list cleanly
                    val exitedText = oldText.substring(0, prevLineStart) + oldText.substring(prefix)
                    val exitedState = applyTextEdit(
                        oldText = oldText,
                        newText = exitedText,
                        newSelection = TextRange(prevLineStart),
                        prefix = prevLineStart,
                        deletedEnd = prefix,
                        insertedLen = 0,
                        applyActiveToInserted = false
                    )
                    return exitedState
                } else {
                    val nextPrefix = when (kind) {
                        ListPrefixKind.BULLET -> "• "
                        ListPrefixKind.CHECK_UNCHECKED, ListPrefixKind.CHECK_CHECKED -> "☐ "
                        ListPrefixKind.NUMBERED -> {
                            val num = NUMBERED_LINE_REGEX.find(prevLine)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                            "${num + 1}. "
                        }
                        ListPrefixKind.NONE -> ""
                    }
                    if (nextPrefix.isNotEmpty() && newText.length + nextPrefix.length <= maxLength) {
                        val continuedText = newText.substring(0, prefix + 1) + nextPrefix + newText.substring(prefix + 1)
                        val continuedCursor = prefix + 1 + nextPrefix.length
                        return applyTextEdit(
                            oldText = oldText,
                            newText = continuedText,
                            newSelection = TextRange(continuedCursor),
                            prefix = prefix,
                            deletedEnd = deletedEnd,
                            insertedLen = 1 + nextPrefix.length,
                            applyActiveToInserted = false
                        )
                    }
                }
            }
        }

        return applyTextEdit(
            oldText = oldText,
            newText = newText,
            newSelection = newValue.selection,
            prefix = prefix,
            deletedEnd = deletedEnd,
            insertedLen = insertedLen,
            applyActiveToInserted = true,
            composition = newValue.composition
        )
    }

    private fun applyTextEdit(
        oldText: String,
        newText: String,
        newSelection: TextRange,
        prefix: Int,
        deletedEnd: Int,
        insertedLen: Int,
        applyActiveToInserted: Boolean,
        composition: TextRange? = null
    ): RichTextEditorState {
        val deletedLen = deletedEnd - prefix
        val delta = insertedLen - deletedLen

        // Shift & slice inline spans
        val updatedSpans = mutableListOf<StyleSpan>()
        for (span in spans) {
            when {
                span.end <= prefix -> updatedSpans.add(span)
                span.start >= deletedEnd -> updatedSpans.add(
                    span.copy(start = span.start + delta, end = span.end + delta)
                )
                else -> {
                    val hasBefore = span.start < prefix
                    val hasAfter = span.end > deletedEnd
                    when {
                        hasBefore && hasAfter -> {
                            updatedSpans.add(span.copy(end = span.end + delta))
                        }
                        hasBefore -> {
                            if (prefix > span.start) {
                                updatedSpans.add(span.copy(end = prefix))
                            }
                        }
                        hasAfter -> {
                            val newStart = prefix + insertedLen
                            val newEnd = span.end + delta
                            if (newEnd > newStart) {
                                updatedSpans.add(span.copy(start = newStart, end = newEnd))
                            }
                        }
                    }
                }
            }
        }

        if (applyActiveToInserted && insertedLen > 0) {
            // Apply active inline styles to inserted characters (excluding pure newline)
            val insertedText = newText.substring(prefix, prefix + insertedLen)
            if (insertedText != "\n") {
                for (style in activeStyles) {
                    updatedSpans.add(StyleSpan(prefix, prefix + insertedLen, style))
                }
            }
        }

        // Shift headingLines & quoteLines
        val startLineIdx = oldText.take(prefix).count { it == '\n' }
        val deletedNewlines = oldText.substring(prefix, deletedEnd).count { it == '\n' }
        val insertedNewlines = newText.substring(prefix, prefix + insertedLen).count { it == '\n' }
        val lineDelta = insertedNewlines - deletedNewlines
        val maxLineIdx = newText.count { it == '\n' }

        fun shiftLineSet(set: Set<Int>): Set<Int> {
            val out = mutableSetOf<Int>()
            for (idx in set) {
                when {
                    idx < startLineIdx -> out.add(idx)
                    idx == startLineIdx -> {
                        // Keep formatting on the original line unless entire line was deleted
                        if (idx <= maxLineIdx) out.add(idx)
                    }
                    idx > startLineIdx + deletedNewlines -> {
                        val shifted = idx + lineDelta
                        if (shifted in 0..maxLineIdx) out.add(shifted)
                    }
                }
            }
            return out
        }

        val mergedSpans = normalizeSpans(updatedSpans, newText.length)
        val nextActive = if (insertedLen > 0 && newText.substring(prefix, prefix + insertedLen).endsWith("\n")) {
            // Reset inline active styles cleanly on a brand new line? Keep user's explicit styles except when entering empty line
            activeStyles
        } else if (deletedLen > 0 && insertedLen == 0) {
            computeActiveStylesForSelection(mergedSpans, newSelection, newText.length)
        } else {
            activeStyles
        }

        return copy(
            fieldValue = TextFieldValue(
                text = newText,
                selection = newSelection,
                composition = composition
            ),
            spans = mergedSpans,
            headingLines = shiftLineSet(headingLines),
            quoteLines = shiftLineSet(quoteLines),
            activeStyles = nextActive
        )
    }

    /**
     * Toggles an inline style (Bold, Italic, Underline, Strikethrough).
     * - If text is selected: cleanly adds or removes that style across the selection (never stacks!).
     * - If cursor is collapsed: toggles active typing style so newly typed characters use it immediately.
     */
    fun toggleInlineStyle(style: InlineStyle): RichTextEditorState {
        val selStart = fieldValue.selection.min.coerceIn(0, plainText.length)
        val selEnd = fieldValue.selection.max.coerceIn(0, plainText.length)

        if (selStart == selEnd) {
            val nextActive = if (style in activeStyles) activeStyles - style else activeStyles + style
            return copy(activeStyles = nextActive)
        }

        val alreadyCovered = isRangeFullyCovered(spans, selStart, selEnd, style)
        val updatedSpans = if (alreadyCovered) {
            removeStyleFromRange(spans, selStart, selEnd, style)
        } else {
            normalizeSpans(spans + StyleSpan(selStart, selEnd, style), plainText.length)
        }
        val nextActive = computeActiveStylesForSelection(updatedSpans, fieldValue.selection, plainText.length)
        return copy(spans = updatedSpans, activeStyles = nextActive)
    }

    /**
     * Toggles Heading style on all lines touched by the current selection/cursor.
     * Mutually exclusive with Quote.
     */
    fun toggleHeading(): RichTextEditorState {
        val lineIndices = selectedLineIndices()
        val allHeading = lineIndices.all { it in headingLines }
        val nextHeadings = if (allHeading) headingLines - lineIndices else headingLines + lineIndices
        val nextQuotes = if (allHeading) quoteLines else quoteLines - lineIndices
        return copy(headingLines = nextHeadings, quoteLines = nextQuotes)
    }

    /**
     * Toggles Blockquote style on all lines touched by the current selection/cursor.
     * Mutually exclusive with Heading.
     */
    fun toggleQuote(): RichTextEditorState {
        val lineIndices = selectedLineIndices()
        val allQuote = lineIndices.all { it in quoteLines }
        val nextQuotes = if (allQuote) quoteLines - lineIndices else quoteLines + lineIndices
        val nextHeadings = if (allQuote) headingLines else headingLines - lineIndices
        return copy(quoteLines = nextQuotes, headingLines = nextHeadings)
    }

    /**
     * Toggles or cleanly switches the current line's list prefix (Bullet, Numbered, Checklist).
     * Never stacks two prefixes (e.g. switching from Checklist to Bullet cleanly replaces ☐ with •).
     */
    fun toggleListPrefix(target: ListPrefixKind): RichTextEditorState {
        val range = currentLineRange
        val lineStart = range.first
        val lineEnd = range.last
        val currentLine = plainText.substring(lineStart, lineEnd)
        val (existingKind, existingLen) = detectListPrefix(currentLine)

        val strippedLine = currentLine.drop(existingLen)
        val isSameCategory = (existingKind == target) ||
            (target == ListPrefixKind.CHECK_UNCHECKED && existingKind == ListPrefixKind.CHECK_CHECKED)

        val replacementPrefix = if (isSameCategory) {
            ""
        } else {
            when (target) {
                ListPrefixKind.BULLET -> "• "
                ListPrefixKind.CHECK_UNCHECKED -> "☐ "
                ListPrefixKind.CHECK_CHECKED -> "☑ "
                ListPrefixKind.NUMBERED -> {
                    val prevNum = previousLineNumber(lineStart)
                    "${prevNum + 1}. "
                }
                ListPrefixKind.NONE -> ""
            }
        }

        val newLine = replacementPrefix + strippedLine
        val newText = plainText.substring(0, lineStart) + newLine + plainText.substring(lineEnd)
        val delta = newLine.length - currentLine.length
        val newCursor = (fieldValue.selection.start + delta).coerceIn(lineStart + replacementPrefix.length, newText.length)

        return applyTextEdit(
            oldText = plainText,
            newText = newText,
            newSelection = TextRange(newCursor),
            prefix = lineStart,
            deletedEnd = lineStart + existingLen,
            insertedLen = replacementPrefix.length,
            applyActiveToInserted = false
        )
    }

    /**
     * Toggles the current checklist line between ☐ (unchecked) and ☑ (checked).
     */
    fun toggleCurrentChecklistChecked(): RichTextEditorState {
        val range = currentLineRange
        val lineStart = range.first
        val lineEnd = range.last
        val currentLine = plainText.substring(lineStart, lineEnd)
        val (existingKind, existingLen) = detectListPrefix(currentLine)

        val newPrefix = when (existingKind) {
            ListPrefixKind.CHECK_UNCHECKED -> "☑ "
            ListPrefixKind.CHECK_CHECKED -> "☐ "
            else -> return this
        }
        val newText = plainText.substring(0, lineStart) + newPrefix + currentLine.drop(existingLen) + plainText.substring(lineEnd)
        return applyTextEdit(
            oldText = plainText,
            newText = newText,
            newSelection = fieldValue.selection,
            prefix = lineStart,
            deletedEnd = lineStart + existingLen,
            insertedLen = newPrefix.length,
            applyActiveToInserted = false
        )
    }

    private fun previousLineNumber(currentLineStart: Int): Int {
        if (currentLineStart <= 0) return 0
        val prevEnd = currentLineStart - 1
        val prevStart = plainText.lastIndexOf('\n', (prevEnd - 1).coerceAtLeast(0))
            .let { if (it < 0) 0 else it + 1 }
        val prevLine = plainText.substring(prevStart, prevEnd)
        return NUMBERED_LINE_REGEX.find(prevLine)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun selectedLineIndices(): Set<Int> {
        val selStart = fieldValue.selection.min.coerceIn(0, plainText.length)
        val selEnd = fieldValue.selection.max.coerceIn(0, plainText.length)
        val firstLine = plainText.take(selStart).count { it == '\n' }
        val lastLine = plainText.take(selEnd).count { it == '\n' }
        return (firstLine..lastLine).toSet()
    }
}

/**
 * Pure WYSIWYG VisualTransformation for [RichTextEditorState].
 * Renders bold, italic, underline, strikethrough, headings, quotes, bullets, and checklists
 * directly onto clean plain text using OffsetMapping.Identity (zero raw syntax tokens!).
 */
class WysiwygRichTextVisualTransformation(
    private val spans: List<StyleSpan>,
    private val headingLines: Set<Int>,
    private val quoteLines: Set<Int>,
    private val accentColor: Color,
    private val emeraldColor: Color,
    private val mutedColor: Color,
    private val textColor: Color
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildStyledAnnotatedString(
            plainText = text.text,
            spans = spans,
            headingLines = headingLines,
            quoteLines = quoteLines,
            accentColor = accentColor,
            emeraldColor = emeraldColor,
            mutedColor = mutedColor,
            textColor = textColor
        )
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

fun buildStyledAnnotatedString(
    plainText: String,
    spans: List<StyleSpan>,
    headingLines: Set<Int>,
    quoteLines: Set<Int>,
    accentColor: Color,
    emeraldColor: Color,
    mutedColor: Color,
    textColor: Color
): AnnotatedString = buildAnnotatedString {
    append(plainText)

    // 1. Line-level visual styling (Headings, Quotes, Bullets, Numbered, Checklists)
    var lineStart = 0
    plainText.split("\n").forEachIndexed { lineIdx, line ->
        val lineEnd = (lineStart + line.length).coerceAtMost(plainText.length)
        if (lineEnd > lineStart) {
            if (lineIdx in headingLines) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = textColor
                    ),
                    lineStart,
                    lineEnd
                )
            } else if (lineIdx in quoteLines) {
                addStyle(
                    SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = mutedColor
                    ),
                    lineStart,
                    lineEnd
                )
            }

            val (kind, prefixLen) = detectListPrefix(line)
            val prefixEnd = (lineStart + prefixLen).coerceAtMost(lineEnd)
            when (kind) {
                ListPrefixKind.BULLET -> {
                    addStyle(
                        SpanStyle(color = accentColor, fontWeight = FontWeight.ExtraBold),
                        lineStart,
                        prefixEnd
                    )
                }
                ListPrefixKind.NUMBERED -> {
                    addStyle(
                        SpanStyle(color = accentColor, fontWeight = FontWeight.Bold),
                        lineStart,
                        prefixEnd
                    )
                }
                ListPrefixKind.CHECK_UNCHECKED -> {
                    addStyle(
                        SpanStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        lineStart,
                        prefixEnd
                    )
                }
                ListPrefixKind.CHECK_CHECKED -> {
                    addStyle(
                        SpanStyle(color = emeraldColor, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        lineStart,
                        prefixEnd
                    )
                    if (lineEnd > prefixEnd) {
                        addStyle(
                            SpanStyle(
                                color = mutedColor,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            prefixEnd,
                            lineEnd
                        )
                    }
                }
                ListPrefixKind.NONE -> Unit
            }
        }
        lineStart = lineEnd + 1
    }

    // 2. Inline SpanStyles (supports combined Bold + Italic + Underline + Strikethrough cleanly)
    val textLen = plainText.length
    for (span in spans) {
        val s = span.start.coerceIn(0, textLen)
        val e = span.end.coerceIn(0, textLen)
        if (e > s) {
            val style = when (span.style) {
                InlineStyle.BOLD -> SpanStyle(fontWeight = FontWeight.ExtraBold)
                InlineStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                InlineStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                InlineStyle.STRIKETHROUGH -> SpanStyle(
                    textDecoration = TextDecoration.LineThrough,
                    color = mutedColor
                )
            }
            addStyle(style, s, e)
        }
    }
}

/**
 * Serializes [RichTextEditorState] cleanly for Firestore storage.
 * Uses compact JSON envelope when rich spans/headings/quotes exist, or plain text when unformatted.
 */
fun serializeRichNote(state: RichTextEditorState): String {
    val text = state.plainText
    if (state.spans.isEmpty() && state.headingLines.isEmpty() && state.quoteLines.isEmpty()) {
        return text
    }
    val root = JSONObject()
    root.put("t", text)
    if (state.spans.isNotEmpty()) {
        val arr = JSONArray()
        for (span in normalizeSpans(state.spans, text.length)) {
            val item = JSONArray()
            item.put(span.start)
            item.put(span.end)
            item.put(span.style.code)
            arr.put(item)
        }
        root.put("s", arr)
    }
    if (state.headingLines.isNotEmpty()) {
        val arr = JSONArray()
        state.headingLines.sorted().forEach { arr.put(it) }
        root.put("h", arr)
    }
    if (state.quoteLines.isNotEmpty()) {
        val arr = JSONArray()
        state.quoteLines.sorted().forEach { arr.put(it) }
        root.put("q", arr)
    }
    return RTF_V2_PREFIX + root.toString()
}

/**
 * Deserializes a stored Firestore note string (either new RTF2 JSON or legacy Markdown notes)
 * into a 100% clean [RichTextEditorState] with zero visible raw markdown characters.
 */
fun deserializeRichNote(raw: String): RichTextEditorState {
    if (raw.isEmpty()) return RichTextEditorState()

    if (raw.startsWith(RTF_V2_PREFIX)) {
        runCatching {
            val obj = JSONObject(raw.removePrefix(RTF_V2_PREFIX))
            val text = obj.optString("t", "")
            val spans = mutableListOf<StyleSpan>()
            val sArr = obj.optJSONArray("s")
            if (sArr != null) {
                for (i in 0 until sArr.length()) {
                    val item = sArr.optJSONArray(i) ?: continue
                    if (item.length() >= 3) {
                        val start = item.optInt(0, 0)
                        val end = item.optInt(1, 0)
                        val style = InlineStyle.fromCode(item.optString(2, ""))
                        if (style != null && end > start) {
                            spans.add(StyleSpan(start, end, style))
                        }
                    }
                }
            }
            val headings = mutableSetOf<Int>()
            val hArr = obj.optJSONArray("h")
            if (hArr != null) {
                for (i in 0 until hArr.length()) headings.add(hArr.optInt(i))
            }
            val quotes = mutableSetOf<Int>()
            val qArr = obj.optJSONArray("q")
            if (qArr != null) {
                for (i in 0 until qArr.length()) quotes.add(qArr.optInt(i))
            }
            return RichTextEditorState(
                fieldValue = TextFieldValue(text = text, selection = TextRange(text.length)),
                spans = normalizeSpans(spans, text.length),
                headingLines = headings,
                quoteLines = quotes
            )
        }
    }

    // Legacy Markdown note migration -> clean WYSIWYG state (strips stray **, *, __, ~~, [], #)
    return parseLegacyMarkdownToWysiwyg(raw)
}

/**
 * Extracts clean plain text from any stored note (for search filtering, word count, or fallback title).
 */
fun extractPlainNoteText(raw: String): String = deserializeRichNote(raw).plainText

/**
 * Renders any stored note string into a clean rich-text [AnnotatedString] for Google Keep cards.
 */
fun buildCleanRichTextAnnotatedString(
    raw: String,
    accentColor: Color,
    emeraldColor: Color,
    mutedColor: Color,
    textColor: Color
): AnnotatedString {
    val parsed = deserializeRichNote(raw)
    return buildStyledAnnotatedString(
        plainText = parsed.plainText,
        spans = parsed.spans,
        headingLines = parsed.headingLines,
        quoteLines = parsed.quoteLines,
        accentColor = accentColor,
        emeraldColor = emeraldColor,
        mutedColor = mutedColor,
        textColor = textColor
    )
}

private val NUMBERED_LINE_REGEX = Regex("""^(\d+)\.\s""")

private fun detectListPrefix(line: String): Pair<ListPrefixKind, Int> {
    return when {
        line.startsWith("• ") -> ListPrefixKind.BULLET to 2
        line.startsWith("☐ ") -> ListPrefixKind.CHECK_UNCHECKED to 2
        line.startsWith("☑ ") -> ListPrefixKind.CHECK_CHECKED to 2
        else -> {
            val match = NUMBERED_LINE_REGEX.find(line)
            if (match != null) {
                ListPrefixKind.NUMBERED to match.value.length
            } else {
                ListPrefixKind.NONE to 0
            }
        }
    }
}

private fun parseLegacyMarkdownToWysiwyg(raw: String): RichTextEditorState {
    val outText = StringBuilder()
    val spans = mutableListOf<StyleSpan>()
    val headings = mutableSetOf<Int>()
    val quotes = mutableSetOf<Int>()

    val lines = raw.split("\n")
    lines.forEachIndexed { lineIdx, rawLine ->
        if (lineIdx > 0) outText.append("\n")
        var line = rawLine

        // Clean legacy line prefixes
        when {
            line.startsWith("## ") -> {
                headings.add(lineIdx)
                line = line.removePrefix("## ")
            }
            line.startsWith("# ") -> {
                headings.add(lineIdx)
                line = line.removePrefix("# ")
            }
            line.startsWith("> ") -> {
                quotes.add(lineIdx)
                line = line.removePrefix("> ")
            }
            line.startsWith("[x] ") || line.startsWith("[X] ") -> {
                outText.append("☑ ")
                line = line.drop(4).removePrefix("- ").removePrefix("• ")
            }
            line.startsWith("[ ] ") -> {
                outText.append("☐ ")
                line = line.drop(4).removePrefix("- ").removePrefix("• ")
            }
            line.startsWith("[] ") -> {
                outText.append("☐ ")
                line = line.drop(3).removePrefix("- ").removePrefix("• ")
            }
            line.startsWith("- ") -> {
                outText.append("• ")
                line = line.removePrefix("- ")
            }
        }

        // Parse legacy inline tokens cleanly (including stacked ***...***)
        val tokenRegex = Regex("""(\*{2,}([^*\n]+?)\*{2,}|__(.+?)__|~~(.+?)~~|\*([^*\n]+?)\*)""")
        var cursor = 0
        for (match in tokenRegex.findAll(line)) {
            if (match.range.first > cursor) {
                outText.append(stripStrayMarkers(line.substring(cursor, match.range.first)))
            }
            val spanStart = outText.length
            when {
                match.groups[2] != null -> {
                    val inner = match.groups[2]!!.value
                    outText.append(inner)
                    spans.add(StyleSpan(spanStart, outText.length, InlineStyle.BOLD))
                    if (match.value.startsWith("***") && match.value.endsWith("***")) {
                        spans.add(StyleSpan(spanStart, outText.length, InlineStyle.ITALIC))
                    }
                }
                match.groups[3] != null -> {
                    outText.append(match.groups[3]!!.value)
                    spans.add(StyleSpan(spanStart, outText.length, InlineStyle.UNDERLINE))
                }
                match.groups[4] != null -> {
                    outText.append(match.groups[4]!!.value)
                    spans.add(StyleSpan(spanStart, outText.length, InlineStyle.STRIKETHROUGH))
                }
                match.groups[5] != null -> {
                    outText.append(match.groups[5]!!.value)
                    spans.add(StyleSpan(spanStart, outText.length, InlineStyle.ITALIC))
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < line.length) {
            outText.append(stripStrayMarkers(line.substring(cursor)))
        }
    }

    val cleanText = outText.toString()
    return RichTextEditorState(
        fieldValue = TextFieldValue(text = cleanText, selection = TextRange(cleanText.length)),
        spans = normalizeSpans(spans, cleanText.length),
        headingLines = headings,
        quoteLines = quotes
    )
}

private fun stripStrayMarkers(chunk: String): String =
    chunk.replace("**", "").replace("~~", "").replace("__", "")

private fun isRangeFullyCovered(
    spans: List<StyleSpan>,
    start: Int,
    end: Int,
    style: InlineStyle
): Boolean {
    if (end <= start) return false
    val relevant = spans
        .filter { it.style == style && it.end > start && it.start < end }
        .sortedBy { it.start }
    var coveredUntil = start
    for (s in relevant) {
        if (s.start > coveredUntil) return false
        if (s.end > coveredUntil) coveredUntil = s.end
        if (coveredUntil >= end) return true
    }
    return coveredUntil >= end
}

private fun removeStyleFromRange(
    spans: List<StyleSpan>,
    start: Int,
    end: Int,
    style: InlineStyle
): List<StyleSpan> {
    val out = mutableListOf<StyleSpan>()
    for (span in spans) {
        if (span.style != style || span.end <= start || span.start >= end) {
            out.add(span)
        } else {
            if (span.start < start) out.add(span.copy(end = start))
            if (span.end > end) out.add(span.copy(start = end))
        }
    }
    return out
}

private fun normalizeSpans(spans: List<StyleSpan>, textLength: Int): List<StyleSpan> {
    if (spans.isEmpty() || textLength <= 0) return emptyList()
    val result = mutableListOf<StyleSpan>()
    for (style in InlineStyle.entries) {
        val sorted = spans
            .filter { it.style == style }
            .map { it.copy(start = it.start.coerceIn(0, textLength), end = it.end.coerceIn(0, textLength)) }
            .filter { it.end > it.start }
            .sortedBy { it.start }

        var current: StyleSpan? = null
        for (span in sorted) {
            if (current == null) {
                current = span
            } else if (span.start <= current.end) {
                current = current.copy(end = maxOf(current.end, span.end))
            } else {
                result.add(current)
                current = span
            }
        }
        if (current != null) result.add(current)
    }
    return result
}

private fun computeActiveStylesForSelection(
    spans: List<StyleSpan>,
    selection: TextRange,
    textLength: Int
): Set<InlineStyle> {
    val start = selection.min.coerceIn(0, textLength)
    val end = selection.max.coerceIn(0, textLength)
    return if (start == end) {
        if (start == 0) {
            emptySet()
        } else {
            val probe = start - 1
            InlineStyle.entries
                .filter { style -> spans.any { it.style == style && it.start <= probe && it.end > probe } }
                .toSet()
        }
    } else {
        InlineStyle.entries
            .filter { style -> isRangeFullyCovered(spans, start, end, style) }
            .toSet()
    }
}
