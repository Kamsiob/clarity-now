package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import com.kamsiob.claritynow.domain.engine.validate.ReportCheck
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Section 9's composition rules, each driven by a set of lines built to break it.
 * CLARITY_LOGIC_ENGINE.md 9.2, `CORPUS_2_REPORT.md` 7, `design-v3.md` 11.1.
 *
 * These drive `ReportComposer.assemble` rather than `compose`. Every rule in section 9 is a
 * property of a set of sentences rather than of the machinery that chose them, and a test
 * that has to coax the engine into producing a violating set in order to check the rule
 * that catches it is a test of the engine wearing a composition test's name. What the
 * engine produces is checked instead over ten thousand generated weeks and over the
 * simulator's personas, in `ReportPropertyTest`.
 */
class ReportCompositionTest {

    private val composer = ReportComposer(CorpusFixture.catalog, ReportFixture.ZONE)

    private fun assemble(
        observations: List<Candidate>,
        headline: Candidate? = ReportFixture.headline(),
        pattern: Candidate? = null,
        facts: FactSet = ReportFixture.facts(),
    ): ReportOutcome = composer.assemble(
        headline = headline,
        observations = observations,
        pattern = pattern,
        facts = facts,
        dateKey = ReportFixture.DATE_KEY,
        weekStartKey = ReportFixture.WEEK_START_KEY,
    )

    private fun composed(
        observations: List<Candidate>,
        headline: Candidate? = ReportFixture.headline(),
        pattern: Candidate? = null,
        facts: FactSet = ReportFixture.facts(),
    ): ClarityReport {
        val outcome = assemble(observations, headline, pattern, facts)
        assertTrue("expected a composed report and got $outcome", outcome is ReportOutcome.Composed)
        return (outcome as ReportOutcome.Composed).report
    }

    private fun familiesOf(report: ClarityReport) = report.observations.map { it.candidate.familyKey }

    // ------------------------------------------------------------------ the shape

    @Test
    fun `a report of four observations keeps all four and passes the integrity layer`() {
        val report = composed(
            listOf(
                ReportFixture.workShare(),
                ReportFixture.persistentItem(),
                ReportFixture.focus(),
                ReportFixture.queues(lengthBand = LengthBand.LONG),
            ),
        )
        assertEquals(4, report.observations.size)
        assertTrue("nothing should have been dropped: ${report.dropped}", report.dropped.isEmpty())
    }

    @Test
    fun `one qualifying observation means one observation, and nothing is invented to reach two`() {
        val report = composed(listOf(ReportFixture.workShare()))
        assertEquals(listOf("singleFocus"), familiesOf(report))
        assertTrue(report.dropped.isEmpty())
    }

    @Test
    fun `a week with no events at all is the styled empty state and generates no observations`() {
        val facts = ValidateFixture.facts(window = ValidateFixture.window(totalEvents = 0))
        val outcome = composer.compose(facts, FiringHistory.EMPTY, ReportFixture.WEEK_START_KEY)
        assertTrue("expected the empty state and got $outcome", outcome is ReportOutcome.Empty)
        val note = (outcome as ReportOutcome.Empty).note
        assertNotNull("the corpus should carry an ed.none line", note)
        assertTrue(note!!.variantKey, note.variantKey.startsWith("ed.none."))
    }

    // ------------------------------------ 3.16, the pattern section's empty state

    /**
     * Under three weeks the section says so, and it says so without a rule.
     *
     * `insufficientData` was written as a pattern rule and could never fire: `compose` asks
     * the engine for a pattern only when there are three weeks of snapshots, and the rule
     * required fewer than three, so the two conditions were complements. The owner's ruling
     * is that it was never a pattern in the first place, it is the section's empty state,
     * and the Report renders it directly. `ReportComposer.patternNote` carries the full
     * reasoning; this is the behavior it produces.
     */
    @Test
    fun `under three weeks the pattern section is the corpus empty state rather than a pattern`() {
        val early = ValidateFixture.facts(
            history = ValidateFixture.history(daysSinceInstall = 16, weeksOfData = 2),
        )
        val report = composed(
            listOf(ReportFixture.workShare(), ReportFixture.focus()),
            facts = early,
        )
        assertNull("a pattern needs three weeks behind it", report.pattern)
        val note = report.patternNote
        assertNotNull("the corpus carries four pt.none lines and one of them should be here", note)
        assertTrue(note!!.variantKey, note.variantKey.startsWith("pt.none."))
    }

