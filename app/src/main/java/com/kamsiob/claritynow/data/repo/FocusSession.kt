package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The arithmetic and the rules of a focus session, with nothing Android in them.
 *
 * MASTER_BUILD_PROMPT 10, 14b.5 and 14b.6, design-v3.md 10.18 and 11.
 *
 * **Why this is a file of its own rather than more of [ClarityRepository].** Every
 * decision below is a pure function of the projection, the wall clock instant and
 * one stored session id, and none of them needs Room, DataStore or a Context. Held
 * inside the repository they would be reachable only by an instrumented test, and
 * "a session survives process death with the right remaining time" is exactly the
 * kind of statement that has to be checked by a test rather than by reasoning. This
 * file has no `android.` or `androidx.` import and must not acquire one. It is not
 * in `domain`, because `domain.replay` and `domain.query` fold and read the log and
 * this does neither: it answers what one device should show and write right now,
 * which is a data layer question.
 *
 * **Nothing here editorializes.** A session that ended before its planned time is a
 * shorter session and not a worse one, and there is no field, name or branch below
 * that says otherwise. Addendum 01 4e.
 */

/** One second. The countdown redraws at this rate and no faster. design-v3.md 8.2 item 7. */
const val FOCUS_TICK_MILLIS: Long = 1_000L

/**
 * Under this many seconds an ending is a mis-tap rather than a short session, so it
 * is discarded silently: no confirm before it, no completion screen after it. It is
 * still written to the log as `FOCUS_ENDED_EARLY`, because the log records what
 * happened rather than what was worth showing. MASTER_BUILD_PROMPT 10.
 */
const val FOCUS_DISCARD_UNDER_SECONDS: Int = 60

/** What `Add 10 minutes` adds. Repeatable and uncapped. Addendum 01 4f. */
const val FOCUS_EXTENSION_SECONDS: Int = 600

/**
 * How far out the optional transition warning sits. design-v3.md 10.18.
 *
 * The mark is on the ring track from the moment the session starts, so the warning
 * is a landmark that was already there rather than an event that arrives.
 */
const val FOCUS_TRANSITION_WARNING_SECONDS: Int = 300

/**
 * The instant this session's planned time runs out.
 *
 * Derived from the start and the **folded** planned duration, so an extension moves
 * it and a replay of two extensions arrives at the same instant the person was
 * shown. MASTER_BUILD_PROMPT 14b.5.
 */
val FocusSessionState.plannedEndsAt: Long
    get() = startedAt + plannedSeconds * 1000L

/**
 * Everything the three surfaces that show a running session need, computed once.
 *
 * The focus screen, the ongoing notification and the Live Update all read one of
 * these rather than each deriving remaining time from the clock, because three
 * derivations drift and a notification that disagrees with the screen by a second
 * is a notification a person stops trusting.
 *
 * **[fractionRemaining] is the primary figure and [remainingSeconds] is the
 * secondary one.** Addendum 01 8d: duration reads as a shape before it reads as a
 * number, so the depleting arc carries the session and the digits confirm it. The
 * order of the fields is not the order of the hierarchy, and this sentence is here
 * so that nobody reads it as one.
 */
