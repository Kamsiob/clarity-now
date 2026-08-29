package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.domain.engine.catalog.AbsenceSubjectRules
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.catalog.ReportRules
import com.kamsiob.claritynow.domain.engine.validate.ValidatorVocabulary
import com.kamsiob.claritynow.domain.guidance.PlanVocabulary

/**
 * The twelve automated checks over the dump. CLARITY_LOGIC_ENGINE.md 12.
 *
 * Ten of them are the ten section 12 names. The other two are the coverage readings the
 * facts phase added, `familyCoverage` and the rewritten `stageCoverage`, and they are here
 * rather than in a report because the owner's condition on phase 9 is stated as two
 * numbers: no corpus line is written until Pulse silence is inside the band and every
 * family fires. A condition on a phase that nothing measures on every run is a condition
 * somebody has to remember, and phase 5 already proved that one does not survive a phase
 * boundary.
 *
 * ## Why eight of them are deferred, and why that is not a skip
 *
 * Section 12 lists these checks in the phase that builds the simulator, and issue #3 says
 * plainly that the statistical ones cannot pass here: the corpus is not grown until phase 9,
 * issue #7, and a bench of four lines cannot avoid repeating inside ninety days no matter
 * how good the selector is. So they are built now, run now, measured now, and **reported
 * with the number they actually produced** rather than skipped, because those numbers are
 * what phase 9 steers by.
 *
 * A deferred check carries a [Deferral] naming the date it was deferred and the issue that
 * lifts it. Nothing here is a quiet `assumeTrue` or a commented out assertion: every check
 * runs on every simulation, and the report prints the deferred failures as loudly as the
 * enforced ones. What deferral changes is only whether the build goes red.
 *
 * ## What is enforced from today
 *
 * The four checks whose failure would mean something already built is wrong rather than
 * something not yet written is small:
 *
 * - **Vocabulary.** Validator check 8 already vetoes a banned word, a dash or a character
 *   above ASCII, so a dump containing one means layer 5 was bypassed
 * - **Phantom areas.** Validator check 1 already vetoes naming an area with no events in
 *   the window, and prohibition 3 of 1.1 removes archived and tombstoned areas from
 *   `AreaFacts` entirely, so a dump containing one means both went wrong at once
 * - **Slot syntax.** A visible marker on screen is check 7 and is never acceptable
 * - **Non-compliance.** The plan-accepting, plan-ignoring persona's year, which section 12
 *   says must contain no sentence referencing a plan, a commitment, an intention or a
 *   failure to act. It passes trivially today because layer 6 does not exist, and it is
 *   enforced from today anyway so that the day layer 6 arrives, this is already watching
 */
object SimulationChecks {

    /** Runs every check over every persona's year. */
    fun run(runs: List<SimulationRun>): CheckReport = CheckReport(
        checks = listOf(
            noRepeatedVariantWithinNinetyDays(runs),
            noBannedLanguage(runs),
            noPhantomArea(runs),
            noVisibleSlotSyntax(runs),
            pulseSilenceInBand(runs),
            layerSixSilence(runs),
            everyFamilyFires(runs),
            noFamilyOverOneFifth(runs),
            everyStageOfEveryHotFamilyFires(runs),
            noTwoConsecutiveLeadsShareABand(runs),
            noThreeParallelNumericClauses(runs),
            theNonComplianceTest(runs),
        ),
    )

    // -------------------------------------------------------------------- deferred

