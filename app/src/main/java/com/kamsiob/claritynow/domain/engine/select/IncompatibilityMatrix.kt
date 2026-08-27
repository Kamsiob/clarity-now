package com.kamsiob.claritynow.domain.engine.select

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The pairs of observations that must never appear in one report.
 * CLARITY_LOGIC_ENGINE.md 9.1.
 *
 * **This is the failure class the Pulse cannot have.** A Pulse says one thing. A report
 * says eight to ten at once, and two individually true sentences can contradict each other
 * in the same column: one saying the week was narrow and the next saying it was broad, both
 * correct, the pair incoherent. Section 5 applies this after ranking and before taking the
 * second, third and fourth observations, and the lower ranked family is the one dropped.
 *
 * Three entries in 9.1 are not here, and each is enforced somewhere it belongs better.
 * `intakeVsOutput` stage 2 against stage 3 is a family against itself, which the family
 * exclusion in section 5 already covers. `selfReportVsData` against itself on one subject
 * is a cooldown of `Int.MAX_VALUE` in `EngineFamilies`. `hardStretch` against any plan is
 * layer 6's, which cannot be reached from here.
 */
object IncompatibilityMatrix {

    /** 9.1, as unordered pairs. */
    private val PAIRS: Set<Set<FamilyKey>> = setOf(
        setOf("singleFocus", "areaBalance"),
        setOf("quietWeek", "personalBest"),
        setOf("quietWeek", "focusInvestment"),
        setOf("steadyPace", "personalBest"),
        setOf("steadyPace", "mostActiveSince"),
        setOf("selfReportVsData", "neglectedArea"),
        setOf("hardStretch", "selfReportVsData"),
    )

    /**
     * Pairs that conflict only when they are about the same subject.
     *
     * `neglectedArea` and `areaRevival` describe opposite things and are both true in one
     * week, about two different areas. Excluding the pair outright would lose a real
     * observation to a conflict that is not there.
     */
    private val SAME_SUBJECT_PAIRS: Set<Set<FamilyKey>> = setOf(
        setOf("neglectedArea", "areaRevival"),
    )

    /** The observation families a `quietWeek` headline leaves room for. 9.1. */
    private val QUIET_WEEK_ALLOWS: Set<FamilyKey> =
        setOf("quietWeek", "neglectedArea", "persistentItem", "hardStretch")

    /**
     * The headlines 9.1 means by `a declining headline`.
     *
     * The two that say less happened. `netInflow` and `queuePressure` describe a week that
     * filled up rather than one that slowed down, and reading them as declines would
     * suppress the flagship observation on a busy week.
     */
    private val DECLINING_HEADLINES: Set<FamilyKey> = setOf("decliningActivity", "quietWeek")

    /** True when [a] and [b] may not both appear. */
    fun conflicts(a: Selection, b: Selection): Boolean {
        val pair = setOf(a.rule.family, b.rule.family)
        if (pair.size < 2) return false
        if (pair in PAIRS) return true
        return pair in SAME_SUBJECT_PAIRS && a.subjectId != null && a.subjectId == b.subjectId
    }

    /**
     * True when an observation conflicts with the headline already chosen.
     *
     * **The headline is selected first and constrains everything after it**, per 9.2, and a
     * conflicting observation is excluded entirely rather than deprioritized. The headline
     * is the largest text on the screen and it sets the frame the rest of the report is
     * read inside; an observation that argues with it does not read as nuance, it reads as
     * the app disagreeing with itself.
     */
    fun conflictsWithHeadline(headlineFamily: FamilyKey?, candidate: Selection): Boolean {
        val headline = headlineFamily ?: return false
        val family = candidate.rule.family
        if (headline == "quietWeek" && family !in QUIET_WEEK_ALLOWS) return true
        if (headline == "singleFocus" && family == "areaBalance") return true
        if (headline == "balanced" && family == "singleFocus") return true
        if (headline in DECLINING_HEADLINES && family == "selfReportVsData") return true
        return false
    }
}
