package com.kamsiob.claritynow

import android.app.Activity
import android.app.Application
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.domain.engine.catalog.CorpusVolume
import com.kamsiob.claritynow.domain.pulse.CorpusSource
import com.kamsiob.claritynow.domain.pulse.CorpusText
import com.kamsiob.claritynow.domain.pulse.PulseCoordinator
import com.kamsiob.claritynow.domain.pulse.PulseOutcome
import com.kamsiob.claritynow.notifications.ClarityNotifications
import com.kamsiob.claritynow.widget.ClarityWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClarityApp : Application() {

    /**
     * The one piece of work in this app that belongs to the process rather than to
     * a screen, so it is the one thing that needs a scope living here.
     *
     * [Dispatchers.Default] rather than the main dispatcher, deliberately. The work
     * below opens the database, replays the log and may append one event, and it is
     * started from a foreground callback that runs immediately before the first
     * frame. A coroutine launched on the main dispatcher would run inline until its
     * first suspension point, which is exactly the wrong place to put a cold start
     * replay. A [SupervisorJob] because a failure here must not take anything else
     * in the scope down with it: presence is the least important thing the app does
     * and the person is trying to look at their queue.
     */
    private val presenceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * The Pulse lifecycle, built once for the process. MASTER_BUILD_PROMPT 11.3.
     *
     * **The catalog inside it is built once and held**, per 11.7: it parses three
     * markdown files and runs its integrity checks, so it is not a per invocation cost.
     * The firing history inside it is the opposite and is rebuilt on every invocation,
     * because it derives from a log that merges.
     *
     * **It belongs in [ClarityGraph] and is here instead**, because that file was not
     * this slice's to edit. It is a process scoped singleton built out of the graph's own
     * repository and clock, which is exactly what the graph holds, and every other screen
     * in the app reaches its dependencies through it rather than through the Application.
     * Moving it is one lazy binding there and [AssetCorpus] moving with it, since the only
     * thing it needs from this class is the asset manager.
     */
    val pulse: PulseCoordinator by lazy {
        PulseCoordinator(
            repository = ClarityGraph.repository,
            clock = ClarityGraph.clock,
            corpus = AssetCorpus(assets),
        )
    }

    override fun onCreate() {
        super.onCreate()
        ClarityGraph.install(this)
        // Creates the notification channels and starts watching for a focus session.
        // **It posts nothing and asks for nothing**: a channel needs no permission and
        // shows no prompt, and the first notification of any kind waits for a person to
        // start a session. It is here rather than in an Activity because a session
        // outlives every screen, per design-v3.md 10.15, and because the two Live
        // Update actions reach this process through a broadcast receiver that can wake
        // it with no Activity at all. See ClarityNotifications.install.
        ClarityNotifications.install(this)
        // The widget snapshot, MASTER_BUILD_PROMPT 13.3. Like the line above it, this
        // posts nothing, shows nothing and needs no permission. It follows the
        // projection and keeps the home screen in step with it, and a person with no
        // widget placed pays one collector for it and nothing else.
        ClarityWidgets.install(this)
        registerActivityLifecycleCallbacks(ForegroundPresence())
    }

    /**
     * Writes the presence marker on the first foreground of each calendar day.
     * MASTER_BUILD_PROMPT 14b.4, Addendum 01 2d and 4d, issue #27.
     *
     * `APP_OPENED` is a date key and nothing else, and it exists for exactly one
     * reader: the gap detection that lets a person who has been away a fortnight be
     * met with something other than a count of what they did not do. It is excluded
     * from `ClarityEventType.isUserActivity`, renders no Trail row, and is absent
     * from the Trail day header count. DECISIONS.md C7.
     *
     * **Foreground, not process start, and the difference is not academic.**
     * `Application.onCreate` is the obvious place and is wrong twice over. This
     * process is created by a widget update and by a scheduled refresh as well as
     * by a person, from phase 12 onward, and neither of those is somebody opening
     * the app. It also runs once per process, so a phone that keeps the process
     * alive across midnight would never write the second day's marker at all. The
     * gap that produced would be an absence nobody had, reported to somebody who
     * had opened the app every single day.
     *
     * **Counting started activities rather than watching one activity.** The
     * platform's own answer to "the app came to the foreground" is
     * `ProcessLifecycleOwner`, which lives in `androidx.lifecycle:lifecycle-process`
     * and is not a dependency of this module. What it wraps is this: the count of
     * started activities crossing zero. Written out here rather than added as a
     * library, because it is a counter and two callbacks and the alternative was a
     * dependency for a counter and two callbacks. `MainActivity.onStart` would
     * answer correctly today, since there is one activity, and would quietly become
     * "the queue screen appeared" the first time a second one is added.
     *
     * Both callbacks arrive on the main thread, which is what makes the counter safe
     * without a lock. A configuration change this activity does not declare in the
     * manifest destroys and recreates it, taking the count through zero and firing
     * this a second time; that costs one indexed read and can write nothing, because
     * the guard that decides is a query against the log.
     *
     * **The ordering below is load bearing.** `ClarityRepository.recordAppOpened`
     * commits, and every commit asserts that the log has been loaded. The
     * ViewModels call `load` too and it is idempotent under the repository's own
     * lock, so whichever of them arrives first does the work and the other returns.
     * Calling `recordAppOpened` alone would be a crash on first foreground rather
     * than a missing event.
     *
     * **The Pulse runs last, and that is load bearing too.** MASTER_BUILD_PROMPT
     * 14b.4 suppresses the Pulse for the first two days after a return from a long
     * absence, and the return is detected by comparing today's `APP_OPENED` against
     * the newest one before it. Generating before the marker is written would find
     * no return on the one day it matters, and the person coming back after a
     * fortnight would be met by exactly the observation about their absence that the
     * whole of 14b.4 exists to prevent.
     *
     * It is one call because generation is idempotent by construction: an entry for
     * today stops it, and that check is a lookup in the in memory projection. The
     * second foreground of a day costs a map read.
     */
    private inner class ForegroundPresence : Application.ActivityLifecycleCallbacks {

        private var startedActivities = 0

        override fun onActivityStarted(activity: Activity) {
            if (startedActivities++ > 0) return
            presenceScope.launch {
                val repository = ClarityGraph.repository
                repository.load()
                repository.recordAppOpened()
                report(pulse.generateOnForeground())
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (startedActivities > 0) startedActivities -= 1
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    /**
     * One line in logcat saying what the Pulse did, and nothing anywhere else.
     *
     * **It prints the outcome and never the observation.** A sentence about somebody's
     * own week does not belong in a system log that any process with the permission can
     * read, and nothing here is an analytic: it is written to the device, it leaves
     * nothing behind, and it exists because three of the four outcomes are silence and
     * silence is indistinguishable from breakage from the outside. CLAUDE.md asks for
     * logcat to be checked after every device test, and this is what that check reads.
     */
    private fun report(outcome: PulseOutcome) {
        when (outcome) {
            is PulseOutcome.Present -> Log.i(
                PULSE_TAG,
                "${outcome.pulse.entry.dateKey} ${outcome.pulse.state} " +
                    "family=${outcome.pulse.entry.family} generated=${outcome.justGenerated}",
            )

            is PulseOutcome.Silent ->
                Log.i(PULSE_TAG, "${outcome.dateKey} IDLE, ${outcome.reason}")

            is PulseOutcome.Suppressed ->
                Log.i(PULSE_TAG, "${outcome.dateKey} IDLE, suppressed after a return, 14b.4")

            // Warn rather than info. This one is a packaging fault and it is the only
            // outcome that means the app cannot speak at all.
            is PulseOutcome.Unavailable ->
                Log.w(PULSE_TAG, "no Pulse: ${outcome.reason}")
        }
    }

    /**
     * The three corpus files, read out of the packaged assets.
     *
     * They are assets rather than Kotlin constants because CLAUDE.md's authority order
     * gives the corpus the last word on the wording of every sentence, and a copy of a
     * corpus embedded in code is a second corpus that drifts. The build copies the three
     * committed markdown files into `assets/corpus/` so that the file an author edits is
     * the file the app reads.
     *
     * A missing or malformed asset throws, and [PulseCoordinator] turns that into
     * [PulseOutcome.Unavailable] rather than into a crash. The app is entirely usable
     * with no Pulse.
     */
    private class AssetCorpus(private val assets: AssetManager) : CorpusSource {

        override suspend fun read(): CorpusText = withContext(Dispatchers.IO) {
            CorpusText(
                pulse = read(CorpusVolume.PULSE),
                report = read(CorpusVolume.REPORT),
                momentum = read(CorpusVolume.MOMENTUM),
            )
        }

        private fun read(volume: CorpusVolume): String =
            assets.open(CorpusSource.assetPathOf(volume)).bufferedReader().use { it.readText() }
    }

    private companion object {

        /** `adb logcat -s ClarityPulse` is the whole of the Pulse's diagnostics. */
        const val PULSE_TAG = "ClarityPulse"
    }
}
