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
import java.lang.reflect.Modifier
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The non compliance test. CLARITY_LOGIC_ENGINE.md 12 last bullet, 10.6, and
 * `MASTER_BUILD_PROMPT.md` 19.
 *
 * > The persona who accepts every plan and completes none produces a simulated year in
 * > which no sentence references a plan, a commitment, an intention, or a failure to act.
 * > If a reader of that dump could tell plans were accepted, the implementation has failed
 * > and must be removed rather than tuned.
 *
 * **This file was written before the follow through code, and section 19 asks for that in
 * those words.** The reason is not ceremony. A test written after the mechanism is written
 * by somebody who already knows what the mechanism does, and it tests that rather than the
 * property. Everything asserted below was decided from section 10 and section 19 alone,
 * and the mechanism was then built to satisfy it.
 *
 * ## Four assertions, and only one of them is about sentences
 *
 * The end to end grep is the weakest of the four, because it can only find what the
 * personas happened to provoke. The three structural ones are stronger, because they close
 * the door rather than watching it:
 *
 * 1. **The corpus cannot say it.** No line in any of the section 4 benches matches the
 *    forbidden vocabulary, so no plan surface the composer can assemble does either
 * 2. **The history cannot carry it.** `PlanHistory.Accepted` has three fields and all
 *    three are keys, so there is no string about a plan for anything downstream to reach
 * 3. **The follow through cannot return it.** Every public member of `FollowThrough`
 *    returns a set of keys or a number. No signature in it can carry a sentence
 * 4. **A year of accepting and never acting says nothing about it.** The behavioral
 *    reading, run over the real corpus
 *
 * `SimulationChecks.theNonComplianceTest` is the same property asserted over eleven
 * persona years including `acceptsEveryPlan`, and both read `PlanVocabulary.FORBIDDEN` so
 * there is one list rather than two.
 */
class GuidanceNonComplianceTest {

    private val composer = GuidanceComposer(CorpusFixture.catalog, ZONE)

    /**
     * Assertion 1. Nothing an author wrote into section 4 references a plan.
     *
     * The composer can only emit lines from these benches, so this is the whole language
     * layer 6 has. It is checked against the committed corpus rather than a fixture,
     * because the file the app reads is the file that has to hold the property.
     */
    @Test
    fun `no line in the guidance benches references a plan, a commitment or an intention`() {
        val offenders = PlanBenches.of(CorpusFixture.catalog).allLines.mapNotNull { line ->
            PlanVocabulary.referenceIn(line.text)?.let { "${line.key} references $it: ${line.text}" }
        }
        assertEquals("section 4 lines referencing a plan", emptyList<String>(), offenders)
    }

    /**
     * Assertion 2. The accepted plan record is three keys and nothing else.
     *
     * Read off the class rather than asserted in prose, so that adding a field to
     * `PlanHistory.Accepted` fails here. That is the point: a leak has to be able to reach
     * a string, and the only way to give it one through this path is to add a field, which
     * this makes a visible act.
     */
    @Test
    fun `the accepted plan record carries no sentence and no verdict`() {
        val fields = PlanHistory.Accepted::class.java.declaredFields
            .filterNot { it.isSynthetic }
            .map { it.name }
            // The Compose compiler adds a `$stable` field to every class it sees. It is
            // generated rather than declared and it holds an integer.
            .filterNot { it.startsWith("$") }
            .sorted()
        assertEquals(
            "PlanHistory.Accepted must carry only the keys the two rules in 10.4 and 10.6 need. " +
                "A field beyond these three is a field a sentence about a plan could be built from",
            listOf("familyKey", "subjectId", "weekStartKey"),
            fields,
        )
    }

