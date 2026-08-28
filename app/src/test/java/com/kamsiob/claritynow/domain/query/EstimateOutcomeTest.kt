package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemReopened
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * What a completion says about the estimate behind it. MASTER_BUILD_PROMPT 14b.8,
 * Addendum 01 7a.
 *
 * **The prohibition this suite is really about cannot be tested by asserting a
 * number.** 14b.8 bans a rendered delta between an estimate and an actual, and the
 * shape chosen for it is a facade that divides the two magnitudes inside its own body
 * and hands back a ratio, so that nothing above this line ever holds both. The last
 * test asserts that shape by reflection, because it is the assertion that keeps the
 * ban true when somebody later adds a field here for a reason that looks good.
 *
 * The rest are the four decisions the definition rests on, each of which is
 * defensible either way and each of which is silently wrong if it goes the other way:
 * which estimate is the prediction, which spell is the actual, what an item that was
 * never active is, and what a clock disagreement is.
 */
class EstimateOutcomeTest {

    private fun log(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 8), "work", "Work")
        return log
    }

    /** The whole log's outcomes, over a fortnight from install. */
    private fun outcomesOf(log: TrailTestLog): List<EstimateOutcome> =
        log.queries().estimateOutcomes(startOfDay(0), startOfDay(14))

    private fun onlyRatio(log: TrailTestLog): Double {
        val outcomes = outcomesOf(log)
        assertEquals("exactly one completion should have carried a prediction", 1, outcomes.size)
        return outcomes.single().activeToEstimate
    }

    // ---------------------------------------------------------------- the ratio

    @Test
    fun `a three hour stay against a one hour estimate is a ratio of three`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals(3.0, onlyRatio(log), 0.0001)
    }

    /**
     * The estimate is measured from the promotion, not from the capture.
     *
     * An item captured on Monday, started on Thursday and finished an hour later took
     * an hour. How long it waited is a different quantity with a different name, and
     * `daysActiveForItem` and `daysSinceItemAdded` already keep the two apart.
     */
    @Test
    fun `the wait before the promotion is not part of the actual`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 30)
        log.promote(at(4, 9), "i1", "work", "Thing")
        log.complete(at(4, 9, 30), "i1", "work", "Thing")

        assertEquals(1.0, onlyRatio(log), 0.0001)
    }

    // ---------------------------------------------------------- which prediction

    /**
     * The prediction is the estimate in force when the work started.
     *
     * A revision made while the work is under way is not a prediction any more. It is
     * a progress report, informed by exactly the thing being calibrated, and honoring
     * it would move every ratio toward one and flatter the person the fact is about.
     */
    @Test
    fun `an estimate raised after the promotion is not the prediction`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.estimate(at(1, 11), "i1", previousMinutes = 60, newMinutes = 180)
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals("the revision is a progress report, not a prediction", 3.0, onlyRatio(log), 0.0001)
    }

    /** A revision made before the work started is a better prediction and is the one used. */
    @Test
    fun `an estimate revised before the promotion is the prediction`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.estimate(at(1, 9, 30), "i1", previousMinutes = 60, newMinutes = 30)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 11), "i1", "work", "Thing")

        assertEquals(2.0, onlyRatio(log), 0.0001)
    }

    @Test
    fun `an estimate cleared before the promotion leaves nothing to calibrate`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.estimate(at(1, 9, 30), "i1", previousMinutes = 60, newMinutes = null)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals(emptyList<EstimateOutcome>(), outcomesOf(log))
    }

    /**
     * An estimate given only after the work started predicted nothing.
     *
     * This is the same rule as the revision case and it is the one that would be most
     * tempting to write the other way, because the item does carry an estimate by the
     * time it is finished. It was never a guess about how long the thing would take.
     */
    @Test
    fun `an estimate set only after the promotion is not a prediction`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing")
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.estimate(at(1, 11), "i1", previousMinutes = null, newMinutes = 60)
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals(emptyList<EstimateOutcome>(), outcomesOf(log))
    }

    @Test
    fun `an item with no estimate anywhere is absent`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing")
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals(emptyList<EstimateOutcome>(), outcomesOf(log))
    }

    // ------------------------------------------------------------- which spell

    /**
     * An item completed without ever having been active has no actual.
     *
     * There is a moment it was added and no moment it was started, and falling back to
     * the add is how "how long it waited" becomes "how long it took". The repository
     * has exactly that fallback for its own displayed figure and `daysActiveForItem`
     * already refuses to copy it.
     */
    @Test
    fun `an item completed without a promotion is absent`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.complete(at(1, 13), "i1", "work", "Thing")

        assertEquals(emptyList<EstimateOutcome>(), outcomesOf(log))
    }

    /**
     * A reopened item is measured over the spell that ended in the second completion.
     *
     * Reading back to the first promotion would count the finished work, the days the
     * item sat completed, and the second attempt as one stay.
     */
    @Test
    fun `a reopened item is measured over its second spell`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 11), "i1", "work", "Thing")
        log.add(at(5, 9), ItemReopened("i1", "work", "a1"))
        log.promote(at(5, 10), "i1", "work", "Thing")
        log.complete(at(5, 12), "i1", "work", "Thing")

        val ratios = outcomesOf(log).map { it.activeToEstimate }
        assertEquals(listOf(1.0, 2.0), ratios)
    }

    // ------------------------------------------------------- clocks and windows

    /**
     * A completion stamped before its own promotion is two devices disagreeing.
     *
     * Dropped rather than clamped to zero. A clamp enters the sample as a ratio of
     * nothing, which reads as somebody who beat every estimate they ever made, and it
     * is the direction that would flatter rather than the direction that is true.
     */
    @Test
    fun `a completion whose clock precedes its promotion is dropped rather than clamped`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.add(at(1, 8), ItemCompleted("i1", "work", "Thing", "Work", 0))

        assertEquals(emptyList<EstimateOutcome>(), outcomesOf(log))
    }

    @Test
    fun `only completions inside the window are read`() {
        val log = log()
        log.item(at(1, 9), "i1", "work", "Thing", estimateMinutes = 60)
        log.promote(at(1, 10), "i1", "work", "Thing")
        log.complete(at(1, 11), "i1", "work", "Thing")
        log.item(at(9, 9), "i2", "work", "Later", estimateMinutes = 60)
        log.promote(at(9, 10), "i2", "work", "Later")
        log.complete(at(9, 11), "i2", "work", "Later")

        assertEquals(2, outcomesOf(log).size)
        assertEquals(
            listOf("i1"),
            log.queries().estimateOutcomes(startOfDay(0), startOfDay(7)).map { it.itemId },
        )
    }

    // ------------------------------------------------------------- the shape

    /**
     * Neither magnitude survives the facade, and that is the ban rather than a
     * consequence of it.
     *
     * 14b.8 forbids a rendered delta between an estimate and an actual. The strongest
     * form of that is a codebase in which the subtraction cannot be written, which
     * needs the two numbers never to be in one place above the division. A field here
     * holding minutes puts them there, and every layer above would then be one
     * subtraction from the forbidden sentence with nothing to stop it but review.
     */
    @Test
    fun `an outcome carries a ratio and an id and no magnitude`() {
        val fields = EstimateOutcome::class.java.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .map { it.name to it.type.simpleName }
            .sortedBy { it.first }
        assertEquals(
            "a magnitude on this type is the estimate and the actual in one place, and " +
                "MASTER_BUILD_PROMPT 14b.8 is kept by that being impossible rather than by " +
                "somebody remembering not to subtract them",
            listOf("activeToEstimate" to "double", "itemId" to "String"),
            fields,
        )
    }

    /**
     * Nothing on the facade hands out a duration in the unit an estimate is typed in.
     *
     * The companion to the check above. `estimateOutcomes` is the only function that
     * reads an estimate at all, and if a second one appeared returning minutes, the
     * type check would still pass and the ban would be gone.
     */
    @Test
    fun `no function on the facade returns an estimate or an elapsed time in minutes`() {
        val offenders = TrailQueries::class.java.declaredMethods
            .filterNot { it.isSynthetic }
            .filter { Modifier.isPublic(it.modifiers) }
            .map { it.name }
            .filter { it.contains("estimate", ignoreCase = true) }
            .filterNot { it == "estimateOutcomes" }
        assertTrue(
            "a second path to an estimate magnitude appeared on the facade: $offenders",
            offenders.isEmpty(),
        )
    }
}
