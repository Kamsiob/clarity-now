package com.kamsiob.claritynow.domain.engine

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Calendar arithmetic for layer one, with the zone always passed in.
 *
 * Every function here takes its zone as a parameter. There is no default and there
 * is no overload without one, because `ZoneId.systemDefault()` is the documented
 * cause of two Pulses in one day or none at all, and `DomainPurityTest` fails the
 * build on it. The zone the engine uses is the one `TrailQueries` counted with,
 * handed over by `TrailQueries.zone()`.
 *
 * Days are counted between calendar dates and never by dividing milliseconds. The
 * division is wrong across every daylight saving boundary and in every zone whose
 * offset is not a whole number of hours, and it is wrong quietly.
 */
internal object FactDates {

    /** The local calendar date of an instant. */
    fun dateOf(atMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    /** The `yyyy-MM-dd` key of an instant, matching `ClarityClock.dateKey`. */
    fun keyOf(atMillis: Long, zone: ZoneId): String = keyOf(dateOf(atMillis, zone))

    /** The `yyyy-MM-dd` key of a date. ISO formatted, so it never reads a locale. */
    fun keyOf(date: LocalDate): String = DateTimeFormatter.ISO_LOCAL_DATE.format(date)

    /** Local midnight starting [date], as epoch millis. */
    fun startOfDayMillis(date: LocalDate, zone: ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * A `yyyy-MM-dd` key as a date, or null when it is not one.
     *
     * Null rather than an exception. Keys reaching here come from event payloads,
     * and a payload can arrive from an imported file or from a second implementation
     * written against `docs/EVENT_FORMAT.md`. One malformed key must cost that key
     * and not the first screen of somebody's day.
     */
    fun parse(dateKey: String): LocalDate? = runCatching { LocalDate.parse(dateKey) }.getOrNull()

    /** Whole calendar days between two dates. Negative when [to] precedes [from]. */
    fun daysBetween(from: LocalDate, to: LocalDate): Int =
        ChronoUnit.DAYS.between(from, to).toInt()

    /**
     * Whole calendar days between two `yyyy-MM-dd` keys, or null when either is not
     * a date.
     *
     * Null rather than a substituted zero, because every caller is a window
     * comparison and a zero would read as today. A variant whose recorded key cannot
     * be parsed is treated as never used rather than as used this morning, which is
     * the direction that costs a repeat rather than a permanent exclusion.
     */
    fun daysBetweenKeys(fromKey: String, toKey: String): Int? {
        val from = parse(fromKey) ?: return null
        val to = parse(toKey) ?: return null
        return daysBetween(from, to)
    }
}
