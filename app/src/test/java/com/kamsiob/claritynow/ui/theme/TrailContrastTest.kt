package com.kamsiob.claritynow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The Trail's small text, measured rather than eyeballed. design-v3.md 13, "text
 * contrast 4.5 to 1 minimum".
 *
 * The Trail puts two pieces of 12sp text on the canvas, the row timestamp and the day
 * header count, and the obvious token for both of them is `inkTertiary`, which is
 * what a de-emphasized caption is for. It measures 2.34 to 1 in light mode, missing
 * the floor by a factor of two, and it is design-v3.md 15.1's "Dark mode with low
 * contrast body text" in its light mode form. That is the finding this file exists to
 * keep true: the Trail uses `inkSecondary` for both.
 *
 * `contrastRatio` is the app's own WCAG 2.1 implementation, and the tokens are the
 * shipped ones, so these numbers are what a person actually sees rather than what a
 * palette document claims. Both ink tokens carry an alpha, so each is composited over
 * its surface first; a contrast ratio between a translucent color and a background is
 * not a defined quantity.
 *
 * **Phase 3c moved the ground under all of it.** The light canvas went from `#F1F1F6`
 * to `#E6E6EC` and `inkSecondary` went from 0.60 to 0.64, and the second is the
 * consequence of the first rather than an improvement chosen alongside it: at 0.60 this
 * screen's timestamps measured 4.34 to one on the new canvas. Every figure below is the
 * re-measurement, and one of the findings this file recorded no longer holds. It is
 * marked where it is.
 *
 * **The `inkTertiary` gate below is the Trail's half of a rule that covers the whole
 * app**, and for three phases it was the only half anybody checked. `FaintInkTest` now
 * scans every screen and both widget packages for the same thing and finds thirty one
 * foregrounds drawn in it, which is what a rule proved on one directory buys. This test
 * is kept rather than folded in, because the Trail is where the finding was made and
 * because a screen specific test names the screen.
 *
 * Every figure is what the running app computes, which means 8 bits per channel: an
 * `androidx.compose.ui.graphics.Color` in the sRGB space quantizes on construction, so
 * a wash composited onto a card lands on a real pixel value and not on a float. Two of
 * the numbers phase 3b wrote down here were carried at full precision and are 0.02 out
 * as a result. The ranges below are the measured values.
 */
class TrailContrastTest {

    /** design-v3.md 13. Normal sized text, so 4.5 to 1 rather than 3 to 1. */
    private val floor = 4.5

    /** design-v3.md 11, the completed row's mint, stated as a number and not a range. */
    private val completedRowWashAlpha = 0.08f

    private val trailSources = "src/main/java/com/kamsiob/claritynow/ui/trail"

    private fun ratioOn(foreground: Color, background: Color): Double =
        contrastRatio(foreground.compositeOver(background), background)

    /**
     * design-v3.md 11's mint completed row, as a solid ground to measure text on.
     *
     * The mint goes over `card`, which is what the word "card" in section 11 means and
     * also the only reading that clears the contrast floor. See the test below.
     */
    private fun completedRow(colors: ClarityColors): Color =
        colors.positiveGreen.copy(alpha = completedRowWashAlpha).compositeOver(colors.card)

    /** The rejected alternative: the same wash laid straight onto the page ground. */
    private fun completedRowOnCanvas(colors: ClarityColors): Color =
        colors.positiveGreen.copy(alpha = completedRowWashAlpha).compositeOver(colors.canvas)

