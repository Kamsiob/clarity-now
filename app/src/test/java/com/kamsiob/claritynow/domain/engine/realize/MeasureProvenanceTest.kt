package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.AnsweredPulse
import com.kamsiob.claritynow.domain.engine.CompletedItem
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CLARITY_LOGIC_ENGINE.md 8 check 3, at the layer that makes it possible.
 *
 * The validator re-reads the fact behind every rendered number and compares. That only
 * works if a `FactRef` is an address rather than a label, so this walks every numeric
 * measure the engine has, reads it, turns it into a ref, reads the ref back, and asserts
 * the two agree. A measure that failed this would give the validator a number it could not
 * check, and check 3 would have to veto a sentence that was true.
 */
class MeasureProvenanceTest {

    private val facts: FactSet = rich()

    @Test
    fun `every numeric measure re-reads to the same value through its own FactRef`() {
        var checked = 0
        for (measure in Measures.NUMERIC) {
            for (entity in entitiesFor(measure)) {
                val value = measure.read(facts, entity, EngineFacts.ZONE) as? MeasureValue.Number ?: continue
                val ref = measure.refFor(entity)
                assertEquals(
                    "${measure.id} for $entity does not re-read through $ref",
                    value.value,
                    FactLookup.readNumber(facts, ref, EngineFacts.ZONE),
                )
                checked++
            }
        }
        assertTrue("no numeric measure produced a value, so this test proved nothing", checked > 20)
    }

    @Test
    fun `a ref names the measure and the entity it was read for`() {
        val measure = requireNotNull(Measures.byId("areaEvents"))
        val ref = measure.refFor("area-work")
        assertEquals("area", ref.category)
        assertEquals("areaEvents:area-work", ref.path)
        assertEquals(measure.id, FactLookup.measureOf(ref)?.id)
        assertEquals("area-work", FactLookup.entityOf(ref))
    }

    @Test
    fun `a ref whose category does not match the measure resolves to nothing`() {
        val wrong = FactRef("window", "areaEvents:area-work")
        assertNull(FactLookup.measureOf(wrong))
        assertNull(FactLookup.readNumber(facts, wrong, EngineFacts.ZONE))
    }

    @Test
    fun `zero never leaves a measure, so it can never reach a template`() {
        val empty = EngineFacts.factSet(window = EngineFacts.window(totalEvents = 0, completions = 0))
        for (measure in Measures.NUMERIC) {
            val value = measure.read(empty, null, EngineFacts.ZONE)
            if (value is MeasureValue.Number) {
                assertTrue("${measure.id} produced ${value.value} on an empty window", value.value > 0)
            }
        }
    }

    @Test
    fun `an area that has never had an event does not produce a day count`() {
        val never = EngineFacts.factSet(
            areas = listOf(EngineFacts.area("cold", daysSinceLastEvent = Int.MAX_VALUE)),
        )
        val measure = requireNotNull(Measures.byId("areaDaysSinceLastEvent"))
        assertNull(measure.read(never, "cold", EngineFacts.ZONE))
    }

    @Test
    fun `a name measure records the entity it named`() {
        val name = requireNotNull(Measures.byId("areaName")).read(facts, "work", EngineFacts.ZONE)
        assertEquals("Work", (name as MeasureValue.Text).value)
        assertEquals("work", name.namedArea)

        val title = requireNotNull(Measures.byId("itemTitle")).read(facts, "item-1", EngineFacts.ZONE)
        assertEquals("item-1", (title as MeasureValue.Text).namedItem)

        // Read for an item, and it names the area holding it. The realizer cannot work this
        // out for itself, which is why the measure records it.
        val holder = requireNotNull(Measures.byId("itemAreaName")).read(facts, "item-1", EngineFacts.ZONE)
        assertEquals("work", (holder as MeasureValue.Text).namedArea)
        assertNull(holder.namedItem)
    }

    @Test
    fun `a month reference renders a month name and carries the key it came from`() {
        val since = requireNotNull(Measures.byId("mostRecentBetterWeekRef")).read(facts, null, EngineFacts.ZONE)
        assertEquals("January", (since as MeasureValue.Date).display)
        assertEquals("2026-01-11", since.weekKey)
    }

