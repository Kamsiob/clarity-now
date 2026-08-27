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
 * what a de-emphasized caption is for. It measures 2.37 to 1 in light mode, missing
 * the floor by a factor of two, and it is design-v3.md 15.1's "Dark mode with low
 * contrast body text" in its light mode form. That is the finding this file exists to
 * keep true: the Trail uses `inkSecondary` for both.
 *
 * `contrastRatio` is the app's own WCAG 2.1 implementation, and the tokens are the
 * shipped ones, so these numbers are what a person actually sees rather than what a
 * palette document claims. Both ink tokens carry an alpha, so each is composited over
 * its surface first; a contrast ratio between a translucent color and a background is
 * not a defined quantity.
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
     * The light canvas is the tightest of the six by a wide margin, at about 4.50 to
     * one against a floor of 4.5. It passes, and it passes with almost nothing to
     * spare, so a later darkening of `canvas` or a lightening of `inkSecondary` would
     * take the whole Daylight world under the floor without touching this screen.
     * Pinned so that change fails here rather than in an audit.
     */
    @Test
    fun `the light canvas is the tightest surface and has almost no margin`() {
        val tightest = ratioOn(ClarityLightColors.inkSecondary, ClarityLightColors.canvas)
        assertTrue("expected about 4.50 to one, measured $tightest", tightest in 4.50..4.55)
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
     * The completed row is the one place on this screen where the choice of ground
     * decides whether the app passes design-v3.md 13, and it is a one word decision.
     *
     * design-v3.md 11 calls it a "mint wash card". Read as a card it is
     * `positiveGreen` at 8 percent over `card`, and the timestamp on it clears the
     * floor. Read as a tint on the page it is the same wash over `canvas`, which
     * darkens the ground and takes the same text under the floor. Nothing else on the
     * screen distinguishes the two readings, so without this test the failing one is
     * an equally reasonable thing for somebody to build.
     */
    @Test
    fun `the completed row's mint goes over card, because over canvas it fails the floor`() {
        val overCard = ratioOn(ClarityLightColors.inkSecondary, completedRow(ClarityLightColors))
        val overCanvas =
            ratioOn(ClarityLightColors.inkSecondary, completedRowOnCanvas(ClarityLightColors))

        assertTrue("the shipped reading measures $overCard to one", overCard >= floor)
        assertTrue(
            "the canvas reading measures $overCanvas to one, which is supposed to be " +
                "the failing one this test exists to rule out",
            overCanvas < floor,
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
