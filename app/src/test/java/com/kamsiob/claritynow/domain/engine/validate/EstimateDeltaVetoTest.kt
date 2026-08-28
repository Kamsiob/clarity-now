package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.EstimateTendency
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.catalog.CatalogIntegrity
import com.kamsiob.claritynow.domain.engine.catalog.ClarityRule
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Subjects
import com.kamsiob.claritynow.domain.engine.catalog.estimateFloor
import com.kamsiob.claritynow.domain.engine.catalog.window
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.realize.FactLookup
import com.kamsiob.claritynow.domain.engine.realize.MeasureKind
import com.kamsiob.claritynow.domain.engine.realize.MeasureValue
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.realize.Slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Check 11, and the floor under it. `MASTER_BUILD_PROMPT.md` 14b.8, Addendum 01 7a.
 *
 * ## What this file is asserting
 *
 * 14b.8 gives one permitted line and two forbidden ones, by name:
 *
 * | | |
 * |---|---|
 * | permitted | `Things you estimate at an hour tend to take about three.` |
 * | forbidden | `You underestimated by two hours.` |
 * | forbidden | `You were off by 140 percent.` |
 *
 * All three are built below, against a fact set that is internally consistent, and the
 * permitted one is built first so that a veto beneath it means something. Section 17 asks
 * for exactly this shape, and it names the reason: "a veto test constructs the forbidden
 * form and proves it cannot render", because a validator whose failure branch never
 * executes is a validator nobody has verified.
 *
 * ## Why the candidates are hand built rather than realized
 *
 * There is no estimate observation family yet. Phase 9 authors it, per 14b.8, and until
 * then no rule reads an estimate fact and no bench holds an estimate line. **That is the
 * honest version of this test rather than a limitation of it.** A veto tested against the
 * output of a catalog that cannot produce the forbidden form proves that nothing currently
 * triggers the check; building the form by hand proves the check.
 *
 * The prohibition also holds above this layer, by arithmetic rather than by inspection: no
 * quantity of minutes exists anywhere in the fact set, so `actual - estimate` is not a
 * subtraction any rule or template can write. This is the backstop 14b.8 asks for anyway,
 * for a number arriving some other way, and the two forbidden candidates below are what
 * arriving some other way looks like.
 */
class EstimateDeltaVetoTest {

    private val validator = ClarityValidator(ZoneId.of("UTC"))

    /** Five estimated completions, and a median stay of two and a half times the estimate. */
    private fun calibratedWeek(
        estimatedCompletions: Int = 6,
        ratio: Double? = 2.6,
    ): FactSet = ValidateFixture.facts(
        history = ValidateFixture.history(
            estimatedCompletions = estimatedCompletions,
            activeToEstimateRatio = ratio,
        ),
    )

    /** A candidate with no entity references, so nothing but the sentence is under test. */
    private fun line(
        rendered: String,
        slots: Map<String, Slot> = emptyMap(),
        sourceFacts: Map<String, FactRef> = emptyMap(),
    ): Candidate = ValidateFixture.candidate(
        ruleKey = "report.observation.estimateCalibration",
        familyKey = "estimateCalibration",
        variantKey = "estimateCalibration.s1.01",
        purpose = Purpose.REPORT_OBSERVATION,
        stage = 1,
        rendered = rendered,
        renderedQuestion = null,
        slots = slots,
        sourceFacts = sourceFacts,
        namedAreaIds = emptySet(),
        namedItemIds = emptySet(),
        subjectId = null,
    )

    private fun veto(candidate: Candidate, facts: FactSet = calibratedWeek()): ValidationResult.Vetoed {
        val result = validator.validate(candidate, facts)
        assertTrue("expected a veto and the candidate passed: ${candidate.rendered}", result is ValidationResult.Vetoed)
        return result as ValidationResult.Vetoed
    }

    private fun passes(candidate: Candidate, facts: FactSet = calibratedWeek()) {
        val result = validator.validate(candidate, facts)
        assertTrue("expected this to pass and it was vetoed: $result", result is ValidationResult.Passed)
    }

    // ------------------------------------------------------------- the permitted form

