package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.data.event.ReflectionPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Which day a Pulse belongs to, and what it is about.
 * `MASTER_BUILD_PROMPT.md` 11.3 steps 1 and 3, and issue #4's generation criteria.
 *
 * **Two defects would each ruin the feature and neither is visible in a screenshot.**
 * Generating twice in one day makes the app say two different things about the same
 * morning and files the second as a duplicate conflict. Generating not at all across a
 * boundary makes a day silently IDLE. Both come from the same mistake, which is treating
 * a day as 86400000 milliseconds, and both only ever happen on two days a year in zones
 * the author does not live in.
 *
 * Every instant below is built from a local date and a local time in a zone with real
 * daylight saving, never from an epoch offset, because an implementation that divided
 * milliseconds would agree with a UTC fixture on every assertion here.
 */
class PulseScheduleTest {

    private val zone: ZoneId = ZoneId.of("America/New_York")

    /** Daylight saving begins. 02:00 becomes 03:00, so the local day is twenty three hours. */
    private val springForward: LocalDate = LocalDate.of(2026, 3, 8)

    /** Daylight saving ends. 02:00 becomes 01:00, so the local day is twenty five hours. */
    private val fallBack: LocalDate = LocalDate.of(2026, 11, 1)

    private val hour = 3_600_000L

    private fun at(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    // The 17:00 switch ---------------------------------------------------------

    @Test
    fun `before 17 00 the Pulse reflects on yesterday, whole`() {
        val date = LocalDate.of(2026, 6, 10)
        val day = PulseSchedule.dayAt(at(date, 16, 59), zone)

        assertEquals("2026-06-10", day.dateKey)
        assertEquals(ReflectionPeriod.YESTERDAY, day.reflectionPeriod)
        assertEquals(at(date.minusDays(1), 0), day.window.fromMillis)
        assertEquals(
            "the window ends at the day boundary rather than at the moment of asking, " +
                "which is what makes it a finished day being reflected on",
            at(date, 0),
            day.window.toMillis,
        )
    }

    @Test
    fun `at 17 00 the Pulse reflects on today so far`() {
        val date = LocalDate.of(2026, 6, 10)
        val asked = at(date, 17, 0)
        val day = PulseSchedule.dayAt(asked, zone)

        assertEquals("2026-06-10", day.dateKey)
        assertEquals(ReflectionPeriod.TODAY_SO_FAR, day.reflectionPeriod)
        assertEquals(at(date, 0), day.window.fromMillis)
        assertEquals("today so far ends now, because now is how far today has got", asked, day.window.toMillis)
    }

    @Test
    fun `the switch happens once, on the boundary minute`() {
        val date = LocalDate.of(2026, 6, 10)
        val before = PulseSchedule.dayAt(at(date, 16, 59), zone)
        val on = PulseSchedule.dayAt(at(date, 17, 0), zone)

        assertNotEquals(before.reflectionPeriod, on.reflectionPeriod)
        assertEquals("both are the same local day", before.dateKey, on.dateKey)
    }

    // Spring forward -----------------------------------------------------------

    @Test
    fun `the date key is right on both sides of a spring forward`() {
        assertEquals("2026-03-08", PulseSchedule.dayAt(at(springForward, 0, 30), zone).dateKey)
        assertEquals("2026-03-08", PulseSchedule.dayAt(at(springForward, 23, 30), zone).dateKey)
        assertEquals(
            "2026-03-07",
            PulseSchedule.dayAt(at(springForward.minusDays(1), 23, 30), zone).dateKey,
        )
    }

    @Test
    fun `yesterday is twenty three hours long the morning after a spring forward`() {
        val day = PulseSchedule.dayAt(at(springForward.plusDays(1), 8), zone)

        assertEquals(ReflectionPeriod.YESTERDAY, day.reflectionPeriod)
        assertEquals(
            "the day the clocks went forward is twenty three hours, and a window built by " +
                "subtracting 86400000 would start an hour into it and drop everything before that",
            23 * hour,
            day.window.toMillis - day.window.fromMillis,
        )
    }

    @Test
    fun `a spring forward produces one day key per calendar day and no more`() {
        assertEquals(
            listOf("2026-03-07", "2026-03-08", "2026-03-09"),
            dayKeysAcross(springForward.minusDays(1), springForward.plusDays(2)),
        )
    }

    // Fall back ----------------------------------------------------------------

    @Test
    fun `an hour that happens twice is one day and one reflection period`() {
        val ambiguous = LocalDateTime.of(fallBack, LocalTime.of(1, 30))
        val firstPass = ZonedDateTime.of(ambiguous, zone).toInstant().toEpochMilli()
        val secondPass = ZonedDateTime.of(ambiguous, zone)
            .withLaterOffsetAtOverlap()
            .toInstant()
            .toEpochMilli()

        assertNotEquals("01:30 happens at two different instants that day", firstPass, secondPass)
        assertEquals(
            "both are 01:30 on the first of November, and a Pulse generated at either of " +
                "them belongs to the same day",
            PulseSchedule.dayAt(firstPass, zone),
            PulseSchedule.dayAt(secondPass, zone),
        )
    }

    @Test
    fun `yesterday is twenty five hours long the morning after a fall back`() {
        val day = PulseSchedule.dayAt(at(fallBack.plusDays(1), 8), zone)

        assertEquals(ReflectionPeriod.YESTERDAY, day.reflectionPeriod)
        assertEquals(
            "the day the clocks went back is twenty five hours, and an hour of it would be " +
                "missing from a window built by subtracting 86400000",
            25 * hour,
            day.window.toMillis - day.window.fromMillis,
        )
    }

    @Test
    fun `a fall back produces one day key per calendar day and no more`() {
        assertEquals(
            listOf("2026-10-31", "2026-11-01", "2026-11-02"),
            dayKeysAcross(fallBack.minusDays(1), fallBack.plusDays(2)),
        )
    }

    /**
     * Every distinct date key an hourly walk from [from] to [to] produces.
     *
     * The walk is over instants rather than over local times, so the twenty five hour day
     * really does yield twenty five samples and the twenty three hour day yields twenty
     * three. What the assertion cares about is that neither produces a fourth key and
     * neither skips one: one Pulse per local day, whatever the offset did.
     */
    private fun dayKeysAcross(from: LocalDate, to: LocalDate): List<String> {
        val start = at(from, 0)
        val end = at(to, 0)
        val keys = LinkedHashSet<String>()
        var cursor = start
        while (cursor < end) {
            keys += PulseSchedule.dayAt(cursor, zone).dateKey
            cursor += hour
        }
        return keys.toList()
    }
}