    /**
     * No variant key appears twice inside ninety simulated days, per 7.6 step 1.
     *
     * Measured per persona, because two people never share a firing history. The number
     * reported is the worst gap found, which is the number phase 9 grows a bench to fix: a
     * bench of `n` lines firing every `d` days can hold out for `n * d` days and no longer.
     */
    private fun noRepeatedVariantWithinNinetyDays(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        var worstGap = Int.MAX_VALUE
        for (run in runs) {
            val lastUsed = mutableMapOf<String, Int>()
            for (invocation in run.invocations) {
                val variant = invocation.spoken?.variantKey ?: continue
                val previous = lastUsed.put(variant, invocation.day) ?: continue
                val gap = invocation.day - previous
                if (gap >= EXCLUSION_DAYS) continue
                if (gap < worstGap) worstGap = gap
                failures += "${run.persona.key} ${invocation.dateKey} $variant repeated after $gap days"
            }
        }
        return CheckOutcome(
            id = "variantRepetition",
            name = "no variant repeats inside ninety days",
            citation = "CLARITY_LOGIC_ENGINE.md 12 and 7.6 step 1",
            passed = failures.isEmpty(),
            measured = if (failures.isEmpty()) {
                "no repeat inside $EXCLUSION_DAYS days"
            } else {
                "${failures.size} repeats, the tightest after $worstGap days"
            },
            failures = failures,
            deferral = CORPUS_PHASE,
        )
    }

    /**
     * Pulse silence between 8 and 25 percent of days. 12, and `MASTER_BUILD_PROMPT.md` 11.4.
     *
     * Measured over the days the app was opened, because a day nobody opened the app had no
     * Pulse to suppress and counting it as silence would flatter the number.
     *
     * **Every persona, and then every persona together**, because the band is a claim about
     * how often the app speaks to a person and eleven separate percentages cannot be
     * averaged by eye: the personas open the app between 21 and 365 times, so the unweighted
     * mean of the eleven is not the share of days that were silent. The last row is that
     * share, it is measured against the same band, and it is the number the phase 5 baseline
     * recorded as 76 percent.
     */
    private fun pulseSilenceInBand(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        val readings = mutableListOf<String>()
        for (reading in SimulationAggregate.pulseSilence(runs)) {
            if (reading.opened == 0) continue
            readings += "${reading.label} ${reading.percent}"
            if (!reading.inBand) {
                failures += "${reading.label} is silent on ${reading.percent} percent of " +
                    "${reading.opened} opened days, which is ${reading.verdict}"
            }
        }
        return CheckOutcome(
            id = "pulseSilence",
            name = "pulse silence between $SILENCE_FLOOR and $SILENCE_CEILING percent of days",
            citation = "CLARITY_LOGIC_ENGINE.md 12, MASTER_BUILD_PROMPT.md 11.4",
            passed = failures.isEmpty(),
            measured = readings.joinToString(", "),
            failures = failures,
            deferral = CORPUS_PHASE,
        )
    }

    /**
     * Layer 6 silence on at least 15 percent of reports.
     *
     * Unmeasurable today and reported as such rather than passed. `GuidanceComposer` is
     * phase 9b and CLARITY_LOGIC_ENGINE.md 10 calls it the last thing built, so there is no
     * closing line to be silent about. A check that reported this as passing would be a
     * check that goes green because the thing it watches does not exist.
     */
    private fun layerSixSilence(runs: List<SimulationRun>): CheckOutcome {
        val reports = runs.sumOf { it.of(SimulatedSurface.REPORT_HEADLINE).size }
        val closings = runs.flatMap { it.of(SimulatedSurface.REPORT_CLOSING) }
        val plans = closings.count { it.spoken?.ruleKey == ClaritySimulator.GUIDANCE_PLAN_RULE }
        // Nothing, or a non plan closing. A report with no closing invocation recorded got
        // `GuidanceResult.Nothing`, and a closing whose rule key is not the plan one is a
        // complete authored line from 4.6 with no pill under it. Both are the silence 10.7
        // is about; only a plan is not.
        val silent = reports - plans
        val share = SimulationSummary.percent(silent, reports)
        return CheckOutcome(
            id = "guidanceSilence",
            name = "layer 6 silent on at least $GUIDANCE_SILENCE_FLOOR percent of reports",
            citation = "CLARITY_LOGIC_ENGINE.md 12 and 10.7",
            passed = reports > 0 && share >= GUIDANCE_SILENCE_FLOOR,
            measured = "$silent of $reports reports carried no plan, $share percent. " +
                "${closings.size - plans} were a non plan closing and " +
                "${reports - closings.size} were nothing at all",
            failures = if (reports > 0 && share >= GUIDANCE_SILENCE_FLOOR) {
                emptyList()
            } else {
                listOf("layer 6 offered a plan on ${SimulationSummary.percent(plans, reports)} percent of reports")
            },
            deferral = null,
        )
    }

