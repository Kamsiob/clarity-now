package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.Criterion
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subject
import com.kamsiob.claritynow.domain.engine.catalog.SubjectKind
import com.kamsiob.claritynow.domain.engine.catalog.Subjects
import com.kamsiob.claritynow.domain.engine.select.Selection
import com.kamsiob.claritynow.domain.engine.select.Selector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The follow through, as a ranking and as nothing else. CLARITY_LOGIC_ENGINE.md 10.6.
 *
 * `GuidanceNonComplianceTest` holds the door shut: it asserts the mechanism cannot say
 * anything. **This is the other half, and without it the safeguard would be satisfied by a
 * mechanism that also does nothing at all.** A boost that never moved a selection would
 * pass every assertion in that file and would make the whole of 10.6 decorative.
 *
 * So four statements, and the last three are the limits rather than the capability:
 *
 * 1. An accepted plan's family goes **first** among observations of equal specificity
 * 2. It **cannot** pass a more specific observation, however many plans were accepted
 * 3. It **cannot** make anything qualify, because it is read after step 1
 * 4. With no accepted plan the ranking is `ClarityRule.RANKING` exactly, so the mechanism
 *    is inert until somebody says yes
 */
class FollowThroughTest {

    /**
     * 10.6. Among equals, the family a person said yes to is read first.
     *
     * **The peer is chosen so that it wins without the boost**, and the test asserts both
     * orders rather than one. Step 6's last term is `rule.key` ascending, so two families of
     * equal specificity and equal priority are ordered alphabetically; `areaBalance` comes
     * before `neglectedArea` and would come first anyway. Asserting only the boosted order
     * would have passed against a boost that did nothing at all, which is exactly the
     * failure this file exists to catch in the other direction.
     */
    @Test
    fun `a boosted family outranks an equal peer that would otherwise win`() {
        val pairs = listOf(other(), motivating())
        assertEquals(
            "the fixture no longer isolates the boost: the peer must win the key tie break",
            OTHER,
            pairs.sortedWith(Selector.ranking(emptySet())).first().rule.family,
        )
        assertEquals(
            "an accepted plan's family must be read before an equally specific peer",
            NEGLECTED,
            pairs.sortedWith(Selector.ranking(setOf(NEGLECTED to AREA))).first().rule.family,
        )
    }

    /**
     * The limit that matters most, and the reason the boost is on priority.
     *
     * Specificity is `criteria.size` and section 4 calls it the whole mechanism behind the
     * illusion: a rule requiring four conditions describes a narrower and more surprising
     * situation than one requiring two. A boost that could cross it would let a plan
     * somebody accepted in March push aside the most specific thing the app noticed this
     * week, which is the engine flattering the feature rather than describing the person.
     */
    @Test
    fun `a boosted family cannot pass a more specific observation`() {
        val boosted = setOf(NEGLECTED to AREA)
        val ordered = listOf(motivating(), narrower()).sortedWith(Selector.ranking(boosted))
        assertEquals(
            "the boost crossed a specificity level, and 10.6 only raises a rank inside one",
            NARROWER,
            ordered.first().rule.family,
        )
    }

    /**
     * The second limit, and it is structural rather than checked.
     *
     * The boost is a `Set<Pair<FamilyKey, String?>>` handed to step 6. Step 1 evaluates
     * criteria against facts and never sees it, so "that family still has to qualify on its
     * own merits" is not a rule anybody enforces: there is no value reachable from
     * `FollowThrough` that qualification reads. Asserted here as the shape of the type,
     * because that is what the guarantee actually is.
     */
    @Test
    fun `the boost is a set of keys and reaches only the ranking`() {
        val boosted = FollowThrough.boosted(
            PlanHistory(listOf(PlanHistory.Accepted("2026-03-01", NEGLECTED, AREA))),
            "2026-03-08",
        )
        assertEquals(setOf(NEGLECTED to AREA), boosted)
        assertTrue(
            "a boost for a subject nobody accepted a plan about",
            (OTHER to AREA) !in boosted,
        )
    }

    /** The whole mechanism is inert until somebody accepts something. */
    @Test
    fun `an empty boost reproduces the ordinary ranking exactly`() {
        val pairs = listOf(narrower(), other(), motivating())
        assertEquals(
            "Selector.RANKING and ranking(emptySet()) must be one order, not two",
            pairs.sortedWith(Selector.RANKING).map { it.rule.key },
            pairs.sortedWith(Selector.ranking(emptySet())).map { it.rule.key },
        )
    }

    /** An accepted plan older than the two weeks 10.6 reaches back over boosts nothing. */
    @Test
    fun `an accepted plan stops boosting after two weeks`() {
        val old = PlanHistory(listOf(PlanHistory.Accepted("2026-01-04", NEGLECTED, AREA)))
        assertEquals(emptySet<Pair<String, String?>>(), FollowThrough.boosted(old, "2026-03-08"))
    }

    // ----------------------------------------------------------------- fixtures

    private fun motivating() = selection(NEGLECTED, criteria = 2, priority = 5)

    private fun other() = selection(OTHER, criteria = 2, priority = 5)

    private fun narrower() = selection(NARROWER, criteria = 3, priority = 0)

    /**
     * One `(rule, subject)` pair, built by hand.
     *
     * Hand built rather than drawn from the catalog, because the point is a comparator over
     * two specificities and two priorities and the real catalog does not happen to hold a
     * pair that isolates them. `SelectorTest` drives the real rules; this drives the order.
     */
    private fun selection(family: String, criteria: Int, priority: Int) = Selection(
        rule = ClarityRule(
            key = "test.$family",
            purpose = setOf(Purpose.REPORT_OBSERVATION),
            family = family,
            subject = Subjects.AREA,
            criteria = (1..criteria).map { Criterion("c$it", "condition $it") { _, _ -> true } },
            priority = priority,
            horizonDays = 7,
            unflattering = false,
        ),
        purpose = Purpose.REPORT_OBSERVATION,
        subject = Subject(AREA, SubjectKind.AREA),
        callback = null,
        windowDays = 7,
    )

    private companion object {
        const val NEGLECTED = "neglectedArea"
        const val OTHER = "areaBalance"
        const val NARROWER = "persistentItem"
        const val AREA = "area-reading"
    }
}