    @Test
    fun `the trail's small text clears the contrast floor on every surface it sits on`() {
        val cases = mapOf(
            "inkSecondary light on canvas" to
                ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.canvas),
            "inkSecondary light on the completed row" to
                ratioOn(ClarityLightColors.inkSecondary, completedRow(ClarityLightColors)),
            "inkSecondary light on card" to
                ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.card),
            "inkSecondary dark on canvas" to
                ratioOn(ClarityDarkColors.inkSecondary, ClarityDarkColors.canvas),
            "inkSecondary dark on the completed row" to
                ratioOn(ClarityDarkColors.inkSecondary, completedRow(ClarityDarkColors)),
            "inkSecondary dark on card" to
                ratioOn(ClarityDarkColors.inkSecondary, ClarityDarkColors.card),
        )
        val failures = cases.filterValues { it < floor }
        assertTrue(
            "below design-v3.md 13's floor of $floor to one: " +
                failures.entries.joinToString { "${it.key} at ${it.value}" } +
                ". Raising inkSecondary's alpha above 0.60 is the fix; it is a " +
                "ClarityColors token rather than anything the Trail owns.",
            failures.isEmpty(),
        )
    }

    /**
     * The light canvas is still the tightest of the six, and the margin grew again.
     *
     * It measured 4.5046 to one before phase 3c, 5.19 after it, and **5.78 since the
     * palette moved to Flexoki**: the new ink is `#100F0F` against the old `#17171C`,
     * which is both darker and, more to the point, no longer a violet. Every ink level
     * in the app improved by the same move.
     *
     * The band is what this pins rather than the number. A value drifting up is a value
     * somebody should look at as much as one drifting down: `inkSecondary` climbing means
     * the ladder is being flattened somewhere.
     */
    @Test
    fun `the light canvas is the tightest surface, with a margin since phase 3c`() {
        val tightest = ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.canvas)
        assertTrue("expected about 5.78 to one, measured $tightest", tightest in 5.65..5.95)
        val others = listOf(
            ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.card),
            ratioOn(ClarityDarkColors.inkSecondary, ClarityDarkColors.canvas),
            ratioOn(ClarityDarkColors.inkSecondary, ClarityDarkColors.card),
        )
        assertTrue(
            "the light canvas is supposed to be the worst case, and it measured " +
                "$tightest against $others",
            others.all { it > tightest },
        )
    }

    /**
     * The completed row is a card, and **the argument for that changed in phase 3c**.
     *
     * design-v3.md 11 calls it a "mint wash card". Read as a card it is `positiveGreen`
     * at 8 percent over `card`; read as a tint on the page it is the same wash over
     * `canvas`. Until phase 3c the contrast floor decided between them: the card reading
     * measured 4.56 to one and the canvas reading 4.40, so only one of the two shipped a
     * legible timestamp. With `inkSecondary` at 0.64 both clear, 5.14 and 4.77, and the
     * floor no longer chooses.
     *
     * The visual refresh moved both grounds again and both still clear: the card
     * reading is 5.53 and the canvas reading 5.02, on `inkSecondary` at 0.68.
     *
     * That is recorded rather than quietly dropped, because the reading is unchanged and
     * the reasons for it now have to carry it alone: design-v3.md 11 says "card", and the
     * phase 3c surface ladder says a completed event is content sitting at the top rank
     * rather than a tint on the page.
     *
     * What is asserted is therefore the ordering rather than a pass and a fail. The card
     * reading is the lighter ground and has to stay the more legible one, and the gap
     * between the two is what getting the word wrong still costs.
     */
    @Test
    fun `the completed row's mint goes over card, and stays the more legible ground`() {
        val overCard = ratioOn(ClarityLightColors.inkSecondary, completedRow(ClarityLightColors))
        val overCanvas =
            ratioOn(ClarityLightColors.inkSecondary, completedRowOnCanvas(ClarityLightColors))

        assertTrue("the shipped reading measures $overCard to one", overCard >= floor)
        assertTrue(
            "the canvas reading measures $overCanvas to one. Since phase 3c it clears " +
                "the floor as well, so if this is ever under $floor again the tokens " +
                "moved and the whole file needs re-measuring rather than this test " +
                "relaxing.",
            overCanvas >= floor,
        )
        assertTrue(
            "the card reading is the lighter ground and is supposed to be the more " +
                "legible one. Card $overCard, canvas $overCanvas.",
            overCard > overCanvas,
        )
    }

    @Test
    fun `ink tertiary fails the contrast floor by close to a factor of two`() {
        val lightOnCanvas = ratioOn(ClarityLightColors.inkTertiary, ClarityLightColors.canvas)
        val lightOnCard = ratioOn(ClarityLightColors.inkTertiary, ClarityLightColors.card)
        val darkOnCanvas = ratioOn(ClarityDarkColors.inkTertiary, ClarityDarkColors.canvas)

        assertTrue("inkTertiary light on canvas is $lightOnCanvas to one", lightOnCanvas < 2.5)
        assertTrue("inkTertiary light on card is $lightOnCard to one", lightOnCard < 2.5)
        assertTrue("inkTertiary dark on canvas is $darkOnCanvas to one", darkOnCanvas < 3.5)
        // Which is the whole argument for spending the darker token on a timestamp.
        assertTrue(
            ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.canvas) >
                lightOnCanvas * 1.8,
        )
    }

    @Test
    fun `ink tertiary is not used for any trail text`() {
        val dir = File(trailSources)
        assertTrue(
            "expected the Trail screen at $trailSources, and this run is in " +
                File("").absolutePath + ". Without it this test passes vacuously.",
            dir.isDirectory,
        )
        val sources = dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertTrue("no Kotlin source under $trailSources", sources.isNotEmpty())

        val offenders = sources.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, text ->
                val trimmed = text.trimStart()
                val isComment = trimmed.startsWith("//") || trimmed.startsWith("*") ||
                    trimmed.startsWith("/*")
                if (!isComment && text.contains("inkTertiary")) {
                    "${file.path}:${index + 1}: ${text.trim()}"
                } else {
                    null
                }
            }
        }
        assertTrue(
            "inkTertiary measures " +
                ratioOn(ClarityLightColors.inkTertiary, ClarityLightColors.canvas) +
                " to one on the light canvas, against design-v3.md 13's floor of " +
                "$floor to one. Use inkSecondary:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }
}