    /** No family accounts for more than a fifth of a year's Pulses. 12. */
    private fun noFamilyOverOneFifth(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        val readings = mutableListOf<String>()
        for (run in runs) {
            val spoken = run.of(SimulatedSurface.PULSE).mapNotNull { it.spoken?.familyKey }
            if (spoken.isEmpty()) continue
            val worst = spoken.groupingBy { it }.eachCount().maxByOrNull { it.value } ?: continue
            val share = SimulationSummary.percent(worst.value, spoken.size)
            readings += "${run.persona.key} ${worst.key} $share"
            if (share > FAMILY_SHARE_CEILING) {
                failures += "${run.persona.key} takes $share percent of its Pulses from ${worst.key}"
            }
        }
        return CheckOutcome(
            id = "familyShare",
            name = "no family over $FAMILY_SHARE_CEILING percent of a year's Pulses",
            citation = "CLARITY_LOGIC_ENGINE.md 12",
            passed = failures.isEmpty(),
            measured = readings.joinToString(", "),
            failures = failures,
            deferral = CORPUS_PHASE,
        )
    }

    /**
     * Every family the corpus declares speaks at least once across the run.
     *
     * **The check phase 5 did not have, and the one number the facts phase was measured
     * by.** Phase 5 read "of the eleven Pulse families in 6.1, six ever fired" out of a
     * dump by hand, wrote it in `DECISIONS.md`, and nothing watched it afterward. A family
     * that never fires leaves no trace in a year of output, so every other check here is
     * blind to it: the silence figure counts the days it was quiet on without ever
     * attributing them, and the family share check divides a year's Pulses among the
     * families that did speak.
     *
     * The denominator is the catalog. Every family, at every purpose, whether or not it has
     * a rule, so that the two ways of never firing are separated in the reading rather than
     * summed: a family with no rule at all is a gap `RulesAwaitingFacts` is supposed to be
     * holding, and a family with rules that fired nothing is a rule no simulated life
     * satisfies or a bench whose slots cannot be filled.
     *
     * **A quiet family is not automatically a defect**, which is why this is a reading with
     * a threshold rather than an assertion. `CORPUS_1_PULSE.md` authors families for lives
     * none of the eleven personas lead. What the number is for is the owner's condition on
     * phase 9: no corpus line is written until silence is in band and every family fires, so
     * the thing that gates the corpus phase has to be a number the build prints on every
     * run.
     */
    /**
     * **One quiet family is a decision rather than a silence, and it is named as one.**
     * `insufficientData` is the pattern section's empty state, the Report renders it through
     * `ReportLanguage` and the simulator never calls the composer, so it can only ever read
     * as a family that never fired. The denominator stays at every family the corpus
     * declares, deliberately: dropping it would make this run's number incomparable with the
     * three before it, which is the mistake the stage check already made once. So the count
     * is unchanged and the line says which of the quiet families left the engine on purpose.
     */
    private fun everyFamilyFires(runs: List<SimulationRun>): CheckOutcome {
        val readings = SimulationAggregate.familyFirings(runs)
        val failures = mutableListOf<String>()
        val measured = mutableListOf<String>()
        for (purpose in Purpose.entries) {
            val ofPurpose = readings.filter { it.purpose == purpose }
            if (ofPurpose.isEmpty()) continue
            val fired = ofPurpose.count { it.fired }
            measured += "${purpose.name} $fired of ${ofPurpose.size}"
            for (quiet in ofPurpose.filterNot { it.fired }) {
                failures += when {
                    quiet.family in ReportRules.RENDERED_DIRECTLY ->
                        "${purpose.name} ${quiet.family} never fired here, and never will: the " +
                            "Report renders it itself, so it has no rule for the engine to select. " +
                            "ReportRules.RENDERED_DIRECTLY carries the decision"

                    quiet.rules == 0 -> "${purpose.name} ${quiet.family} never fired, and has no rule at all"
                    quiet.rules == 1 -> "${purpose.name} ${quiet.family} never fired, and has 1 rule"
                    else -> "${purpose.name} ${quiet.family} never fired, and has ${quiet.rules} rules"
                }
            }
        }
        return CheckOutcome(
            id = "familyCoverage",
            name = "every family the corpus declares fires at least once",
            citation = "CLARITY_LOGIC_ENGINE.md 12 and 11.1",
            passed = failures.isEmpty() && readings.isNotEmpty(),
            measured = if (readings.isEmpty()) {
                "no catalog reached the checks, so nothing could be counted"
            } else {
                measured.joinToString(", ")
            },
            failures = failures,
            deferral = COVERAGE_WORK,
        )
    }

