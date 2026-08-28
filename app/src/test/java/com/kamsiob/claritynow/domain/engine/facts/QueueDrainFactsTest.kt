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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `AreaFacts.queueDrainedFrom`, the fall the three drain families describe.
 * CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * Every case here is a queue that reads one way at the two window boundaries and another
 * way across the window, which is the whole reason the fact exists. A boundary pair says
 * five and nothing, and so does a week that opened at five and a week that built five on
 * Tuesday; `{areaName}'s queue went from {n} to nothing` is true of both and
 * `queueLengthAtWindowStart` sees only the first. The tests are written as the shapes a
 * person's week can take rather than as the arithmetic, because the arithmetic is what was
 * right before and the shape is what was wrong.
 */
class QueueDrainFactsTest {

    private fun extract(log: TrailTestLog): FactSet =
        FactExtractor(log.queries()).extract(window(WINDOW_FROM, WINDOW_TO))

    private fun drainOf(log: TrailTestLog): Int? =
        extract(log).areas.getValue(AREA).queueDrainedFrom

    /**
     * The shape the boundary pair could not see, and the reason for the whole change.
     *
     * Nothing was queued when the window opened and nothing is queued now, so
     * `queueLengthAtWindowStart` and `queueLength` are both zero and their difference is
     * zero. Three things were queued on Monday and worked through by Thursday, which is
     * `{areaName} finished everything it was holding` and an ordinary week.
     */
    @Test
    fun `a queue built and finished inside the window is a drain`() {
        val log = emptyArea()
        log.queue(21, 1..3)
        log.workThrough(22, 1..3)

        val facts = extract(log)
        val drain = facts.areas.getValue(AREA)
        assertEquals("the fall began at three, whatever the boundary held", 3, drain.queueDrainedFrom)
        assertEquals("the boundary pair sees nothing at either end", 0, drain.queueLengthAtWindowStart)
        assertEquals(0, drain.queueDelta)
        assertEquals(listOf(AREA), facts.rollup.queueDrainedAreaIds)
    }

    /**
     * The shape that already worked still works, and reads the same number it always did.
     *
     * A queue standing at three when the window opened and worked to nothing inside it is a
     * fall that began at or before the boundary, so the fall's height and the boundary
     * reading agree. That agreement is what `SlotBindings` overrides `ob.drain.l01` on, the
     * one corpus line that says `on Sunday`.
     */
    @Test
    fun `a queue standing at the boundary and worked to nothing still reads its own height`() {
        val log = emptyArea()
        log.queue(1, 1..3)
        log.workThrough(21, 1..3)

        val drain = extract(log).areas.getValue(AREA)
        assertEquals(3, drain.queueDrainedFrom)
        assertEquals(
            "the fall began at the boundary, so the dated line is true of this week",
            drain.queueLengthAtWindowStart,
            drain.queueDrainedFrom,
        )
    }

    /**
     * An arrival ends a fall, and the answer is the fall that is still standing.
     *
     * Five went out on Tuesday, two arrived on Wednesday and went out on Thursday. The
     * queue is empty now and the largest thing it held this week was five, and
     * `queuedrain.s1.08`, *{n} things left {areaName}, and nothing replaced them*, is false
     * of five and true of two. Two is below the corpus floor of three, so the family stays
     * quiet about a week it has nothing exactly true to say about.
     */
    @Test
    fun `something arriving after a fall ends it, and the later fall is the answer`() {
        val log = emptyArea()
        log.queue(21, 1..5)
        log.workThrough(22, 1..5)
        log.queue(23, 6..7)
        log.workThrough(24, 6..7)

        val facts = extract(log)
        assertEquals(2, facts.areas.getValue(AREA).queueDrainedFrom)
        assertEquals(
            "five left and two replaced them, so no sentence on this bench is true of five",
            emptyList<String>(),
            facts.rollup.queueDrainedAreaIds,
        )
    }

    /**
     * The height is the top of the fall, not the top of the window.
     *
     * Three queued, two worked off, three more added, and the four that were then standing
     * all worked off. The fall that is still standing began at four, and four is what
     * `{areaName} went from {n} waiting to nothing` names.
     */
    @Test
    fun `the height is where the uninterrupted fall began`() {
        val log = emptyArea()
        log.queue(21, 1..3)
        log.workThrough(22, 1..2)
        log.queue(23, 4..6)
        log.workThrough(24, listOf(3, 4, 5, 6))

        assertEquals(4, drainOf(log))
    }

    /** A queue that is not empty now did not drain, whatever it did in between. */
    @Test
    fun `a queue holding something at the window end has not drained`() {
        val log = emptyArea()
        log.queue(21, 1..4)
        log.workThrough(22, 1..3)

        assertNull(drainOf(log))
    }

