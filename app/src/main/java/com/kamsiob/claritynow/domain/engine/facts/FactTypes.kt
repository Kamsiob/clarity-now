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
 * Whether a fall like the one a subject is in now has happened to it before.
 * CLARITY_LOGIC_ENGINE.md 3.1, MASTER_BUILD_PROMPT 14b.9, Addendum 01 7b.
 *
 * **A correctness fix, not politeness.** A fluctuating condition looks identical to a
 * decline in the data: the same fall in completions, the same idle days, the same
 * area going quiet. Without this the app tells a person with a cyclical or relapsing
 * condition that they are deteriorating, on a fixed schedule, forever, and every
 * individual report passes its integrity rules while the claim the sequence makes is
 * false. A shape that has happened before is a rhythm and not a decline, and the
 * difference is a fact rather than a wording.
 *
 * **Four values, because three of them are different reasons a decline family might
 * be allowed to fire and only one of them is a good one.** [NONE] is the permission:
 * there is a fall, there is enough history behind it to have seen its like, and
 * nothing like it is there. [PRESENT] is the veto. [INSUFFICIENT] is neither: it
 * cannot be read as [PRESENT], because that claims a rhythm nothing has seen, and the
 * rhythm branch therefore tests for [PRESENT] and not merely for "not [NONE]".
 *
 * **What it means for the decline branch was settled when the gate was built, and it
 * is not what this block first said.** It said both branches test for their own value,
 * so that a subject with too little history gets neither sentence, which asks the
 * decline families to require [NONE]. `FamilyAvailability.CLOSES_THE_GATE` closes on
 * [PRESENT] alone instead, and the argument that decided it is [NOT_IN_A_DIP] rather
 * than [INSUFFICIENT]: this fact's notion of low is a week under three quarters of the
 * subject's normal, and no decline family asks that question. `decliningActivity`
 * reads a run of three falling weeks that can end on an ordinary week, and
 * `neglectedArea` reads a gap in days that can open inside a week the area was busy at
 * the start of. Requiring [NONE] would silence a true observation every time the two
 * definitions came apart, invisibly. Read that constant for the whole reasoning and
 * for the one line that changes it.
 *
 * [NOT_IN_A_DIP] is the fourth and it is an honest nothing: by this fact's own
 * definition the subject is not low, so there is no fall here to look for a
 * precedent for. It is separate from [NONE] because the two are different claims and
 * a gate is entitled to treat them differently.
 *
 * **Only the verdict travels.** There is deliberately no depth, no duration and no
 * date beside it. Those are numbers about somebody's worst weeks, and a fact set
 * carrying them would be one measure away from a sentence counting them out.
 */
enum class Precedent {

    /** Not low now, on the definition in the companion. Nothing to find a precedent for. */
    NOT_IN_A_DIP,

    /** Low now, enough history to have seen its like, and nothing comparable in it. */
    NONE,

    /** Low now, and at least one earlier fall at least as deep and at least as long. */
    PRESENT,

    /** Low now, and too little history to answer either way. */
    INSUFFICIENT,
    ;

