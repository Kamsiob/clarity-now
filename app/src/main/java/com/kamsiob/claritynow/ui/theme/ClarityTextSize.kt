package com.kamsiob.claritynow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType

/**
 * The in app text size control, design-v3.md 13 and Addendum 01 8f.
 *
 * ## It multiplies the OS font scale. It does not override it
 *
 * The two behaviors are different products and the choice is recorded in design-v3.md
 * 13 with the same reasoning it is given here.
 *
 * Overriding gives a person one dial that means one thing, which is the tidier
 * interface, and it fails the person this feature exists for on the first screen they
 * see. Somebody who has set the phone to 200 percent has already said, once, that they
 * cannot read text at 100 percent. An app that overrides that opens at 100 percent
 * until they find a setting they have no reason to look for, and it opens that way
 * silently. There is no version of that trade worth taking.
 *
 * Multiplying keeps the system answer as the starting point and lets this app go
 * further, which is what 8f actually asks for: larger text in the one app somebody
 * reads prose in, without making every other app on the phone larger to get it.
 *
 * ## The combined scale is capped at 200 percent, and that is a measurement
 *
 * [MAX_COMBINED_FONT_SCALE] is not a round number picked for tidiness. Every clipping
 * analysis in this project is written against the 200 percent condition:
 * design-v3.md 13 states it, `ClarityTabBar` measures the floating bar against it and
 * concludes that one label plus four icons comes to roughly 290dp of the 314dp there
 * is, and 5.3 caps the timer numeral at 1.3x of it. A control able to exceed 200
 * percent would invalidate all three at once, quietly, on a device nobody has run yet.
 * So the app's steps spend the headroom below the cap and stop there.
 *
 * The cost is real and is stated rather than hidden: a person whose phone is already
 * at 200 percent has no headroom left, and the picker says so instead of moving a
 * selection that changes nothing. [SMALL] is why the control is still not inert for
 * them.
 *
 * ## The steps are Android's own
 *
 * 0.85, 1.0, 1.15, 1.3 and 1.5 are the values behind the platform's own font size
 * control, which is what design-v3.md 13 means by "the same steps on top of it". A
 * bespoke ladder would have been a second vocabulary for a quantity the phone already
 * has one for, and somebody comparing this app's Large with the phone's Large would
 * have found two different sizes wearing one word.
 *
 * The two platform steps above 1.5, 1.8 and 2.0, are deliberately not offered. Reaching
 * them through this control would mean a combined scale at the cap or clamped by it in
 * every case where the phone is not at 100 percent, so they would be two more rows that
 * usually do nothing. They remain reachable, at the phone's own setting, which is where
 * a person who needs them has already been.
 */
enum class ClarityTextSize(val scale: Float) {
    SMALL(0.85f),
    DEFAULT(1f),
    LARGE(1.15f),
    LARGER(1.3f),
    LARGEST(1.5f),
}

/**
 * The ceiling on system scale times app scale. design-v3.md 13.
 *
 * Nothing in this app renders text above this, whatever the two settings say
 * separately, because 200 percent is the largest condition anything here has been
 * measured against.
 */
const val MAX_COMBINED_FONT_SCALE = 2f

/**
 * The floor, and it is the platform's own smallest step rather than zero.
 *
 * A phone at 0.85 with the app at [ClarityTextSize.SMALL] would otherwise land at
 * 0.72, which is smaller than anything Android itself will render and smaller than any
 * size in design-v3.md 5.3 was drawn for. The app is allowed to be smaller than the
 * phone asked for, because that is a choice made inside this app about this app, and
 * it is not allowed to be smaller than the platform's own minimum.
 */
const val MIN_COMBINED_FONT_SCALE = 0.85f

/** The system font scale times the app's, held between the two bounds above. */
fun combinedFontScale(systemFontScale: Float, setting: ClarityTextSize): Float =
    (systemFontScale * setting.scale)
        .coerceIn(MIN_COMBINED_FONT_SCALE, MAX_COMBINED_FONT_SCALE)

/** True when [MAX_COMBINED_FONT_SCALE] is what is deciding the size, not the two settings. */
fun isClampedByCeiling(systemFontScale: Float, setting: ClarityTextSize): Boolean =
    systemFontScale * setting.scale > MAX_COMBINED_FONT_SCALE

/**
 * The stored text size, so anything under the theme can read the setting itself rather
 * than infer it from a density.
 */
val LocalClarityTextSize = staticCompositionLocalOf { ClarityTextSize.DEFAULT }

/**
 * The phone's own font scale, before this app multiplied it.
 *
 * `LocalDensity.current.fontScale` under the theme is the combined figure, which is the
 * right value for everything that lays text out and the wrong one for the single
 * question the picker has to answer: whether the phone alone has already used up the
 * headroom. Captured once, where the override happens, rather than recomputed by
 * dividing, which would carry the clamp back into the answer.
 */
val LocalSystemFontScale = staticCompositionLocalOf { 1f }