    @Test
    fun `an area with no events in the window cannot be named as an earlier week's leader`() {
        // The name would be true and validator check 1 would veto the whole candidate, so
        // the measure declines and the bench offers a line without a name in it.
        assertNotNull(Measures.byId("dominantAreaAgo")?.read(facts, "0", EngineFacts.ZONE))
        assertNull(Measures.byId("dominantAreaAgo")?.read(facts, "2", EngineFacts.ZONE))
    }

    /**
     * The dated series read at an offset, which is what every `since {sinceRef}` in a
     * report pattern comes from.
     *
     * Offset zero is the week being described and offset three is three buckets back, the
     * same direction every other offset measure counts in. Past the end of the history it
     * answers null rather than the oldest bucket it has, so a family whose rule reaches
     * further than the person's history drops the line instead of naming the wrong week.
     */
    @Test
    fun `a numbered week back renders the month that week began in`() {
        val dated = datedWeeks(listOf(4, 5, 6, 7))
        val measure = requireNotNull(Measures.byId("weekRefAgo"))
        assertEquals("March", (measure.read(dated, "0", EngineFacts.ZONE) as MeasureValue.Date).display)
        assertEquals("February", (measure.read(dated, "3", EngineFacts.ZONE) as MeasureValue.Date).display)
        assertNull("a week older than the history is not a week", measure.read(dated, "4", EngineFacts.ZONE))
        assertNull("an offset that is not a number addresses nothing", measure.read(dated, "x", EngineFacts.ZONE))
    }

    /**
     * `What is waiting has doubled since {sinceRef}` finds the nearest week it is true of.
     *
     * Queues of three, two, five and eight. Two weeks back holds five and doubling that
     * would be ten, so the claim is not true of it; three weeks back holds two, and the
     * nearest week the doubling actually happened since is the one the sentence names.
     */
    @Test
    fun `the doubling reference is the newest week the doubling is true of`() {
        val measure = requireNotNull(Measures.byId("queueDoubledSinceRef"))
        val doubled = measure.read(datedWeeks(listOf(3, 2, 5, 8)), null, EngineFacts.ZONE)
        assertEquals("February", (doubled as MeasureValue.Date).display)
        assertEquals(EngineFacts.dateKey(-14), doubled.weekKey)
        assertNull(
            "a queue that grew steadily never doubled from any week in the history",
            measure.read(datedWeeks(listOf(6, 7, 8, 9)), null, EngineFacts.ZONE),
        )
        assertNull(
            "one thing becoming three is a doubling by arithmetic and not a sentence",
            measure.read(datedWeeks(listOf(1, 1, 2, 3)), null, EngineFacts.ZONE),
        )
    }

    /** The month a gap started in, which is the other end of the gap `{ageDays}` renders. */
    @Test
    fun `a returned area names the month it was last active in before the gap`() {
        val returned = EngineFacts.factSet(
            areas = listOf(EngineFacts.area("home", "Home", events = 3, dormantDaysBeforeReturn = 9)),
        )
        val measure = requireNotNull(Measures.byId("areaDormancyStartRef"))
        val ref = measure.read(returned, "home", EngineFacts.ZONE) as MeasureValue.Date
        assertEquals(EngineFacts.dateKey(-9), ref.weekKey)
        assertNull(
            "an area that did not come back has no gap to date",
            measure.read(EngineFacts.factSet(areas = listOf(EngineFacts.area("home", "Home"))), "home", EngineFacts.ZONE),
        )
    }

    /** The week that beat this one, as a length, beside the same week as a date. */
    @Test
    fun `the weeks since the better week read back through their own ref`() {
        val dated = datedWeeks(listOf(4, 5, 6, 7), betterWeekKey = EngineFacts.dateKey(-14))
        val measure = requireNotNull(Measures.byId("weeksSinceBetterWeek"))
        assertEquals(MeasureValue.Number(2), measure.read(dated, null, EngineFacts.ZONE))
        assertEquals(2, FactLookup.readNumber(dated, measure.refFor(null), EngineFacts.ZONE))
    }