data class FocusCountdown(
    val sessionId: String,
    val areaId: String,
    val itemId: String,
    val startedAt: Long,
    /** The folded planned duration, which an extension moves. */
    val plannedSeconds: Int,
    val endsAtMillis: Long,
    val remainingSeconds: Int,
) {

    /**
     * How much of the ring is still filled, 1.0 at the start and 0.0 at the end.
     *
     * Stepped once a second rather than computed per frame, because the ring is
     * specified to deplete at 1Hz from one ticker. An extension moves this upward,
     * and design-v3.md 8.2 item 28 has the arc grow to the new length rather than
     * jump, which is an animation over this value and not a second source of it.
     */
    val fractionRemaining: Float
        get() = if (plannedSeconds <= 0) 0f else remainingSeconds.toFloat() / plannedSeconds

    /**
     * Seconds run so far, and the value to pass as `actualSeconds` when a person
     * ends the session. It is a real duration, never a shortfall against the plan.
     */
    val elapsedSeconds: Int get() = plannedSeconds - remainingSeconds

    /** True once the planned time is gone. The completion path, not a failure. */
    val hasElapsed: Boolean get() = remainingSeconds <= 0

    /**
     * Where the transition warning tick sits on the ring track, or null when this
     * session is too short to carry one.
     *
     * Null rather than zero on a short session is the rule in design-v3.md 10.18
     * that the warning never fires when fewer than five minutes remained when the
     * session began, expressed as a value the ring cannot draw rather than as a
     * condition each of the three surfaces has to remember.
     */
    val transitionMarkFraction: Float?
        get() = if (plannedSeconds > FOCUS_TRANSITION_WARNING_SECONDS) {
            FOCUS_TRANSITION_WARNING_SECONDS.toFloat() / plannedSeconds
        } else {
            null
        }

    /** True once the arc has reached the mark, on a session that has one. */
    val pastTransitionMark: Boolean
        get() = transitionMarkFraction != null &&
            remainingSeconds <= FOCUS_TRANSITION_WARNING_SECONDS
}

/**
 * The countdown for this session at [nowMillis].
 *
 * Remaining seconds round **up**, so a twenty five minute session reads 25:00 for
 * the whole of its first second and reaches 24:59 exactly one second in. Rounding
 * down instead would drop a digit in the first millisecond and make the numeral
 * look like it had started late.
 */
fun FocusSessionState.countdownAt(nowMillis: Long): FocusCountdown {
    val endsAt = plannedEndsAt
    val remaining = wholeSecondsUp(endsAt - nowMillis).coerceIn(0, plannedSeconds.coerceAtLeast(0))
    return FocusCountdown(
        sessionId = id,
        areaId = areaId,
        itemId = itemId,
        startedAt = startedAt,
        plannedSeconds = plannedSeconds,
        endsAtMillis = endsAt,
        remainingSeconds = remaining,
    )
}

