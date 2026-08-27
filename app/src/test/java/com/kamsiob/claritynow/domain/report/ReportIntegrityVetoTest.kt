package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ReportCheck
import com.kamsiob.claritynow.domain.engine.validate.ReportIntegrity
import com.kamsiob.claritynow.domain.engine.validate.ReportLine
import com.kamsiob.claritynow.domain.engine.validate.ReportRole
import com.kamsiob.claritynow.domain.engine.validate.ReportVerdict
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The report scope integrity layer, with one violating report per check.
 *
 * **This is the test `MASTER_BUILD_PROMPT.md` 12.3 asks for, at the scale section 9 works
 * at.** 12.3 says the veto path must be reachable in unit tests, and the reason is the same
 * one `ValidatorVetoTest` opens with: a validator whose failure branch never executes is a
 * validator nobody has verified. A check that cannot fire is worse than no check, because
 * everyone downstream believes it is holding.
 *
 * Every report below is a real one with a single thing wrong. None is a mock, none is
 * nonsense, and each is accompanied by the almost identical report that passes, so a
 * validator that vetoed everything would fail here rather than sail through.
 *
 * The week is `ValidateFixture`'s and every number in it is re-read from the same
 * `Measures` table the app uses, so a report that passes here passes by being true.
 */
class ReportIntegrityVetoTest {

    private val integrity = ReportIntegrity(CorpusFixture.catalog, ReportFixture.ZONE)

    private fun inspect(lines: List<ReportLine>, facts: FactSet = ReportFixture.facts()) =
        integrity.inspect(lines, facts)

    private fun veto(lines: List<ReportLine>, facts: FactSet = ReportFixture.facts()): ReportVerdict.Vetoed {
        val verdict = inspect(lines, facts)
        assertTrue("expected a veto and the report passed", verdict is ReportVerdict.Vetoed)
        return verdict as ReportVerdict.Vetoed
    }

    private fun passes(lines: List<ReportLine>, facts: FactSet = ReportFixture.facts()) {
        assertEquals("expected this report to pass", ReportVerdict.Passed, inspect(lines, facts))
    }

    private fun observations(vararg candidates: Candidate) =
        candidates.map { ReportLine(ReportRole.OBSERVATION, it) }

    /** The report every violating one below is a copy of, with one thing changed. */
    private fun goodReport() = listOf(
        ReportLine(ReportRole.HEADLINE, ReportFixture.headline()),
    ) + observations(
        ReportFixture.workShare(),
        ReportFixture.persistentItem(),
        ReportFixture.focus(),
    ) + ReportLine(ReportRole.PATTERN, ReportFixture.pattern())

    @Test
    fun `the fixture report passes every check, so a veto below means something`() {
        passes(goodReport())
    }

    @Test
    fun `all nine checks are declared, in the order this layer runs them`() {
        assertEquals(ReportCheck.entries.toList(), integrity.checkOrder)
        assertEquals((1..ReportCheck.entries.size).toList(), ReportCheck.entries.map { it.number })
    }

    // Check 1. An area named without events in the window. 12.3's first hard rule.

    @Test
    fun `check 1 vetoes an area that exists and had no events in the window`() {
        val phantom = ReportFixture.observation(
            family = "neglectedArea",
            ruleKey = "report.observation.neglectedArea.s1",
            variantKey = "ob.neg.s1.l01",
            rendered = "Reading has been quiet.",
            namedAreaIds = setOf(ReportFixture.READING),
            subjectId = ReportFixture.READING,
        )
        val verdict = veto(observations(ReportFixture.workShare(), phantom))
        assertEquals(ReportCheck.AREA_HAS_EVENTS, verdict.check)
        assertTrue(verdict.detail, "Reading" in verdict.detail)
    }

    @Test
    fun `check 1 vetoes a new area with no activity, which is the one that looks harmless`() {
        val newArea = ValidateFixture.area(
            areaId = "area-new",
            nameSnapshot = "Garden",
            eventsInWindow = 0,
            lifetimeEvents = 0,
            lifetimeCompletions = 0,
            ageDays = 2,
        )
        val facts = ValidateFixture.facts(
            areas = ValidateFixture.facts().areas + (newArea.areaId to newArea),
        )
        val named = ReportFixture.observation(
            family = "areaBalance",
            ruleKey = "report.observation.areaBalance",
            rendered = "Garden is new here.",
            namedAreaIds = setOf("area-new"),
        )
        val verdict = veto(observations(named), facts)
        assertEquals(ReportCheck.AREA_HAS_EVENTS, verdict.check)
        assertTrue(verdict.detail, "Garden" in verdict.detail)
    }

