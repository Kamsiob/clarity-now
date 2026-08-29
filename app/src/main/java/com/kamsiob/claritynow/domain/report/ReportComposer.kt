package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.AreaId
import com.kamsiob.claritynow.domain.engine.ClarityEngine
import com.kamsiob.claritynow.domain.engine.EngineResult
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.engine.FactSet
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.Validated
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.LengthBand
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.realize.Candidate
import com.kamsiob.claritynow.domain.engine.validate.ClarityValidator
import com.kamsiob.claritynow.domain.engine.validate.ReportIntegrity
import com.kamsiob.claritynow.domain.engine.validate.ReportVerdict
import com.kamsiob.claritynow.domain.engine.validate.ValidationResult
import com.kamsiob.claritynow.domain.guidance.FollowThrough
import com.kamsiob.claritynow.domain.guidance.GuidanceComposer
import com.kamsiob.claritynow.domain.guidance.GuidanceResult
import com.kamsiob.claritynow.domain.guidance.PlanHistory
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
 * a query facade and this class needs neither. Steps 7 and 8 are [closing], which hands
 * layer 6 exactly the observations the page will carry and nothing else. Step 9 is the
 * repository's, because `ClarityRepository` is the only writer in the app.
 *
 * ## What composition adds to what the engine already does
 *
 * The engine picks the best thing to say and proves it true. It cannot see the page. Four
 * rules only exist at the scale of the page, and they are here:
 *
 * - **Reading order.** Where each observation is read, and therefore what follows what
 * - **The area mention cap.** One area named three times makes a report about an area
 * - **The two rhythm rules of 9.2.** The length band alternation, and the cap on runs of
 *   numeric leads. Both decide the order and neither removes a line; see [arrange]
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
 *
 * **And nothing is ever dropped for rhythm.** The three rules that remove a line remove it
 * because it would state something the report may not state: an area named a third time, a
 * third editorial voice, an answer quoted on the strength of one pulse. The two rules in
 * 9.2 that are about cadence decide the **order** and take the highest ranked line anyway
 * when the order cannot be made to hold them. A report that is allowed to be short is not
 * thereby allowed to lose a true sentence to its own rhythm.
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

    private val guidance = GuidanceComposer(catalog, zone)

    /**
     * Layer 5 again, for one purpose: minting the `Validated` layer 6 takes.
     *
     * `Validated` is constructed by layer 5 and by nothing else, and 10.4 rule 2 rests on
     * that: layer 6 takes a `List<Validated>` so it cannot be handed a sentence that was
     * vetoed. The engine validates every candidate it returns and then throws the proof
     * away, because `CandidateValidator.veto` answers a reason rather than a token.
     *
     * So the kept observations are validated a second time here rather than wrapped. It
     * costs five validations per report and it keeps the type's guarantee exact: the
     * `Validated` layer 6 receives really was minted by layer 5, for that candidate,
     * against these facts. Wrapping instead would make the type a promise this file makes
     * rather than one layer 5 makes, and the promise is the only thing holding rule 2 up.
     */
    private val validator = ClarityValidator(zone)

    /**
     * One report for the week [facts] describes.
     *
     * [history] must be rebuilt from the whole log on every call, per 11.7. It carries the
     * ninety day variant exclusion, the fourteen day family cooldowns and the escalation
     * ladders, and caching it is how two devices holding one log start disagreeing.
     */
    fun compose(
        facts: FactSet,
        history: FiringHistory,
        weekStartKey: String,
        plans: PlanHistory = PlanHistory.EMPTY,
    ): ReportOutcome {
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
        //
        // The boost is layer 6's follow through, 10.6, and it is the only thing an accepted
        // plan ever does. It reorders observations of equal specificity and cannot make one
        // qualify, so a family a person accepted a plan about still has to earn its place
        // here on its own merits. See `FollowThrough` and `Selector.FOLLOW_THROUGH_BOOST`.
        val selected = engine
            .observeObservations(
                facts,
                history,
                headline?.familyKey,
                MAX_OBSERVATIONS,
                FollowThrough.boosted(plans, weekStartKey),
            )
            .map { it.meta }

        // 5. At most one pattern, and only with three weeks behind it. No trend means the
        // section is omitted entirely rather than filled with a line saying there is none.
        val pattern = if (facts.history.weeksOfData >= ReportIntegrity.PATTERN_WEEKS) {
            spoken(engine.observe(facts, history, Purpose.REPORT_PATTERN))
        } else {
            null
        }

        return assemble(headline, selected, pattern, facts, dateKey, weekStartKey, plans, history)
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
        plans: PlanHistory = PlanHistory.EMPTY,
        history: FiringHistory = FiringHistory.EMPTY,
    ): ReportOutcome {
        val dropped = mutableListOf<DroppedLine>()
        val gated = intentGate(observations, facts, dropped)
        val within = areaMentionCap(gated, facts, dropped)
        val budgeted = editorialBudget(within, dropped)
        val kept = arrange(budgeted, headline)

        val basis = language.basis(facts, dateKey)
        val report = ClarityReport(
            weekStartKey = weekStartKey,
            headline = headline,
            observations = kept,
            pattern = pattern,
            patternNote = patternNote(facts, dateKey),
            basis = basis,
            generated = language.generatedLine(),
            firstWeekNote = if (facts.history.isFirstWeekEver) language.firstWeek(facts, dateKey) else null,
            totals = language.totals(facts),
            numbers = emptyMap(),
            dropped = dropped.toList(),
            closing = closing(headline, kept, facts, plans, history, weekStartKey),
        )
        val numbered = report.copy(numbers = numbersOf(report))
        return when (val verdict = integrity.inspect(numbered.lines, facts)) {
            is ReportVerdict.Passed -> ReportOutcome.Composed(numbered)
            is ReportVerdict.Vetoed -> ReportOutcome.Suppressed(weekStartKey, verdict)
        }
    }

    /**
     * 11.3 steps 7 and 8. Layer 6, handed only what the reader will actually see.
     *
     * **`kept` is the whole of 10.4 rule 2 and it is why this is here rather than earlier.**
     * The observations the engine selected are not the observations the report shows: the
     * intent gate, the area mention cap and the editorial budget each remove lines, and a
     * plan motivated by a line that was dropped would refer to something that did not
     * happen. So layer 6 runs after every composition rule has taken what it takes, on the
     * final list, in reading order.
     *
     * The pattern is deliberately not passed. Sections 6.3 and 10.4 make a plan a response
     * to a week, and a pattern is a statement about three or more of them; an action
     * completable inside one week, 10.4 rule 5, cannot be a response to a shape that took a
     * month to form.
     */
    private fun closing(
        headline: Candidate?,
        kept: List<ReportObservation>,
        facts: FactSet,
        plans: PlanHistory,
        history: FiringHistory,
        weekStartKey: String,
    ): GuidanceResult = guidance.compose(
        headline = headline?.let { validated(it, facts) },
        appeared = kept.mapNotNull { validated(it.candidate, facts) },
        facts = facts,
        plans = plans,
        history = history,
        weekStartKey = weekStartKey,
    )

    /**
     * Layer 5's proof that [candidate] may be shown, or null.
     *
     * Null cannot happen through [compose], because the engine already refused every
     * candidate the validator vetoed. It can happen through [assemble], which the
     * composition tests drive with candidates built by hand, and refusing there is right:
     * a line layer 5 will not pass is not a line layer 6 may be motivated by.
     */
    private fun validated(candidate: Candidate, facts: FactSet): Validated? =
        (validator.validate(candidate, facts) as? ValidationResult.Passed)?.validated

    /**
     * The pattern section's empty state, or null. `CORPUS_2_REPORT.md` 3.16.
     *
     * ## This is deliberate, and it is not a bug to be fixed back into the engine
     *
     * Every sentence in this app about a person's own data comes out of a corpus file
     * through the engine layers in order, and there is no second path. **This line is an
     * authorized exception, decided by the owner, and it is narrow enough to state
     * exactly:** `insufficientData` is not a pattern, it is the pattern section's empty
     * state. Its four lines say that there are not three weeks of history yet. There is no
     * subject, no number, no escalation and no claim about the person, so there is nothing
     * for a rule to decide and nothing for the validator to disbelieve.
     *
     * It was written as a rule and the rule could never fire. Step 5 above asks the engine
     * for a pattern only when `weeksOfData >= ReportIntegrity.PATTERN_WEEKS`, and the rule
     * required fewer weeks than that, so the two conditions were complements and the family
     * had four authored lines that could not be reached. Restoring the rule means making
     * the composer ask for a pattern it has already decided it does not want, so that a
     * rule can answer that there is none. `ReportRules.RENDERED_DIRECTLY` records the
     * decision on the catalog side and `CatalogIntegrity` reads it, so the family is a
     * decision rather than a silence.
     *
     * **What is not skipped is layer 5.** `ReportLanguage.insufficientData` chooses the
     * line with `VariantChoice`, renders it with `SlotRenderer` and validates it with
     * `ClarityValidator`, exactly like the footer, the basis line and the two edge states.
     * 11.4 forbids bypassing the validator for an empty state and that still holds. What is
     * skipped is rule selection, which is the layer that decides whether something is worth
     * saying about somebody, and here there is nothing to decide.
     *
     * Computed here rather than passed in, so [compose] and [assemble] cannot disagree
     * about which weeks get it, and so the condition sits beside its complement in step 5.
     */
    private fun patternNote(facts: FactSet, dateKey: String): ReportNote? =
        if (facts.history.weeksOfData < ReportIntegrity.PATTERN_WEEKS) {
            language.insufficientData(facts, dateKey)
        } else {
            null
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
     * section the highest ranked line that holds both of 9.2's rhythm rules.
     *
     * ## Why the observations are grouped rather than left in rank order
     *
     * Rank order is what the engine returns and it is one plausible reading of the design.
     * It has a defect that only shows on the page: three observations can arrive as general,
     * focus, general, and the screen then draws `Your week, honestly` twice with `Focus`
     * between them. A repeated sidehead reads as a bug, and the sideheads are the only
     * structure a page of prose has.
     *
     * ## The two rules, and both of them are preferences
     *
     * 9.2 states a length band rule and a parallel clause cap side by side. Neither is a
     * veto here. The next line is the highest ranked one in this section that holds both,
     * and where no remaining line does, the highest ranked one is taken anyway.
     *
     * 11.4 forbids padding a section to reach a minimum, and dropping a true observation to
     * improve the cadence is the same trade in the other direction. **Rhythm is worth a
     * line, not a paragraph.**
     *
     * ## Where the two rules disagree, the clause cap wins
     *
     * A line can break the band and hold the run, or hold the band and be the third numeric
     * lead, and one of them has to give. The cap wins, for two reasons that point the same
     * way. A band collision is a property of two adjacent lines and the very next line is
     * another chance to fix the rhythm; a numeric run is a property of three, so the chance
     * to repair it is scarcer. And 7.5 says what the failure costs: the three part list is a
     * rhetorical reflex and once a reader sees it they cannot stop seeing it, which is a
     * louder complaint than two sentences of one length.
     *
     * ## Seeded by the headline, and not looked ahead to the pattern
     *
     * The headline is read immediately before the first observation, so it seeds both the
     * band and the run. It is free and it was missing: the band rule used to start each page
     * against nothing, which left the one seam a reader meets first unchecked.
     *
     * The pattern is read immediately after the last observation and is deliberately **not**
     * looked ahead to. It is one line, its position is fixed, and the only thing a lookahead
     * could change is which observation lands last, so it would buy one seam by putting a
     * weight on a trade 9.2 states as two equal rules. It is measured instead:
     * `ReportRhythm` counts the seam and names it, so the price of leaving it is a number
     * rather than a silence.
     */
    private fun arrange(observations: List<Candidate>, headline: Candidate?): List<ReportObservation> {
        val sections = observations.groupBy { ReportSection.of(it.familyKey) }
        val out = mutableListOf<ReportObservation>()
        var previous: LengthBand? = headline?.lengthBand
        var run = if (headline != null && isParallelNumeric(headline)) 1 else 0
        for (section in ReportSection.entries) {
            val remaining = sections[section].orEmpty().toMutableList()
            while (remaining.isNotEmpty()) {
                val holdsBand = { candidate: Candidate -> candidate.lengthBand != previous }
                val holdsRun = { candidate: Candidate -> !isThirdInARun(candidate, run) }
                val pick = remaining.firstOrNull { holdsBand(it) && holdsRun(it) }
                    ?: remaining.firstOrNull(holdsRun)
                    ?: remaining.firstOrNull(holdsBand)
                    ?: remaining.first()
                remaining.remove(pick)
                previous = pick.lengthBand
                run = if (isParallelNumeric(pick)) run + 1 else 0
                out += ReportObservation(section, pick)
            }
        }
        return out
    }

    /** True where placing [candidate] after a run of [run] would be the third in a row. */
    private fun isThirdInARun(candidate: Candidate, run: Int): Boolean =
        isParallelNumeric(candidate) && run >= MAX_PARALLEL_CLAUSES

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

    /**
     * True where [candidate] is a numeric clause for the purposes of 9.2's cap.
     * CLARITY_LOGIC_ENGINE.md 7.5 and `CORPUS_2_REPORT.md` 7.4b.
     *
     * > No more than two parallel numeric clauses may appear consecutively. Where a third
     * > would follow, the composer drops it or re-realizes it at a different length. The
     * > three-part list is a rhetorical reflex and once a reader sees it they cannot stop
     * > seeing it.
     *
     * ## A lead that renders a number at all, which is wider than this used to read
     *
     * The narrow reading was a lead rendering **two or more** numbers, one set against
     * another inside one sentence, and it was chosen because the rule dropped the third
     * lead: nearly every observation in this corpus carries one number, so the wide reading
     * plus a drop would have quietly shortened almost every report and no test would have
     * failed. That argument was about the drop and it went with it. A preference that
     * reorders costs no true sentence, so the reading can be the one 7.5 is actually about,
     * and the numbers say it has to be. Across eleven persona years the narrow reading found
     * **one** run of three and the wide reading finds a hundred and forty seven, which is
     * the difference between a rule and a decoration.
     *
     * **Counted from the slots rather than from the digits in the rendered string.** The
     * simulator reads its own dump and has only the string, so it asks whether a lead
     * contains a digit; an area named `Q3` or an item titled `Rewrite intro v2` is a digit
     * this app did not choose to say. A slot is a number the engine stated, which is the
     * thing the rule is about. For a Report lead the two readings otherwise agree, because
     * 7.2 renders every number on this surface as a digit and spells none of them out.
     */
    private fun isParallelNumeric(candidate: Candidate): Boolean =
        candidate.slots.values.any { it.numericValue != null }

    private fun spoken(result: EngineResult): Candidate? = (result as? EngineResult.Spoke)?.output?.meta

    companion object {

        /** Section 5, second paragraph. Two to four, and four is the ceiling. */
        const val MAX_OBSERVATIONS = 4

        /** 7.4 step 3. */
        const val EDITORIAL_BUDGET = 2

        /** 7.4b. Two consecutive, never three, as a preference. See [arrange]. */
        const val MAX_PARALLEL_CLAUSES = 2

        /** 12.3. Below this, the report is trail data only. */
        const val INTENT_QUALIFIED_ANSWERS = 3
    }
}
