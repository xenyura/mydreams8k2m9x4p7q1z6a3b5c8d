package com.xenyura.mydreams

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class DreamTheme(
    val id: String,
    val name: String,
    val dark: Boolean,
    val bg: Color,
    val a: Color,
    val b: Color,
    val text: Color,
    val muted: Color,
    val card: Color,
    val btn: Color,
    val onBtn: Color,
    val hero: List<Color>
)

private fun c(h: Long) = Color(0xFF000000 or h)

private fun light(id: String, name: String, bg: Long, a: Long, b: Long) = DreamTheme(
    id, name, false, c(bg), c(a), c(b),
    text = c(0x1E293B), muted = c(0x64748B), card = Color.White.copy(alpha = 0.72f),
    btn = c(a), onBtn = c(0x1E293B), hero = listOf(c(a), c(b))
)

val Themes = listOf(
    light("pastel-sky", "Pastel Sky", 0xF0F9FF, 0x7DD3FC, 0xFDE68A),
    light("cotton-cloud", "Cotton Cloud", 0xFDF2F8, 0xC4B5FD, 0xFBCFE8),
    light("peach-fuzz", "Peach Fuzz", 0xFFF7ED, 0xFDBA74, 0xFBCFE8),
    light("mint-dream", "Mint Dream", 0xF0FDF4, 0x86EFAC, 0xC4B5FD),
    DreamTheme(
        "midnight-dream", "Midnight Dream", true, c(0x0B1026), c(0x3B2F7F), c(0xB8A9FF),
        text = c(0xEDE9FF), muted = c(0xA79FD6), card = c(0xB8A9FF).copy(alpha = 0.10f),
        btn = c(0xB8A9FF), onBtn = c(0x0B1026), hero = listOf(c(0x3B2F7F), c(0x0B1026))
    ),
    DreamTheme(
        "moonlight", "Moonlight", true, c(0x0F172A), c(0x94A3B8), c(0xF8FAFC),
        text = c(0xF8FAFC), muted = c(0x94A3B8), card = c(0x94A3B8).copy(alpha = 0.12f),
        btn = c(0x94A3B8), onBtn = c(0x0F172A), hero = listOf(c(0x94A3B8).copy(alpha = 0.4f), c(0x0F172A))
    )
)

fun themeById(id: String) = Themes.firstOrNull { it.id == id } ?: Themes[0]

val LocalDT = staticCompositionLocalOf { Themes[0] }

@Composable
fun MyDreamsTheme(t: DreamTheme, content: @Composable () -> Unit) {
    val scheme = if (t.dark) {
        darkColorScheme(primary = t.btn, background = t.bg, surface = t.bg, onSurface = t.text)
    } else {
        lightColorScheme(primary = t.btn, background = t.bg, surface = t.bg, onSurface = t.text)
    }
    CompositionLocalProvider(LocalDT provides t) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
