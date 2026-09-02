package com.kamsiob.claritynow.ui.focus

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.data.repo.FOCUS_EXTENSION_SECONDS
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.data.repo.FocusRestore
import com.kamsiob.claritynow.data.repo.focusEndingIsSilent
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One row in the chooser. MASTER_BUILD_PROMPT section 10.
 *
 * An area with no active item is present and dimmed rather than absent, because the
 * list of areas is the same list every time and a row that comes and goes is a list a
 * person has to re-read. [activeItemId] being null is the whole of what makes a row
 * unselectable, so there is no second flag that could disagree with it.
 */
@Immutable
data class FocusAreaOption(
    val areaId: String,
    val areaName: String,
    val colorHex: String,
    val activeItemId: String?,
    val activeItemTitle: String?,
    /**
     * The active item's first step. `ADDENDUM_01` 4b, and issue #62.
     *
     * 4b exists so that a person who cannot start has already written down how to, and
     * the moment it is for is this one: the chooser is where somebody decides which
     * thing to sit down with. It was on the card, in the area sheet and on a widget,
     * and missing from the two Focus screens, which are the two the field was written
     * for.
     */
    val activeItemFirstStep: String? = null,
) {
    val selectable: Boolean get() = activeItemId != null
}

/**
 * The facts a running session's screen draws that do not change once a second.
 *
 * Held apart from [FocusCountdown] on purpose. design-v3.md 8.2 item 7 has only the
 * numeral and the arc redrawing on a tick, and the cheapest way to keep that true is
 * for the area name, the item title and the color to live in a value the tick cannot
 * touch. [plannedSeconds] rides along so the ring can be drawn correctly in the one
 * frame between a session starting and the first tick arriving.
 */
@Immutable
data class FocusSessionModel(
    val sessionId: String,
    val areaId: String,
    val areaName: String,
    val colorHex: String,
    val itemId: String,
    val itemTitle: String,
    val plannedSeconds: Int,
    /** The item's first step, read under the title while the ring runs. Issue #62. */
    val itemFirstStep: String? = null,
)

/**
 * A session that has finished, in the one shape both endings use.
 *
 * **There is no field here saying which kind of ending this was**, and that absence is
 * the design. Addendum 01 4e and design-v3.md 11: a session ended early is a completed
 * short session, reaches the same screen, in the same words, with the same actions and
 * no qualifier. A boolean recording that this one was cut short would be a thing the
 * screen could later be taught to render, and the only honest way to keep the screen
 * from ever saying it is not to hand it the fact.
 *
 * [minutes] is the real duration and never a comparison against the plan.
 *
 * **There is no color on this model either.** design-v3.md section 11 gives the
 * completion screen a serif line, a title, one small line and two actions, and the area
 * is already named in words. A hex that nothing draws is an invitation to draw it.
 *
 * [announce] is not about the ending either: it records whether the person was looking
 * at the ring when the time ran out, which decides whether the `focusEnd` haptic in
 * design-v3.md 9 fires. A session resolved on the next resume must not fire it, because
 * section 9 forbids a haptic on screen entry.
 */
@Immutable
data class FocusCompletionModel(
    val sessionId: String,
    val areaName: String,
    val itemId: String,
    val itemTitle: String,
    val minutes: Int,
    val canCompleteItem: Boolean,
    val announce: Boolean,
)

/** Which of the Focus surface's screens is showing. */
sealed interface FocusPhase {

    /** The log is still being read. Nothing is drawn over the backdrop. */
    data object Loading : FocusPhase

    data class Choosing(
        val options: List<FocusAreaOption>,
        val durationMinutes: Int,
    ) : FocusPhase

    data class Running(val session: FocusSessionModel) : FocusPhase

    data class Complete(val completion: FocusCompletionModel) : FocusPhase

    /**
     * The flow is finished and the caller should navigate away.
     *
     * A phase rather than a callback fired from a click handler, because every exit
     * this app takes writes something first: ending a session, completing an item.
     * A screen that navigated away on the tap would take its ViewModel scope with it
     * and could cancel that write halfway. Reaching this state means the write has
     * already returned.
     */
    data object Dismissed : FocusPhase
}

