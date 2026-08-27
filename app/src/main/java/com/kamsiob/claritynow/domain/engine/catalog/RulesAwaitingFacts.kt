package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Families and stages that have authored language and no rule, because the fact their
 * trigger names is not declared in CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * **This exists instead of an approximation.** Every entry below could have been given a
 * criterion that is nearly the right shape: `window.activeDays` in place of a run of
 * consecutive days, `window.swaps` in place of a per area swap count,
 * `AreaFacts.daysSinceLastEvent` in place of the dormancy an area returned from. Each of
 * those would fire the family on a shape it does not describe, and the sentence that came
 * out would be arithmetic nobody could fault and a claim about a person's week that was
 * not true. Prime directive: every claim must be true, and provably so from a count query.
 *
 * It is a data structure rather than a comment because a test reads it. [CatalogIntegrity]
 * asserts that every family in the corpus either has a rule or is listed here, so a family
 * cannot go quiet without someone deciding it should.
 */
internal object RulesAwaitingFacts {

    /** One family or stage, the fact it needs, and where the trigger is written down. */
    data class Gap(
        val family: FamilyKey,
        val purpose: Purpose,
        val stage: Int?,
        val missingFact: String,
        val citation: String,
    )

    val GAPS: List<Gap> = listOf(
        Gap(
            family = "switching",
            purpose = Purpose.PULSE,
            stage = null,
            missingFact = "AreaFacts.swapsInWindow, a per area count of active item changes",
            citation = "CLARITY_LOGIC_ENGINE.md 6.1 gives switching the area as its subject and the " +
                "swap count as its escalation fact. 3.1 declares swaps only on WindowFacts, for the " +
                "whole window, and all eighteen of the family's statements name an area",
        ),
        Gap(
            family = "rebalance",
            purpose = Purpose.PULSE,
            stage = null,
            missingFact = "AreaFacts.dormantDaysBeforeReturn, how long the area had been still before it moved",
            citation = "CLARITY_LOGIC_ENGINE.md 6.1 gives rebalance dormancy length as its escalation " +
                "fact and CORPUS_1_PULSE.md splits it at five to thirteen days and fourteen or more. " +
                "RollupFacts.dormantReturnedAreaIds says that an area returned but not from how long, " +
                "and daysSinceLastEvent is zero once it has",
        ),
        Gap(
            family = "concentration",
            purpose = Purpose.PULSE,
            stage = 3,
            missingFact = "a count of consecutive days on which one area held the window's events",
            citation = "CORPUS_1_PULSE.md stage 3 reads `ninety five percent and above, or four or more " +
                "consecutive days`. The share branch has a rule; the days branch has no fact. " +
                "window.activeDays counts days with any activity at all, which is a different shape",
        ),
        Gap(
            family = "quietDay",
            purpose = Purpose.PULSE,
            stage = 2,
            missingFact = "consecutiveQuietDays",
            citation = "CLARITY_LOGIC_ENGINE.md 6.1 names consecutive quiet days as quietDay's " +
                "escalation fact and CORPUS_1_PULSE.md splits stages 2 and 3 at two to three and " +
                "four or more. 3.1 declares no such count",
        ),
        Gap(
            family = "quietDay",
            purpose = Purpose.PULSE,
            stage = 3,
            missingFact = "consecutiveQuietDays",
            citation = "the same fact stage 2 needs. CORPUS_1_PULSE.md stage 3 reads `four or more " +
                "consecutive quiet days`, and window.activeDays counts days with activity inside " +
                "one window rather than a run of days without any",
        ),
        Gap(
            family = "narrowingFocus",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "HistoryFacts.weekAreaCountSeries, the number of areas that moved in each week",
            citation = "CORPUS_2_REPORT.md 3.7 claims fewer areas have moved each week and names the " +
                "count three weeks ago against this week. HistoryFacts carries completions, queue size " +
                "and total events per week, and no area count",
        ),
        Gap(
            family = "broadeningFocus",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "HistoryFacts.weekAreaCountSeries",
            citation = "as narrowingFocus, in the other direction. CORPUS_2_REPORT.md 3.8",
        ),
        Gap(
            family = "focusHabitForming",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "HistoryFacts.weekFocusSessionSeries, finished focus sessions per week",
            citation = "CORPUS_2_REPORT.md 3.9 claims sessions appeared every week for a month and " +
                "renders three weekly counts. HistoryFacts carries personalBestFocusMinutesWeek and " +
                "no series",
        ),
        Gap(
            family = "focusHabitFading",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "HistoryFacts.weekFocusSessionSeries",
            citation = "as focusHabitForming, in the other direction. CORPUS_2_REPORT.md 3.10",
        ),
        Gap(
            family = "weekendShift",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "a weekday distribution outside CueFacts, or permission to read CueFacts here",
            citation = "CORPUS_2_REPORT.md 3.13 claims nothing has happened on a weekend in four weeks. " +
                "The only weekday facts in 3.1 are in CueFacts, and 3.7 restricts CueFacts to layer 6",
        ),
        Gap(
            family = "abandonmentPattern",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "HistoryFacts.weekFocusStartedSeries and weekFocusCompletedSeries",
            citation = "CORPUS_2_REPORT.md 3.14 claims more sessions ended early than finished three " +
                "weeks running. WindowFacts carries this week's counts and nothing earlier",
        ),
        Gap(
            family = "comebackPattern",
            purpose = Purpose.REPORT_PATTERN,
            stage = null,
            missingFact = "a per area weekly activity series, so a second return can be counted",
            citation = "CORPUS_2_REPORT.md 3.15 claims an area has gone quiet and returned twice. " +
                "RollupFacts.dormantReturnedAreaIds describes this window only",
        ),
    )

    /** Families with no rule at all, as opposed to a single stage that has none. */
    val FAMILIES_WITHOUT_RULES: Set<FamilyKey> = GAPS.filter { it.stage == null }.map { it.family }.toSet()
}
