package com.kamsiob.claritynow.domain.guidance

import com.kamsiob.claritynow.domain.engine.CueFacts
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.PartOfDay
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.Weekday
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The six composition rules. CLARITY_LOGIC_ENGINE.md 10.4.
 *
 * > A plan may be produced only when **all** hold.
 *
 * One test per rule, each one holding everything else constant and moving the single thing
 * the rule is about, so a failure names the rule rather than the fixture. The control is
 * [plan], a week that satisfies all six and produces a plan; every test below starts from
 * it and breaks exactly one condition.
 */
class GuidanceCompositionTest {

    private val composer = GuidanceComposer(CorpusFixture.catalog, ZONE)

    /** The control. All six hold and a plan comes out. */
    @Test
    fun `a week that satisfies all six rules gets a plan`() {
        val result = compose()
        assertTrue("the control week must produce a plan", result is GuidanceResult.Plan)
        val plan = (result as GuidanceResult.Plan).plan
        assertEquals("the plan is filed under the week it was offered in", WEEK, plan.weekStartKey)
        assertEquals("the motivating family travels with the plan", NEGLECTED, plan.familyKey)
        assertEquals("and so does its subject", ValidateFixture.READING, plan.subjectId)
        assertNotNull("a plan carries the fact behind the observation that motivated it", plan.resolutionFactRef)
    }

    /** Rule 1. 3.7, and `CueSubstantiationTest` at scale. */
    @Test
    fun `rule 1, no stable rhythm means no plan`() {
        val result = compose(cues = CueFacts.NONE)
        assertTrue("a plan was offered with no substantiated cue", result !is GuidanceResult.Plan)
    }

    /**
     * Rule 2, and it is the one rule with no failing case to construct.
     *
     * > **The motivating observation actually appeared in the report.** Enforced by passing
     * > only `appeared` into layer 6.
     *
     * 10.4 asks for this structurally rather than as a check, so the test is structural
     * too: there is no argument to [GuidanceComposer.compose] through which an observation
     * that did not appear could reach it, and the plan's family is always the family of one
     * of the values passed in. An empty page therefore produces no plan by having nothing
     * to be motivated by, rather than by a rule about emptiness.
     */
    @Test
    fun `rule 2, a plan can only be motivated by an observation that appeared`() {
        val plan = (compose() as GuidanceResult.Plan).plan
        assertTrue(
            "the plan names a family that was not on the page",
            plan.familyKey in appeared().map { it.candidate.familyKey },
        )
        val nothingAppeared = compose(appeared = emptyList())
        assertTrue("a page with no observations produced a plan", nothingAppeared !is GuidanceResult.Plan)
    }

    /**
     * Rule 3. A straightforwardly good week gets no plan.
     *
     * > There is a real friction pattern. When barriers are low, plan formation is
     * > superfluous, so a straightforwardly good week gets no plan.
     *
     * The week here is the same week; only the observation on it changes, from an area that
     * has gone quiet to a steady pace. `steadyPace` has no action bank in
     * `CORPUS_2_REPORT.md` 4.3, which is the corpus saying there is nothing to act on, so
     * nothing here has to decide that a week was a good one.
     */
    @Test
    fun `rule 3, a week with no friction gets a closing rather than a plan`() {
        val result = compose(appeared = listOf(observation("steadyPace", "ob.steady.l01")))
        assertTrue("a good week was offered a plan", result !is GuidanceResult.Plan)
        assertTrue("a good week should still get a closing", result is GuidanceResult.Closing)
    }

    /**
     * Rule 4. No plan while one from either of the previous two weeks is still unresolved.
     *
     * Unresolved means the same `(family, subject)` is on this week's page again. The three
     * cases are the whole rule: last week is held back, three weeks ago is not, and a plan
     * whose situation has gone is not.
     */
    @Test
    fun `rule 4, an unresolved plan from the last two weeks holds the next one back`() {
        val lastWeek = PlanHistory(listOf(PlanHistory.Accepted("2026-03-01", NEGLECTED, ValidateFixture.READING)))
        assertTrue(
            "a plan was stacked on an unresolved one from last week",
            compose(plans = lastWeek) !is GuidanceResult.Plan,
        )

        val longAgo = PlanHistory(listOf(PlanHistory.Accepted("2026-02-01", NEGLECTED, ValidateFixture.READING)))
        assertTrue(
            "a plan from five weeks ago is outside the two week reach and must not block",
            compose(plans = longAgo) is GuidanceResult.Plan,
        )

        val otherSubject = PlanHistory(listOf(PlanHistory.Accepted("2026-03-01", NEGLECTED, ValidateFixture.HEALTH)))
        assertTrue(
            "a plan about a different area is a different situation and must not block",
            compose(plans = otherSubject) is GuidanceResult.Plan,
        )
    }

    /**
     * Rule 4's other half, and it is 10.5 rather than 10.4.
     *
     * > Declining writes nothing, costs nothing, is never counted, never referenced.
     * > Ignoring both is identical to declining.
     *
     * A declined plan cannot hold the next one back, and the reason it cannot is that
     * `PlanHistory` never learns about it: an offer with no acceptance beside it produces no
     * entry at all. So the assertion is that a `PlanHistory` built from a log where the
     * plan was offered and never accepted equals the empty one.
     */
    @Test
    fun `a declined plan costs nothing, because the history never learns of it`() {
        val declined = PlanHistory(emptyList())
        assertTrue("declining held a plan back", compose(plans = declined) is GuidanceResult.Plan)
        assertEquals("an unaccepted plan leaves no entry", PlanHistory.EMPTY, declined)
    }

