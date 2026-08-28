package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.theme.*

object MinecraftColorParser {

    private val COLOR_MAP = mapOf(
        '0' to McBlack,
        '1' to McDarkBlue,
        '2' to McDarkGreen,
        '3' to McDarkAqua,
        '4' to McDarkRed,
        '5' to McDarkPurple,
        '6' to McGold,
        '7' to McGray,
        '8' to McDarkGray,
        '9' to McBlue,
        'a' to McGreen,
        'b' to McAqua,
        'c' to McRed,
        'd' to McLightPurple,
        'e' to McYellow,
        'f' to McWhite
    )

    fun parse(input: String, defaultColor: Color = TextWhite): AnnotatedString {
        if (input.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            var currentColor: Color = defaultColor
            var isBold = false
            var isItalic = false
            var isUnderline = false
            var isStrikethrough = false

            var i = 0
            val len = input.length

            while (i < len) {
                val c = input[i]
                if ((c == '§' || c == '&') && i + 1 < len) {
                    val code = input[i + 1].lowercaseChar()
                    when {
                        COLOR_MAP.containsKey(code) -> {
                            currentColor = COLOR_MAP[code] ?: defaultColor
                            // Color codes in Minecraft reset formatting styles
                            isBold = false
                            isItalic = false
                            isUnderline = false
                            isStrikethrough = false
                            i += 2
                            continue
                        }
                        code == 'l' -> {
                            isBold = true
                            i += 2
                            continue
                        }
                        code == 'o' -> {
                            isItalic = true
                            i += 2
                            continue
                        }
                        code == 'n' -> {
                            isUnderline = true
                            i += 2
                            continue
                        }
                        code == 'm' -> {
                            isStrikethrough = true
                            i += 2
                            continue
                        }
                        code == 'r' -> {
                            currentColor = defaultColor
                            isBold = false
                            isItalic = false
                            isUnderline = false
                            isStrikethrough = false
                            i += 2
                            continue
                        }
                        code == 'k' -> { // Obfuscated
                            i += 2
                            continue
                        }
                    }
                }

                // Determine decorations
                val decorations = when {
                    isUnderline && isStrikethrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    isUnderline -> TextDecoration.Underline
                    isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }

                val spanStyle = SpanStyle(
                    color = currentColor,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = decorations
                )

                val start = length
                append(c)
                addStyle(spanStyle, start, length)
                i++
            }
        }
    }

    fun stripCodes(input: String): String {
        return input.replace(Regex("[§&][0-9a-fk-orA-FK-OR]"), "")
    }
}
