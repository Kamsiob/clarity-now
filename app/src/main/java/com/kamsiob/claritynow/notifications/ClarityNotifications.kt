package com.kamsiob.claritynow.notifications

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.compose.ui.graphics.toArgb
import com.kamsiob.claritynow.data.repo.FOCUS_TRANSITION_WARNING_SECONDS
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.di.ClarityGraph
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.ui.theme.calmed
import com.kamsiob.claritynow.ui.theme.parseAreaColor
import com.kamsiob.claritynow.ui.theme.resolveCalmMode
import com.kamsiob.claritynow.work.PulseReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps the notification shade agreeing with the focus screen, for the whole of a
 * session and for none of the rest of the time.
 *
 * **It reads the app's one ticker rather than running a second one.** design-v3.md
 * 8.2 item 7 puts the countdown on a single 1Hz Flow, and `ClarityRepository`
 * publishes it as `focusCountdown`. Everything below is a collector of that,
 * so the Live Update, the ongoing notification and the ring cannot drift apart: a
 * notification that disagrees with the screen by a second is a notification a person
 * stops trusting, and there is no arithmetic here that could produce one.
 *
 * **The shade renders minutes, so it is written to once a minute.** The ticker still
 * emits every second, and [FocusNotificationModel.renderKey] is what decides that
 * fifty nine of those seconds change nothing anybody can see. That is a throttle on
 * posting and not a second clock.
 *
 * ### What it decides, and what it deliberately does not
 *
 * | moment | what happens here |
 * |---|---|
 * | a session is running | the Live Update, or the ongoing notification where the platform will not promote one |
 * | five minutes remain, warning on, app in the foreground | [transitionWarnings] emits, and the surface fires the haptic |
 * | five minutes remain, warning on, app elsewhere, no Live Update | one silent notification on the Ongoing channel |
 * | five minutes remain, warning on, app elsewhere, Live Update posted | nothing, because the track reaching its point already is the signal, design-v3.md 11.4 |
 * | the planned time runs out, app in the foreground | the running notification goes and nothing is posted; the focus screen is already showing it |
 * | the planned time runs out, app elsewhere | one gentle notification, and the session resolves on the next resume or from that notification, MASTER_BUILD_PROMPT 10 |
 * | the session ends, however it ended | everything is cleared and nothing takes its place |
 *
 * ### The Pulse reminder is wired here and posted somewhere else
 *
 * Phase 6 added two collectors to [install] and no fourth notification to this object.
 * One follows the two reminder preferences and keeps a piece of WorkManager work armed,
 * `PulseReminderScheduler`; the other takes the reminder out of the shade once the day
 * has been answered. **Neither of them can post anything**, which is deliberate: the
 * reminder is posted by `PulseReminderWorker` alone, holding a token it can only obtain
 * from an entry that exists and is unanswered, per MASTER_BUILD_PROMPT 12.1.
 *
 * They live here because this is the app's one process wide hook that runs before any
 * screen and outlives all of them, which is the same reason the channels are created
 * here. A reminder somebody switched on has to survive the app never being opened.
 *
 * **No event is written here.** In particular nothing in this file writes
 * `FOCUS_COMPLETED` when a session's planned time runs out while the app is away,
 * and that is load bearing rather than an omission: `ClarityRepository.restoreFocus`
 * is what resolves that session, it can only do so while the session is still
 * running, and a completion written from here would take the completion screen away
 * from the person who is owed it. The notification announces; the app resolves.
 *
 * **The one thing this cannot do, stated rather than hidden.** If the process is
 * killed while a session is running and the planned time then runs out, no
 * completion notification is posted, because there is nothing alive to post it. The
 * ongoing notification stays in the shade with its chronometer at zero, because the
 * platform draws that, and the session resolves at the next resume, which is the
 * other half of what MASTER_BUILD_PROMPT 10 allows. The fix would be an alarm, and
 * an exact alarm needs a permission MASTER_BUILD_PROMPT 18 puts out of scope, while
 * an inexact one can be held by Doze for a quarter of an hour and would announce the
 * end of a session long after it ended. **Telling someone with time blindness the
 * wrong time is worse than telling them nothing**, and design-v3.md 11.3 already
 * refuses to promise a cadence a surface cannot deliver.
 */