    /**
     * Every stage of every hot family fires at least once.
     *
     * **Hot is measured rather than authored.** 11.1 defines the tiers by expected firing
     * frequency, roughly fifteen families at forty or more firings a year, so the simulator
     * already holds the only honest answer to which families are hot: the ones that fired
     * forty or more times across the run. A hand written list would have been the obvious
     * way to do this and the wrong one. It would be a second place where a judgment about
     * frequency lives, it would drift the first time a rule changed, and it would let a
     * family be called hot without ever having fired.
     *
     * **The denominator is the stages that have a rule, and it used to be the stages that
     * fired.** Reading the ladder off the firings meant the highest stage anybody reached
     * was the highest stage the check knew about, so a family whose stage 3 never fired
     * passed by never being asked: the gap and the ceiling were the same number. That was
     * the honest reading while `RulesAwaitingFacts` held twelve entries, because a stage
     * with language and no rule cannot fire and is a different finding with its own home.
     * The facts phase emptied that register, so every authored stage has a rule and the
     * question the check was always for, whether a ladder is reachable end to end, is now
     * askable. It is asked against the rules rather than against the corpus so that if a
     * stage ever loses its rule again, this check stays quiet about it and
     * `CatalogIntegrity` is the one that fails.
     */
    private fun everyStageOfEveryHotFamilyFires(runs: List<SimulationRun>): CheckOutcome {
        val hot = SimulationAggregate.hotFamilies(runs)
        val failures = mutableListOf<String>()
        val readings = mutableListOf<String>()
        for (reading in hot) {
            readings += "${reading.family} ${reading.firings} firings, " +
                "rules at ${reading.stagesWithARule.joinToString("/")}, " +
                "fired ${reading.stagesFired.joinToString("/")}"
            if (!reading.complete) {
                failures += "${reading.family} never reached stage ${reading.missing.joinToString(", ")}"
            }
        }
        return CheckOutcome(
            id = "stageCoverage",
            name = "every stage of every hot family fires at least once",
            citation = "CLARITY_LOGIC_ENGINE.md 12 and 11.1, hot being $HOT_FAMILY_FIRINGS firings or more",
            passed = failures.isEmpty() && hot.isNotEmpty(),
            measured = if (hot.isEmpty()) {
                "no family reached $HOT_FAMILY_FIRINGS firings, so nothing counts as hot yet"
            } else {
                readings.joinToString("; ")
            },
            failures = failures,
            deferral = COVERAGE_WORK,
        )
    }

