package com.kamsiob.claritynow.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kamsiob.claritynow.di.ClarityGraph
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit

/**
 * Arms and disarms the daily Pulse reminder. MASTER_BUILD_PROMPT 12.1 and 13.4.
 *
 * ## One hop at a time, rather than a periodic request
 *
 * The obvious implementation is a `PeriodicWorkRequest` with a twenty four hour period,
 * and design-v3.md 15 asks for the obvious answer to be identified and then beaten
 * where something serves the brief better. It loses here on the one thing this feature
 * is, which is a time.
 *
 * A periodic request's period is a fixed duration measured from the previous period,
 * not a wall clock hour. The morning the clocks change it fires an hour early or an
 * hour late and stays there until something reschedules it, and the small batching
 * WorkManager applies to each period accumulates over months. A reminder that arrives
 * at the wrong hour is worse than useless for the audience MASTER_BUILD_PROMPT 14b
 * describes: it teaches them the app's timing cannot be trusted, and they switch it
 * off.
 *
 * So each run arms the next one, and each arming recomputes the target against the
 * clock and its zone through [nextReminderAtMillis]. Nothing accumulates, a zone change
 * is absorbed on the next hop, and daylight saving is a calendar question the
 * scheduling code never sees.
 *
 * **This is not an exact alarm and does not try to be.** WorkManager may run the work
 * late, and Doze can hold it for a while on a phone that has been asleep. An exact
 * alarm needs `SCHEDULE_EXACT_ALARM`, and MASTER_BUILD_PROMPT 18 puts every permission
 * beyond notifications out of scope for v1. A reminder that arrives a little late is
 * the honest version of what this app can promise.
 *
 * ## Where it is called from
 *
 * `ClarityNotifications.install` launches [watchSettings] at process start, which is
 * this app's one process wide hook and the same place the notification channels are
 * created. **Nothing else calls this object**, and in particular the Settings row in
 * phase 11 does not need to: it writes the preference, and the collector does the rest.
 * A switch that had to remember to schedule something is a switch that eventually
 * forgets, and it would forget on the device of somebody who had already turned the
 * reminder on.
 */
internal object PulseReminderScheduler {

    /**
     * The unique work name. One reminder exists at a time, and enqueuing under this
     * name replaces whatever was pending rather than adding a second.
     */
    const val UNIQUE_NAME: String = "com.kamsiob.claritynow.pulse.reminder"

    /**
     * Follows the two settings for the life of the process, and never returns.
     *
     * The first emission arms the chain at process start, which is what recovers from a
     * force stop, from work the system dropped, and from an install that has never
     * scheduled anything. Every emission after that is somebody moving the switch or
     * the hour in Settings, and the reminder follows within the same second.
     */
    suspend fun watchSettings(context: Context) {
        if (!ClarityGraph.isInstalled) return
        val preferences = ClarityGraph.preferences
        combine(
            preferences.pulseRemindersEnabled,
            preferences.pulseReminderHour,
        ) { enabled, hour -> ReminderSetting(enabled, hour) }
            .distinctUntilChanged()
            .collect { setting -> update(context, setting.enabled, setting.hour) }
    }

    /**
     * Brings the schedule in line with the two settings.
     *
     * Enqueuing is unconditional when [enabled] is true, rather than skipped when
     * something is already pending: the pending hop may have been armed against a
     * different hour, a different zone or a stale idea of what day it is, and the cost
     * of being sure is one row in a database.
     */
    fun update(context: Context, enabled: Boolean, hour: Int) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
            return
        }
        schedule(context, hour)
    }

    /**
     * Arms the next reminder at [hour], local, on whichever day that next falls.
     *
     * **Called from inside [PulseReminderWorker] as its last act**, which is how the
     * chain continues, and that has one consequence worth stating: replacing unique
     * work cancels work that is running, and at that moment the running work is the
     * worker doing the calling. It has already posted by then and everything after this
     * line is a return, so the run is recorded as canceled and nothing is lost. The
     * alternative, appending to the chain, leaves a row per day in the database that
     * can never be pruned, because every finished node keeps an unfinished dependent.
     *
     * The same cancellation is the one way a reminder can go missing: opening the app
     * in the same second the worker runs re-arms the chain from the process start
     * collector and stops that run. The person is looking at the app, where the chip
     * carries the dot, which is the better of the two places to find out.
     */
    fun schedule(context: Context, hour: Int) {
        if (!ClarityGraph.isInstalled) return
        val clock = ClarityGraph.clock
        val delay = reminderDelayMillis(clock.nowMillis(), clock.zone(), hour)
        val request = OneTimeWorkRequestBuilder<PulseReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            // No constraints, deliberately and not by omission. A reminder that waited
            // for a charger or a network would be a reminder that arrives at the wrong
            // time or never, and a network constraint would need the one permission
            // this app removes from the merged manifest on purpose. See the manifest.
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}

/** The two preferences that decide the schedule, as one value so it can be compared. */
private data class ReminderSetting(val enabled: Boolean, val hour: Int)
