package com.kamsiob.claritynow.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.kamsiob.claritynow.R

/**
 * The three notification channels this app has, and the only three it will have.
 * MASTER_BUILD_PROMPT 13.4.
 *
 * | channel | importance | sound | what posts to it |
 * |---|---|---|---|
 * | [FOCUS] | default | the phone's own notification sound | one gentle notification when a session's planned time runs out while the app is somewhere else |
 * | [REMINDERS] | default | the phone's own notification sound | the daily Pulse reminder, phase 6 |
 * | [ONGOING] | low | none | the running session, as a Live Update where the platform allows one and as a chronometer where it does not |
 *
 * **No marketing notification, no re-engagement notification, and nothing that
 * exists to pull a person back.** 13.4 states that as a rule about this app rather
 * than about these channels, and the shape of the list is what enforces it: every
 * one of the three is either something the person started themself or something they
 * switched on themself, and there is no channel a fourth kind of notification could
 * be posted to without one being added here.
 *
 * **Three properties are set on all three and each is a rule from somewhere else.**
 *
 * 1. **No badge.** design-v3.md 14, as amended by Addendum 01, forbids a numeric
 *    badge and a red dot on any surface in this app, and a launcher badge is exactly
 *    that surface. [NotificationChannel.setShowBadge] is where a notification would
 *    otherwise put one, and the default is on.
 * 2. **No vibration.** design-v3.md 9: haptics never fire on notification arrival.
 *    The sixteen haptic events in that table are all responses to something a person
 *    did with their hands, and a notification is not one.
 * 3. **No light.** A notification light is an ambient alert with no content, and
 *    this app has one ambient signal, the amber Pulse dot in 10.1.
 *
 * **Why the three are created together at process start rather than each at first
 * use.** A channel's importance and sound are fixed for an install the moment it is
 * created and cannot be raised afterwards, so a channel created late by whichever
 * code path happened to need it is a channel whose settings were chosen by that code
 * path. Creating all three in one place makes the table above the record of the
 * decision. Creating a channel posts nothing, alerts nobody and needs no permission.
 *
 * **[REMINDERS] is created here in phase 4 and used in phase 6.** It appears in the
 * system notification settings with nothing behind it until then, which is the price
 * of the rule above and is the smaller cost.
 *
 * **If a later phase needs different settings on a channel**, it changes the id
 * rather than the settings, deletes the old id and creates the new one. That is the
 * platform's only escape from the immutability above, and it is worth knowing about
 * before someone concludes a channel cannot be corrected.
 */
object ClarityNotificationChannels {

    /**
     * Session complete. Default importance and the phone's own notification sound.
     *
     * MASTER_BUILD_PROMPT 13.4 asks for a gentle sound, and **the deliberate answer
     * is the sound the person already chose rather than one this app supplies.**
     * design-v3.md 15: identify the obvious answer and choose otherwise unless the
     * obvious one is genuinely best. The obvious answer is to bundle a soft chime,
     * and it is wrong here for a specific reason rather than a stylistic one: a
     * person who has already set their phone to a quiet notification sound has told
     * the device what gentle means to them, and an app supplied tone overrides that
     * decision on the one notification this app posts that makes any sound at all.
     * Default importance takes the user's sound with no call to setSound.
     */
    const val FOCUS = "focus"

    /** The daily Pulse reminder. Phase 6, issue #4. */
    const val REMINDERS = "reminders"

    /**
     * The running session. Low importance, silent, and the channel the Live Update
     * is posted to as well as the chronometer that stands in for it.
     */
    const val ONGOING = "ongoing"

    fun ensure(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            channel(
                context = context,
                id = FOCUS,
                nameRes = R.string.channel_focus_name,
                descriptionRes = R.string.channel_focus_description,
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannel(
            channel(
                context = context,
                id = REMINDERS,
                nameRes = R.string.channel_reminders_name,
                descriptionRes = R.string.channel_reminders_description,
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannel(
            channel(
                context = context,
                id = ONGOING,
                nameRes = R.string.channel_ongoing_name,
                descriptionRes = R.string.channel_ongoing_description,
                importance = NotificationManager.IMPORTANCE_LOW,
            ).apply {
                // Low importance is already silent. Stated rather than assumed,
                // because MASTER_BUILD_PROMPT 13.4 names this channel silent and a
                // default that happens to agree is not the same as a decision.
                setSound(null, null)
            },
        )
    }

    private fun channel(
        context: Context,
        id: String,
        nameRes: Int,
        descriptionRes: Int,
        importance: Int,
    ): NotificationChannel {
        val text = context.getString(descriptionRes)
        return NotificationChannel(
            id,
            context.getString(nameRes),
            importance,
        ).apply {
            description = text
            enableVibration(false)
            enableLights(false)
            setShowBadge(false)
        }
    }
}
