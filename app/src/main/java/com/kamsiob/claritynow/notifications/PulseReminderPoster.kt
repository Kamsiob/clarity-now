package com.kamsiob.claritynow.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.pulse.PulseDayState
import com.kamsiob.claritynow.domain.replay.PulseEntryState

/**
 * Proof that one local day's Pulse exists and has not been answered.
 *
 * **This type is the rule.** MASTER_BUILD_PROMPT 12.1 and design-v3.md 12.1 both say
 * the daily notification is posted **only if that day's entry exists and is
 * unanswered**, and never when IDLE. Issue #4 lists the other way round as a risk in
 * its own right: a notification on a day the engine chose to stay silent turns designed
 * silence into a broken promise, and it is the kind of defect that ships, because the
 * check is one line and everything looks right without it.
 *
 * So the check is not a line anybody has to remember. [PulseReminderPoster.post] takes
 * one of these and there is no other way to reach it, the constructor is private, and
 * [from] is the only thing that can produce one. **You cannot post the reminder without
 * having read the day's entry out of the log**, which is the same shape
 * [NotificationMoment] uses to make "never ask for permission at launch" structural.
 *
 * [PulseDayState.of] is the single definition of the three states, shared with
 * `PulseCoordinator.reminderIsDue` and with the Areas chip's dot, so a notification and
 * a dot can never disagree about what READY means.
 */
internal class PulseReminderDue private constructor(val dateKey: String) {

    companion object {

        /**
         * A token for [entry], or null when there is nothing to remind anybody about.
         *
         * Null in both of the cases 12.1 rules out. A null entry is IDLE: the engine
         * was silent that day, or the app was not opened and nothing was generated at
         * all. An answered entry is done, and an app that asked again would be asking
         * about something a person has already dealt with.
         *
         * **A missing entry is never a reason to generate one.** Generation runs on the
         * first foreground of a day, MASTER_BUILD_PROMPT 11.3, and a worker that
         * generated one so that it would have something to post would be an app
         * speaking to somebody who did not open it, which 13.4 forbids by name.
         */
        fun from(entry: PulseEntryState?): PulseReminderDue? {
            val day = entry ?: return null
            if (PulseDayState.of(day) != PulseDayState.READY) return null
            return PulseReminderDue(day.dateKey)
        }
    }
}

/**
 * Posts the daily Pulse reminder, and decides nothing.
 *
 * One notification, on [ClarityNotificationChannels.REMINDERS], which
 * MASTER_BUILD_PROMPT 13.4 names as the channel this and only this is posted to. It
 * carries two fixed strings, a tap that opens the Pulse surface, and nothing else.
 *
 * **The observation stays inside the app.** The entry holds the sentence the engine
 * wrote about somebody's own week and none of it reaches this notification, which is
 * two rules at once: MASTER_BUILD_PROMPT 11.2 closes the list of things that may put a
 * corpus sentence on a surface and a notification poster is not on it, and a lock
 * screen is read by whoever is holding the phone. The token above carries a date key
 * and no text for exactly this reason: there is no sentence here to leak.
 *
 * **Silent, and that is set on the post rather than left to the channel.** Issue #4
 * requires a silent reminder. [ClarityNotificationChannels.REMINDERS] was created at
 * default importance in phase 4 and a channel's importance and sound are fixed for an
 * install the moment it is created, so silence has to come from the builder;
 * `setSilent` overrides the channel's sound and vibration for this post. Changing the
 * channel instead would mean a new channel id and a deleted old one, which is a phase 4
 * decision to revisit deliberately rather than a side effect of this phase.
 */
internal class PulseReminderPoster(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    /**
     * Whether anything may be posted at all.
     *
     * False when the person denied `POST_NOTIFICATIONS` or switched this app's
     * notifications off, and a false answer is the end of it: nothing is built, nothing
     * is retried and nothing anywhere tells them what they are not seeing.
     */
    fun canPost(): Boolean = manager.areNotificationsEnabled()

    /**
     * Posts the reminder for the day [due] proves is ready.
     *
     * [due] is not read. It is the proof that the check happened, and the notification
     * has no content that depends on which day it is.
     */
    fun post(due: PulseReminderDue) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, ClarityNotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_graphic_eq)
            .setContentTitle(context.getString(R.string.pulse_reminder_title))
            .setContentText(context.getString(R.string.pulse_reminder_text))
            .setAutoCancel(true)
            .setSilent(true)
            .setLocalOnly(true)
            // No timestamp. `When` on a reminder invites the reading that it has been
            // sitting there a while, which is the nudge this app does not make.
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(PulseIntents.openPulse(context))
            .build()
        manager.notify(ID_REMINDER, notification)
    }

    /**
     * Takes it out of the shade.
     *
     * Called when the day's Pulse is answered, wherever it was answered, so a reminder
     * to answer something does not sit in the shade after it has been answered. A
     * cancel for a notification that is not there costs nothing, which is why this is
     * not conditional on having posted one.
     */
    fun clear() = manager.cancel(ID_REMINDER)

    private companion object {

        /** Distinct from the focus session ids, which are 4001 to 4003. */
        const val ID_REMINDER = 4004
    }
}
