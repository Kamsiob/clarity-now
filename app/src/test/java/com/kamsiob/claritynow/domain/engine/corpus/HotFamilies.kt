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
 * The numbers below are the ninth measurement, taken over the eleven personas of section 12
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
 * are hot, at 91 and 608.
 *
 * ## What the ninth measurement moved, and the three rows at the boundary
 *
 * The Pulse repeat filter was bounded to yesterday, which is what 7.3 always said it
 * covered, and the Pulse spoke 305 more times across the eleven years. Every Pulse row here
 * rose. Three families sit within two firings of the forty firing line and all three
 * crossed it: `freshStart` came in at 42, and `consistentRhythm` and `cleanSlate` both fell
 * from 42 to below forty and are gone from the table.
 *
 * **A row this close to the line will cross it again.** The tier is a threshold and the
 * families near it will flap, which is a property of a hard cut and not a defect in the
 * measurement. What follows a crossing is mechanical and cheap: the anchors move with the
 * table, and a bench that arrives short of 11.1's sixty has its debt recorded rather than
 * paid, exactly as `weekQuiet`'s was.
 *
 * ## The tenth measurement, phase 9b, and why fourteen rows moved by a little
 *
 * **Not one family entered or left the hot tier.** The set is identical to the ninth
 * measurement's and every change is a count: the largest is `singleAreaWeek` at plus 56 out
 * of 1,033, and eleven of the fourteen moved by fewer than ten firings across eleven
 * simulated years.
 *
 * Two things in phase 9b could move these numbers and both did a little.
 *
 * **The persona's log changed, which is most of it.** `acceptsEveryPlan` used to write a
 * synthetic `PLAN_OFFERED` and `PLAN_ACCEPTED` every single report week, because layer 6
 * did not exist and the simulator had to fabricate the shape. It now writes a real offer
 * and a real acceptance only on the weeks layer 6 actually produces a plan, which is
 * roughly half of them. Fewer events on different days in one persona's year is a different
 * year, and the Momentum and banner rows are where a persona's own event count shows up
 * most directly. All three Momentum moves net to roughly zero, which is what a redistributed
 * fortnight looks like rather than a changed rule.
 *
 * **The follow through boost reorders, which is the rest of it.** 10.6 raises the family of
 * an accepted plan by one place in step 6's second term, for that persona only, on the weeks
 * after an acceptance. The four `REPORT_OBSERVATION` rows that moved are exactly what a
 * reordering inside one specificity level produces: `timeOfDay` gives up five, three of
 * which go to `completionSplit`. **No row gained or lost enough to change what any bench is
 * sized against**, which is the property this table exists to protect.
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
        Hot(Purpose.PULSE, "persistence", 482),
        Hot(Purpose.PULSE, "quietDay", 286),
        Hot(Purpose.PULSE, "concentration", 247),
        Hot(Purpose.PULSE, "accumulation", 163),
        Hot(Purpose.PULSE, "rebalance", 100),
        // **New, and it is the second bench debt this table carries.** 26 firings at the
        // eighth measurement. The repeat filter's recency bound let a family that had been
        // blocked off a Pulse from any distance speak again, and this is the family that
        // gained proportionally most from it: it fires on a first week and on a return, both
        // of which are exactly the moments a long stale `lastGeneratedFamily` used to sit on.
        // Its bench is short of 11.1's sixty and the debt is recorded rather than paid, for
        // the reason `weekQuiet`'s is below.
        Hot(Purpose.PULSE, "freshStart", 42),
        Hot(Purpose.REPORT_HEADLINE, "comeback", 90),
        Hot(Purpose.REPORT_HEADLINE, "personalBest", 88),
        Hot(Purpose.REPORT_HEADLINE, "mostActiveSince", 78),
        Hot(Purpose.REPORT_HEADLINE, "balanced", 62),
        Hot(Purpose.REPORT_OBSERVATION, "areaRevival", 219),
        Hot(Purpose.REPORT_OBSERVATION, "intakeVsOutput", 212),
        // 102 at the sixth measurement, 113 at the eighth, 162 now. This family reads the
        // label a person last gave a Pulse, so it moves whenever the Pulse speaks more: the
        // recency bound put 305 more Pulses into the eleven logs and this row is where most
        // of that arrives on the Report.
        Hot(Purpose.REPORT_OBSERVATION, "completionSplit", 165),
        Hot(Purpose.REPORT_OBSERVATION, "queuePressure", 156),
        Hot(Purpose.REPORT_OBSERVATION, "areaBalance", 118),
        Hot(Purpose.REPORT_OBSERVATION, "timeOfDay", 100),
        Hot(Purpose.REPORT_OBSERVATION, "persistentItem", 90),
        Hot(Purpose.REPORT_OBSERVATION, "mostActiveSince", 88),
        Hot(Purpose.REPORT_OBSERVATION, "personalBest", 88),
        // 60 at the eighth measurement. Same cause as `completionSplit`: it sets what
        // somebody said against what happened, and there is more of what somebody said.
        Hot(Purpose.REPORT_OBSERVATION, "selfReportVsData", 85),
        Hot(Purpose.REPORT_OBSERVATION, "singleFocus", 50),
        Hot(Purpose.REPORT_OBSERVATION, "steadyPace", 45),
        Hot(Purpose.REPORT_PATTERN, "reportedVsActual", 177),
        Hot(Purpose.REPORT_PATTERN, "comebackPattern", 70),
        Hot(Purpose.REPORT_PATTERN, "growingQueues", 57),
        Hot(Purpose.MOMENTUM_HEADLINE, "singleAreaWeek", 1089),
        Hot(Purpose.MOMENTUM_HEADLINE, "balancedWeek", 849),
        Hot(Purpose.MOMENTUM_HEADLINE, "comeback", 574),
        Hot(Purpose.MOMENTUM_HEADLINE, "steadyStretch", 321),
        Hot(Purpose.MOMENTUM_HEADLINE, "quietStretch", 153),
        Hot(Purpose.MOMENTUM_HEADLINE, "firstDays", 139),
        Hot(Purpose.AREAS_BANNER, "weekMixed", 1244),
        // 804 at the sixth measurement. The register pass gave `weekQuiet` a voice it could
        // speak in, and the windows this family used to take are windows where the quiet week
        // now outranks it. Those windows did not change; what changed is that one of the two
        // candidates on them stopped being unrealizable.
        Hot(Purpose.AREAS_BANNER, "weekStarting", 724),
        Hot(Purpose.AREAS_BANNER, "weekBuilding", 331),
        // **The largest bench debt in the corpus.** This family fired zero times in every
        // measurement before the register pass, because all eight of its lines are `[N]` and
        // nothing could ask for that register: it qualified, the realizer answered
        // `NotProducible`, and the selector moved on. 7.4 now marks it unflattering and it
        // takes 234 banner windows a year.
        //
        // **234 firings makes it hot, and it holds eight lines against 11.1's sixty.** That
        // is recorded in `CorpusGateBaseline.REGISTERS` rather than paid, because authoring is
        // not this pass's to do. `CORPUS_3_MOMENTUM.md` says the bench was left at eight on
        // purpose "because it has never once spoken", which was true and is not any more.
        Hot(Purpose.AREAS_BANNER, "weekQuiet", 242),
        Hot(Purpose.AREAS_BANNER, "weekStrong", 65),
    )

    private val KEYS: Set<Pair<Purpose, FamilyKey>> = ALL.map { it.purpose to it.family }.toSet()

    /** True when this bench is one 11.1 sizes at sixty to a hundred. */
    fun isHot(purpose: Purpose, family: FamilyKey): Boolean = (purpose to family) in KEYS

    /** The measured firings, or null for a family that is not hot. */
    fun firings(purpose: Purpose, family: FamilyKey): Int? =
        ALL.firstOrNull { it.purpose == purpose && it.family == family }?.firingsPerYear
}