    /**
     * No two consecutive Report leads share a length band. 9.2 and 7.5.
     *
     * The realizer is handed the previous band and prefers a different one, so a failure
     * here is a bench with only one band in it rather than a composer that forgot. That is
     * a corpus size problem and is deferred for the same reason the others are.
     */
    private fun noTwoConsecutiveLeadsShareABand(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        var reports = 0
        for (run in runs) {
            for (report in reportsOf(run)) {
                reports++
                report.zipWithNext().forEach { (earlier, later) ->
                    if (earlier.lengthBand == later.lengthBand) {
                        failures += "${run.persona.key} ${earlier.variantKey} and ${later.variantKey} " +
                            "are both ${earlier.lengthBand}"
                    }
                }
            }
        }
        return CheckOutcome(
            id = "lengthBands",
            name = "no two consecutive report leads share a length band",
            citation = "CLARITY_LOGIC_ENGINE.md 9.2 and 7.5",
            passed = failures.isEmpty(),
            measured = "${failures.size} collisions across $reports reports",
            failures = failures,
            deferral = CORPUS_PHASE,
        )
    }

    /**
     * No report carries three consecutive parallel numeric clauses. 9.2.
     *
     * A numeric clause here is a rendered lead containing a digit, which is what a reader
     * sees as numeric whether the number came from a `Count`, a `Percent` or a `Days`. Three
     * in a row is the rhythm 9.2 caps, and the cap is on consecutive sections rather than on
     * the report as a whole.
     */
    private fun noThreeParallelNumericClauses(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        for (run in runs) {
            for (report in reportsOf(run)) {
                var streak = 0
                for (lead in report) {
                    streak = if (lead.statement.any { it.isDigit() }) streak + 1 else 0
                    if (streak >= PARALLEL_CLAUSE_CEILING + 1) {
                        failures += "${run.persona.key} report has $streak numeric leads in a row, " +
                            "ending at ${lead.variantKey}"
                    }
                }
            }
        }
        return CheckOutcome(
            id = "parallelClauses",
            name = "no more than $PARALLEL_CLAUSE_CEILING consecutive parallel numeric clauses",
            citation = "CLARITY_LOGIC_ENGINE.md 9.2",
            passed = failures.isEmpty(),
            measured = "${failures.size} runs of three or more",
            failures = failures,
            deferral = CORPUS_PHASE,
        )
    }

    // -------------------------------------------------------------------- enforced

