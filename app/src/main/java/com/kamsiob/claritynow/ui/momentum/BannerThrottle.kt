package com.kamsiob.claritynow.ui.momentum

/**
 * The Areas banner's rate limit. `CLARITY_LOGIC_ENGINE.md` 6.5 and
 * `MASTER_BUILD_PROMPT.md` 11.2: "recomputed at most once per hour of app use, throttled
 * in the ViewModel and not in the engine."
 *
 * ## Why it is a class of its own rather than two fields on the ViewModel
 *
 * Issue #5 lists it as one of the phase's two risks: "letting the banner recompute on
 * every recomposition rather than once per hour". A rule stated as two fields and a
 * comparison inside a ViewModel is a rule nothing tests, and the ways it goes wrong are
 * all silent. Here it is eleven lines with no Android in them and a test can walk a clock
 * across the boundary in both directions.
 *
 * ## An hour of app use, not an hour of wall clock
 *
 * The instance lives on the ViewModel, which lives as long as the Activity's store, so it
 * is reset by the app being killed and not by an hour passing in the drawer. That is what
 * "an hour of app use" means and it is the cheaper of the two readings to get right: the
 * alternative, accumulating foreground time, would need a second clock and would put a
 * number nobody can check behind a sentence.
 *
 * **It is not persisted, and it must not become persisted.** `MASTER_BUILD_PROMPT.md` 11.4
 * forbids reading engine state from DataStore, and while a throttle is not variation
 * history it is one step from it: a stored "last banner at" would be the first engine
 * adjacent value in preferences and the next one would not be argued about.
 *
 * ## The clock going backwards
 *
 * A device whose clock is corrected backwards, or which crosses a daylight saving boundary
 * the wrong way, produces a negative interval. Treating that as "not due" would freeze the
 * banner until the clock caught up, which for a manual correction of an hour is an hour of
 * a stale sentence. So a negative interval is due: the measurement is no longer meaningful
 * and recomputing costs one read of the log.
 */
class BannerThrottle(private val intervalMillis: Long = ONE_HOUR_MILLIS) {

    private var lastAtMillis: Long? = null

    /** True when the banner may be recomputed at [nowMillis]. Always true the first time. */
    fun isDue(nowMillis: Long): Boolean {
        val last = lastAtMillis ?: return true
        if (nowMillis < last) return true
        return nowMillis - last >= intervalMillis
    }

    /**
     * Records that a recomputation happened at [nowMillis].
     *
     * Called after the attempt whatever it produced, including when the engine was silent
     * and when the corpus could not be read. Recording only a success would put the whole
     * of a quiet week back to reading the log on every visit to the Areas screen, which is
     * the expensive half of what this exists to prevent.
     */
    fun recordAt(nowMillis: Long) {
        lastAtMillis = nowMillis
    }

    companion object {

        /** 6.5. One hour, and the only number in this file. */
        const val ONE_HOUR_MILLIS: Long = 60L * 60L * 1_000L
    }
}