    /**
     * Nothing to fall from is null rather than zero, exactly as in
     * `dormantDaysBeforeReturn`, and a small fall is a number rather than a null.
     *
     * The two have to be different things. A rule reading this for a length must not be
     * able to render an absence as a number, so an area that never held anything answers
     * null. An area that held one thing and let it go did fall, from one, and the reason
     * nothing is said about it is the corpus floor of three in the rollup rather than a
     * measurement that pretended the fall did not happen. Keeping the floor out of the
     * measurement is what lets the fall's height be the number a sentence prints.
     */
    @Test
    fun `an area that never held a queue has no fall, and a fall of one is still a fall`() {
        val log = emptyArea()
        log.area(at(0, 9, 1), QUIET, "Quiet", orderKey = "a1")
        log.queue(21, 1..1)
        log.workThrough(22, 1..1)

        val facts = extract(log)
        assertNull(
            "an area with nothing in it did not drain, and must not read as a fall of zero",
            facts.areas.getValue(QUIET).queueDrainedFrom,
        )
        assertEquals(1, facts.areas.getValue(AREA).queueDrainedFrom)
        assertEquals(
            "one item let go is not the clean sweep this bench claims",
            emptyList<String>(),
            facts.rollup.queueDrainedAreaIds,
        )
    }

    /**
     * The fact says the queue fell and never says how, which is what the rules guard.
     *
     * A queue empties by deletion too, and this measurement cannot tell the difference. It
     * is not supposed to: `RuleBuilders.drainedByFinishing` requires the completions to
     * cover the fall, and it is carried by both drain families precisely because every
     * sentence on both benches claims somebody finished something. The value is asserted
     * here so that a future reader sees the fact is deliberately silent on the question.
     */
    @Test
    fun `a queue thrown away falls the same distance and carries no completions behind it`() {
        val log = emptyArea()
        log.queue(21, 1..3)
        for (index in 1..3) log.deleteItem(22, "d$index", AREA, "Thing $index", hour = 9 + index)

        val drain = extract(log).areas.getValue(AREA)
        assertEquals(3, drain.queueDrainedFrom)
        assertEquals(
            "nothing was finished, which is what the rule guard exists to catch",
            0,
            drain.completionsInWindow,
        )
    }

    /**
     * The history behind `FIRST_QUEUE_DRAIN` is read the way the present is.
     *
     * The flag licenses `{areaName} is completely clear for the first time` and *There is an
     * area with nothing in it for the first time*. An earlier week that built a queue on
     * Wednesday and finished it on Friday is a drain, and comparing that week's two
     * boundaries would miss it and announce a first that had already happened.
     */
    @Test
    fun `an earlier week that drained between its own boundaries is not a first`() {
        val log = emptyArea()
        log.queue(14, 1..3)
        log.workThrough(16, 1..3)
        log.queue(21, 4..6)
        log.workThrough(22, 4..6)

        val facts = extract(log)
        assertEquals(listOf(AREA), facts.rollup.queueDrainedAreaIds)
        assertFalse(
            "the queue already went to nothing the week before, inside that week",
            FirstEver.FIRST_QUEUE_DRAIN in facts.history.firstEverFlags,
        )
    }

    /** The same log without the earlier week, so the case above is proved by its difference. */
    @Test
    fun `a drain with no earlier drain behind it is a first`() {
        val log = emptyArea()
        log.queue(21, 4..6)
        log.workThrough(22, 4..6)

        val facts = extract(log)
        assertTrue(FirstEver.FIRST_QUEUE_DRAIN in facts.history.firstEverFlags)
    }

    // The fixture, written as the acts a person performs.

    private fun emptyArea(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9), AREA, AREA_NAME)
        return log
    }

    /** Adds items into the area's queue, one an hour so the order in the log is the order here. */
    private fun TrailTestLog.queue(day: Int, ids: Iterable<Int>) {
        for ((offset, index) in ids.withIndex()) {
            item(at(day, 9 + offset), "d$index", AREA, "Thing $index", areaName = AREA_NAME)
        }
    }

    /**
     * Promotes and completes each item in turn, which is how a queue empties by being
     * worked through: one promotion takes an item off the queue, and the completion that
     * follows clears the way for the next.
     */
    private fun TrailTestLog.workThrough(day: Int, ids: Iterable<Int>) {
        for ((offset, index) in ids.withIndex()) {
            val hour = 9 + offset * 2
            promote(at(day, hour), "d$index", AREA, "Thing $index", areaName = AREA_NAME)
            complete(at(day, hour + 1), "d$index", AREA, "Thing $index", areaName = AREA_NAME)
        }
    }

    private companion object {
        const val AREA = "drain"
        const val AREA_NAME = "Drain"
        const val QUIET = "quiet"
        const val WINDOW_FROM = 20
        const val WINDOW_TO = 27
    }
}