object ClarityNotifications {

    private val _transitionWarnings = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /**
     * Fires once when a running session reaches five minutes remaining **while the
     * app is in the foreground**, and only when the transition warning is on.
     * Addendum 01 4g, issue #30, design-v3.md 10.18.
     *
     * The focus surface collects this to fire the `transitionWarn` haptic in
     * design-v3.md 9 and to change the word beneath the numeral. It carries the
     * session id and nothing else, because the signal is a moment rather than a
     * measurement: it fires once, it does not repeat, and nothing counts down after
     * it.
     *
     * **Nothing is replayed.** A subscriber that arrives after the moment has passed
     * gets nothing, which is correct: the mark on the ring track has been sitting
     * there since the session started and the numeral already reads what it reads.
     */
    val transitionWarnings: SharedFlow<String> = _transitionWarnings.asSharedFlow()

    /**
     * Creates the channels, starts watching for a session and for the Pulse reminder
     * settings, and starts counting started activities so the two rules that turn on
     * where the person is looking can be answered.
     *
     * Called once, from `Application.onCreate`, immediately after
     * `ClarityGraph.install`. **It posts nothing and asks for nothing**: creating a
     * channel needs no permission and shows no prompt, and the first notification of
     * any kind waits for a person to start a session.
     *
     * Counting started activities rather than taking a dependency on
     * `ProcessLifecycleOwner` follows what `ClarityApp` already does for presence,
     * and for the same reason: the platform's own answer to "the app is in front of
     * the person" is this counter, and it is a counter and two callbacks. Both
     * callbacks arrive on the main thread, which is what makes it safe without a
     * lock.
     */
    fun install(application: Application) {
        if (installed || !ClarityGraph.isInstalled) return
        installed = true
        val context = application.applicationContext
        appContext = context
        poster = FocusNotificationPoster(context)
        ClarityNotificationChannels.ensure(context)
        application.registerActivityLifecycleCallbacks(foregroundCounter())
        scope.launch { watchFocusSessions() }
        // The Pulse reminder, MASTER_BUILD_PROMPT 12.1. Both of these are collectors
        // rather than one call, and neither posts anything at process start: the first
        // arms a piece of work for tonight, and the second only ever takes something
        // out of the shade.
        scope.launch { PulseReminderScheduler.watchSettings(context) }
        scope.launch { watchAnsweredPulses(context) }
    }

    /**
     * The person swiped the running notification away. See
     * [FocusIntents.ACTION_DISMISSED] for why this is honored rather than overridden
     * by the next post.
     */
    internal fun onRunningDismissed(sessionId: String) {
        dismissedSessionId = sessionId
    }

    // The collector -----------------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun watchFocusSessions() {
        val repository = ClarityGraph.repository
        val preferences = ClarityGraph.preferences

        // The projection has to be real before an empty answer can be trusted.
        // Without this, a cold start would read "no session running" for as long as
        // the replay takes and clear a notification that is correctly still there.
        // Idempotent under the repository's own lock, which is why the app shell
        // calling it too costs nothing.
        repository.load()

