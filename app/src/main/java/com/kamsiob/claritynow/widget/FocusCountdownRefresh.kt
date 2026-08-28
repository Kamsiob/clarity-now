package com.kamsiob.claritynow.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kamsiob.claritynow.data.widget.ClarityWidgetSnapshotStore
import com.kamsiob.claritynow.data.widget.WidgetFocus
import com.kamsiob.claritynow.di.ClarityGraph
import java.util.concurrent.TimeUnit

/**
 * The instant a widget computes against.
 *
 * The app's injected clock when the process is up, which it always is here: a widget
 * update and a worker both run inside this application, so `Application.onCreate` has
 * run and `ClarityGraph` is installed. The fallback exists because a widget that threw
 * would leave Glance's error layout on somebody's home screen until they removed it by
 * hand.
 */
internal fun widgetNowMillis(): Long =
    if (ClarityGraph.isInstalled) ClarityGraph.clock.nowMillis() else System.currentTimeMillis()

/**
 * How the Focus Countdown widget gets a new frame while a session is running.
 * `design-v3.md` 12.2 asks for the cadence to be chosen deliberately and the reasoning
 * recorded rather than tuned until it looks right, and this is that record.
 *
 * ## Once a minute
 *
 * **The digits read minutes and the arc moves in whole minutes, so a minute is the
 * shortest refresh that changes anything on screen.** Everything else follows from that.
 *
 * - **Not once a second.** Every widget update is an inter process call and a re-layout
 *   inside the launcher, paid all day for a shape somebody looks at a few times an hour.
 *   The system will not honor it either: work below a minute is batched, Doze holds it,
 *   and a design that asks for a per second frame is promising a precision Android does
 *   not deliver. `design-v3.md` 12.2's rule is that the arc's granularity never implies
 *   more than the refresh can carry, and the way to keep it is to make the granularity
 *   the refresh.
 * - **Not once every five or fifteen minutes.** That is the failure 12.2 names by name:
 *   an arc that jumps four minutes at a time is worse than one that moves in minutes and
 *   is honest about it. Fifteen minutes is also the floor on a `PeriodicWorkRequest`,
 *   which is why this is a chain of one time requests instead. The same shape, and for
 *   the same reason, as `work/PulseReminderScheduler.kt`.
 * - **Not the widget's own `updatePeriodMillis`.** The platform's floor there is thirty
 *   minutes and it keeps running when no session does. It is set to zero in each
 *   provider's metadata, and every update this app's widgets get is one it asked for.
 *
 * ## The chain arms itself and dies on its own
 *
 * [arm] is called from `FocusCountdownWidget.provideGlance`, which runs on every render.
 * While a session is running it enqueues the next hop under [ExistingWorkPolicy.KEEP],
 * so a hop that is already pending or already running is left alone and a render can
 * never displace one. [FocusCountdownRefreshWorker] draws a frame and then arms the next
 * hop itself.
 *
 * **Nothing here ever cancels.** That is the deliberate choice and not an omission. The
 * obvious implementation cancels the chain the moment the snapshot has no session, and
 * it has a hole in it: the update that would notice the session had ended is the same
 * update the cancel would interrupt, because the cancel would be issued from inside the
 * worker's own render. So the session ending is simply a hop that finds nothing to
 * arm: one last frame is drawn, showing the `Start focus` state, and no successor is
 * enqueued. A pending hop left over from a session that ended is one wakeup that
 * redraws a widget correctly and stops, which costs less than the failure mode it
 * removes.
 *
 * A widget that has been removed from the home screen stops the chain at the next hop,
 * because [FocusCountdownRefreshWorker] asks whether any instance is placed before it
 * does anything else.
 *
 * **It is not an exact alarm and does not try to be.** An exact alarm needs
 * `SCHEDULE_EXACT_ALARM`, and `MASTER_BUILD_PROMPT.md` 18 puts every permission beyond
 * notifications out of scope. A frame that arrives a little late still draws the right
 * shape, because the widget subtracts from the stored end instant at render rather than
 * counting frames.
 */
internal object FocusCountdownRefresh {

    /** One chain at a time, per device. */
    const val UNIQUE_NAME: String = "com.kamsiob.claritynow.widget.focus.countdown"

    /**
     * Arms the next frame if a session is running, and does nothing otherwise.
     *
     * [KEEP][ExistingWorkPolicy.KEEP] rather than `REPLACE`, because this is called from
     * inside a render and a render may be the one the running worker is performing.
     * Replacing there would cancel that worker mid update. A hop already in flight is
     * always within a minute of firing and re-arms itself when it does, so keeping it is
     * never more than a minute stale.
     */
    fun arm(context: Context, focus: WidgetFocus?, nowMillis: Long) {
        enqueue(context, focus, nowMillis, ExistingWorkPolicy.KEEP)
    }

    /**
     * Arms the frame after the one [FocusCountdownRefreshWorker] has just drawn.
     *
     * `REPLACE` from inside the running worker, which is the same act
     * `work/PulseReminderScheduler.kt` performs and has the same consequence: replacing
     * unique work cancels work that is running, and the work running at that moment is
     * the worker doing the calling. It has already drawn by then and everything after
     * this line is a return, so the run is recorded as canceled and nothing is lost.
     * Appending instead would leave a row per minute that can never be pruned.
     */
    fun rearm(context: Context, focus: WidgetFocus?, nowMillis: Long) {
        enqueue(context, focus, nowMillis, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(
        context: Context,
        focus: WidgetFocus?,
        nowMillis: Long,
        policy: ExistingWorkPolicy,
    ) {
        val remaining = focus?.let { it.endsAtMillis - nowMillis } ?: return
        if (remaining <= 0L) return
        // The last hop lands on the end instant rather than a minute after it, so the
        // widget reaches its finished state when the session does. Every other hop is a
        // minute. A floor keeps a clock that jumped from producing a busy loop.
        val delay = minOf(REFRESH_INTERVAL_MILLIS, remaining).coerceAtLeast(MIN_DELAY_MILLIS)
        val request = OneTimeWorkRequestBuilder<FocusCountdownRefreshWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            // No constraints, deliberately rather than by omission. A constraint would
            // need the network permission this app removes from the merged manifest on
            // purpose, and nothing here touches a network. See AndroidManifest.xml.
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, policy, request)
    }

    private const val REFRESH_INTERVAL_MILLIS = 60_000L

    private const val MIN_DELAY_MILLIS = 1_000L
}

/**
 * One frame of the Focus Countdown widget, and the arming of the next.
 *
 * The order below is load bearing. It asks whether the widget is placed before it draws,
 * so a widget somebody removed stops the chain rather than waking the device once a
 * minute forever. It reads the snapshot again after drawing rather than trusting the one
 * the render used, so a session that ended during the update is noticed here rather than
 * a minute later.
 *
 * **It never retries.** A frame that failed is a frame; the next hop is a minute away and
 * will draw the right thing. A retry would run at whatever time WorkManager chose and
 * would be drawing a countdown from an instant that had already passed.
 */
class FocusCountdownRefreshWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (!ClarityGraph.isInstalled) return Result.success()
        val placed = GlanceAppWidgetManager(applicationContext)
            .getGlanceIds(FocusCountdownWidget::class.java)
        if (placed.isEmpty()) return Result.success()

        FocusCountdownWidget().updateAll(applicationContext)

        val snapshot = ClarityWidgetSnapshotStore(applicationContext).read()
        FocusCountdownRefresh.rearm(applicationContext, snapshot?.focus, widgetNowMillis())
        return Result.success()
    }
}
