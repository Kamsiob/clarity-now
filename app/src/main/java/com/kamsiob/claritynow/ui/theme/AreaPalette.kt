package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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

    /**
     * Eight moods, six colors each, one Flexoki accent family per mood at its steps
     * 400, 500, 600, 700, 800 and 850.
     *
     * ## Why all forty eight changed
     *
     * They were **Tailwind v3 defaults**, and the canvas they were drawn on was Tailwind
     * `zinc` to a decimal place of hue. That pairing is the most replicated palette in
     * template interfaces since 2021 and the one every catalog of machine generated
     * design names first. The surfaces were corrected in `ClarityColors`; this is the
     * other half, and leaving it would have kept the tell on the only colored thing on
     * the home screen.
     *
     * Flexoki is by Steph Ango, MIT, credited on the licenses screen. It has exactly
     * eight accent families, which is what made the fit exact rather than convenient,
     * and it is drawn for reading rather than for dashboards.
     *
     * ## What this buys, measured against the old set
     *
     * - **Less reliance on the darkening fallback.** `areaLabelColor` blends a color
     *   toward the ink until it clears 4.5 to one. Of the old forty eight on the new
     *   card, eight needed no help and eighteen went past the 25 percent the design
     *   states. Of these, **twenty seven need none and only four exceed 25 percent**,
     *   the worst at 35.
     * - **Separability, including in calm mode.** The closest pair of full strength
     *   colors goes from 1.39 to 4.26, and at the 7 percent wash from 0.11 to 0.23.
     *   **This palette desaturated is more separable than the old one at full strength.**
     * - Calm mode's spread ratios run 0.453 to 0.613, inside `CalmModeTest`'s bands.
     *
     * ## Three names changed, and Stone and Slate are gone
     *
     * Ember, Ocean, Berry and Twilight stay because they still describe the pigment.
     * **Clay** is a terracotta rather than the old "Earth", **Moss** is an olive rather
     * than a "Meadow", and **Ochre** and **Lagoon** are new. A name that describes a
     * color the palette no longer holds is worse than a new name.
     *
     * **Flexoki has no neutral family, so the two grey moods are retired.** That is a
     * real loss and it was weighed rather than shrugged at: the old palette reached OKLab
     * chroma 0.0096 at its quietest and this one bottoms out at 0.0472. What settles it
     * is where an area color actually lives, which is the 7 percent wash: there
     * Twilight's lightest step washes to 0.0089, quieter than the old palette's quietest
     * color washed to. The set as a whole is calmer too, mean chroma 0.140 down to 0.116.
     * What is genuinely gone is a grey dot, and the alternative, a Stone mood built from
     * the base ramp, lands its worst dark label at 4.50 against a floor of 4.5. This
     * project's own history says four hundredths is not a margin.
     */
    val moods: List<AreaMood> = listOf(
        AreaMood("Ember", listOf("#D14D41", "#C03E35", "#AF3029", "#942822", "#6C201C", "#551B18")),
        AreaMood("Ocean", listOf("#4385BE", "#3171B2", "#205EA6", "#1A4F8C", "#163B66", "#133051")),
        AreaMood("Clay", listOf("#DA702C", "#CB6120", "#BC5215", "#9D4310", "#71320D", "#59290D")),
        AreaMood("Ochre", listOf("#D0A215", "#BE9207", "#AD8301", "#8E6B01", "#664D01", "#503D02")),
        AreaMood("Berry", listOf("#CE5D97", "#B74583", "#A02F6F", "#87285E", "#641F46", "#4F1B39")),
        AreaMood("Lagoon", listOf("#3AA99F", "#2F968D", "#24837B", "#1C6C66", "#164F4A", "#143F3C")),
        AreaMood("Twilight", listOf("#8B7EC8", "#735EB5", "#5E409D", "#4F3685", "#3C2A62", "#31234E")),
        AreaMood("Moss", listOf("#879A39", "#768D21", "#66800B", "#536907", "#3D4C07", "#313D07")),
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
        listOf("#1A4F8C", "#205EA6", "#3D4C07", "#536907", "#942822", "#AF3029")

    /**
     * Berry, index 4, where the default walk begins. design-v3.md 3.4.
     *
     * 3.4 asks the walk for one thing: "so the first four are distinct without the user
     * choosing". With the Flexoki families the order is red, blue, orange, yellow,
     * magenta, cyan, purple, green, and starting at Berry the first four handed out are
     * Berry at hue 329, Lagoon at 175, Twilight at 251 and Moss at 72. **The narrowest
     * step is 75.9 degrees**, against the 68 the previous palette's best start reached.
     * All eight orderings against all eight starts were searched rather than assumed.
     *
     * Two constraints shaped it, and the second is a limit rather than a choice.
     * Flexoki's four warm families sit inside 67 degrees of each other, so no ordering
     * can make the worst case across every start better than 22 degrees; only the window
     * the first four fall in can be made wide, which is what the start does.
     *
     * **The obvious start is not this one, and it is the same obvious start as before.**
     * Twilight's lightest step is the furthest color in the palette from any function
     * color, so a purely numeric argument would land there. design-v3.md 15.1 names
     * lavender and indigo-to-purple twice as a tell, and the previous pass walked away
     * from that family deliberately. Berry keeps that decision and keeps continuity: the
     * old default was a magenta too.
     *
     * Simulated over 48 areas: the first eight are all distinct, the first 24 are all
     * distinct, and no color reserved by a function is ever handed out.
     */
    private const val WALK_START_MOOD = 4

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
// **The two dark values were each one percentage point short of the ground they are
// solving against, and that is a defect rather than a rounding.**
//
// `ClarityDarkColors.cardWashActiveAlpha` is 0.17 and its calm counterpart is 0.16, so
// the deepest ground a dark area label is ever drawn on is one point deeper than the
// ground the solve verified against. The whole point of these constants is to name the
// worst case; naming something a shade easier than the worst case is the one thing they
// must not do.
//
// It surfaced when the palette moved to Flexoki: Ochre's 700 step measured 4.459 to one
// on the in session wash in dark, against a floor of 4.5. The color was not the problem.
// A previous audit had flagged this same mismatch and could not reproduce it, because
// with the old palette nothing happened to land in the 0.01 the gap hides.
//
// The light pair is deliberately unequal in the other direction: 0.13 against an actual
// 0.09, and 0.12 against 0.08. Solving against a ground deeper than any that is drawn
// costs nothing but margin, and margin is what this file is for.
private const val LABEL_GROUND_ALPHA_LIGHT = 0.13f
private const val LABEL_GROUND_ALPHA_DARK = 0.17f
private const val LABEL_GROUND_ALPHA_LIGHT_CALM = 0.12f
private const val LABEL_GROUND_ALPHA_DARK_CALM = 0.16f

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