        repository.runningFocusSession
            .map { it?.id }
            .distinctUntilChanged()
            // The ticker is attached only while a session is running. Collecting
            // focusCountdown at rest would hold a coroutine awake once a second for
            // the life of the process with nothing to show for it.
            .flatMapLatest { sessionId ->
                if (sessionId == null) {
                    flowOf(null)
                } else {
                    combine(
                        repository.focusCountdown,
                        repository.state,
                        preferences.transitionWarningEnabled,
                        preferences.calmMode,
                    ) { countdown, state, warningEnabled, storedCalmMode ->
                        modelFor(countdown, state, warningEnabled, storedCalmMode)
                    }
                }
            }
            .distinctUntilChangedBy { it?.renderKey }
            .collect { model -> render(model) }
    }

    /**
     * Takes the reminder out of the shade once the day's Pulse has been answered.
     *
     * A notification that asks somebody to answer a question they have already answered
     * is the app failing to notice what they did, which is the one thing this app is
     * supposed to be good at. Tapping it already dismisses it; this is the person who
     * opened the app some other way, which is most of them.
     *
     * **It cancels and never posts.** There is exactly one thing in this app that can
     * post the reminder and it is `PulseReminderWorker`, holding a token it can only
     * get from an unanswered entry. A cancel needs no permission, no token and no
     * notification to have been posted in the first place.
     *
     * Reading the day from the clock on each emission is the same arrangement the Areas
     * chip uses. A process alive across midnight cancels against the new day, which can
     * only ever mean one redundant cancel.
     */
    private suspend fun watchAnsweredPulses(context: Context) {
        val repository = ClarityGraph.repository
        val clock = ClarityGraph.clock
        val reminders = PulseReminderPoster(context)
        repository.state
            .map { state -> state.pulses[clock.dateKey()]?.isAnswered == true }
            .distinctUntilChanged()
            .collect { answered -> if (answered) reminders.clear() }
    }

    private fun modelFor(
        countdown: FocusCountdown?,
        state: ClarityState,
        warningEnabled: Boolean,
        storedCalmMode: Boolean?,
    ): FocusNotificationModel? {
        if (countdown == null) return null
        val area = state.areas[countdown.areaId] ?: return null
        val item = state.items[countdown.itemId] ?: return null
        val calm = resolveCalmMode(storedCalmMode, systemAsksForReducedMotion())
        return FocusNotificationModel(
            sessionId = countdown.sessionId,
            areaName = area.name,
            itemTitle = item.title,
            // design-v3.md 16.3: calm mode reaches this surface as color and as the
            // absence of anything that would have moved. The track still depletes,
            // because that is the surface's only content.
            accent = parseAreaColor(area.colorHex).calmed(calm).toArgb(),
            plannedSeconds = countdown.plannedSeconds,
            remainingSeconds = countdown.remainingSeconds,
            endsAtMillis = countdown.endsAtMillis,
            // The point is drawn from the moment the session starts, per
            // design-v3.md 11.4, and only when the warning is on and the session is
            // long enough to have a five minute mark that is not its beginning.
            transitionMarkSeconds = FOCUS_TRANSITION_WARNING_SECONDS
                .takeIf { warningEnabled && countdown.transitionMarkFraction != null },
        )
    }

    private fun render(model: FocusNotificationModel?) {
        val surfaces = poster ?: return
        if (model == null) {
            surfaces.clearRunning()
            surfaces.clearTransitionWarning()
            // The session is resolved, so an announcement that it finished has
            // nothing left to announce. Tapping it had already dismissed it; this is
            // the person who opened the app some other way.
            if (completionPosted) surfaces.clearCompletion()
            currentSessionId = null
            warningArmed = false
            completionPosted = false
            return
        }

        if (model.sessionId != currentSessionId) {
            currentSessionId = model.sessionId
            warningArmed = false
            completionPosted = false
            dismissedSessionId = null
            surfaces.clearCompletion()
        }

        if (model.remainingSeconds <= 0) {
            onPlannedTimeElapsed(surfaces, model)
            return
        }

        updateTransitionWarning(surfaces, model)
        if (dismissedSessionId == model.sessionId) return
        surfaces.showRunning(model)
    }

    /**
     * The planned time has run out. MASTER_BUILD_PROMPT 10.
     *
     * The running notification goes either way, because the session is no longer
     * running and 13.4 requires it to be dismissed when the session ends. What
     * follows depends only on where the person is looking: in the app, the focus
     * screen is already showing the completion state and a notification would be
     * telling them something they can see; somewhere else, one gentle notification,
     * which is the only thing this app posts that a person did not ask for in the
     * previous few seconds and is the thing MASTER_BUILD_PROMPT 10 asks for by name.
     *
     * It is posted even to somebody who swiped the running notification away. That
     * dismissal was of a countdown they did not want in their shade, and this is the
     * end of the session rather than more of it.
     */
    private fun onPlannedTimeElapsed(
        surfaces: FocusNotificationPoster,
        model: FocusNotificationModel,
    ) {
        surfaces.clearRunning()
        surfaces.clearTransitionWarning()
        if (completionPosted) return
        completionPosted = true
        if (isForeground) return
        surfaces.showCompletion(model)
    }

    /**
     * Arms at more than five minutes and fires once on the way past it. Addendum 01
     * 4g, issue #30, design-v3.md 10.18.
     *
     * **Every case that issue asks about falls out of two lines rather than being
     * handled.** A session with five minutes or less on it when it starts never gets
     * above the mark, so it never arms and fires nothing. Switching the warning on
     * inside the last five minutes finds it disarmed and fires nothing, which is the
     * rule in MASTER_BUILD_PROMPT 14b.5 about a warning that arrives the instant it
     * is switched on and teaches a person to distrust it. Adding ten minutes puts the
     * session back above the mark and arms it again, exactly once, so two extensions
     * are two firings. Switching it off mid session disarms it for that session,
     * because the mark stops existing at all.
     *
     * **It is not a countdown and it does not escalate.** One event, at one known
     * moment, that a person went looking for. Issue #30 names a four, three, two and
     * one minute warning as the way this feature becomes a worse one.
     */
    private fun updateTransitionWarning(
        surfaces: FocusNotificationPoster,
        model: FocusNotificationModel,
    ) {
        val step = transitionWarningStep(
            markSeconds = model.transitionMarkSeconds,
            remainingSeconds = model.remainingSeconds,
            wasArmed = warningArmed,
        )
        warningArmed = step.armed
        if (!step.fires) return
        when {
            // In the app it is an in app signal and never a notification, which
            // Addendum 01 4g states as the whole shape of the feature.
            isForeground -> _transitionWarnings.tryEmit(model.sessionId)
            // With a Live Update on screen, the track arriving at its point already
            // carried the moment, and a second surface saying the same thing is the
            // escalation this feature is defined against. design-v3.md 10.18.
            surfaces.canPromote() -> Unit
            else -> surfaces.showTransitionWarning(model)
        }
    }

    private fun systemAsksForReducedMotion(): Boolean {
        val resolver = appContext?.contentResolver ?: return false
        val scale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        return scale == 0f
    }

    // Where the person is looking ---------------------------------------------

    private val isForeground: Boolean get() = startedActivities > 0

    private fun foregroundCounter(): Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivities += 1
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

    // Process state -----------------------------------------------------------

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var installed = false
    private var appContext: Context? = null
    private var poster: FocusNotificationPoster? = null

    private var currentSessionId: String? = null
    private var warningArmed = false
    private var completionPosted = false

    @Volatile
    private var startedActivities = 0

    @Volatile
    private var dismissedSessionId: String? = null
}

