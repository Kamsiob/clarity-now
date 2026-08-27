package com.kamsiob.claritynow.domain.engine

/**
 * The rhythms a plan may be anchored to. CLARITY_LOGIC_ENGINE.md 3.7.
 *
 * Computed over a **twelve week** window ending with the fact window, because a cue
 * has to be a pattern and twelve weeks is long enough that an accident cannot look
 * like one. Read only by layer six.
 *
 * **An invented cue is worse than no plan**, because it makes a claim about the
 * person's life that the person knows to be false, in the one place the app is
 * allowed to sound like advice. Every field below is null unless its cue cleared all
 * three thresholds in [Thresholds], and [hasStableRhythm] is false unless at least
 * one of them did. Layer six may not produce a plan when it is false.
 */
data class CueFacts(
    /** The weekday carrying the most events, or null when the cue did not clear. */
    val strongestWeekday: Weekday?,
    /**
     * The share of weeks in which [strongestWeekday] was that week's own busiest day.
     *
     * 0.0 when there is no strongest weekday. This is the "holding in at least 60
     * percent of those weeks" test made into a number, so a corpus line rendering a
     * cue can be traced back to how often it was actually true.
     */
    val strongestWeekdayConfidence: Double,
    /** The weekday carrying the fewest events, or null when the cue did not clear. */
    val quietestWeekday: Weekday?,
    /** The band of the day carrying the most events, or null when the cue did not clear. */
    val productiveBand: PartOfDay?,
    /** [productiveBand]'s share of events in the cue window. 0.0 when there is none. */
    val productiveBandShare: Double,
    /** The weekday focus sessions usually start on, or null when the cue did not clear. */
    val focusTypicalWeekday: Weekday?,
    /** The band focus sessions usually start in, or null when the cue did not clear. */
    val focusTypicalBand: PartOfDay?,
    /** The band items are usually added in, or null when the cue did not clear. */
    val addingBand: PartOfDay?,
    /**
     * True when the weekend holds less than a tenth of the cue window's events.
     *
     * A plan anchored to a Saturday for somebody who has never opened the app on a
     * weekend is the invented cue failure in its purest form, so layer six reads
     * this before choosing a day.
     */
    val weekdayOnly: Boolean,
    /**
     * True when at least one cue above cleared its thresholds.
     *
     * 3.7: "If nothing clears these, `hasStableRhythm` is false and layer 6 may not
     * produce a plan."
     */
    val hasStableRhythm: Boolean,
) {

    /**
     * The three gates every cue passes, all mandatory. CLARITY_LOGIC_ENGINE.md 3.7.
     *
     * Stated once, here, because a threshold copied into a second place is a
     * threshold that will one day be two different numbers.
     */
    object Thresholds {

        /** Weeks of data the cue window must hold. */
        const val MIN_WEEKS = 6

        /** The share of those weeks in which the pattern must hold. */
        const val MIN_CONFIDENCE = 0.6

        /** The underlying event count a cue must be drawn from. */
        const val MIN_EVENTS = 8

        /** Weeks the cue window covers. */
        const val WINDOW_WEEKS = 12

        /** The weekend share below which [weekdayOnly] is true. */
        const val WEEKEND_SHARE_CEILING = 0.1
    }

    companion object {

        /** Every cue absent. What an install with no rhythm yet extracts to. */
        val NONE = CueFacts(
            strongestWeekday = null,
            strongestWeekdayConfidence = 0.0,
            quietestWeekday = null,
            productiveBand = null,
            productiveBandShare = 0.0,
            focusTypicalWeekday = null,
            focusTypicalBand = null,
            addingBand = null,
            weekdayOnly = false,
            hasStableRhythm = false,
        )
    }
}