    /**
     * And with the history behind it the section is a pattern, with no empty state beside it.
     *
     * The two are complements, which is what lets one block on the screen draw either.
     */
    @Test
    fun `with three weeks behind it the section is a pattern and carries no empty state`() {
        val report = composed(
            listOf(ReportFixture.workShare(), ReportFixture.focus()),
            pattern = ReportFixture.pattern(),
        )
        assertNotNull(report.pattern)
        assertNull("an empty state beside a pattern would draw the section twice", report.patternNote)
    }

    /**
     * The rule is gone, and this is the test that says so out loud.
     *
     * Leaving the rule in place beside the direct render would give the section two sources
     * that disagree the first time somebody edits one of them.
     */
    @Test
    fun `insufficientData has no rule anywhere in the catalog`() {
        assertTrue(
            "the pattern section's empty state is rendered by ReportComposer.patternNote, not " +
                "selected: a rule for it would be a second source for one line",
            CorpusFixture.catalog.rules.none { it.family == "insufficientData" },
        )
    }

    // ------------------------------------------------------- 9.2, one area, two mentions

    @Test
    fun `a third observation naming one area is dropped rather than vetoing the report`() {
        val second = ReportFixture.observation(
            family = "queueDrained",
            ruleKey = "report.observation.queueDrained",
            variantKey = "ob.drain.l01",
            lengthBand = LengthBand.SHORT,
            rendered = "Work cleared what was waiting.",
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        val third = ReportFixture.observation(
            family = "areaRevival",
            ruleKey = "report.observation.areaRevival",
            variantKey = "ob.rev.l01",
            lengthBand = LengthBand.LONG,
            rendered = "Work is moving again after a stretch of nothing much at all.",
            namedAreaIds = setOf(ReportFixture.WORK),
        )
        val report = composed(listOf(ReportFixture.workShare(), second, third))
        assertEquals(listOf("singleFocus", "queueDrained"), familiesOf(report))
        assertEquals(listOf("areaRevival"), report.dropped.map { it.family })
        assertTrue(report.dropped.single().reason, "third time" in report.dropped.single().reason)
    }

    @Test
    fun `the observation dropped for a third mention is the lower ranked one`() {
        val later = ReportFixture.observation(
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
        // Ranked order in, so the third one down is the one that loses, which is the same
        // direction the incompatibility matrix resolves in.
        val report = composed(listOf(ReportFixture.workShare(), later, third))
        assertFalse("areaRevival" in familiesOf(report))
        assertTrue("singleFocus" in familiesOf(report))
    }

    // -------------------------------------------------------------- 7.4, the editorial cap

    @Test
    fun `a third editorial lead is dropped, and the first two stay`() {
        val editorials = listOf("singleFocus", "queuePressure", "steadyPace").mapIndexed { index, family ->
            ReportFixture.observation(
                family = family,
                ruleKey = when (family) {
                    "singleFocus" -> "report.observation.singleFocus.s1"
                    "queuePressure" -> "report.observation.queuePressure"
                    else -> "report.observation.steadyPace"
                },
                variantKey = "ob.ed.l0$index",
                register = Register.EDITORIAL,
                lengthBand = LengthBand.entries[index % LengthBand.entries.size],
                rendered = "The week had a shape, and this was it.",
            )
        }
        val report = composed(editorials)
        assertEquals(2, report.observations.count { it.candidate.register == Register.EDITORIAL })
        assertEquals(listOf("steadyPace"), report.dropped.map { it.family })
    }

    // ------------------------------------------------------ 12.3, the intent gate

    @Test
    fun `a callback observation is dropped when fewer than three pulses were answered`() {
        val facts = ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = 2))
        val report = composed(
            listOf(ReportFixture.workShare(), ReportFixture.completionSplit()),
            facts = facts,
        )
        assertEquals(listOf("singleFocus"), familiesOf(report))
        assertEquals(listOf("completionSplit"), report.dropped.map { it.family })
    }

