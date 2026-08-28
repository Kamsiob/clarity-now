package com.kamsiob.claritynow.domain.engine.facts

import com.kamsiob.claritynow.domain.engine.ActiveItem
import com.kamsiob.claritynow.domain.engine.AreaFacts
import com.kamsiob.claritynow.domain.engine.CompletedItem
import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.EstimateTendency
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.HistoryFacts
import com.kamsiob.claritynow.domain.engine.ItemFacts
import com.kamsiob.claritynow.domain.engine.PulseFacts
import com.kamsiob.claritynow.domain.engine.RollupFacts
import com.kamsiob.claritynow.domain.engine.WindowFacts
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * Estimates as calibration. MASTER_BUILD_PROMPT 14b.8, Addendum 01 7a.
 *
 * Three things are checked here and they are not the same kind of thing.
 *
 * The **floor** is arithmetic: under five completed items carrying an estimate there
 * is no ratio and the tendency says so, and the count is reported truthfully either
 * way so that a rule can read it and the validator can re-read the number that gated
 * the sentence.
 *
 * The **reading** is a median over a twelve week window, and the two tests that
 * matter are the ones that would pass under a mean and under a lifetime span. Both of
 * those are the obvious implementation and both produce a number that is true of
 * nothing the person would recognize.
 *
 * The **shape** is the prohibition. 14b.8 bans a rendered delta between an estimate
 * and an actual, and the form built for it is a fact set holding no quantity of
 * minutes at all, so `actual - estimate` is not a subtraction anything downstream can
 * write. That is a claim about every field of every fact class, so it is checked by
 * walking them rather than by reading them.
 */
class EstimateCalibrationFactsTest {

    private fun log(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        return log
    }

    /**
     * One captured, started and finished item, with a prediction and a stay.
     *
     * [minutes] is the elapsed stay in minutes, so a test states the ratio it wants
     * rather than a pair of clock times somebody has to subtract to read the test.
     */
    private fun TrailTestLog.finished(day: Int, id: String, estimate: Int?, minutes: Int) {
        item(at(day, 9), id, "work", "Thing $id", estimateMinutes = estimate)
        promote(at(day, 10), id, "work", "Thing $id")
        complete(at(day, 10) + minutes * MILLIS_PER_MINUTE, id, "work", "Thing $id")
    }

    private fun history(log: TrailTestLog, fromDay: Int, toDay: Int): HistoryFacts =
        FactExtractor(log.queries()).extract(window(fromDay, toDay)).history

    // ------------------------------------------------------------------ the floor

    @Test
    fun `four estimated completions leave the tendency unavailable and the count true`() {
        val log = log()
        for (day in 1..4) log.finished(day, "i$day", estimate = 60, minutes = 120)

        val history = history(log, 0, 7)
        assertEquals(4, history.estimatedCompletions)
        assertNull("under the floor there is no ratio to render", history.activeToEstimateRatio)
        assertEquals(EstimateTendency.INSUFFICIENT, history.estimateTendency)
    }

    @Test
    fun `five estimated completions are the floor and the reading appears`() {
        val log = log()
        for (day in 1..5) log.finished(day, "i$day", estimate = 60, minutes = 120)

        val history = history(log, 0, 7)
        assertEquals(EstimateTendency.MIN_COMPLETIONS, history.estimatedCompletions)
        assertEquals(2.0, history.activeToEstimateRatio!!, 0.0001)
        assertEquals(EstimateTendency.LONGER, history.estimateTendency)
    }

    /** Completions with no prediction behind them are not in the count that gates the family. */
    @Test
    fun `completions without an estimate do not count toward the floor`() {
        val log = log()
        for (day in 1..4) log.finished(day, "i$day", estimate = 60, minutes = 120)
        for (day in 5..9) log.finished(day, "u$day", estimate = null, minutes = 120)

        val history = history(log, 0, 14)
        assertEquals(4, history.estimatedCompletions)
        assertEquals(EstimateTendency.INSUFFICIENT, history.estimateTendency)
    }

    // ---------------------------------------------------------------- the reading

    /**
     * One thing left running is not the shape of a person's estimates.
     *
     * Under a mean this reading is 13.6, which is a claim no week of this person's
     * life resembles, and "tend to" is a median word. `ItemFacts.medianDaysToComplete`
     * takes the same view of the same kind of question.
     */
    @Test
    fun `the reading is the median of the ratios and not their mean`() {
        val log = log()
        for (day in 1..4) log.finished(day, "i$day", estimate = 60, minutes = 120)
        log.finished(5, "outlier", estimate = 5, minutes = 300)

        val history = history(log, 0, 7)
        assertEquals(5, history.estimatedCompletions)
        assertEquals(2.0, history.activeToEstimateRatio!!, 0.0001)
    }