/**
 * The Focus surface. MASTER_BUILD_PROMPT section 10 and 14b.5, design-v3.md 11.
 *
 * **Nothing here decides whether a session may start, end or be extended.** Those
 * rules are pure functions in `data/repo/FocusSession.kt` and are applied inside the
 * repository, which is the only writer in the app: a chooser is not the only door into
 * a session, and a rule enforced in a screen holds until the next screen. What this
 * class does is choose which of the surface's three screens is showing and hand each
 * one exactly the values it draws.
 *
 * **The countdown is not folded into [phase].** It is exposed separately as
 * [countdown] so that a value arriving once a second reaches the ring and the numeral
 * and nothing else, per design-v3.md 8.2 item 7. Folding it in would recompose the
 * item title, the End pill and the backdrop every second for no reason.
 *
 * **Back is not modeled here at all**, which is deliberate. design-v3.md 10.15 makes
 * back navigating away from a running session and leaving it running, and the surest
 * way to keep that true is that there is nothing in this class for a back handler to
 * call. Ending a session is [endSession] and it has exactly two callers, both of them
 * the End session control.
 */
class FocusViewModel(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
) : ViewModel() {

    /**
     * The three pieces of state that belong to this screen rather than to the log.
     *
     * One value rather than three flows because [phase] combines four sources and
     * `combine` starts costing more than it explains past that.
     */
    private data class Local(
        val loading: Boolean = true,
        val completion: FocusCompletionModel? = null,
        val dismissed: Boolean = false,
    )

    private val local = MutableStateFlow(Local())

    /**
     * The one countdown in the app, shared with the ongoing notification and the Live
     * Update rather than derived a second time here. Null whenever this device has no
     * running session, which is the ordinary state.
     */
    val countdown: StateFlow<FocusCountdown?> = repository.focusCountdown

    /**
     * design-v3.md 10.18. Off by default, and the Settings row that turns it on
     * arrives in phase 11. Read here rather than inside the ring so that the ring
     * draws what it is told and decides nothing.
     */
    val transitionWarningEnabled: StateFlow<Boolean> = preferences.transitionWarningEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE), false)

    val phase: StateFlow<FocusPhase> = combine(
        repository.state,
        repository.runningFocusSession,
        preferences.focusDurationMinutes,
        local,
    ) { state, running, durationMinutes, screen ->
        when {
            screen.dismissed -> FocusPhase.Dismissed
            screen.completion != null -> FocusPhase.Complete(screen.completion)
            running != null -> {
                // Never null in practice: the log tombstones rather than deleting rows,
                // so the area and the item behind a running session are always there to
                // be read. Loading rather than a crash if that ever stops being true.
                val model = state.sessionModel(running)
                if (model == null) FocusPhase.Loading else FocusPhase.Running(model)
            }

            screen.loading -> FocusPhase.Loading
            else -> FocusPhase.Choosing(state.options(), durationMinutes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE), FocusPhase.Loading)

    /**
     * The entry read: load the log, then ask the repository what to do about a session.
     *
     * **Held as a [Job] rather than run from an `init` block, because one thing outside
     * this class has to be able to wait for it.** A session asked for by name, by the
     * `First Step` widget, arrives at [startOnItem] on the frame this surface is
     * composed, and `ClarityRepository.startFocus` refuses while the log is unloaded. A
     * start that did not wait would therefore work on a warm start, where the log is
     * already read, and be silently dropped on a cold one, leaving the person on the
     * chooser. It is the classic version of that bug and this is where it is prevented.
     */
    private val entered: Job = viewModelScope.launch {
        repository.load()
        // Every entry into this surface asks the repository what to do about a
        // session, which is what makes a killed process land back on the ring with
        // the right time left and a session that ran out while the app was away
        // land on the completion screen. MASTER_BUILD_PROMPT section 10.
        val restored = repository.restoreFocus()
        val resolved = (restored as? FocusRestore.Completed)?.let { done ->
            repository.state.value.completionModel(
                session = done.session,
                actualSeconds = done.session.actualSeconds ?: done.session.plannedSeconds,
                announce = false,
            )
        }
        local.update { it.copy(loading = false, completion = resolved) }
    }

    init {
        // Natural completion, while somebody is watching the ring. The repository
        // refuses a second terminal event for the same session, so this racing with
        // the notification side or with another restore is safe: whoever arrives first
        // writes, and the loser writes nothing.
        viewModelScope.launch {
            repository.focusCountdown.collect { tick ->
                if (tick != null && tick.hasElapsed) completeNaturally(tick)
            }
        }
    }

    /**
     * Starts a session on an area's active item, at the length in settings.
     *
     * The length is read at the moment of starting rather than held, because a session
     * takes its duration from what the setting says now and nothing later re-reads it.
     */
    fun start(areaId: String, itemId: String) {
        viewModelScope.launch {
            val minutes = preferences.focusDurationMinutes.first()
            local.update { it.copy(completion = null) }
            repository.startFocus(areaId, itemId, minutes * SECONDS_PER_MINUTE)
        }
    }

    /**
     * Starts a session on one item named from outside, which is the `First Step`
     * widget's whole promise. MASTER_BUILD_PROMPT 13.3 and Addendum 01 6b.
     *
     * **It decides nothing about whether the session may start.** Whether one is already
     * running, whether [itemId] is still the active item of its area and whether the
     * duration is a duration are `canStartFocus`, applied inside the repository under
     * the one lock, exactly as they are for the chooser. A widget draws a snapshot that
     * is a few seconds old at best, so a refusal here is an ordinary outcome rather than
     * an error: the surface is already showing, and it shows the chooser, which is what
     * the same widget sends for itself when it has no item to name.
     *
     * **The area comes from the log rather than from the intent.** The `First Step`
     * intent carries one, and it is deliberately not read, for the reason `MainActivity`
     * has always given about the session id on a notification: an item belongs to
     * exactly one area, the log says which, and a second copy taken from a snapshot can
     * only ever be a staler opinion about a fact with one answer.
     */
    fun startOnItem(itemId: String) {
        viewModelScope.launch {
            entered.join()
            // A session that ran out while the app was away is owed its completion
            // screen once, and the entry read above has already resolved it. [phase]
            // shows a completion ahead of a running session, so starting over the top of
            // one would run a new session behind a screen about a different one.
            if (local.value.completion != null) return@launch
            val areaId = repository.state.value.items[itemId]?.areaId ?: return@launch
            val minutes = preferences.focusDurationMinutes.first()
            repository.startFocus(areaId, itemId, minutes * SECONDS_PER_MINUTE)
        }
    }

    /**
     * Sets the session length from the chooser. Issue #62.
     *
     * **The same preference `Settings` writes, and there is no second one.** A length
     * chosen here is the length Settings then shows, which is the point: changing it used
     * to mean leaving the surface, finding a row four screens away and coming back, and
     * the person doing that is holding a decision they have already made.
     *
     * Nothing is appended to the log, exactly as nothing is appended when Settings writes
     * it. `SettingsViewModel.setAfterCompleting` says at its own declaration which
     * preferences belong in the log and which do not, and a session's length is read at
     * the moment of starting and recorded on `FOCUS_STARTED` as `plannedSeconds`, so the
     * log already carries the only version of this fact that a later reader can use.
     */
    fun setDurationMinutes(minutes: Int) {
        viewModelScope.launch { preferences.setFocusDurationMinutes(minutes) }
    }

    /**
     * Ends a running session before its planned time. design-v3.md 10.15 and section 10.
     *
     * [elapsedSeconds] is a real duration and is never compared against the plan. Under
     * the threshold in `FocusSession.kt` the ending is a mis-tap: the event is still
     * written, because the log records what happened, and the interface shows no
     * completion screen and navigates away instead. Above it the session is a completed
     * short session and reaches the completion screen in the same words a full one does.
     *
     * The caller applies `focusEndingIsSilent` too, to decide whether to ask first. One
     * function, read twice, rather than a threshold written down in two places.
     */
    fun endSession(sessionId: String, elapsedSeconds: Int) {
        viewModelScope.launch {
            repository.endFocusEarly(sessionId, elapsedSeconds)
            val state = repository.state.value
            val session = state.focusSessions[sessionId]
            val completion = if (focusEndingIsSilent(elapsedSeconds) || session == null) {
                null
            } else {
                state.completionModel(session, actualSeconds = elapsedSeconds, announce = false)
            }
            local.update { it.copy(completion = completion, dismissed = completion == null) }
        }
    }

    /**
     * Addendum 01 4f. Adds ten minutes to the running session without ending it,
     * restarting it or opening a second one. Repeatable and uncapped, because a limit
     * is an argument with someone who is working.
     *
     * Nothing is returned and nothing is shown. design-v3.md 10.18: no confirmation, no
     * toast, no acknowledgment beyond the ring growing to its new length.
     */
    fun addTenMinutes(sessionId: String) {
        viewModelScope.launch { repository.extendFocus(sessionId, FOCUS_EXTENSION_SECONDS) }
    }

    /**
     * `Mark item complete` on the completion screen, design-v3.md 11.
     *
     * The session has already ended by the time this can be tapped, so this completes
     * an item and nothing else. With `After completing` set to choose from the queue the
     * area is left idle, which design-v3.md 10.15 calls a real state: the queue chooser
     * belongs to the Areas screen and opening it from the Contemplative world would put
     * a Daylight sheet over the indigo night.
     */
    fun markItemComplete(itemId: String) {
        viewModelScope.launch {
            repository.completeItem(itemId)
            local.update { it.copy(completion = null, dismissed = true) }
        }
    }

    /**
     * Leaves the surface. `Done`, and back from every phase.
     *
     * **This never ends a session and there is no argument that would let it.**
     * design-v3.md 10.15: back navigates away and leaves the session running. The
     * ongoing notification is the way back in and the Areas card shows the countdown.
     */
    fun leave() {
        local.update { it.copy(completion = null, dismissed = true) }
    }

    private suspend fun completeNaturally(tick: FocusCountdown) {
        repository.completeFocus(tick.sessionId, tick.plannedSeconds)
        val state = repository.state.value
        val session = state.focusSessions[tick.sessionId] ?: return
        // The planned seconds, not the wall clock gap. A session runs for the time it
        // was planned to run; anything else is a fact about the phone.
        val done = state.completionModel(session, actualSeconds = tick.plannedSeconds, announce = true)
        local.update { it.copy(completion = done) }
    }

    private fun ClarityState.options(): List<FocusAreaOption> = liveAreas.map { area ->
        val active = activeItemIn(area.id)
        FocusAreaOption(
            areaId = area.id,
            areaName = area.name,
            colorHex = area.colorHex,
            activeItemId = active?.id,
            activeItemTitle = active?.title,
            activeItemFirstStep = active?.firstStep,
        )
    }

    private fun ClarityState.sessionModel(session: FocusSessionState): FocusSessionModel? {
        val area = areas[session.areaId] ?: return null
        val item = items[session.itemId] ?: return null
        return FocusSessionModel(
            sessionId = session.id,
            areaId = session.areaId,
            areaName = area.name,
            colorHex = area.colorHex,
            itemId = session.itemId,
            itemTitle = item.title,
            plannedSeconds = session.plannedSeconds,
            itemFirstStep = item.firstStep,
        )
    }

    private fun ClarityState.completionModel(
        session: FocusSessionState,
        actualSeconds: Int,
        announce: Boolean,
    ): FocusCompletionModel? {
        val area = areas[session.areaId] ?: return null
        val item = items[session.itemId] ?: return null
        return FocusCompletionModel(
            sessionId = session.id,
            areaName = area.name,
            itemId = item.id,
            itemTitle = item.title,
            minutes = wholeMinutes(actualSeconds),
            // An item completed elsewhere, swapped out or deleted while the session ran
            // cannot be completed again, and offering a control that would do nothing is
            // worse than not offering it. `Done` is still there and still leaves.
            canCompleteItem = item.status == ItemStatus.ACTIVE && item.deletedAt == null,
            announce = announce,
        )
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60

        /** Matches the repository's own grace, so a rotation does not restart the log read. */
        const val SUBSCRIPTION_GRACE = 5_000L
    }
}

/**
 * Whole minutes from a duration in seconds, rounded to nearest.
 *
 * The same rule `domain/query/TrailRow.kt` applies, deliberately: the completion screen
 * and the Trail row for the same session are read minutes apart and disagreeing about
 * whether it was 14 or 15 minutes would make both of them look approximate.
 */
internal fun wholeMinutes(seconds: Int): Int = (seconds + 30) / 60
