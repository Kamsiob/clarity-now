package com.kamsiob.claritynow.devtools

/**
 * The plain text dump, in the format CLARITY_LOGIC_ENGINE.md 12 shows.
 *
 * ```
 * 2026-03-14  [pulse]  persistence / stage 2 / reflective / v.persist.s2.r.017
 *   fired: active item age 9 days (>= 6), area has 4+ events, no swap in window
 *   facts: activeItemAgeDays=9, areaName=Work, itemTitle=Rewrite the proposal intro
 *   > Still Rewrite the proposal intro. Nine days now.
 *   > Deep work, or stuck?
 *
 * 2026-03-15  [pulse]  SILENT (DELIBERATE_SILENCE)
 * ```
 *
 * The header line, the `fired:` line, the `facts:` line and the `>` lines are that format
 * exactly. Three lines are added to it and none replaces anything:
 *
 * - `rule:` names the rule key. Section 12's header carries the family, the stage, the
 *   register and the variant, which is the language side of a firing. The rule key is the
 *   logic side, and phase 9 judges lines against the rule that chose them
 * - `vetoed:` names candidates layer 5 rejected before this one, with the check that
 *   rejected each. A run where these are common has a rule qualifying on facts its
 *   sentences cannot describe, and without the line that is indistinguishable from a
 *   quiet week
 * - the `facts:` entries carry the `FactRef` in brackets after a number, so a reader can
 *   follow a rendered number back to the query it came from without opening the code
 *
 * **Plain text and nothing else.** No color, no table, no markup. This is read in a pager
 * beside a corpus file, and a format that needed a tool to read would not be read.
 */
object SimulationDump {

    /** Every persona's year, one after another. */
    fun of(runs: List<SimulationRun>): String = runs.joinToString("\n") { of(it) }

    /** One persona's year: a header, every invocation in order, then the counts. */
    fun of(run: SimulationRun): String = buildString {
        appendLine(RULE)
        appendLine("persona: ${run.persona.key}, ${run.persona.title}")
        appendLine("why:     ${run.persona.why}")
        appendLine(
            "span:    ${run.days} simulated days, ${run.openDays} opens, " +
                "${run.eventCount} events, ${run.invocations.size} engine invocations",
        )
        appendLine(RULE)
        appendLine()
        var lastDay = -1
        for (invocation in run.invocations) {
            if (invocation.day != lastDay && lastDay >= 0) appendLine()
            lastDay = invocation.day
            append(of(invocation))
        }
        appendLine()
        append(SimulationSummary.of(run))
    }

    /** One invocation, spoken or silent. */
    fun of(invocation: SimulatedInvocation): String = buildString {
        val where = "[${invocation.surface.label}]"
        val spoken = invocation.spoken
        if (spoken == null) {
            appendLine("${invocation.dateKey}  $where  SILENT (${invocation.silence})")
            invocation.vetoes.forEach { appendLine("  vetoed: $it") }
            return@buildString
        }
        appendLine(
            "${invocation.dateKey}  $where  ${spoken.familyKey} / stage ${spoken.stage} / " +
                "${spoken.register.name.lowercase()} / ${spoken.variantKey}",
        )
        appendLine("  rule:  ${spoken.ruleKey}")
        if (spoken.fired.isNotEmpty()) appendLine("  fired: ${spoken.fired.joinToString(", ")}")
        if (spoken.facts.isNotEmpty()) appendLine("  facts: ${spoken.facts.joinToString(", ")}")
        invocation.vetoes.forEach { appendLine("  vetoed: $it") }
        appendLine("  > ${spoken.statement}")
        spoken.question?.let { appendLine("  > $it") }
        if (spoken.responses.isNotEmpty()) {
            appendLine("  ? ${spoken.responses.joinToString(" | ") { it.label }}")
        }
    }

    private const val RULE = "================================================================"
}

/**
 * The counts phase 9 steers by, printed under every persona.
 *
 * These are the same numbers [SimulationChecks] measures, and they are printed whether the
 * check that reads them passes or not. A gate that only reports on failure tells an author
 * nothing about how close a corpus is to the target it is being grown toward, and the
 * whole reason the simulator comes before the corpus is so that the growing can be aimed.
 */
object SimulationSummary {

    fun of(run: SimulationRun): String = buildString {
        val pulses = run.of(SimulatedSurface.PULSE)
        val spoken = pulses.count { it.spoken != null }
        appendLine("-- counts ------------------------------------------------------")
        appendLine("pulse: ${pulses.size} days, $spoken spoken, ${pulses.size - spoken} silent " +
            "(${percent(pulses.size - spoken, pulses.size)} percent silent)")
        appendLine("silence reasons: " + tally(pulses.mapNotNull { it.silence?.name }))
        appendLine("pulse families: " + tally(pulses.mapNotNull { it.spoken?.familyKey }))
        appendLine("pulse stages:   " + tally(pulses.mapNotNull { it.spoken?.let { s -> "${s.familyKey}.s${s.stage}" } }))
        for (surface in listOf(
            SimulatedSurface.MOMENTUM,
            SimulatedSurface.BANNER,
            SimulatedSurface.REPORT_HEADLINE,
            SimulatedSurface.REPORT_OBSERVATION,
            SimulatedSurface.REPORT_PATTERN,
        )) {
            val on = run.of(surface)
            val said = on.count { it.spoken != null }
            appendLine("${surface.label}: ${on.size} invocations, $said spoken")
            if (said > 0) appendLine("  families: " + tally(on.mapNotNull { it.spoken?.familyKey }))
        }
        val vetoes = run.invocations.sumOf { it.vetoes.size }
        appendLine("layer 5 vetoes: $vetoes")
        appendLine("distinct variants used: " + run.invocations.mapNotNull { it.spoken?.variantKey }.toSet().size)
    }

    /** Counts by value, most frequent first, then by name so two runs print the same order. */
    fun tally(values: List<String>): String {
        if (values.isEmpty()) return "none"
        return values.groupingBy { it }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .joinToString(", ") { "${it.key} ${it.value}" }
    }

    /** A whole percentage, and zero when there is nothing to divide by. */
    fun percent(part: Int, whole: Int): Int = if (whole == 0) 0 else part * 100 / whole
}