    @Test
    fun `check 1 vetoes an area the fact set does not carry at all, which is how an archived one would arrive`() {
        val archived = ReportFixture.observation(
            family = "areaBalance",
            ruleKey = "report.observation.areaBalance",
            rendered = "That area is quiet.",
            namedAreaIds = setOf(ValidateFixture.ARCHIVED),
        )
        assertEquals(ReportCheck.AREA_HAS_EVENTS, veto(observations(archived)).check)
    }

    // Check 2. A deleted item named.

    @Test
    fun `check 2 vetoes an item that nothing in the fact set resolves`() {
        val ghost = ReportFixture.observation(
            family = "persistentItem",
            ruleKey = "report.observation.persistentItem.low",
            rendered = "Something has been sitting there a while.",
            namedItemIds = setOf(ValidateFixture.DELETED_ITEM),
        )
        val verdict = veto(observations(ReportFixture.workShare(), ghost))
        assertEquals(ReportCheck.ITEM_RESOLVES, verdict.check)
        assertTrue(verdict.detail, ValidateFixture.DELETED_ITEM in verdict.detail)
    }

    // Check 3. The one a per sentence validator cannot make.

    @Test
    fun `check 3 vetoes the whole report when one fact renders two different numbers`() {
        // Two observations reaching for the window's completions and disagreeing, which is
        // what a fact recomputation between two realizations looks like from the page.
        val disagreeing = ReportFixture.observation(
            family = "steadyPace",
            ruleKey = "report.observation.steadyPace",
            variantKey = "ob.stead.l01",
            rendered = "You finished 4 things.",
            slots = mapOf("n" to Slot.Count("n", 4, "thing", "things")),
            sourceFacts = mapOf("n" to ReportFixture.COMPLETIONS),
        )
        val verdict = veto(observations(ReportFixture.flow(), disagreeing))
        assertEquals(ReportCheck.NUMBER_CONSISTENCY, verdict.check)
        assertTrue(verdict.detail, "window.completions" in verdict.detail)
    }

    @Test
    fun `check 3 allows two lines that state the same fact as the same number`() {
        val agreeing = ReportFixture.observation(
            family = "steadyPace",
            ruleKey = "report.observation.steadyPace",
            variantKey = "ob.stead.l01",
            rendered = "You finished 5 things.",
            slots = mapOf("n" to Slot.Count("n", 5, "thing", "things")),
            sourceFacts = mapOf("n" to ReportFixture.COMPLETIONS),
        )
        passes(observations(ReportFixture.flow(), agreeing))
    }

    // Check 4. Provenance, at the scale of a page.

    @Test
    fun `check 4 vetoes a number with no FactRef behind it`() {
        val untraceable = ReportFixture.observation(
            family = "queuePressure",
            ruleKey = "report.observation.queuePressure",
            variantKey = "ob.qp.l01",
            rendered = "Your queues hold 7 things now.",
            slots = mapOf("n" to Slot.Count("n", 7, "thing", "things")),
            sourceFacts = emptyMap(),
        )
        val verdict = veto(observations(untraceable))
        assertEquals(ReportCheck.NUMBER_PROVENANCE, verdict.check)
        assertTrue(verdict.detail, "no FactRef" in verdict.detail)
    }

