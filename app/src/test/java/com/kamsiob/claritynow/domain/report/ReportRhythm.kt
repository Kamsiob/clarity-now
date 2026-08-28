package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.realize.Candidate

/**
 * The two rhythm rules of CLARITY_LOGIC_ENGINE.md 9.2, counted over finished reports.
 *
 * ## Why they are counted and not asserted
 *
 * Both are preferences. The composer takes the highest ranked line whose band differs and
 * whose place in the run is free, and where no remaining line in the section offers either,
 * it takes the highest ranked one anyway. 11.4 is the reason: a report may be short and it
 * may not be padded, and dropping a true observation to improve the cadence is the same
 * trade in the other direction. So a residue is expected, and a number that nobody prints
 * is a number nobody argues about.
 *
 * ## What it separates, and why the split is the whole point
 *
 * A collision has three possible causes and only one of them is anybody's fault:
 *
 * - **Nothing was left to reorder.** The line was the last one in its section, so the only
 *   remedy was to drop it. That is the preference behaving correctly
 * - **Every remaining line in the section shared the band.** The realizer chose one band
 *   for several families at once, and the composer had nothing else to offer. That is a
 *   bench that cannot offer a second band, and it is the reading 11.1 acts on
 * - **The other rule won.** A line that would have broken the band ran into the parallel
 *   clause cap, or the reverse. That is the trade the ordering in `ReportComposer.arrange`
 *   makes on purpose, and it is counted so the price of the ordering is visible
 *
 * The pattern is a fourth bucket of its own. It is one line, it is read last, and its
 * position is fixed, so the composer can seed a run with it and can never move it.
 *
 * ## The page, rather than the observations
 *
 * A reader meets the headline, then the observations, then the pattern, so that is the
 * sequence measured here. The basis line is a footer rather than a lead and is left out,
 * which is the same boundary `SimulationChecks` draws over the simulator's own dump.
 */
internal class ReportRhythm {

    var reports: Int = 0
        private set
    var leads: Int = 0
        private set

    /** Consecutive pairs of leads sharing a length band, over the whole page. */
    var bandCollisions: Int = 0
        private set

    /** Leads that are the third or later numeric lead in a row. See [numbersIn]. */
    var parallelRun: Int = 0
        private set

    /** The same count under the narrower reading: a lead that renders two or more numbers. */
    var parallelRunPaired: Int = 0
        private set

    /** Leads that state a number at all, which is the ceiling on what either rule can fix. */
    var numericLeads: Int = 0
        private set

    /** Reports carrying at least one run of three, which is what a reader would notice. */
    var reportsWithARun: Int = 0
        private set

    private val bandCause = mutableMapOf<Cause, Int>()

    private val runCause = mutableMapOf<Cause, Int>()

    /** Why a collision could not be avoided. */
    enum class Cause {
        /** The line was the last one left in its section. Only a drop would have moved it. */
        NOTHING_LEFT,

        /** Lines were left and every one of them collided the same way. */
        NO_ALTERNATIVE,

        /** An alternative existed and the other rhythm rule took the pick. */
        TRADED,

        /** The pattern is read last and is never reordered. */
        PATTERN,
    }

    fun read(report: ClarityReport) {
        reports++
        val page = page(report)
        leads += page.size
        var run = 0
        var runPaired = 0
        var caught = false
        for ((index, lead) in page.withIndex()) {
            if (numbersIn(lead.candidate) >= ONE_NUMBER) numericLeads++
            val previous = page.getOrNull(index - 1)
            if (previous != null && previous.candidate.lengthBand == lead.candidate.lengthBand) {
                bandCollisions++
                bandCause.merge(causeOf(lead, report) { it.lengthBand != previous.candidate.lengthBand }, 1, Int::plus)
            }
            run = if (numbersIn(lead.candidate) >= ONE_NUMBER) run + 1 else 0
            runPaired = if (numbersIn(lead.candidate) >= PAIRED_NUMBERS) runPaired + 1 else 0
            if (run > MAX_PARALLEL_CLAUSES) {
                parallelRun++
                caught = true
                runCause.merge(causeOf(lead, report) { numbersIn(it) < ONE_NUMBER }, 1, Int::plus)
            }
            if (runPaired > MAX_PARALLEL_CLAUSES) parallelRunPaired++
        }
        if (caught) reportsWithARun++
    }

    /**
     * Which of the four situations [lead] was in, given what would have avoided the collision.
     *
     * The reconstruction is exact rather than approximate. `ReportComposer.arrange` walks the
     * sections in order and consumes each section's candidates one at a time, so the lines
     * still on the bench when a pick was made are precisely the lines of the same section
     * that appear later on the finished page.
     */
    private fun causeOf(lead: Lead, report: ClarityReport, avoids: (Candidate) -> Boolean): Cause {
        if (lead.section == null) return Cause.PATTERN
        val later = report.observations
            .drop(lead.observationIndex + 1)
            .filter { it.section == lead.section }
            .map { it.candidate }
        return when {
            later.isEmpty() -> Cause.NOTHING_LEFT
            later.none(avoids) -> Cause.NO_ALTERNATIVE
            else -> Cause.TRADED
        }
    }

    private fun page(report: ClarityReport): List<Lead> = buildList {
        report.headline?.let { add(Lead(it, null, -1)) }
        report.observations.forEachIndexed { index, observation ->
            add(Lead(observation.candidate, observation.section, index))
        }
        report.pattern?.let { add(Lead(it, null, -1)) }
    }

    /** One lead, and where on the page it sits. A null section is the headline or the pattern. */
    private data class Lead(val candidate: Candidate, val section: ReportSection?, val observationIndex: Int)

    fun render(): String = buildString {
        appendLine("rhythm over $reports composed reports, $leads leads, $numericLeads of them numeric:")
        appendLine("  $bandCollisions consecutive pairs share a length band ${render(bandCause)}")
        appendLine(
            "  $parallelRun leads are the third numeric lead in a row, in $reportsWithARun reports " +
                render(runCause),
        )
        appendLine("  $parallelRunPaired under the narrower reading, a lead rendering two or more numbers")
    }

    private fun render(causes: Map<Cause, Int>): String =
        Cause.entries.filter { causes[it] != null }.joinToString(", ", "(", ")") { "${causes[it]} ${label(it)}" }

    private fun label(cause: Cause): String = when (cause) {
        Cause.NOTHING_LEFT -> "with nothing left in the section to reorder"
        Cause.NO_ALTERNATIVE -> "where every line left in the section collided the same way"
        Cause.TRADED -> "traded away to hold the other rule"
        Cause.PATTERN -> "on the pattern, which is read last and never reordered"
    }

    private companion object {

        /** 9.2, and `ReportComposer.MAX_PARALLEL_CLAUSES`, restated rather than imported. */
        const val MAX_PARALLEL_CLAUSES = 2

        const val ONE_NUMBER = 1

        const val PAIRED_NUMBERS = 2

        /**
         * How many numbers a lead renders.
         *
         * Counted from the slots rather than from the digits in the rendered string, which
         * is what the simulator does over its dump. A person's own area name or item title
         * can carry a digit, and the string test would read `Proposal v2` as a number the
         * engine stated. A slot is a number the engine chose to say.
         */
        fun numbersIn(candidate: Candidate): Int = candidate.slots.values.count { it.numericValue != null }
    }
}
