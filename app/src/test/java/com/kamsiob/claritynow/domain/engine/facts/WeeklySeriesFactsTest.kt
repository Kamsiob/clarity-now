package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The weekly series the report pattern families were waiting on, and the two per
 * area facts the Pulse families were.
 *
 * Every series here is built over the same seven day buckets anchored at the window
 * end that `HistoryFacts` already documents, so the fixtures below all use a window
 * of days 21 to 28 and the four buckets that fall out of it: days 0 to 6, 7 to 13,
 * 14 to 20 and 21 to 27, oldest first. A series that quietly used a different
 * bucketing would still look plausible on screen, which is why the expected lists
 * here are written out in full rather than computed.
 */
class WeeklySeriesFactsTest {

    private fun extract(log: TrailTestLog, fromDay: Int, toDay: Int): FactSet =
        FactExtractor(log.queries()).extract(window(fromDay, toDay))

    private fun twoAreas(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "health", "Health", orderKey = "a1")
        return log
    }

    // Areas per week -----------------------------------------------------------

    @Test
    fun `the area count series counts the areas that moved in each week`() {
        val log = twoAreas()
        // Week three: both areas. Week two: one. Week one: one.
        log.item(at(7, 10), "w1", "work", "Work one")
        log.item(at(8, 10), "h1", "health", "Health one", areaName = "Health")
        log.item(at(14, 10), "w2", "work", "Work two")
        log.item(at(21, 10), "w3", "work", "Work three")

        val facts = extract(log, 21, 28)
        // The oldest bucket holds the two area creations, which are events in them.
        assertEquals(listOf(2, 2, 1, 1), facts.history.weekAreaCountSeries)
        assertEquals(
            "the newest entry is the number the report prints beside it",
            facts.rollup.areasWithEvents,
            facts.history.weekAreaCountSeries.last(),
        )
    }

    /**
     * An area the person has since deleted is not counted in the weeks it moved.
     *
     * It is absent from `FactSet.areas` and unnameable, so counting it three weeks
     * ago would print a number that cannot be reconciled with anything the app is
     * willing to show. The count is of the areas they still have.
     */
    @Test
    fun `an area deleted since is absent from every bucket of the count`() {
        val log = twoAreas()
        log.area(at(0, 9, 2), "reading", "Reading", orderKey = "a2")
        log.item(at(7, 10), "w1", "work", "Work one")
        log.item(at(7, 11), "r1", "reading", "Reading one", areaName = "Reading")
        log.item(at(21, 10), "w3", "work", "Work three")
        log.deleteArea(day = 22, areaId = "reading", name = "Reading")

        // Reading moved in the oldest bucket, in the one after it, and again on the
        // day it was deleted. It is counted in none of them.
        val history = extract(log, 21, 28).history
        assertEquals(listOf(2, 1, 0, 1), history.weekAreaCountSeries)
    }

    // One area's own weeks -----------------------------------------------------

    /**
     * The per area series, which is what `comebackPattern` counts returns in.
     *
     * The family claims an area has gone quiet and returned **twice**, and a second
     * return is a second transition from a zero bucket to a non zero one. Nothing
     * else in the fact set can see past this window.
     */
    @Test
    fun `an area carries its own events per week`() {
        val log = twoAreas()
        log.item(at(2, 10), "w0", "work", "Work zero")
        log.item(at(9, 10), "w1", "work", "Work one")
        log.item(at(9, 11), "w2", "work", "Work two")
        // Nothing in Work in the third bucket at all.
        log.item(at(15, 10), "h1", "health", "Health one", areaName = "Health")
        log.item(at(23, 10), "w3", "work", "Work three")

        val areas = extract(log, 21, 28).areas
        // The area creation on day zero is an event in the area, so the oldest
        // bucket holds two for Work: the creation and the item.
        assertEquals(listOf(2, 2, 0, 1), areas.getValue("work").weekEventsSeries)
        assertEquals(listOf(1, 0, 1, 0), areas.getValue("health").weekEventsSeries)
    }

    // Focus per week -----------------------------------------------------------

    /**
     * Started, finished and ended early are three series and never two.
     *
     * `abandonmentPattern` claims more sessions ended early than finished, which is
     * the third against the second. Inferring it from started minus finished would
     * count a session whose process was killed as an ending the person chose.
     */
    @Test
    fun `the focus series separate what started, what finished and what ended early`() {
        val log = twoAreas()
        log.item(at(1, 10), "w1", "work", "Work one")
        log.promote(at(1, 11), "w1", "work", "Work one")

        log.focusRun(day = 8, sessionId = "s1", areaId = "work", itemId = "w1")
        log.focusRun(day = 9, sessionId = "s2", areaId = "work", itemId = "w1")
        log.focusRun(day = 15, sessionId = "s3", areaId = "work", itemId = "w1")
        log.focusRun(day = 16, sessionId = "s4", areaId = "work", itemId = "w1", finished = false)
        log.focusRun(day = 22, sessionId = "s5", areaId = "work", itemId = "w1")
        log.focusRun(day = 23, sessionId = "s6", areaId = "work", itemId = "w1", finished = false)
        log.focusRun(day = 24, sessionId = "s7", areaId = "work", itemId = "w1", finished = false)

        val history = extract(log, 21, 28).history
        assertEquals(listOf(0, 2, 2, 3), history.weekFocusStartedSeries)
        assertEquals(listOf(0, 2, 1, 1), history.weekFocusCompletedSeries)
        assertEquals(listOf(0, 0, 1, 2), history.weekFocusEndedEarlySeries)
    }

    // Weekends -----------------------------------------------------------------

    /**
     * A bucket is seven consecutive days, so it holds one Saturday and one Sunday
     * however it is aligned, and a bucket at zero is a weekend with nothing in it.
     *
     * The fixture's day zero is a Sunday, so days 21 and 27 are the weekend of the
     * newest bucket and days 14 and 20 are the weekend of the one before it.
     */
    @Test
    fun `the weekend series counts only the Saturday and the Sunday of each bucket`() {
        val log = twoAreas()
        log.item(at(20, 10), "w1", "work", "Saturday")
        log.item(at(23, 10), "w2", "work", "Wednesday")
        log.item(at(24, 10), "w3", "work", "Thursday")
        log.item(at(27, 10), "w4", "work", "Saturday again")

        val history = extract(log, 21, 28).history
        assertEquals(listOf(2, 0, 1, 1), history.weekWeekendEventsSeries)
        assertEquals(
            "a weekend count is never more than the week that holds it",
            listOf(true, true, true, true),
            history.weekWeekendEventsSeries
                .zip(history.weekTotalEventsSeries) { weekend, total -> weekend <= total },
        )
    }

    @Test
    fun `four weeks with nothing on a weekend read as four zeros`() {
        val log = TrailTestLog()
        // The areas are created on the Monday. Day zero of the fixture is a Sunday,
        // and an area created then would be a weekend event.
        log.area(at(1, 9), "work", "Work", orderKey = "a0")
        log.area(at(1, 9, 1), "health", "Health", orderKey = "a1")
        // Every event on a Wednesday, which is day three of each bucket here.
        for (day in listOf(3, 10, 17, 24)) log.item(at(day, 10), "w$day", "work", "Midweek $day")

        val history = extract(log, 21, 28).history
        assertEquals(listOf(0, 0, 0, 0), history.weekWeekendEventsSeries)
    }

    // The two per area Pulse facts ---------------------------------------------

    @Test
    fun `swaps are counted for the area they happened in`() {
        val log = twoAreas()
        log.item(at(21, 9), "w1", "work", "First")
        log.item(at(21, 9, 1), "w2", "work", "Second")
        log.item(at(21, 9, 2), "h1", "health", "Health first", areaName = "Health")
        log.item(at(21, 9, 3), "h2", "health", "Health second", areaName = "Health")
        log.promote(at(22, 10), "w1", "work", "First")
        log.promote(at(22, 11), "w2", "work", "Second", demotedItemId = "w1")
        log.promote(at(23, 10), "w1", "work", "First", demotedItemId = "w2")
        log.promote(at(24, 10), "h1", "health", "Health first", areaName = "Health")
        log.promote(
            at(24, 11),
            "h2",
            "health",
            "Health second",
            areaName = "Health",
            demotedItemId = "h1",
        )

        val facts = extract(log, 21, 28)
        assertEquals(2, facts.areas.getValue("work").swapsInWindow)
        assertEquals(1, facts.areas.getValue("health").swapsInWindow)
        assertEquals(
            "the window count is the sum, which is what made it the wrong fact",
            3,
            facts.window.swaps,
        )
    }

    /**
     * The dormancy an area returned **from**, which `daysSinceLastEvent` cannot be.
     *
     * That field is zero the moment the area moves, so it answers how long the area
     * has been quiet since the return. This one is measured from the area's own
     * previous event to its first event inside the window.
     */
    @Test
    fun `an area that came back reports how long it had been still`() {
        val log = twoAreas()
        log.item(at(10, 10), "w1", "work", "Before the gap")
        log.item(at(21, 10), "w2", "work", "After the gap")

        val work = extract(log, 21, 28).areas.getValue("work")
        assertEquals(11, work.dormantDaysBeforeReturn)
        assertEquals(
            "days since last event counts forward from the return, not back to before it",
            6,
            work.daysSinceLastEvent,
        )
        assertEquals(
            "the other end of the same gap: the day the area was last active before it",
            dateKey(10),
            work.dormancyStartKey,
        )
    }

    @Test
    fun `an area with nothing in the window has not come back from anything`() {
        val log = twoAreas()
        log.item(at(10, 10), "w1", "work", "Before")

        val work = extract(log, 21, 28).areas.getValue("work")
        assertNull(work.dormantDaysBeforeReturn)
        assertNull("the length and the date go absent together", work.dormancyStartKey)
    }

    @Test
    fun `an area whose first event is in the window is a fresh start rather than a return`() {
        val log = TrailTestLog()
        log.area(at(21, 9), "new", "New")
        log.item(at(22, 10), "n1", "new", "First thing", areaName = "New")

        val facts = extract(log, 21, 28)
        assertNull(facts.areas.getValue("new").dormantDaysBeforeReturn)
        assertEquals(emptyList<String>(), facts.rollup.dormantReturnedAreaIds)
        assertEquals(listOf("new"), facts.rollup.freshStartAreaIds)
    }

    @Test
    fun `the revival list is the dormancy fact with the corpus floor applied`() {
        val log = twoAreas()
        // Work returns after eleven days. Health returns after two, which is not a
        // revival: CORPUS_1_PULSE.md sets the floor at five.
        log.item(at(10, 10), "w1", "work", "Before the gap")
        log.item(at(21, 10), "w2", "work", "After the gap")
        log.item(at(19, 10), "h1", "health", "Before", areaName = "Health")
        log.item(at(21, 11), "h2", "health", "After", areaName = "Health")

        val facts = extract(log, 21, 28)
        assertEquals(2, facts.areas.getValue("health").dormantDaysBeforeReturn)
        assertEquals(listOf("work"), facts.rollup.dormantReturnedAreaIds)
    }

    // The dates behind the series ---------------------------------------------

    /**
     * Every weekly series is dated, and the dates are the buckets the numbers came from.
     *
     * A window of days 21 to 28 gives four buckets, days 0 to 6, 7 to 13, 14 to 20 and
     * 21 to 27, and the keys are their first days. This is asserted rather than derived
     * because the whole value of the field is that nothing outside the extractor has to
     * know the rule `endDate - 7k - 6`; a series that quietly used a different anchor
     * would print a plausible month beside a claim about a trend.
     */
    @Test
    fun `the week start keys date each bucket and line up with every other series`() {
        val log = twoAreas()
        log.item(at(7, 10), "w1", "work", "Week two")
        log.item(at(21, 10), "w2", "work", "This week")

        val history = extract(log, 21, 28).history
        assertEquals(
            listOf(dateKey(0), dateKey(7), dateKey(14), dateKey(21)),
            history.weekStartKeySeries,
        )
        assertEquals(
            "a key per bucket, so an offset means one week in the dates and in the numbers",
            history.weekTotalEventsSeries.size,
            history.weekStartKeySeries.size,
        )
    }

    /**
     * The week that beat this one, as a date and as a length, from one measurement.
     *
     * Three completions in the bucket two weeks back and one in this one, with the bucket
     * between them empty, so the newest week that beats this one is the older of the two
     * and both facts have to name it.
     */
    @Test
    fun `the weeks since the better week count back to the week the key names`() {
        val log = twoAreas()
        log.item(at(7, 9), "a", "work", "A")
        log.item(at(7, 9, 1), "b", "work", "B")
        log.item(at(7, 9, 2), "c", "work", "C")
        log.complete(at(8, 10), "a", "work", "A")
        log.complete(at(9, 10), "b", "work", "B")
        log.complete(at(10, 10), "c", "work", "C")
        log.item(at(21, 9), "d", "work", "D")
        log.complete(at(22, 10), "d", "work", "D")

        val history = extract(log, 21, 28).history
        assertEquals(listOf(0, 3, 0, 1), history.weekCompletionsSeries)
        assertEquals(dateKey(7), history.mostRecentBetterWeekKey)
        assertEquals(
            "the key and the length are one bucket read two ways",
            history.weekStartKeySeries.indexOf(history.mostRecentBetterWeekKey),
            history.weekStartKeySeries.size - 1 - requireNotNull(history.weeksSinceBetterWeek),
        )
        assertEquals(2, history.weeksSinceBetterWeek)
    }

    @Test
    fun `a week nothing earlier beat has no length to count back`() {
        val log = twoAreas()
        log.item(at(21, 9), "d", "work", "D")
        log.complete(at(22, 10), "d", "work", "D")

        val history = extract(log, 21, 28).history
        assertNull(history.mostRecentBetterWeekKey)
        assertNull(history.weeksSinceBetterWeek)
    }

    // Days with focus in them --------------------------------------------------

    /**
     * Focus days are days and not sessions.
     *
     * Three sessions across two days. `Focused time appeared on {n} different days` and
     * `{sessions} focus sessions` are two readings of one week and the corpus says both,
     * so the two counts have to be able to disagree.
     */
    @Test
    fun `the focus day count counts days rather than sessions`() {
        val log = twoAreas()
        log.item(at(21, 9), "w1", "work", "Thing")
        log.add(at(22, 9), FocusStarted("f1", "work", "w1", 1_500))
        log.add(at(22, 14), FocusStarted("f2", "work", "w1", 1_500))
        log.add(at(24, 9), FocusStarted("f3", "work", "w1", 1_500))

        val window = extract(log, 21, 28).window
        assertEquals(3, window.focusStarted)
        assertEquals(2, window.focusDays)
    }

    @Test
    fun `a week with no focus in it has no focus days`() {
        val log = twoAreas()
        log.item(at(21, 9), "w1", "work", "Thing")

        assertEquals(0, extract(log, 21, 28).window.focusDays)
    }

    // What was finished while one thing sat ------------------------------------

    /**
     * Completions since the active item became active, counted across the whole log.
     *
     * The span is the item's own age rather than the window, because the age it is set
     * against in `You have finished {n} other things since {itemTitle} became active` is
     * the item's whole age. A completion before the promotion is outside the claim.
     */
    @Test
    fun `the count of what was finished elsewhere runs from the promotion`() {
        val log = twoAreas()
        log.item(at(5, 9), "early", "work", "Early")
        log.complete(at(6, 10), "early", "work", "Early")
        log.item(at(9, 9), "sitter", "work", "The one that sits")
        log.promote(at(10, 9), "sitter", "work", "The one that sits")
        log.item(at(11, 9), "x1", "work", "X one")
        log.complete(at(12, 10), "x1", "work", "X one")
        log.item(at(20, 9), "x2", "work", "X two")
        log.complete(at(22, 10), "x2", "work", "X two")

        val work = extract(log, 21, 28).areas.getValue("work")
        assertEquals("sitter", work.activeItemId)
        assertEquals(
            "the completion before the promotion is outside the span the sentence names",
            2,
            work.completionsSinceActiveItemStarted,
        )
    }

    @Test
    fun `an area with nothing active counts nothing`() {
        val log = twoAreas()
        log.item(at(21, 9), "w1", "work", "Queued and never promoted")

        assertEquals(0, extract(log, 21, 28).areas.getValue("work").completionsSinceActiveItemStarted)
    }
}
