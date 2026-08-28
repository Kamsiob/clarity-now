package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog

/**
 * How many lines every bench held when the census was last taken, as a floor.
 *
 * ## The hole this closes
 *
 * `CorpusGateBaseline` grandfathers a finding by recording the size of the bench it was
 * found on, and the exemption lapses when the bench grows. That is the right shape and it
 * had one direction. A bench that **shrank** was smaller than its record, so it was still
 * excused, and it was excused more deeply than before: a rule whose strictness is a
 * function of how much work is present is always weakest immediately after work is lost.
 *
 * That is not a hypothetical. Mid way through phase 9 a `git checkout` destroyed 336
 * uncommitted lines, and all seven fast gates went green over the shortened corpus. Only
 * `CorpusAnchorsTest` said anything, and only because an anchor happened to quote a line
 * that no longer existed. **A gate that goes green when work disappears is worse than no
 * gate, because it is trusted.**
 *
 * Two things answer it, and they are separate on purpose. The exemptions in
 * `CorpusGateBaseline` are now statements about a bench of exactly that size, so they lapse
 * in both directions. And this file records what every bench holds, so a bench that loses a
 * line fails here, by name, with the count it lost, instead of failing somewhere else as a
 * band spread that nobody moved.
 *
 * ## Why per bench, when the volumes are already counted exactly
 *
 * `KnownCorpusViolations.TOTALS` records each file's keyed line count and `CorpusParseTest`
 * asserts it exactly, which already fails on a file that shrinks. It is blind to one shape
 * this is not: a **compensating** change. Thirty lines lost from four benches while thirty
 * are added to a fifth leaves every volume total untouched, and the shape of a corpus is
 * bench by bench rather than file by file. The volume totals stay where they are and cover
 * what this cannot, which is the auxiliary lines that sit on no bench at all.
 *
 * ## What it cannot see, stated rather than implied
 *
 * **This cannot see the loss of work that was never committed, and nothing in the tree
 * can.** A `git checkout` that reverts an uncommitted corpus edit reverts an uncommitted
 * census with it, and what is left is a state that is consistent with itself. What this
 * gives is a floor under everything that has been committed, and a loss that is localized
 * and named the moment it happens rather than a year later. The rest is the habit the
 * incident actually argues for, which is to commit an authoring batch before starting the
 * next one.
 *
 * ## Regenerating it is deliberate, never a side effect
 *
 * Growth is the job and never fails the build. `CorpusCensusTest` prints the number of
 * benches that have grown past their floor and prints the replacement for [BENCHES], and a
 * person pastes it in. That is the same rule the golden fixture keeps, one notch stricter:
 * there is no flag that rewrites this file, because the only thing a floor is for is to be
 * harder to move than the thing it measures.
 */
internal object CorpusCensus {

