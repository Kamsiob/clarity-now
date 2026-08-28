package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The adjustable text size control and the spacing that scales with it. Addendum 01 8f,
 * issue #51, design-v3.md 13.
 *
 * Almost nothing in this feature can be seen by looking. A combined scale that quietly
 * exceeded 200 percent would clip on one screen out of a dozen; a factor applied twice
 * would look like a person having chosen a larger size; a spacing rule that scaled
 * downward would tighten a screen at the one setting meant to open it. All of it is
 * arithmetic, so all of it is pinned here.
 */
class TextSizeScaleTest {

    /** The scales a phone can be set to, including the two above the app's own ladder. */
    private val systemScales = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f, 1.8f, 2f)

    // ------------------------------------------------------------ the ladder

    @Test
    fun `the steps ascend and DEFAULT is exactly one`() {
        val scales = ClarityTextSize.entries.map { it.scale }
        assertEquals(
            "the steps must be declared in ascending order, because the picker renders " +
                "them in declaration order and a screen reader reads that order aloud",
            scales.sorted(),
            scales,
        )
        assertEquals(
            "DEFAULT has to be a true no-op, so that a person who never opens this row " +
                "gets exactly what their phone asked for",
            1f,
            ClarityTextSize.DEFAULT.scale,
            0f,
        )
    }

    /**
     * The decision recorded in design-v3.md 13, as arithmetic.
     *
     * At `DEFAULT` the app returns the phone's own figure unchanged at every setting the
     * phone has. An implementation that overrode instead of multiplying would return 1.0
     * here for a phone at 2.0, which is the failure that decision exists to prevent and
     * the one nobody would notice on their own device.
     */
    @Test
    fun `the app setting multiplies the phone's scale rather than replacing it`() {
        systemScales.forEach { system ->
            assertEquals(
                "at DEFAULT the phone's own scale must survive untouched, and $system did not",
                system,
                combinedFontScale(system, ClarityTextSize.DEFAULT),
                TOLERANCE,
            )
        }
        assertEquals(
            "on a phone at 100 percent, Largest is the app's own 1.5",
            1.5f,
            combinedFontScale(1f, ClarityTextSize.LARGEST),
            TOLERANCE,
        )
        assertEquals(
            "the two multiply below the ceiling",
            1.15f * 1.15f,
            combinedFontScale(1.15f, ClarityTextSize.LARGE),
            TOLERANCE,
        )
    }

    @Test
    fun `no combination of the two settings passes 200 percent`() {
        systemScales.forEach { system ->
            ClarityTextSize.entries.forEach { step ->
                val combined = combinedFontScale(system, step)
                assertTrue(
                    "phone $system times ${step.name} reached $combined. Every clipping " +
                        "analysis in this project, design-v3.md 13, 5.3's timer cap and " +
                        "ClarityTabBar's measurement of the floating bar, is written " +
                        "against 200 percent and none of them survives a larger one",
                    combined <= MAX_COMBINED_FONT_SCALE + TOLERANCE,
                )
                assertTrue(
                    "phone $system times ${step.name} reached $combined, under the " +
                        "platform's own smallest step",
                    combined >= MIN_COMBINED_FONT_SCALE - TOLERANCE,
                )
            }
        }
    }

    /**
     * The picker's line about the ceiling appears exactly when the ceiling is what is
     * deciding, and never when it is not.
     *
     * A person told their phone is holding the app back while it is not would go and
     * change a system setting for nothing. The other direction is worse: a step that
     * changes nothing, with no explanation, is a broken control.
     */
    @Test
    fun `the ceiling notice appears exactly when the ceiling is deciding`() {
        systemScales.forEach { system ->
            ClarityTextSize.entries.forEach { step ->
                val combined = combinedFontScale(system, step)
                if (isClampedByCeiling(system, step)) {
                    assertEquals(
                        "phone $system with ${step.name} says the ceiling is deciding, so " +
                            "the result has to be the ceiling",
                        MAX_COMBINED_FONT_SCALE,
                        combined,
                        TOLERANCE,
                    )
                } else {
                    assertEquals(
                        "phone $system with ${step.name} says the ceiling is not deciding, " +
                            "so the result has to be the two settings multiplied",
                        (system * step.scale).coerceAtLeast(MIN_COMBINED_FONT_SCALE),
                        combined,
                        TOLERANCE,
                    )
                }
            }
        }
        assertTrue(
            "a phone at 200 percent has nothing left to give, and the picker has to say so",
            isClampedByCeiling(2f, ClarityTextSize.LARGE),
        )
        assertTrue(
            "a phone at 100 percent is not the thing deciding, at any step",
            ClarityTextSize.entries.none { isClampedByCeiling(1f, it) },
        )
    }

    // ----------------------------------------------------------- the spacing

    /**
     * design-v3.md 13 calls the numbers in section 6 minimums rather than targets to
     * compress. So the grid opens with the text and never closes below it, which also
     * makes every dimension in the app identical to what it was before this control
     * existed on a phone at its default setting.
     */
    @Test
    fun `spacing grows with the text and never shrinks below the specified grid`() {
        listOf(0.5f, 0.85f, 1f).forEach { small ->
            assertEquals(
                "the grid must hold at its specified value at $small",
                1f,
                spacingScaleFor(small),
                TOLERANCE,
            )
        }
        assertEquals("linear above one", 1.5f, spacingScaleFor(1.5f), TOLERANCE)
        assertEquals(
            "held at the ceiling, so an OEM that allows a larger system scale than " +
                "Android's own cannot open the grid past what has been measured",
            2f,
            spacingScaleFor(2.4f),
            TOLERANCE,
        )
    }

    /**
     * 48dp is a statement about a fingertip, so it is the one measure that may not move
     * in either direction. Everything that grows with the text grows past it on its own.
     */
    @Test
    fun `the touch minimum is 48dp at every text size`() {
        assertEquals(48.dp, ClaritySpacing.minTouchTarget)
        systemScales.forEach { system ->
            ClarityTextSize.entries.forEach { step ->
                val grid = spacingScaleFor(combinedFontScale(system, step))
                assertTrue(
                    "a row whose height comes from the grid must still clear 48dp at " +
                        "phone $system with ${step.name}",
                    ClaritySpacing.minTouchTarget.value * grid >= 48f - TOLERANCE,
                )
            }
        }
    }

    // ----------------------------------------------------------- the density

    /**
     * The app's factor is applied in sp space and the phone's own density performs the
     * conversion, so a platform that converts sp to px along a curve keeps doing so.
     *
     * The fake below is deliberately non linear, in the same direction Android 14's
     * converter is: it grows large text less than the raw multiplication would. Replacing
     * `LocalDensity` with a plain `Density(density, combined)` would pass the first
     * assertion and fail the other two, and on a device it would put design-v3.md 5.3's
     * 40sp Report headline well past the size the platform intended for it.
     */
    @Test
    fun `the app factor composes with the platform's sp curve rather than replacing it`() {
        val curved = CompressingDensity(fontScale = 1.5f)
        val scaled = curved.withTextSize(ClarityTextSize.LARGE)

        assertEquals(
            "the reported scale is the product, which is what the timer numeral's cap reads",
            1.5f * 1.15f,
            scaled.fontScale,
            TOLERANCE,
        )
        val throughTheCurve = with(curved) { (40f * 1.15f).sp.toDp() }
        val throughTheWrapper = with(scaled) { 40.sp.toDp() }
        assertEquals(
            "40sp at Large must go through the phone's own curve, not around it",
            throughTheCurve.value,
            throughTheWrapper.value,
            TOLERANCE,
        )
        assertTrue(
            "a curve that compresses large text must still be compressing it, and " +
                "${throughTheWrapper.value} is the raw product",
            throughTheWrapper.value < 40f * 1.5f * 1.15f,
        )
    }

    @Test
    fun `a dp is untouched by the text size`() {
        val scaled = CompressingDensity(fontScale = 1f).withTextSize(ClarityTextSize.LARGEST)
        val pixels = with(scaled) { 48.dp.toPx() }
        assertEquals(
            "a physical measure does not move because somebody chose larger text",
            48f * DENSITY,
            pixels,
            TOLERANCE,
        )
        assertEquals("and neither does the pixel density", DENSITY, scaled.density, TOLERANCE)
    }

    @Test
    fun `the default setting adds nothing at all`() {
        val base = CompressingDensity(fontScale = 1.3f)
        assertSame(
            "DEFAULT must return the density it was given, so the common case carries no " +
                "wrapper and no rounding of its own",
            base,
            base.withTextSize(ClarityTextSize.DEFAULT),
        )
    }

    /**
     * design-v3.md 5.3's timer cap, now measured against the combined figure.
     *
     * `FocusRing` reads `LocalDensity`, which under the theme is the phone's scale times
     * the app's, so the cap it applies is the cap on the product. Before the app setting
     * existed the two were the same number and this distinction did not exist.
     */
    @Test
    fun `capping a font scale holds it at the cap whatever the two settings were`() {
        val atCeiling = CompressingDensity(fontScale = 2f).withTextSize(ClarityTextSize.LARGEST)
        assertEquals(
            "the combined figure is what the cap is applied to",
            MAX_COMBINED_FONT_SCALE,
            atCeiling.fontScale,
            TOLERANCE,
        )
        assertEquals(
            TIMER_NUMERAL_CAP,
            atCeiling.cappedFontScale(TIMER_NUMERAL_CAP).fontScale,
            TOLERANCE,
        )

        val underTheCap = CompressingDensity(fontScale = 1f)
        assertSame(
            "a density already under the cap is returned as it is",
            underTheCap,
            underTheCap.cappedFontScale(TIMER_NUMERAL_CAP),
        )
    }

    // ------------------------------------------------------------- the seams

    /**
     * The factor is applied in one place, and applying it twice would square it.
     *
     * `ContemplativeTheme` is always composed inside `ClarityTheme`, so it inherits the
     * scaled density. A second `LocalDensity provides` in that function would give Focus,
     * Pulse, the Report and Onboarding 2.25x while every Daylight screen ran at 1.5x, and
     * it would look like a person having chosen a very large size rather than like a bug.
     */
    @Test
    fun `only ClarityTheme overrides the density with the text size`() {
        val theme = File("src/main/java/com/kamsiob/claritynow/ui/theme/ClarityTheme.kt")
        assertTrue("expected ${theme.path}, run was in ${File("").absolutePath}", theme.isFile)
        val text = theme.readText()

        val clarity = text
            .substringAfter("fun ClarityTheme(")
            .substringBefore("fun ContemplativeTheme(")
        val contemplative = text.substringAfter("fun ContemplativeTheme(")

        assertTrue(
            "ClarityTheme must provide the scaled density. Without it the setting is " +
                "stored and nothing reads it, which is a control that does nothing",
            clarity.contains("LocalDensity provides"),
        )
        assertTrue(
            "ClarityTheme must publish the phone's own scale, which is the only thing " +
                "that can tell the picker whether the ceiling is what is deciding",
            clarity.contains("LocalSystemFontScale provides"),
        )
        assertTrue(
            "ContemplativeTheme is nested inside ClarityTheme and must not apply the " +
                "factor a second time",
            !contemplative.contains("LocalDensity provides"),
        )
    }

    /**
     * Addendum 01 8f's refusal, as a fact about the build rather than a line in a document.
     *
     * The evidence for specialized dyslexia typefaces is thin and the same effort spent on
     * size, spacing and contrast has evidence behind it. A later session adding one would
     * be undoing the decision this whole issue is built on, and the cheapest place to
     * catch that is the moment a fourth font file lands in the tree.
     */
    @Test
    fun `the app bundles two families and no third one`() {
        val fonts = File("src/main/res/font")
        assertTrue("expected ${fonts.path}, run was in ${File("").absolutePath}", fonts.isDirectory)
        val names = fonts.listFiles().orEmpty().map { it.name }.sorted()
        assertEquals(
            "design-v3.md 5 bundles Newsreader and Hanken Grotesk, and Addendum 01 8f " +
                "refuses a dyslexia friendly typeface by name. Found: $names",
            listOf("hanken_grotesk.ttf", "newsreader.ttf", "newsreader_italic.ttf"),
            names,
        )
    }

    /**
     * A stand in for the platform's non linear font scale converter.
     *
     * Android 14 grows large text less than a raw multiplication would, so that a display
     * size at 200 percent does not run off the screen. The exact curve is the platform's
     * and is not reproduced here; what matters is that the conversion is not
     * `value * fontScale`, so a wrapper that silently replaced it would be visible.
     */
    private class CompressingDensity(override val fontScale: Float) : Density {

        override val density: Float = DENSITY

        override fun TextUnit.toDp(): Dp {
            val raw = value * fontScale
            return Dp(value + (raw - value) * COMPRESSION)
        }

        override fun TextUnit.toPx(): Float = toDp().value * density
    }

    private companion object {
        const val TOLERANCE = 0.0001f
        const val DENSITY = 2.75f
        const val COMPRESSION = 0.5f

        /** design-v3.md 5.3, and `FocusRing.NUMERAL_MAX_FONT_SCALE`, which is private. */
        const val TIMER_NUMERAL_CAP = 1.3f
    }
}
