package com.kamsiob.claritynow.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kamsiob.claritynow.R

/**
 * Everything the shade needs to know about a running session, computed once.
 *
 * **[remainingSeconds] is here and the digits are not.** design-v3.md 11.3 is
 * explicit that outside the app a session reads in minutes and never in seconds, so
 * [minutesLeft] is the only figure any string in this package is allowed to take,
 * and the seconds exist for one thing: the position of the depleting track, which is
 * exact because the platform draws it from a number rather than from a label.
 *
 * [accent] is already transformed for calm mode by the time it arrives here, per
 * design-v3.md 16.3. This class does no color work, so there is nowhere for a second
 * saturation rule to appear.
 */
internal data class FocusNotificationModel(
    val sessionId: String,
    val areaName: String,
    val itemTitle: String,
    val accent: Int,
    val plannedSeconds: Int,
    val remainingSeconds: Int,
    val endsAtMillis: Long,
    /**
     * Where the transition warning point sits on the track, in the same units as
     * [remainingSeconds], or null when no point is drawn.
     *
     * Null covers both of the cases design-v3.md 11.4 and 10.18 rule out: the
     * warning is switched off, in which case there is no point and no state change
     * at all, and the session is too short to carry one, in which case a point would
     * sit at the very start of the track and fire the moment the session began.
     */
    val transitionMarkSeconds: Int?,
) {

    /**
     * Whole minutes left, rounded up, so a session with forty seconds to run reads
     * one minute rather than zero. It reaches zero only once the time is gone, at
     * which point the running notification is replaced rather than updated.
     */
    val minutesLeft: Int get() = (remainingSeconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE

    /**
     * What has to change before the shade is written to again.
     *
     * The countdown ticks once a second, per design-v3.md 8.2 item 7, and this
     * surface renders minutes, so a post per tick would be fifty nine writes nobody
     * can see followed by one they can. Everything that is actually drawn is in this
     * key and nothing else is.
     */
    val renderKey: List<Any?>
        get() = listOf(
            sessionId,
            minutesLeft,
            plannedSeconds,
            itemTitle,
            areaName,
            accent,
            transitionMarkSeconds,
        )

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}

/**
 * Builds and posts the focus session notifications. It decides nothing.
 *
 * The three surfaces here are one notification in two renderings plus two one-shot
 * notifications:
 *
 * - **The Live Update**, `Notification.ProgressStyle` on a promoted ongoing
 *   notification. MASTER_BUILD_PROMPT 14b.6, design-v3.md 11.4
 * - **The ongoing notification**, the same id with a countdown chronometer, which is
 *   what MASTER_BUILD_PROMPT 10 specifies and what the Live Update degrades to
 * - **One completion notification** and **one transition warning**, each posted at
 *   most once per session and only when the app is somewhere else
 *
 * **The fallback is the ordinary path, not the exception.** Addendum 01 5d requires
 * it, issue #32 names building it second as the risk, and it is the path every
 * device below Android 16 takes. [runningNotification] therefore builds both from
 * one set of inputs, with the promoted half as the branch rather than the base.
 *
 * **Nothing here interprets a session.** There is no line that says a session was
 * long or short, on track or behind, and there is no word for the state a session
 * was left in. Addendum 01 4e, and CLAUDE.md rule 8.
 */
internal class FocusNotificationPoster(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    /**
     * Whether anything may be posted at all.
     *
     * False when the user denied `POST_NOTIFICATIONS` or switched this app's
     * notifications off, and a false answer is the end of it: nothing is built,
     * nothing is retried and nothing anywhere tells the person what they are not
     * seeing. MASTER_BUILD_PROMPT 13.4 requests that permission contextually and
     * design-v3.md 11.4 forbids the app remarking on its absence.
     */
    fun canPost(): Boolean = manager.areNotificationsEnabled()

    /**
     * Whether the Live Update may be posted.
     *
     * One call answers both of the cases Addendum 01 5d asks about: on this androidx
     * version [NotificationManagerCompat.canPostPromotedNotifications] returns false
     * below API 36 without touching the platform, and asks the platform above it.
     * Checked before every post rather than once, because a person can revoke
     * promotion in system settings while a session is running.
     */
    fun canPromote(): Boolean = manager.canPostPromotedNotifications()

    fun showRunning(model: FocusNotificationModel) {
        if (!canPost()) return
        manager.notify(ID_RUNNING, runningNotification(model, promoted = canPromote()))
    }

    fun clearRunning() = manager.cancel(ID_RUNNING)

    fun showCompletion(model: FocusNotificationModel) {
        if (!canPost()) return
        manager.notify(ID_COMPLETE, completionNotification(model))
    }

    fun clearCompletion() = manager.cancel(ID_COMPLETE)

    fun showTransitionWarning(model: FocusNotificationModel) {
        if (!canPost()) return
        manager.notify(ID_TRANSITION, transitionNotification(model))
    }

    fun clearTransitionWarning() = manager.cancel(ID_TRANSITION)

    /**
     * The running session, promoted where the platform allows it and a chronometer
     * where it does not. **One id for both**, so a person who revokes promotion in
     * the middle of a session sees the same notification change rather than a second
     * one appear.
     */
    private fun runningNotification(model: FocusNotificationModel, promoted: Boolean): Notification {
        val builder = NotificationCompat.Builder(context, ClarityNotificationChannels.ONGOING)
            .setSmallIcon(R.drawable.ic_timer)
            // Area, then item. design-v3.md 11.4 asks for both and issue #32 asks
            // for TalkBack to read them in this order, which is the order the shade
            // reads a notification in: the header line carries the subtext, the
            // title follows it, and the body follows that.
            .setSubText(model.areaName)
            .setContentTitle(model.itemTitle)
            .setOngoing(true)
            // Silence comes from the channel, which MASTER_BUILD_PROMPT 13.4
            // specifies as silent and low importance, rather than from
            // `setSilent(true)` on the builder. Addendum 01 3d asks for the platform
            // to be verified rather than trusted, and what could not be verified on
            // this machine is whether a builder level silence flag is one of the
            // characteristics the system reads when it decides whether to promote a
            // notification. Everything that flag does above API 26 is done by the
            // channel already, so the version that cannot cost the Live Update is
            // the one to ship. design-v3.md 11.4 still gets its silent surface.
            .setOnlyAlertOnce(true)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            // Tints the app name and the small icon, which is the only color the
            // platform lets a notification carry without being colorized. Colorized
            // is deliberately not set: it fills the notification with the area color,
            // and design-v3.md 3.4 rules the area color out as a filled block as
            // firmly as it rules it out as a stripe.
            .setColor(model.accent)
            .setContentIntent(FocusIntents.openFocus(context, model.sessionId))
            .setDeleteIntent(FocusIntents.dismissed(context, model.sessionId))
            .addAction(
                action(R.drawable.ic_add, R.string.focus_action_add_ten, FocusIntents.addTen(context, model.sessionId)),
            )
            .addAction(
                action(R.drawable.ic_close, R.string.focus_action_end, FocusIntents.end(context, model.sessionId)),
            )

        if (promoted) {
            builder
                .setRequestPromotedOngoing(true)
                // The status bar chip, which is what a promoted notification
                // collapses to on a Pixel. Minutes, per design-v3.md 11.3, and
                // abbreviated because the platform gives this field about seven
                // characters.
                .setShortCriticalText(
                    context.getString(R.string.focus_notification_chip_minutes, model.minutesLeft),
                )
                .setContentText(minutesLeftText(model))
                .setStyle(progressStyle(model))
        } else {
            // The chronometer MASTER_BUILD_PROMPT 10 specifies, counting down to the
            // end instant. The platform redraws it every second with no help from
            // this process, which is why it is still right after a process death and
            // why nothing here re-posts to keep it moving.
            //
            // **No progress bar here, and that is a decision rather than an
            // omission.** The shape is the primary carrier of a duration on the
            // three surfaces design-v3.md 11.3 names, and this is not one of them:
            // it is the degradation path, and both MASTER_BUILD_PROMPT 10 and 14b.6
            // specify exactly one thing for it, a chronometer. Reaching for
            // `setProgress` to give it a shape too would put a bar on an element,
            // which CLAUDE.md rule 10 and design-v3.md 14 forbid outright, in order
            // to satisfy a rule that was written about somewhere else.
            //
            // Seconds here and minutes on the Live Update is not an inconsistency.
            // design-v3.md 11.3 scopes "minutes only outside the app" to the three
            // surfaces it names, the ring, the Live Update and the widget, and both
            // MASTER_BUILD_PROMPT 10 and 14b.6 specify this one as a chronometer.
            builder
                .setShowWhen(true)
                .setWhen(model.endsAtMillis)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        }
        return builder.build()
    }

    /**
     * One undivided track that depletes, and at most one point on it.
     *
     * **Progress is the time remaining rather than the time spent**, which is what
     * makes the track deplete instead of fill. Addendum 01 8d and design-v3.md 11.3:
     * a shrinking length is the feeling of having room left, with no arithmetic in
     * between, and a growing one is progress toward a target, which this app does not
     * have and design-v3.md 14 forbids drawing.
     *
     * **Segments and points, per Addendum 01 5b and design-v3.md 11.4, and the choice
     * is recorded either way.** One segment, because a session is one undivided
     * thing and dividing the track would invent a structure inside it that the
     * session does not have. One point, and only when the transition warning is on:
     * design-v3.md 11.4 makes the track reaching that point the state change the
     * warning asks for, so the point is not decoration, it is the mechanism. With the
     * warning off there is no point and no state change at all. The point takes no
     * color of its own, because 11.4 allows the track one color and no second one.
     */
    private fun progressStyle(model: FocusNotificationModel): NotificationCompat.ProgressStyle {
        val style = NotificationCompat.ProgressStyle()
            .addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(model.plannedSeconds)
                    .setColor(model.accent),
            )
            .setProgress(model.remainingSeconds)
        model.transitionMarkSeconds?.let { position ->
            style.addProgressPoint(NotificationCompat.ProgressStyle.Point(position))
        }
        return style
    }

    /**
     * The gentle notification MASTER_BUILD_PROMPT 10 asks for when the planned time
     * runs out while the app is somewhere else.
     *
     * **It carries the completion screen's own words and no others.** design-v3.md 11
     * sets that line, and Addendum 01 4e requires the same line whether the session
     * ran its full planned length or a person ended it sooner. There is no second,
     * quieter version of this notification for a shorter session, no duration
     * compared against a plan, and no action beyond opening the app, which is where
     * the session is resolved.
     */
    private fun completionNotification(model: FocusNotificationModel): Notification =
        NotificationCompat.Builder(context, ClarityNotificationChannels.FOCUS)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setSubText(model.areaName)
            .setContentTitle(context.getString(R.string.focus_complete_notification_title))
            .setContentText(model.itemTitle)
            .setAutoCancel(true)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(model.accent)
            .setContentIntent(FocusIntents.openFocus(context, model.sessionId))
            .build()

    /**
     * The transition warning, posted only when the app is somewhere else and no Live
     * Update is available. design-v3.md 10.18, Addendum 01 4g, issue #30.
     *
     * On the silent [ClarityNotificationChannels.ONGOING] channel, never on Focus and
     * never on Reminders, so it arrives quietly in the shade rather than as an
     * interruption, which is the distinction the whole feature turns on. It fires
     * once, it does not count anything down after it, and it says the one sentence
     * design-v3.md 10.18 puts beneath the numeral at the same moment.
     */
    private fun transitionNotification(model: FocusNotificationModel): Notification =
        NotificationCompat.Builder(context, ClarityNotificationChannels.ONGOING)
            .setSmallIcon(R.drawable.ic_timer)
            .setSubText(model.areaName)
            .setContentTitle(context.getString(R.string.focus_transition_notification_title))
            .setContentText(model.itemTitle)
            .setAutoCancel(true)
            .setSilent(true)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(model.accent)
            .setContentIntent(FocusIntents.openFocus(context, model.sessionId))
            .build()

    private fun minutesLeftText(model: FocusNotificationModel): String =
        context.resources.getQuantityString(
            R.plurals.focus_notification_minutes_left,
            model.minutesLeft,
            model.minutesLeft,
        )

    /**
     * `setShowsUserInterface(false)` is the part of this that Addendum 01 5c asks
     * for: the action does its work in the background and opens nothing.
     */
    private fun action(
        icon: Int,
        label: Int,
        intent: PendingIntent,
    ): NotificationCompat.Action =
        NotificationCompat.Action.Builder(icon, context.getString(label), intent)
            .setShowsUserInterface(false)
            .build()

    private companion object {
        /**
         * The running session, in whichever of its two renderings this device can
         * show. One id, deliberately.
         */
        const val ID_RUNNING = 4001
        const val ID_COMPLETE = 4002
        const val ID_TRANSITION = 4003
    }
}