private fun wholeSecondsUp(millis: Long): Int {
    if (millis <= 0L) return 0
    val seconds = (millis + FOCUS_TICK_MILLIS - 1L) / FOCUS_TICK_MILLIS
    return seconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

/**
 * What a cold start or a resume should do with the session this device was running.
 *
 * The three cases are the whole of the answer to MASTER_BUILD_PROMPT 10's "on
 * relaunch during a running session the focus screen is restored" and "if
 * backgrounded at completion, resolve on next resume".
 */
sealed interface FocusRestore {

    /** This device has no running session. The ordinary route stands. */
    data object None : FocusRestore

    /** Still running. Restore the focus screen at this countdown. */
    data class Running(val countdown: FocusCountdown) : FocusRestore

    /**
     * The planned time ran out while the app was away, so the session finished and
     * the completion state is what a person should see.
     *
     * [session] carries the resolved session once [ClarityRepository.restoreFocus]
     * has written its `FOCUS_COMPLETED`; [focusRestoreFor], which only decides the
     * branch, returns the session as it stood before that write.
     */
    data class Completed(val session: FocusSessionState) : FocusRestore
}

/**
 * The restore decision, given the session this device was running and the instant
 * it is being asked at.
 *
 * A session whose planned time has passed completes with `actualSeconds` equal to
 * its **planned** seconds rather than the wall clock gap since it started. The
 * process may have been dead for an hour; the session still ran for the time it was
 * planned to run and not a second more, and the gap is a fact about the phone.
 */
fun focusRestoreFor(session: FocusSessionState?, nowMillis: Long): FocusRestore = when {
    session == null || session.outcome != FocusOutcome.RUNNING -> FocusRestore.None
    nowMillis < session.plannedEndsAt -> FocusRestore.Running(session.countdownAt(nowMillis))
    else -> FocusRestore.Completed(session)
}

/**
 * Which running session belongs to this device.
 *
 * Two answers, in order, and the order is what makes the stored end timestamp a
 * cache rather than a second source of truth.
 *
 * 1. The session id this device has stored, if it is still running. One map lookup,
 *    and the ordinary path.
 * 2. Failing that, the newest running session whose `FOCUS_STARTED` this device
 *    wrote, which [startedBy] answers from the log. This is what a lost or never
 *    written preference falls back to, and it is why nothing is unrecoverable if
 *    the preference and the log ever disagree: the log wins.
 *
 * A running session another device started is never picked up by either branch. A
 * merged log can legitimately hold two, one per device, and each phone shows its
 * own. A session left RUNNING forever by a killed process is a legal state that
 * nothing may infer an ending from, so this returns it rather than treating it as
 * stale, and [focusRestoreFor] decides what that means.
 */
fun pickDeviceSession(
    state: ClarityState,
    handleSessionId: String?,
    deviceOriginId: String,
    startedBy: (sessionId: String) -> String?,
): FocusSessionState? {
    val running = state.focusSessions.values.filter { it.outcome == FocusOutcome.RUNNING }
    if (running.isEmpty()) return null
    if (handleSessionId != null) {
        running.firstOrNull { it.id == handleSessionId }?.let { return it }
    }
    return running
        .filter { startedBy(it.id) == deviceOriginId }
        .maxWithOrNull(compareBy({ it.startedAt }, { it.lastEventLamport }, { it.id }))
}

/**
 * Whether a session may start. Enforced in the write path, never in a screen.
 *
 * Three conditions, and the middle one is the app wide rule that there is one
 * running session at a time. It lives here rather than in the chooser because a
 * chooser is not the only way in: a notification action, a shortcut and a widget
 * all reach the same repository, and a rule enforced in one screen is a rule.
 *
 * An area can only start a session on its own active item, which is what
 * MASTER_BUILD_PROMPT 10 means by an area without an active item being unselectable.
 * The chooser dims those areas; this refuses them.
 */
fun canStartFocus(
    state: ClarityState,
    areaId: String,
    itemId: String,
    plannedSeconds: Int,
    deviceSession: FocusSessionState?,
): Boolean = plannedSeconds > 0 &&
    deviceSession == null &&
    state.activeItemIn(areaId)?.id == itemId

/**
 * Whether an ending is shown to the person, or discarded without a word.
 *
 * **This decides what the interface does, never what is written.** Both endings
 * write `FOCUS_ENDED_EARLY`, because the log records what happened. What changes
 * under [FOCUS_DISCARD_UNDER_SECONDS] is that there is no confirm before it and no
 * completion screen after it, since a session forty seconds old is a mis-tap and
 * not a short session a person meant to have.
 *
 * Above the threshold the ending is a **completed short session** and reaches the
 * completion screen in the same words a full one does, with the same actions and no
 * qualifier. Addendum 01 4e: fourteen minutes is fourteen minutes.
 */
fun focusEndingIsSilent(actualSeconds: Int): Boolean =
    actualSeconds < FOCUS_DISCARD_UNDER_SECONDS

/**
 * The one ticker. design-v3.md 8.2 item 7.
 *
 * Emits the wall clock instant once a second, aligned to the second boundary so the
 * numeral changes when the clock does rather than a fraction of a second after the
 * session happened to start. The delay is recomputed from the instant just read, so
 * the emissions cannot drift the way a fixed `delay(1000)` loop does over an hour.
 *
 * **It is a `Flow` and not a timer, and there is exactly one of it in the process.**
 * [ClarityRepository] shares it, so the focus screen, the ongoing notification and
 * the Live Update read the same emissions rather than each running a scheduler and
 * disagreeing by up to a second. Nothing here animates: only the numeral and the
 * arc redraw on a tick, and everything else on the surface is still.
 */
fun secondTicks(nowMillis: () -> Long): Flow<Long> = flow {
    while (true) {
        val now = nowMillis()
        emit(now)
        delay(FOCUS_TICK_MILLIS - now.mod(FOCUS_TICK_MILLIS))
    }
}
