package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.Purpose

/**
 * The readings across every persona at once, rather than one persona at a time.
 * CLARITY_LOGIC_ENGINE.md 12, and the owner's request at the close of the facts phase.
 *
 * ## Why this exists beside [SimulationSummary]
 *
 * [SimulationSummary] prints one persona's counts under that persona's year, which is the
 * right shape for reading a dump and the wrong shape for answering the two questions the
 * facts phase was measured by. Both are about the run as a whole. **How silent is the
 * Pulse, as a share of the days somebody opened the app**, and **how many of the families
 * the corpus authors ever fire at all**. Eleven separate tallies cannot be added up by eye,
 * and the second question cannot be answered from a tally at all: a family that never fired
 * is absent from every persona's counts, which is exactly how "only six of eleven Pulse
 * families ever fired" survived phase 5 as a sentence somebody remembered instead of a
 * number something watched.
 *
 * So this reads the **catalog** as well as the runs. The denominator is the families the
 * corpus declares, the numerator is the families that spoke, and the difference is named.
 *
 * ## Measurement here, verdicts in [SimulationChecks]
 *
 * Every number below is computed once, here, and the checks call these functions rather
 * than recomputing anything. A gate and a report that measure the same property twice will
 * eventually disagree about it, and the one that disagrees quietly is the report, because
 * nothing fails when a printed number is wrong.
 */
object SimulationAggregate {

    /**
     * One persona's Pulse silence, or every persona's together.
     *
     * The share is of **opened days** and not of simulated days, for the reason
     * [SimulationChecks.pulseSilenceInBand] gives: a day nobody opened the app had no Pulse
     * to suppress, and counting it as silence would flatter a number that is already three
     * times its target.
     */
    data class SilenceReading(val label: String, val opened: Int, val silent: Int) {

        val percent: Int get() = SimulationSummary.percent(silent, opened)

        val spoken: Int get() = opened - silent

        /** Where the reading sits against the band section 12 states. */
        val verdict: String
            get() = when {
                opened == 0 -> "no opens"
                percent < SimulationChecks.SILENCE_FLOOR -> "below the band"
                percent > SimulationChecks.SILENCE_CEILING -> "above the band"
                else -> "in band"
            }

        val inBand: Boolean get() = opened > 0 && verdict == "in band"
    }

    /**
     * One family the catalog declares, and how often it spoke across every persona's year.
     *
     * [rules] is carried because zero has two causes and they are not the same defect. A
     * family with no rule cannot fire and is a gap in the catalog, which
     * `RulesAwaitingFacts` and `CatalogIntegrity` are already about. A family with rules
     * that fired nothing is a rule no simulated life satisfies, or a bench whose slots
     * cannot be filled, and neither of those is visible anywhere else.
     */
    data class FamilyReading(
        val purpose: Purpose,
        val family: FamilyKey,
        val firings: Int,
        val rules: Int,
    ) {
        val fired: Boolean get() = firings > 0
    }

    /**
     * A family that fired often enough to be hot, and which of its stages were reached.
     *
     * **Hot is measured rather than authored**, for the reason the stage check gives: 11.1
     * defines the tiers by expected firing frequency, so the run itself holds the only
     * honest answer to which families are hot.
     *
     * [stagesWithARule] is the denominator rather than the stages the corpus authors,
     * because a stage with language and no rule is a different finding with its own home in
     * `RulesAwaitingFacts`. Since the facts phase emptied that register the two sets are the
     * same, and they are still kept apart here so that the day they diverge again, this
     * reading blames the right thing.
     */
    data class StageReading(
        val purpose: Purpose,
        val family: FamilyKey,
        val firings: Int,
        val stagesWithARule: List<Int>,
        val stagesFired: List<Int>,
    ) {
        val missing: List<Int> get() = stagesWithARule.filterNot { it in stagesFired }

        val complete: Boolean get() = missing.isEmpty()
    }

    // ------------------------------------------------------------------ measurements

    /** Every persona's Pulse silence, then every persona's together. */
    fun pulseSilence(runs: List<SimulationRun>): List<SilenceReading> {
        val perPersona = runs.map { run ->
            val pulses = run.of(SimulatedSurface.PULSE)
            SilenceReading(run.persona.key, pulses.size, pulses.count { it.spoken == null })
        }
        return perPersona + SilenceReading(
            ALL_PERSONAS,
            perPersona.sumOf { it.opened },
            perPersona.sumOf { it.silent },
        )
    }

    /**
     * Every family the catalog declares, with the number of times it spoke.
     *
     * Ordered by purpose, then by firings descending, then by key, so two runs print the
     * same list and a reader looking for what is quiet finds it at the bottom of each block.
     */
    fun familyFirings(runs: List<SimulationRun>): List<FamilyReading> {
        val catalog = catalogOf(runs) ?: return emptyList()
        val counts = HashMap<Pair<Purpose, FamilyKey>, Int>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                val key = invocation.surface.purpose to spoken.familyKey
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        return Purpose.entries.flatMap { purpose ->
            catalog.familiesFor(purpose)
                .map { family ->
                    FamilyReading(
                        purpose = purpose,
                        family = family.key,
                        firings = counts[purpose to family.key] ?: 0,
                        rules = catalog.rulesOf(purpose, family.key).size,
                    )
                }
                .sortedWith(compareByDescending<FamilyReading> { it.firings }.thenBy { it.family })
        }
    }

