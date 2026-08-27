package com.kamsiob.claritynow.ui.areas

import com.kamsiob.claritynow.data.repo.FocusCountdown

/**
 * The Areas screen's half of a focus session, as two values and two pure functions.
 *
 * MASTER_BUILD_PROMPT section 10, design-v3.md 10.3.
 *
 * **Why these are here rather than inside [AreasViewModel].** Both of them answer a
 * question that has a right and a wrong answer at a given second, and neither of them
 * needs Room, DataStore, a clock or a Context to answer it. Held inside the ViewModel
 * they would be reachable only from an instrumented test, and "the card stops saying a
 * session is running the moment it stops running" is exactly the kind of statement
 * that has to be checked rather than reasoned about. This file has no `android.` and
 * no `androidx.` import and must not acquire one.
 *
 * **Nothing here draws a bar and there is nowhere for one to go.**
 * MASTER_BUILD_PROMPT 10 and design-v3.md 10.3 both say it in the same sentence they
 * specify the countdown in, because a remaining time and a planned total is exactly
 * the pair a progress bar is made from. The card is handed whole minutes and no
 * denominator, so the fraction a bar would need is not on this side of the boundary at
 * all.
 */

/**
 * Which area is in session, and how many whole minutes are left on it.
 *
 * One value rather than a map keyed by area, because there is one running session at a
 * time and the repository is what enforces that. A map would be a shape that could
 * hold two and would invite a screen to render both.
 */
internal data class FocusHighlight(val areaId: String, val minutesRemaining: Int)

/**
 * What the app shell needs to know about a running session, and nothing more.
 *
 * [timeIsUp] rather than a remaining count, because the shell's only two questions are
 * whether a session exists and whether its planned time has run out. A shell holding a
 * countdown would recompose the whole app once a second, which design-v3.md 8.2 item 7
 * rules out in the same sentence it puts the countdown on one ticker.
 */
internal data class FocusPresence(val sessionId: String, val timeIsUp: Boolean)

/**
 * The card's highlight for this tick, or null when there is nothing to show.
 *
 * Null in three cases and each of them is a rule rather than a guard:
 *
 * - [highlightEnabled] is off. `focusHighlightEnabled` is the setting
 *   MASTER_BUILD_PROMPT 10 names, and it governs the intensified wash and the
 *   countdown together, because they are one state and not two decorations
 * - no session is running on this device
 * - the planned time has run out. A session whose time is up is over, and a card
 *   reading `In focus, 0 minutes left` would be saying something false for as long as
 *   it took the person to come back and resolve it. The card returns to its ordinary
 *   active state and the item title, which is still true, is what it says
 */
internal fun focusHighlightFor(
    countdown: FocusCountdown?,
    highlightEnabled: Boolean,
): FocusHighlight? {
    if (!highlightEnabled) return null
    val running = countdown ?: return null
    if (running.hasElapsed) return null
    return FocusHighlight(
        areaId = running.areaId,
        minutesRemaining = focusMinutesLeft(running.remainingSeconds),
    )
}

/**
 * Whole minutes left, rounded **up**.
 *
 * A session with forty seconds to run reads one minute rather than zero, and the row
 * reaches zero only once the time is gone, at which point
 * [focusHighlightFor] has already stopped drawing it.
 *
 * Rounding up rather than to nearest is deliberate and matches the notification, which
 * rounds the same way for the same reason: the card and the shade are read seconds
 * apart, and one of them saying seven while the other says eight is how a person stops
 * trusting both. The completion screen and the Trail round to nearest instead, because
 * those report a session that is over and are not counting anything down.
 */
internal fun focusMinutesLeft(remainingSeconds: Int): Int = when {
    remainingSeconds <= 0 -> 0
    else -> (remainingSeconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
}

private const val SECONDS_PER_MINUTE = 60
