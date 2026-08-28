package com.kamsiob.claritynow.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.widget.ClarityWidgets
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

/**
 * The six hourly widget refresh. MASTER_BUILD_PROMPT 13.3.
 *
 * The snapshot is written on every meaningful change already, so this is not how a
 * widget finds out that something happened. It is how a widget finds out that **time**
 * has passed: the automatic area rotates on the local day and the fourteen day row
 * rolls forward, and neither is a change to the log, so nothing in the app would have
 * noticed on a phone that was left alone overnight.
 *
 * **Nothing is retried.** A failed pass leaves the previous snapshot in place, which is
 * the right degradation, and the next pass is at most six hours away. A retry would run
 * at whatever moment WorkManager chose and would buy nothing that waiting does not.
 */
class WidgetRefreshWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        // A worker cannot start before Application.onCreate, so this is always true. It
        // is checked because a graph that is not installed would be a crash in a
        // background process nobody is looking at.
        if (!ClarityGraph.isInstalled) return Result.success()
        try {
            ClarityWidgets.refresh(applicationContext)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            // Reading the log or writing the snapshot failed. Nothing on a home screen
            // changes, which is the quiet failure this path is allowed to have, and it
            // is said out loud here because a stale widget is otherwise indistinguishable
            // from a correct one.
            Log.w(TAG, "the widget refresh failed: ${failure.message ?: "no detail"}")
        }
        return Result.success()
    }

    private companion object {
        const val TAG = "ClarityWidgets"
    }
}

/**
 * Arms the refresh, once per process, from `ClarityWidgets.install`.
 *
 * **A `PeriodicWorkRequest` here, where the Pulse reminder deliberately refuses one.**
 * `PulseReminderScheduler` chains one hop at a time because its target is a wall clock
 * hour, and a period measured from the previous run drifts across a daylight saving
 * change. This has no hour to hit: 13.3 asks for a refresh every six hours, and six
 * hours after the last one is exactly what that means. The platform is also better at
 * batching a periodic request than at anything a chain could do.
 *
 * No constraints, deliberately. A refresh that waited for a charger would leave a home
 * screen stale for a day, and a network constraint would need the one permission this
 * app removes from the merged manifest on purpose. See the manifest.
 */
object WidgetRefreshSchedule {

    /** One refresh exists at a time, and enqueuing replaces rather than adds. */
    const val UNIQUE_NAME: String = "com.kamsiob.claritynow.widget.refresh"

    /** Six hours, MASTER_BUILD_PROMPT 13.3. Not a number to tune until it looks right. */
    private const val PERIOD_HOURS = 6L

    fun ensure(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            PERIOD_HOURS,
            TimeUnit.HOURS,
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            // UPDATE rather than KEEP, so that a build which changes the period takes
            // effect on the next launch. KEEP would leave a phone that installed this
            // version running whatever the first version it ever saw had asked for.
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
