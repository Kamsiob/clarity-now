package com.kamsiob.claritynow.ui.nav

/**
 * Whether the Focus surface is showing, and the memory that keeps back honest.
 *
 * design-v3.md 10.15, MASTER_BUILD_PROMPT section 10.
 *
 * **This is a value with no Compose, no Android and no coroutine in it, and that is
 * the point.** The one rule this phase is most likely to break is the rule that back
 * during a session navigates away and leaves the session running. The specification
 * says it twice because the obvious implementation ends the session, and the second
 * obvious implementation is subtler: a shell that shows this surface whenever a
 * session is running re-opens it on the very next frame, which is a back button that
 * does nothing. Both failures are one line each. Neither is visible in a screenshot.
 * They are decided here, where a unit test can hold them.
 *
 * **Nothing in this file writes anything, and there is no method on it that could.**
 * Leaving the surface is [left] and it records where the person went, never what
 * happened to their session. Ending a session is a deliberate act with a button on the
 * surface itself.
 *
 * The four ways in, all of them arriving as one of the three transitions below:
 *
 * | the person did this | the shell calls |
 * |---|---|
 * | tapped the Focus chip in the Areas header, 10.1 | [requested] |
 * | tapped the ongoing notification or the Live Update | [requested] |
 * | opened the app while a session was running, 10 | [sessionSeen] |
 * | let a session they had left run out of time | [sessionSeen] |
 */
internal data class FocusEntry(
    /** Whether the surface is showing right now. */
    val open: Boolean = false,
    /**
     * The session that was running when the person last left the surface, or null if
     * none was.
     *
     * This is the whole of what makes back work. A session that has been deliberately
     * left keeps running, keeps its ongoing notification and keeps its countdown on
     * the Areas card, and the surface stays away until the person asks for it again.
     */
    val leftSessionId: String? = null,
    /**
     * The session whose end has already been offered once, or null.
     *
     * Without it, a session left running whose time then runs out would re-open the
     * surface, be dismissed, re-open on the next emission, and trap somebody who
     * pressed back three times. It is offered once and then it is theirs to come back
     * to, which is the same promise the completion notification makes when the app is
     * elsewhere.
     */
    val elapsedOfferedFor: String? = null,
) {

    /**
     * The person asked for the Focus surface: the chip, or a notification.
     *
     * It clears nothing. A session they had left is a session they are now returning
     * to, and [leftSessionId] naming it costs nothing while the surface is open: every
     * transition that could act on it runs while the surface is closed.
     */
    fun requested(): FocusEntry = copy(open = true)

    /**
     * The person left the surface, by back or by finishing with it.
     *
     * [runningSessionId] is whatever is running at the moment they left, which is null
     * on the chooser and null after a completion has been resolved. **Nothing about a
     * session changes here.** design-v3.md 10.15: back navigates away and leaves the
     * session running, it does not end it, prompt or warn.
     */
    fun left(runningSessionId: String?): FocusEntry =
        copy(open = false, leftSessionId = runningSessionId)

    /**
     * What this device's running session says about which surface should be showing.
     *
     * Three answers, and the order of them is the specification:
     *
     * 1. **A session this device is running that the person has not walked away from
     *    opens the surface.** MASTER_BUILD_PROMPT 10: on relaunch during a running
     *    session the focus screen is restored. A cold start reaches this with an empty
     *    [FocusEntry], because nothing about having left is worth persisting across a
     *    process death: a person who force quits the app and opens it again is asking
     *    where their session got to
     * 2. **A session they did walk away from stays away.** design-v3.md 10.15
     * 3. **Except once, when its planned time runs out.** The completion state with
     *    `Mark item complete` and `Done` is owed to them either way, and if they are
     *    looking at the app there is nowhere else for it to be: section 10 posts the
     *    gentle notification only when the app is backgrounded, precisely so that the
     *    app never tells somebody something they are already looking at
     */
    fun sessionSeen(sessionId: String?, timeIsUp: Boolean): FocusEntry {
        if (sessionId == null) return this
        val walkedAwayFromThisOne = sessionId == leftSessionId
        val owesCompletion = timeIsUp && sessionId != elapsedOfferedFor
        if (walkedAwayFromThisOne && !owesCompletion) return this
        return copy(
            open = true,
            elapsedOfferedFor = if (timeIsUp) sessionId else elapsedOfferedFor,
        )
    }
}
