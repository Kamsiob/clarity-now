package com.kamsiob.claritynow.work

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * When the daily Pulse reminder is next due. MASTER_BUILD_PROMPT 12.1, issue #4.
 *
 * The reminder is the one part of the Pulse that promises a time, and the mistake that
 * breaks it is the same one that breaks generation: treating a day as 86400000
 * milliseconds. An implementation that added a fixed day would pass every assertion in
 * a zone with no daylight saving and would move the reminder an hour, permanently, on
 * two mornings a year. So every instant below is built from a local date and a local
 * time in a zone that really shifts.
 *
 * The second defect this covers has no season. [nextReminderAtMillis] is called by the
 * worker while standing on the hour it just fired at, so a boundary of "at or after"
 * rather than "after" would return the current instant, hand WorkManager a delay of
 * zero, and turn a daily reminder into a loop.
 */
class PulseReminderScheduleTest {

    private val zone: ZoneId = ZoneId.of("America/New_York")

    /** Daylight saving begins. 02:00 becomes 03:00, so the local day is twenty three hours. */
    private val springForward: LocalDate = LocalDate.of(2026, 3, 8)

    /** Daylight saving ends. 01:00 happens twice, so the local day is twenty five hours. */
    private val fallBack: LocalDate = LocalDate.of(2026, 11, 1)

    private val hourMillis = 3_600_000L

    private fun at(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    // The ordinary day ---------------------------------------------------------

    @Test
    fun `before the hour, the reminder is today`() {
        val date = LocalDate.of(2026, 6, 10)

        assertEquals(at(date, 20), nextReminderAtMillis(at(date, 19), zone, hour = 20))
        assertEquals(hourMillis, reminderDelayMillis(at(date, 19), zone, hour = 20))
    }

    @Test
    fun `after the hour, the reminder is tomorrow`() {
        val date = LocalDate.of(2026, 6, 10)

        assertEquals(at(date.plusDays(1), 20), nextReminderAtMillis(at(date, 20, 1), zone, 20))
    }

    /**
     * The worker arms the next hop from inside the run it was armed for, so it asks this
     * question standing exactly on the hour. "At or after" would answer with now.
     */
    @Test
    fun `standing exactly on the hour, the reminder is tomorrow and the delay is a day`() {
        val date = LocalDate.of(2026, 6, 10)
        val now = at(date, 20)

        assertEquals(at(date.plusDays(1), 20), nextReminderAtMillis(now, zone, 20))
        assertEquals(24 * hourMillis, reminderDelayMillis(now, zone, 20))
    }

    @Test
    fun `the delay is always positive`() {
        val date = LocalDate.of(2026, 6, 10)

        for (hour in 0..23) {
            assertTrue(
                "hour $hour",
                reminderDelayMillis(at(date, 20), zone, hour) > 0,
            )
        }
    }

    // Daylight saving ----------------------------------------------------------

    /**
     * The evening before the clocks go forward, the next reminder is twenty three hours
     * away rather than twenty four, and it lands on 20:00 local on the short day.
     */
    @Test
    fun `spring forward, the gap is twenty three hours and the hour is kept`() {
        val evening = at(springForward.minusDays(1), 20)

        val next = nextReminderAtMillis(evening, zone, 20)

        assertEquals(at(springForward, 20), next)
        assertEquals(23 * hourMillis, next - evening)
    }

    /**
     * The hour that does not exist. With the reminder set to 02:00, `atZone` resolves
     * the gap forward, so the morning the clocks go forward carries one reminder at
     * 03:00 rather than none at all.
     */
    @Test
    fun `spring forward, a reminder set inside the gap arrives an hour later`() {
        val night = at(springForward.minusDays(1), 23)

        val next = nextReminderAtMillis(night, zone, hour = 2)

        assertEquals(at(springForward, 3), next)
        assertEquals(3 * hourMillis, next - night)
    }

    /**
     * The evening before the clocks go back, the next reminder is twenty five hours away
     * rather than twenty four. This is the assertion an implementation that added a
     * fixed day would fail: it would arrive at 19:00 local and stay there.
     */
    @Test
    fun `fall back, the gap is twenty five hours and the hour is kept`() {
        val evening = at(fallBack.minusDays(1), 20)

        val next = nextReminderAtMillis(evening, zone, 20)

        assertEquals(at(fallBack, 20), next)
        assertEquals(25 * hourMillis, next - evening)
    }

    /**
     * The hour that happens twice. With the reminder set to 01:00, `atZone` takes the
     * earlier of the two offsets, so the morning the clocks go back carries one reminder
     * rather than two.
     */
    @Test
    fun `fall back, a reminder set on the repeated hour fires on the first pass`() {
        val midnight = at(fallBack, 0)
        val earlier = fallBack.atTime(1, 0)
            .atZone(zone)
            .withEarlierOffsetAtOverlap()
            .toInstant()
            .toEpochMilli()

        val next = nextReminderAtMillis(midnight, zone, hour = 1)

        assertEquals(earlier, next)
        assertEquals(hourMillis, next - midnight)
    }

    // The stored hour ----------------------------------------------------------

    /**
     * The hour is a stored preference, which is whatever was last written to it. A value
     * outside the day is clamped rather than throwing, because a scheduler that threw
     * would take the reminder away permanently for whoever hit it.
     */
    @Test
    fun `an impossible hour is clamped rather than thrown`() {
        val date = LocalDate.of(2026, 6, 10)
        val now = at(date, 12)

        assertEquals(at(date.plusDays(1), 0), nextReminderAtMillis(now, zone, hour = -3))
        assertEquals(at(date, 23), nextReminderAtMillis(now, zone, hour = 99))
    }
}
