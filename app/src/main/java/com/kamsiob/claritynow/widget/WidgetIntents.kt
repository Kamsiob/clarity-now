package com.kamsiob.claritynow.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kamsiob.claritynow.MainActivity
import com.kamsiob.claritynow.notifications.FocusIntents

/**
 * Every intent a widget can send, in one file.
 *
 * Written as a contract for the same reason `notifications/FocusIntents.kt` is: the
 * other side of it is an Activity this package cannot see, and "deep links open the
 * right surface", `MASTER_BUILD_PROMPT.md` 13.3, is a defect that is invisible from
 * either side alone.
 *
 * `MainActivity` is `singleTask`, so a widget tap reaches a live process through
 * `onNewIntent` and a dead one through `onCreate`, and each predicate below answers
 * both the same way.
 *
 * ## The one override, and why it is a function rather than a rule to remember
 *
 * 13.3 and `design-v3.md` 12.1: **while a focus session is running, any widget tap
 * opens the focus screen.** Six widgets each remembering that is six places for it to
 * be forgotten, so [tap] takes the destination a widget wants and the session the
 * snapshot holds, and returns what actually opens. A widget calls it and does not think
 * about the rule.
 *
 * The reason for the override is worth keeping: a person who is inside a timed session
 * and touches this app's home screen is almost never asking to go somewhere else, and
 * an app that took them to a queue would have interrupted the one activity it exists to
 * protect.
 */
object WidgetIntents {

    /**
     * Open capture, straight into the unfiled inbox, with the keyboard already up and
     * no area to choose. `design-v3.md` 10.16 and 12.2.
     *
     * The receiving side is `AreaSheet.AddItem(areaId = null)`, which is the same sheet
     * the FAB opens and is already the app's one capture surface. **No second capture
     * path, and no dialog of the widget's own**: a capture that behaved differently
     * depending on where it started would be two features wearing one word.
     */
    const val ACTION_CAPTURE_UNFILED: String = "com.kamsiob.claritynow.action.CAPTURE_UNFILED"

    /** Open the Momentum tab. `MASTER_BUILD_PROMPT.md` 12.2, the Rhythm widget's tap. */
    const val ACTION_OPEN_MOMENTUM: String = "com.kamsiob.claritynow.action.OPEN_MOMENTUM"

    /**
     * Open one area, which is what a tap on `Next Up` and on a row of `All Areas` does.
     * `design-v3.md` 12.2.
     *
     * The destination is the area detail sheet in 10.15's table, over the Areas tab,
     * which is what "opens that area" means everywhere else in this app. A tap that
     * landed on the Areas list with nothing open would be a widget that saved no steps.
     */
    const val ACTION_OPEN_AREA: String = "com.kamsiob.claritynow.action.OPEN_AREA"

    /** The area an [ACTION_OPEN_AREA] or [ACTION_START_FOCUS] intent is about. */
    const val EXTRA_AREA_ID: String = "com.kamsiob.claritynow.extra.AREA_ID"

    /**
     * Start a focus session on one item, which is what a tap on `First Step` does.
     * `design-v3.md` 12.2 and Addendum 01 6b.
     *
     * **It asks for a session rather than starting one.** The write path in this app is
     * `ClarityRepository` and the surface a session belongs to is the Focus screen, so a
     * widget that appended `FOCUS_STARTED` from a broadcast receiver would put somebody
     * in a running session with nothing on screen saying so. This opens the app on that
     * item and the session starts where every other session starts.
     */
    const val ACTION_START_FOCUS: String = "com.kamsiob.claritynow.action.START_FOCUS"

    /** The item an [ACTION_START_FOCUS] intent is about. */
    const val EXTRA_ITEM_ID: String = "com.kamsiob.claritynow.extra.ITEM_ID"

    /**
     * True when [intent] is a widget asking for capture into the inbox.
     *
     * Whoever owns `MainActivity` calls this from `onCreate` and from `onNewIntent`,
     * exactly as it already calls [FocusIntents.opensFocusSession], and opens the add
     * sheet with no area. There is no second way for a widget to reach a screen.
     */
    fun opensUnfiledCapture(intent: Intent?): Boolean = intent?.action == ACTION_CAPTURE_UNFILED

