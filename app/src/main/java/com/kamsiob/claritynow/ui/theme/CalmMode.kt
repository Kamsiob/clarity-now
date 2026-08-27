package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * design-v3.md 16.2. Chroma is multiplied by this, holding lightness.
 *
 * A number rather than a range, so the audit table in 16.7 can state what happens to
 * every token as a figure and no later session has to guess what "less saturated"
 * meant.
 */
const val CALM_CHROMA_SCALE = 0.6f

/**
 * True when the app should render calm. Set once at the top of the tree by
 * [ClarityTheme] and [ContemplativeTheme], never computed by a screen.
 *
 * design-v3.md 16: **one switch, not a spectrum.** There is no per-screen and no
 * per-element calm flag, and nothing below the theme may decide to be calmer than the
 * rest of the app.
 */
val LocalCalmMode = compositionLocalOf { false }

/**
 * The switch's value, resolved from what the user chose and what the system asks for.
 * design-v3.md 16.1.
 *
 * [stored] is null while the user has never touched the switch, and while it is null
 * calm mode follows the system reduce-motion setting live. The first explicit choice
 * gives the switch a value of its own and it stops following.
 *
 * Deliberately a pure function of two inputs so that both halves of the default can be
 * asserted in a unit test without a device, which is what issue #48 asks for.
 */
fun resolveCalmMode(stored: Boolean?, systemReduceMotion: Boolean): Boolean =
    stored ?: systemReduceMotion

/**
 * Calm mode's color half, the one transform in one place. design-v3.md 16.2,
 * MASTER_BUILD_PROMPT 14b.12, Addendum 01 item 8c.
 *
 * Material 3 Expressive is this app's motion model and it is the right direction for
 * this product. It is also the wrong thing to impose on a person for whom movement and
 * saturation are expensive. Both are true at once, which is what calm mode is for:
 * **ship the expressive direction, and ship the exit.**
 *
 * Two halves, and this file holds the color half. The motion half is already one
 * global flag in `ClarityMotion.kt`, which calm mode joins rather than duplicates,
 * per design-v3.md 8.5.
 *
 * **Three properties of this transform are load bearing.**
 *
 * 1. **Chroma is multiplied, lightness is held.** design-v3.md 16.2 rejects the
 *    obvious implementation, blending toward grey, by name: blending lightens dark
 *    colors and darkens light ones, which moves every contrast ratio design-v3.md 13
 *    has already verified per area color. Holding lightness means calm mode cannot
 *    break a measurement that passed. The residual movement is real but tiny: across
 *    the 48 area colors the largest change in WCAG relative luminance is 0.018, and
 *    every ratio measured in `CalmModeContrastTest` clears design-v3.md 13's floor.
 * 2. **The space is OKLab, not HSL.** HSL saturation is not perceptual: dropping it by
 *    40 percent takes `#F59E0B` somewhere quite different from where it takes
 *    `#334155`, and the two would stop looking like the same treatment. OKLab's `a`
 *    and `b` axes carry chroma and hue together, so scaling both by one factor is
 *    exactly "less colorful, same lightness, same hue".
 * 3. **The exclusions are closed and named.** design-v3.md 16.2 excludes the 7dp area
 *    dot and the area label text, because they are how an area is recognized and the
 *    two places contrast was measured, plus `actionBlue`, `positiveGreen`, `warnAmber`
 *    and `deleteMuted`, because design-v3.md 3.1 scopes each of those to exactly one
 *    job. Everything else that takes an accent takes it through here. The list does not
 *    grow by argument at a call site; it grows by an edit to 16.2.
 *
 * The transform is deliberately not applied inside [parseAreaColor], which is where a
 * reader looking for one place would expect it. Two of the six exclusions come through
 * that function, so a transform there would desaturate the dot and the label, which is
 * the one thing 16.2 forbids by name.
 *
 * Returns the color unchanged when [calm] is false, so a call site reads as "this
 * accent is atmosphere" rather than as a branch. Alpha is carried through untouched:
 * the wash's opacity is a separate token with its own calm value, in
 * [ClarityColors.calmed].
 */
