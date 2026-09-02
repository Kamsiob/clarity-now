package com.kamsiob.claritynow.ui.onboarding

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The five things onboarding exists to convey, and the two that had no carrier. Issue #66,
 * `MASTER_BUILD_PROMPT.md` 13.1 and `docs/ONBOARDING_VARIANTS.md`.
 *
 * By the end of the sequence all six personas could state the one active item rule. None of
 * them could say why the app exists, and none could say where the rest of their work goes.
 * Group B put it in one line: "four cards with one job each is just a to do list with a
 * haircut. Where did my other forty things go? Nobody said."
 *
 * ## The queue is shown, and that is a fact about a string rather than about a picture
 *
 * The strongest thing that can be asserted here without a device is that the demo card
 * reads the **same resource** the real area card reads. That is what makes it a preview of
 * the app rather than an illustration of it: a person meets `3 waiting` in onboarding and
 * the identical line on their own screen a few taps later, and neither can drift from the
 * other because there is one string.
 */
class OnboardingConveyanceTest {

    // ------------------------------------------------------------------ the queue

    /**
     * Beat 1 shows a queue rather than asserting one, in the words the app itself uses.
     *
     * This could only be done once issue #65 put the same line on the real card, which is
     * what the issue said and why the two were closed together.
     */
    @Test
    fun `the demo cards carry the same waiting line the real card carries`() {
        assertTrue(
            "the demo card reads the one plural that says what is waiting",
            "R.plurals.queue_waiting" in beatOne(),
        )
        assertTrue(
            "and so does the real area card, which is what makes this a preview",
            "R.plurals.queue_waiting" in areaCard(),
        )
    }

    /**
     * The count falls as the next item takes its place, and is absent where there is
     * nothing behind.
     *
     * Both halves are the object rather than decoration: a number that never moved would be
     * a label, and a `0 waiting` on the two quiet cards would teach the wrong rule about
     * the card a person is about to meet.
     */
    @Test
    fun `the count moves with the promotion and is absent at zero`() {
        val card = beatOne()
        assertTrue(
            "the top card's queue is one shorter once the next item is active",
            "val waiting = card.waiting - if (promoted) 1 else 0" in card,
        )
        assertTrue("and nothing is drawn at zero", "if (waiting > 0) {" in card)
        assertEquals(
            "two of the four demo areas have nothing behind them, which is the other half " +
                "of what the object is",
            2,
            Regex("""waiting = 0,""").findAll(card).count(),
        )
    }

    // ------------------------------------------------------------------ what it does not do

    /**
     * One moment says the app notices what happened and never invents, in plain words.
     *
     * The Report is the surface that does the noticing, so the promise is in its own
     * caption rather than in a sixth moment. The tone rules in `docs/ONBOARDING_VARIANTS.md`
     * are what the second assertion holds: it describes the app, never the reader.
     */
    @Test
    fun `one moment says the app does not invent, without naming any machinery`() {
        val caption = string("onboarding_depth_report_caption")
        assertTrue("it says the app notices what happened", "notices what happened" in caption)
        assertTrue("and that it invents nothing", "does not make anything up" in caption)
        assertTrue(
            "and the other half, that it never judges, is one card above",
            "no score anywhere" in string("onboarding_depth_momentum_caption"),
        )
    }

    /**
     * No onboarding string names the machinery or the reader.
     *
     * `engine`, `corpus` and `algorithm` are the words issue #66 forbids by name, and
     * `neurodivergent` is the one the tone brief forbids across the whole sequence. The
     * scan is over every onboarding string rather than the ones that changed, because a
     * word arrives in this file the same way any other regression does.
     */
    @Test
    fun `no onboarding copy names an engine, a corpus, an algorithm or the reader`() {
        val forbidden = listOf("engine", "corpus", "algorithm", "neurodivergent", "neurotypical")
        val offenders = onboardingStrings().flatMap { (name, text) ->
            forbidden.filter { it in text.lowercase() }.map { "$name says $it" }
        }
        assertEquals(emptyList<String>(), offenders)
    }

    // ------------------------------------------------------------------ the pace

    /**
     * Nothing takes the screen away from somebody who is still on it.
     *
     * The decision is recorded in `OnboardingBeatFour` and applies to beat 1 word for word.
     * A page that leaves on a timer is the worst behavior in the sequence and it is pure
     * loss, because a tap does the same job better.
     */
    @Test
    fun `no beat advances itself`() {
        listOf(beatOne(), beatFour()).forEach { source ->
            val code = source.lines().filterNot { it.trimStart().startsWith("//") }
            val strays = code.filter { "onAdvance()" in it || "onMoment(" in it }
                .filter { line -> "delay" in line }
            assertEquals("a beat advanced itself on a timer", emptyList<String>(), strays)
        }
        assertTrue(
            "beat 1 says at its own timeline that the advance was removed",
            "**No auto advance.**" in beatOne(),
        )
    }

    /**
     * The closing line belongs to beat 3 and cannot outlive it.
     *
     * Its fade runs in a child of the reveal's effect, so tapping through beat 3 cancels it
     * at whatever opacity it had reached and `Nothing here can break.` stayed on screen for
     * the whole of beat 4, drawn over the Pulse sample's second answer. It only ever
     * appeared on the fast path, which is the path the fifteen second budget is about.
     */
    @Test
    fun `the closing line cannot be stranded on a later beat`() {
        val route = this.route()
        assertTrue(
            "it is snapped away when the beat changes",
            "closing.snapTo(0f)" in route,
        )
        assertTrue(
            "and it is not composed on any other beat, because an effect runs after the " +
                "frame that caused it",
            "if (state.beat == OnboardingBeat.THE_REVEAL && closing.value > 0f)" in route,
        )
    }

    // ------------------------------------------------------------------ helpers

    private fun read(path: String): String =
        File("src/main/java/com/kamsiob/claritynow/$path").readText()

    private fun beatOne(): String = read("ui/onboarding/OnboardingBeatOne.kt")
    private fun beatFour(): String = read("ui/onboarding/OnboardingBeatFour.kt")
    private fun route(): String = read("ui/onboarding/OnboardingRoute.kt")
    private fun areaCard(): String = read("ui/areas/AreaCard.kt")

    private fun strings(): String = File("src/main/res/values/strings.xml").readText()

    private fun string(name: String): String =
        Regex("""<string name="$name">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(strings())
            ?.groupValues
            ?.get(1)
            .orEmpty()

    private fun onboardingStrings(): List<Pair<String, String>> =
        Regex("""<string name="(onboarding_[a-z_]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(strings())
            .map { it.groupValues[1] to it.groupValues[2] }
            .toList()
}
