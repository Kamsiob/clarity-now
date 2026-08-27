package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
 * The deepest ground an area label can ever sit on, per world: the card carrying that
 * area's own wash at the in-session opacity from design-v3.md 3.1 and 3.2.
 *
 * These are the **ordinary** opacities, not calm mode's. design-v3.md 16.2 leaves the
 * label unchanged in calm mode and pins the wash under it shallower, so a variant that
 * clears the floor here clears it there too, and the label does not change color when
 * the switch is thrown. It also covers the 11 percent peak of the promotion in 8.2
 * item 1, which is shallower than both.
 */
private const val LABEL_GROUND_ALPHA_LIGHT = 0.13f
private const val LABEL_GROUND_ALPHA_DARK = 0.16f

/**
 * Area label text uses the accent at full strength. design-v3.md 3.4 requires 4.5:1,
 * and specifies the only permitted remedy: darken the label variant by blending 25
 * percent black in light mode, lighten it by blending 30 percent white in dark mode.
 * The dot and the wash are never adjusted.
 *
 * **The ratio is measured against the wash, not against the bare card**, and that is a
 * defect this function shipped with. design-v3.md 3.4 says "verify 4.5:1 against the
 * card", and the card a label actually sits on carries the area's own accent at up to
 * 13 percent in light and 16 in dark. Measured against `colors.card` the worst of the
 * 48 colors clears at 4.58:1 and looks fine; measured against the card as drawn, the
 * same label on an in-session area is `#E11D48` at **3.83:1**, well under the floor in
 * design-v3.md 13, and 3.95:1 at the peak of a promotion. It is the same class of
 * mistake phase 3 found in the Trail's mint completed row, where a wash was composited
 * over the wrong ground and cost 0.1 of a contrast ratio; here it costs 0.75.
 *
 * It surfaced during the calm mode audit, issue #48, because calm mode's transform has
 * to be measured on the ground it lands on and measuring it exposed that nothing else
 * ever had been. Calm mode is not the cause and does not fix it: with the transform
 * applied the same label reads 4.41:1, better and still failing.
 *
 * With the ground corrected the worst case is 4.55:1 in light and 4.56:1 in dark, on
 * every one of the 48 colors, in ordinary and in calm mode, on every wash opacity the
 * design permits. Twenty three of the 48 light labels move as a result and five of the
 * dark ones. `CalmModeContrastTest` holds all of it.
 */
fun areaLabelColor(accent: Color, colors: ClarityColors): Color {
    val groundAlpha = if (colors.isDark) LABEL_GROUND_ALPHA_DARK else LABEL_GROUND_ALPHA_LIGHT
    val ground = accent.copy(alpha = groundAlpha).compositeOver(colors.card)
    if (colors.isDark) {
        val lightened = accent.blendWith(Color.White, 0.30f)
        return if (contrastRatio(lightened, ground) >= 4.5) lightened else accent.forceContrast(ground, Color.White)
    }
    if (contrastRatio(accent, ground) >= 4.5) return accent
    val darkened = accent.blendWith(Color.Black, 0.25f)
    return if (contrastRatio(darkened, ground) >= 4.5) darkened else accent.forceContrast(ground, Color.Black)
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
