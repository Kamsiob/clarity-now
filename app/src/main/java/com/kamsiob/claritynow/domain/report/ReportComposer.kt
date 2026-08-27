package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ReportIntegrity
import com.kamsiob.claritynow.domain.engine.validate.ReportVerdict
import java.time.ZoneId

/**
 * Layer 3 to 5 for a whole week, plus section 9's composition rules.
 * CLARITY_LOGIC_ENGINE.md 9 and `MASTER_BUILD_PROMPT.md` 11.3 and 12.3.
 *
 * ## The sequence, exactly as 11.3 writes it
 *
 * ```
 * 1. Window is the trailing 7 days ending today             -> ReportSchedule
 * 2. FactExtractor -> FactSet, including CueFacts over 12 weeks
 * 3. Select the headline FIRST. It constrains everything after it
 * 4. Select 2 to 4 observations, applying the incompatibility matrix against the
 *    headline and against each other, plus the length band and parallel clause rules
 * 5. Select at most one pattern, only if weeksOfData >= 3
 * 6. Realize and validate each. Vetoed candidates fall through to the next ranked selection
 * 7. Pass ONLY the observations that ACTUALLY APPEARED into GuidanceComposer
 * 8. GuidanceComposer returns a plan, a non-plan closing, or nothing
 * 9. Write REPORT_GENERATED, and PLAN_OFFERED if a plan was produced
 * ```
 *
 * Steps 3 to 6 are [compose]. Steps 1 and 2 are the caller's, because they need a clock and
 * a query facade and this class needs neither. Steps 7 and 8 are layer 6, which is phase 9b
 * and which takes the observations that appeared; [ClarityReport.observations] is that list
 * and there is deliberately no other. Step 9 is the repository's, because
 * `ClarityRepository` is the only writer in the app.
 *
 * ## What composition adds to what the engine already does
 *
 * The engine picks the best thing to say and proves it true. It cannot see the page. Four
 * rules only exist at the scale of the page, and they are here:
 *
 * - **Reading order.** Where each observation is read, and therefore what follows what
 * - **The area mention cap.** One area named three times makes a report about an area
 * - **The parallel clause cap.** Three numeric parallels in a row is the three part list
 * - **The intent gate.** 12.3: a callback insight needs three or more answered pulses
 *
 * ## Nothing is ever padded, and nothing is ever backfilled
 *
 * 11.4: never pad a section to reach a minimum. One qualifying observation means one
 * observation. The rule has a second half that is easier to break without noticing: when a
 * composition rule above removes an observation, **the composer does not reach for another
 * one to take its place.** The obvious implementation asks the engine for eight and keeps
 * the first four that survive, which is padding with extra steps: the fifth ranked
 * observation was not worth saying when the page had four things on it and it does not
 * become worth saying because one of them was dropped for naming the wrong area twice.
 *
 * So [compose] asks for four, and a report where two of them were dropped is a report of
 * two. [ClarityReport.dropped] records every one, because a report of two is otherwise
 * indistinguishable from a quiet week and those are different states.
 */
class ReportComposer(private val catalog: ClarityCatalog, private val zone: ZoneId) {

    /**
     * Step 6's constructor call, quoted from 11.3 rather than parameterized.
     *
     * There is no seam for a validator here for the same reason `PulseGenerator` has none:
     * a caller that accepted one would be a way to hand the engine a validator that vetoes
     * nothing. 11.4, never bypass the validator, not for a simple sentence, not for an empty
     * state, not to fix a bug.
     */
    private val engine = ClarityEngine(catalog, ClarityValidator(zone), zone)

    private val language = ReportLanguage(catalog, zone)

    private val integrity = ReportIntegrity(catalog, zone)

