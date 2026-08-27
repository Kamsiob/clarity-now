package com.kamsiob.claritynow.domain.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * The window and the cadence. `MASTER_BUILD_PROMPT.md` 11.3 step 1 and 12.3.
 *
 * The daylight saving case is the one worth having. A window built by subtracting seven
 * times 86,400,000 milliseconds is wrong twice a year, in one direction each time, and it is
 * wrong quietly: the report covers six days and twenty three hours, the week ribbon draws
 * seven marks anyway, and the only visible symptom is a mark that is slightly short.
 */
class ReportScheduleTest {

    private val newYork = ZoneId.of("America/New_York")

    private fun at(date: LocalDate, hour: Int, zone: ZoneId = newYork): Long =
        date.atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `the window is the seven completed days before today`() {
        val week = ReportSchedule.weekAt(at(LocalDate.of(2026, 7, 19), 9), newYork)
        assertEquals("2026-07-12", week.weekStartKey)
        assertEquals("2026-07-19", week.dateKey)
        assertEquals(at(LocalDate.of(2026, 7, 12), 0), week.window.fromMillis)
        assertEquals(at(LocalDate.of(2026, 7, 19), 0), week.window.toMillis)
    }

    @Test
    fun `the cadence week begins on the Sunday of the week generation happened in`() {
        // A Wednesday. The trailing seven days are Wednesday to Tuesday, and the week the
        // report counts against is the one that began on Sunday. Keying the cadence on the
        // window would let somebody who opened the app on Wednesday and again on Friday get
        // two reports in one week.
        val wednesday = ReportSchedule.weekAt(at(LocalDate.of(2026, 7, 22), 9), newYork)
        assertEquals("2026-07-15", wednesday.weekStartKey)
        assertEquals(at(LocalDate.of(2026, 7, 19), 0), wednesday.currentWeekStartMillis)
    }

    @Test
    fun `a Sunday generation counts against the week it opens`() {
        val sunday = ReportSchedule.weekAt(at(LocalDate.of(2026, 7, 19), 9), newYork)
        assertEquals(at(LocalDate.of(2026, 7, 19), 0), sunday.currentWeekStartMillis)
    }

    @Test
    fun `the window is seven calendar days across the spring forward, not seven times a day of milliseconds`() {
        // The clocks go forward on March 8, 2026 in New York, so the seven days ending on
        // March 12 are twenty three hours shorter than seven times 86,400,000.
        val week = ReportSchedule.weekAt(at(LocalDate.of(2026, 3, 12), 9), newYork)
        assertEquals("2026-03-05", week.weekStartKey)
        assertEquals(at(LocalDate.of(2026, 3, 5), 0), week.window.fromMillis)
        assertEquals(at(LocalDate.of(2026, 3, 12), 0), week.window.toMillis)
        assertNotEquals(
            "the window was built by multiplying rather than by counting days",
            SEVEN_DAYS_OF_MILLIS,
            week.window.toMillis - week.window.fromMillis,
        )
        assertEquals(SEVEN_DAYS_OF_MILLIS - HOUR, week.window.toMillis - week.window.fromMillis)
    }

    @Test
    fun `the window is seven calendar days across the fall back too`() {
        // November 1, 2026, and the week is an hour longer rather than an hour shorter.
        val week = ReportSchedule.weekAt(at(LocalDate.of(2026, 11, 5), 9), newYork)
        assertEquals(SEVEN_DAYS_OF_MILLIS + HOUR, week.window.toMillis - week.window.fromMillis)
    }

    @Test
    fun `the same instant in two zones can be two different weeks, which is why the zone is a parameter`() {
        // Just after midnight in Auckland is the previous afternoon in New York, and the two
        // name different days. There is no overload of anything here without a zone.
        val instant = at(LocalDate.of(2026, 7, 20), 9, ZoneId.of("Pacific/Auckland"))
        assertNotEquals(
            ReportSchedule.weekAt(instant, ZoneId.of("Pacific/Auckland")).dateKey,
            ReportSchedule.weekAt(instant, newYork).dateKey,
        )
    }

    private companion object {
        const val HOUR = 3_600_000L
        const val SEVEN_DAYS_OF_MILLIS = 7 * 24 * HOUR
    }
}
