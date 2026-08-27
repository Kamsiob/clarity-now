package com.kamsiob.claritynow.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Which surface the shell is showing, and above all what back does to a running
 * session. design-v3.md 10.15, MASTER_BUILD_PROMPT section 10, issue #2.
 *
 * **The rule this file exists for is the one the specification states twice.** Back
 * during a focus session navigates away and leaves the session running: it does not
 * end it, prompt or warn. Issue #2 lists it under Risks with the reason, which is that
 * ending the session as a side effect of navigation is the obvious implementation.
 * There is a second, quieter version of the same failure, and it is checked here too:
 * a shell that shows the surface whenever a session is running puts it straight back on
 * screen, which is a back button that appears to do nothing.
 *
 * Neither failure is visible in a screenshot and neither one crashes, which is why
 * `FocusEntry` is a plain value with no Compose in it and why this file is a unit test
 * rather than a note in the verification checklist.
 */
class FocusEntryTest {

    private val session = "session-1"
    private val nextSession = "session-2"

    // ---------------------------------------------------------------------------
    // Back. design-v3.md 10.15.
    // ---------------------------------------------------------------------------

    /**
     * The whole rule, in the order a person performs it: a session is running, the
     * surface is showing, they press back, and the app keeps showing them the Areas
     * screen for as long as the session runs.
     */
    @Test
    fun `back leaves the surface and the session keeps running behind it`() {
        var entry = FocusEntry().sessionSeen(session, timeIsUp = false)
        assertTrue("a running session opens the surface", entry.open)

        entry = entry.left(session)
        assertFalse("back closes the surface", entry.open)
        assertEquals(
            "and records only that the person went elsewhere",
            session,
            entry.leftSessionId,
        )

        // Every second of the rest of the session.
        repeat(600) { entry = entry.sessionSeen(session, timeIsUp = false) }
        assertFalse("the surface stays away for the whole session", entry.open)
    }

    /**
     * The way back in, which is what makes leaving safe: the Focus chip in the Areas
     * header and the ongoing notification both reach the same running session.
     */
    @Test
    fun `the chip and the notification both reopen a session that was left`() {
        val left = FocusEntry().sessionSeen(session, timeIsUp = false).left(session)
        assertTrue("the chip", left.requested().open)
        assertTrue("a notification tap", left.requested().open)
    }

    // ---------------------------------------------------------------------------
    // Restoring a session. MASTER_BUILD_PROMPT section 10.
    // ---------------------------------------------------------------------------

    /**
     * "On relaunch during a running session the focus screen is restored." A cold start
     * arrives here with a fresh value, because nothing about having walked away is
     * persisted across a process death.
     */
    @Test
    fun `a cold start during a running session opens the surface`() {
        val entry = FocusEntry().sessionSeen(session, timeIsUp = false)
        assertTrue(entry.open)
    }

    /** No session, no surface. The ordinary state of the app. */
    @Test
    fun `no running session leaves the shell alone`() {
        val entry = FocusEntry().sessionSeen(sessionId = null, timeIsUp = false)
        assertFalse(entry.open)
        assertEquals(FocusEntry(), entry)
    }

    /**
     * Walking away from one session says nothing about the next one. The person starts
     * another session later that day and it opens normally.
     */
    @Test
    fun `leaving one session does not suppress the next one`() {
        val entry = FocusEntry()
            .sessionSeen(session, timeIsUp = false)
            .left(session)
            .sessionSeen(nextSession, timeIsUp = false)
        assertTrue(entry.open)
    }

    // ---------------------------------------------------------------------------
    // The end of a session somebody had left. Section 10 and Addendum 01 4e.
    // ---------------------------------------------------------------------------

    /**
     * The completion state is owed to the person however they were sitting when the
     * time ran out. Section 10 posts the gentle notification only when the app is
     * backgrounded, so with the app in front of them there is nowhere else for it to
     * be.
     */
    @Test
    fun `a session that runs out while the person is elsewhere in the app is offered once`() {
        var entry = FocusEntry().sessionSeen(session, timeIsUp = false).left(session)

        entry = entry.sessionSeen(session, timeIsUp = true)
        assertTrue("the completion is offered", entry.open)

        // They press back on it rather than resolving it, and the session is still
        // sitting there unresolved. It must not be pushed at them again.
        entry = entry.left(session)
        repeat(60) { entry = entry.sessionSeen(session, timeIsUp = true) }
        assertFalse("and never a second time", entry.open)
    }

    /**
     * The same session running out while somebody is watching the ring changes nothing
     * about which surface is showing. It is already the right one.
     */
    @Test
    fun `time running out while the surface is open leaves it open`() {
        val entry = FocusEntry()
            .sessionSeen(session, timeIsUp = false)
            .sessionSeen(session, timeIsUp = true)
        assertTrue(entry.open)
    }

    /**
     * A new session gets its own single offer. The memory is per session and not a flag
     * that is spent for the life of the process.
     */
    @Test
    fun `each session is offered its own ending`() {
        var entry = FocusEntry()
            .sessionSeen(session, timeIsUp = false)
            .left(session)
            .sessionSeen(session, timeIsUp = true)
            .left(session)

        entry = entry.sessionSeen(nextSession, timeIsUp = false).left(nextSession)
        assertFalse(entry.open)
        entry = entry.sessionSeen(nextSession, timeIsUp = true)
        assertTrue(entry.open)
    }

    // ---------------------------------------------------------------------------
    // Leaving with nothing running.
    // ---------------------------------------------------------------------------

    /**
     * Backing out of the chooser, and finishing with the completion screen, both leave
     * with no session running. Neither is a session the person walked away from.
     */
    @Test
    fun `leaving with no session running remembers nothing`() {
        val entry = FocusEntry().requested().left(runningSessionId = null)
        assertFalse(entry.open)
        assertEquals(null, entry.leftSessionId)
        assertTrue("and the next session still opens", entry.sessionSeen(session, false).open)
    }
}
