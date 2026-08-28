package com.kamsiob.claritynow.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kamsiob.claritynow.MainActivity

/**
 * Every intent a focus notification can send, in one file.
 *
 * **This is a contract between two packages and it is written down here because the
 * other side of it is a screen this file cannot see.** A notification that opens the
 * wrong place is the defect MASTER_BUILD_PROMPT 13.4 means by "every notification
 * deep links correctly", and it is not visible from either side alone.
 *
 * `MainActivity` is `singleTask`, so an intent from a notification arrives at a live
 * process through `onNewIntent` and at a dead one through `onCreate`. Both are handled,
 * and phase 12's routing pass moved the branch itself into `ui/nav/ExternalRequest.kt`,
 * which reads the action string rather than calling [opensFocusSession]. The predicate
 * stays because it is the readable statement of the contract and because a test asserts
 * every `ACTION_` constant in this file is routed somewhere. It is not the caller.
 */
object FocusIntents {

    /**
     * Open the running focus session. The body of the running notification and of
     * the Live Update, per Addendum 01 5c, and the body of the completion
     * notification, which resolves the session per MASTER_BUILD_PROMPT 10.
     */
    const val ACTION_OPEN_FOCUS: String = "com.kamsiob.claritynow.action.OPEN_FOCUS"

    /** The session the intent is about. Always present on [ACTION_OPEN_FOCUS]. */
    const val EXTRA_SESSION_ID: String = "com.kamsiob.claritynow.extra.FOCUS_SESSION_ID"

    /** `Add 10 min`. Addendum 01 4f and 5c. Handled by [FocusActionReceiver]. */
    internal const val ACTION_ADD_TEN: String = "com.kamsiob.claritynow.action.FOCUS_ADD_TEN"

    /** `End`. Addendum 01 5c. Handled by [FocusActionReceiver]. */
    internal const val ACTION_END: String = "com.kamsiob.claritynow.action.FOCUS_END"

    /**
     * The person swiped the running notification away.
     *
     * Handled rather than ignored, and that is the deliberate choice. design-v3.md
     * 15 asks for the unobvious answer where a decision is open, and the obvious
     * implementation is to let the next tick post it again, because the notification
     * is meant to be there for the whole session and re-posting is one line. That
     * makes the app argue with someone who just told it something, and it is the
     * shape MASTER_BUILD_PROMPT 13.4 rules out by name: nothing that exists to pull
     * the user back. Dismissing it ends the notification for that session and
     * nothing else: the session keeps running, the screen keeps working, and the
     * next session posts normally.
     */
    internal const val ACTION_DISMISSED: String =
        "com.kamsiob.claritynow.action.FOCUS_NOTIFICATION_DISMISSED"

    /**
     * True when [intent] is a notification asking for the focus session.
     *
     * Whoever owns `MainActivity` calls this from `onCreate` and from `onNewIntent`
     * and routes to the focus surface when it answers true. There is no second way
     * for a notification to reach a screen in this app.
     */
    fun opensFocusSession(intent: Intent?): Boolean = intent?.action == ACTION_OPEN_FOCUS

    /** The session id carried by an [ACTION_OPEN_FOCUS] intent, or null. */
    fun sessionIdIn(intent: Intent?): String? =
        intent?.takeIf { opensFocusSession(it) }?.getStringExtra(EXTRA_SESSION_ID)

    internal fun openFocus(context: Context, sessionId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_OPEN_FOCUS)
            .putExtra(EXTRA_SESSION_ID, sessionId)
            // SINGLE_TOP beside the manifest's singleTask so that tapping the
            // notification while the app is already open delivers a new intent
            // rather than rebuilding the task. NEW_TASK because the sender is a
            // notification and not an Activity.
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    internal fun addTen(context: Context, sessionId: String): PendingIntent =
        broadcast(context, ACTION_ADD_TEN, REQUEST_ADD_TEN, sessionId)

    internal fun end(context: Context, sessionId: String): PendingIntent =
        broadcast(context, ACTION_END, REQUEST_END, sessionId)

    internal fun dismissed(context: Context, sessionId: String): PendingIntent =
        broadcast(context, ACTION_DISMISSED, REQUEST_DISMISSED, sessionId)

    private fun broadcast(
        context: Context,
        action: String,
        requestCode: Int,
        sessionId: String,
    ): PendingIntent {
        // An explicit component, so the intent can only ever reach this app's own
        // receiver. The manifest declares it not exported for the same reason.
        val intent = Intent(context, FocusActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_SESSION_ID, sessionId)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            // UPDATE_CURRENT so a second session replaces the first session's id in
            // an existing PendingIntent rather than reusing it. Without it, ending
            // today's session could carry yesterday's id. IMMUTABLE because nothing
            // outside this app has any business filling anything in.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val REQUEST_OPEN = 1
    private const val REQUEST_ADD_TEN = 2
    private const val REQUEST_END = 3
    private const val REQUEST_DISMISSED = 4
}