fun Color.calmed(calm: Boolean): Color {
    if (!calm) return this
    val oklab = toOklab()
    return oklabToColor(
        lightness = oklab.lightness,
        a = oklab.a * CALM_CHROMA_SCALE,
        b = oklab.b * CALM_CHROMA_SCALE,
        alpha = alpha,
    )
}

/** The composable form, for a call site that has a theme but no flag in hand. */
@Composable
@ReadOnlyComposable
fun calmAccent(accent: Color): Color = accent.calmed(LocalCalmMode.current)

/**
 * design-v3.md 16.2's two pinned rows, expressed on the token set rather than at a
 * call site.
 *
 * `cardWash` and `cardWashActive` are stated in 3.1 and 3.2 as ranges, and calm mode
 * pins each to the low end of its own range: 5 and 12 percent in light, 7 and 15 in
 * dark. Every other token in section 3 is untouched, because calm mode is not a theme,
 * 16.4.
 *
 * The alpha pinning lives here and the chroma transform lives in [calmed] because they
 * act on different things. The alpha belongs to the surface and is a token; the chroma
 * belongs to the area's own color, which is user data and never a token.
 */
fun ClarityColors.calmed(): ClarityColors = copy(
    cardWashAlpha = if (isDark) 0.07f else 0.05f,
    cardWashActiveAlpha = if (isDark) 0.15f else 0.12f,
)

// ---------------------------------------------------------------------------
// OKLab. Bjorn Ottosson's matrices, transcribed rather than approximated.
//
// Kept private except for the two entry points above, so that the only way an accent
// reaches the screen desaturated is through the transform the design specifies.
// ---------------------------------------------------------------------------

/** The sRGB transfer function, decode. */
private fun decodeGamma(channel: Float): Float {
    val c = channel.toDouble()
    val linear = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    return linear.toFloat()
}

/** The sRGB transfer function, encode. */
private fun encodeGamma(linear: Float): Float {
    val c = linear.toDouble().coerceIn(0.0, 1.0)
    val encoded = if (c <= 0.0031308) 12.92 * c else 1.055 * c.pow(1.0 / 2.4) - 0.055
    return encoded.toFloat().coerceIn(0f, 1f)
}

/**
 * `Math.cbrt` rather than a signed `pow(1/3)`, because the three cone responses can go
 * very slightly negative on a saturated color and a fractional power of a negative
 * number is not a number.
 */
private fun cubeRoot(value: Float): Float = Math.cbrt(value.toDouble()).toFloat()

private data class Oklab(val lightness: Float, val a: Float, val b: Float)

private fun Color.toOklab(): Oklab {
    val r = decodeGamma(red)
    val g = decodeGamma(green)
    val b = decodeGamma(blue)

    val long = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b
    val medium = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b
    val short = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b

    val l = cubeRoot(long)
    val m = cubeRoot(medium)
    val s = cubeRoot(short)

    return Oklab(
        lightness = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s,
        a = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s,
        b = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s,
    )
}

private fun oklabToColor(lightness: Float, a: Float, b: Float, alpha: Float): Color {
    val l = lightness + 0.3963377774f * a + 0.2158037573f * b
    val m = lightness - 0.1055613458f * a - 0.0638541728f * b
    val s = lightness - 0.0894841775f * a - 1.2914855480f * b

    val long = l * l * l
    val medium = m * m * m
    val short = s * s * s

    // Reducing chroma at a fixed lightness moves toward the neutral axis, which is
    // inside the gamut wherever the starting color was, so the clamp in encodeGamma is
    // a guard against float drift rather than a real gamut mapping step.
    return Color(
        red = encodeGamma(4.0767416621f * long - 3.3077115913f * medium + 0.2309699292f * short),
        green = encodeGamma(-1.2684380046f * long + 2.6097574011f * medium - 0.3413193965f * short),
        blue = encodeGamma(-0.0041960863f * long - 0.7034186147f * medium + 1.7076147010f * short),
        alpha = alpha,
    )
}
