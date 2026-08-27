package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.Slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * The integrity core: checks 1 to 4 of CLARITY_LOGIC_ENGINE.md 8, each vetoing a candidate
 * built to violate it.
 *
 * **This is the test the specification singles out.** "The veto path for each must be
 * reachable in a unit test that deliberately constructs a violating candidate and asserts
 * the veto. A validator whose failure branch is never executed is a validator nobody has
 * verified."
 *
 * Every violating candidate here is a real one. None is a mock returning false and none is
 * nonsense: each is a sentence a plausible bug in layers 3 or 4 would actually produce,
 * built against a fact set that is internally consistent, and each is accompanied by the
 * almost identical candidate that passes. The failure this file is written against is a
 * validator that vetoes everything, which would pass a test that only ever asserted a veto.
 *
 * Every `FactRef` below is a real address from the `Measures` table, so the passing cases
 * pass by being re-read rather than by being unreadable in a way the check tolerates.
 */
class ValidatorVetoTest {

    private val validator = ClarityValidator(ZoneId.of("UTC"))

    private fun veto(candidate: Candidate, facts: FactSet = ValidateFixture.facts()): ValidationResult.Vetoed {
        val result = validator.validate(candidate, facts)
        assertTrue("expected a veto and the candidate passed: ${candidate.rendered}", result is ValidationResult.Vetoed)
        return result as ValidationResult.Vetoed
    }

    private fun passes(candidate: Candidate, facts: FactSet = ValidateFixture.facts()) {
        val result = validator.validate(candidate, facts)
        assertTrue("expected this to pass and it was vetoed: $result", result is ValidationResult.Passed)
    }

    @Test
    fun `the fixture candidate passes every check, so a veto below means something`() {
        passes(ValidateFixture.candidate())
    }

    /** The seam `ClarityEngine` holds: null to speak, a reason not to. */
    @Test
    fun `the engine seam answers null for a candidate that passed and a reason for one that did not`() {
        val facts = ValidateFixture.facts()
        assertNull(validator.veto(ValidateFixture.candidate(), facts))
        val reason = validator.veto(namesReading(), facts)
        assertTrue("$reason", reason != null && reason.startsWith("check 1"))
    }

    @Test
    fun `all ten checks are declared, in the order section 8 numbers them`() {
        assertEquals(ValidationCheck.entries.toList(), validator.checkOrder)
        assertEquals((1..10).toList(), ValidationCheck.entries.map { it.number })
    }

    // Check 1, area existence.

    @Test
    fun `check 1 vetoes an area that exists but had no events in the window`() {
        val veto = veto(namesReading())
        assertEquals(ValidationCheck.AREA_EXISTENCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("Reading"))
        assertTrue(veto.detail, veto.detail.contains("0 events"))
    }

    @Test
    fun `check 1 vetoes an area that is not in the fact set at all`() {
        val veto = veto(namesReading().copy(namedAreaIds = setOf(ValidateFixture.ARCHIVED)))
        assertEquals(ValidationCheck.AREA_EXISTENCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains(ValidateFixture.ARCHIVED))
    }

    @Test
    fun `check 1 passes an area with real events, which is the point of the check`() {
        passes(
            namesReading().copy(
                rendered = "Work moved again today.",
                slots = mapOf("areaName" to Slot.Text("areaName", ValidateFixture.WORK_NAME)),
                namedAreaIds = setOf(ValidateFixture.WORK),
            ),
        )
    }

    /** A rebalance line about the one area that did nothing all week. */
    private fun namesReading(): Candidate = ValidateFixture.candidate(
        ruleKey = "pulse.rebalance.s1",
        familyKey = "rebalance",
        variantKey = "rebalance.s1.04",
        stage = 1,
        rendered = "Reading moved again today.",
        renderedQuestion = null,
        slots = mapOf("areaName" to Slot.Text("areaName", "Reading")),
        sourceFacts = emptyMap(),
        namedAreaIds = setOf(ValidateFixture.READING),
        namedItemIds = emptySet(),
        subjectId = ValidateFixture.READING,
    )

    // Check 2, item existence.