    @Test
    fun `a callback observation stays when three pulses were answered`() {
        val facts = ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = 3))
        val report = composed(
            listOf(ReportFixture.workShare(), ReportFixture.completionSplit()),
            facts = facts,
        )
        assertTrue("completionSplit" in familiesOf(report))
        assertTrue(report.dropped.isEmpty())
    }

    // ------------------------------------------------- design 11.1, the reading order

    @Test
    fun `observations are grouped so a sidehead is never drawn twice`() {
        // Rank order interleaves the sections. Grouping is what stops the screen drawing
        // `Your week, honestly` on either side of `Focus`.
        val report = composed(
            listOf(ReportFixture.workShare(), ReportFixture.focus(), ReportFixture.persistentItem()),
        )
        val sections = report.observations.map { it.section }
        val runs = sections.fold(emptyList<ReportSection>()) { seen, section ->
            if (seen.lastOrNull() == section) seen else seen + section
        }
        assertEquals("a section is drawn twice with another between them", sections.distinct(), runs)
        assertEquals(ReportSection.YOUR_WEEK, sections.first())
    }

    @Test
    fun `the sections are read in the order design 11_1 lists them`() {
        val report = composed(
            listOf(ReportFixture.focus(), ReportFixture.completionSplit(), ReportFixture.workShare()),
            facts = ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = 3)),
        )
        assertEquals(
            listOf(ReportSection.YOUR_WEEK, ReportSection.WHAT_YOU_SAID, ReportSection.FOCUS),
            report.observations.map { it.section },
        )
    }

    // ------------------------------------------------------------- 7.5, the length bands

    @Test
    fun `no two consecutive leads share a length band where the section has an alternative`() {
        val alsoMedium = ReportFixture.queues(variantKey = "ob.qp.l02", lengthBand = LengthBand.MEDIUM)
        val short = ReportFixture.persistentItem(lengthBand = LengthBand.SHORT)
        val report = composed(listOf(ReportFixture.workShare(lengthBand = LengthBand.MEDIUM), alsoMedium, short))
        val bands = report.observations.map { it.candidate.lengthBand }
        assertEquals(listOf(LengthBand.MEDIUM, LengthBand.SHORT, LengthBand.MEDIUM), bands)
    }

    @Test
    fun `a section with nothing but one band still speaks, because rhythm is worth a line and not a paragraph`() {
        val second = ReportFixture.queues(variantKey = "ob.qp.l02", lengthBand = LengthBand.MEDIUM)
        val report = composed(listOf(ReportFixture.workShare(lengthBand = LengthBand.MEDIUM), second))
        assertEquals(2, report.observations.size)
        assertTrue(report.dropped.isEmpty())
    }

    // --------------------------------------------------------- 7.4b, the parallel clauses

    @Test
    fun `a third consecutive parallel numeric lead is dropped`() {
        // Three leads that each set one number against another. The third is the three part
        // list, and it is the one that goes.
        val report = composed(
            listOf(
                ReportFixture.workShare(lengthBand = LengthBand.MEDIUM),
                ReportFixture.flow(lengthBand = LengthBand.SHORT),
                ReportFixture.queues(lengthBand = LengthBand.LONG),
            ),
        )
        assertEquals(2, report.observations.size)
        assertEquals(listOf("queuePressure"), report.dropped.map { it.family })
        assertTrue(report.dropped.single().reason, "third parallel numeric" in report.dropped.single().reason)
    }

    @Test
    fun `two parallel numeric leads in a row are allowed, and a single number lead resets the run`() {
        val report = composed(
            listOf(
                ReportFixture.workShare(lengthBand = LengthBand.MEDIUM),
                ReportFixture.flow(lengthBand = LengthBand.SHORT),
                ReportFixture.persistentItem(lengthBand = LengthBand.LONG),
                ReportFixture.queues(lengthBand = LengthBand.MEDIUM),
            ),
        )
        assertEquals(4, report.observations.size)
        assertTrue(report.dropped.isEmpty())
    }

    // ------------------------------------------------------------------- the integrity seam

    @Test
    fun `an observation that argues with the headline suppresses the report rather than appearing`() {
        val outcome = assemble(
            observations = listOf(ReportFixture.workShare()),
            headline = ReportFixture.headline(family = "balanced"),
        )
        assertTrue("expected a suppression and got $outcome", outcome is ReportOutcome.Suppressed)
        assertEquals(
            ReportCheck.HEADLINE_CONFLICT,
            (outcome as ReportOutcome.Suppressed).verdict.check,
        )
    }

    @Test
    fun `a fact rendering two numbers suppresses the whole report and not the second sentence`() {
        val disagreeing = ReportFixture.observation(
            family = "steadyPace",
            ruleKey = "report.observation.steadyPace",
            variantKey = "ob.stead.l01",
            rendered = "You finished 4 things.",
            slots = mapOf("n" to Slot.Count("n", 4, "thing", "things")),
            sourceFacts = mapOf("n" to ReportFixture.COMPLETIONS),
        )
        val outcome = assemble(listOf(ReportFixture.flow(), disagreeing))
        assertTrue("expected a suppression and got $outcome", outcome is ReportOutcome.Suppressed)
        assertEquals(
            ReportCheck.NUMBER_CONSISTENCY,
            (outcome as ReportOutcome.Suppressed).verdict.check,
        )
    }

    // --------------------------------------------------------------- the footer and totals

    @Test
    fun `the number map holds every rendered number and the three the caption states`() {
        val report = composed(listOf(ReportFixture.workShare(), ReportFixture.persistentItem()))
        assertEquals(9, report.numberFor(ReportFixture.WORK_EVENTS))
        assertEquals(12, report.numberFor(ReportFixture.TOTAL_EVENTS))
        assertEquals(9, report.numberFor(ReportFixture.ITEM_AGE))
        assertEquals(listOf(12, 5, 3), report.totals.map { it.value })
        assertEquals(5, report.numberFor(ReportFixture.COMPLETIONS))
    }

    @Test
    fun `the footer states where the report was generated, from the corpus`() {
        val report = composed(listOf(ReportFixture.workShare()))
        assertEquals("Generated on your device", report.generated?.text)
    }

    @Test
    fun `the basis line states the answers it can prove and omits the clause it cannot`() {
        val report = composed(listOf(ReportFixture.workShare()))
        val basis = report.basis
        assertNotNull("a week with two answered pulses has a basis to state", basis)
        assertTrue(basis!!.rendered, basis.rendered.startsWith("Based on 2 Pulse response"))
    }

    @Test
    fun `the basis line is absent when there is nothing about the basis to state`() {
        val facts = ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = 0))
        val report = composed(listOf(ReportFixture.workShare()), facts = facts)
        assertNull(report.basis)
    }

    @Test
    fun `the first week gets its own note and its own basis line`() {
        val facts = ValidateFixture.facts(
            history = ValidateFixture.history(daysSinceInstall = 4, weeksOfData = 0),
        )
        val report = composed(listOf(ReportFixture.workShare()), facts = facts)
        assertNotNull(report.firstWeekNote)
        assertTrue(report.firstWeekNote!!.variantKey.startsWith("ed.first."))
        assertEquals("Based on your first week.", report.basis?.rendered)
    }
}