    /**
     * The window is twelve weeks, so a calibration two seasons ago is not this one.
     *
     * A lifetime reading would average how somebody estimated a year ago into how
     * they estimate now, and how they estimate now is the thing that is supposed to be
     * able to change. Under a lifetime span this reads ten completions and a median of
     * six.
     */
    @Test
    fun `completions older than the calibration window are not read`() {
        val log = log()
        for (day in 5..9) log.finished(day, "old$day", estimate = 60, minutes = 600)
        for (day in 100..104) log.finished(day, "new$day", estimate = 60, minutes = 120)

        val history = history(log, 140, 147)
        assertEquals(5, history.estimatedCompletions)
        assertEquals(2.0, history.activeToEstimateRatio!!, 0.0001)
    }

    // ----------------------------------------------------------------- the bands

    @Test
    fun `a stay equal to the estimate is close and has nothing to say`() {
        val log = log()
        for (day in 1..5) log.finished(day, "i$day", estimate = 60, minutes = 60)

        val history = history(log, 0, 7)
        assertEquals(1.0, history.activeToEstimateRatio!!, 0.0001)
        assertEquals(EstimateTendency.CLOSE, history.estimateTendency)
    }

    @Test
    fun `a stay under half the estimate is shorter`() {
        val log = log()
        for (day in 1..5) log.finished(day, "i$day", estimate = 120, minutes = 30)

        val history = history(log, 0, 7)
        assertEquals(0.25, history.activeToEstimateRatio!!, 0.0001)
        assertEquals(EstimateTendency.SHORTER, history.estimateTendency)
    }

    /** The band is drawn where the rendering is: a ratio that would print as one says nothing. */
    @Test
    fun `a ratio that would round to one is close on both sides of one`() {
        assertEquals(EstimateTendency.CLOSE, EstimateTendency.of(1.4))
        assertEquals(EstimateTendency.CLOSE, EstimateTendency.of(0.5))
        assertEquals(EstimateTendency.LONGER, EstimateTendency.of(1.5))
        assertEquals(EstimateTendency.SHORTER, EstimateTendency.of(0.49))
    }

    // ------------------------------------------------------------------ the shape

    /**
     * No quantity of minutes exists on the fact set, estimated or actual.
     *
     * **This is 14b.8's prohibition in its strongest available form.** The hard rule
     * is that no rendered sentence may state a delta between an estimate and an
     * actual. A validator catching the number afterward leaves the number computed; a
     * fact set with no minutes in it leaves the subtraction unwritable. The three
     * fields below are a count, a dimensionless multiple and a band, and none of them
     * can be combined with any other fact into `actual - estimate` because no fact
     * carries either term.
     *
     * A fourth entry here is somebody putting one of the two terms back, whatever it
     * is called and whatever it was for.
     */
    @Test
    fun `the fact set carries a count, a ratio and a tendency and no magnitude`() {
        val found = factClasses.flatMap { type ->
            type.declaredFields
                .filterNot { Modifier.isStatic(it.modifiers) }
                .filter { it.name.contains("estimate", ignoreCase = true) }
                .map { "${type.simpleName}.${it.name}" to it.type.simpleName }
        }.sortedBy { it.first }
        assertEquals(
            "MASTER_BUILD_PROMPT 14b.8 forbids a rendered delta between an estimate and an " +
                "actual, and the form it takes here is that neither number exists to subtract. " +
                "A minutes field on any of these classes puts one of them back",
            listOf(
                "HistoryFacts.activeToEstimateRatio" to "Double",
                "HistoryFacts.estimateTendency" to "EstimateTendency",
                "HistoryFacts.estimatedCompletions" to "int",
            ),
            found,
        )
    }

    /** A stay is stated in minutes and added to an instant, because a minute is not a clock field. */
    private val MILLIS_PER_MINUTE = 60_000L

    private val factClasses = listOf(
        FactSet::class.java,
        WindowFacts::class.java,
        AreaFacts::class.java,
        RollupFacts::class.java,
        ItemFacts::class.java,
        ActiveItem::class.java,
        CompletedItem::class.java,
        HistoryFacts::class.java,
        PulseFacts::class.java,
        CueFacts::class.java,
    )
}