    /**
     * Bench id to the number of lines it is known to hold. See [Bench.id].
     *
     * A floor rather than a count. More is a reading; fewer is a finding.
     */
    val BENCHES: Map<String, Int> = mapOf(
        "AREAS_BANNER weekBuilding s1" to 72,
        "AREAS_BANNER weekMixed s1" to 77,
        "AREAS_BANNER weekQuiet s1" to 8,
        "AREAS_BANNER weekStarting s1" to 62,
        "AREAS_BANNER weekStrong s1" to 62,
        "MOMENTUM_HEADLINE balancedWeek s1" to 65,
        "MOMENTUM_HEADLINE cleanSlate s1" to 60,
        "MOMENTUM_HEADLINE comeback s1" to 70,
        "MOMENTUM_HEADLINE firstDays s1" to 62,
        "MOMENTUM_HEADLINE quietStretch s1" to 66,
        "MOMENTUM_HEADLINE singleAreaWeek s1" to 72,
        "MOMENTUM_HEADLINE steadyStretch s1" to 60,
        "MOMENTUM_HEADLINE strongPace s1" to 12,
        "PULSE accumulation s1" to 63,
        "PULSE accumulation s2" to 65,
        "PULSE accumulation s3" to 71,
        "PULSE burst s1" to 9,
        "PULSE burst s2" to 8,
        "PULSE concentration s1" to 64,
        "PULSE concentration s2" to 65,
        "PULSE concentration s3" to 70,
        "PULSE freshStart s1" to 10,
        "PULSE persistence s1" to 68,
        "PULSE persistence s2" to 70,
        "PULSE persistence s3" to 71,
        "PULSE persistence s4" to 67,
        "PULSE queueDrain s1" to 8,
        "PULSE queueDrain s2" to 7,
        "PULSE quietDay s1" to 67,
        "PULSE quietDay s2" to 65,
        "PULSE quietDay s3" to 66,
        "PULSE rebalance s1" to 68,
        "PULSE rebalance s2" to 68,
        "PULSE spread s1" to 11,
        "PULSE spread s2" to 11,
        "PULSE switching s1" to 9,
        "PULSE switching s2" to 9,
        "PULSE throughput s1" to 12,
        "PULSE throughput s2" to 13,
        "PULSE throughput s3" to 10,
        "REPORT_HEADLINE balanced s1" to 60,
        "REPORT_HEADLINE clearing s1" to 8,
        "REPORT_HEADLINE comeback s1" to 60,
        "REPORT_HEADLINE datedFallback s1" to 4,
        "REPORT_HEADLINE decliningActivity s1" to 10,
        "REPORT_HEADLINE firstWeek s1" to 6,
        "REPORT_HEADLINE focusProtected s1" to 10,
        "REPORT_HEADLINE fragmented s1" to 8,
        "REPORT_HEADLINE mostActiveSince s1" to 60,
        "REPORT_HEADLINE netInflow s1" to 12,
        "REPORT_HEADLINE netOutflow s1" to 12,
        "REPORT_HEADLINE personalBest s1" to 60,
        "REPORT_HEADLINE queuePressure s1" to 8,
        "REPORT_HEADLINE quietWeek s1" to 12,
        "REPORT_HEADLINE risingActivity s1" to 10,
        "REPORT_HEADLINE singleFocus s1" to 12,
        "REPORT_HEADLINE steadyPace s1" to 8,
        "REPORT_OBSERVATION areaBalance s1" to 60,
        "REPORT_OBSERVATION areaBalance s1 ext" to 20,
        "REPORT_OBSERVATION areaRevival s1" to 60,
        "REPORT_OBSERVATION areaRevival s1 ext" to 20,
        "REPORT_OBSERVATION completionSplit s1" to 60,
        "REPORT_OBSERVATION completionSplit s1 ext" to 20,
        "REPORT_OBSERVATION dayShape s1" to 7,
        "REPORT_OBSERVATION dayShape s1 ext" to 5,
        "REPORT_OBSERVATION estimateCalibration s1" to 8,
        "REPORT_OBSERVATION estimateCalibration s1 ext" to 6,
        "REPORT_OBSERVATION familiarDip s1" to 12,
        "REPORT_OBSERVATION familiarDip s1 ext" to 6,
        "REPORT_OBSERVATION firstMilestone s1" to 6,
        "REPORT_OBSERVATION firstMilestone s1 ext" to 3,
        "REPORT_OBSERVATION focusAbandonment s1" to 8,
        "REPORT_OBSERVATION focusAbandonment s1 ext" to 4,
        "REPORT_OBSERVATION focusInvestment s1" to 6,
        "REPORT_OBSERVATION focusInvestment s1 ext" to 5,
        "REPORT_OBSERVATION focusInvestment s2" to 7,
        "REPORT_OBSERVATION focusInvestment s2 ext" to 6,
        "REPORT_OBSERVATION focusInvestment s3" to 6,
        "REPORT_OBSERVATION focusInvestment s3 ext" to 5,
        "REPORT_OBSERVATION hardStretch s1" to 16,
        "REPORT_OBSERVATION hardStretch s1 ext" to 6,
        "REPORT_OBSERVATION intakeVsOutput s1" to 60,
        "REPORT_OBSERVATION intakeVsOutput s1 ext" to 20,
        "REPORT_OBSERVATION intakeVsOutput s2" to 60,
        "REPORT_OBSERVATION intakeVsOutput s2 ext" to 20,
        "REPORT_OBSERVATION intakeVsOutput s3" to 60,
        "REPORT_OBSERVATION intakeVsOutput s3 ext" to 20,
        "REPORT_OBSERVATION mostActiveSince s1" to 60,
        "REPORT_OBSERVATION mostActiveSince s1 ext" to 20,
        "REPORT_OBSERVATION neglectedArea s1" to 10,
        "REPORT_OBSERVATION neglectedArea s1 ext" to 5,
        "REPORT_OBSERVATION neglectedArea s2" to 11,
        "REPORT_OBSERVATION neglectedArea s2 ext" to 5,
        "REPORT_OBSERVATION persistentItem s1" to 60,
        "REPORT_OBSERVATION persistentItem s1 ext" to 20,
        "REPORT_OBSERVATION personalBest s1" to 60,
        "REPORT_OBSERVATION personalBest s1 ext" to 20,
        "REPORT_OBSERVATION queueDrained s1" to 5,
        "REPORT_OBSERVATION queueDrained s1 ext" to 4,
        "REPORT_OBSERVATION queuePressure s1" to 60,
        "REPORT_OBSERVATION queuePressure s1 ext" to 20,
        "REPORT_OBSERVATION quietWeek s1" to 11,
        "REPORT_OBSERVATION quietWeek s1 ext" to 5,
        "REPORT_OBSERVATION selfReportVsData s1" to 60,
        "REPORT_OBSERVATION selfReportVsData s1 ext" to 20,
        "REPORT_OBSERVATION singleFocus s1" to 60,
        "REPORT_OBSERVATION singleFocus s1 ext" to 20,
        "REPORT_OBSERVATION singleFocus s2" to 60,
        "REPORT_OBSERVATION singleFocus s2 ext" to 20,
        "REPORT_OBSERVATION steadyPace s1" to 60,
        "REPORT_OBSERVATION steadyPace s1 ext" to 20,
        "REPORT_OBSERVATION switchingBehavior s1" to 9,
        "REPORT_OBSERVATION switchingBehavior s1 ext" to 5,
        "REPORT_OBSERVATION timeOfDay s1" to 60,
        "REPORT_OBSERVATION timeOfDay s1 ext" to 20,
        "REPORT_PATTERN abandonmentPattern s1" to 4,
        "REPORT_PATTERN areaGoneQuiet s1" to 9,
        "REPORT_PATTERN broadeningFocus s1" to 7,
        "REPORT_PATTERN comebackPattern s1" to 60,
        "REPORT_PATTERN consistentRhythm s1" to 60,
        "REPORT_PATTERN decliningActivity s1" to 8,
        "REPORT_PATTERN focusHabitFading s1" to 6,
        "REPORT_PATTERN focusHabitForming s1" to 7,
        "REPORT_PATTERN growingQueues s1" to 60,
        "REPORT_PATTERN improvingThroughput s1" to 8,
        "REPORT_PATTERN insufficientData s1" to 4,
        "REPORT_PATTERN narrowingFocus s1" to 8,
        "REPORT_PATTERN queueEquilibrium s1" to 5,
        "REPORT_PATTERN reportedVsActual s1" to 60,
        "REPORT_PATTERN shiftingFocus s1" to 10,
        "REPORT_PATTERN weekendShift s1" to 5,
    )