    /**
     * One report for the week [facts] describes.
     *
     * [history] must be rebuilt from the whole log on every call, per 11.7. It carries the
     * ninety day variant exclusion, the fourteen day family cooldowns and the escalation
     * ladders, and caching it is how two devices holding one log start disagreeing.
     */
    fun compose(facts: FactSet, history: FiringHistory, weekStartKey: String): ReportOutcome {
        val dateKey = engine.momentOf(facts).dateKey

        // 12.3's edge case, and it comes before selection rather than after it. A week with
        // no events has nothing for a rule to qualify on, so every branch below would answer
        // nothing anyway; asking first is what makes the difference between a page that says
        // one true sentence and a page that is blank.
        if (facts.window.totalEvents == 0) {
            return ReportOutcome.Empty(
                weekStartKey = weekStartKey,
                note = language.nothingToReport(facts, dateKey),
            )
        }

        // 3. The headline first. Everything after it is constrained by it, and a conflicting
        // observation is excluded entirely rather than deprioritized, which is what passing
        // its family into the observation pass does.
        val headline = spoken(engine.observe(facts, history, Purpose.REPORT_HEADLINE))

        // 4 and 6. The engine's own loop: the family exclusion, the incompatibility matrix,
        // the editorial budget and the length band alternation, with every candidate realized
        // and validated before it comes back.
        val selected = engine
            .observeObservations(facts, history, headline?.familyKey, MAX_OBSERVATIONS)
            .map { it.meta }

        // 5. At most one pattern, and only with three weeks behind it. No trend means the
        // section is omitted entirely rather than filled with a line saying there is none.
        val pattern = if (facts.history.weeksOfData >= ReportIntegrity.PATTERN_WEEKS) {
            spoken(engine.observe(facts, history, Purpose.REPORT_PATTERN))
        } else {
            null
        }

        return assemble(headline, selected, pattern, facts, dateKey, weekStartKey)
    }

    /**
     * The page, assembled from lines that have already been realized and validated.
     *
     * Separate from [compose] because every rule in section 9 is a property of a set of
     * sentences rather than of the machinery that chose them, and a test that has to make
     * the engine produce a violating set in order to check the rule that catches it is a
     * test of the engine. Internal rather than private for exactly that: the composition
     * tests drive this with candidates built by hand.
     */
    internal fun assemble(
        headline: Candidate?,
        observations: List<Candidate>,
        pattern: Candidate?,
        facts: FactSet,
        dateKey: String,
        weekStartKey: String,
    ): ReportOutcome {
        val dropped = mutableListOf<DroppedLine>()
        val gated = intentGate(observations, facts, dropped)
        val within = areaMentionCap(gated, facts, dropped)
        val budgeted = editorialBudget(within, dropped)
        val arranged = arrange(budgeted)
        val kept = parallelClauseCap(arranged, dropped)

        val basis = language.basis(facts, dateKey)
        val report = ClarityReport(
            weekStartKey = weekStartKey,
            headline = headline,
            observations = kept,
            pattern = pattern,
            basis = basis,
            generated = language.generatedLine(),
            firstWeekNote = if (facts.history.isFirstWeekEver) language.firstWeek(facts, dateKey) else null,
            totals = language.totals(facts),
            numbers = emptyMap(),
            dropped = dropped.toList(),
        )
        val numbered = report.copy(numbers = numbersOf(report))
        return when (val verdict = integrity.inspect(numbered.lines, facts)) {
            is ReportVerdict.Passed -> ReportOutcome.Composed(numbered)
            is ReportVerdict.Vetoed -> ReportOutcome.Suppressed(weekStartKey, verdict)
        }
    }

    // ------------------------------------------------------------------- the rules

    /**
     * `MASTER_BUILD_PROMPT.md` 12.3. Intent qualified insights need three answered pulses.
     *
     * > Intent-qualified insights require 3 or more answered pulses in the window; below
     * > that the report is trail data only.
     *
     * The two families that quote a stored answer are the intent qualified ones, and
     * `completionSplit` already carries the floor as a criterion. `selfReportVsData` does
     * not: its rule requires a resolvable callback and one stored answer ever, which is the
     * right condition for the quote to be real and the wrong one for the claim to be
     * representative. One answer is an anecdote and the flagship observation sets it against
     * a week of behavior.
     *
     * **This drops rather than excluding from selection**, which costs a slot, and the clean
     * fix is a criterion on the rule or a family exclusion parameter on
     * `ClarityEngine.observeObservations`. Both are in files this slice does not own, and
     * both are recorded rather than worked around.
     */
    private fun intentGate(
        observations: List<Candidate>,
        facts: FactSet,
        dropped: MutableList<DroppedLine>,
    ): List<Candidate> {
        if (facts.pulse.answeredInWindow >= INTENT_QUALIFIED_ANSWERS) return observations
        return observations.filter { candidate ->
            val gated = candidate.familyKey in ReportSection.CALLBACK_FAMILIES
            if (gated) {
                dropped += DroppedLine(
                    candidate.variantKey,
                    candidate.familyKey,
                    "quotes an answer and only ${facts.pulse.answeredInWindow} pulses were " +
                        "answered in the window, against the $INTENT_QUALIFIED_ANSWERS 12.3 requires",
                )
            }
            !gated
        }
    }

