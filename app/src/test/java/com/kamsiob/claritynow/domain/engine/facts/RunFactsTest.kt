package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.unfiled
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The two capped current runs, which are the scoped exception to the streak ban.
 *
 * Read the class note on `HistoryFacts` first. These tests exist to hold the shape
 * the exception was granted in: a run of **absence**, ending today, capped, with no
 * series behind it. Each test below is one of the ways a run could quietly become
 * something else, and the last two are the ones that would turn a true sentence into
 * a false one.
 */
class RunFactsTest {

    private fun history(log: TrailTestLog, fromDay: Int, toDay: Int): HistoryFacts =
        FactExtractor(log.queries()).extract(window(fromDay, toDay)).history

    // The quiet run ------------------------------------------------------------

    @Test
    fun `the quiet run counts back from the last day the window describes`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.item(at(1, 10), "i1", "work", "Something")

        // Days two through five held nothing, and the window describes day five.
        assertEquals(4, history(log, 5, 6).currentQuietRunDays)
    }

    /**
     * One event ends the run, even though the `quietDay` family would still fire.
     *
     * The family triggers on fewer than two events in the window and the run counts
     * days with none, and the difference is deliberate. Every statement authored at
     * stages 2 and 3 claims nothing moved, and a run built from the family's own
     * threshold would print "Nothing has moved in three days" at somebody who did one
     * thing on each of them.
     */
    @Test
    fun `a day holding one event ends the run rather than counting as quiet`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.item(at(1, 10), "i1", "work", "Something")
        log.item(at(3, 10), "i2", "work", "One thing")

        assertEquals(2, history(log, 5, 6).currentQuietRunDays)
    }

    @Test
    fun `the run stops at the oldest event rather than counting days before the app existed`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")

        // Days one and two are quiet. The day the log begins is not.
        assertEquals(2, history(log, 2, 3).currentQuietRunDays)
    }

    @Test
    fun `an empty log has not gone quiet`() {
        val log = TrailTestLog()

        assertEquals(0, history(log, 5, 6).currentQuietRunDays)
    }

    @Test
    fun `activity on the last day the window describes leaves no run at all`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.item(at(5, 10), "i1", "work", "Today")

        assertEquals(0, history(log, 5, 6).currentQuietRunDays)
    }

    /**
     * The cap, which is what stops the number becoming a record.
     *
     * A value at the cap says at least this many days and nothing more. Sixty days of
     * silence and thirty read alike here on purpose: there is no personal best to
     * lose, so nothing downstream can be built that a person would want to protect.
     */
    @Test
    fun `a very long silence is capped rather than counted`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")

        assertEquals(HistoryFacts.MAX_RUN_DAYS, history(log, 60, 61).currentQuietRunDays)
    }

    // The single area run ------------------------------------------------------

    @Test
    fun `the single area run counts days one area held everything, and names it`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "health", "Health", orderKey = "a1")
        for (day in 1..3) log.item(at(day, 10), "w$day", "work", "Work thing $day")

        val history = history(log, 3, 4)
        assertEquals(3, history.currentSingleAreaRunDays)
        assertEquals("work", history.currentSingleAreaRunAreaId)
    }

    @Test
    fun `a day another area moved ends the run`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "health", "Health", orderKey = "a1")
        for (day in 1..3) log.item(at(day, 10), "w$day", "work", "Work thing $day")
        log.item(at(2, 11), "h1", "health", "Health thing", areaName = "Health")

        assertEquals(1, history(log, 3, 4).currentSingleAreaRunDays)
    }

    /**
     * An unfiled capture ends the run, because it happened outside every area.
     *
     * `concentration.s3.01` reads "Everything yesterday was {areaName}", and that is
     * false on a day something was captured into the inbox. The day's own total is
     * therefore compared against the area's count rather than against the number of
     * areas that moved, and an event belonging to no area breaks the equality.
     */
    @Test
    fun `an event in no area ends the run`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        for (day in 1..3) log.item(at(day, 10), "w$day", "work", "Work thing $day")
        log.unfiled(at(2, 11), "inbox-1", "A thought")

        assertEquals(1, history(log, 3, 4).currentSingleAreaRunDays)
    }

    @Test
    fun `a day with nothing ends the run rather than extending it`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        log.item(at(1, 10), "w1", "work", "Monday")
        log.item(at(3, 10), "w3", "work", "Wednesday")

        assertEquals(1, history(log, 3, 4).currentSingleAreaRunDays)
    }

    /**
     * The run survives the area being archived; the permission to name it does not.
     *
     * Prohibition 3 says an archived area may not be referenced, and it is enforced
     * by the area being absent from the fact set. The length is left alone because
     * those days did happen. What is missing is anybody to name them for, and a rule
     * that needs the subject cannot fire without one.
     */
    @Test
    fun `a run held by an archived area keeps its length and loses its subject`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "solo", "Solo", orderKey = "a0")
        // A second area, so the day both were created is not itself a single area day.
        log.area(at(0, 9, 1), "kept", "Kept", orderKey = "a1")
        for (day in 1..3) log.item(at(day, 10), "s$day", "solo", "Thing $day", areaName = "Solo")
        log.archiveArea(day = 3, areaId = "solo", name = "Solo", hour = 11)

        val history = history(log, 3, 4)
        assertEquals(3, history.currentSingleAreaRunDays)
        assertNull(
            "an archived area may not be named, however long it held the days",
            history.currentSingleAreaRunAreaId,
        )
    }

    @Test
    fun `a very long single area run is capped rather than counted`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work")
        for (day in 1..40) log.item(at(day, 10), "w$day", "work", "Thing $day")

        assertEquals(HistoryFacts.MAX_RUN_DAYS, history(log, 40, 41).currentSingleAreaRunDays)
    }

    /** The two runs are mutually exclusive: a day is either empty or held by an area. */
    @Test
    fun `a quiet run and a single area run are never both open`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "health", "Health", orderKey = "a1")
        for (day in 1..3) log.item(at(day, 10), "w$day", "work", "Thing $day")

        val holding = history(log, 3, 4)
        assertEquals(0, holding.currentQuietRunDays)
        assertEquals(3, holding.currentSingleAreaRunDays)

        val quiet = history(log, 6, 7)
        assertEquals(3, quiet.currentQuietRunDays)
        assertEquals(0, quiet.currentSingleAreaRunDays)
    }
}
