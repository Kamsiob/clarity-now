package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.query.TrailWindow
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Which day a Pulse belongs to, which period it reflects on, and the window it is
 * counted over. `MASTER_BUILD_PROMPT.md` 11.3 steps 1 and 3.
 *
 * All three are decided once, at the instant of generation, and are then carried on
 * the `PULSE_GENERATED` event. Nothing recomputes them afterward, which is what makes
 * "the switch happens exactly once per day" a property of the shape rather than a rule
 * somebody has to remember: the entry is immutable, so the period stored on it is the
 * period the observation was written against, forever.
 */
data class PulseDay(
    /** `yyyy-MM-dd`, the key the entry is stored under and the key every hash is salted with. */
    val dateKey: String,
    val reflectionPeriod: ReflectionPeriod,
    /** Half open, `[fromMillis, toMillis)`, matching every bound in `domain.query`. */
    val window: TrailWindow,
)

/**
 * The one place that decides which day it is and what the Pulse is about.
 *
 * **Every function here takes its zone as a parameter and there is no overload without
 * one.** `ClarityClock`'s own documentation names a date key computed against a default
 * zone as the cause of two Pulses in one day or none at all, and this file is where that
 * mistake would be made. The date key comes from `FactDates`, which is the same helper
 * the engine salts its variant hashes with, so the day the entry is filed under and the
 * day the engine believes it is speaking on cannot be two different strings.
 *
 * **Days are calendar days, never 86400000 milliseconds.** Both windows below are built
 * from `LocalDate` and `ZonedDateTime`, so the day the clocks go forward is twenty three
 * hours long here and the day they go back is twenty five, and a Pulse generated at
 * 00:30 on either of them is filed under the day a person would name.
 */
object PulseSchedule {

    /**
     * The hour the reflection period switches. `MASTER_BUILD_PROMPT.md` 11.3 step 3.
     *
     * Before it the observation describes yesterday, which by then is a whole finished
     * day. At or after it there is enough of today to describe, so it describes today so
     * far. The boundary is inclusive of 17:00 itself, which is what "at or after" says.
     */
    const val REFLECTION_SWITCH_HOUR: Int = 17

    /** The day, the period and the window for a Pulse generated at [atMillis]. */
    fun dayAt(atMillis: Long, zone: ZoneId): PulseDay {
        val today = FactDates.dateOf(atMillis, zone)
        val startOfToday = FactDates.startOfDayMillis(today, zone)
        val localTime = Instant.ofEpochMilli(atMillis).atZone(zone).toLocalTime()
        val beforeSwitch = localTime < LocalTime.of(REFLECTION_SWITCH_HOUR, 0)

        // Yesterday, whole. The window ends at the day boundary rather than at the moment
        // of asking, which is what `ClarityEngine.momentOf` reads as a completed day being
        // reflected on in the morning. Today so far ends now, because now is how far today
        // has got.
        val window = if (beforeSwitch) {
            TrailWindow(
                fromMillis = FactDates.startOfDayMillis(today.minusDays(1), zone),
                toMillis = startOfToday,
            )
        } else {
            TrailWindow(fromMillis = startOfToday, toMillis = atMillis)
        }

        return PulseDay(
            dateKey = FactDates.keyOf(today),
            reflectionPeriod = if (beforeSwitch) {
                ReflectionPeriod.YESTERDAY
            } else {
                ReflectionPeriod.TODAY_SO_FAR
            },
            window = window,
        )
    }
}

/**
 * What a local day's Pulse is, as the log knows it. `MASTER_BUILD_PROMPT.md` 12.1.
 *
 * 12.1 names four states and only three of them are facts about the log. PRESENTED is
 * the sheet being on the screen, which is one process's own knowledge of one moment and
 * is never written down: recording that somebody looked at something is the kind of
 * tracking this app does not do, and a person who opened the sheet and dismissed it is
 * in exactly the state 11.6 protects, which is [READY] and never chased.
 *
 * [IDLE] is the shape of a silent day and of every day before install. It is not an
 * error, it is not a missing row, and nothing renders a placeholder for it: the ambient
 * rhythm row draws it faint, the chip carries no dot, and no reminder is posted.
 */
enum class PulseDayState {

    /** No entry for the day. The engine was silent, or was never asked. */
    IDLE,

    /** An entry exists and has not been answered. The one state a reminder may post in. */
    READY,

    /** An entry exists and carries the answer, stored verbatim. */
    ANSWERED,

    ;

    companion object {

        /** The state [entry] puts its day in. A missing entry is [IDLE]. */
        fun of(entry: PulseEntryState?): PulseDayState = when {
            entry == null -> IDLE
            entry.isAnswered -> ANSWERED
            else -> READY
        }
    }
}