    /**
     * No banned word, no em dash, no en dash, nothing above ASCII, no spelling from
     * elsewhere. 12, 11.3 and validator check 8.
     *
     * Enforced from today. Layer 5 already vetoes every one of these on the way out, so a
     * dump containing one is not a corpus problem, it is evidence that a sentence reached a
     * surface without passing through the validator.
     *
     * **Read over the masked text, because that is the text the validator reads.** Checks 7,
     * 8 and 10 mask the person's own area names, item titles and tapped labels before
     * scanning, per the note on `ClarityValidator`: the words the app chose are the words
     * the app is answerable for. Reading the rendered form here made this check stricter
     * than the layer it claims to be a backstop for, so it reported a bypass that had not
     * happened. It surfaced the day the register chooser landed and `persistence.s1.55` and
     * `persistence.s2.60` became reachable: both read `nothing behind {itemTitle}`, and a
     * persona whose item is called `Plan the trip route` renders `behind Plan`, which is
     * 11.3's own pattern for the evaluative sense of `behind` matching a person's own noun.
     */
    private fun noBannedLanguage(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (text in listOfNotNull(spoken.maskedStatement, spoken.maskedQuestion)) {
                    failures += offenses(text).map { "${run.persona.key} ${spoken.variantKey}: $it" }
                }
            }
        }
        return CheckOutcome(
            id = "vocabulary",
            name = "no banned word, dash, emoji, non ASCII character or spelling from elsewhere",
            citation = "CLARITY_LOGIC_ENGINE.md 12, 11.3 and check 8 of section 8",
            passed = failures.isEmpty(),
            measured = "${failures.size} offenses",
            failures = failures,
            deferral = null,
        )
    }

    private fun offenses(text: String): List<String> = buildList<String> {
        if (ValidatorVocabulary.EM_DASH in text) add("em dash")
        if (ValidatorVocabulary.EN_DASH in text) add("en dash")
        text.forEach { character ->
            if (character.code > LAST_ASCII) add("character above ASCII, U+%04X".format(character.code))
        }
        ValidatorVocabulary.BANNED_WORDS.forEach { (pattern, what) ->
            if (pattern.containsMatchIn(text)) add("banned word, $what")
        }
        ValidatorVocabulary.BANNED_PHRASES.forEach { (pattern, what) ->
            if (pattern.containsMatchIn(text)) add("banned phrase, $what")
        }
        ValidatorVocabulary.BLAME_CONSTRUCTIONS.forEach { (pattern, what) ->
            if (pattern.containsMatchIn(text)) add("blame construction, $what")
        }
        ValidatorVocabulary.OTHER_SPELLING_FORMS.forEach { (pattern, preferred) ->
            if (pattern.containsMatchIn(text)) add("spelling, use $preferred")
        }
    }

    /**
     * No sentence names an area with no events in the window it describes, unless the
     * absence is what the sentence is about. 12, check 1.
     *
     * Enforced from today, and it is the check with the most behind it. Prohibition 3 of 1.1
     * removes archived and tombstoned areas from `AreaFacts` entirely so a rule cannot reach
     * one, validator check 1 re-reads the count for every area a sentence names, and the
     * `Phantom area` failure mode in section 13 is what both exist for. A failure here means
     * two independent guards failed together.
     *
     * **`AbsenceSubjectRules` is the one exception, and it is the same narrowing check 1
     * makes.** `neglectedArea`, `areaGoneQuiet` and `areaRevival` say that an area has been
     * still, so naming an area with no events in the week is the whole of what they do.
     * Against the check as it was first written every one of their candidates was vetoed,
     * 107 times across this year, and the ruling was that the check was right and the
     * writing was wrong. The deeper conditions, a real lifetime, not new, and a measured
     * silence rather than the never sentinel, are `AbsenceSubject`'s, and every sentence
     * counted here has already been through it. What this can still see, and what it
     * therefore checks, is that the exception belongs to those rules and to nothing else.
     *
     * The count of allowed absences is measured rather than dropped. A year with none of
     * them means the three families are silent again, which is the state this change was
     * made to end, and a check that only reported failures would not say so.
     */
    private fun noPhantomArea(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        var absences = 0
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (areaId in spoken.namedAreaIds) {
                    val events = invocation.areaEventsInWindow[areaId]
                    if (events == null) {
                        failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                            "names $areaId, which is not in the fact set at all"
                    } else if (events <= 0) {
                        if (spoken.ruleKey in AbsenceSubjectRules) {
                            absences++
                        } else {
                            failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                                "names $areaId, which had no events in its window"
                        }
                    }
                }
            }
        }
        return CheckOutcome(
            id = "phantomArea",
            name = "no sentence names an area with no events in its window",
            citation = "CLARITY_LOGIC_ENGINE.md 12, 1.1 prohibition 3 and check 1 of section 8",
            passed = failures.isEmpty(),
            measured = "${failures.size} phantom areas, $absences absences named on purpose",
            failures = failures,
            deferral = null,
        )
    }

    /** Nothing that looks like slot syntax survived into a rendered line. Check 7. */
    private fun noVisibleSlotSyntax(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (text in listOfNotNull(spoken.statement, spoken.question)) {
                    if (MARKER in text) {
                        failures += "${run.persona.key} ${spoken.variantKey}: $text"
                    }
                }
            }
        }
        return CheckOutcome(
            id = "slotSyntax",
            name = "no visible slot syntax in a rendered line",
            citation = "CLARITY_LOGIC_ENGINE.md check 7 of section 8",
            passed = failures.isEmpty(),
            measured = "${failures.size} lines with a visible marker",
            failures = failures,
            deferral = null,
        )
    }

    /**
     * The non-compliance test. CLARITY_LOGIC_ENGINE.md 12, last bullet.
     *
     * The plan-accepting, plan-ignoring persona produces a year in which **no sentence
     * references a plan, a commitment, an intention, or a failure to act**. Section 12 is
     * unusually strict about the consequence: if a reader of that dump could tell plans were
     * accepted, the follow-through implementation has failed and must be **removed rather
     * than tuned**.
     *
     * **It stopped passing trivially in phase 9b, which is the point at which it started
     * being a test.** Until then layer 6 composed nothing, the persona accepted a plan the
     * simulator had assembled by hand, and no closing reached the dump at all. The persona
     * now accepts a real plan from `GuidanceComposer`, `PLAN_OFFERED` and `PLAN_ACCEPTED`
     * are written, `PlanHistory` reads them back, and the follow through boost runs on every
     * subsequent report. The year it produces is a real, repeated, visible non compliance,
     * which is what section 12 asks this to be read against.
     *
     * The vocabulary is `PlanVocabulary.FORBIDDEN`, in main source beside the mechanism, so
     * this check and `GuidanceNonComplianceTest` read one list rather than two.
     */
    private fun theNonComplianceTest(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        val subjects = runs.filter { it.persona.acceptsEveryPlan }
        for (run in subjects) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (text in listOfNotNull(spoken.statement, spoken.question)) {
                    val referenced = PlanVocabulary.referenceIn(text) ?: continue
                    failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                        "references $referenced: $text"
                }
            }
        }
        return CheckOutcome(
            id = "nonCompliance",
            name = "the plan-accepting, plan-ignoring persona is never told about a plan",
            citation = "CLARITY_LOGIC_ENGINE.md 12 last bullet, and 10.6",
            passed = failures.isEmpty() && subjects.isNotEmpty(),
            measured = if (subjects.isEmpty()) {
                "the persona is missing from the run, which is itself a failure"
            } else {
                "${subjects.sumOf { it.invocations.size }} invocations read, ${failures.size} references"
            },
            failures = failures,
            deferral = null,
        )
    }

    // -------------------------------------------------------------------- shared

    /** Every report as an ordered list of the leads that appeared in it. */
    private fun reportsOf(run: SimulationRun): List<List<SpokenLine>> =
        run.invocations
            .filter { it.surface.isReport && it.spoken != null }
            .groupBy { it.day }
            .toSortedMap()
            .values
            .map { sections -> sections.sortedBy { it.ordinal ?: 0 }.mapNotNull { it.spoken } }

    private val SimulatedSurface.isReport: Boolean
        get() = this == SimulatedSurface.REPORT_HEADLINE ||
            this == SimulatedSurface.REPORT_OBSERVATION ||
            this == SimulatedSurface.REPORT_PATTERN

    /** 7.6 step 1. */
    private const val EXCLUSION_DAYS = 90

    /**
     * 12, and `MASTER_BUILD_PROMPT.md` 11.4.
     *
     * Public because [SimulationAggregate] prints the same band over the same numbers, and
     * a report that stated a band the gate did not enforce would be worse than no report.
     */
    const val SILENCE_FLOOR = 8

    /** The top of the band. See [SILENCE_FLOOR]. */
    const val SILENCE_CEILING = 25

    /** 12, and 10.7. */
    private const val GUIDANCE_SILENCE_FLOOR = 15

    /** 12. */
    private const val FAMILY_SHARE_CEILING = 20

    /**
     * 11.1, the hot tier: forty firings a year or more.
     *
     * Public for the same reason the band is: [SimulationAggregate] measures which families
     * are hot and this is the line it measures against.
     */
    const val HOT_FAMILY_FIRINGS = 40

    /** 9.2. */
    private const val PARALLEL_CLAUSE_CEILING = 2

    private const val LAST_ASCII = 127

    /** What an unfilled template marker looks like. `Template` writes them as braces. */
    private const val MARKER = '{'

    /**
     * The corpus is grown in phase 9, issue #7. Every check whose failure is a bench too
     * small to satisfy it waits for that.
     */
    private val CORPUS_PHASE = Deferral(
        since = "2026-08-27",
        until = "issue #7, phase 9, the corpus",
        why = "the bench is the size phase 5 found it. 11.1 grows the hot families from " +
            "four to eight lines per stage to sixty to a hundred, and every check below " +
            "measures a property that a bench that small cannot have",
    )

    /**
     * The two coverage readings, deferred because what they measure is not written yet.
     *
     * **Not the same deferral as the corpus one, and the difference is the whole finding of
     * the facts phase.** A bench too small to hold ninety days of variants is fixed by
     * authoring lines. A family that never fires is not: either no simulated life satisfies
     * its rule, or its bench names something no binding can fill, and neither of those gets
     * better by writing more sentences. The owner has gated phase 9 on exactly these two
     * numbers, so they are measured on every run and reported whether they pass or fail,
     * and the day both pass is the day the corpus phase may begin.
     */
    private val COVERAGE_WORK = Deferral(
        since = "2026-08-27",
        until = "issue #7, phase 9, which the owner has gated on these two readings",
        why = "a family or a stage that never fires is a rule no simulated life satisfies, " +
            "or a bench whose slots have no binding. Both are addressed in phase 9, and " +
            "neither is fixed by the authoring that phase is mostly made of",
    )

    /** Layer 6 is phase 9b and is the last thing built, per section 10. */
    private val GUIDANCE_PHASE = Deferral(
        since = "2026-08-27",
        until = "phase 9b, layer 6",
        why = "GuidanceComposer does not exist, so there is no closing line to be silent about",
    )
}

