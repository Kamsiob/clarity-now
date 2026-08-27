package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.query.TEST_ZONE
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.dateKey
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.opened
import com.kamsiob.claritynow.domain.query.promote
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState

/**
 * A month of ordinary use, with the Pulse lifecycle run over it one foreground a day.
 *
 * Shared by the tests of the lifecycle and of the two benches, because both need the same
 * thing: real entries, produced by the real engine from the real corpus, sitting in a real
 * log. A fixture that hand wrote a `PulseGenerated` would prove that the code under test
 * can read a literal.
 *
 * **The order inside a day is the order the app really does it in**: the presence marker,
 * then generation, then whatever the person did. The marker has to be first or the
 * re-entry detection in 14b.4 cannot see the day it is about, and generation has to come
 * before the day's own events or a morning Pulse would be describing a day that had not
 * happened yet.
 */
internal class PulseMonth {

    val log = TrailTestLog()

    /** Every Pulse the month produced, in order. */
    val spoken = mutableListOf<PulseDecision.Speak>()

    /** What a second foreground on the same day, after the reflection switch, answered. */
    val secondCallsSameDay = mutableListOf<PulseDecision>()

    private var minted = 0

    private val generator = PulseGenerator(CorpusFixture.catalog, TEST_ZONE) { "pulse-${minted++}" }

    private val work = "area-work"

    private val home = "area-home"

    /** Two areas, one item that stays active all month, one that is finished and replaced. */
    fun seed(): PulseMonth {
        log.area(at(0, 6, 0), work, "Work")
        log.area(at(0, 6, 1), home, "Home", colorHex = "#3E9E6E", orderKey = "a1")
        log.item(at(0, 6, 2), "work-active", work, "Draft the letter")
        log.promote(at(0, 6, 3), "work-active", work, "Draft the letter")
        log.item(at(0, 6, 4), "home-0", home, "Home item 0", orderKey = "b0", areaName = "Home")
        log.promote(at(0, 6, 5), "home-0", home, "Home item 0", areaName = "Home")
        return this
    }

    /** The presence marker, written on the first foreground of a day. */
    fun opened(day: Int): PulseMonth {
        log.opened(day)
        return this
    }

    /** One decision, at [hour] on [day], against the log as it stands. */
    fun decide(day: Int, hour: Int = 8): PulseDecision =
        generator.decide(log.queries(), at(day, hour))

    /**
     * Runs [days] foregrounds, appending every Pulse that speaks the way the repository
     * would, so each later day sees a firing history that includes the earlier ones.
     */
    fun run(days: Int = 30): PulseMonth {
        seed()
        for (day in 0 until days) {
            opened(day)
            val decision = decide(day)
            if (decision is PulseDecision.Speak) {
                spoken += decision
                log.add(at(day, 8), decision.payload)
                secondCallsSameDay += decide(day, hour = 18)
            }
            act(day)
        }
        return this
    }

    /** The projection, folded from the log the run produced. */
    fun state(): ClarityState = ClarityReplay.replay(log.events())

    /** The simulated day a date key names. */
    fun dayOf(key: String): Int = (0 until DAY_SEARCH_LIMIT).first { dateKey(it) == key }

    /** One capture a day, and a completion in the other area every third day. */
    private fun act(day: Int) {
        if (day == 0) return
        log.item(at(day, 9, 0), "work-$day", work, "Work item $day", orderKey = "a$day")
        if (day % 3 != 0) return
        val finished = day / 3 - 1
        val next = day / 3
        log.complete(at(day, 10, 0), "home-$finished", home, "Home item $finished", areaName = "Home")
        log.item(at(day, 10, 1), "home-$next", home, "Home item $next", orderKey = "b$next", areaName = "Home")
        log.promote(at(day, 10, 2), "home-$next", home, "Home item $next", areaName = "Home")
    }

    private companion object {

        /** Wide enough for any run these tests make, and bounded so a miss fails rather than hangs. */
        const val DAY_SEARCH_LIMIT = 400
    }
}
