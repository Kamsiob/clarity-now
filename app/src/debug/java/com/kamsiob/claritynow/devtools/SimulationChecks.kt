package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.domain.engine.validate.ValidatorVocabulary

/**
 * The ten automated checks over the dump. CLARITY_LOGIC_ENGINE.md 12.
 *
 * ## Why six of them are deferred, and why that is not a skip
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
     */
    private fun pulseSilenceInBand(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        val readings = mutableListOf<String>()
        for (run in runs) {
            val pulses = run.of(SimulatedSurface.PULSE)
            if (pulses.isEmpty()) continue
            val silent = pulses.count { it.spoken == null }
            val share = SimulationSummary.percent(silent, pulses.size)
            readings += "${run.persona.key} $share"
            if (share !in SILENCE_FLOOR..SILENCE_CEILING) {
                failures += "${run.persona.key} is silent on $share percent of ${pulses.size} opened days"
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
        return CheckOutcome(
            id = "guidanceSilence",
            name = "layer 6 silent on at least $GUIDANCE_SILENCE_FLOOR percent of reports",
            citation = "CLARITY_LOGIC_ENGINE.md 12 and 10.7",
            passed = false,
            measured = "not measurable: layer 6 is phase 9b and composed nothing across $reports reports",
            failures = listOf("layer 6 does not exist yet, so its silence cannot be measured"),
            deferral = GUIDANCE_PHASE,
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
     * A family's stages come from the rules that fired for it anywhere in the run, so a
     * stage that has language and no rule, per `RulesAwaitingFacts`, is not counted against
     * it here. That gap is already a failure of `CatalogIntegrity` if nobody recorded it.
     */
    private fun everyStageOfEveryHotFamilyFires(runs: List<SimulationRun>): CheckOutcome {
        val firings = runs.flatMap { it.invocations }.mapNotNull { it.spoken }
        val byFamily = firings.groupBy { it.familyKey }
        val hot = byFamily.filterValues { it.size >= HOT_FAMILY_FIRINGS }
        val failures = mutableListOf<String>()
        val readings = mutableListOf<String>()
        for ((family, lines) in hot.entries.sortedBy { it.key }) {
            val stages = lines.map { it.stage }.toSortedSet()
            val highest = stages.maxOrNull() ?: continue
            val missing = (1..highest).filterNot { it in stages }
            readings += "$family ${lines.size} firings, stages ${stages.joinToString("/")}"
            if (missing.isNotEmpty()) {
                failures += "$family never reached stage ${missing.joinToString(", ")}"
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
            deferral = CORPUS_PHASE,
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
     */
    private fun noBannedLanguage(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (text in listOfNotNull(spoken.statement, spoken.question)) {
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
     * No sentence names an area with no events in the window it describes. 12, check 1.
     *
     * Enforced from today, and it is the check with the most behind it. Prohibition 3 of 1.1
     * removes archived and tombstoned areas from `AreaFacts` entirely so a rule cannot reach
     * one, validator check 1 re-reads the count for every area a sentence names, and the
     * `Phantom area` failure mode in section 13 is what both exist for. A failure here means
     * two independent guards failed together.
     */
    private fun noPhantomArea(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (areaId in spoken.namedAreaIds) {
                    val events = invocation.areaEventsInWindow[areaId]
                    if (events == null) {
                        failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                            "names $areaId, which is not in the fact set at all"
                    } else if (events <= 0) {
                        failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                            "names $areaId, which had no events in its window"
                    }
                }
            }
        }
        return CheckOutcome(
            id = "phantomArea",
            name = "no sentence names an area with no events in its window",
            citation = "CLARITY_LOGIC_ENGINE.md 12, 1.1 prohibition 3 and check 1 of section 8",
            passed = failures.isEmpty(),
            measured = "${failures.size} phantom areas",
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
     * It passes trivially today, because layer 6 is phase 9b and composes nothing, and it is
     * enforced anyway. A check written the day the mechanism arrives is a check written by
     * somebody who already knows what the mechanism does. This one was written first.
     */
    private fun theNonComplianceTest(runs: List<SimulationRun>): CheckOutcome {
        val failures = mutableListOf<String>()
        val subjects = runs.filter { it.persona.acceptsEveryPlan }
        for (run in subjects) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                for (text in listOfNotNull(spoken.statement, spoken.question)) {
                    PLAN_VOCABULARY.forEach { (pattern, what) ->
                        if (pattern.containsMatchIn(text)) {
                            failures += "${run.persona.key} ${invocation.dateKey} ${spoken.variantKey} " +
                                "references $what: $text"
                        }
                    }
                    if (ClaritySimulator.PLAN_LINE_MARKER in text) {
                        failures += "${run.persona.key} ${invocation.dateKey} rendered a plan line"
                    }
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

    /**
     * What the app referring back to a plan of its own would leave behind.
     *
     * **The bare noun is not the test, and getting that wrong would break real language.**
     * `CORPUS_1_PULSE.md` asks `Was that the plan?` and `Planned, or did it just flow?`, and
     * offers `A commitment / A trial` as a response pair. Those are questions about the
     * person's own day in ordinary English and are exactly the kind of line 6.1 asks for.
     * Banning the word would delete twenty three approved lines and prove nothing.
     *
     * What section 12 forbids is narrower and worse: the app **attributing a past
     * commitment to the person**. Every pattern below is second person plus a prior
     * undertaking, which is the shape a follow-through mechanism produces when it leaks.
     *
     * `You said {priorLabel}` is deliberately absent. That is `selfReportVsData`, the
     * flagship family of 2.6, quoting an answer the person actually gave against what the
     * data shows. It is a self report set beside a fact, not a promise recalled, and check 6
     * already guarantees the quote is exact.
     */
    private val PLAN_VOCABULARY: List<Pair<Regex, String>> = listOf(
        Regex("""\byou(?:r)?\s+plan(?:s|ned|ning)?\b""", RegexOption.IGNORE_CASE) to "a plan of the person's",
        Regex("""\bthe\s+plan\s+you\b""", RegexOption.IGNORE_CASE) to "a plan the person was given",
        Regex("""\byou\s+(?:agreed|committed|promised|intended|meant)\b""", RegexOption.IGNORE_CASE)
            to "an undertaking attributed to the person",
        Regex("""\byou\s+were\s+going\s+to\b""", RegexOption.IGNORE_CASE) to "an unmet intention",
        Regex("""\byou\s+accepted\b""", RegexOption.IGNORE_CASE) to "an acceptance recalled",
        Regex("""\byour\s+commitment\b""", RegexOption.IGNORE_CASE) to "a commitment",
        Regex("""\bcommitted\s+to\b""", RegexOption.IGNORE_CASE) to "a commitment",
        Regex("""\bfollow\s*[- ]?\s*through\b""", RegexOption.IGNORE_CASE) to "follow through",
        Regex("""\bset\s+out\s+to\b""", RegexOption.IGNORE_CASE) to "an unmet intention",
    )

    /** 7.6 step 1. */
    private const val EXCLUSION_DAYS = 90

    /** 12, and `MASTER_BUILD_PROMPT.md` 11.4. */
    private const val SILENCE_FLOOR = 8
    private const val SILENCE_CEILING = 25

    /** 12, and 10.7. */
    private const val GUIDANCE_SILENCE_FLOOR = 15

    /** 12. */
    private const val FAMILY_SHARE_CEILING = 20

    /** 11.1, the hot tier: forty firings a year or more. */
    private const val HOT_FAMILY_FIRINGS = 40

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
