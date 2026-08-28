package com.kamsiob.claritynow.ui.reentry

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.dateKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * When the re-entry state appears, and the four ways it must not. MASTER_BUILD_PROMPT
 * 14b.4, design-v3.md 10.15 and 11.2.
 *
 * [offersReEntry] is the whole rule and this drives it, in two registers. The truth
 * table is the first three tests. The rest run it against a real log walked one day at
 * a time, because the assertion that matters is not "the expression is correct" but
 * "somebody living with this app sees this screen once per gap and on no other day",
 * and that is a statement about a sequence of opens rather than about a boolean.
 *
 * **Nothing here can see the composition.** The route is Compose and these tests run on
 * a desktop JVM, which is why the decision is a function rather than a branch inside
 * `FirstRunGate`. What that leaves untested here is the wiring, which is one call in
 * `ReEntryGate` and is checked on the device.
 */
class ReEntryRoutingTest {

    /**
     * `ClarityRepository.recordAppOpened`, reduced to the two lines that decide, exactly
     * as `ReEntryGapTest` writes it. At most once per calendar day, decided against the
     * log rather than against a remembered instant.
     */
    private fun openTheApp(log: TrailTestLog, day: Int) {
        val key = dateKey(day)
        if (log.queries().hasOpenedOn(key)) return
        log.add(at(day, 9), AppOpened(key))
    }

    /**
     * What a cold start on [day] decides, with [settledOn] standing for the stored
     * answer this device already gave. The marker for the day is written first, because
     * that is the order the app runs in and the order the whole detection depends on.
     */
    private fun offerOn(log: TrailTestLog, day: Int, settledOn: String?): Boolean {
        openTheApp(log, day)
        return offersReEntry(
            onboardingComplete = true,
            returnedOn = log.queries().reEntryOn(dateKey(day))?.returnedOn,
            settledOn = settledOn,
        )
    }

    // The rule itself ---------------------------------------------------------

    /**
     * **It does not apply before onboarding is complete.** design-v3.md 10.15 puts this
     * check last "so that it can never delay or replace a first run", and the detection
     * query has no way to know: an install that has been sitting on beat 2 for a
     * fortnight has genuine presence markers with a genuine gap between them.
     */
    @Test
    fun `an unfinished onboarding is never interrupted by this screen`() {
        assertFalse(offersReEntry(false, returnedOn = dateKey(14), settledOn = null))
    }

    /** No return, no screen. A day that is not the day of a return answers null. */
    @Test
    fun `a day that is not a return offers nothing`() {
        assertFalse(offersReEntry(true, returnedOn = null, settledOn = null))
    }

    /** The answer already given for this return, which is what a relaunch reads. */
    @Test
    fun `a return that has been answered is not offered again`() {
        assertTrue(offersReEntry(true, returnedOn = dateKey(14), settledOn = null))
        assertTrue(offersReEntry(true, returnedOn = dateKey(14), settledOn = dateKey(3)))
        assertFalse(offersReEntry(true, returnedOn = dateKey(14), settledOn = dateKey(14)))
    }

    // The rule against a log --------------------------------------------------

    /**
     * A person who opens the app every day is never shown this screen.
     *
     * The case that would be caught last if it were wrong, because the person it would
     * be wrong for is everybody who is fine.
     */
    @Test
    fun `daily use never offers the screen`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work")

        (0..60).forEach { day ->
            assertFalse("day $day", offerOn(log, day, settledOn = null))
        }
    }

    /**
     * The first open this app has ever had is not a return.
     *
     * There is no marker before it, and the two plausible defaults for a missing one
     * both cost somebody something: the same day makes every return a gap of zero and
     * the screen never appears, and the epoch greets every new install as somebody back
     * from decades away. `ReEntryGapTest` holds the query's half of this; here it is the
     * routing's, because a first run is exactly the moment 10.15 forbids this to reach.
     */
    @Test
    fun `a first ever open offers nothing`() {
        val log = TrailTestLog()
        assertFalse(offerOn(log, day = 0, settledOn = null))
    }

    /**
     * **At most once per calendar day, and in practice once per gap.** 14b.4.
     *
     * Two absences in one log, one of twenty days and one of forty, with ordinary daily
     * use in between and after. The screen is offered on the two days somebody came
     * back and on none of the other fifty nine, and the second gap offers again because
     * the answer stored is the date of the first return rather than a flag saying this
     * has happened once.
     */
    @Test
    fun `two absences offer the screen exactly twice, on the days of the returns`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work")

        var settledOn: String? = null
        val offered = mutableListOf<Int>()
        // Day 0, then away until day 20, then five ordinary days, then away until 60.
        val opens = listOf(0) + (20..25) + listOf(60, 61)
        opens.forEach { day ->
            if (offerOn(log, day, settledOn)) {
                offered += day
                settledOn = dateKey(day)
            }
        }

        assertEquals(listOf(20, 60), offered)
    }

    /**
     * The process dying before the offer was answered leaves it standing that day.
     *
     * This is the one case the stored answer exists for. Detection is true for the whole
     * of the calendar day of the return, so a relaunch inside that day asks again, and
     * what decides whether it is shown a second time is whether the person answered it
     * the first time. Both directions are asserted, because a stored answer that was
     * never read and one that is written too early fail in opposite ways and either
     * would pass a test of only one of them.
     */
    @Test
    fun `a relaunch on the day of the return shows it again only if it was not answered`() {
        val log = TrailTestLog()
        openTheApp(log, day = 0)

        assertTrue("the return itself", offerOn(log, day = 20, settledOn = null))
        assertTrue("relaunched, and nothing was answered", offerOn(log, day = 20, settledOn = null))
        assertFalse("relaunched after answering", offerOn(log, day = 20, settledOn = dateKey(20)))
        // And the day after the return is not a return, whatever was stored.
        assertFalse("the next day", offerOn(log, day = 21, settledOn = dateKey(20)))
        assertFalse("the next day, unanswered", offerOn(log, day = 21, settledOn = null))
    }

    /**
     * Thirteen days is not a gap and fourteen is, at the routing layer as well as at the
     * query. `ReEntry.MIN_GAP_DAYS` is the one place the number lives and `ReEntryGapTest`
     * walks the boundary; this asserts the surface inherits it rather than holding a
     * threshold of its own.
     */
    @Test
    fun `the boundary the query draws is the boundary the screen appears at`() {
        val thirteen = TrailTestLog()
        openTheApp(thirteen, day = 0)
        assertFalse(offerOn(thirteen, day = 13, settledOn = null))

        val fourteen = TrailTestLog()
        openTheApp(fourteen, day = 0)
        assertTrue(offerOn(fourteen, day = 14, settledOn = null))
    }
}