    /**
     * What makes a week low, and what makes an earlier fall comparable.
     *
     * Stated once, here, because a threshold copied into a second place is a threshold
     * that will one day be two different numbers.
     */
    companion object {

        /**
         * Weeks of the subject's own history before this fact will answer at all.
         *
         * Twelve, matching `CueFacts.WINDOW_WEEKS` and `FactExtractor.SERIES_LENGTH`,
         * because a cycle needs two turns before it is a cycle and 14b.9 is explicit
         * that "a person with six weeks of data has no precedent for anything". Under
         * this the answer is [INSUFFICIENT] and the decline families say nothing, which
         * costs a shorter report and buys never telling somebody in their first season
         * that they are falling away.
         */
        const val MIN_HISTORY_WEEKS = 12

        /**
         * Three quarters, as [LOW_NUMERATOR] over [LOW_DENOMINATOR]: a week under three
         * quarters of the subject's normal week is low.
         *
         * Below normal on its own would make half of any history low by construction,
         * and half a history is not a fall. Three quarters is a week somebody would
         * notice. Stated as a fraction and compared by cross multiplication, so the
         * band is exact integer arithmetic and two devices cannot round it apart.
         */
        const val LOW_NUMERATOR = 3

        /** See [LOW_NUMERATOR]. */
        const val LOW_DENOMINATOR = 4

        /**
         * A half, as [DEEP_NUMERATOR] over [DEEP_DENOMINATOR]: a low week under half of
         * normal is the deeper of the two low bands.
         *
         * Three bands rather than a continuous depth, because comparability is what
         * this fact is for. Two falls are the same depth when they are in the same
         * band, and a continuous measure would make almost no two falls comparable and
         * the answer would be [NONE] forever.
         */
        const val DEEP_NUMERATOR = 1

        /** See [DEEP_NUMERATOR]. */
        const val DEEP_DENOMINATOR = 2

        /** A week at or above three quarters of normal. Not low, and not part of a fall. */
        const val BAND_STEADY = 0

        /** A week under three quarters of normal. */
        const val BAND_LOW = 1

        /** A week under half of normal. */
        const val BAND_DEEP = 2

        /**
         * A week with nothing in it at all.
         *
         * Its own band rather than the bottom of [BAND_DEEP], because a subject that
         * stops completely and a subject that halves are different shapes, and an
         * earlier halving is not a precedent for a stop.
         */
        const val BAND_EMPTY = 3
    }
}

/**
 * Which way a person's estimates run against the days they actually happen in.
 * CLARITY_LOGIC_ENGINE.md 3.1, MASTER_BUILD_PROMPT 14b.8, Addendum 01 7a.
 *
 * **This is the tendency half of "only ratios and tendencies".** 14b.8 permits
 * `Things you estimate at an hour tend to take about three` and forbids both `You
 * underestimated by two hours` and `You were off by 140 percent`. The difference is
 * not tone: a ratio describes how somebody's estimates map onto their days, and a
 * delta scores them against a target they set themselves, in a population where time
 * blindness is the reason the estimate was wrong in the first place. The delta
 * measures the symptom and reports it as a mistake.
 *
 * [CLOSE] is the band in which there is nothing to say, and it is defined by the
 * rendering rather than by a chosen number: a median ratio that would print as
 * `about one` is a person whose estimates land, and no family speaks. [INSUFFICIENT]
 * is under the floor of five completed items 14b.8 sets, which is the only floor it
 * sets.
 */
enum class EstimateTendency {

    /** Things stay active markedly longer than they were estimated at. */
    LONGER,

    /** The estimate and the stay are close enough that the ratio would print as one. */
    CLOSE,

    /** Things are finished in markedly less than they were estimated at. */
    SHORTER,

    /** Fewer than [MIN_COMPLETIONS] estimated completions. Nothing may fire. */
    INSUFFICIENT,
    ;

    /** The floor, the window and the band, stated once. */
    companion object {

        /**
         * Completed items carrying an estimate before anything may be said.
         *
         * 14b.8: "at least five completed items carry an estimate inside the window
         * the sentence describes", and the count travels as a `FactRef` so the
         * validator re-reads it.
         */
        const val MIN_COMPLETIONS = 5

        /** Weeks the calibration is measured over. See `HistoryFacts`. */
        const val WINDOW_WEEKS = 12

        /** At or above this the tendency is [LONGER]. A ratio that prints as two or more. */
        const val LONGER_AT = 1.5

        /** Below this the tendency is [SHORTER]. A stay under half the estimate. */
        const val SHORTER_BELOW = 0.5

        /** The band [ratio] falls in, or [INSUFFICIENT] when there is no ratio. */
        fun of(ratio: Double?): EstimateTendency = when {
            ratio == null -> INSUFFICIENT
            ratio >= LONGER_AT -> LONGER
            ratio < SHORTER_BELOW -> SHORTER
            else -> CLOSE
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