    /**
     * CLARITY_LOGIC_ENGINE.md 9.2. No area named in more than two of the four observations.
     *
     * Applied down the ranked order, so the observation that loses is the lower ranked one,
     * which is the same direction the incompatibility matrix resolves in.
     */
    private fun areaMentionCap(
        observations: List<Candidate>,
        facts: FactSet,
        dropped: MutableList<DroppedLine>,
    ): List<Candidate> {
        val mentions = mutableMapOf<AreaId, Int>()
        val kept = mutableListOf<Candidate>()
        for (candidate in observations) {
            val over = candidate.namedAreaIds.firstOrNull {
                (mentions[it] ?: 0) >= ReportIntegrity.MAX_AREA_MENTIONS
            }
            if (over != null) {
                val name = facts.areas[over]?.nameSnapshot ?: over
                dropped += DroppedLine(
                    candidate.variantKey,
                    candidate.familyKey,
                    "would name $name a third time, and 9.2 allows two of the four observations",
                )
                continue
            }
            candidate.namedAreaIds.forEach { mentions[it] = (mentions[it] ?: 0) + 1 }
            kept += candidate
        }
        return kept
    }

    /**
     * CLARITY_LOGIC_ENGINE.md 7.4 step 3 and `CORPUS_2_REPORT.md` 7.5. Two editorial leads.
     *
     * The engine spends the budget while it realizes, and a third editorial lead is
     * re-realized in the observational register there, which is what 7.5 asks for and what
     * cannot be done from here: re-realizing means going back to the bench, and the composer
     * holds finished sentences. So this is the backstop for a budget that did not hold, and
     * it drops rather than re-realizing.
     *
     * **It is unreachable through [compose]**, and it is here anyway. Editorial voice
     * attached to an ordinary fact is the clearest tell of generated writing, per the
     * corpus's own authoring rules, and three of them in a page of five sentences is the
     * whole report reading as written by a machine trying to sound like a writer.
     */
    private fun editorialBudget(
        observations: List<Candidate>,
        dropped: MutableList<DroppedLine>,
    ): List<Candidate> {
        var spent = 0
        val kept = mutableListOf<Candidate>()
        for (candidate in observations) {
            if (candidate.register == Register.EDITORIAL) {
                if (spent >= EDITORIAL_BUDGET) {
                    dropped += DroppedLine(
                        candidate.variantKey,
                        candidate.familyKey,
                        "is a third editorial lead, and 7.4 allows two per report",
                    )
                    continue
                }
                spent++
            }
            kept += candidate
        }
        return kept
    }

    /**
     * Reading order: the sections in the order `design-v3.md` 11.1 lists them, and inside a
     * section the highest ranked line whose length band is not the one just used.
     *
     * ## Why the observations are grouped rather than left in rank order
     *
     * Rank order is what the engine returns and it is one plausible reading of the design.
     * It has a defect that only shows on the page: three observations can arrive as general,
     * focus, general, and the screen then draws `Your week, honestly` twice with `Focus`
     * between them. A repeated sidehead reads as a bug, and the sideheads are the only
     * structure a page of prose has.
     *
     * ## What grouping costs, and how it is paid
     *
     * 7.5 forbids two consecutive leads from the same length band, and the engine applies
     * that while it realizes, in rank order. Regrouping can therefore put two leads of one
     * band together. So the band rule is applied again here, over the order the page is
     * actually read in, as the same preference the realizer treats it as: the next line is
     * the highest ranked one in this section whose band differs, and where every remaining
     * line in the section shares the band, the highest ranked one is taken anyway.
     *
     * 11.4 forbids padding a section to reach a minimum, and dropping a true observation to
     * improve the cadence is the same trade in the other direction. **Rhythm is worth a
     * line, not a paragraph.**
     */
    private fun arrange(observations: List<Candidate>): List<ReportObservation> {
        val sections = observations.groupBy { ReportSection.of(it.familyKey) }
        val out = mutableListOf<ReportObservation>()
        var previous: LengthBand? = null
        for (section in ReportSection.entries) {
            val remaining = sections[section].orEmpty().toMutableList()
            while (remaining.isNotEmpty()) {
                val pick = remaining.firstOrNull { it.lengthBand != previous } ?: remaining.first()
                remaining.remove(pick)
                previous = pick.lengthBand
                out += ReportObservation(section, pick)
            }
        }
        return out
    }

