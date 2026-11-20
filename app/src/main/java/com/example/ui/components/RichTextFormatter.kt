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

/**
 * Live WYSIWYG-style VisualTransformation for KarirKu's Rich Text Note Editor.
 * Keeps 1:1 character offsets (OffsetMapping.Identity) so cursor positioning, IME composition,
 * and selection remain 100% smooth while rendering real bold, italic, underline, strikethrough,
 * headings, checklists, bullets, and quotes as you type.
 */
class RichTextLiveVisualTransformation(
    private val accentColor: Color,
    private val emeraldColor: Color,
    private val mutedColor: Color,
    private val textColor: Color
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val styled = buildAnnotatedString {
            append(raw)

            val markerStyle = SpanStyle(color = mutedColor.copy(alpha = 0.38f), fontSize = 11.sp)

            // 1. Line-level formatting (Headings, Checklists, Bullets, Blockquotes)
            var lineStart = 0
            raw.split("\n").forEach { line ->
                val lineEnd = lineStart + line.length
                when {
                    line.startsWith("## ") -> {
                        addStyle(markerStyle, lineStart, (lineStart + 3).coerceAtMost(lineEnd))
                        addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor),
                            (lineStart + 3).coerceAtMost(lineEnd),
                            lineEnd
                        )
                    }

                    line.startsWith("# ") -> {
                        addStyle(markerStyle, lineStart, (lineStart + 2).coerceAtMost(lineEnd))
                        addStyle(
                            SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = accentColor),
                            (lineStart + 2).coerceAtMost(lineEnd),
                            lineEnd
                        )
                    }

                    line.startsWith("[x] ") || line.startsWith("[X] ") -> {
                        addStyle(
                            SpanStyle(color = emeraldColor, fontWeight = FontWeight.Bold),
                            lineStart,
                            (lineStart + 4).coerceAtMost(lineEnd)
                        )
                        addStyle(
                            SpanStyle(
                                color = mutedColor,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            (lineStart + 4).coerceAtMost(lineEnd),
                            lineEnd
                        )
                    }

                    line.startsWith("[ ] ") -> {
                        addStyle(
                            SpanStyle(color = accentColor, fontWeight = FontWeight.Bold),
                            lineStart,
                            (lineStart + 4).coerceAtMost(lineEnd)
                        )
                    }

                    line.startsWith("- ") || line.startsWith("• ") -> {
                        addStyle(
                            SpanStyle(color = accentColor, fontWeight = FontWeight.ExtraBold),
                            lineStart,
                            (lineStart + 2).coerceAtMost(lineEnd)
                        )
                    }

                    line.startsWith("> ") -> {
                        addStyle(
                            SpanStyle(color = accentColor, fontWeight = FontWeight.ExtraBold),
                            lineStart,
                            (lineStart + 2).coerceAtMost(lineEnd)
                        )
                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic, color = mutedColor),
                            (lineStart + 2).coerceAtMost(lineEnd),
                            lineEnd
                        )
                    }
                }
                lineStart = lineEnd + 1
            }

            // 2. Inline formatting spans
            // Bold: **text**
            Regex("""\*\*(.+?)\*\*""").findAll(raw).forEach { m ->
                addStyle(markerStyle, m.range.first, m.range.first + 2)
                addStyle(SpanStyle(fontWeight = FontWeight.ExtraBold), m.range.first + 2, m.range.last - 1)
                addStyle(markerStyle, m.range.last - 1, m.range.last + 1)
            }
            // Underline: __text__
            Regex("""__(.+?)__""").findAll(raw).forEach { m ->
                addStyle(markerStyle, m.range.first, m.range.first + 2)
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), m.range.first + 2, m.range.last - 1)
                addStyle(markerStyle, m.range.last - 1, m.range.last + 1)
            }
            // Strikethrough: ~~text~~
            Regex("""~~(.+?)~~""").findAll(raw).forEach { m ->
                addStyle(markerStyle, m.range.first, m.range.first + 2)
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = mutedColor), m.range.first + 2, m.range.last - 1)
                addStyle(markerStyle, m.range.last - 1, m.range.last + 1)
            }
            // Italic: *text* (avoid matching inside **)
            Regex("""(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)""").findAll(raw).forEach { m ->
                addStyle(markerStyle, m.range.first, m.range.first + 1)
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), m.range.first + 1, m.range.last)
                addStyle(markerStyle, m.range.last, m.range.last + 1)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

/**
 * Parses stored rich-text Markdown into a clean display AnnotatedString with raw markers stripped
 * for Google Keep masonry cards and Read Mode.
 */
