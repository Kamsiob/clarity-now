package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.query.TrailWindow
import java.time.DayOfWeek
import java.time.ZoneId

/**
 * The two windows the Momentum surfaces are counted over, and the day keys that go with
 * them. `MASTER_BUILD_PROMPT.md` 12.2 and `CORPUS_3_MOMENTUM.md`.
 *
 * **Days are calendar days here, never 86400000 milliseconds.** Every bound below is
 * built from a `LocalDate` in the zone the facade counted with, so the fortnight that
 * spans a daylight saving boundary is still fourteen days a person would name and the
 * dot for the day the clocks went back is one dot rather than one and a bit.
 *
 * **The week starts on Monday, and the Report's week starts on Sunday.** That looks like
 * a contradiction and it is not one. `MASTER_BUILD_PROMPT.md` 12.3 keys a report to a
 * Sunday because a report is a document about a finished week, and 12.2 asks the Momentum
 * stats for "Monday to now" because that is what a person means by this week while they
 * are still in it. `CORPUS_3_MOMENTUM.md` settles it beyond argument for this file: the
 * `weekStarting` banner family triggers on "Monday or Tuesday", which the rule in
 * `MomentumRules` reads as a window one or two days long, and that is only true of a
 * window that opens on Monday. Neither surface here is ever keyed by a week start, so the
 * two definitions never meet in a stored value.
 */
internal object MomentumWindows {

    /** design-v3.md section 11 and `MASTER_BUILD_PROMPT.md` 12.2. Fourteen, always. */
    const val FORTNIGHT_DAYS = 14

    /** The 8 week sparkline in 12.2, and the average `strongPace` is measured against. */
    const val PACE_WEEKS = 8

    /** The 7 day heat strip in 12.2. */
    const val FOCUS_STRIP_DAYS = 7

    /**
     * The trailing fourteen local days ending at [atMillis], half open.
     *
     * It opens at midnight thirteen days ago and closes at the moment of asking, so the
     * window touches exactly fourteen calendar days and `WindowFacts.dayCount` reads 14.
     * Several rules in `MomentumRules` carry a `dayCount >= 14` criterion precisely so
     * that a fortnight family cannot fire on a window that is not a fortnight.
     */
    fun fortnight(atMillis: Long, zone: ZoneId): TrailWindow =
        trailingDays(atMillis, zone, FORTNIGHT_DAYS)

    /**
     * The trailing [days] local days ending at [atMillis], half open, opening at local
     * midnight on the oldest of them.
     *
     * The generalized form of [fortnight], because the focus heat strip in 12.2 is the
     * same shape over seven days and two functions differing by a constant is how the dot
     * row and the strip end up disagreeing about which day is the oldest.
     */
    fun trailingDays(atMillis: Long, zone: ZoneId, days: Int): TrailWindow {
        val today = FactDates.dateOf(atMillis, zone)
        return TrailWindow(
            fromMillis = FactDates.startOfDayMillis(today.minusDays((days - 1).toLong()), zone),
            toMillis = atMillis,
        )
    }

    /**
     * This week so far: local midnight on Monday to the moment of asking, half open.
     *
     * On a Monday this is one day long and on a Sunday it is seven, which is what the
     * three banner families keyed to the point in the week read.
     */
    fun weekToDate(atMillis: Long, zone: ZoneId): TrailWindow {
        val today = FactDates.dateOf(atMillis, zone)
        val intoWeek = (today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        return TrailWindow(
            fromMillis = FactDates.startOfDayMillis(today.minusDays(intoWeek), zone),
            toMillis = atMillis,
        )
    }

    /**
     * The last [days] local day keys ending on the day containing [atMillis], oldest
     * first, so a row built from them reads left to right with today at the trailing end.
     *
     * Walked as calendar dates for the reason at the top of this file, and keyed through
     * `FactDates` so that a key here and a key the engine files anything under can never
     * be two different strings for one day.
     */
    fun dayKeys(atMillis: Long, zone: ZoneId, days: Int): List<String> {
        val today = FactDates.dateOf(atMillis, zone)
        return (days - 1 downTo 0).map { back -> FactDates.keyOf(today.minusDays(back.toLong())) }
    }
}