    @Test
    fun `check 4 vetoes a number that no longer reads what the sentence says`() {
        val stale = ReportFixture.observation(
            family = "queueDrained",
            ruleKey = "report.observation.queueDrained",
            variantKey = "ob.drain.l01",
            rendered = "Work had 8 events.",
            slots = mapOf("n" to Slot.Count("n", 8, "event", "events")),
            sourceFacts = mapOf("n" to ReportFixture.WORK_EVENTS),
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        val verdict = veto(observations(stale))
        assertEquals(ReportCheck.NUMBER_PROVENANCE, verdict.check)
        assertTrue(verdict.detail, "reads 9" in verdict.detail)
    }

    @Test
    fun `check 4 reaches the basis line, which the engine loop never produced`() {
        val basis = ReportFixture.observation(
            family = "bs",
            ruleKey = "report.auxiliary.bs",
            variantKey = "bs.04",
            rendered = "Based on 6 Pulse responses.",
            slots = mapOf("n" to Slot.Count("n", 6, "answer", "answers")),
            sourceFacts = mapOf("n" to FactRef("pulse", "answeredInWindow")),
        )
        val verdict = veto(listOf(ReportLine(ReportRole.BASIS, basis)))
        assertEquals(ReportCheck.NUMBER_PROVENANCE, verdict.check)
    }

    // Check 5. The incompatibility matrix, 9.1.

    @Test
    fun `check 5 vetoes a report holding both members of an incompatible pair`() {
        // singleFocus says the week was narrow and areaBalance says it was broad. Both are
        // arithmetically true of this week and the pair is incoherent.
        val verdict = veto(observations(ReportFixture.workShare(), ReportFixture.healthEvents()))
        assertEquals(ReportCheck.INCOMPATIBLE_PAIR, verdict.check)
    }

    @Test
    fun `check 5 leaves two families that are not a pair alone`() {
        passes(observations(ReportFixture.workShare(), ReportFixture.persistentItem()))
    }

    // Check 6. The headline sets the frame, 9.2.

    @Test
    fun `check 6 vetoes an observation that argues with the headline`() {
        val lines = listOf(ReportLine(ReportRole.HEADLINE, ReportFixture.headline(family = "balanced"))) +
            observations(ReportFixture.workShare())
        val verdict = veto(lines)
        assertEquals(ReportCheck.HEADLINE_CONFLICT, verdict.check)
        assertTrue(verdict.detail, "balanced" in verdict.detail)
    }

    @Test
    fun `check 6 vetoes an observation a quiet week headline left no room for`() {
        val lines = listOf(ReportLine(ReportRole.HEADLINE, ReportFixture.headline(family = "quietWeek"))) +
            observations(ReportFixture.focus())
        assertEquals(ReportCheck.HEADLINE_CONFLICT, veto(lines).check)
    }

    @Test
    fun `check 6 allows what a quiet week headline does leave room for`() {
        val lines = listOf(ReportLine(ReportRole.HEADLINE, ReportFixture.headline(family = "quietWeek"))) +
            observations(ReportFixture.persistentItem())
        passes(lines)
    }

    // Check 7. One area, two mentions.

    @Test
    fun `check 7 vetoes an area named in three of the observations`() {
        val second = ReportFixture.observation(
            family = "queueDrained",
            ruleKey = "report.observation.queueDrained",
            variantKey = "ob.drain.l01",
            rendered = "Work cleared what was waiting.",
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        val third = ReportFixture.observation(
            family = "areaRevival",
            ruleKey = "report.observation.areaRevival",
            variantKey = "ob.rev.l01",
            rendered = "Work is moving again.",
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        val verdict = veto(observations(ReportFixture.workShare(), second, third))
        assertEquals(ReportCheck.AREA_MENTION_CAP, verdict.check)
        assertTrue(verdict.detail, "Work" in verdict.detail)
    }

    @Test
    fun `check 7 allows two mentions, which is what 9_2 permits`() {
        val second = ReportFixture.observation(
            family = "queueDrained",
            ruleKey = "report.observation.queueDrained",
            variantKey = "ob.drain.l01",
            rendered = "Work cleared what was waiting.",
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        passes(observations(ReportFixture.workShare(), second))
    }

    // Check 8. Two to four, and four is the ceiling.

    @Test
    fun `check 8 vetoes a fifth observation`() {
        val filler = (1..5).map { index ->
            ReportFixture.observation(
                family = "steadyPace",
                ruleKey = "report.observation.steadyPace",
                variantKey = "ob.stead.l0$index",
                rendered = "The week held its pace.",
            )
        }
        val verdict = veto(observations(*filler.toTypedArray()))
        assertEquals(ReportCheck.OBSERVATION_COUNT, verdict.check)
    }

    @Test
    fun `check 8 allows one observation, because a week with one thing to say gets one`() {
        passes(observations(ReportFixture.workShare()))
    }

    // Check 9. A pattern needs three weeks.

    @Test
    fun `check 9 vetoes a pattern with two weeks of data behind it`() {
        val facts = ValidateFixture.facts(history = ValidateFixture.history(daysSinceInstall = 16, weeksOfData = 2))
        val lines = observations(ReportFixture.workShare()) +
            ReportLine(ReportRole.PATTERN, ReportFixture.pattern())
        val verdict = veto(lines, facts)
        assertEquals(ReportCheck.PATTERN_WITHOUT_HISTORY, verdict.check)
    }

    @Test
    fun `check 9 has nothing to say about a report with no pattern section`() {
        val facts = ValidateFixture.facts(history = ValidateFixture.history(daysSinceInstall = 16, weeksOfData = 2))
        passes(observations(ReportFixture.workShare()), facts)
    }
}
