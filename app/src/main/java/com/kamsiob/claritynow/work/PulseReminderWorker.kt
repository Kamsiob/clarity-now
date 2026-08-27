package com.kamsiob.claritynow.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.notifications.PulseReminderDue
import com.kamsiob.claritynow.notifications.PulseReminderPoster
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * The daily reminder, at the hour the person chose. MASTER_BUILD_PROMPT 12.1.
 *
 * Three things happen here and they happen in this order, which is load bearing.
 *
 * 1. **The day is read out of the log**, never generated. Generation runs on the first
 *    foreground of a day, 11.3, and a worker that generated a Pulse so that it would
 *    have something to remind somebody about would be the app speaking to a person who
 *    did not open it. MASTER_BUILD_PROMPT 13.4 forbids that in the same sentence it
 *    forbids re-engagement notifications. A day nobody opened the app on has no entry
 *    and therefore no reminder, and that is the specified behavior rather than a gap.
 * 2. **The notification is posted only through [PulseReminderDue]**, which cannot be
 *    built from anything except an entry that exists and is unanswered. There is no
 *    line in this file that decides whether to post; there is a token that either
 *    exists or does not.
 * 3. **The next hop is armed.** See [PulseReminderScheduler.schedule] for why the chain
 *    is one hop at a time and what replacing unique work from inside a running worker
 *    does.
 *
 * **It posts whether or not the app is on screen**, which is the opposite of what the
 * focus notifications do and is deliberate. Those announce a moment that the screen is
 * already showing, so a second surface saying the same thing is noise. This one is a
 * fixed daily promise a person switched on, and suppressing it because they happened to
 * be holding the phone at eight would mean a reminder that arrives on some days and not
 * others for a reason they cannot see. It is silent, so the cost of it being redundant
 * for the minute they are already in the app is a line in the shade.
 *
 * **Nothing here is ever retried.** A retry runs at whatever time WorkManager gets
 * around to, and a reminder at the wrong hour is worse than a missed one for the
 * audience this feature is for. A failure is logged, the day is skipped, and tomorrow
 * is armed as usual, which is also why the arming sits outside the part that can throw.
 *
 * The process is alive whenever this runs, because a worker cannot start before
 * `Application.onCreate`, so `ClarityGraph` is installed. It is checked anyway.
 */
class PulseReminderWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (!ClarityGraph.isInstalled) return Result.success()

        // The hour to arm tomorrow at, whatever happens below. A settings read that
        // throws must not be the reason a person's reminder stops forever, so the
        // fallback is the specified default rather than an abandoned chain.
        var hour = ClarityPreferences.DEFAULT_REMINDER_HOUR

        try {
            val preferences = ClarityGraph.preferences
            val enabled = preferences.pulseRemindersEnabled.first()
            hour = preferences.pulseReminderHour.first()
            // Switched off while the app was closed, by an import or by a restore.
            // Nothing is posted and nothing is armed; the collector in
            // ClarityNotifications cancels the chain on the same reading.
            if (!enabled) return Result.success()
            postIfDue()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            // Reading the settings or the log failed. The one thing that must not
            // happen is a notification posted without the check, and that cannot happen
            // here: the token is what posts, and there is no token. Tomorrow is armed
            // anyway, because a reminder that stops is indistinguishable from a
            // reminder that was never switched on.
            Log.w(TAG, "the reminder could not be checked: ${failure.message ?: "no detail"}")
        }

        PulseReminderScheduler.schedule(applicationContext, hour)
        return Result.success()
    }

    private suspend fun postIfDue() {
        val clock = ClarityGraph.clock
        val dateKey = clock.dateKey()
        val repository = ClarityGraph.repository
        // The projection is the log replayed, and this process may have just been
        // started by WorkManager with nothing loaded. Idempotent under the repository's
        // own lock, which is why every other entry point calls it too.
        repository.load()

        val due = PulseReminderDue.from(repository.pulseFor(dateKey))
        if (due == null) {
            // Both silences look the same from the outside, which is why they are said
            // out loud here: a month of no reminders is either working exactly as
            // designed or completely broken, and nothing else can tell the difference.
            Log.i(TAG, "$dateKey not READY, no reminder posted")
            return
        }
        PulseReminderPoster(applicationContext).post(due)
        Log.i(TAG, "$dateKey reminder posted")
    }

    private companion object {

        /**
         * The same tag `ClarityApp` prints the generation outcome under, so
         * `adb logcat -s ClarityPulse` is the whole of the Pulse's diagnostics,
         * generation and reminder together.
         *
         * **It prints the day and what was decided, never the observation.** A sentence
         * about somebody's own week does not belong in a system log any process with
         * the permission can read.
         */
        const val TAG = "ClarityPulse"
    }
}