/**
 * A [Density] whose font scale is [factor] times [base]'s, with the platform's own sp
 * curve left intact.
 *
 * **This is why the app setting is not applied by constructing `Density(density,
 * scale)`.** Android 14 converts sp to px along a non linear curve, which grows small
 * text more than large text precisely so that a headline at 200 percent does not run
 * off the screen; `LocalDensity` at the root of an Activity carries a converter that
 * knows it. A plain `Density` does not, so replacing the root one with a plain one
 * would make every size in the app scale linearly, and design-v3.md 5.3's 40sp Report
 * headline would arrive at 80sp on a phone where the platform intended about 64sp. The
 * combined scale would then be doing the opposite of what the cap above is for.
 *
 * So the multiplication happens in sp space and the base density performs the
 * conversion: `(size * factor).toDp()` through the original density, whatever curve
 * that density implements. The reverse conversions divide by the same factor so that a
 * round trip is still a round trip.
 *
 * Only the sp conversions are overridden. Everything expressed in dp goes through
 * untouched, which is the whole point: a dp is a physical size, a touch target is a
 * finger, and neither of those changes because somebody chose larger text.
 */
private data class TextScaledDensity(
    private val base: Density,
    private val factor: Float,
) : Density {

    override val density: Float get() = base.density

    override val fontScale: Float get() = base.fontScale * factor

    override fun TextUnit.toDp(): Dp = with(base) { scaled(this@toDp).toDp() }

    override fun TextUnit.toPx(): Float = with(base) { scaled(this@toPx).toPx() }

    override fun Float.toSp(): TextUnit = with(base) { this@toSp.toSp() } / factor

    override fun Int.toSp(): TextUnit = with(base) { this@toSp.toSp() } / factor

    override fun Dp.toSp(): TextUnit = with(base) { this@toSp.toSp() } / factor

    /**
     * Em and unspecified units pass through untouched so the base density raises its
     * own error for them, rather than this class raising a different one a line earlier
     * and burying which conversion was actually asked for.
     */
    private fun scaled(unit: TextUnit): TextUnit =
        if (unit.type == TextUnitType.Sp) unit * factor else unit
}

/**
 * This density with the app's text size applied and the combined result held inside the
 * two bounds. Returns the receiver untouched when the setting changes nothing, so the
 * default case adds no object and no indirection.
 */
fun Density.withTextSize(setting: ClarityTextSize): Density {
    val combined = combinedFontScale(fontScale, setting)
    if (combined == fontScale) return this
    return TextScaledDensity(this, combined / fontScale)
}

/**
 * This density with its font scale held at or below [max], design-v3.md 5.3's cap on
 * the timer numeral.
 *
 * The cap is expressed as a ratio against the density it is given rather than as an
 * absolute font scale, for the same reason as above: an absolute figure would need a
 * plain [Density] to carry it and would throw the platform's sp curve away at the one
 * size in the app where the curve matters most.
 */
fun Density.cappedFontScale(max: Float): Density =
    if (fontScale <= max) this else TextScaledDensity(this, max / fontScale)

/**
 * How far the spacing grid opens at the current text size. Addendum 01 8f.
 *
 * ## Why spacing scales at all
 *
 * A size control that grows the type inside boxes that stay put makes an interface
 * less legible rather than more: the lines get longer and the air between them does
 * not, so the screen that was meant to become readable becomes a wall. 8f names that
 * as the risk and issue #51 repeats it. The rhythm between elements is part of the
 * type specification, not decoration around it, so it moves with the type.
 *
 * ## The three rules this expresses
 *
 * **It never goes below one.** design-v3.md 13 says the 4dp grid, the 28dp section
 * spacing and the 11dp card rhythm are minimums rather than targets to compress. So
 * [ClarityTextSize.SMALL], and a phone set below 100 percent, make the text smaller and
 * leave the air exactly where section 6 put it. Every dimension in this app is
 * therefore byte identical to before this control existed whenever the combined scale
 * is at or under 1.0, which is the default on the default phone.
 *
 * **It is linear above one.** A line box at twice the size next to a gap at twice the
 * size is the same page, read larger. Damping the gaps would tighten the rhythm exactly
 * where 8f wants it loosened.
 *
 * **It is read off the density rather than the setting.** The person's phone and the
 * person's app choice are one combined number by the time anything lays out, and the
 * grid answers to the size of the text actually being drawn. That also means the cap
 * applies here with nothing extra written: inside the focus ring, where the numeral is
 * held at 1.3x, the gap under it is held at 1.3x too.
 */
@Composable
fun spacingScale(): Float = spacingScaleFor(LocalDensity.current.fontScale)

/**
 * [spacingScale] as a function of a font scale, so the three rules above are a thing a
 * unit test can call rather than a thing a screenshot has to show.
 */
fun spacingScaleFor(fontScale: Float): Float =
    fontScale.coerceIn(1f, MAX_COMBINED_FONT_SCALE)
