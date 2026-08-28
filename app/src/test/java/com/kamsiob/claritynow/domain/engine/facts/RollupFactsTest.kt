package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FirstEver
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The four rollup lists that turn into a Pulse family.
 * CLARITY_LOGIC_ENGINE.md 3.1 and 6.1.
 *
 * Each one is the trigger of a family that says something about an area by name, so
 * a list that is slightly too generous is a sentence about the wrong area rather
 * than a rounding difference.
 */
class RollupFactsTest {

    private fun extract(log: TrailTestLog, fromDay: Int, toDay: Int): FactSet =
        FactExtractor(log.queries()).extract(window(fromDay, toDay))

    /**
     * Neglect needs a real history behind it, silence, and an area past its first
     * fortnight. All three, per 3.1.
     */
    @Test
    fun `neglect needs a history, a silence and an area that is not new`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "old", "Old", orderKey = "a0")
        log.area(at(0, 9, 1), "young", "Young", orderKey = "a1")
        for (index in 1..5) log.item(at(1, 9 + index), "o$index", "old", "Thing $index", areaName = "Old")
        // A brand new area with the same silence. New areas are never neglected.
        log.area(at(19, 9), "fresh", "Fresh", orderKey = "a2")

        val rollup = extract(log, 20, 27).rollup
        assertEquals(listOf("old"), rollup.neglectedAreaIds)
    }

    /**
     * A revival is measured from the area's own previous event, never from the
     * window start.
     */
    @Test
    fun `a revival is a real gap rather than an artifact of the window boundary`() {
        val log = TrailTestLog()
        log.area(at(0, 9), "work", "Work", orderKey = "a0")
        log.area(at(0, 9, 1), "steady", "Steady", orderKey = "a1")
        log.item(at(1, 9), "w1", "work", "Old thing")
        log.item(at(21, 9), "w2", "work", "New thing")
        // Steady was touched the day before the window, so its gap is one day.
        log.item(at(19, 9), "s1", "steady", "Yesterday", areaName = "Steady")
        log.item(at(21, 10), "s2", "steady", "Today", areaName = "Steady")

        val rollup = extract(log, 20, 27).rollup
        assertEquals(listOf("work"), rollup.dormantReturnedAreaIds)
    }

    /**
     * The list is `AreaFacts.queueDrainedFrom` with the corpus floor of three applied.
     * `QueueDrainFactsTest` carries the shapes the boundary pair could not see; this holds
     * the one it could, so the change is proved not to have moved it.
     */
    @Test
    fun `an area that went from three queued to none is drained`() {
        val log = drainLog()
        val facts = extract(log, 20, 27)
        assertEquals(listOf("drain"), facts.rollup.queueDrainedAreaIds)
        val drain = facts.areas.getValue("drain")
        assertEquals(3, drain.queueLengthAtWindowStart)
        assertEquals(0, drain.queueLength)
        assertEquals(-3, drain.queueDelta)
        assertEquals(
            "the fall began at or before the boundary here, so the two readings agree",
            3,
            drain.queueDrainedFrom,
        )
        assertTrue(FirstEver.FIRST_QUEUE_DRAIN in facts.history.firstEverFlags)
    }

    /** Three completions is the floor, and the middle value is the answer. */
    @Test
    fun `a median appears at three completions`() {
        val items = extract(drainLog(), 20, 27).items
        assertEquals(listOf(1, 3, 5), items.completedInWindow.map { it.daysActive }.sorted())
        assertEquals(3, items.medianDaysToComplete)
    }

    /**
     * An even count takes the mean of the two central values, rounded down.
     *
     * Rounding down never overstates how long things take, which is the direction a
     * sentence about somebody's own pace should err in.
     */
    @Test
    fun `an even count of completions rounds the median down`() {
        val log = drainLog()
        log.item(at(21, 15), "extra", "drain", "Fourth", areaName = "Drain")
        log.promote(at(23, 15), "extra", "drain", "Fourth", areaName = "Drain")
        log.complete(at(23, 16), "extra", "drain", "Fourth", areaName = "Drain", activeDurationDays = 6)

        val items = extract(log, 20, 27).items
        assertEquals(listOf(1, 3, 5, 6), items.completedInWindow.map { it.daysActive }.sorted())
        assertEquals("the mean of 3 and 5 is 4", 4, items.medianDaysToComplete)
    }

    /** Three queued at the window start, all finished inside it, nothing left behind. */
    private fun drainLog(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), "drain", "Drain")
        for (index in 1..3) {
            log.item(at(1, 9 + index), "d$index", "drain", "Thing $index", areaName = "Drain")
        }
        val durations = listOf(1, 3, 5)
        for (index in 1..3) {
            log.promote(at(21, 9 + index), "d$index", "drain", "Thing $index", areaName = "Drain")
            log.complete(
                at(21, 9 + index, 30),
                "d$index",
                "drain",
                "Thing $index",
                areaName = "Drain",
                activeDurationDays = durations[index - 1],
            )
        }
        return log
    }
}
