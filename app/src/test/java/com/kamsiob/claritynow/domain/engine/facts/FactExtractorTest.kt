package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FirstEver
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Trend
import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Layer one against a log a person could plausibly have produced.
 * CLARITY_LOGIC_ENGINE.md 3.
 *
 * The fixture is one week of two areas rather than a set of single fact cases,
 * because most of what can go wrong here is a fact that is individually plausible
 * and inconsistent with the fact beside it. Every expected number below was counted
 * by hand off the fixture, which is the only way this suite catches an extractor
 * that agrees with its own mistake.
 */
class FactExtractorTest {

    /**
     * Two areas, six days, one completion, one swap, one focus session.
     *
     * Days are local days in `TEST_ZONE`, which is deliberately not UTC, so an
     * implementation that divided milliseconds by the length of a day would fail
     * here rather than on somebody's phone.
     */
    private fun standardLog(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 5), "health", "Health", orderKey = "a1")
        log.item(at(1, 10), "i1", "work", "Rewrite the intro")
        log.promote(at(1, 10, 1), "i1", "work", "Rewrite the intro")
        log.item(at(2, 9), "i2", "work", "Second")
        log.item(at(3, 9), "i3", "work", "Third")
        log.complete(at(3, 11), "i1", "work", "Rewrite the intro", activeDurationDays = 2)
        log.promote(at(3, 11, 1), "i2", "work", "Second")
        log.item(at(4, 20), "h1", "health", "Walk", areaName = "Health")
        log.promote(at(4, 20, 1), "h1", "health", "Walk", areaName = "Health")
        log.focusRun(day = 5, sessionId = "s1", areaId = "work", itemId = "i2", hour = 9)
        log.promote(at(6, 9), "i3", "work", "Third", demotedItemId = "i2", demotedToOrderKey = "b0")
        return log
    }

    private fun facts(log: TrailTestLog, fromDay: Int, toDay: Int): FactSet =
        FactExtractor(log.queries()).extract(window(fromDay, toDay))

    @Test
    fun `the window is counted the way the facade counts it`() {
        val facts = facts(standardLog(), 1, 7)
        with(facts.window) {
            assertEquals("six local days", 6, dayCount)
            assertEquals(11, totalEvents)
            assertEquals(1, completions)
            assertEquals(4, additions)
            assertEquals(4, promotions)
            assertEquals(1, swaps)
            assertEquals(0, deletions)
            assertEquals(-3, netFlow)
            assertEquals(1, focusStarted)
            assertEquals(1, focusCompleted)
            assertEquals(0, focusEndedEarly)
            assertEquals(1500L, focusSecondsTotal)
            assertEquals(25, focusMinutesTotal)
            assertEquals(6, activeDays)
        }
    }

    @Test
    fun `the busiest day is the day with the most events`() {
        val facts = facts(standardLog(), 1, 7)
        assertEquals(dateKey(3), facts.window.busiestDayKey)
        assertEquals(3, facts.window.busiestDayCount)
    }

    /**
     * Every band is present, including the one nothing happened in.
     *
     * A missing band is a missing denominator term, and a share computed over the
     * bands that happen to be there does not reach a hundred with nothing on the
     * screen to explain why.
     */
    @Test
    fun `events are banded by local hour and every band is present`() {
        val bands = facts(standardLog(), 1, 7).window.eventsByPartOfDay
        assertEquals(PartOfDay.entries.toSet(), bands.keys)
        assertEquals(7, bands.getValue(PartOfDay.MORNING))
        assertEquals(2, bands.getValue(PartOfDay.AFTERNOON))
        assertEquals(2, bands.getValue(PartOfDay.EVENING))
        assertEquals(0, bands.getValue(PartOfDay.NIGHT))
        assertEquals(11, bands.values.sum())
    }

    @Test
    fun `an area carries its window counts, its queue and its snapshots`() {
        val work = facts(standardLog(), 1, 7).areas.getValue("work")
        assertEquals("Work", work.nameSnapshot)
        assertEquals(9, work.eventsInWindow)
        assertEquals(1, work.completionsInWindow)
        assertEquals(3, work.additionsInWindow)
        assertEquals(9.0 / 11.0, work.shareOfEvents, 1e-9)
        assertTrue(work.hasActiveItem)
        assertEquals("i3", work.activeItemId)
        assertEquals("Third", work.activeItemTitleSnapshot)
        assertEquals(0, work.activeItemAgeDays)
        assertEquals(1, work.queueLength)
        assertEquals(0, work.queueLengthAtWindowStart)
        assertEquals(1, work.queueDelta)
        assertEquals(0, work.daysSinceLastEvent)
        assertEquals(6, work.ageDays)
        assertTrue(work.isNew)
        assertEquals(1500L, work.focusSecondsInWindow)
        assertEquals(1, work.focusSessionsInWindow)
    }

    @Test
    fun `the rollup names one dominant area and no neglected ones`() {
        val rollup = facts(standardLog(), 1, 7).rollup
        assertEquals(2, rollup.areasTotal)
        assertEquals(2, rollup.areasWithEvents)
        assertEquals(0, rollup.areasIdle)
        assertEquals("work", rollup.dominantAreaId)
        assertEquals(9.0 / 11.0, rollup.dominantShare, 1e-9)
        assertEquals(emptyList<String>(), rollup.neglectedAreaIds)
        assertEquals(emptyList<String>(), rollup.dormantReturnedAreaIds)
        assertEquals(emptyList<String>(), rollup.queueDrainedAreaIds)
        assertEquals(listOf("work"), rollup.queueGrowingAreaIds)
        assertEquals(listOf("health", "work"), rollup.freshStartAreaIds)
    }

    @Test
    fun `items carry the active one per area and the completions with real names`() {
        val items = facts(standardLog(), 1, 7).items
        assertEquals(setOf("work", "health"), items.activeByArea.keys)
        assertEquals("Third", items.activeByArea.getValue("work").titleSnapshot)
        assertEquals("Health", items.activeByArea.getValue("health").areaNameSnapshot)
        assertEquals("h1", items.longestActiveItemId)
        assertEquals(2, items.longestActiveDays)
        assertEquals(listOf("i1"), items.completedInWindow.map { it.itemId })
        assertEquals(2, items.completedInWindow.single().daysActive)
    }

    /** Under three completions there is no typical time to finish, and none is claimed. */
    @Test
    fun `a median needs three completions`() {
        assertNull(facts(standardLog(), 1, 7).items.medianDaysToComplete)
    }

    @Test
    fun `a first is flagged only in the window it happened in`() {
        val log = standardLog()
        val inWindow = facts(log, 1, 7).history.firstEverFlags
        assertTrue(FirstEver.FIRST_COMPLETION in inWindow)
        assertTrue(FirstEver.FIRST_FOCUS_SESSION in inWindow)
        assertTrue(FirstEver.FIRST_SWAP in inWindow)
        assertTrue(FirstEver.FIRST_WEEK_WITH_ALL_AREAS_ACTIVE in inWindow)
        assertFalse(FirstEver.FIRST_AREA_ARCHIVED in inWindow)

        // The week after. Nothing new happened, so nothing is a first.
        val later = facts(log, 7, 14).history.firstEverFlags
        assertFalse(FirstEver.FIRST_COMPLETION in later)
        assertFalse(FirstEver.FIRST_FOCUS_SESSION in later)
        assertFalse(FirstEver.FIRST_SWAP in later)
        assertFalse(FirstEver.FIRST_WEEK_WITH_ALL_AREAS_ACTIVE in later)
    }

    @Test
    fun `history over one week of data has no trend and no personal best`() {
        val history = facts(standardLog(), 1, 7).history
        assertEquals(6, history.daysSinceInstall)
        assertEquals(0, history.weeksOfData)
        assertTrue(history.isFirstWeekEver)
        assertEquals(1, history.lifetimeCompletions)
        assertNull(history.lastWeekCompletions)
        assertNull(history.weekOverWeekDelta)
        assertEquals(Trend.INSUFFICIENT, history.completionsTrend)
        assertEquals(0, history.personalBestWeekCompletions)
        assertNull(history.personalBestWeekKey)
        assertNull(history.weeksSincePersonalBest)
        assertNull(history.mostRecentBetterWeekKey)
    }

    /** The record is held across both sides: what is active now, and what finished. */
    @Test
    fun `the longest ever active item reads both active and completed items`() {
        val history = facts(standardLog(), 1, 7).history
        assertEquals(2, history.longestEverActiveDays)
        assertEquals("h1", history.longestEverActiveItemId)
    }

    @Test
    fun `there are no cues before six weeks of data`() {
        val cues = facts(standardLog(), 1, 7).cues
        assertFalse(cues.hasStableRhythm)
        assertNull(cues.strongestWeekday)
        assertNull(cues.productiveBand)
        assertEquals(0.0, cues.strongestWeekdayConfidence, 0.0)
    }

    /**
     * Identical inputs produce an identical `FactSet`.
     *
     * The determinism requirement in 14, at the layer it starts at. Two extractions
     * over one log must be equal as values, which they are only if nothing inside
     * read a clock, a random number or an iteration order that could differ.
     */
    @Test
    fun `two extractions over one log are equal`() {
        val log = standardLog()
        assertEquals(facts(log, 1, 7), facts(log, 1, 7))
    }

    /**
     * The same events, arriving in a different order, extract to the same facts.
     *
     * `TrailQueries` sorts into total order at construction, so a log arriving out of
     * order from a merge cannot move a number. This is the property that makes two
     * devices agree, checked at layer one rather than assumed.
     */
    @Test
    fun `a reordered log extracts to the same facts`() {
        val log = standardLog()
        val forward = FactExtractor(log.queries()).extract(window(1, 7))
        val reversed = TrailQueries(log.events().reversed(), TEST_ZONE)
        assertEquals(forward, FactExtractor(reversed).extract(window(1, 7)))
    }

    @Test
    fun `an empty log extracts without a division by zero`() {
        val facts = FactExtractor(TrailTestLog().queries()).extract(window(0, 7))
        assertEquals(0, facts.window.totalEvents)
        assertNull(facts.window.busiestDayKey)
        assertEquals(0, facts.window.busiestDayCount)
        assertTrue(facts.areas.isEmpty())
        assertNull(facts.rollup.dominantAreaId)
        assertEquals(0.0, facts.rollup.dominantShare, 0.0)
        assertEquals(0, facts.history.daysSinceInstall)
        assertEquals(emptyList<Int>(), facts.history.weekCompletionsSeries)
        assertNull(facts.pulse.lastGeneratedFamily)
    }
}
