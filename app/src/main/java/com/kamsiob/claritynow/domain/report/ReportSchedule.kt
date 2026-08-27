package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.query.TrailWindow
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Which seven days a report describes, and which week it counts against.
 * `MASTER_BUILD_PROMPT.md` 11.3 step 1 and 12.3.
 *
 * **Every function here takes its zone as a parameter and there is no overload without
 * one**, for the reason `PulseSchedule` states in the same words: a date key computed
 * against a default zone is the documented cause of a surface firing twice in one day or
 * not at all. Days are calendar days built from `LocalDate`, never 86,400,000 milliseconds,
 * so the week the clocks change is 167 hours long here and the report still covers seven
 * days.
 */
data class ReportWeek(
    /** The first of the seven days described, `yyyy-MM-dd`. The eyebrow names it. */
    val weekStartKey: String,
    /** The local day generation happened on, which is what the variant hash is salted with. */
    val dateKey: String,
    /** Half open, `[fromMillis, toMillis)`, matching every bound in `domain.query`. */
    val window: TrailWindow,
    /** Local midnight starting the calendar week generation happened in. See [ReportSchedule]. */
    val currentWeekStartMillis: Long,
)

/**
 * The window and the cadence. `MASTER_BUILD_PROMPT.md` 12.3.
 *
 * > Trailing 7 days ending today, recalculated on every generation. Generated
 * > automatically on first open in a new week (Sunday start), regenerable at any time.
 * > Past weeks remain forever.
 *
 * ## What `ending today` was read as, and why
 *
 * The seven **completed** days before today, `[startOfDay(today - 7), startOfDay(today))`.
 * The other reading includes today so far, and it costs something visible: `design-v3.md`
 * 11.1 draws the week as seven marks whose height is that day's activity against the
 * busiest, and a day three hours old drawn at full width beside six whole ones is a claim
 * about a day that is not over. It is also the shape the Pulse already uses before 17:00,
 * where the reflection period is yesterday, whole, and it is the window the simulator has
 * been generating a year of reports against since phase 5.
 *
 * ## The week and the window are two different questions
 *
 * "Generated on first open in a new week" is a question about the calendar, and "the
 * trailing seven days" is a question about the window, and on any day but Sunday they name
 * different spans. Conflating them is the mistake this type exists to prevent: keying the
 * cadence on the window's first day would let a person who opened the app on Wednesday and
 * again on Friday get two reports in one week, because the trailing seven days had moved.
 *
 * So the cadence is asked of the log directly, with [ReportWeek.currentWeekStartMillis]:
 * **a report is due when no `REPORT_GENERATED` event has been written since local midnight
 * on the Sunday that begins this week.** Every event carries its wall clock, so this needs
 * no extra field on the payload and cannot disagree with itself.
 */
object ReportSchedule {

    /** 12.3. The week begins on Sunday. */
    val WEEK_START: DayOfWeek = DayOfWeek.SUNDAY

    /** 11.3 step 1. */
    const val WINDOW_DAYS: Int = 7

    /** The week a report generated at [atMillis] describes, and the week it counts against. */
    fun weekAt(atMillis: Long, zone: ZoneId): ReportWeek {
        val today = FactDates.dateOf(atMillis, zone)
        val windowStart = today.minusDays(WINDOW_DAYS.toLong())
        val weekStart = today.with(TemporalAdjusters.previousOrSame(WEEK_START))
        return ReportWeek(
            weekStartKey = FactDates.keyOf(windowStart),
            dateKey = FactDates.keyOf(today),
            window = TrailWindow(
                fromMillis = FactDates.startOfDayMillis(windowStart, zone),
                toMillis = FactDates.startOfDayMillis(today, zone),
            ),
            currentWeekStartMillis = FactDates.startOfDayMillis(weekStart, zone),
        )
    }
}
