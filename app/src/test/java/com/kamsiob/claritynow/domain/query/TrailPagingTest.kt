package com.kamsiob.claritynow.domain.query

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * The page window arithmetic. MASTER_BUILD_PROMPT 9, "pagination loads 14 days per
 * page".
 *
 * A page is fourteen **local calendar days**, which is not the same thing as
 * fourteen times 86400000 milliseconds and is only the same thing in a zone with no
 * daylight saving and a whole hour offset. The obvious implementation divides
 * milliseconds by the length of a day, agrees with this one everywhere the author
 * lives, and loses or gains a day twice a year everywhere else. `ClarityClock` already
 * records what that class of defect costs: "A dateKey computed against a default zone
 * is the documented cause of two pulses in one day, or none at all."
 *
 * The three awkward zones are here rather than on a device because they cannot be
 * checked by looking at a screen in August.
 */
class TrailPagingTest {

    private val newYork: ZoneId = ZoneId.of("America/New_York")

    /** UTC+05:45. No daylight saving, and an offset that is not a whole hour. */
    private val kathmandu: ZoneId = ZoneId.of("Asia/Kathmandu")

    private val dayMillis = 86_400_000L
    private val hourMillis = 3_600_000L

    private fun instant(zone: ZoneId, date: LocalDate, hour: Int, minute: Int = 0): Long =
        ZonedDateTime.of(date.atTime(hour, minute), zone).toInstant().toEpochMilli()