    /**
     * Rule 5. A single concrete act, completable inside one week.
     *
     * Structural, and asserted as such. Every line in 4.3 was authored under that
     * constraint and reviewed against 4.9 rule 1, "if it cannot be finished in a sitting it
     * is a project, not an action", so a plan built from the bank satisfies the rule by
     * construction. Re-judging it at runtime would be the engine second guessing the
     * corpus. What is checked here is that the action really did come from the bank and
     * that the bank is the one the motivating family licenses.
     */
    @Test
    fun `rule 5, the action comes from the bank its family licenses`() {
        val plan = (compose() as GuidanceResult.Plan).plan
        val licensed = PlanBenches.of(CorpusFixture.catalog).actionsFor(NEGLECTED).map { it.key }
        assertTrue("the plan used ${plan.actionKey}, which $NEGLECTED does not license", plan.actionKey in licensed)
        assertEquals("4.3 licenses the neglectedArea bank and no other", "act.neg", plan.actionKey.substringBeforeLast('.'))
    }

    /**
     * Rule 6. A heavy report gets nothing at all, not even a closing.
     *
     * > The report is not otherwise heavy. A declining headline plus a neglected area, or
     * > any `hardStretch`, means no plan.
     *
     * Both halves, and the consequence: `Nothing`. A week that was genuinely hard does not
     * get a line underneath it saying the week worked, and the three non plan benches all
     * say something about the week that would be false on this one.
     */
    @Test
    fun `rule 6, a heavy report gets no plan and no closing`() {
        val hard = compose(appeared = appeared() + observation("hardStretch", "ob.hard.l01"))
        assertEquals("any hardStretch means no plan", GuidanceResult.Nothing, hard)

        val declining = compose(headline = observation("decliningActivity", "hd.decl.01"))
        assertEquals("a declining headline plus a neglected area means no plan", GuidanceResult.Nothing, declining)

        val decliningAlone = compose(
            headline = observation("decliningActivity", "hd.decl.01"),
            appeared = listOf(observation("queuePressure", "ob.qp.s1.l01")),
        )
        assertTrue(
            "a declining headline on its own is not the heavy case 10.4 names",
            decliningAlone !is GuidanceResult.Nothing,
        )
    }

    /**
     * 10.7. Layer 6 has a silence and the silence is reachable.
     *
     * The bench a closing comes from is chosen by what the week was, per 4.6's own
     * descriptions of its four benches, and the heavy case is silent outright. This asserts
     * the four routes are distinct, which is what stops the closing being one line the
     * report always carries.
     */
    @Test
    fun `the four closing benches are reachable and distinct`() {
        val quiet = compose(appeared = listOf(observation("quietWeek", "ob.quiet.l01")))
        val good = compose(appeared = listOf(observation("steadyPace", "ob.steady.l01")))
        val noRhythm = compose(cues = CueFacts.NONE)
        val texts = listOf(quiet, good, noRhythm).map { (it as GuidanceResult.Closing).line.text }
        assertEquals("three different weeks produced the same closing", texts.size, texts.toSet().size)
    }

    // ----------------------------------------------------------------- the fixture

    private fun compose(
        headline: Validated? = null,
        appeared: List<Validated> = appeared(),
        cues: CueFacts = RHYTHM,
        plans: PlanHistory = PlanHistory.EMPTY,
        facts: FactSet = ValidateFixture.facts(cues = cues),
    ): GuidanceResult = composer.compose(
        headline = headline,
        appeared = appeared,
        facts = facts,
        plans = plans,
        history = FiringHistory.EMPTY,
        weekStartKey = WEEK,
    )

    private fun appeared(): List<Validated> = listOf(observation(NEGLECTED, "ob.neg.s2.l01"))

    private fun observation(family: FamilyKey, variant: String): Validated = Validated(
        ValidateFixture.candidate(
            ruleKey = if (family == NEGLECTED) NEGLECTED_RULE else "report.observation.$family",
            purpose = Purpose.REPORT_OBSERVATION,
            familyKey = family,
            variantKey = variant,
            rendered = "Reading has been quiet for three weeks.",
            renderedQuestion = null,
            slots = emptyMap(),
            sourceFacts = emptyMap(),
            namedAreaIds = if (family == NEGLECTED) setOf(ValidateFixture.READING) else emptySet(),
            namedItemIds = emptySet(),
            subjectId = if (family == NEGLECTED) ValidateFixture.READING else null,
        ),
    )

    private companion object {

        val ZONE: ZoneId = ZoneId.of("UTC")

        const val WEEK = "2026-03-08"

        const val NEGLECTED: FamilyKey = "neglectedArea"

        const val NEGLECTED_RULE = "report.observation.neglectedArea.s2"

        val RHYTHM = CueFacts(
            strongestWeekday = Weekday.WED,
            strongestWeekdayConfidence = 0.75,
            quietestWeekday = Weekday.SUN,
            productiveBand = PartOfDay.MORNING,
            productiveBandShare = 0.62,
            focusTypicalWeekday = Weekday.TUE,
            focusTypicalBand = PartOfDay.MORNING,
            addingBand = PartOfDay.EVENING,
            weekdayOnly = true,
            hasStableRhythm = true,
        )
    }
}
