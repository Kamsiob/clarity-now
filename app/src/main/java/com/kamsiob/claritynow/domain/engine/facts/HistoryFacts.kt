package com.kamsiob.claritynow.domain.engine

/**
 * Everything the window is compared against. CLARITY_LOGIC_ENGINE.md 3.1.
 *
 * ## The weekly series, and why the buckets are not calendar weeks
 *
 * Every series here is built from **seven day buckets anchored at the window end**,
 * oldest first, so the newest bucket is the seven local days ending with the window.
 * They are not Sunday aligned calendar weeks, and the reason is a false sentence that
 * calendar weeks would produce.
 *
 * The Report window is "the trailing 7 days ending today", per MASTER_BUILD_PROMPT
 * 11.3. A report generated on a Wednesday against Sunday aligned buckets would
 * compare the seven days it is about against a four day fragment of the current
 * calendar week, and `mostActiveSince` would fire on a comparison the person could
 * not reproduce from the numbers printed beside it. Anchoring the buckets at the
 * window end makes the newest bucket **exactly the window** whenever the window is
 * seven local days ending on a day boundary, which is the shape the Report uses, so
 * the number compared is the number shown. For a one day Pulse window the newest
 * bucket is instead the seven days ending with it, which is what a fact named for a
 * week should mean whatever length of window asked for it.
 *
 * The keys are still `yyyy-MM-dd` and still name the first local day of their
 * bucket, which is all `DateRef` needs: 7.2 renders a week key as a month name,
 * `since March`, and never as a numeric date.
 *
 * ## Which buckets a superlative is measured over
 *
 * [personalBestWeekCompletions], [personalBestWeekKey], [weeksSincePersonalBest],
 * [mostRecentBetterWeekKey] and [personalBestFocusMinutesWeek] are all computed over
 * the buckets **strictly before** the newest one, back to install rather than only
 * over the twelve carried in the series.
 *
 * Excluding the newest bucket is what makes the two families expressible at all.
 * `personalBest` fires when this week beats every previous week, which is
 * `window.completions > personalBestWeekCompletions`; if the current week were
 * included, the best would always be at least this week and the comparison could
 * never be written. `mostActiveSince` fires when [mostRecentBetterWeekKey] is not
 * null, and `personalBest` when it is null, which is exactly the split 3.1 describes.
 *
 * ## No streak facts
 *
 * There is no `currentStreak`, no `longestStreak` and no `daysInARow` here, and
 * there must never be. Their absence is what makes streak language structurally
 * impossible rather than merely discouraged. [weekTotalEventsSeries] and
 * `WindowFacts.activeDays` between them make a streak a few lines of work, and those
 * lines must not be written here or anywhere downstream.
 */
data class HistoryFacts(
    /** Whole local days from the oldest event in the log to the window end. 0 on an empty log. */
    val daysSinceInstall: Int,
    /** Whole seven day periods since install. `daysSinceInstall / 7`. */
    val weeksOfData: Int,
    /** `daysSinceInstall < 7`. */
    val isFirstWeekEver: Boolean,
    val lifetimeCompletions: Int,
    /** The bucket before the newest one, or null when there is no earlier bucket. */
    val lastWeekCompletions: Int?,
    /** Oldest first, up to 12 buckets, newest last. */
    val weekCompletionsSeries: List<Int>,
    /** Total queue length at the end of each bucket. Oldest first, up to 12. */
    val weekQueueSizeSeries: List<Int>,
    /** Oldest first, up to 12. */
    val weekTotalEventsSeries: List<Int>,
    /** Newest bucket minus the one before it, or null when there is no earlier bucket. */
    val weekOverWeekDelta: Int?,
    val completionsTrend: Trend,
    val queueSizeTrend: Trend,
    val activityTrend: Trend,
    /**
     * The dominant area of each of the three most recent buckets, oldest first.
     *
     * Null entries are allowed and mean the same thing they mean in
     * `RollupFacts.dominantAreaId`: a tie, or no events at all. An area that is
     * archived or tombstoned at the window end is never named here, so a pattern
     * about shifting focus cannot point at something the person can no longer see.
     */
    val dominantAreaLastThreeWeeks: List<AreaId?>,
    /** The best earlier bucket's completions. **0 when there is no earlier bucket.** */
    val personalBestWeekCompletions: Int,
    /**
     * The key of the best earlier bucket, or null when there is none.
     *
     * Ties resolve to the **most recent** bucket holding the record, so
     * [weeksSincePersonalBest] reads as how long it has been since that number was
     * last reached rather than since it was first reached.
     */
    val personalBestWeekKey: String?,
    /** Buckets from [personalBestWeekKey] to the newest bucket, or null when there is none. */
    val weeksSincePersonalBest: Int?,
    /**
     * The newest earlier bucket **strictly exceeding** the newest bucket's
     * completions, or null when no earlier bucket beats it.
     *
     * **Strictly greater, never greater or equal.** 3.1 and 13 both single this out:
     * `your most active week since March` is subtly false if March merely equaled
     * this week, and a subtly false claim is worse than an obviously false one
     * because the person has no reason to check it. When this is null nothing may
     * say `since`, and the personal best family applies instead.
     */
    val mostRecentBetterWeekKey: String?,
    /**
     * The longest any item has ever been active, in whole local days. 0 when none.
     *
     * Read across both the items active now and every completed item's own
     * `activeDurationDays` payload value. Stage 4 of the persistence family quotes
     * this as a record and its rule carries a criterion asserting the item in hand
     * genuinely holds it, without which the sentence becomes a lie the moment a
     * longer running item exists.
     */
    val longestEverActiveDays: Int,
    /** The item holding [longestEverActiveDays], or null. Never a tombstoned item. */
    val longestEverActiveItemId: ItemId?,
    /** The best earlier bucket's focus minutes. 0 when there is no earlier bucket. */
    val personalBestFocusMinutesWeek: Int,
    /**
     * The firsts that happened **inside this window**, and nothing else.
     *
     * A flag is present only in the window where the first occurrence happened, so
     * the same first cannot be celebrated in two consecutive reports. A first that
     * happened before the window is absent, and so is one that has not happened.
     */
    val firstEverFlags: Set<FirstEver>,
)