fun buildCleanRichTextAnnotatedString(
    raw: String,
    accentColor: Color,
    emeraldColor: Color,
    mutedColor: Color,
    textColor: Color
): AnnotatedString = buildAnnotatedString {
    val lines = raw.split("\n")
    lines.forEachIndexed { index, line ->
        if (index > 0) append("\n")

        when {
            line.startsWith("## ") -> {
                val start = length
                appendInlineFormatted(line.removePrefix("## "), mutedColor)
                addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor), start, length)
            }

            line.startsWith("# ") -> {
                val start = length
                appendInlineFormatted(line.removePrefix("# "), mutedColor)
                addStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = accentColor), start, length)
            }

            line.startsWith("[x] ") || line.startsWith("[X] ") -> {
                val iconStart = length
                append("☑ ")
                addStyle(SpanStyle(color = emeraldColor, fontWeight = FontWeight.Bold), iconStart, length)
                val bodyStart = length
                appendInlineFormatted(line.drop(4), mutedColor)
                addStyle(SpanStyle(color = mutedColor, textDecoration = TextDecoration.LineThrough), bodyStart, length)
            }

            line.startsWith("[ ] ") -> {
                val iconStart = length
                append("☐ ")
                addStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold), iconStart, length)
                appendInlineFormatted(line.drop(4), mutedColor)
            }

            line.startsWith("- ") -> {
                val dotStart = length
                append("• ")
                addStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.ExtraBold), dotStart, length)
                appendInlineFormatted(line.removePrefix("- "), mutedColor)
            }

            line.startsWith("> ") -> {
                val barStart = length
                append("▎ ")
                addStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.ExtraBold), barStart, length)
                val bodyStart = length
                appendInlineFormatted(line.removePrefix("> "), mutedColor)
                addStyle(SpanStyle(fontStyle = FontStyle.Italic, color = mutedColor), bodyStart, length)
            }

            else -> {
                appendInlineFormatted(line, mutedColor)
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineFormatted(text: String, mutedColor: Color) {
    val tokenRegex = Regex("""(\*\*(.+?)\*\*|__(.+?)__|~~(.+?)~~|(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*))""")
    var cursor = 0
    for (match in tokenRegex.findAll(text)) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        val start = length
        when {
            match.groups[2] != null -> {
                append(match.groups[2]!!.value)
                addStyle(SpanStyle(fontWeight = FontWeight.ExtraBold), start, length)
            }

            match.groups[3] != null -> {
                append(match.groups[3]!!.value)
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, length)
            }

            match.groups[4] != null -> {
                append(match.groups[4]!!.value)
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = mutedColor), start, length)
            }

            match.groups[5] != null -> {
                append(match.groups[5]!!.value)
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}

/**
 * Wraps the current selection with rich-text markers (e.g., **bold**, *italic*, __underline__, ~~strike~~),
 * or toggles them off if already wrapped.
 */
fun applyRichWrapFormat(
    value: TextFieldValue,
    marker: String,
    placeholder: String
): TextFieldValue {
    val text = value.text
    val start = value.selection.min.coerceIn(0, text.length)
    val end = value.selection.max.coerceIn(0, text.length)

    if (start == end) {
        val inserted = "$marker$placeholder$marker"
        val newText = text.substring(0, start) + inserted + text.substring(end)
        return TextFieldValue(
            text = newText,
            selection = TextRange(start + marker.length, start + marker.length + placeholder.length)
        )
    }

    val selected = text.substring(start, end)
    // Toggle off if selection itself is wrapped
    if (selected.startsWith(marker) && selected.endsWith(marker) && selected.length >= marker.length * 2) {
        val unwrapped = selected.removePrefix(marker).removeSuffix(marker)
        val newText = text.substring(0, start) + unwrapped + text.substring(end)
        return TextFieldValue(
            text = newText,
            selection = TextRange(start, start + unwrapped.length)
        )
    }

    val wrapped = "$marker$selected$marker"
    val newText = text.substring(0, start) + wrapped + text.substring(end)
    return TextFieldValue(
        text = newText,
        selection = TextRange(start + marker.length, start + marker.length + selected.length)
    )
}

/**
 * Toggles or inserts a line-level prefix (Heading `# `, Bullet `- `, Checklist `[ ] `, Quote `> `, Numbered `1. `)
 * on the line where the cursor currently sits.
 */
fun applyRichLinePrefix(
    value: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.start.coerceIn(0, text.length)
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
    val currentLine = text.substring(lineStart, lineEnd)

    val newLine = if (currentLine.startsWith(prefix)) {
        currentLine.removePrefix(prefix)
    } else {
        prefix + currentLine
    }
    val delta = newLine.length - currentLine.length
    val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
    val newCursor = (cursor + delta).coerceIn(0, newText.length)
    return TextFieldValue(text = newText, selection = TextRange(newCursor))
}