    @Test
    fun `check 2 vetoes an item nothing in the fact set resolves`() {
        val veto = veto(ValidateFixture.candidate(namedItemIds = setOf(ValidateFixture.DELETED_ITEM)))
        assertEquals(ValidationCheck.ITEM_EXISTENCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains(ValidateFixture.DELETED_ITEM))
    }

    @Test
    fun `check 2 passes the item holding the longest ever record, which persistence stage 4 names`() {
        passes(ValidateFixture.candidate(namedItemIds = setOf(ValidateFixture.RECORD_ITEM)))
    }

    @Test
    fun `check 2 passes an item completed inside the window`() {
        passes(
            ValidateFixture.candidate(
                rendered = "Send the invoice is done. Nine days now.",
                namedItemIds = setOf(ValidateFixture.COMPLETED_ITEM),
                slots = mapOf(
                    "itemTitle" to Slot.Text("itemTitle", "Send the invoice"),
                    "ageDays" to Slot.Days("ageDays", 9),
                ),
            ),
        )
    }

    // Check 3, number provenance. The check FactRef exists for.

    @Test
    fun `check 3 vetoes a count that disagrees with the fact it claims`() {
        val veto = veto(counts(value = 7, ref = FactRef("window", "completions")))
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("reads 5"))
    }

    @Test
    fun `check 3 vetoes a number carrying no FactRef at all`() {
        val veto = veto(counts(value = 5, ref = null))
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("no FactRef"))
    }

    @Test
    fun `check 3 vetoes a number whose FactRef addresses no measure this app declares`() {
        val veto = veto(counts(value = 5, ref = FactRef("window", "thingsFinished")))
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("no measure declares"))
    }

    @Test
    fun `check 3 vetoes a number whose fact reads nothing in this window`() {
        val veto = veto(
            counts(value = 4, ref = FactRef("items", "medianDaysToComplete")),
        )
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("reads nothing now"))
    }

    @Test
    fun `check 3 vetoes a percentage that does not match the share behind it`() {
        val veto = veto(
            ValidateFixture.candidate(
                rendered = "Ninety percent of the week landed in one area.",
                renderedQuestion = null,
                slots = mapOf("pct" to Slot.Percent("pct", 90)),
                sourceFacts = mapOf("pct" to FactRef("rollup", "dominantPercent")),
                namedItemIds = emptySet(),
            ),
        )
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("reads 75"))
    }

    /**
     * The false superlative in section 13, caught at the last moment.
     *
     * `mostRecentBetterWeekKey` is null in a week nothing beats, and a `since` line that
     * fired anyway is a claim the person has no way to check and every reason to believe.
     */
    @Test
    fun `check 3 vetoes a since reference to a week that does not exist`() {
        val veto = veto(
            ValidateFixture.candidate(
                ruleKey = "report.mostActiveSince.s1",
                familyKey = "mostActiveSince",
                purpose = Purpose.REPORT_HEADLINE,
                rendered = "Your most active week since February.",
                renderedQuestion = null,
                slots = mapOf("sinceRef" to Slot.DateRef("sinceRef", "2026-02-16", "February")),
                sourceFacts = mapOf("sinceRef" to FactRef("history", "mostRecentBetterWeekRef")),
                namedItemIds = emptySet(),
            ),
            facts = ValidateFixture.facts(history = ValidateFixture.history(mostRecentBetterWeekKey = null)),
        )
        assertEquals(ValidationCheck.NUMBER_PROVENANCE, veto.check)
        assertTrue(veto.detail, veto.detail.contains("reads nothing now"))
    }

    @Test
    fun `check 3 passes a count, a percentage and a week that re-read to what the sentence says`() {
        passes(counts(value = 5, ref = FactRef("window", "completions")))
        passes(
            ValidateFixture.candidate(
                rendered = "Seventy five percent of the week landed in one area.",
                renderedQuestion = null,
                slots = mapOf("pct" to Slot.Percent("pct", 75)),
                sourceFacts = mapOf("pct" to FactRef("rollup", "dominantPercent")),
                namedItemIds = emptySet(),
            ),
        )
        passes(
            ValidateFixture.candidate(
                purpose = Purpose.REPORT_HEADLINE,
                rendered = "Your most active week since February.",
                renderedQuestion = null,
                slots = mapOf("sinceRef" to Slot.DateRef("sinceRef", "2026-02-16", "February")),
                sourceFacts = mapOf("sinceRef" to FactRef("history", "mostRecentBetterWeekRef")),
                namedItemIds = emptySet(),
            ),
        )
    }

    private fun counts(value: Int, ref: FactRef?): Candidate = ValidateFixture.candidate(
        ruleKey = "pulse.throughput.s1",
        familyKey = "throughput",
        variantKey = "throughput.s1.02",
        stage = 1,
        rendered = "Five things left the queues this week.",
        renderedQuestion = null,
        slots = mapOf("n" to Slot.Count("n", value, "thing", "things")),
        sourceFacts = if (ref == null) emptyMap() else mapOf("n" to ref),
        namedItemIds = emptySet(),
    )

    // Check 4, no zeros.

    /**
     * A rule that could produce zero and carries no criterion preventing it.
     *
     * Check 3 leaves a non positive number alone precisely so that this check reports it,
     * because `Measures` answers null rather than zero and the check 3 message would have
     * said the fact was unreadable, which is true of the fact and misleading about the
     * sentence. What is wrong here is that a sentence contains a zero.
     */
    @Test
    fun `check 4 vetoes a count of zero`() {
        val veto = veto(
            ValidateFixture.candidate(
                ruleKey = "pulse.switching.s1",
                familyKey = "switching",
                variantKey = "switching.s1.01",
                rendered = "Zero swaps this week.",
                renderedQuestion = null,
                slots = mapOf("n" to Slot.Count("n", 0, "swap", "swaps")),
                sourceFacts = mapOf("n" to FactRef("window", "swaps")),
                namedItemIds = emptySet(),
            ),
            facts = ValidateFixture.facts(window = ValidateFixture.window(swaps = 0)),
        )
        assertEquals(ValidationCheck.NO_ZEROS, veto.check)
        assertTrue(veto.detail, veto.detail.contains("zero never reaches a template"))
    }

    @Test
    fun `check 4 vetoes a negative, which is the same failure with a sign on it`() {
        val veto = veto(
            ValidateFixture.candidate(
                ruleKey = "pulse.accumulation.s1",
                familyKey = "accumulation",
                variantKey = "accumulation.s1.01",
                rendered = "Two more things arrived than left.",
                renderedQuestion = null,
                slots = mapOf("k" to Slot.Count("k", -2, "thing", "things")),
                sourceFacts = mapOf("k" to FactRef("window", "outflowGap")),
                namedItemIds = emptySet(),
            ),
            facts = ValidateFixture.facts(window = ValidateFixture.window(completions = 3, additions = 5, netFlow = -2)),
        )
        assertEquals(ValidationCheck.NO_ZEROS, veto.check)
        assertTrue(veto.detail, veto.detail.contains("magnitude"))
    }

    @Test
    fun `check 4 passes the same sentence built on the magnitude instead of the signed fact`() {
        passes(
            ValidateFixture.candidate(
                ruleKey = "pulse.accumulation.s1",
                familyKey = "accumulation",
                variantKey = "accumulation.s1.01",
                rendered = "Two more things arrived than left.",
                renderedQuestion = null,
                slots = mapOf("k" to Slot.Count("k", 2, "thing", "things")),
                sourceFacts = mapOf("k" to FactRef("window", "intakeGap")),
                namedItemIds = emptySet(),
            ),
            facts = ValidateFixture.facts(window = ValidateFixture.window(completions = 3, additions = 5, netFlow = -2)),
        )
    }

    // Order.

    @Test
    fun `a candidate failing several checks is reported against the lowest numbered one`() {
        val veto = veto(
            namesReading().copy(
                slots = mapOf(
                    "areaName" to Slot.Text("areaName", "Reading"),
                    "n" to Slot.Count("n", 99, "thing", "things"),
                ),
                sourceFacts = mapOf("n" to FactRef("window", "completions")),
            ),
        )
        assertEquals(ValidationCheck.AREA_EXISTENCE, veto.check)
    }
}
