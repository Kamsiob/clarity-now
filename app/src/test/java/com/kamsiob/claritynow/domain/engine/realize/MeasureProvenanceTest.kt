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
