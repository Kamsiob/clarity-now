package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Weekday
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The confidence thresholds in CLARITY_LOGIC_ENGINE.md 3.7.
 *
 * "An invented cue is worse than no plan, because it makes a claim about the user's
 * life the user knows to be false." Every assertion here is about a cue **not**
 * appearing, except the one fixture built to be a genuine rhythm.
 */
class CueFactsTest {

    /**
     * Twelve weeks with the same weekly shape: nothing on the weekend, one event on
     * Monday, two on Tuesday, three on Wednesday, two on Thursday, two on Friday.
     *
     * `TEST_START_DATE` is a Sunday, so day 3 of each week is a Wednesday.
     */
    private fun rhythmLog(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        val perWeekday = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 2, 5 to 2)
        for (week in 0 until 12) {
            for ((weekday, count) in perWeekday) {
                for (index in 0 until count) {
                    val day = week * 7 + weekday
                    log.item(at(day, 9, index), "w$day-$index", "work", "Thing $day-$index")
                }
            }
        }
        return log
    }

    private fun cues(log: TrailTestLog, fromDay: Int, toDay: Int): CueFacts =
        FactExtractor(log.queries()).extract(window(fromDay, toDay)).cues

    @Test
    fun `a rhythm that held for twelve weeks is a cue`() {
        val cues = cues(rhythmLog(), 77, 84)
        assertTrue(cues.hasStableRhythm)
        assertEquals(Weekday.WED, cues.strongestWeekday)
        assertEquals(1.0, cues.strongestWeekdayConfidence, 1e-9)
        assertEquals(PartOfDay.MORNING, cues.productiveBand)
        assertEquals(1.0, cues.productiveBandShare, 1e-9)
        assertEquals(PartOfDay.MORNING, cues.addingBand)
        assertTrue("nothing happened on a weekend", cues.weekdayOnly)
    }

    /**
     * A cue nobody has the data for is never guessed at.
     *
     * There are no focus sessions in the fixture, so both focus cues are null rather
     * than a weekday picked out of an all zero table.
     */
    @Test
    fun `a cue with no underlying events is null rather than arbitrary`() {
        val cues = cues(rhythmLog(), 77, 84)
        assertNull(cues.focusTypicalWeekday)
        assertNull(cues.focusTypicalBand)
    }

    /**
     * A pattern that does not hold in six of ten weeks is not a pattern.
     *
     * Saturday and Sunday tie at zero in every week but the first, so no week has a
     * quietest day of its own and the confidence never reaches 60 percent.
     */
    @Test
    fun `a tied quietest day never clears its confidence`() {
        assertNull(cues(rhythmLog(), 77, 84).quietestWeekday)
    }

    /** Under six weeks of data there are no cues at all, and layer six may not plan. */
    @Test
    fun `five weeks of data produces no cues`() {
        val cues = cues(rhythmLog(), 28, 35)
        assertEquals(CueFacts.NONE, cues)
        assertFalse(cues.hasStableRhythm)
    }
}