    /**
     * Assertion 3. The follow through has no signature that could carry a sentence.
     *
     * 10.6: "Layer 6 does not inject sentences. It sets a priority boost on the observation
     * family whose `resolutionFactRef` matches the accepted plan. The user can never be told
     * about a plan they did not keep, **because the mechanism has no way to say it**."
     *
     * This is that sentence made checkable. Every public member of the object returns a set
     * of keys or an integer, so there is no return value anywhere in the mechanism that a
     * caller could put on a screen.
     */
    @Test
    fun `the follow through mechanism cannot return a sentence`() {
        val returning = FollowThrough::class.java.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic }
            .filter { CharSequence::class.java.isAssignableFrom(it.returnType) }
            .map { it.name }
        assertEquals(
            "a public member of FollowThrough returns text. 10.6 requires the mechanism to " +
                "have no way to say anything, and a String return is a way",
            emptyList<String>(),
            returning,
        )
    }

    /**
     * Assertion 4. A year of accepting every plan and doing nothing about any of them.
     *
     * The same week is composed fifty two times with the same facts, and every plan the
     * composer offers is recorded as accepted before the next week is composed. Nothing in
     * the person's behavior ever changes, which is the persona `acceptsEveryPlan` exists to
     * be: a real, visible, repeated non compliance.
     *
     * Every string the composer produced across the year is then read. **Not one of them may
     * reference a plan**, and the reading includes the committed line, which is the one a
     * person only ever sees because they accepted something.
     */
    @Test
    fun `accepting every plan for a year and acting on none of them produces no sentence about it`() {
        val spoken = mutableListOf<String>()
        var plans = PlanHistory.EMPTY
        val accepted = mutableListOf<PlanHistory.Accepted>()

        for (week in 0 until WEEKS_IN_A_YEAR) {
            val weekStartKey = weekStartKey(week)
            val facts = frictionWeek()
            val result = composer.compose(
                headline = headline(),
                appeared = appeared(),
                facts = facts,
                plans = plans,
                history = FiringHistory.EMPTY,
                weekStartKey = weekStartKey,
            )
            when (result) {
                is GuidanceResult.Plan -> {
                    spoken += result.plan.offeredLine
                    spoken += result.plan.committedLine
                    // The persona accepts, every time, and then does nothing at all.
                    accepted += PlanHistory.Accepted(
                        weekStartKey = weekStartKey,
                        familyKey = result.plan.familyKey,
                        subjectId = result.plan.subjectId,
                    )
                    plans = PlanHistory(accepted.toList())
                }
                is GuidanceResult.Closing -> spoken += result.line.text
                is GuidanceResult.Nothing -> Unit
            }
        }

        assertTrue(
            "the year produced no guidance at all, so this test proved nothing. " +
                "The fixture week must be able to motivate a plan",
            spoken.isNotEmpty(),
        )
        assertTrue(
            "no plan was ever accepted, so the follow through was never exercised",
            accepted.isNotEmpty(),
        )
        val offenders = spoken.mapNotNull { line ->
            PlanVocabulary.referenceIn(line)?.let { "references $it: $line" }
        }
        assertEquals("a year of accepted and unkept plans said something about them", emptyList<String>(), offenders)
    }

    /**
     * The other half of assertion 4, and the half a grep cannot reach.
     *
     * A reader of the dump must not be able to **tell** plans were accepted. Greping for
     * vocabulary catches the obvious leak; this catches the subtle one, which is layer 6
     * producing a different kind of output once a plan is in the history. The set of lines
     * it can produce must be the same either way, because the only thing acceptance changes
     * is a rank inside layer 3, and a rank is not a sentence.
     */
    @Test
    fun `an accepted plan changes no sentence layer six is able to produce`() {
        val everAccepted = PlanHistory(
            listOf(PlanHistory.Accepted(weekStartKey(0), MOTIVATING_FAMILY, NEGLECTED_AREA)),
        )
        val withHistory = composer.compose(
            headline = headline(),
            appeared = appeared(),
            facts = frictionWeek(),
            plans = everAccepted,
            history = FiringHistory.EMPTY,
            weekStartKey = weekStartKey(1),
        )
        val withNone = composer.compose(
            headline = headline(),
            appeared = appeared(),
            facts = frictionWeek(),
            plans = PlanHistory.EMPTY,
            history = FiringHistory.EMPTY,
            weekStartKey = weekStartKey(1),
        )
        // The accepted plan's family is on this week's page, so 10.4 rule 4 holds the next
        // plan back. That is the whole visible consequence of an acceptance, and it is a
        // withheld plan rather than an added sentence.
        assertTrue(
            "an unresolved accepted plan must hold the next plan back, per 10.4 rule 4",
            withHistory !is GuidanceResult.Plan,
        )
        assertTrue("the same week with no history offers a plan", withNone is GuidanceResult.Plan)
        assertNull(
            "layer 6 spoke about the plan it withheld",
            (withHistory as? GuidanceResult.Closing)?.line?.text?.let { PlanVocabulary.referenceIn(it) },
        )
    }

    // ---------------------------------------------------------------- the fixture week

    /**
     * A week with a real friction pattern in it, and a rhythm a cue can be drawn from.
     *
     * `ValidateFixture`'s week already has an area that has been still for eleven days,
     * which is what `neglectedArea` is about and what the action bank in 4.3 writes lines
     * for. The cues are the only thing added, because `ValidateFixture` builds
     * `CueFacts.NONE` and a fact set with no rhythm can produce no plan at all, which would
     * make this test pass by saying nothing.
     */
    private fun frictionWeek(): FactSet = ValidateFixture.facts(cues = STABLE_RHYTHM)

    private fun headline(): Validated = Validated(
        ValidateFixture.candidate(
            purpose = Purpose.REPORT_HEADLINE,
            familyKey = "steadyPace",
            variantKey = "hd.steady.01",
            rendered = "A steady week.",
            renderedQuestion = null,
            slots = emptyMap(),
            sourceFacts = emptyMap(),
            namedItemIds = emptySet(),
            subjectId = null,
        ),
    )

    private fun appeared(): List<Validated> = listOf(
        Validated(
            ValidateFixture.candidate(
                ruleKey = MOTIVATING_RULE,
                purpose = Purpose.REPORT_OBSERVATION,
                familyKey = MOTIVATING_FAMILY,
                variantKey = "ob.neg.s2.l01",
                rendered = "Reading has been quiet for three weeks.",
                renderedQuestion = null,
                slots = emptyMap(),
                sourceFacts = emptyMap(),
                namedAreaIds = setOf(NEGLECTED_AREA),
                namedItemIds = emptySet(),
                subjectId = NEGLECTED_AREA,
            ),
        ),
    )

    private fun weekStartKey(week: Int): String = FIRST_WEEK.plusWeeks(week.toLong()).toString()

    private companion object {

        const val WEEKS_IN_A_YEAR = 52

        val ZONE: ZoneId = ZoneId.of("UTC")

        /** `neglectedArea`, which 4.3 writes an action bank for. */
        const val MOTIVATING_FAMILY: FamilyKey = "neglectedArea"

        /**
         * The real catalog key, and it has to be real.
         *
         * `neglectedArea` is flagged `absenceSubject`, which is the only reason check 1
         * lets it name an area with no events in the window. A plan inherits the
         * motivating observation's rule key precisely so it inherits that permission, so a
         * fixture carrying an invented key would test a plan nobody could ever be offered.
         */
        const val MOTIVATING_RULE = "report.observation.neglectedArea.s2"

        /** `ValidateFixture.reading()`: live, visible, twenty one days still, and in `neglectedAreaIds`. */
        const val NEGLECTED_AREA: String = ValidateFixture.READING

        val FIRST_WEEK: LocalDate = LocalDate.parse("2026-03-08")

        /**
         * A rhythm every one of whose cues cleared the three thresholds in 3.7.
         *
         * Built by hand rather than extracted, because this test is about what the composer
         * does with a rhythm and `CueFactsTest` is about whether one exists. A cue below
         * threshold is `CueSubstantiationTest`'s subject.
         */
        val STABLE_RHYTHM = CueFacts(
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
