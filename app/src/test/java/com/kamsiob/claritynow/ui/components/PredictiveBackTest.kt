package com.kamsiob.claritynow.ui.components

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Back, and whether it shows a person where they are going. Issue #63.
 *
 * The app targets an SDK where predictive back is on by default and every one of its eight
 * `BackHandler` call sites was the non predictive form, so pressing back anywhere committed
 * with no preview at all. The migration is not a flag: each site had to be looked at and
 * either given a preview or given a reason, because a preview that uncovers a bare ground
 * is a picture of leaving rather than a preview of a destination, and that is worse than
 * none.
 *
 * What this file holds is the part a later session could undo without noticing: that every
 * site is accounted for, that the preview only exists where there is something behind to
 * see, and that reduce motion collapses it rather than shortening it.
 */
class PredictiveBackTest {

    /**
     * Every `BackHandler` left in the app names its reason.
     *
     * The issue's first criterion, made mechanical. A new one added without a comment fails
     * here, which is the only thing that stops the count drifting back to eight.
     */
    @Test
    fun `every remaining back handler says why it is not predictive`() {
        val unexplained = uiSources()
            .flatMap { file ->
                val lines = file.readText().lines()
                lines.mapIndexedNotNull { index, line ->
                    if (!line.trimStart().startsWith("BackHandler")) {
                        null
                    } else {
                        // The reason is the comment block immediately above it.
                        val above = lines.take(index).takeLastWhile { it.trimStart().startsWith("//") }
                        val explained = above.any { "Not predictive" in it }
                        if (explained) null else "${file.name}:${index + 1}"
                    }
                }
            }
        assertEquals(
            "a BackHandler with no reason above it. Either give it a preview or say why " +
                "it must not have one, per issue #63",
            emptyList<String>(),
            unexplained,
        )
    }

    /** And the three that stay are the three that were decided, not a fourth added quietly. */
    @Test
    fun `three sites stay non predictive and they are the three that were reasoned about`() {
        val remaining = uiSources()
            .filter { it.readText().lines().any { line -> line.trimStart().startsWith("BackHandler") } }
            .map { it.name }
            .sorted()
        assertEquals(
            listOf("ClarityShell.kt", "OnboardingRoute.kt", "TutorialHost.kt"),
            remaining,
        )
    }

    /**
     * The five that are previewed, and the two shapes of them.
     *
     * `FocusRoute`, `ArchiveScreen` and `SettingsSurface` are drawn over the tab they were
     * opened from, so the destination is already composed and the preview simply uncovers
     * it. The two history pages swap with their surface rather than stacking over it, so
     * they compose the destination while a gesture is in flight, which is what
     * `isDrawing` is for.
     */
    @Test
    fun `the previewed sites are the ones with a destination behind them`() {
        listOf("FocusRoute.kt", "ArchiveScreen.kt", "SettingsSurface.kt", "ReportRoute.kt", "PulseRoute.kt")
            .forEach { name ->
                val source = uiSources().first { it.name == name }.readText()
                assertTrue("$name should take the gesture", "rememberPredictiveBack" in source)
                assertTrue("$name should draw the preview", "predictiveBackPreview" in source)
            }
        listOf("ReportRoute.kt", "PulseRoute.kt").forEach { name ->
            val source = uiSources().first { it.name == name }.readText()
            assertTrue(
                "$name swaps rather than stacks, so it has to compose the destination " +
                    "while the gesture is in flight or the preview uncovers nothing",
                "predictiveBack?.isDrawing == true" in source,
            )
        }
    }

    /**
     * `design-v3.md` 8.3, and the difference between collapsing an animation and shortening
     * one.
     *
     * A shape change is spatial, so the rule gates it off rather than speeding it up: the
     * progress is held at zero and back commits on release exactly as it did before.
     * Somebody who asked for less motion gets the behavior without the movement rather than
     * a faster version of it.
     */
    @Test
    fun `reduce motion collapses the preview rather than shortening it`() {
        val source = component()
        assertTrue(
            "the progress is never taken from the finger at all, rather than taken and " +
                "animated over a shorter time",
            "if (!motion.reduced) state.drawn.snapTo(event.progress)" in source,
        )
        assertFalse(
            "no duration belongs here. The finger is the progress and the spring is the " +
                "app's own spatial spring",
            Regex("""\b\d+\s*L?\s*,?\s*//.*millis""").containsMatchIn(source) ||
                "durationMillis" in source ||
                "tween(" in source,
        )
        assertTrue("the return is the app's own spatial spring", "motion.springStandard()" in source)
    }

    /**
     * Abandoning a gesture runs nothing, which is the whole of "canceling leaves the
     * session running".
     *
     * `FocusRoute` passes `viewModel.leave()`, and `leave` is the only thing that could
     * take a person off a running session's screen. It is called after the collect
     * completes, so a cancellation jumps over it to the catch.
     */
    @Test
    fun `a canceled gesture runs no callback at all`() {
        val handler = component().substringAfter("PredictiveBackHandler(").substringBefore("\n    return state")
        val onBackAt = handler.indexOf("onBack()")
        val catchAt = handler.indexOf("catch (canceled")
        assertTrue("onBack must be inside the try", onBackAt in 0 until catchAt)
        assertFalse("and never in the catch", "onBack()" in handler.substring(catchAt))
    }

    private fun component(): String =
        File("src/main/java/com/kamsiob/claritynow/ui/components/PredictiveBack.kt").readText()

    private fun uiSources(): List<File> =
        File("src/main/java/com/kamsiob/claritynow/ui").walkTopDown().filter { it.extension == "kt" }.toList()
}