/**
 * Why a check does not fail the build yet, and what lifts the deferral.
 *
 * A date and an issue, both required. A deferral with neither is a skip with a comment on
 * it, and the difference between those two is whether anybody ever comes back.
 */
data class Deferral(val since: String, val until: String, val why: String)

/** One check, its verdict, and the number it measured whether it passed or not. */
data class CheckOutcome(
    /** Stable, and what a test looks a check up by. Renaming one loses that test's target. */
    val id: String,
    val name: String,
    val citation: String,
    val passed: Boolean,
    /** The reading, printed on a pass as well as a failure. This is what phase 9 aims at. */
    val measured: String,
    val failures: List<String>,
    /** Null when the check is enforced from today. */
    val deferral: Deferral?,
) {
    val enforced: Boolean get() = deferral == null

    /** True when this check would fail the build. */
    val fatal: Boolean get() = enforced && !passed
}

/** Every check over one set of simulated years. */
data class CheckReport(val checks: List<CheckOutcome>) {

    val fatal: List<CheckOutcome> get() = checks.filter { it.fatal }

    /** The whole report as plain text, deferred checks included and clearly marked. */
    override fun toString(): String = buildString {
        appendLine("================================================================")
        appendLine("simulator checks, CLARITY_LOGIC_ENGINE.md 12")
        appendLine("================================================================")
        for (check in checks) {
            val verdict = when {
                check.passed && check.enforced -> "PASS     "
                check.passed -> "PASS     (deferred, and passing anyway)"
                check.enforced -> "FAIL     "
                else -> "FAIL     (deferred, does not fail the build)"
            }
            appendLine()
            appendLine("$verdict ${check.name}")
            appendLine("          ${check.citation}")
            appendLine("          measured: ${check.measured}")
            check.deferral?.let {
                appendLine("          deferred ${it.since} until ${it.until}")
                appendLine("          because ${it.why}")
            }
            check.failures.take(EXAMPLES).forEach { appendLine("          - $it") }
            if (check.failures.size > EXAMPLES) {
                appendLine("          - and ${check.failures.size - EXAMPLES} more")
            }
        }
    }

    private companion object {
        /** Enough failures to see the shape of the problem, not enough to bury the report. */
        const val EXAMPLES = 8
    }
}