    /**
     * The line 14b.8 permits, with its number traced to a real measure.
     *
     * `estimateMultiple` reads the median ratio rounded, which is three for a person whose
     * estimated things typically sit active two and a half times as long as they said. The
     * ref is a real address and `FactLookup` reads it back, so this passes by being
     * checkable rather than by being unreadable in a way the check tolerates.
     */
    @Test
    fun `a ratio sentence renders, and its number re-reads through the measure it came from`() {
        val ref = FactRef("history", "estimateMultiple")
        val candidate = line(
            rendered = "Things you estimate at an hour tend to take about three.",
            slots = mapOf("multiple" to Slot.Count("multiple", 3, "time", "times")),
            sourceFacts = mapOf("multiple" to ref),
        )
        assertEquals(MeasureValue.Number(3), FactLookup.read(calibratedWeek(), ref, ZoneId.of("UTC")))
        passes(candidate)
    }

    /** A tendency with no number in it at all is a sentence this check has no quarrel with. */
    @Test
    fun `a tendency sentence with no number renders`() {
        passes(line("Things you estimate tend to stay active longer than the day you pictured."))
    }

    // ------------------------------------------------------------- the forbidden forms

    /** 14b.8's first forbidden line, word for word. */
    @Test
    fun `the first forbidden line cannot render`() {
        val veto = veto(line("You underestimated by two hours."))
        assertEquals(ValidationCheck.ESTIMATE_DELTA, veto.check)
        assertTrue(veto.detail, veto.detail.contains("underestimated"))
    }

    /** 14b.8's second forbidden line, word for word, and it never says the word estimate. */
    @Test
    fun `the second forbidden line cannot render, and it does not mention an estimate at all`() {
        val rendered = "You were off by 140 percent."
        assertFalse(
            "this line is the reason check 11 does not require the word estimate nearby",
            ValidatorVocabulary.ESTIMATE_MENTION.containsMatchIn(rendered),
        )
        assertEquals(ValidationCheck.ESTIMATE_DELTA, veto(line(rendered)).check)
    }

    /** The same claim in the other direction, and in the shapes a rewrite would reach for. */
    @Test
    fun `every other way of saying the same difference is refused too`() {
        val forms = listOf(
            "You overestimated by an hour.",
            "That took three days longer than you estimated.",
            "Four things ran past their estimate.",
            "Your estimate was out by half a day.",
            "You missed your estimate on six things.",
            "That one was over by two days.",
        )
        for (form in forms) {
            assertEquals("`$form` should be refused by check 11", ValidationCheck.ESTIMATE_DELTA, veto(line(form)).check)
        }
    }

    // ------------------------------------------------------------- the shape rule

    /**
     * A count measure funneled into a percentage slot, which is the number arriving some
     * other way that 14b.8 says the veto is the backstop for.
     *
     * Nothing in `Measures` can produce this today: both estimate measures are counts. The
     * rule is written against the slot rather than against the table precisely because the
     * table is the half somebody could change.
     */
    @Test
    fun `a percentage in a sentence about an estimate cannot render`() {
        val candidate = line(
            rendered = "Your estimates held 60 percent of the time.",
            slots = mapOf("pct" to Slot.Percent("pct", 60)),
            sourceFacts = mapOf("pct" to FactRef("history", "estimatedCompletions")),
        )
        val veto = veto(candidate, calibratedWeek(estimatedCompletions = 60))
        assertEquals(ValidationCheck.ESTIMATE_DELTA, veto.check)
        assertTrue(veto.detail, veto.detail.contains("multiple"))
    }

    /** The sentence need not say so. A number that came from an estimate is about an estimate. */
    @Test
    fun `a percentage whose fact is an estimate cannot render even when the sentence is silent about it`() {
        val candidate = line(
            rendered = "Six of ten landed where you thought.",
            slots = mapOf("pct" to Slot.Percent("pct", 60)),
            sourceFacts = mapOf("pct" to FactRef("history", "estimatedCompletions")),
        )
        assertEquals(
            ValidationCheck.ESTIMATE_DELTA,
            veto(candidate, calibratedWeek(estimatedCompletions = 60)).check,
        )
    }

