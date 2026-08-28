package com.kamsiob.claritynow.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kamsiob.claritynow.MainActivity

/**
 * The one intent the Pulse reminder can send, and the predicate that receives it.
 *
 * Written as a contract in one file for the same reason [FocusIntents] is: the other
 * side of it is an Activity this package cannot see, and MASTER_BUILD_PROMPT 13.4's
 * "every notification deep links correctly" is a defect that is invisible from either
 * side alone.
 *
 * `MainActivity` is `singleTask`, so this arrives at a live process through
 * `onNewIntent` and at a dead one through `onCreate`, and [opensPulse] answers both the
 * same way.
 *
 * **It carries no date key and no entry id**, which is deliberate. The reminder is
 * posted for the day it fires on, and by the time somebody taps it that may be the
 * previous day: a notification tapped at 00:10 that opened a surface pinned to
 * yesterday would be showing a person a question about a day that has ended. The Pulse
 * surface opens on whatever day it is when it opens, which is what
 * MASTER_BUILD_PROMPT 13.5 asks of the shortcut beside it, and it opens in whatever
 * state that day is in, including the ambient state of a silent one.
 */
object PulseIntents {

    /** Open the Pulse surface. The body of the daily reminder, MASTER_BUILD_PROMPT 12.1. */
    const val ACTION_OPEN_PULSE: String = "com.kamsiob.claritynow.action.OPEN_PULSE"

    /**
     * True when [intent] is the reminder asking for the Pulse surface.
     *
     * The routing itself lives in `ui/nav/ExternalRequest.kt`, which reads the action
     * string, and `MainActivity` notes the request from both `onCreate` and
     * `onNewIntent`. This predicate is the readable statement of the contract rather
     * than the caller, and a test asserts that every `ACTION_` constant in this file is
     * routed somewhere. There is no second way for a notification to reach a screen in
     * this app.
     */
    fun opensPulse(intent: Intent?): Boolean = intent?.action == ACTION_OPEN_PULSE

    internal fun openPulse(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_OPEN_PULSE)
            // SINGLE_TOP beside the manifest's singleTask so that tapping this while
            // the app is already open delivers a new intent rather than rebuilding the
            // task. NEW_TASK because the sender is a notification and not an Activity.
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN_PULSE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * Distinct from every code in [FocusIntents], which uses 1 to 4.
     *
     * Two PendingIntents with the same request code and the same component collide
     * unless their intents differ by `filterEquals`, which action alone already
     * satisfies. Kept distinct anyway, because the day one of these grows an extra it
     * is the collision nobody looks for.
     */
    private const val REQUEST_OPEN_PULSE = 5
}