    /** Every bench that holds fewer lines than the census recorded, or has gone entirely. */
    fun shortfalls(catalog: ClarityCatalog): List<GateFinding> {
        val sizes = CorpusBenches.of(catalog).associate { it.id to it.size }
        return BENCHES.entries.sortedBy { it.key }.mapNotNull { (benchId, floor) ->
            val size = sizes[benchId]
            when {
                size == null -> GateFinding(
                    subject = benchId,
                    detail = "held $floor lines when the census was taken and is not in the " +
                        "corpus at all. A bench does not disappear by accident",
                )

                size < floor -> GateFinding(
                    subject = benchId,
                    detail = "held $floor lines when the census was taken and holds $size. " +
                        "Lines were lost rather than authored, or the census was regenerated " +
                        "over a corpus somebody had already broken",
                )

                else -> null
            }
        }
    }

    /** Benches that have grown past the floor, and benches the census has never seen. */
    fun growth(catalog: ClarityCatalog): List<String> {
        val benches = CorpusBenches.of(catalog)
        return benches
            .sortedBy { it.id }
            .mapNotNull { bench ->
                val floor = BENCHES[bench.id]
                when {
                    floor == null -> "${bench.id} is new, at ${bench.size} lines"
                    bench.size > floor -> "${bench.id} has grown from $floor to ${bench.size}"
                    else -> null
                }
            }
    }

    /** The replacement for [BENCHES], as source a person pastes over it. */
    fun regenerate(catalog: ClarityCatalog): String = buildString {
        appendLine("    val BENCHES: Map<String, Int> = mapOf(")
        CorpusBenches.of(catalog).sortedBy { it.id }.forEach { appendLine("""        "${it.id}" to ${it.size},""") }
        appendLine("    )")
    }
}
