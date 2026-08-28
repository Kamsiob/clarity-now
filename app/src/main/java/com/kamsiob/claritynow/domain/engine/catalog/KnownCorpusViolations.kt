package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The places where the corpus as it stands today breaks a rule the catalog checks, listed
 * so the check can stay on.
 *
 * **Why this is a list and not a disabled test.** 7.7 requires that no fragment appear in
 * two families and that no rhetorical construction appear in more than two. The corpus was
 * authored before either check existed and it breaks both, in five places and two shapes.
 * There are three things one can do with that: delete the checks, ignore them until phase
 * 9 grows the corpus, or write down exactly what is wrong and fail on anything new. Only
 * the third leaves the build honest in the meantime, and it hands phase 9 a list rather
 * than a rediscovery.
 *
 * **The sixth entry left in the pass that fixed `mo.steady`.** `Active {dayCount} of the
 * last fourteen days.` stood under two keys, `mo.steady.01` and `mo.quiet.01`, and was
 * recorded here as one string in two families. `{dayCount}` renders 9 to 14 in
 * `steadyStretch` and Momentum renders ten and above as digits, so that line put a
 * numeral and the word fourteen side by side for the same unit on five of the six values
 * the slot can take. The nine `mo.steady` lines that did it now write `14`, `mo.quiet`
 * still writes `fourteen` because its own slot never reaches ten, and the two sentences
 * are no longer one sentence.
 *
 * **Nothing is added here without a corpus edit being the alternative.** An entry is a
 * debt with a name on it. Phase 9 authors in batches of forty, one family and stage at a
 * time, and each batch that touches a family below should leave this list shorter.
 */
internal object KnownCorpusViolations {

    /** One clause that two families share today, with the families that share it. */
    data class SharedFragment(
        val purpose: Purpose,
        val fragment: String,
        val families: Set<FamilyKey>,
        val note: String,
    )

    val SHARED_FRAGMENTS: List<SharedFragment> = listOf(
        SharedFragment(
            Purpose.REPORT_OBSERVATION,
            "the queues have grown three weeks running",
            setOf("intakeVsOutput", "queuePressure"),
            "ob.flow.s2.e02 and ob.qp.e02 are the same sentence. Both families can appear in " +
                "one report, so a reader could see it twice in one screen",
        ),
        SharedFragment(
            Purpose.REPORT_PATTERN,
            "{}, then {}, then {}",
            setOf("decliningActivity", "improvingThroughput", "shiftingFocus"),
            "pt.dec.02, pt.imp.02 and pt.shift.03. At most one pattern appears per report, so " +
                "the collision is across weeks rather than within a screen",
        ),
        SharedFragment(
            Purpose.REPORT_PATTERN,
            "three weeks ago you touched {} areas",
            setOf("broadeningFocus", "narrowingFocus"),
            "pt.narrow.05 and pt.broad.02 are the same sentence pointed in opposite directions",
        ),
        SharedFragment(
            Purpose.REPORT_PATTERN,
            "this week, {}",
            setOf("broadeningFocus", "narrowingFocus"),
            "the second half of the same two lines",
        ),
        SharedFragment(
            Purpose.REPORT_PATTERN,
            "{}, then {}, then {} sessions",
            setOf("focusHabitFading", "focusHabitForming"),
            "pt.fade.02 and pt.hab.02",
        ),
    )

    /**
     * The families each over used construction is currently allowed in.
     *
     * `tripleThen` is `A, then B, then C`, the three part list that 7.5 calls a rhetorical
     * reflex: once a reader sees it they cannot stop seeing it. It is in seven families.
     * `xCommaNotY` is `Up, not down.`, the headline form, in four.
     */
    val CONSTRUCTION_ALLOWANCE: Map<String, Set<FamilyKey>> = mapOf(
        "tripleThen" to setOf(
            "dayShape", "decliningActivity", "focusHabitFading", "focusHabitForming",
            "growingQueues", "improvingThroughput", "shiftingFocus",
        ),
        "xCommaNotY" to setOf("balanced", "fragmented", "netInflow", "netOutflow"),
    )

    /** True when this exact collision is one of the recorded ones. */
    fun isKnownSharedFragment(purpose: Purpose, fragment: String, families: Set<FamilyKey>): Boolean =
        SHARED_FRAGMENTS.any { it.purpose == purpose && it.fragment == fragment && it.families == families }

    /**
     * Counted rather than estimated, and set against what each corpus file claims about
     * itself, so a totals table that has drifted from the lines beneath it is visible.
     *
     * **None of them disagrees today.** All three drifted once, all three were corrected,
     * and every stated figure in all three files is now recounted against the keyed lines
     * beneath it by `CorpusTotalsAuditTest` on every run. What is recorded here is the
     * counted total per volume, so a phase that grows a corpus has one place to state the
     * new number and one test that fails if it forgets.
     */
    data class ClaimedTotal(val file: String, val claimed: Int, val actual: Int, val note: String)

    val TOTALS: List<ClaimedTotal> = listOf(
        ClaimedTotal(
            "CORPUS_1_PULSE.md",
            claimed = 1775,
            actual = 1775,
            note = "the totals table agrees, and so does every per family surface count above " +
                "it. Phase 9 has grown all five of volume 1's hot families to the band in " +
                "11.1: persistence, quietDay, concentration, accumulation and rebalance. " +
                "rebalance was the last hot bench in the whole corpus to be grown and the " +
                "shallowest, sixteen statements against 101 firings a year, and it moved this " +
                "volume from 1,607 lines to 1,775. Nothing in volume 1 is owed a figure now",
        ),
        ClaimedTotal(
            "CORPUS_2_REPORT.md",
            claimed = 2200,
            actual = 2200,
            note = "the totals table agrees, and every prose total in the file agrees with it. " +
                "Phase 9 grew this volume's nineteen hot benches to 11.1's hot band in two " +
                "passes, from 804 lines to 1,617 and then to 2,200, and each pass moved the " +
                "same figure in six places in the file and in 11.1 of the engine document. " +
                "The two figures this entry used to record as stale, 176 headlines in " +
                "section 1's prose and 128 patterns in section 3's, were corrected by the " +
                "facts phase. A third kind of wrong figure appeared between the two growth " +
                "passes and is worth the sentence: 11.1 and CORPUS_3_MOMENTUM.md both stated " +
                "1,874 Report lines while the file carried 1,617, which is a projected total " +
                "rather than a stale one and failed the audit identically. Every stated " +
                "total here is recounted by CorpusTotalsAuditTest on every run",
        ),
        ClaimedTotal(
            "CORPUS_3_MOMENTUM.md",
            claimed = 810,
            actual = 810,
            note = "the totals table agrees, and so does every prose total in the file. " +
                "Every family in this volume clearing forty firings a year now sits inside " +
                "11.1's hot band, and weekQuiet was the last of them. It was left at eight " +
                "lines because it had never once spoken, which was a register rule to amend " +
                "rather than a bench to deepen; 7.4 now marks it unflattering, it takes 240 " +
                "banner windows a year, and the batch that followed took it from eight lines " +
                "to sixty. All sixty are neutral agent, because 7.4 step 1 is a tier of one " +
                "and no line in that bench carries a slot, so nothing else could ever be " +
                "said there. strongPace is the one bench that does not clear forty. The " +
                "figure this entry used to record as stale, 112 Momentum headlines against " +
                "96 authored, was corrected by the facts phase and is recounted by " +
                "CorpusTotalsAuditTest on every run",
        ),
    )
}
