package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.Purpose

/**
 * The hot families, measured rather than judged. CLARITY_LOGIC_ENGINE.md 11.1 and 12.
 *
 * 11.1 sizes a bench by how often it fires: forty firings a year or more is hot and wants
 * sixty to a hundred variants per stage bench, five to twenty is warm and wants fifteen to
 * thirty, under five is long tail and wants four to eight. Phase 9 grows the hot benches
 * and touches nothing else, so which families are hot decides which lines get written and
 * which benches these gates hold to the tighter standard.
 *
 * ## Why the table is a constant with a test behind it
 *
 * The numbers below are the sixth measurement, taken over the eleven personas of section 12
 * across a full simulated year each, read out of `SimulationAggregate.hotFamilies`. They are
 * a constant here because the six fast gates in this package run in under a second and
 * measuring a year of eleven lives takes three minutes; an author running the gates after
 * every batch of forty would stop running them.
 *
 * `CorpusRenderTest` re-measures on every run and fails if this table has drifted, so the
 * constant is watched rather than remembered. That matters more than it looks: a family
 * that crosses forty firings and is not listed here is a bench that quietly keeps the long
 * tail's standards while firing weekly.
 *
 * ## What the measurement settled that the brief could not
 *
 * Five family keys are declared at two purposes, and a firing count attached to the key
 * alone cannot say which bench earned it. The measurement separates all five.
 * `queuePressure` is hot as an observation at 166 and dark as a headline, which is why only
 * one of its two benches is here. `personalBest` is hot at both, at 88 each.
 * `mostActiveSince` is hot at both, at 88 and 78. `singleFocus` and `steadyPace` are hot as
 * observations only. `comeback` is a different family at each of its two purposes and both
 * are hot, at 90 and 604.
 */
internal object HotFamilies {

    /** 11.1. Forty firings a year or more. The same constant `SimulationChecks` uses. */
    const val HOT_FIRINGS_PER_YEAR = 40

    /** 11.1. What a hot stage bench is grown to. */
    const val HOT_BENCH_FLOOR = 60

    /** 11.1. The upper end, above which quality is the constraint rather than depth. */
    const val HOT_BENCH_CEILING = 100

    /** One measured family, at one purpose. */
    data class Hot(val purpose: Purpose, val family: FamilyKey, val firingsPerYear: Int)

    /**
     * Every hot bench, with the firings the sixth measurement counted.
     *
     * Sorted as the simulator prints them: purpose in declaration order, then family.
     */
    val ALL: List<Hot> = listOf(
        Hot(Purpose.PULSE, "persistence", 277),
        Hot(Purpose.PULSE, "quietDay", 227),
        Hot(Purpose.PULSE, "concentration", 211),
        Hot(Purpose.PULSE, "accumulation", 157),
        Hot(Purpose.PULSE, "rebalance", 101),
        Hot(Purpose.REPORT_HEADLINE, "comeback", 90),
        Hot(Purpose.REPORT_HEADLINE, "personalBest", 88),
        Hot(Purpose.REPORT_HEADLINE, "mostActiveSince", 78),
        Hot(Purpose.REPORT_HEADLINE, "balanced", 61),
        Hot(Purpose.REPORT_OBSERVATION, "intakeVsOutput", 246),
        Hot(Purpose.REPORT_OBSERVATION, "areaRevival", 219),
        Hot(Purpose.REPORT_OBSERVATION, "queuePressure", 166),
        Hot(Purpose.REPORT_OBSERVATION, "areaBalance", 117),
        Hot(Purpose.REPORT_OBSERVATION, "persistentItem", 106),
        Hot(Purpose.REPORT_OBSERVATION, "timeOfDay", 106),
        // 102 at the sixth measurement, 101 once phase 9 grew the Pulse response benches and
        // 113 once it grew the Report ones: this family reads the label a person last gave a
        // Pulse, and a deeper pool of response pairs changes which label the simulated
        // personas hand back. It is the only row in this table that phase 9 moved, and the
        // tier is unchanged at every reading of it. Re-measure the whole table when phase 9
        // closes.
        Hot(Purpose.REPORT_OBSERVATION, "completionSplit", 113),
        Hot(Purpose.REPORT_OBSERVATION, "mostActiveSince", 88),
        Hot(Purpose.REPORT_OBSERVATION, "personalBest", 88),
        Hot(Purpose.REPORT_OBSERVATION, "selfReportVsData", 60),
        Hot(Purpose.REPORT_OBSERVATION, "singleFocus", 50),
        Hot(Purpose.REPORT_OBSERVATION, "steadyPace", 46),
        Hot(Purpose.REPORT_PATTERN, "reportedVsActual", 173),
        Hot(Purpose.REPORT_PATTERN, "comebackPattern", 70),
        Hot(Purpose.REPORT_PATTERN, "growingQueues", 55),
        Hot(Purpose.REPORT_PATTERN, "consistentRhythm", 42),
        Hot(Purpose.MOMENTUM_HEADLINE, "singleAreaWeek", 1054),
        Hot(Purpose.MOMENTUM_HEADLINE, "balancedWeek", 831),
        Hot(Purpose.MOMENTUM_HEADLINE, "comeback", 604),
        Hot(Purpose.MOMENTUM_HEADLINE, "steadyStretch", 335),
        Hot(Purpose.MOMENTUM_HEADLINE, "firstDays", 139),
        Hot(Purpose.MOMENTUM_HEADLINE, "quietStretch", 124),
        Hot(Purpose.MOMENTUM_HEADLINE, "cleanSlate", 42),
        Hot(Purpose.AREAS_BANNER, "weekMixed", 1308),
        // 804 at the sixth measurement. The register pass gave `weekQuiet` a voice it could
        // speak in, and 73 of the windows this family used to take are windows where the
        // quiet week now outranks it. Those windows did not change; what changed is that
        // one of the two candidates on them stopped being unrealizable.
        Hot(Purpose.AREAS_BANNER, "weekStarting", 731),
        Hot(Purpose.AREAS_BANNER, "weekBuilding", 284),
        // **New, and it is the finding rather than a row.** This family fired zero times in
        // every measurement before the register pass, because all eight of its lines are
        // `[N]` and nothing could ask for that register: it qualified, the realizer answered
        // `NotProducible`, and the selector moved on. 7.4 now marks it unflattering and it
        // takes 240 banner windows a year, 167 of which nothing spoke on at all.
        //
        // **240 firings makes it hot, and it holds eight lines against 11.1's sixty.** That
        // is the largest bench debt in the corpus and it is recorded in
        // `CorpusGateBaseline.REGISTERS` rather than paid, because authoring is not this
        // pass's to do. `CORPUS_3_MOMENTUM.md` says the bench was left at eight on purpose
        // "because it has never once spoken", which was true and is not any more.
        Hot(Purpose.AREAS_BANNER, "weekQuiet", 240),
        Hot(Purpose.AREAS_BANNER, "weekStrong", 65),
    )

    private val KEYS: Set<Pair<Purpose, FamilyKey>> = ALL.map { it.purpose to it.family }.toSet()

    /** True when this bench is one 11.1 sizes at sixty to a hundred. */
    fun isHot(purpose: Purpose, family: FamilyKey): Boolean = (purpose to family) in KEYS

    /** The measured firings, or null for a family that is not hot. */
    fun firings(purpose: Purpose, family: FamilyKey): Int? =
        ALL.firstOrNull { it.purpose == purpose && it.family == family }?.firingsPerYear
}