    /** True when [intent] is a widget asking for Momentum. */
    fun opensMomentum(intent: Intent?): Boolean = intent?.action == ACTION_OPEN_MOMENTUM

    /**
     * Where a tap on [destination] actually goes, given the session the snapshot holds.
     *
     * [runningSessionId] is `WidgetSnapshot.focus?.sessionId`, which is null when this
     * device has no session running.
     */
    fun tap(context: Context, destination: Intent, runningSessionId: String?): Intent =
        if (runningSessionId == null) destination else focusSurface(context, runningSessionId)

    /** Capture, into the inbox. */
    fun capture(context: Context): Intent = toMainActivity(context, ACTION_CAPTURE_UNFILED)

    /** Momentum. */
    fun momentum(context: Context): Intent = toMainActivity(context, ACTION_OPEN_MOMENTUM)

    /**
     * The app, wherever it was.
     *
     * The tap on a widget that has nothing to show, which is a real state on a fresh
     * install and after an erase. It names no destination because there is nothing to
     * name, and the screen it lands on is the one whose single control makes an area,
     * per `design-v3.md` 10.15.
     */
    fun app(context: Context): Intent = toMainActivity(context, Intent.ACTION_MAIN)

    /** True when [intent] is a widget asking for one area. */
    fun opensArea(intent: Intent?): Boolean = intent?.action == ACTION_OPEN_AREA

    /** The area [intent] is about, or null. */
    fun areaIdIn(intent: Intent?): String? = intent?.getStringExtra(EXTRA_AREA_ID)

    /** True when [intent] is a widget asking for a session on one item. */
    fun startsFocusOnItem(intent: Intent?): Boolean = intent?.action == ACTION_START_FOCUS

    /** The item [intent] is about, or null. */
    fun itemIdIn(intent: Intent?): String? = intent?.getStringExtra(EXTRA_ITEM_ID)

    /** One area, opened. */
    fun area(context: Context, areaId: String): Intent =
        toMainActivity(context, ACTION_OPEN_AREA)
            .setData(Uri.parse("claritynow://widget/area/$areaId"))
            .putExtra(EXTRA_AREA_ID, areaId)

    /** A session on one item, in one area. */
    fun startFocus(context: Context, areaId: String, itemId: String): Intent =
        toMainActivity(context, ACTION_START_FOCUS)
            .setData(Uri.parse("claritynow://widget/focus/$itemId"))
            .putExtra(EXTRA_AREA_ID, areaId)
            .putExtra(EXTRA_ITEM_ID, itemId)

    /**
     * The Focus surface, with a session or without one.
     *
     * **It reuses `FocusIntents.ACTION_OPEN_FOCUS` rather than declaring a second
     * action**, because the two would mean the same thing and `MainActivity` already
     * routes that one. That file documents the session id as always present, which is
     * true of the notifications it was written for; a widget with no session sends the
     * same action with no id, which is the state the Focus surface already handles every
     * time somebody taps the Focus chip on the Areas screen. `MainActivity` reads the
     * counter and never the id, deliberately and for a reason it states, so nothing
     * downstream can tell the difference.
     */
    fun focusSurface(context: Context, sessionId: String?): Intent =
        toMainActivity(context, FocusIntents.ACTION_OPEN_FOCUS).apply {
            if (sessionId != null) putExtra(FocusIntents.EXTRA_SESSION_ID, sessionId)
        }

    /**
     * An explicit component, so a widget's `PendingIntent` can only ever reach this
     * app's own Activity.
     *
     * `SINGLE_TOP` beside the manifest's `singleTask` so that a tap while the app is
     * open delivers a new intent rather than rebuilding the task, and `NEW_TASK` because
     * the sender is a launcher and not an Activity. The same pair the notifications use.
     */
    private fun toMainActivity(context: Context, action: String): Intent =
        Intent(context, MainActivity::class.java)
            .setAction(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    /*
     * The two builders above set a `data` URI that nothing ever reads, and it is not
     * decoration. Glance turns an action into a `PendingIntent` with a fixed request
     * code, and two `PendingIntent`s whose intents match under `Intent.filterEquals` are
     * one `PendingIntent`. `filterEquals` ignores extras, so two `Next Up` widgets
     * pinned to two areas would open the same one. A distinct URI makes them distinct
     * intents. The actions with no extras above need none of this and have none.
     */
}