    /**
     * The control, and the failure this file is written against is a check that vetoes
     * everything.
     *
     * A percentage is ordinary in this app. `78 percent of the week was Work` is an
     * approved shape, and check 11 has to leave it alone.
     */
    @Test
    fun `a percentage that has nothing to do with an estimate renders`() {
        val candidate = line(
            rendered = "Work held 75 percent of the week.",
            slots = mapOf("pct" to Slot.Percent("pct", 75)),
            sourceFacts = mapOf("pct" to FactRef("area", "areaShare:${ValidateFixture.WORK}")),
        )
        passes(candidate)
    }

    // ------------------------------------------------------------- the floor of five

    /**
     * The floor, at the value 14b.8 sets and on the fact it sets it on.
     *
     * Four estimated completions is not a tendency, and the criterion says so before any
     * language is chosen. The fact refuses in parallel: `activeToEstimateRatio` is null
     * under the floor and `estimateTendency` is `INSUFFICIENT`, so a family reading either
     * one cannot fire on four items whatever its criteria say.
     */
    @Test
    fun `no estimate observation may fire below five estimated completions`() {
        val floor = estimateFloor()
        assertEquals(5, EstimateTendency.MIN_COMPLETIONS)
        assertFalse(floor.test(calibratedWeek(estimatedCompletions = 4), null))
        assertTrue(floor.test(calibratedWeek(estimatedCompletions = 5), null))
    }

    /** The count itself travels, so the validator re-reads the number that gated the sentence. */
    @Test
    fun `the count that gates the sentence is a fact the validator can re-read`() {
        val ref = FactRef("history", "estimatedCompletions")
        assertNotNull("14b.8 requires the count to reach the validator as a FactRef", FactLookup.measureOf(ref))
        assertEquals(
            MeasureValue.Number(6),
            FactLookup.read(calibratedWeek(estimatedCompletions = 6), ref, ZoneId.of("UTC")),
        )
    }

    /** Both estimate measures are counts, which is 14b.8's `a multiple and never a percentage`. */
    @Test
    fun `no estimate measure produces a percentage, and none produces a quantity of minutes`() {
        val estimate = Measures.ALL.filter { it.id.startsWith(Measures.ESTIMATE_MEASURE_PREFIX) }
        assertEquals(
            "the two 14b.8 declares",
            listOf("estimatedCompletions", "estimateMultiple"),
            estimate.map { it.id },
        )
        assertTrue(
            "a ratio of 2.4 rendered as 240 percent is one literal hundred from the second " +
                "forbidden line, so the reading is a multiple and the measures are counts",
            estimate.all { it.kind == MeasureKind.COUNT },
        )
        assertTrue(
            "neither counts a unit of time. The delta 14b.8 bans is a quantity of minutes, " +
                "and no measure hands one out because no fact holds one",
            estimate.none { it.singular in setOf("minute", "hour", "day", "week") },
        )
    }

    /**
     * The floor is enforced the way the share floor is enforced, and the check would catch
     * a rule that skipped it.
     *
     * There is no estimate rule in the catalog today, so the check finds nothing, which is
     * why this builds one that reads an estimate fact without the floor and asserts the
     * finding. A check nobody has seen fire is a check nobody has verified.
     */
    @Test
    fun `the catalog check catches an estimate rule with no floor of five`() {
        assertTrue(
            "the shipped catalog has no estimate rule yet, so the check should find nothing in it",
            CatalogIntegrity.estimateRulesCarryAFloor(CorpusFixture.catalog.rules).isEmpty(),
        )
        val unguarded = ClarityRule(
            key = "report.observation.estimateTest",
            purpose = setOf(Purpose.REPORT_OBSERVATION),
            family = "estimateCalibration",
            subject = Subjects.NONE,
            criteria = listOf(
                window("estimate.tendency.longer", "things stay active longer than they were estimated at") {
                    it.history.estimateTendency == EstimateTendency.LONGER
                },
            ),
            priority = 0,
            horizonDays = 90,
            unflattering = false,
            stage = 1,
        )
        assertEquals(
            listOf("estimate rule with no floor of five"),
            CatalogIntegrity.estimateRulesCarryAFloor(CorpusFixture.catalog.rules + unguarded).map { it.check },
        )
    }
}
