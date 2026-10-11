package com.buvettefacile

import androidx.compose.ui.geometry.Offset
import java.util.Locale

data class PriceBox(
    val id: Long,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val cents: Int,
    val label: String,
) {
    fun contains(p: Offset) = p.x in left..right && p.y in top..bottom

    fun movedBy(delta: Offset): PriceBox {
        val w = right - left
        val h = bottom - top
        val l = (left + delta.x).coerceIn(0f, 1f - w)
        val t = (top + delta.y).coerceIn(0f, 1f - h)
        return copy(left = l, top = t, right = l + w, bottom = t + h)
    }

    fun displayText() = if (label.isBlank()) formatEuro(cents) else "$label\n${formatEuro(cents)}"
}

fun formatEuro(cents: Int): String = String.format(Locale.FRANCE, "%d,%02d €", cents / 100, cents % 100)

fun parseCents(text: String): Int? {
    val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
    if (value < 0 || value > 100_000) return null
    return Math.round(value * 100).toInt()
}
