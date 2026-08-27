package com.kamsiob.claritynow.ui.momentum

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Areas banner's rate limit. `CLARITY_LOGIC_ENGINE.md` 6.5 and
 * `MASTER_BUILD_PROMPT.md` 11.2: at most once per hour of app use, throttled in the
 * ViewModel and not in the engine.
 *
 * Issue #5 names the failure this file exists for: "letting the banner recompute on every
 * recomposition rather than once per hour". None of that is visible in a screenshot, and
 * the shape it would take is a comparison buried in a ViewModel that nothing walks a clock
 * across.
 */
class BannerThrottleTest {

    private val hour = BannerThrottle.ONE_HOUR_MILLIS

    @Test
    fun `the first ask is always due`() {
        assertTrue(BannerThrottle().isDue(nowMillis = 0L))
    }

    @Test
    fun `a second ask inside the hour is refused`() {
        val throttle = BannerThrottle()
        throttle.recordAt(START)

        assertFalse("one millisecond later", throttle.isDue(START + 1L))
        assertFalse("a minute later", throttle.isDue(START + 60_000L))
        assertFalse(
            "and one millisecond short of the hour, which is the boundary a recomposition " +
                "storm would sit inside",
            throttle.isDue(START + hour - 1L),
        )
    }

    @Test
    fun `the hour itself is due`() {
        val throttle = BannerThrottle()
        throttle.recordAt(START)

        assertTrue("at most once per hour, so the hour mark is the next one", throttle.isDue(START + hour))
        assertTrue(throttle.isDue(START + hour * 3))
    }

    @Test
    fun `each recomputation starts the hour again`() {
        val throttle = BannerThrottle()
        throttle.recordAt(START)
        throttle.recordAt(START + hour)

        assertFalse(throttle.isDue(START + hour + 1L))
        assertTrue(throttle.isDue(START + hour * 2))
    }

    @Test
    fun `a clock corrected backwards does not freeze the banner`() {
        // A manual correction, or a zone change that moves the wall clock back an hour.
        // Treating a negative interval as "not due" would leave a stale sentence up for as
        // long as it took the clock to catch up, on the screen a person opens most often.
        val throttle = BannerThrottle()
        throttle.recordAt(START)

        assertTrue(throttle.isDue(START - hour))
    }

    private companion object {

        /** An arbitrary instant well clear of zero, so a subtraction cannot pass by accident. */
        const val START = 1_800_000_000_000L
    }
}
