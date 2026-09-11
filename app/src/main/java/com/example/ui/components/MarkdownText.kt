package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val sections = parseMarkdownSections(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { section ->
            when (section) {
                is MarkdownSection.CodeBlock -> {
                    CodeBlockCard(
                        code = section.code,
                        language = section.language,
                        onCopy = {
                            copyToClipboard(context, section.code, "تم نسخ الكود")
                        }
                    )
                }
                is MarkdownSection.TextContent -> {
                    val annotatedString = formatRichText(section.text, textColor)
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private sealed class MarkdownSection {
    data class CodeBlock(val code: String, val language: String) : MarkdownSection()
    data class TextContent(val text: String) : MarkdownSection()
}

private fun parseMarkdownSections(raw: String): List<MarkdownSection> {
    val list = mutableListOf<MarkdownSection>()
    val lines = raw.split("\n")

    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()
    val textBuilder = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                // Closing code block
                list.add(MarkdownSection.CodeBlock(codeBuilder.toString().trimEnd(), codeLang))
                codeBuilder.clear()
                codeLang = ""
                inCodeBlock = false
            } else {
                // Flush text
                if (textBuilder.isNotEmpty()) {
                    list.add(MarkdownSection.TextContent(textBuilder.toString().trimEnd()))
                    textBuilder.clear()
                }
                inCodeBlock = true
                codeLang = line.trim().removePrefix("```").trim()
            }
        } else {
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n")
            } else {
                textBuilder.append(line).append("\n")
            }
        }
    }

    if (inCodeBlock) {
        list.add(MarkdownSection.CodeBlock(codeBuilder.toString().trimEnd(), codeLang))
    } else if (textBuilder.isNotEmpty()) {
        list.add(MarkdownSection.TextContent(textBuilder.toString().trimEnd()))
    }

    return list
}

@Composable
private fun CodeBlockCard(
    code: String,
    language: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131A26))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B2434))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (language.isNotBlank()) language.uppercase() else "CODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )
        }
    }
}

private fun formatRichText(text: String, defaultColor: Color) = buildAnnotatedString {
    val regex = Regex("(\\*\\*([^*]+)\\*\\*)|(`([^`]+)`)")
    var lastIndex = 0

    regex.findAll(text).forEach { match ->
        val range = match.range
        if (range.first > lastIndex) {
            withStyle(SpanStyle(color = defaultColor)) {
                append(text.substring(lastIndex, range.first))
            }
        }

        if (match.value.startsWith("**") && match.value.endsWith("**")) {
            val boldContent = match.value.removeSurrounding("**")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                append(boldContent)
            }
        } else if (match.value.startsWith("`") && match.value.endsWith("`")) {
            val inlineCode = match.value.removeSurrounding("`")
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8),
                    background = Color(0x3338BDF8)
                )
            ) {
                append(" $inlineCode ")
            }
        }
        lastIndex = range.last + 1
    }

    if (lastIndex < text.length) {
        withStyle(SpanStyle(color = defaultColor)) {
            append(text.substring(lastIndex))
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
