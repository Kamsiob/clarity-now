package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.FamilyKey
import com.kamsiob.claritynow.domain.engine.catalog.Purpose

/**
 * What the corpus looked like on the day the gates were written, so a gate can fail on
 * something new without failing on everything old.
 *
 * **This file is the honest answer to "which gates does the corpus already fail".** On the
 * day it was written the seven fast gates produced **259 findings** over 1,554 rendered
 * strings, and the slow render gate produced 82 more:
 *
 * | gate | findings on the day this was written |
 * |---|---|
 * | fragment | 9 clauses shared by two families of one purpose |
 * | construction | 4 shapes past the cap of two families, two of them already recorded by the catalog |
 * | vocabulary | 0 |
 * | binding | 99 markers with no fact behind them, across 80 lines |
 * | lengthBand | 74 of 108 measurable benches with one band over sixty percent |
 * | register | 63, across 53 benches with a thin or missing register |
 * | duplicate | 10 pairs of lines that read as one line |
 * | renders | 82 lines that no real moment could fill or that layer 5 always vetoed |
 *
 * Every one of them is recorded below and nothing else is, so the next finding fails the
 * build.
 *
 * Every entry is a debt with a name on it, and phase 9 should leave this file shorter rather
 * than longer. Nothing is added here without a corpus edit being the alternative, which is
 * the rule `KnownCorpusViolations` states for the two checks it already grandfathers.
 *
 * ## Two kinds of entry, and why the difference matters
 *
 * A **bench** entry records the size of the bench on the day it was recorded. The exemption
 * holds while the bench is that size or smaller and lapses the moment a line is added. That
 * is the whole mechanism, and it is what makes this file expire instead of accumulating:
 * phase 9 grows a hot bench from a dozen lines to sixty, and on the thirteenth line the
 * exemption is gone and the whole bench is held to the rule. An author who grows a bench
 * inherits its debt, which is right, because they are the only person who will ever be in a
 * position to pay it.
 *
 * A **line** entry records specific keys, and never lapses. The instruction to phase 9 is
 * not to reword an approved line, so a collision between two lines that were both approved
 * before the gate existed cannot be fixed by somebody who is only allowed to add. One of
 * them is worth an editor's eye all the same and is called out where it is recorded.
 */
internal object CorpusGateBaseline {

    /** `purpose`, the shared clause, and the families that shared it when this was recorded. */
    data class RecordedFragment(val purpose: Purpose, val fragment: String, val families: Set<FamilyKey>)

