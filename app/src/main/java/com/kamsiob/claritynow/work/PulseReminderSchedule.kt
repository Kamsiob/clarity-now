package com.kamsiob.claritynow.work

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * When the next daily reminder is due, as a calendar question rather than an arithmetic
 * one. MASTER_BUILD_PROMPT 12.1: "WorkManager schedules a daily notification at the
 * chosen hour".
 *
 * **Pure, and separated from the scheduler for one reason: this is the part that can be
 * wrong across a daylight saving boundary, and it is the part a test can walk.** The
 * enqueue around it needs a device.
 *
 * **A day here is a calendar day and never 86400000 milliseconds.** The next occurrence
 * is built as a local date plus a local time, resolved back through the zone, so the
 * day the clocks go forward is twenty three hours long and the day they go back is
 * twenty five. Adding a fixed day of milliseconds would move the reminder an hour on
 * each transition and leave it there, which is a promise about a time quietly broken
 * twice a year.
 *
 * Both awkward hours fall out of `java.time` rather than being handled here, and the
 * behavior is stated because it is a decision either way.
 *
 * - **The hour that does not exist.** With the reminder at 02:00 on the morning the
 *   clocks go forward, `atZone` resolves the gap forward to 03:00 local. One reminder
 *   that day, an hour later than usual, rather than none.
 * - **The hour that happens twice.** With the reminder at 01:00 on the morning they go
 *   back, `atZone` takes the earlier of the two offsets. One reminder that day, on the
 *   first pass, rather than two.
 *
 * **The next occurrence is always strictly after [nowMillis].** The worker calls this
 * to arm the following day while standing on the hour it just fired at, and a boundary
 * of "at or after" would hand it a delay of zero and a reminder every few seconds.
 */
internal fun nextReminderAtMillis(nowMillis: Long, zone: ZoneId, hour: Int): Long {
    val now = Instant.ofEpochMilli(nowMillis)
    val here = now.atZone(zone)
    val at = LocalTime.of(hour.coerceIn(FIRST_HOUR, LAST_HOUR), 0)
    val today = here.toLocalDate().atTime(at).atZone(zone)
    val next = if (today.toInstant() > now) {
        today
    } else {
        here.toLocalDate().plusDays(1).atTime(at).atZone(zone)
    }
    return next.toInstant().toEpochMilli()
}

/** How long from [nowMillis] until the next reminder. Always positive. */
internal fun reminderDelayMillis(nowMillis: Long, zone: ZoneId, hour: Int): Long =
    nextReminderAtMillis(nowMillis, zone, hour) - nowMillis

/**
 * The hour is a stored preference and a stored preference is whatever was last written
 * to it, so it is clamped rather than trusted. `ClarityPreferences` defaults it to 20.
 */
internal const val FIRST_HOUR: Int = 0
internal const val LAST_HOUR: Int = 23
