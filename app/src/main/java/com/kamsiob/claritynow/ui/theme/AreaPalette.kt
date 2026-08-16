package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.kamsiob.claritynow.domain.engine.StableHash
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * The 48 area colors in 8 mood groups, design-v3.md 3.4. All 48 are available to
 * everyone. There is no locked subset, because there is nothing to unlock.
 *
 * Color reaches the screen in exactly four forms: a 7dp dot, a 5 to 14 percent
 * wash, a 60 percent tile in Momentum, and the area label text. Never a stripe,
 * bar, edge, border or filled block.
 */
@Immutable
data class AreaMood(val name: String, val colors: List<String>)

object AreaPalette {

    val moods: List<AreaMood> = listOf(
        AreaMood("Ocean", listOf("#2D7FF9", "#4DA3FF", "#18BFFF", "#1B6ACB", "#3B82F6", "#06B6D4")),
        AreaMood("Twilight", listOf("#6366F1", "#4F46E5", "#7C3AED", "#8B5CF6", "#A855F7", "#C084FC")),
        AreaMood("Berry", listOf("#D946EF", "#EC4899", "#F472B6", "#E11D48", "#BE185D", "#DC2626")),
        AreaMood("Ember", listOf("#EF4444", "#F97316", "#FB923C", "#F59E0B", "#FBBF24", "#EAB308")),
        AreaMood("Meadow", listOf("#22C55E", "#16A34A", "#4ADE80", "#10B981", "#059669", "#14B8A6")),
        AreaMood("Earth", listOf("#CA8A04", "#92400E", "#A16207", "#B45309", "#D97706", "#8B7355")),
        AreaMood("Stone", listOf("#A68B6B", "#78716C", "#57534E", "#7F8C8D", "#95A5A6", "#9CA3AF")),
        AreaMood("Slate", listOf("#0D9488", "#64748B", "#475569", "#6B7280", "#334155", "#1E293B")),
    )

    val all: List<String> = moods.flatMap { it.colors }

    /**
     * Walks the mood groups in order taking the first color of each, so the first
     * four areas a person creates are distinct without anyone choosing. Wraps into
     * the second color of each mood once all eight firsts are spent.
     */
    fun defaultColorForIndex(existingAreaCount: Int): String {
        val moodIndex = existingAreaCount % moods.size
        val shadeIndex = (existingAreaCount / moods.size) % moods[moodIndex].colors.size
        return moods[moodIndex].colors[shadeIndex]
    }

    fun moodOf(hex: String): AreaMood? =
        moods.firstOrNull { mood -> mood.colors.any { it.equalsHex(hex) } }

    fun isKnown(hex: String): Boolean = all.any { it.equalsHex(hex) }

    private fun String.equalsHex(other: String) = equals(other, ignoreCase = true)
}

/** Parses `#RRGGBB`. Unknown or malformed input falls back to the first Ocean color. */
fun parseAreaColor(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6) return Color(0xFF2D7FF9)
    val value = cleaned.toLongOrNull(16) ?: return Color(0xFF2D7FF9)
    return Color(0xFF000000L or value)
}

/**
 * The corner an area's wash pools toward, chosen by hashing the area id so it is
 * stable for the life of the area and varied across a screen of cards.
 */
enum class WashCorner { TOP_START, TOP_END, BOTTOM_START, BOTTOM_END }

fun washCornerFor(areaId: String): WashCorner =
    WashCorner.entries[StableHash.bucket(areaId, WashCorner.entries.size)]

/**
 * Area label text uses the accent at full strength. design-v3.md 3.4 requires
 * 4.5:1 against the card, and specifies the only permitted remedy: darken the
 * label variant by blending 25 percent black in light mode, lighten it by blending
 * 30 percent white in dark mode. The dot and the wash are never adjusted.
 */
fun areaLabelColor(accent: Color, colors: ClarityColors): Color {
    if (colors.isDark) {
        val lightened = accent.blendWith(Color.White, 0.30f)
        return if (contrastRatio(lightened, colors.card) >= 4.5) lightened else accent.forceContrast(colors.card, Color.White)
    }
    if (contrastRatio(accent, colors.card) >= 4.5) return accent
    val darkened = accent.blendWith(Color.Black, 0.25f)
    return if (contrastRatio(darkened, colors.card) >= 4.5) darkened else accent.forceContrast(colors.card, Color.Black)
}

/** Blends further toward [toward] in 5 percent steps until 4.5:1 is met or the blend is spent. */
private fun Color.forceContrast(against: Color, toward: Color): Color {
    var amount = 0.30f
    while (amount <= 0.95f) {
        val candidate = blendWith(toward, amount)
        if (contrastRatio(candidate, against) >= 4.5) return candidate
        amount += 0.05f
    }
    return blendWith(toward, 0.95f)
}

fun Color.blendWith(other: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = red + (other.red - red) * t,
        green = green + (other.green - green) * t,
        blue = blue + (other.blue - blue) * t,
        alpha = alpha,
    )
}

/** WCAG 2.1 relative luminance. */
fun relativeLuminance(color: Color): Double {
    fun channel(value: Float): Double {
        val c = value.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
}

/** WCAG 2.1 contrast ratio. Both colors must be opaque. */
fun contrastRatio(foreground: Color, background: Color): Double {
    val a = relativeLuminance(foreground) + 0.05
    val b = relativeLuminance(background) + 0.05
    return max(a, b) / min(a, b)
}