    /**
     * Clauses in two families of one purpose today. See [CorpusGates.sharedFragments].
     *
     * Nine, and only the longest run of each collision is listed, so one shared clause is
     * one entry rather than one entry per window inside it. Two of the nine are already
     * recorded in `KnownCorpusViolations` as whole shared sentences; the other seven are
     * clauses inside sentences that differ, which the production check cannot see.
     */
    val FRAGMENTS: List<RecordedFragment> = listOf(
        RecordedFragment(
            Purpose.MOMENTUM_HEADLINE,
            "active {} of the last fourteen days",
            setOf("quietStretch", "steadyStretch"),
        ),
        RecordedFragment(
            Purpose.MOMENTUM_HEADLINE,
            "the last two weeks have been",
            setOf("quietStretch", "steadyStretch"),
        ),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "been waiting more than a fortnight",
            setOf("intakeVsOutput", "queuePressure"),
        ),
        RecordedFragment(Purpose.REPORT_OBSERVATION, "on {} of the seven days", setOf("dayShape", "focusInvestment")),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "the queues have grown three weeks running",
            setOf("intakeVsOutput", "queuePressure"),
        ),
        RecordedFragment(
            Purpose.REPORT_OBSERVATION,
            "{} has been active in {}",
            setOf("neglectedArea", "persistentItem"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "each of the last three weeks",
            setOf("decliningActivity", "reportedVsActual", "shiftingFocus"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "three weeks ago you touched {} areas this week {}",
            setOf("broadeningFocus", "narrowingFocus"),
        ),
        RecordedFragment(
            Purpose.REPORT_PATTERN,
            "{} then {} then {} sessions",
            setOf("focusHabitFading", "focusHabitForming"),
        ),
    )
    /** True when this exact collision, between these exact families, is recorded. */
    fun isRecordedFragment(purpose: Purpose, fragment: String, families: Set<FamilyKey>): Boolean =
        FRAGMENTS.any { it.purpose == purpose && it.fragment == fragment && it.families == families }

    /**
     * Families each over used construction is allowed in today, beyond the two shapes the
     * catalog already records for itself.
     *
     * `notXthenY` is `Not X.` standing as a whole sentence, in five families. `xCommaAndY`
     * is `X, and Y.`, in ten. Both are named as constructions by the phase 9 brief, so both
     * stay under the cap of two and an eleventh family reaching for either is a finding.
     */
    val CONSTRUCTIONS: Map<String, Set<FamilyKey>> = mapOf(
        "notXthenY" to setOf(
            "accumulation", "insufficientData", "mostActiveSince", "quietWeek", "weekQuiet"
        ),
        "xCommaAndY" to setOf(
            "concentration", "focusInvestment", "growingQueues", "hardStretch", "intakeVsOutput",
            "persistence", "queueDrain", "reportedVsActual", "singleFocus", "weekStrong"
        ),
    )
    /** The catalog's recorded allowance and this file's, together. */
    fun constructionAllowance(name: String): Set<FamilyKey> =
        CorpusGates.catalogAllowance(name) + CONSTRUCTIONS[name].orEmpty()

    /**
     * Bench id to the number of lines it held when its band spread was recorded as over the
     * cap.
     *
     * **Seventy four of the hundred and eight measurable benches when this was recorded,
     * which was the largest single finding in the corpus.** Read as one number it said the
     * corpus was written almost entirely at one length: most of the seventy four were over
     * the cap on `MEDIUM`, seven to fourteen words. That is not a defect in any one line, it
     * is exactly what a reader means when generated text feels flat, and it is why this gate
     * exists rather than being left to somebody's ear at the end.
     *
     * **Phase 9 took it to twenty eight of a hundred and thirteen**, because an exemption
     * here lapses on the first line added to a bench and every bench phase 9 grew had to
     * meet the cap on its own. The entries below that name a grown bench are therefore dead
     * records rather than live exemptions, and are kept only so a later reader can see what
     * the bench looked like before.
     */
    val LENGTH_BANDS: Map<String, Int> = mapOf(
        "AREAS_BANNER weekBuilding s1" to 8,
        "AREAS_BANNER weekMixed s1" to 8,
        "AREAS_BANNER weekQuiet s1" to 8,
        "AREAS_BANNER weekStarting s1" to 8,
        "AREAS_BANNER weekStrong s1" to 8,
        "MOMENTUM_HEADLINE balancedWeek s1" to 12,
        "MOMENTUM_HEADLINE cleanSlate s1" to 6,
        "MOMENTUM_HEADLINE comeback s1" to 12,
        "MOMENTUM_HEADLINE firstDays s1" to 10,
        "MOMENTUM_HEADLINE strongPace s1" to 12,
        "PULSE accumulation s3" to 11,
        "PULSE concentration s3" to 12,
        "PULSE persistence s1" to 15,
        "PULSE persistence s2" to 18,
        "PULSE persistence s3" to 16,
        "PULSE persistence s4" to 12,
        "PULSE queueDrain s1" to 8,
        "PULSE queueDrain s2" to 7,
        "PULSE quietDay s1" to 12,
        "PULSE rebalance s1" to 8,
        "PULSE spread s1" to 11,
        "PULSE spread s2" to 11,
        "PULSE throughput s3" to 10,
        "REPORT_OBSERVATION areaBalance s1 ext" to 4,
        "REPORT_OBSERVATION areaRevival s1" to 6,
        "REPORT_OBSERVATION areaRevival s1 ext" to 5,
        "REPORT_OBSERVATION completionSplit s1" to 7,
        "REPORT_OBSERVATION dayShape s1" to 7,
        "REPORT_OBSERVATION dayShape s1 ext" to 5,
        "REPORT_OBSERVATION firstMilestone s1" to 6,
        "REPORT_OBSERVATION focusAbandonment s1" to 5,
        "REPORT_OBSERVATION focusAbandonment s1 ext" to 4,
        "REPORT_OBSERVATION focusInvestment s1" to 6,
        "REPORT_OBSERVATION focusInvestment s2" to 7,
        "REPORT_OBSERVATION focusInvestment s3" to 6,
        "REPORT_OBSERVATION hardStretch s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s2" to 16,
        "REPORT_OBSERVATION intakeVsOutput s2 ext" to 6,
        "REPORT_OBSERVATION intakeVsOutput s3 ext" to 5,
        "REPORT_OBSERVATION mostActiveSince s1 ext" to 4,
        "REPORT_OBSERVATION neglectedArea s1 ext" to 5,
        "REPORT_OBSERVATION neglectedArea s2 ext" to 5,
        "REPORT_OBSERVATION persistentItem s1" to 7,
        "REPORT_OBSERVATION persistentItem s1 ext" to 5,
        "REPORT_OBSERVATION personalBest s1" to 8,
        "REPORT_OBSERVATION queueDrained s1" to 5,
        "REPORT_OBSERVATION queuePressure s1" to 6,
        "REPORT_OBSERVATION queuePressure s1 ext" to 5,
        "REPORT_OBSERVATION quietWeek s1" to 11,
        "REPORT_OBSERVATION selfReportVsData s1" to 10,
        "REPORT_OBSERVATION selfReportVsData s1 ext" to 6,
        "REPORT_OBSERVATION singleFocus s1" to 10,
        "REPORT_OBSERVATION singleFocus s1 ext" to 8,
        "REPORT_OBSERVATION singleFocus s2 ext" to 7,
        "REPORT_OBSERVATION steadyPace s1" to 5,
        "REPORT_OBSERVATION steadyPace s1 ext" to 4,
        "REPORT_OBSERVATION switchingBehavior s1" to 9,
        "REPORT_OBSERVATION switchingBehavior s1 ext" to 5,
        "REPORT_OBSERVATION timeOfDay s1" to 6,
        "REPORT_OBSERVATION timeOfDay s1 ext" to 4,
        "REPORT_PATTERN abandonmentPattern s1" to 4,
        "REPORT_PATTERN areaGoneQuiet s1" to 9,
        "REPORT_PATTERN comebackPattern s1" to 5,
        "REPORT_PATTERN consistentRhythm s1" to 8,
        "REPORT_PATTERN decliningActivity s1" to 8,
        "REPORT_PATTERN focusHabitFading s1" to 6,
        "REPORT_PATTERN focusHabitForming s1" to 7,
        "REPORT_PATTERN growingQueues s1" to 10,
        "REPORT_PATTERN insufficientData s1" to 4,
        "REPORT_PATTERN queueEquilibrium s1" to 5,
        "REPORT_PATTERN reportedVsActual s1" to 7,
        "REPORT_PATTERN shiftingFocus s1" to 10,
    )
    /** True while the bench is no larger than it was when the exemption was written. */
    fun bandExemptAt(benchId: String, size: Int): Boolean = size <= (LENGTH_BANDS[benchId] ?: -1)

    /**
     * Bench id to the number of lines it held when its thin or missing register was recorded.
     *
     * Fifty three benches, and their shape is worth stating: thirty of the findings are a
     * single editorial line in a Report observation bench, which means that every time the
     * realizer reaches for the editorial voice in that family it gets the same sentence,
     * until the ninety day exclusion takes it away and it gets a different register instead.
     * The rest are hot benches with no line at all in a register their volume's fallback
     * order will ask for.
     */
    val REGISTERS: Map<String, Int> = mapOf(
        "AREAS_BANNER weekBuilding s1" to 8,
        "AREAS_BANNER weekMixed s1" to 8,
        "AREAS_BANNER weekStarting s1" to 8,
        "AREAS_BANNER weekStrong s1" to 8,
        "MOMENTUM_HEADLINE cleanSlate s1" to 6,
        "MOMENTUM_HEADLINE steadyStretch s1" to 18,
        "REPORT_HEADLINE balanced s1" to 12,
        "REPORT_HEADLINE comeback s1" to 8,
        "REPORT_HEADLINE mostActiveSince s1" to 8,
        "REPORT_HEADLINE personalBest s1" to 10,
        "REPORT_OBSERVATION areaBalance s1" to 6,
        "REPORT_OBSERVATION areaBalance s1 ext" to 4,
        "REPORT_OBSERVATION areaRevival s1" to 6,
        "REPORT_OBSERVATION areaRevival s1 ext" to 5,
        "REPORT_OBSERVATION completionSplit s1 ext" to 6,
        "REPORT_OBSERVATION dayShape s1 ext" to 5,
        "REPORT_OBSERVATION firstMilestone s1" to 6,
        "REPORT_OBSERVATION firstMilestone s1 ext" to 3,
        "REPORT_OBSERVATION focusAbandonment s1" to 5,
        "REPORT_OBSERVATION focusAbandonment s1 ext" to 4,
        "REPORT_OBSERVATION focusInvestment s1" to 6,
        "REPORT_OBSERVATION focusInvestment s1 ext" to 5,
        "REPORT_OBSERVATION focusInvestment s2 ext" to 6,
        "REPORT_OBSERVATION focusInvestment s3 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s1" to 8,
        "REPORT_OBSERVATION intakeVsOutput s1 ext" to 5,
        "REPORT_OBSERVATION intakeVsOutput s2 ext" to 6,
        "REPORT_OBSERVATION intakeVsOutput s3 ext" to 5,
        "REPORT_OBSERVATION mostActiveSince s1" to 5,
        "REPORT_OBSERVATION mostActiveSince s1 ext" to 4,
        "REPORT_OBSERVATION neglectedArea s1" to 7,
        "REPORT_OBSERVATION neglectedArea s1 ext" to 5,
        "REPORT_OBSERVATION persistentItem s1 ext" to 5,
        "REPORT_OBSERVATION personalBest s1" to 8,
        "REPORT_OBSERVATION personalBest s1 ext" to 5,
        "REPORT_OBSERVATION queueDrained s1" to 5,
        "REPORT_OBSERVATION queueDrained s1 ext" to 4,
        "REPORT_OBSERVATION queuePressure s1" to 6,
        "REPORT_OBSERVATION queuePressure s1 ext" to 5,
        "REPORT_OBSERVATION selfReportVsData s1" to 10,
        "REPORT_OBSERVATION selfReportVsData s1 ext" to 6,
        "REPORT_OBSERVATION singleFocus s1 ext" to 8,
        "REPORT_OBSERVATION singleFocus s2 ext" to 7,
        "REPORT_OBSERVATION steadyPace s1" to 5,
        "REPORT_OBSERVATION steadyPace s1 ext" to 4,
        "REPORT_OBSERVATION switchingBehavior s1" to 9,
        "REPORT_OBSERVATION switchingBehavior s1 ext" to 5,
        "REPORT_OBSERVATION timeOfDay s1" to 6,
        "REPORT_OBSERVATION timeOfDay s1 ext" to 4,
        "REPORT_PATTERN comebackPattern s1" to 5,
        "REPORT_PATTERN consistentRhythm s1" to 8,
        "REPORT_PATTERN growingQueues s1" to 10,
        "REPORT_PATTERN reportedVsActual s1" to 7,
    )
    /** True while the bench is no larger than it was when the exemption was written. */
    fun registerExemptAt(benchId: String, size: Int): Boolean = size <= (REGISTERS[benchId] ?: -1)

    /**
     * Pairs of keys that read as one line today. Recorded by key, because neither may be
     * reworded by an author who is only allowed to add.
     *
     * **One of these is not a near duplicate, it is a duplicate.** `ob.neg.s2.l02` and
     * `ob.neg.s2.l09` are the same sentence, character for character, under two keys, so
     * that bench is one line smaller than it counts itself. It is recorded rather than fixed
     * because retiring a key is the owner's call and a key is never reused, and it is called
     * out here so that the call can be made.
     */
    val DUPLICATES: Set<Set<String>> = setOf(
        setOf("accumulation.s1.01", "accumulation.s1.03"),
        setOf("throughput.s1.01", "throughput.s1.03"),
        setOf("hd.single.03", "hd.single.06"),
        setOf("hd.back.01", "hd.back.06"),
        setOf("hd.fall.01", "hd.fall.02"),
        setOf("hd.fall.01", "hd.fall.04"),
        setOf("ob.single.s2.l04", "ob.single.s2.l13"),
        setOf("ob.neg.s2.l02", "ob.neg.s2.l09"),
        setOf("bn.quiet.01", "bn.quiet.04"),
        setOf("bn.quiet.04", "bn.quiet.08"),
    )
    /** True when this pair is recorded. */
    fun isRecordedDuplicate(one: String, other: String): Boolean = setOf(one, other) in DUPLICATES

    /**
     * Lines the engine cannot say today, with the marker that stops each one.
     *
     * **Eighty six lines, and this is the finding worth reading twice.** Eighty of them carry
     * a marker with no entry in `SlotBindings` at all, so they can never render on any day, on
     * any device, and no screen has ever shown the absence. The other six were never filled by
     * any of the twenty four differently shaped moments their stage produced across eleven
     * simulated years.
     *
     * They are recorded rather than fixed because a binding is engine work and phase 9 is
     * authoring, and because a wrong binding is worse than a missing one: it prints a number
     * that is arithmetically correct and untrue. Each entry names the marker, so what is left
     * is a list rather than a rediscovery.
     *
     * The list is keyed by variant and never lapses on size. A line either has a fact behind
     * it or it does not, and growing the bench around it changes nothing.
     */
    val UNRENDERABLE: Map<String, String> = mapOf(
        "persistence.s3.07" to "{n} has no binding",
        "persistence.s3.09" to "{m} reads medianDaysToComplete, which read nothing in any sampled moment",
        "persistence.s4.05" to "{n} has no binding",
        "concentration.s3.02" to "{dayCount} has no binding",
        "concentration.s3.03" to "{dayCount} has no binding",
        "concentration.s3.04" to "{dayCount} has no binding",
        "concentration.s3.06" to "{dayCount} has no binding",
        "concentration.s3.07" to "{dayCount} has no binding",
        "concentration.s3.10" to "{sinceRef} has no binding",
        "concentration.s3.11" to "{otherArea} needs the OTHER_THAN_SUBJECT, which no sampled moment carried",
        "throughput.s3.03" to "{sinceRef} has no binding",
        "quietday.s2.01" to "{dayCount} has no binding",
        "quietday.s2.02" to "{dayCount} has no binding",
        "quietday.s2.04" to "{dayCount} has no binding",
        "quietday.s2.05" to "{sinceRef} has no binding",
        "quietday.s2.08" to "{dayCount} has no binding",
        "quietday.s2.10" to "{itemTitle} has no binding",
        "quietday.s3.01" to "{dayCount} has no binding",
        "quietday.s3.02" to "{sinceRef} has no binding",
        "quietday.s3.03" to "{dayCount} has no binding",
        "quietday.s3.06" to "{dayCount} has no binding",
        "quietday.s3.08" to "{itemTitle} has no binding",
        "quietday.s3.09" to "{sinceRef} has no binding",
        "rebalance.s1.05" to "{sinceRef} has no binding",
        "rebalance.s2.02" to "{sinceRef} has no binding",
        "ob.single.s1.l10" to "{otherArea} has no binding",
        "ob.single.s1.e06" to "{otherArea} has no binding",
        "ob.single.s2.e03" to "{otherArea} has no binding",
        "ob.flow.s1.e05" to "{dayName} has no binding",
        "ob.flow.s2.e01" to "{sinceRef} has no binding",
        "ob.flow.s3.e01" to "{sinceRef} has no binding",
        "ob.focus.s2.l06" to "{n} has no binding",
        "ob.focus.s2.e01" to "{sinceRef} has no binding",
        "ob.focus.s2.e02" to "{n} has no binding",
        "ob.focus.s3.l04" to "{n} has no binding",
        "ob.focus.s3.e01" to "{m} has no binding",
        "ob.srvd.l02" to "{n} has no binding",
        "ob.srvd.l04" to "{priorLabel} needs the CALLBACK_LABEL, which no sampled moment carried",
        "ob.srvd.l07" to "{n} has no binding",
        "ob.srvd.l08" to "{priorLabel} needs the CALLBACK_LABEL, which no sampled moment carried",
        "ob.srvd.l10" to "{priorLabel} needs the CALLBACK_LABEL, which no sampled moment carried",
        "ob.quiet.e04" to "{itemTitle} has no binding",
        "ob.qp.e03" to "{itemTitle} has no binding",
        "ob.qp.e05" to "{sinceRef} has no binding",
        "ob.rev.l03" to "{n} has no binding",
        "ob.rev.l05" to "{sinceRef} has no binding",
        "ob.rev.e03" to "{sinceRef} has no binding",
        "ob.since.e01" to "{areaName} has no binding",
        "ob.since.e04" to "{m} has no binding",
        "ob.tod.l02" to "{n} has no binding",
        "ob.tod.l05" to "{pct} has no binding",
        "ob.swi.l02" to "{areaName} has no binding",
        "ob.swi.l03" to "{areaName} has no binding",
        "ob.swi.l04" to "{areaName} has no binding",
        "ob.swi.l05" to "{areaCount} has no binding",
        "ob.swi.l06" to "{areaName} has no binding",
        "ob.swi.l07" to "{areaName} has no binding",
        "ob.swi.l08" to "{areaName} has no binding",
        "ob.drain.e04" to "{sinceRef} has no binding",
        "ob.first.l02" to "{minutes} has no binding",
        "ob.first.e01" to "{ageDays} has no binding",
        "ob.first.e03" to "{n} has no binding",
        "pt.grow.04" to "{sinceRef} has no binding",
        "pt.grow.06" to "{sinceRef} has no binding",
        "pt.grow.08" to "{sinceRef} has no binding",
        "pt.imp.04" to "{sinceRef} has no binding",
        "pt.imp.07" to "{sinceRef} has no binding",
        "pt.dec.04" to "{sinceRef} has no binding",
        "pt.dec.06" to "{sinceRef} has no binding",
        "pt.rhy.04" to "{sinceRef} has no binding",
        "pt.narrow.06" to "{sinceRef} has no binding",
        "pt.broad.05" to "{areaName} has no binding",
        "pt.hab.01" to "{sinceRef} has no binding",
        "pt.fade.03" to "{sinceRef} has no binding",
        "pt.fade.05" to "{ageDays} has no binding",
        "pt.fade.06" to "{sinceRef} has no binding",
        "pt.rva.01" to "{n} has no binding",
        "pt.rva.02" to "{n} has no binding",
        "pt.rva.07" to "{priorLabel} has no binding",
        "pt.eq.03" to "{sinceRef} has no binding",
        "pt.come.05" to "{ageDays} has no binding",
        "mo.pace.12" to "{sessions} reads focusSessions, which read nothing in any sampled moment",
        // Four more in the four stages that never qualified in any simulated year, so the
        // render walk could not reach them and only the binding table can see them.
        "ob.focus.s1.e01" to "{dayName} has no binding",
        "ob.focus.s1.e02" to "{m} has no binding",
        "pt.wknd.02" to "{sinceRef} has no binding",
        "pt.ab.04" to "{sinceRef} has no binding",
    )

    /** True when this line is one of the ones already known to be unsayable. */
    fun isRecordedUnrenderable(variantKey: String): Boolean = variantKey in UNRENDERABLE
}
