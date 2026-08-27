package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The places where the corpus as it stands today breaks a rule the catalog checks, listed
 * so the check can stay on.
 *
 * **Why this is a list and not a disabled test.** 7.7 requires that no fragment appear in
 * two families and that no rhetorical construction appear in more than two. The corpus was
 * authored before either check existed and it breaks both, in six places and two shapes.
 * There are three things one can do with that: delete the checks, ignore them until phase
 * 9 grows the corpus, or write down exactly what is wrong and fail on anything new. Only
 * the third leaves the build honest in the meantime, and it hands phase 9 a list rather
 * than a rediscovery.
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
        SharedFragment(
            Purpose.MOMENTUM_HEADLINE,
            "active {} of the last fourteen days",
            setOf("quietStretch", "steadyStretch"),
            "mo.steady.01 and mo.quiet.01. The same sentence carries the whole difference in " +
                "its number, which is defensible and is still one string in two families",
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
     * Three of these disagree today. `CORPUS_3_MOMENTUM.md` claims 112 Momentum headlines
     * and carries 96, so its stated total of 162 is really 146. `CORPUS_2_REPORT.md` says
     * `Headline totals: 176 lines` in section 1 and 158 in its own totals table, and 158 is
     * the true count. Its pattern section says 128 and carries 111. Following those through,
     * CLARITY_LOGIC_ENGINE.md 11.1's combined figure of 1,519 authored lines is 1,503.
     */
    data class ClaimedTotal(val file: String, val claimed: Int, val actual: Int, val note: String)

    val TOTALS: List<ClaimedTotal> = listOf(
        ClaimedTotal("CORPUS_1_PULSE.md", claimed = 620, actual = 620, note = "agrees"),
        ClaimedTotal(
            "CORPUS_2_REPORT.md",
            claimed = 737,
            actual = 737,
            note = "the totals table agrees. Section 1's prose says 176 headlines against the " +
                "table's 158, and section 3's says 128 patterns against 111",
        ),
        ClaimedTotal(
            "CORPUS_3_MOMENTUM.md",
            claimed = 162,
            actual = 146,
            note = "the totals table claims 112 Momentum headlines and the file carries 96",
        ),
    )
}