    /** Four dated weekly buckets ending on day zero, with the queue sizes a test names. */
    private fun datedWeeks(queues: List<Int>, betterWeekKey: String? = null): FactSet =
        EngineFacts.factSet(
            history = EngineFacts.history(
                daysSinceInstall = 90,
                weekQueueSizes = queues,
                weekTotalEvents = queues,
                mostRecentBetterWeekKey = betterWeekKey,
            ),
        )

    // ------------------------------------------------------------------ fixture

    private fun entitiesFor(measure: Measure): List<String?> = when (measure.scope) {
        MeasureScope.WINDOW -> listOf(null)
        MeasureScope.AREA -> facts.areas.keys.toList()
        MeasureScope.ITEM -> facts.areas.values.mapNotNull { it.activeItemId }
        MeasureScope.OFFSET -> listOf("0", "1", "2")
        MeasureScope.LABEL -> facts.pulse.recentAnswers.map { it.responseLabel }.distinct()
    }

    /** One fact set with something in every corner, so no measure is skipped for want of data. */
    private fun rich(): FactSet {
        val work = EngineFacts.area(
            areaId = "work",
            name = "Work",
            events = 9,
            completions = 4,
            additions = 5,
            share = 0.6,
            activeItemId = "item-1",
            activeItemTitle = "Rewrite the proposal intro",
            activeItemAgeDays = 9,
            queueLength = 3,
            queueLengthAtWindowStart = 5,
            daysSinceLastEvent = 1,
            focusSessions = 2,
            focusMinutes = 50,
        )
        val health = EngineFacts.area(
            areaId = "health",
            name = "Health",
            events = 6,
            completions = 2,
            additions = 4,
            share = 0.4,
            activeItemId = "item-2",
            activeItemTitle = "Book the eye test",
            activeItemAgeDays = 3,
            queueLength = 2,
            queueLengthAtWindowStart = 1,
            daysSinceLastEvent = 2,
        )
        val quiet = EngineFacts.area(areaId = "personal", name = "Personal", daysSinceLastEvent = 30, ageDays = 200)
        val answers = listOf(
            AnsweredPulse(EngineFacts.dateKey(1), "persistence", "item-1", "persistence.s2.r01.1", "Deep work", true),
            AnsweredPulse(EngineFacts.dateKey(3), "persistence", "item-1", "persistence.s2.r01.1", "Deep work", true),
            AnsweredPulse(EngineFacts.dateKey(4), "concentration", "work", "concentration.s1.r01.2", "Narrow", false),
        )
        return EngineFacts.factSet(
            window = EngineFacts.window(
                startDay = 0,
                endDay = 7,
                totalEvents = 15,
                completions = 6,
                additions = 9,
                swaps = 2,
                focusStarted = 4,
                focusCompleted = 2,
                focusEndedEarly = 2,
                focusMinutes = 50,
                activeDays = 5,
                busiestDayKey = EngineFacts.dateKey(2),
                busiestDayCount = 6,
            ),
            areas = listOf(work, health, quiet),
            dominantAreaId = "work",
            history = EngineFacts.history(
                daysSinceInstall = 120,
                lastWeekCompletions = 4,
                weekCompletions = listOf(2, 3, 4, 6),
                weekQueueSizes = listOf(4, 5, 6, 5),
                weekTotalEvents = listOf(9, 11, 13, 15),
                dominantAreaLastThreeWeeks = listOf("personal", "health", "work"),
                personalBestWeekCompletions = 8,
                personalBestWeekKey = "2026-01-04",
                weeksSincePersonalBest = 8,
                mostRecentBetterWeekKey = "2026-01-11",
                longestEverActiveDays = 40,
                longestEverActiveItemId = "item-9",
                personalBestFocusMinutesWeek = 90,
            ),
            pulse = EngineFacts.pulse(
                answeredLifetime = 12,
                answeredInWindow = 3,
                positiveInWindow = 2,
                flaggedInWindow = 1,
                recentAnswers = answers,
            ),
            items = EngineFacts.items(
                areas = mapOf("work" to work, "health" to health, "personal" to quiet),
                completed = listOf(
                    CompletedItem("done-1", "Send the invoice", "work", "Work", 4),
                    CompletedItem("done-2", "Fix the tap", "health", "Health", 9),
                ),
                medianDaysToComplete = 6,
            ),
        )
    }
}