    /**
     * CLARITY_LOGIC_ENGINE.md 7.5 and `CORPUS_2_REPORT.md` 7.4b. At most two in a row.
     *
     * > No more than two parallel numeric clauses may appear consecutively. Where a third
     * > would follow, the composer drops it or re-realizes it at a different length. The
     * > three-part list is a rhetorical reflex and once a reader sees it they cannot stop
     * > seeing it.
     *
     * ## What counts as one, which the specification leaves open
     *
     * A lead is a parallel numeric clause here when it **renders two or more numbers**. That
     * is the shape the corpus actually writes them in, *You added 9 things and finished 6*,
     * one number set against another inside one sentence, and three of those in a row is the
     * three part list at the only scale a composer can see it.
     *
     * **The obvious reading is any lead containing a number at all**, and it is wrong in a
     * way that would be hard to find afterwards: nearly every observation in this corpus
     * carries one number, so capping runs of them at two would silently drop the third and
     * fourth observation of almost every report, and the reports would get quietly shorter
     * with no test failing. Dropping a true observation for cadence is the same trade 11.4
     * forbids in the other direction. Section 15, and the choice is recorded rather than
     * assumed.
     *
     * Re-realizing at a different length, the other resolution 7.5 offers, needs the bench
     * and the composer holds finished sentences, so this drops.
     */
    private fun parallelClauseCap(
        observations: List<ReportObservation>,
        dropped: MutableList<DroppedLine>,
    ): List<ReportObservation> {
        var run = 0
        val kept = mutableListOf<ReportObservation>()
        for (observation in observations) {
            if (!isParallelNumeric(observation.candidate)) {
                run = 0
                kept += observation
                continue
            }
            if (run >= MAX_PARALLEL_CLAUSES) {
                dropped += DroppedLine(
                    observation.candidate.variantKey,
                    observation.candidate.familyKey,
                    "would be the third parallel numeric lead in a row, and 7.4b allows two",
                )
                continue
            }
            run++
            kept += observation
        }
        return kept
    }

    /**
     * 9.2's map: every rendered numeric slot in the whole report against its [FactRef].
     *
     * The three totals beneath the ribbon are entered too. They are numbers on the page, the
     * screen prints them, and 9.2 says the map holds every one; leaving them out would make
     * the one number a reader compares against the ribbon the one number nothing checks.
     *
     * A slot with no reference is not entered and is not lost: report check 4 vetoes it, and
     * entering it under a reference it does not have would be the same fabrication one line
     * lower down. The map is keyed by reference and keeps the first value entered under each,
     * because it is read to answer what the report already states for a fact; two lines
     * disagreeing about one fact is caught by report check 3, which walks the lines
     * themselves rather than this map and therefore cannot be blinded by it.
     */
    private fun numbersOf(report: ClarityReport): Map<FactRef, ReportNumber> {
        val numbers = LinkedHashMap<FactRef, ReportNumber>()
        for (line in report.lines) {
            for (slot in line.candidate.slots.values.sortedBy { it.key }) {
                val value = slot.numericValue ?: continue
                val ref = line.candidate.sourceFacts[slot.key] ?: continue
                numbers.putIfAbsent(ref, ReportNumber(value, slot.key, line.candidate.variantKey))
            }
        }
        for (total in report.totals) {
            numbers.putIfAbsent(
                total.ref,
                ReportNumber(total.value, total.measure, ReportNumber.RIBBON_CAPTION),
            )
        }
        return numbers
    }

    private fun isParallelNumeric(candidate: Candidate): Boolean =
        candidate.slots.values.count { it.numericValue != null } >= PARALLEL_CLAUSE_NUMBERS

    private fun spoken(result: EngineResult): Candidate? = (result as? EngineResult.Spoke)?.output?.meta

    companion object {

        /** Section 5, second paragraph. Two to four, and four is the ceiling. */
        const val MAX_OBSERVATIONS = 4

        /** 7.4 step 3. */
        const val EDITORIAL_BUDGET = 2

        /** 7.4b. Two consecutive, never three. */
        const val MAX_PARALLEL_CLAUSES = 2

        /** How many numbers make a lead a parallel numeric clause. See [parallelClauseCap]. */
        const val PARALLEL_CLAUSE_NUMBERS = 2

        /** 12.3. Below this, the report is trail data only. */
        const val INTENT_QUALIFIED_ANSWERS = 3
    }
}
