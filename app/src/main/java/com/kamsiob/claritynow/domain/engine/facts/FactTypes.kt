package com.kamsiob.claritynow.domain.engine

import java.time.DayOfWeek

/**
 * The vocabulary layer one is declared in. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * **These live in `domain.engine` while the file sits under `engine/facts/`.** The
 * directory records who owns them; the package is what 2.1 specifies and what every
 * other layer writes without an import. A rule reading
 * `Criterion(test = { facts, subject -> ... })` in `domain.engine` must see
 * [FactSet] with nothing imported, because a type that needs an import is a type
 * somebody eventually redeclares.
 *
 * The four typealiases are documentation rather than safety. Kotlin typealiases are
 * transparent, so `AreaId` and `String` are one type and no compiler will stop an
 * item id being passed where an area id belongs. They are here because 2.1 declares
 * them and because a map named `Map<AreaId, AreaFacts>` reads as what it is.
 */

typealias AreaId = String

typealias ItemId = String

typealias FamilyKey = String

typealias VariantKey = String

/**
 * The direction of a weekly series, over its three most recent buckets.
 *
 * **Strictly monotonic, deliberately.** The corpus asks for "three weeks of rising
 * completions", "queues growing three weeks running" and "falling activity across
 * three or more weeks", and every one of those sentences claims a run rather than a
 * net change. A trend computed from first against last would call 4, 1, 5 rising,
 * and the person who lived that week would know the sentence was wrong.
 *
 * [INSUFFICIENT] rather than [FLAT] when there are fewer than three buckets, so a
 * rule can tell "no direction" from "not enough data to have one". A pattern family
 * needs `weeksOfData >= 3` anyway, and a flat reading on a two week old install
 * would be the app claiming steadiness it cannot see.
 */
enum class Trend {
    RISING,
    FALLING,
    FLAT,
    INSUFFICIENT,
    ;

    companion object {

        /** How many buckets the direction is read over. */
        const val WINDOW = 3

        /**
         * The direction of [series], oldest first.
         *
         * Reads only the last [WINDOW] values. Everything older is history the
         * series already carries for the rules that want it.
         */
        fun of(series: List<Int>): Trend {
            if (series.size < WINDOW) return INSUFFICIENT
            val recent = series.takeLast(WINDOW)
            val rising = recent.zipWithNext().all { (earlier, later) -> later > earlier }
            if (rising) return RISING
            val falling = recent.zipWithNext().all { (earlier, later) -> later < earlier }
            if (falling) return FALLING
            return FLAT
        }
    }
}

/**
 * The four bands of a local day. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * **The boundaries are the app's own, not an even quarter split.** design-v3.md 5
 * already divides the Pulse day into dawn 05 to 11, midday 11 to 17 and evening 17
 * to 05, and CLARITY_LOGIC_ENGINE.md 7.4 selects a register against those same three
 * names. A fourth band has to come from somewhere, so [NIGHT] splits the long
 * evening at 22:00 and the other three boundaries are the ones the app already draws.
 *
 * design-v3.md 15 asks that the statistically common option be identified and
 * rejected where it serves no better. Here that is the even six hour split, 00 to 06
 * night, 06 to 12 morning, 12 to 18 afternoon, 18 to 24 evening. It is rejected
 * because it would put 17:30 in the afternoon while the same instant is evening to
 * the Pulse background and evening to the register rule, and one app cannot hold two
 * definitions of evening without eventually printing the wrong one.
 */
enum class PartOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ;

    companion object {

        /**
         * The band containing [hourOfDay], 0 to 23.
         *
         * Out of range hours answer [NIGHT] rather than throwing. The only hour
         * that reaches here comes from a local time, so an out of range value is a
         * programming error, and a crash inside fact extraction takes down the first
         * screen of the day over an arithmetic slip.
         */
        fun of(hourOfDay: Int): PartOfDay = when (hourOfDay) {
            in 5..10 -> MORNING
            in 11..16 -> AFTERNOON
            in 17..21 -> EVENING
            else -> NIGHT
        }
    }
}

/** The days, as the engine names them. CLARITY_LOGIC_ENGINE.md 2.1. */
enum class Weekday {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN,
    ;

    /** True for Saturday and Sunday, which is what `CueFacts.weekdayOnly` measures. */
    val isWeekend: Boolean get() = this == SAT || this == SUN

    companion object {

        /**
         * The engine's name for a `java.time` day.
         *
         * Mapped by ordinal rather than by name so the two orderings are pinned
         * together by the compiler: `DayOfWeek` is Monday first and so is this, and
         * a name lookup would keep working if either list were reordered.
         */
        fun of(day: DayOfWeek): Weekday = entries[day.ordinal]
    }
}

/**
 * The one time events. CLARITY_LOGIC_ENGINE.md 2.1 and 3.1.
 *
 * A flag is present **only in the window where the first occurrence happened**, so a
 * rule reading it cannot congratulate somebody twice for the same first. That is a
 * property of the extraction rather than of this enum, and `HistoryFacts.firstEverFlags`
 * states it again where the set is declared.
 */
enum class FirstEver {
    FIRST_COMPLETION,
    FIRST_FOCUS_SESSION,
    FIRST_SWAP,
    FIRST_AREA_ARCHIVED,
    FIRST_QUEUE_DRAIN,
    FIRST_WEEK_WITH_ALL_AREAS_ACTIVE,
}
