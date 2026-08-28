package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import java.lang.reflect.Modifier as JavaModifier

/**
 * The measurement harness every contrast test in this package reads from.
 *
 * design-v3.md 13 states one floor and issue #51 states one method: **contrast is a
 * computed number, never an eye judgment.** Phase 3 shipped a 4.40 to one failure on a
 * screen that looked completely correct, and it was found by computing the number.
 *
 * ## Why this file exists rather than another list of pairs
 *
 * A test that measures the pairs somebody remembered to list is the test that let phase
 * 3's failure through, and it is the test that let `inkTertiary` reach nineteen `Text`
 * call sites while `TrailContrastTest` was proving it reached none in `ui/trail`. So the
 * enumeration here is built to be total along both of its axes, and both are generated
 * rather than typed out:
 *
 * 1. **Tokens are read off the theme by reflection.** [tokensOf] walks the declared
 *    fields of `ClarityColors`, `ContemplativeColors` and every palette object, so a
 *    token added to the theme appears in the audit on the next run with nobody editing a
 *    test. What a new token does need is a line in [ROLES], and the coverage test fails
 *    until it has one.
 * 2. **Grounds are generated from the ranges the design permits, not from the two values
 *    the code happens to use.** design-v3.md 3.1, 3.2 and 12.1 between them permit an
 *    area wash anywhere from 3 to 16 percent; [washPercents] measures every whole percent
 *    of it, on all 48 colors, with the calm transform on and off. A later change to a
 *    wash opacity inside its own stated range therefore cannot introduce a failure this
 *    file has not already seen.
 *
 * A pair that is deliberately not measured says so by name in [ROLES] or in
 * [RULE_B_EXEMPTIONS], with the sentence of the specification that permits it.
 *
 * ## One floor, and no large text exception
 *
 * WCAG allows 3:1 for large text, and taking that exception is the statistically common
 * answer: it would let the 21sp item title, the 40sp hero and the 64sp timer numeral off
 * the 4.5 floor for free. **It is declined**, design-v3.md 15, and the reason is in this
 * issue rather than in taste. `sp` is not `pt`, so the boundary is already approximate,
 * and issue #51 adds an in-app text size control on top of the OS font scale, which
 * moves every size in the app by a factor the audit cannot know. A floor that depends on
 * a size is a floor that changes when somebody drags a slider. design-v3.md 13 states one
 * number for text and this file uses it: 4.5, with 3.0 reserved for a shape rather than
 * for a large word.
 */
internal object ContrastAudit {

    /** design-v3.md 13. Normal sized text. */
    const val TEXT_FLOOR = 4.5

    /** design-v3.md 13, and WCAG 1.4.11. A glyph is a graphic. */
    const val GRAPHIC_FLOOR = 3.0

    // -----------------------------------------------------------------------
    // Measurement
    // -----------------------------------------------------------------------

    /**
     * The ratio of [ink] as actually drawn on [ground].
     *
     * A translucent foreground is composited first, because a contrast ratio between a
     * color carrying an alpha and a background is not a defined quantity. Every value
     * here is quantized to 8 bits per channel on the way through, which is what an
     * `androidx.compose.ui.graphics.Color` in the sRGB space does on construction, so
     * these are pixel values rather than floats.
     */
    fun ratio(ink: Color, ground: Color): Double =
        contrastRatio(ink.compositeOver(ground), ground)

    /** [tint] laid onto [base] at [alpha]. */
    fun over(tint: Color, alpha: Float, base: Color): Color =
        if (alpha <= 0f) base else tint.copy(alpha = alpha).compositeOver(base)

    // -----------------------------------------------------------------------
    // Token enumeration, by reflection
    // -----------------------------------------------------------------------

    /**
     * Every `Color` property of [target], by name.
     *
     * `Color` is a value class over `ULong`, so on the JVM each one is a `long` field and
     * the packed value can be handed straight back to the constructor. Filtering on the
     * field type is what makes this total: a token added to `ClarityColors` is a new
     * `long` field and arrives here without anybody being asked. The `Float` wash alphas
     * and the `Boolean` world flag are not colors and are skipped by the same rule.
     */
    fun tokensOf(target: Any): Map<String, Color> = colorFields(target.javaClass, target)

    /**
     * The same, for the file level `val`s in `ClarityColors.kt`.
     *
     * Kotlin compiles a top level property into a static field on a synthetic
     * `...Kt` class, so `MarkBackground`, `MarkForeground` and `SupportAccent` are not
     * declared on any of the token holders above and would otherwise be the one way a
     * color could enter the theme without the coverage gate seeing it.
     */
    private fun fileLevelTokens(): Map<String, Color> =
        colorFields(Class.forName("com.kamsiob.claritynow.ui.theme.ClarityColorsKt"), null)

    private fun colorFields(type: Class<*>, holder: Any?): Map<String, Color> =
        type.declaredFields
            .filter { it.type == java.lang.Long.TYPE && !it.isSynthetic }
            .associate { field ->
                field.isAccessible = true
                val instance = if (JavaModifier.isStatic(field.modifiers)) null else holder
                field.name to Color(field.getLong(instance).toULong())
            }

    /** Every token the theme declares, prefixed by where it lives. */
    fun allThemeTokens(): Map<String, Color> =
        tokensOf(ClarityLightColors).mapKeys { "light.${it.key}" } +
            tokensOf(ClarityDarkColors).mapKeys { "dark.${it.key}" } +
            tokensOf(ClarityContemplativeColors).mapKeys { "contemplative.${it.key}" } +
            tokensOf(FocusPalette).mapKeys { "focus.${it.key}" } +
            tokensOf(PulsePalette).mapKeys { "pulse.${it.key}" } +
            tokensOf(ReportPalette).mapKeys { "report.${it.key}" } +
            tokensOf(OnboardingPalette).mapKeys { "onboarding.${it.key}" } +
            fileLevelTokens().mapKeys { "file.${it.key}" }
}

/**
 * design-v3.md 10.3.1 and SwipeableRow.kt: a swipe action's face fades in to its base
 * alpha and then deepens by 40 percent past the commit threshold, which is the deepest
 * the ground under its label ever gets.
 */
internal const val SWIPE_DEEPEN = 1.4f

/** A named color, so a failure can say which one it was. */
internal data class Swatch(val name: String, val color: Color)

/**
 * One measurable pair.
 *
 * A null [floor] means measured and printed but not asserted on, and [where] then has to
 * carry the sentence of the specification that says why. See [Role].
 */
internal data class Measured(
    val ink: Swatch,
    val ground: Swatch,
    val floor: Double?,
    val where: String,
) {
    val ratio: Double get() = ContrastAudit.ratio(ink.color, ground.color)
    val fails: Boolean get() = floor != null && ratio < floor
    val margin: Double? get() = floor?.let { ratio - it }
    override fun toString(): String =
        "${ink.name} on ${ground.name} at ${"%.3f".format(ratio)} to one" +
            (floor?.let { ", floor ${"%.1f".format(it)}" } ?: ", not asserted") +
            ". $where"
}
