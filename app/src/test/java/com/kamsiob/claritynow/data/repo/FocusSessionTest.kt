package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.domain.query.TEST_ORIGIN
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules of a focus session, checked rather than reasoned about.
 *
 * Every statement here is one of the ones MASTER_BUILD_PROMPT 10 and Addendum 01
 * 4e, 4f and 4g make, and each is checked at the layer that decides it. The
 * repository above these functions holds Room and DataStore and cannot be reached
 * from a desktop JVM at all, which is why the decisions were put in
 * `FocusSession.kt` instead of inside it: the acceptance criterion "killing the app
 * mid session and relaunching restores the correct remaining time" is verified on
 * the device by `adb shell am force-stop`, and this is the half of it that can be
 * held still and read.
 */
class FocusSessionTest {

    private val areaId = "area-1"
    private val itemId = "item-1"
    private val sessionId = "session-1"
    private val startedAt = at(0, 9, 30)
    private val twentyFiveMinutes = 1_500

    /**
     * A person with one area, one active item, and a session started at 9:30.
     *
     * Built as a log rather than as a state literal on purpose. Restoring a session
     * after the process is killed is exactly a replay of this log, so a fixture that
     * skipped the fold would be testing an arrangement that never happens.
     */
    private fun focusLog(plannedSeconds: Int = twentyFiveMinutes): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 9, 0), areaId, "Work")
        log.item(at(0, 9, 0), itemId, areaId, "Draft the letter")
        log.promote(at(0, 9, 1), itemId, areaId, "Draft the letter")
        log.add(
            startedAt,
            FocusStarted(
                sessionId = sessionId,
                areaId = areaId,
                itemId = itemId,
                plannedSeconds = plannedSeconds,
            ),
        )
        return log
    }

    /** What a cold start does: rebuild the projection from the log and nothing else. */
    private fun coldStart(log: TrailTestLog): ClarityState = ClarityReplay.replay(log.events())

    private fun runningSession(state: ClarityState): FocusSessionState =
        state.focusSessions.getValue(sessionId)

    /** The origin of every `FOCUS_STARTED` in a log, which is the fallback's input. */
    private fun startedBy(events: List<ClarityEvent>): (String) -> String? {
        val byId = events
            .mapNotNull { event ->
                val payload = event.payload
                if (payload is FocusStarted) payload.sessionId to event.originId else null
            }
            .toMap()
        return { byId[it] }
    }

    // Surviving process death -------------------------------------------------

    @Test
    fun `a session survives process death and restores the right remaining time`() {
        val log = focusLog()
        // The process is killed here. Nothing in memory survives; the log does.
        val restarted = coldStart(log)
        val session = pickDeviceSession(restarted, sessionId, TEST_ORIGIN) { null }

        val restore = focusRestoreFor(session, at(0, 9, 40))

        assertTrue("a running session must restore the focus screen", restore is FocusRestore.Running)
        val countdown = (restore as FocusRestore.Running).countdown
        assertEquals("ten minutes in, fifteen are left", 900, countdown.remainingSeconds)
        assertEquals(0.6f, countdown.fractionRemaining, 0.0001f)
        assertEquals(startedAt + 1_500_000L, countdown.endsAtMillis)
    }

    @Test
    fun `the restored session is found from the log when the stored handle is gone`() {
        val log = focusLog()
        val restarted = coldStart(log)

        // No handle at all: the preference was cleared, corrupted or never written.
        val session = pickDeviceSession(restarted, null, TEST_ORIGIN, startedBy(log.events()))

        assertEquals(
            "the log is the truth and the stored instant is a cache of it",
            sessionId,
            session?.id,
        )
    }

    @Test
    fun `a running session another device started is never picked up here`() {
        val log = focusLog()
        val restarted = coldStart(log)

        val session = pickDeviceSession(restarted, null, "device-b", startedBy(log.events()))

        assertNull(
            "a merged log can hold one running session per device and each phone " +
                "shows its own",
            session,
        )
    }

    @Test
    fun `a session whose planned time ran out while the process was dead completes`() {
        val log = focusLog()
        val restarted = coldStart(log)
        val session = pickDeviceSession(restarted, sessionId, TEST_ORIGIN) { null }

        // Opened again the next morning, long after the session's time ran out.
        val restore = focusRestoreFor(session, at(1, 9, 0))

        assertTrue(restore is FocusRestore.Completed)
        assertEquals(
            "the duration recorded is the time it was planned to run, never the gap " +
                "since it started, because the gap is a fact about the phone",
            twentyFiveMinutes,
            (restore as FocusRestore.Completed).session.plannedSeconds,
        )
    }

    @Test
    fun `a session that already ended restores nothing`() {
        val restarted = coldStart(focusLog())
        val ended = runningSession(restarted).copy(outcome = FocusOutcome.ENDED_EARLY)

        assertEquals(FocusRestore.None, focusRestoreFor(ended, at(0, 9, 40)))
        assertEquals(FocusRestore.None, focusRestoreFor(null, at(0, 9, 40)))
    }

    // Extensions --------------------------------------------------------------

    @Test
    fun `two extensions accumulate and the folded planned duration is the newest value`() {
        val log = focusLog()
        log.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
        log.add(at(0, 9, 50), FocusExtended(sessionId, 600, 2_700))

        val session = runningSession(coldStart(log))

        assertEquals("forty five minutes, not twenty five", 2_700, session.plannedSeconds)
        assertEquals(startedAt + 2_700_000L, session.plannedEndsAt)
    }

    @Test
    fun `an extension grows the arc rather than resetting it`() {
        val once = focusLog().also { it.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100)) }
        val twice = focusLog().also {
            it.add(at(0, 9, 40), FocusExtended(sessionId, 600, 2_100))
            it.add(at(0, 9, 50), FocusExtended(sessionId, 600, 2_700))
        }
        val askedAt = at(0, 9, 50)

        val before = runningSession(coldStart(once)).countdownAt(askedAt)
        val after = runningSession(coldStart(twice)).countdownAt(askedAt)

        assertEquals(900, before.remainingSeconds)
        assertEquals(1_500, after.remainingSeconds)
        assertTrue(
            "the remaining arc must be longer after the extension, and it must be " +
                "the same session rather than a second one",
            after.fractionRemaining > before.fractionRemaining,
        )
        assertEquals(before.sessionId, after.sessionId)
        assertEquals(before.startedAt, after.startedAt)
        assertFalse("a full arc would mean the session had restarted", after.fractionRemaining == 1f)
    }

    // One session at a time ---------------------------------------------------

    @Test
    fun `only one session runs at a time`() {
        val state = coldStart(focusLog())
        val running = runningSession(state)

        assertFalse(
            "a second session may not start while this device has one running",
            canStartFocus(state, areaId, itemId, twentyFiveMinutes, running),
        )
        assertTrue(canStartFocus(state, areaId, itemId, twentyFiveMinutes, deviceSession = null))
    }

    @Test
    fun `only an area with an active item can start a session`() {
        val log = TrailTestLog()
        log.area(at(0, 9, 0), areaId, "Work")
        log.item(at(0, 9, 0), itemId, areaId, "Draft the letter")
        val queuedOnly = ClarityReplay.replay(log.events())

        assertNull("the fixture is meant to have nothing active", queuedOnly.activeItemIn(areaId))
        assertFalse(canStartFocus(queuedOnly, areaId, itemId, twentyFiveMinutes, null))
        assertFalse(canStartFocus(queuedOnly, "no-such-area", itemId, twentyFiveMinutes, null))
    }

    @Test
    fun `a session cannot start on an item that is not the active one`() {
        val log = focusLog()
        log.item(at(0, 9, 2), "item-2", areaId, "Something queued", orderKey = "a1")
        val state = ClarityReplay.replay(log.events())

        assertFalse(canStartFocus(state, areaId, "item-2", twentyFiveMinutes, null))
    }

    @Test
    fun `a session cannot start with no duration`() {
        val state = coldStart(focusLog())
        assertFalse(canStartFocus(state, areaId, itemId, 0, null))
        assertFalse(canStartFocus(state, areaId, itemId, -60, null))
    }

    // Ending ------------------------------------------------------------------

    @Test
    fun `an ending under sixty seconds is silent and one over it is a completed short session`() {
        assertTrue(focusEndingIsSilent(0))
        assertTrue(focusEndingIsSilent(59))
        assertFalse("sixty seconds is a session", focusEndingIsSilent(60))
        assertFalse("fourteen minutes is fourteen minutes", focusEndingIsSilent(14 * 60))
    }

    @Test
    fun `the elapsed seconds an ending records are a real duration`() {
        val session = runningSession(coldStart(focusLog()))

        assertEquals(0, session.countdownAt(startedAt).elapsedSeconds)
        assertEquals(45, session.countdownAt(startedAt + 45_000L).elapsedSeconds)
        assertEquals(14 * 60, session.countdownAt(startedAt + 840_000L).elapsedSeconds)
        assertEquals(
            "elapsed is never more than the plan, so it can never read as an overrun",
            twentyFiveMinutes,
            session.countdownAt(startedAt + 9_000_000L).elapsedSeconds,
        )
    }

    // The numeral and the arc -------------------------------------------------

    @Test
    fun `the numeral holds its opening value for a whole second`() {
        val session = runningSession(coldStart(focusLog()))

        assertEquals(twentyFiveMinutes, session.countdownAt(startedAt).remainingSeconds)
        assertEquals(twentyFiveMinutes, session.countdownAt(startedAt + 1L).remainingSeconds)
        assertEquals(twentyFiveMinutes, session.countdownAt(startedAt + 999L).remainingSeconds)
        assertEquals(twentyFiveMinutes - 1, session.countdownAt(startedAt + 1_000L).remainingSeconds)
    }

    @Test
    fun `the countdown floors at zero and reports that the plan is spent`() {
        val session = runningSession(coldStart(focusLog()))

        val atTheEnd = session.countdownAt(startedAt + 1_500_000L)
        assertEquals(0, atTheEnd.remainingSeconds)
        assertEquals(0f, atTheEnd.fractionRemaining, 0f)
        assertTrue(atTheEnd.hasElapsed)

        val longAfter = session.countdownAt(startedAt + 90_000_000L)
        assertEquals(0, longAfter.remainingSeconds)
        assertTrue(longAfter.hasElapsed)
    }

    @Test
    fun `the arc is full before the first tick`() {
        val session = runningSession(coldStart(focusLog()))
        val opening = session.countdownAt(startedAt)

        assertEquals(1f, opening.fractionRemaining, 0f)
        assertFalse(opening.hasElapsed)
    }

    // The transition warning mark ---------------------------------------------

    @Test
    fun `a session with five minutes or less carries no transition mark`() {
        val fiveMinutes = runningSession(coldStart(focusLog(plannedSeconds = 300)))
        val threeMinutes = runningSession(coldStart(focusLog(plannedSeconds = 180)))

        assertNull(
            "a mark that would fire the moment the session starts teaches a person " +
                "to distrust it",
            fiveMinutes.countdownAt(startedAt).transitionMarkFraction,
        )
        assertNull(threeMinutes.countdownAt(startedAt).transitionMarkFraction)
        assertFalse(fiveMinutes.countdownAt(startedAt).pastTransitionMark)
    }

    @Test
    fun `the transition mark sits five minutes from the end and is reached once`() {
        val session = runningSession(coldStart(focusLog()))

        val opening = session.countdownAt(startedAt)
        assertEquals(0.2f, opening.transitionMarkFraction!!, 0.0001f)
        assertFalse("the mark is visible from the start but not reached", opening.pastTransitionMark)

        assertFalse(session.countdownAt(startedAt + 1_199_000L).pastTransitionMark)
        assertTrue(session.countdownAt(startedAt + 1_200_000L).pastTransitionMark)
        assertTrue(session.countdownAt(startedAt + 1_400_000L).pastTransitionMark)
    }

    // The ticker --------------------------------------------------------------

    // The two below read the test scheduler's virtual clock as the ticker's clock,
    // so one second of `delay` is one second of wall time to the flow under test and
    // the suite does not actually wait. `currentTime` is opt in, and the opt in is
    // scoped to these two functions rather than to the class so that nothing else
    // here quietly acquires an experimental dependency.

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `the ticker emits once a second`() = runTest {
        val ticks = secondTicks { testScheduler.currentTime }.take(4).toList()

        assertEquals(listOf(0L, 1_000L, 2_000L, 3_000L), ticks)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a ticker started off the second boundary aligns to it and does not drift`() = runTest {
        val offsetIntoTheSecond = 250L

        val ticks = secondTicks { testScheduler.currentTime + offsetIntoTheSecond }.take(4).toList()

        assertEquals(
            "the first emission is immediate and every later one lands on the second, " +
                "so an hour long session is still on the second at the end of it",
            listOf(250L, 1_000L, 2_000L, 3_000L),
            ticks,
        )
    }
}
