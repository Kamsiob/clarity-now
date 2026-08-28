package com.kamsiob.claritynow.domain.engine.corpus

/**
 * What a gate found, and how a gate says it.
 *
 * ## Why these gates exist at all
 *
 * Phase 9 writes roughly two thousand lines across eight sessions, and the owner reads the
 * corpus **once, at the end**, as one annotated file. That is only safe if everything a
 * reader would have checked batch by batch is checked by a machine on every run instead. So
 * every gate here is written to replace a specific act of reading, and every finding names
 * the line, the file and the line number, because a finding an author cannot act on in
 * thirty seconds is a finding that gets argued with rather than fixed.
 *
 * ## Grandfathering, and why it is by size rather than by name
 *
 * Several of these gates find things in the corpus as it stands, and the corpus was authored
 * before any of them existed. There are three things one can do with that: turn the gate
 * off, record the offending lines by name forever, or record what the bench looked like on
 * the day the gate was written and hold anything larger to the rule.
 *
 * The third is used wherever the rule is about a bench rather than about a line, and it is
 * the only one of the three that expires. `persistence s1` holds fifteen lines today and
 * twelve of them are in one length band; the entry that grandfathers it is `fifteen lines`,
 * so the moment phase 9 adds a sixteenth the exemption lapses and the whole bench is held to
 * the cap. An author who grows a bench inherits its debt, which is right: they are the only
 * person who will ever be in a position to pay it.
 *
 * **An exemption holds at the recorded size and at no other, in both directions.** A bench
 * that loses a line is not the bench somebody looked at, and reading the record as a ceiling
 * meant a shrinking corpus was an excused one. `CorpusCensus` carries the incident that
 * made the point and the floor that answers it.
 *
 * Where the rule is about a specific pair of lines rather than about a bench, the pair is
 * recorded by key instead, because the instruction to phase 9 is not to reword an approved
 * line.
 */
internal data class GateFinding(
    /** What the finding is about: a bench id, a variant key, a fragment. */
    val subject: String,
    /** What is wrong, in a sentence an author can act on. */
    val detail: String,
    /** `file:line key`, or empty where the finding is about a bench rather than a line. */
    val origin: String = "",
) {
    override fun toString(): String = if (origin.isEmpty()) "$subject: $detail" else "$subject: $detail [$origin]"
}

/** One gate's result over one catalog. */
internal data class GateOutcome(
    val id: String,
    val name: String,
    val citation: String,
    /** What fails the build. */
    val findings: List<GateFinding>,
    /** What the gate found and a recorded baseline excuses, kept so it is visible rather than invisible. */
    val grandfathered: List<GateFinding> = emptyList(),
    /** The number this gate produced, printed whether it passed or failed. */
    val measured: String = "",
) {
    val passed: Boolean get() = findings.isEmpty()

    fun render(): String = buildString {
        appendLine("${if (passed) "pass" else "FAIL"}  $id: $name")
        appendLine("      $citation")
        if (measured.isNotEmpty()) appendLine("      measured: $measured")
        if (grandfathered.isNotEmpty()) {
            appendLine("      grandfathered: ${grandfathered.size}")
            grandfathered.take(GRANDFATHER_SAMPLE).forEach { appendLine("        - $it") }
            if (grandfathered.size > GRANDFATHER_SAMPLE) {
                appendLine("        - and ${grandfathered.size - GRANDFATHER_SAMPLE} more")
            }
        }
        findings.forEach { appendLine("      ! $it") }
    }

    private companion object {
        const val GRANDFATHER_SAMPLE = 6
    }
}

/** Every gate, over one catalog. */
internal data class GateReport(val outcomes: List<GateOutcome>) {

    val failed: List<GateOutcome> get() = outcomes.filterNot { it.passed }

    fun outcome(id: String): GateOutcome = outcomes.first { it.id == id }

    fun render(): String = buildString {
        appendLine(RULE)
        appendLine("corpus gates, CLARITY_LOGIC_ENGINE.md 11.2 and 11.3")
        appendLine(RULE)
        outcomes.forEach { append(it.render()) }
        appendLine(RULE)
        appendLine("${outcomes.count { it.passed }} of ${outcomes.size} gates pass")
        appendLine(RULE)
    }

    private companion object {
        const val RULE = "--------------------------------------------------------------------"
    }
}