/**
 * Whether the transition warning is armed after this tick, and whether it fires on
 * this one. Addendum 01 4g, design-v3.md 10.18, issue #30.
 *
 * **A pure function of three values, deliberately, because this is the part of the
 * feature that is worth a test and the rest of the file is not testable without a
 * device.** Issue #30 asks for three assertions that all live here: a five minute
 * session fires nothing, a session extended twice fires exactly twice, and the signal
 * fires once rather than counting down.
 *
 * [markSeconds] is null when there is no mark: the warning is switched off, or the
 * session was never long enough to have a five minute point that is not its
 * beginning. Both mean the same thing to this function, which is that nothing arms
 * and nothing fires.
 *
 * Arming above the mark and firing on the way past it is what makes every one of
 * those cases fall out rather than be handled. A session that starts below the mark
 * never arms. A warning switched on below the mark finds it disarmed. An extension
 * lifts the session above the mark, which arms it again, once.
 */
internal fun transitionWarningStep(
    markSeconds: Int?,
    remainingSeconds: Int,
    wasArmed: Boolean,
): TransitionWarningStep = when {
    markSeconds == null -> TransitionWarningStep(armed = false, fires = false)
    remainingSeconds > markSeconds -> TransitionWarningStep(armed = true, fires = false)
    wasArmed -> TransitionWarningStep(armed = false, fires = true)
    else -> TransitionWarningStep(armed = false, fires = false)
}

/** The answer [transitionWarningStep] gives. */
internal data class TransitionWarningStep(val armed: Boolean, val fires: Boolean)