    private fun localDateOf(zone: ZoneId, millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private fun localTimeOf(zone: ZoneId, millis: Long): LocalTime =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalTime()

    /** Every local date the window covers, walked one calendar day at a time. */
    private fun datesCovered(zone: ZoneId, window: TrailWindow): List<LocalDate> {
        val first = localDateOf(zone, window.fromMillis)
        val last = localDateOf(zone, window.toMillis - 1)
        return generateSequence(first) { it.plusDays(1) }
            .takeWhile { !it.isAfter(last) }
            .toList()
    }

    @Test
    fun `a page covers fourteen local days ending on the anchor day`() {
        val anchorDate = LocalDate.of(2026, 8, 24)
        val anchor = instant(newYork, anchorDate, 15, 42)
        val window = TrailPaging.pageEndingAt(anchor, newYork)

        val covered = datesCovered(newYork, window)
        assertEquals(TrailPaging.PAGE_DAYS, covered.size)
        assertEquals(anchorDate, covered.last())
        assertEquals(anchorDate.minusDays(13), covered.first())
        // Every day in the page has its own midnight inside the window, so no day is
        // half covered and none is skipped.
        covered.forEach { date ->
            val midnight = ZonedDateTime.of(date.atStartOfDay(), newYork).toInstant().toEpochMilli()
            assertTrue(
                "$date starts outside its own page",
                midnight >= window.fromMillis && midnight < window.toMillis,
            )
        }
        val dayBefore = ZonedDateTime
            .of(covered.first().minusDays(1).atStartOfDay(), newYork)
            .toInstant()
            .toEpochMilli()
        assertTrue(dayBefore < window.fromMillis)
    }

    @Test
    fun `a page boundary lands on local midnight, not on a millisecond arithmetic boundary`() {
        val anchor = instant(newYork, LocalDate.of(2026, 8, 24), 15, 42)
        val window = TrailPaging.pageEndingAt(anchor, newYork)

        assertEquals(LocalTime.MIDNIGHT, localTimeOf(newYork, window.fromMillis))
        // Subtracting thirteen days of milliseconds from a mid afternoon anchor lands
        // in the middle of the afternoon thirteen days ago, which would cut a page off
        // halfway through its own first day.
        assertTrue(window.fromMillis < anchor - 13 * dayMillis)
    }

    @Test
    fun `the anchor event falls inside its own page`() {
        val anchor = instant(newYork, LocalDate.of(2026, 8, 24), 23, 59)
        val window = TrailPaging.pageEndingAt(anchor, newYork)

        // The upper bound is exclusive everywhere in this package, so a page that
        // ended at the anchor would leave out the event it was anchored on.
        assertTrue(anchor >= window.fromMillis && anchor < window.toMillis)
        assertEquals(anchor + 1, window.toMillis)
    }

    @Test
    fun `a page spanning a spring forward still covers fourteen days`() {
        // Daylight saving begins on 2026-03-08 in the United States, so this page
        // contains one local day that is twenty three hours long.
        val anchorDate = LocalDate.of(2026, 3, 14)
        val anchor = instant(newYork, anchorDate, 12)
        val window = TrailPaging.pageEndingAt(anchor, newYork)

        assertEquals(TrailPaging.PAGE_DAYS, datesCovered(newYork, window).size)
        assertEquals(LocalDate.of(2026, 3, 1), localDateOf(newYork, window.fromMillis))
        assertEquals(LocalTime.MIDNIGHT, localTimeOf(newYork, window.fromMillis))
        // Thirteen days and twelve hours of calendar, one hour of which the clocks
        // skipped. Millisecond subtraction cannot produce this number.
        assertEquals(13 * dayMillis + 11 * hourMillis, window.toMillis - 1 - window.fromMillis)
    }

    @Test
    fun `a page spanning a fall back still covers fourteen days`() {
        // Daylight saving ends on 2026-11-01, so this page contains one local day
        // that is twenty five hours long.
        val anchorDate = LocalDate.of(2026, 11, 10)
        val anchor = instant(newYork, anchorDate, 12)
        val window = TrailPaging.pageEndingAt(anchor, newYork)

        assertEquals(TrailPaging.PAGE_DAYS, datesCovered(newYork, window).size)
        assertEquals(LocalDate.of(2026, 10, 28), localDateOf(newYork, window.fromMillis))
        assertEquals(LocalTime.MIDNIGHT, localTimeOf(newYork, window.fromMillis))
        assertEquals(13 * dayMillis + 13 * hourMillis, window.toMillis - 1 - window.fromMillis)
    }

    @Test
    fun `a page in a non integral offset zone covers fourteen days`() {
        val anchorDate = LocalDate.of(2026, 6, 10)
        val anchor = instant(kathmandu, anchorDate, 12)
        val window = TrailPaging.pageEndingAt(anchor, kathmandu)

        val offsetSeconds = Instant.ofEpochMilli(anchor).atZone(kathmandu).offset.totalSeconds
        assertTrue(
            "this test is worthless in a whole hour zone, and the offset here is " +
                "$offsetSeconds seconds",
            offsetSeconds % 3600 != 0,
        )
        assertEquals(TrailPaging.PAGE_DAYS, datesCovered(kathmandu, window).size)
        assertEquals(LocalDate.of(2026, 5, 28), localDateOf(kathmandu, window.fromMillis))
        assertEquals(LocalTime.MIDNIGHT, localTimeOf(kathmandu, window.fromMillis))
        assertEquals(13 * dayMillis + 12 * hourMillis, window.toMillis - 1 - window.fromMillis)
    }

    @Test
    fun `a shorter page is still anchored on the day the anchor fell on`() {
        val anchorDate = LocalDate.of(2026, 3, 9)
        val anchor = instant(newYork, anchorDate, 12)
        val window = TrailPaging.pageEndingAt(anchor, newYork, days = 3)

        assertEquals(
            listOf(
                LocalDate.of(2026, 3, 7),
                LocalDate.of(2026, 3, 8),
                LocalDate.of(2026, 3, 9),
            ),
            datesCovered(newYork, window),
        )
        assertEquals(
            2L,
            ChronoUnit.DAYS.between(
                localDateOf(newYork, window.fromMillis),
                localDateOf(newYork, window.toMillis - 1),
            ),
        )
    }

    @Test
    fun `a page of no days is refused rather than silently empty`() {
        val failure = runCatching { TrailPaging.pageEndingAt(0L, newYork, days = 0) }
        assertNotNull(failure.exceptionOrNull())
    }
}
