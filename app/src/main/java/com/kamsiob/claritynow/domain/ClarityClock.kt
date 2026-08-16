package com.kamsiob.claritynow.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * The single source of time in the app. Injected everywhere time is read, so a test
 * can move a day forward and a DST boundary can be walked deliberately rather than
 * waited for.
 *
 * The zone is always explicit. A dateKey computed against a default zone is the
 * documented cause of two pulses in one day, or none at all.
 */
interface ClarityClock {
    fun nowMillis(): Long
    fun zone(): ZoneId
}

class SystemClarityClock(private val zoneProvider: () -> ZoneId = ZoneId::systemDefault) : ClarityClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = zoneProvider()
}

/** For tests and the simulator. Never reaches a release build path. */
class FixedClarityClock(
    private var millis: Long,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : ClarityClock {
    override fun nowMillis(): Long = millis
    override fun zone(): ZoneId = zone
    fun advanceMillis(delta: Long) { millis += delta }
    fun advanceDays(days: Long) { millis += days * 86_400_000L }
    fun set(newMillis: Long) { millis = newMillis }
}

private val DATE_KEY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/** The local calendar day, `yyyy-MM-dd`. The key every daily thing is stored under. */
fun ClarityClock.dateKey(atMillis: Long = nowMillis()): String =
    localDate(atMillis).format(DATE_KEY_FORMAT)

fun ClarityClock.localDate(atMillis: Long = nowMillis()): LocalDate =
    Instant.ofEpochMilli(atMillis).atZone(zone()).toLocalDate()

fun ClarityClock.localDateTime(atMillis: Long = nowMillis()): LocalDateTime =
    Instant.ofEpochMilli(atMillis).atZone(zone()).toLocalDateTime()

/** Local hour of day, 0 to 23. Drives the 17:00 Pulse reflection switch. */
fun ClarityClock.hourOfDay(atMillis: Long = nowMillis()): Int = localDateTime(atMillis).hour

/**
 * The dateKey of the Sunday that starts the week containing [atMillis]. Reports are
 * keyed by this. MASTER_BUILD_PROMPT 12.3.
 */
fun ClarityClock.weekStartKey(atMillis: Long = nowMillis()): String {
    val date = localDate(atMillis)
    val daysSinceSunday = (date.dayOfWeek.value % 7) // Monday is 1, Sunday is 7 which maps to 0
    return date.minusDays(daysSinceSunday.toLong()).format(DATE_KEY_FORMAT)
}

/** Start of the local day containing [atMillis], as epoch millis. */
fun ClarityClock.startOfDayMillis(atMillis: Long = nowMillis()): Long =
    localDate(atMillis).atStartOfDay(zone()).toInstant().toEpochMilli()

/**
 * Whole local days between two instants, counted by calendar date rather than by
 * dividing milliseconds, so a DST shift does not lose or gain a day.
 */
fun ClarityClock.daysBetween(fromMillis: Long, toMillis: Long): Int =
    ChronoUnit.DAYS.between(localDate(fromMillis), localDate(toMillis)).toInt()

fun dayOfWeekOf(date: LocalDate): DayOfWeek = date.dayOfWeek

fun parseDateKey(dateKey: String): LocalDate = LocalDate.parse(dateKey, DATE_KEY_FORMAT)
