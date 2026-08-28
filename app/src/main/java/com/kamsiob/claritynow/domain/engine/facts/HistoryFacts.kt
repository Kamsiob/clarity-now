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
 * ## No streak facts, and the one scoped exception
 *
 * There is no `currentStreak`, no `longestStreak` and no `daysInARow` here, and
 * there must never be. Their absence is what makes streak language structurally
 * impossible rather than merely discouraged. [weekTotalEventsSeries] and
 * `WindowFacts.activeDays` between them make a streak a few lines of work, and those
 * lines must not be written here or anywhere downstream.
 *
 * [currentQuietRunDays] and [currentSingleAreaRunDays] are the exception, approved by
 * the owner, and the reasoning is theirs. **A run counted here is a run of absence.**
 * There is nothing to accumulate and nothing to break, so the loss aversion the ban
 * exists to prevent cannot occur: nobody protects a quiet week, and nobody can be
 * told they lost one. Meanwhile the ban was blocking `quietDay` stages 2 and 3, which
 * left the app able to observe one quiet day and never a quiet week, backwards for
 * this audience and more so since Addendum 01.
 *
 * **The exception is scoped by the shape of the two fields, not by an instruction.**
 * Both are the current run only, ending with the last day the window describes. There
 * is no longest run, no best run and no past run. Both are capped at
 * [MAX_RUN_DAYS], so a value at the cap says **at least** that many and nothing
 * more, and no rule may treat it as an exact count. No per day series backs either of
 * them and none is exposed, here or on the facade.
 *
 * **The guarantee is the shape of the fact, not the test that watches it.** A single
 * capped integer with no per day storage beside it cannot be inverted into a series,
 * because the information is not in the data model to invert. That is the claim, and
 * it is deliberately the weaker of the two available, because the stronger one would
 * be false. Nobody has proved these numbers cannot be reconstructed: anything that
 * reads layer one once a day for a month and differences the results recovers a
 * per day series from any fact here, `WindowFacts.totalEvents` included, and that has
 * been true since phase 1. `StreakExceptionAudit` and the tests beside it are a
 * **tripwire against a per day series being added next to these two fields later**,
 * not a proof of non derivability. The sentence that is true, and is the stronger
 * claim anyway, is that the data model does not contain the information and the build
 * fails if that changes.
 *
 * Nothing else may be added under this reasoning. A run of days somebody **did**
 * something is the thing the ban is about, whatever it is called.
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
    /**
     * The first local day of each weekly bucket, oldest first, aligned entry for entry
     * with every other weekly series here and with `AreaFacts.weekEventsSeries`.
     *
     * **The series were always dated and nothing carried the dates.** Every bucket is
     * seven days anchored at the window end, and [personalBestWeekKey] and
     * [mostRecentBetterWeekKey] are already one of these keys, so the arithmetic that
     * turns an offset into a date existed in the extractor and nowhere else. A corpus
     * line saying `since {sinceRef}` about the oldest week of a three week run needed
     * that date, and the alternative was to recompute `endDate - 7k - 6` inside a
     * measure. A second copy of a bucketing rule is a second copy that can disagree
     * with the first, silently, in a month name printed beside a claim about a trend.
     *
     * **It is not a per day series and cannot become one.** Seven days collapse into
     * one entry exactly as they do in every other series, which is the property
     * `StreakExceptionAudit.NO_PER_DAY_SERIES` is about: a date every seven days
     * resolves no day inside a bucket, so nothing here brings a run of days within
     * reach of a rule.
     */
    val weekStartKeySeries: List<String>,
    /** Oldest first, up to 12 buckets, newest last. */
    val weekCompletionsSeries: List<Int>,
    /** Total queue length at the end of each bucket. Oldest first, up to 12. */
    val weekQueueSizeSeries: List<Int>,
    /** Oldest first, up to 12. */
    val weekTotalEventsSeries: List<Int>,
    /**
     * How many live areas had at least one event in each bucket. Oldest first, up to 12.
     *
     * `narrowingFocus` and `broadeningFocus` both claim the count has moved every
     * week and both render it: "Three weeks ago you touched {n} areas. This week,
     * {m}". Nothing else here counts areas per week, which is why neither family had
     * a rule.
     *
     * **Only areas live at the window end are counted, in every bucket.** An area
     * archived or deleted since is absent from `FactSet.areas` and unnameable, so
     * counting it three weeks ago would print a number the person cannot reconcile
     * with anything the app is willing to show them. The consequence is stated rather
     * than hidden: this is how many of the areas they still have moved in that week.
     * The newest entry therefore equals `RollupFacts.areasWithEvents` whenever the
     * window is the bucket, which is the shape the Report uses, so the number
     * compared is the number shown.
     */
    val weekAreaCountSeries: List<Int>,
    /**
     * Focus sessions that **started** in each bucket. Oldest first, up to 12.
     *
     * `focusHabitForming` and `focusHabitFading` are about sessions appearing and
     * falling away, and this is the count those sentences name. `pt.fade.01` reads
     * "Focus sessions have fallen every week for three weeks", which is false on a
     * person who started five every week and finished fewer each time: what fell
     * there is finishing, and `abandonmentPattern` is the family that says so.
     *
     * The register in `RulesAwaitingFacts` calls this quantity
     * `weekFocusSessionSeries` and describes it as finished sessions per week. It is
     * deliberately not declared under that name: the two families the register cites
     * it for read the started count, and one field serving two different claims is
     * how a subtly false sentence gets written. Started and finished are two
     * quantities with two names, here and in `WindowFacts`.
     */
    val weekFocusStartedSeries: List<Int>,
    /**
     * Of the sessions that started in each bucket, those that reached FOCUS_COMPLETED.
     *
     * Oldest first, up to 12, and attributed to the bucket the session **started**
     * in, exactly as `WindowFacts.focusCompleted` is, so the two series are
     * comparable entry by entry.
     */
    val weekFocusCompletedSeries: List<Int>,
    /**
     * Of the sessions that started in each bucket, those the person ended before the
     * timer ran out. Oldest first, up to 12.
     *
     * `abandonmentPattern` claims "More focus sessions have ended early than
     * finished, three weeks running", which compares this against
     * [weekFocusCompletedSeries] and not against [weekFocusStartedSeries]. **Never
     * infer this by subtracting completed from started.** A killed process leaves a
     * session with no terminal event, which is a legal state and in neither count,
     * and the difference would attribute it to an ending the person did not choose.
     */
    val weekFocusEndedEarlySeries: List<Int>,
    /**
     * User activity events falling on a Saturday or a Sunday in each bucket. Oldest
     * first, up to 12.
     *
     * `weekendShift` claims "Nothing has happened on a weekend in four weeks", which
     * needs a zero rather than a small share, and it prints "{pct} of your activity
     * has been on weekdays for a month", which is this against
     * [weekTotalEventsSeries].
     *
     * **The buckets make this exact.** A bucket is seven consecutive local days, so
     * it holds exactly one Saturday and one Sunday however it is aligned, and four
     * buckets at zero are four weekends with nothing in them. Sunday aligned calendar
     * weeks would have given the same guarantee and would have broken the comparison
     * every other series here depends on.
     *
     * **This is the reason a report pattern does not read `CueFacts`.**
     * CLARITY_LOGIC_ENGINE.md 3.7 restricts those facts to layer 6, and its
     * `weekdayOnly` is a twelve week weekend share under a ceiling: it cannot
     * substantiate a claim of nothing, and it answers over twelve weeks a family that
     * speaks about four.
     */
    val weekWeekendEventsSeries: List<Int>,
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
     * Buckets from [mostRecentBetterWeekKey] to the newest bucket, or null when there
     * is none.
     *
     * The exact companion [weeksSincePersonalBest] is to [personalBestWeekKey], and it
     * exists for the same reason: the `mostActiveSince` bench says both *No week since
     * {sinceRef} finished more* and *It has been {n} weeks*, and the second is the
     * first read as a length rather than as a date. Without it `{n}` had no fact to
     * come from, and it was bound to the family's event count instead, which rendered
     * *It has been 47 weeks* about a week five weeks ago.
     */
    val weeksSinceBetterWeek: Int?,

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
    /**
     * Local days with no user activity at all, counted back from the last day the
     * window describes, stopping at the first day that had some. Capped at
     * [MAX_RUN_DAYS].
     *
     * 0 when that last day had activity, and 0 on an empty log: an app nobody has
     * ever used has not gone quiet. The walk stops at the oldest event in the log, so
     * the days before somebody installed the app are not counted against them.
     *
     * **A quiet day here is a day with nothing, not a day under the family's
     * trigger.** `quietDay` fires on fewer than two events in the window, and a run
     * built from that reading would let one event a day for three days produce
     * "Nothing has moved in three days", which the person knows is false. Every
     * statement authored at stages 2 and 3 claims nothing moved, so the run counts
     * days where nothing did. A day holding one event ends the run and leaves the
     * family at stage 1, whose language says exactly that.
     *
     * **At the cap this means at least [MAX_RUN_DAYS], never exactly that many.** No
     * rule may render it as a count or compare it for equality; the stage ranges the
     * corpus splits at are all far below the cap, which is what the cap is for.
     *
     * Read the class note for why a run of absence is the one exception to the streak
     * ban and what may not be added beside it.
     */
    val currentQuietRunDays: Int,
    /**
     * Local days on which one area held everything, counted back from the last day
     * the window describes. Capped at [MAX_RUN_DAYS].
     *
     * A day counts when it had at least one user activity event and **every one of
     * them belonged to the same area**, and the run continues only while that area
     * stays the same. A day with nothing ends the run rather than extending it, and
     * so does a day holding an event that belonged to no area at all, which is what
     * an unfiled capture is: `concentration.s3.01` reads "Everything yesterday was
     * {areaName}", and that is not true of a day something happened outside every
     * area.
     *
     * 0 when the last day the window describes does not qualify. This is the days
     * branch of `concentration` stage 3, "ninety five percent and above, or four or
     * more consecutive days". `WindowFacts.activeDays` counts days with any activity
     * at all inside one window, which is a different shape.
     *
     * **At the cap this means at least [MAX_RUN_DAYS] and no rule may render it as a
     * count.** See the class note for the scope of the exception this sits under.
     */
    val currentSingleAreaRunDays: Int,
    /**
     * The area that held every one of those days, or null when there is no run.
     *
     * Every stage 3 statement of `concentration` names the area: "{areaName} has held
     * everything for {dayCount} running". [currentSingleAreaRunDays] on its own is a
     * length with no subject, and a rule pairing it with whichever area happens to
     * lead the window would eventually name an area that led the window without
     * holding the run. So the run carries its own subject and a rule may claim it
     * only for this area.
     *
     * **Null when the area is not live at the window end**, even though the run
     * itself is real. An archived or tombstoned area is absent from `FactSet.areas`
     * and unnameable, per prohibition 3, so a rule requiring this id cannot fire on
     * one. [currentSingleAreaRunDays] is left as it is in that case rather than
     * zeroed, because the days did happen; what is missing is permission to say whose
     * they were.
     */
    val currentSingleAreaRunAreaId: AreaId?,
    /**
     * How many completed items in the calibration window had a prediction behind
     * them. MASTER_BUILD_PROMPT 14b.8.
     *
     * **This is the floor and it travels.** 14b.8 sets it at five and requires the
     * count itself to reach the validator as a `FactRef` so that the number gating
     * the sentence is re-read rather than trusted, per 11.4. Under
     * [EstimateTendency.MIN_COMPLETIONS] the tendency is
     * [EstimateTendency.INSUFFICIENT] and [activeToEstimateRatio] is null, so a
     * family reading either one cannot fire on four items whatever its criteria say.
     *
     * An item counts when it was completed inside the window, was active when it was
     * completed, and carried an estimate at the moment it became active. See
     * `TrailQueries.estimateOutcomes` for each of those three and why they are the
     * three.
     */
    val estimatedCompletions: Int,
    /**
     * How many times its own estimate an estimated thing typically spends active, as
     * a multiple. Null under the floor. MASTER_BUILD_PROMPT 14b.8, Addendum 01 7a.
     *
     * ## What it is, and the two things it is not
     *
     * The **median** of the per item ratios, never the mean and never the ratio of
     * two totals. One item left active over a holiday moves a mean and a total ratio
     * to somewhere no week of the person's life resembles, and "tend to" is a median
     * word. `ItemFacts.medianDaysToComplete` takes the same view of the same kind of
     * question.
     *
     * It is **a multiple and never a percentage.** A ratio of 2.4 rendered as 240
     * percent is one literal hundred away from `You were off by 140 percent`, which is
     * the second line 14b.8 forbids by name. A multiple has no such neighbor, and the
     * corpus family is authored against a count rather than a percent slot.
     *
     * It is **a stay and not an effort.** The actual behind it is elapsed time from
     * the promotion that made an item active to its completion, which is the actual
     * 14b.3 says "comes free" and the only one the log holds. Nothing in this app
     * measures time spent working. A family reading this must say what it measures: a
     * thing estimated at an hour that sits on somebody's plate for a day and a half is
     * a true and useful reading of how their estimates map onto their days, and
     * "took a day and a half" is a different claim that this number does not support.
     *
     * ## Why no delta can be built out of it
     *
     * 14b.8 bans a rendered delta between an estimate and an actual. The ban is kept
     * here by arithmetic rather than by inspection: **no quantity of minutes exists
     * anywhere in the fact set**, estimated or actual, so `actual - estimate` is not a
     * subtraction any rule, measure or template is able to write. `TrailQueries`
     * divides the two magnitudes inside its own body and returns only the quotient,
     * and this is the median of those quotients. `CompletedItem.daysActive` is the
     * closest thing to an actual on the fact set and it is whole days for one item,
     * with no estimate anywhere to set it against.
     *
     * Read over [EstimateTendency.WINDOW_WEEKS] weeks ending with the fact
     * window, on the same seven day buckets every series here uses. Twelve weeks is
     * the span this app already calls a pattern rather than an accident, per
     * `CueFacts`. A one week reading of five items is an accident, and a lifetime
     * reading would average somebody's calibration two years ago into their
     * calibration now, which is the thing that is supposed to be able to change. The
     * consequence for the corpus is a constraint rather than a freedom: a family
     * reading this may not say "this week".
     */
    val activeToEstimateRatio: Double?,
    /**
     * Which way [activeToEstimateRatio] runs, or [EstimateTendency.INSUFFICIENT]
     * under the floor.
     *
     * The band is drawn where the rendering is, not at a number somebody picked: a
     * median that would print as `about one` is a person whose estimates land, and
     * [EstimateTendency.CLOSE] says so with nothing to render. That is what keeps a
     * family from announcing a tendency and then printing a one.
     */
    val estimateTendency: EstimateTendency,
    /**
     * Whether the person's activity as a whole has been as low as it is now, for as
     * long, before. MASTER_BUILD_PROMPT 14b.9.
     *
     * Read over weekly user activity events, back to the first week the log holds
     * any, on the definition in [Precedent]. This is the reading for the families
     * 14b.9 names that have no subject: `decliningActivity` as a headline and as a
     * pattern, `quietWeek`, and `hardStretch`, all of which describe the person's
     * weeks rather than one area's.
     */
    val activityDipPrecedent: Precedent,
    /**
     * Whether focus has fallen away like this before, for as long, in this person's
     * history. MASTER_BUILD_PROMPT 14b.9.
     *
     * Read over focus sessions **started** per week, which is the quantity
     * [weekFocusStartedSeries] carries and the one `focusHabitFading` speaks about. A
     * habit that comes and goes with a condition is the same shape as a habit that is
     * fading, and only one of them is worth a sentence.
     */
    val focusDipPrecedent: Precedent,
    /**
     * Whether the last day this window describes falls inside the week after a return
     * from a long absence. MASTER_BUILD_PROMPT 14b.4.
     *
     * ## What it is for
     *
     * 14b.4: "The Report suppresses every decline, neglect and gap observation for a
     * full week back. For seven days from the re-entry date every rule in those
     * families is unavailable to selection and the next ranked candidate is taken
     * instead. The same suppression applies to the Momentum headline and the Areas
     * banner, which read from the same catalog." All three surfaces extract their own
     * facts through layer one, so all three get this from the same place and cannot
     * disagree about the day the withholding ends. The Pulse's own two day window is
     * older and sits above layer one in `PulseGeneration`, because the Pulse decides
     * not to run the engine at all rather than to withhold some of its families.
     *
     * ## What it deliberately is not
     *
     * **It carries no date and no number, and there is nothing here to render.**
     * 14b.4 forbids a returning person being greeted by a measurement of their
     * absence "not in days, not in weeks, not as a date, not as `since March`", and
     * says the value that answers this must carry the date of the return and never
     * the length of the absence. `ReEntry` is that value and it holds `returnedOn`
     * alone. What reaches a rule is one bit less than that: whether the app is inside
     * the quiet week. A boolean has no length to leak and no date to print, and it is
     * the whole of what a suppression needs, so 14b.4's prohibition holds here without
     * resting on anybody remembering it.
     *
     * ## Which day it is asked about
     *
     * The last local day the window describes, which is the day the sentence would be
     * said on. Asking about the window start would keep withholding for a fortnight
     * after a return, because a trailing seven day window still reaches back into the
     * quiet week for six days after it closes, and 14b.4 buys seven days rather than
     * thirteen.
     *
     * False when there has never been a return, which is the ordinary case and the
     * case a brand new install is in.
     */
    val isJustBackFromAbsence: Boolean,
) {

    companion object {

        /**
         * The ceiling on both current runs, in days.
         *
         * A value at the cap says at least this many and nothing more. It exists so
         * that the two run facts cannot become a record somebody protects: thirty
         * days of quiet and three hundred are the same number here, so there is no
         * personal best to lose and nothing for a later phase to build a milestone
         * out of. Every stage boundary the corpus draws on either run is at four days
         * or fewer, so the cap costs no family anything it can say.
         */
        const val MAX_RUN_DAYS = 30

        /**
         * How long [isJustBackFromAbsence] stays true, in calendar days from the
         * return. MASTER_BUILD_PROMPT 14b.4.
         *
         * Seven, so the day of the return and the six days after it are inside the
         * window and the eighth day is not. It is stated here rather than beside
         * `ReEntry.MIN_GAP_DAYS` because it is a different number answering a
         * different question: that one is the absence that puts the app into the
         * re-entry state, and this one is how long the app then withholds. The Pulse
         * has a third, two days, and it lives with the Pulse for the same reason.
         */
        const val RE_ENTRY_QUIET_DAYS = 7
    }
}