    /** Every hot family, and the stages of it a year of eleven lives reached. */
    fun hotFamilies(runs: List<SimulationRun>): List<StageReading> {
        val catalog = catalogOf(runs) ?: return emptyList()
        val stagesFired = HashMap<Pair<Purpose, FamilyKey>, MutableSet<Int>>()
        for (run in runs) {
            for (invocation in run.invocations) {
                val spoken = invocation.spoken ?: continue
                stagesFired.getOrPut(invocation.surface.purpose to spoken.familyKey) { sortedSetOf() } += spoken.stage
            }
        }
        return familyFirings(runs)
            .filter { it.firings >= SimulationChecks.HOT_FAMILY_FIRINGS }
            .map { reading ->
                val key = reading.purpose to reading.family
                StageReading(
                    purpose = reading.purpose,
                    family = reading.family,
                    firings = reading.firings,
                    stagesWithARule = catalog.rulesOf(reading.purpose, reading.family)
                        .mapNotNull { it.stage }
                        .distinct()
                        .sorted(),
                    stagesFired = stagesFired[key].orEmpty().sorted(),
                )
            }
            .sortedWith(compareBy({ it.purpose.ordinal }, { it.family }))
    }

    /**
     * The catalog every run was measured against.
     *
     * One simulator builds every run in a report and holds one catalog, so the first run's
     * is every run's. Taking it from a run rather than from a parameter is what keeps
     * [SimulationChecks.run] callable with the signature the phase 5 test already uses.
     */
    private fun catalogOf(runs: List<SimulationRun>): ClarityCatalog? = runs.firstOrNull()?.catalog

    // ------------------------------------------------------------------ the report

    /** The three readings the facts phase was asked to produce, as plain text. */
    fun of(runs: List<SimulationRun>): String = buildString {
        appendLine(RULE)
        appendLine("simulator readings across every persona, CLARITY_LOGIC_ENGINE.md 12")
        appendLine(RULE)
        append(silenceBlock(runs))
        append(familyBlock(runs))
        append(stageBlock(runs))
    }

    private fun silenceBlock(runs: List<SimulationRun>): String = buildString {
        appendLine()
        appendLine(
            "-- pulse silence, against the ${SimulationChecks.SILENCE_FLOOR} to " +
                "${SimulationChecks.SILENCE_CEILING} percent band ------------",
        )
        appendLine(
            "persona".padEnd(NAME_WIDTH) + "opened".padStart(NUMBER_WIDTH) +
                "spoken".padStart(NUMBER_WIDTH) + "silent".padStart(NUMBER_WIDTH) +
                "silent %".padStart(NUMBER_WIDTH) + "  verdict",
        )
        for (reading in pulseSilence(runs)) {
            appendLine(
                reading.label.padEnd(NAME_WIDTH) + reading.opened.toString().padStart(NUMBER_WIDTH) +
                    reading.spoken.toString().padStart(NUMBER_WIDTH) +
                    reading.silent.toString().padStart(NUMBER_WIDTH) +
                    reading.percent.toString().padStart(NUMBER_WIDTH) + "  ${reading.verdict}",
            )
        }
        val reasons = runs.flatMap { it.of(SimulatedSurface.PULSE) }.mapNotNull { it.silence?.name }
        appendLine("why the silent days were silent: " + SimulationSummary.tally(reasons))
    }

    private fun familyBlock(runs: List<SimulationRun>): String = buildString {
        val readings = familyFirings(runs)
        appendLine()
        appendLine("-- families that ever fired, out of the families the corpus declares --")
        for (purpose in Purpose.entries) {
            val ofPurpose = readings.filter { it.purpose == purpose }
            if (ofPurpose.isEmpty()) continue
            val fired = ofPurpose.filter { it.fired }
            appendLine()
            appendLine("${purpose.name}: ${fired.size} of ${ofPurpose.size} families fired")
            appendLine("  fired:  " + fired.joinToString(", ") { "${it.family} ${it.firings}" }.ifEmpty { "none" })
            val quietWithRules = ofPurpose.filter { !it.fired && it.rules > 0 }
            val quietWithout = ofPurpose.filter { !it.fired && it.rules == 0 }
            appendLine(
                "  quiet, and has a rule:  " +
                    quietWithRules.joinToString(", ") { "${it.family} (${it.rules})" }.ifEmpty { "none" },
            )
            appendLine(
                "  quiet, and has no rule: " +
                    quietWithout.joinToString(", ") { it.family }.ifEmpty { "none" },
            )
        }
    }

    private fun stageBlock(runs: List<SimulationRun>): String = buildString {
        val readings = hotFamilies(runs)
        appendLine()
        appendLine(
            "-- every stage of every hot family, hot being " +
                "${SimulationChecks.HOT_FAMILY_FIRINGS} firings or more ------",
        )
        if (readings.isEmpty()) {
            appendLine("no family reached ${SimulationChecks.HOT_FAMILY_FIRINGS} firings, so nothing counts as hot yet")
            return@buildString
        }
        val short = readings.filterNot { it.complete }
        appendLine(
            "${readings.size} hot families, ${readings.size - short.size} with every staged rule fired, " +
                "${short.size} short",
        )
        for (reading in readings) {
            appendLine(
                "  " + "${reading.purpose.name}.${reading.family}".padEnd(FAMILY_WIDTH) +
                    reading.firings.toString().padStart(NUMBER_WIDTH) + " firings, " +
                    "rules at stages ${reading.stagesWithARule.joinToString("/").ifEmpty { "none" }}, " +
                    "fired ${reading.stagesFired.joinToString("/").ifEmpty { "none" }}" +
                    if (reading.complete) "" else ", never reached ${reading.missing.joinToString(", ")}",
            )
        }
    }

    /** What every persona's Pulses together are called in the silence table. */
    const val ALL_PERSONAS = "all personas"

    private const val RULE = "================================================================"
    private const val NAME_WIDTH = 22
    private const val FAMILY_WIDTH = 44
    private const val NUMBER_WIDTH = 9
}
