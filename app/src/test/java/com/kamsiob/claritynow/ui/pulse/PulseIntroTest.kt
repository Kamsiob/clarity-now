package com.kamsiob.claritynow.ui.pulse

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one line that explains what a Pulse is has to be drawn where a person meets one.
 *
 * ## The bug
 *
 * `pulse_intro` reads "This is the Pulse. It reads what you have done in the app and asks
 * one question about it. Neither answer is the right one." It is the only sentence in the
 * app that says what the Pulse is to somebody who is looking at one, and **it had never
 * been drawn.**
 *
 * It sat in `PulseSurface`'s ambient branch under `showIntro && responseLabel == null`.
 * Ambient is `entry == null || settled || (entry.isAnswered && chosen == null)`, and
 * `settled` is set by a `LaunchedEffect` that returns immediately while `chosen` is null.
 * So an unanswered Pulse never reaches the ambient branch, and once it does it has an
 * answer, which fails the guard's second half. The only window was the handful of frames
 * between the settle animation ending and the projection catching up.
 *
 * Three usability personas met their first Pulse as a serif sentence, a question and two
 * pills with nothing saying what any of it was. The string written for exactly that moment
 * was one branch away the whole time.
 *
 * ## What this holds
 *
 * That the intro is drawn from the **question** composable, which is the one an unanswered
 * Pulse reaches. A source scan rather than a rendering test, because the defect was not in
 * the drawing, it was in which branch the drawing sat in, and that is a fact about the
 * file.
 */
class PulseIntroTest {

    @Test
    fun `the pulse intro is drawn where an unanswered pulse is drawn`() {
        val source = File("src/main/java/com/kamsiob/claritynow/ui/pulse/PulseSurface.kt")
        assertTrue("expected ${source.path}", source.isFile)
        val text = source.readText()

        val question = text.substringAfter("private fun PulseQuestion(")
            .substringBefore("\n@Composable")
        assertTrue(
            "expected to have found the PulseQuestion body",
            question.contains("pulse.entry.observation"),
        )
        assertTrue(
            "R.string.pulse_intro is not drawn by PulseQuestion, which is the composable " +
                "an unanswered Pulse reaches. If it has moved back to the ambient branch " +
                "it cannot draw: see this test's KDoc for why.",
            question.contains("R.string.pulse_intro"),
        )
        assertTrue(
            "PulseQuestion cannot decide whether to draw the intro without being told",
            text.contains("showIntro = state.showIntro"),
        )
    }

    /**
     * And that the string still exists to be drawn. A resource deleted as unused is the
     * other way this regresses, and lint's unused-resource check would have called it
     * unused for the whole time the guard was unsatisfiable.
     */
    @Test
    fun `the pulse intro string exists and says what a pulse is`() {
        val strings = File("src/main/res/values/strings.xml").readText()
        val line = Regex("""<string name="pulse_intro">(.*?)</string>""")
            .find(strings)
            ?.groupValues
            ?.get(1)
        assertTrue("pulse_intro is gone from strings.xml", line != null)
        assertTrue(
            "pulse_intro no longer says what the Pulse is: $line",
            line!!.contains("Pulse") && line.contains("question"),
        )
    }
}
