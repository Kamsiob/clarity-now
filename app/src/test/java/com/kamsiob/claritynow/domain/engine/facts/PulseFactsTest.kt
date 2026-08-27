package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.data.event.SubjectKind
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.dateKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the person said back, and the label a callback is allowed to quote.
 * CLARITY_LOGIC_ENGINE.md 3.1 and 8 check 6.
 *
 * "A fabricated callback is the single most damaging output the app can produce",
 * per `CORPUS_2_REPORT.md` 2.6. The defense starts here: the label carried into the
 * facts is the string stored on the event, never a label looked up in the current
 * app version.
 */
class PulseFactsTest {

    private fun log(): TrailTestLog {
        val log = TrailTestLog()
        log.pulse(1, "p1", "persistence", "persistence.s1.03", subjectId = "i1", subjectKind = SubjectKind.ITEM)
        log.answer(1, "p1", "deep", "Deep work", isPositive = true, subjectId = "i1")
        log.pulse(3, "p2", "concentration", "concentration.s1.05", subjectId = "work", subjectKind = SubjectKind.AREA)
        log.answer(3, "p2", "drifting", "Drifting", isPositive = false, subjectId = "work")
        log.pulse(5, "p3", "spread", "spread.s1.02")
        // Generated and not answered. Dismissing the sheet is a supported state and
        // is never counted against anyone, so it appears in neither answer count.
        return log
    }

    private fun facts(log: TrailTestLog, fromDay: Int, toDay: Int): PulseFacts =
        FactExtractor(log.queries()).extract(window(fromDay, toDay)).pulse

    @Test
    fun `answers are counted lifetime and in the window, and split by polarity`() {
        val pulse = facts(log(), 3, 7)
        assertEquals(2, pulse.answeredLifetime)
        assertEquals(1, pulse.answeredInWindow)
        assertEquals(0, pulse.positiveInWindow)
        assertEquals(1, pulse.flaggedInWindow)
    }

    @Test
    fun `the last generated family is what the repeat filter reads`() {
        val pulse = facts(log(), 3, 7)
        assertEquals("spread", pulse.lastGeneratedFamily)
        assertEquals(dateKey(5), pulse.lastGeneratedDateKey)
    }

    /** Newest first, and every answer carries the exact label the person tapped. */
    @Test
    fun `recent answers are newest first and quote the stored label`() {
        val pulse = facts(log(), 3, 7)
        assertEquals(listOf("concentration", "persistence"), pulse.recentAnswers.map { it.family })
        val newest = pulse.recentAnswers.first()
        assertEquals(dateKey(3), newest.dateKey)
        assertEquals("Drifting", newest.responseLabel)
        assertEquals("drifting", newest.responseKey)
        assertEquals("work", newest.subjectId)
        assertTrue(pulse.recentAnswers.none { it.isPositive && it.family == "concentration" })
    }

    /** A callback looks a family up, so the answers are grouped by one. */
    @Test
    fun `answers are grouped by family`() {
        val byFamily = facts(log(), 3, 7).answersByFamily
        assertEquals(setOf("persistence", "concentration"), byFamily.keys)
        assertEquals("Deep work", byFamily.getValue("persistence").single().responseLabel)
    }

    @Test
    fun `a log with no pulses reports no last family`() {
        val pulse = facts(TrailTestLog(), 0, 7)
        assertNull(pulse.lastGeneratedFamily)
        assertNull(pulse.lastGeneratedDateKey)
        assertEquals(0, pulse.answeredLifetime)
        assertTrue(pulse.recentAnswers.isEmpty())
        assertTrue(pulse.answersByFamily.isEmpty())
    }
}
