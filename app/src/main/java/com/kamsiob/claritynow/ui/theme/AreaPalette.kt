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
     * The four palette entries the default walk never hands out, because each one is a
     * function color in design-v3.md 3.1 or 3.2 or that color's own hue.
     *
     * `#4DA3FF` is `actionBlue` in dark, `#22C55E` is `positiveGreen` and `#F59E0B` is
     * `warnAmber`, byte for byte. On a screen carrying any of them, the color that means
     * "this is your area" and the color that means "this is done" or "there is a Pulse
     * waiting" are the same pixel value, and an identity that is indistinguishable from
     * a status is not an identity.
     *
     * **`#2D7FF9` is the fourth, and the phase 13 contrast audit changed why rather than
     * whether.** It was `actionBlue` in light until that audit took the light token to
     * `#004BAE`, which is the same OKLab hue at a lower lightness: a lighter step of the
     * same blue rather than a different one. An area drawn in it sits beside a FAB and a
     * primary button that are that color brightened, which is the collision this list
     * exists to prevent, so it stays out of the walk on its hue rather than on its bytes.
     * Byte identity was never the point; it was the evidence.
     *
     * The audit found the first of these on the first run screen, where the shipped
     * walk gave area one `#2D7FF9` beside a FAB that was `#2D7FF9` at the time. It is
     * not a single collision. `#22C55E` is the first color of Meadow, so the shipped
     * walk also gave area five the completion color, and the walk reaches both inside
     * the first eight areas from any starting point. Moving where the walk starts cannot
     * fix that; naming what it may not hand out can.
     *
     * **This is not a locked subset.** design-v3.md 3.4 opens with "All 48 available to
     * everyone" and that is untouched: every one of these four is still in the picker
     * and still choosable. What changed is only what the app assigns on its own. A
     * color someone picks deliberately is a decision they made; a color the app hands
     * out is a decision the app made, and the app should not make this one.
     *
     * `deleteMuted`, `parchment` and every Contemplative accent in 3.3 are checked and
     * are not in the palette, so the list is these four.
     */
    private val RESERVED_BY_FUNCTION =
        listOf("#2D7FF9", "#4DA3FF", "#22C55E", "#F59E0B")

    /**
     * Berry, index 2, where the default walk begins. design-v3.md 3.4.
     *
     * 3.4 asks the walk for one thing, "so the first four are distinct without the user
     * choosing", and the shipped start did not deliver it: Ocean `#2D7FF9` at hue 216
     * and Twilight `#6366F1` at 239 are 23 degrees apart, which is the narrowest step
     * in the whole first four and is two blues in a row. Starting at Berry, the first
     * four are `#D946EF` 292, `#EF4444` 0, `#16A34A` 142 and `#CA8A04` 41, whose
     * narrowest step is 68 degrees. That is the widest that any of the eight possible
     * starts produces, measured across all eight rather than assumed.
     *
     * It also answers the collision the audit named. `#D946EF` is 78 degrees from
     * `actionBlue`, which sits at hue 214 since phase 13, and in a different family
     * entirely, so on the first run screen, where the only two colored things are one
     * area and one FAB, they cannot be read as the same thing.
     *
     * **The obvious start is not this one.** The obvious move is one step along the
     * list, to Twilight, and Twilight's `#6366F1` is 23 degrees from `actionBlue`: an
     * indigo that reads as a shade of the button rather than as an identity. It would
     * also walk toward the family design-v3.md 15.1 names twice, "lavender or
     * indigo-to-purple gradients" and "a blue to purple gradient". Berry walks away
     * from it. design-v3.md 15.
     *
     * Earth at 41 and Stone at 33 sit further from `actionBlue` than Berry does, and
     * both were rejected on the same sentence they would satisfy halfway. Starting at
     * Earth puts `#CA8A04` and `#A68B6B` next to each other 8 degrees apart, two muted
     * yellows a person would have to compare rather than recognize; starting at Stone
     * takes the narrowest step to 22. Each buys distance from one button by giving up
     * the distinctness 3.4 asks the walk for in the first place.
     */
    private const val WALK_START_MOOD = 2

    /**
     * Each mood's colors with the function colors removed, which is what the walk reads
     * instead of reading the mood directly.
     *
     * Filtering the list rather than stepping over a reserved entry at assignment time
     * is deliberate: stepping would map two different area counts onto the same color,
     * so area three and area nineteen would both come out `#18BFFF`. Removing the entry
     * keeps the walk a bijection over each mood's remaining colors, which is the
     * property that makes "distinct without anyone choosing" true past the first eight.
     */
    private val walkShades: List<List<String>> = moods.map { mood ->
        mood.colors.filterNot { hex -> RESERVED_BY_FUNCTION.any { it.equalsHex(hex) } }
    }

    /**
     * Walks the mood groups in order from [WALK_START_MOOD], taking the first color of
     * each that is not one of the app's own function colors, so the first four areas a
     * person creates are distinct from each other and from every colored control on the
     * screen without anyone choosing. Wraps into each mood's next color once all eight
     * firsts are spent.
     */
    fun defaultColorForIndex(existingAreaCount: Int): String {
        val count = existingAreaCount.coerceAtLeast(0)
        val moodIndex = (WALK_START_MOOD + count) % moods.size
        val shades = walkShades[moodIndex]
        return shades[(count / moods.size) % shades.size]
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
 * area's own wash at the in-session opacity from design-v3.md 3.1 and 3.2. It also
 * covers the 11 percent peak of the promotion in 8.2 item 1, which is shallower.
 *
 * **There are two of these grounds per world, not one, and phase 3c is where that was
 * found.** design-v3.md 16.2 pins the calm wash shallower and desaturates the accent
 * under it, and the phase 3b reading of that was "shallower and less colored is an
 * easier ground, so a variant that clears the ordinary one clears the calm one too".
 * In light that is true. **In dark it is false**: a desaturated accent at 15 percent
 * over the card is not uniformly lighter than the true accent at 16, and fourteen of
 * the 48 dark labels measure slightly worse in calm mode than out of it. It survived phase
 * 3b on 0.04 of margin, and 0.04 is not a margin. Lifting the dark card in phase 3c
 * spent it: `#E11D48`, `#BE185D` and `#DC2626` went to 4.48, 4.42 and 4.48 to one.
 *
 * So the variant is verified against **both**, and is the same color in both, which is
 * what 16.2 requires of it. This is the same class of correction phase 3b made when it
 * stopped verifying against the bare card: the label is measured on every ground it is
 * actually drawn on, and calm mode is one of them.
 */
private const val LABEL_GROUND_ALPHA_LIGHT = 0.13f
private const val LABEL_GROUND_ALPHA_DARK = 0.16f
private const val LABEL_GROUND_ALPHA_LIGHT_CALM = 0.12f
private const val LABEL_GROUND_ALPHA_DARK_CALM = 0.15f

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
 * 48 colors clears at 4.54:1 and looks fine; measured against the card as drawn, the
 * same label on an in-session area is `#E11D48` at **3.71:1**, well under the floor in
 * design-v3.md 13. It is the same class of mistake phase 3 found in the Trail's mint
 * completed row, where a wash was composited over the wrong ground and cost 0.1 of a
 * contrast ratio; here it costs 0.8.
 *
 * It surfaced during the calm mode audit, issue #48, because calm mode's transform has
 * to be measured on the ground it lands on and measuring it exposed that nothing else
 * ever had been.
 *
 * **Every ground is recomputed at runtime from `colors`, which is why the token change
 * in phase 3c did not have to be applied here by hand.** Moving `card` and lifting the
 * dark card changed eight of the 96 variants and the function found them on its own:
 * three in light, five in dark. The numbers that do not adapt are the ones written
 * down, so they are the ones the tests hold. With both grounds verified the worst case
 * is 4.538:1 in light and 4.655:1 in dark, on every one of the 48 colors, in ordinary
 * and in calm mode, on every wash opacity the design permits. `CalmModeContrastTest`
 * holds all of it.
 */
fun areaLabelColor(accent: Color, colors: ClarityColors): Color {
    val grounds = labelGrounds(accent, colors)
    if (colors.isDark) {
        val lightened = accent.blendWith(Color.White, 0.30f)
        if (lightened.clears(grounds)) return lightened
        return accent.forceContrast(grounds, Color.White)
    }
    if (accent.clears(grounds)) return accent
    val darkened = accent.blendWith(Color.Black, 0.25f)
    if (darkened.clears(grounds)) return darkened
    return accent.forceContrast(grounds, Color.Black)
}

/**
 * The card as drawn under this label, ordinary and calm. Both alphas are read from the
 * constants above rather than from [colors], so the variant is identical whether the
 * caller holds an ordinary or a calmed token set, which is what design-v3.md 16.2 means
 * by excluding the label from the transform.
 */
private fun labelGrounds(accent: Color, colors: ClarityColors): List<Color> {
    val card = colors.card
    return if (colors.isDark) {
        listOf(
            accent.copy(alpha = LABEL_GROUND_ALPHA_DARK).compositeOver(card),
            accent.calmed(true).copy(alpha = LABEL_GROUND_ALPHA_DARK_CALM).compositeOver(card),
        )
    } else {
        listOf(
            accent.copy(alpha = LABEL_GROUND_ALPHA_LIGHT).compositeOver(card),
            accent.calmed(true).copy(alpha = LABEL_GROUND_ALPHA_LIGHT_CALM).compositeOver(card),
        )
    }
}

private fun Color.clears(grounds: List<Color>): Boolean =
    grounds.all { contrastRatio(this, it) >= 4.5 }

/**
 * The check on a selected swatch, design-v3.md 10.9 stage two and onboarding's color
 * rows, in white or in ink depending on which one reads on that swatch.
 *
 * **A white check is what 10.9 asked for and it fails on 17 of the 48 colors**, worst on
 * `#FBBF24` at 1.67 to one against design-v3.md 13's floor of 3.0 for a graphic. A swatch
 * is the accent at full strength, which is the only place in the app where an area color
 * is a large solid field, so half the palette is too light to hold white and the other
 * half is too dark to hold ink. Neither constant works and the swatch decides.
 *
 * The worst of the 48 measures 4.23 to one on whichever of the two it picks, which is
 * well clear of a floor of 3.0, and the margin is the reason the rule is "whichever reads
 * better" rather than "white unless it fails": the second would leave a swatch sitting on
 * 3.05 with a white check and nothing between it and the floor.
 *
 * **The obvious answers are a white check with a drop shadow, or a white check with a
 * dark outline, and both are refused.** design-v3.md 6.1 gives an element exactly one
 * separation device and a glyph that carries a shadow to be legible has borrowed a second
 * one; an outline on a check is the same move with a hairline. Choosing the ink per
 * swatch is also what this file already does for the area label in 3.4, so it is the
 * mechanism the design has rather than a new one. design-v3.md 15.
 *
 * Nobody sees the two inks side by side: 10.9's grid has one selected swatch at a time.
 */
fun swatchCheckColor(accent: Color): Color =
    if (contrastRatio(Color.White, accent) >= contrastRatio(InkLight, accent)) {
        Color.White
    } else {
        InkLight
    }

/** Blends further toward [toward] in 5 percent steps until 4.5:1 is met or the blend is spent. */
private fun Color.forceContrast(against: List<Color>, toward: Color): Color {
    var amount = 0.30f
    while (amount <= 0.95f) {
        val candidate = blendWith(toward, amount)
        if (candidate.clears(against)) return candidate
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
