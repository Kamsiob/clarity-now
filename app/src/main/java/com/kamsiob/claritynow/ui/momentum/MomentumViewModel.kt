package com.kamsiob.claritynow.ui.momentum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.momentum.AreasBannerView
import com.kamsiob.claritynow.domain.momentum.MomentumCoordinator
import com.kamsiob.claritynow.domain.momentum.MomentumView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** What the Momentum surface draws, plus whether the log has been read yet. */
data class MomentumUiState(val loading: Boolean = true, val view: MomentumView? = null)

/**
 * The Momentum surface's state. `MASTER_BUILD_PROMPT.md` 12.2.
 *
 * **It composes no sentence and reaches no corpus.** The one sentence on the screen is
 * `MomentumView.headline`, which came out of the engine through [MomentumCoordinator],
 * which holds the catalog. 11.2 closes the list of things that may read a corpus and a
 * ViewModel is not on it.
 *
 * **It writes nothing.** Momentum records nothing at all, so unlike the Pulse there is no
 * generation to guard against a second caller and no event to append. Recomputing is free
 * of consequence and costs one read of the log.
 *
 * **Every date it works with comes from the injected clock inside the coordinator**, never
 * from `LocalDate.now()` and never from a composable. `ClarityClock` documents a date key
 * taken against a default zone as the cause of two Pulses in one day, and a dot row that
 * answered "which day is today" a second way would put the ring on the wrong dot for
 * anybody east or west of the machine that built it.
 */
class MomentumViewModel(
    private val repository: ClarityRepository,
    private val coordinator: MomentumCoordinator,
) : ViewModel() {

    /**
     * False until the log has been read.
     *
     * Without it the screen would draw one frame against the empty projection, which is
     * fourteen empty dots and no tiles, before the real fortnight arrived. `load` is
     * idempotent under the repository's own lock, so this costs nothing after the first
     * call of the process.
     */
    private val loaded = MutableStateFlow(false)

    /**
     * Recomposed whenever the projection changes, and only while something is collecting.
     *
     * The projection is the cheap signal that the log has grown; the composition itself
     * reads the whole log through the coordinator, which is why it is behind
     * `WhileSubscribed` rather than eagerly started. A person who never opens this tab
     * never pays for it.
     */
    val state: StateFlow<MomentumUiState> = combine(loaded, repository.state) { ready, _ -> ready }
        .map { ready ->
            if (!ready) MomentumUiState() else MomentumUiState(loading = false, view = coordinator.momentum())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
            initialValue = MomentumUiState(),
        )

    init {
        viewModelScope.launch {
            repository.load()
            loaded.value = true
        }
    }

    private companion object {

        /** Long enough to survive a rotation without recomposing the whole fortnight. */
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}

/**
 * The Areas banner's state, and **the one hour throttle lives here**.
 *
 * `CLARITY_LOGIC_ENGINE.md` 6.5 and `MASTER_BUILD_PROMPT.md` 11.2 both say where it goes:
 * in the ViewModel and not in the engine. The rule itself is [BannerThrottle], which is a
 * value with no Android in it so that the boundary can be walked in a test rather than
 * reasoned about.
 *
 * ## Why this is a ViewModel of its own rather than a field on `AreasViewModel`
 *
 * Two reasons and the second is the real one. It is resolved against the Activity's store,
 * so one instance serves every visit to the Areas tab and the throttle is not reset by a
 * tab switch, which is what "once per hour of app use" requires. And `AreasViewModel` is
 * the queue: it holds the areas, the swipes, the promotions and the running session, and
 * none of that has anything to do with a sentence from the engine. Keeping them apart also
 * meant this phase did not have to edit a file it does not own, which is a smaller reason
 * and an honest one.
 *
 * **It does not recompute on the projection.** The banner is a sentence about the shape of
 * a week, and a week does not change shape because one item was completed. Redrawing it on
 * every write would be the recomposition-per-frame failure issue #5 names as this phase's
 * second risk, dressed as correctness.
 */
class AreasBannerViewModel(
    private val coordinator: MomentumCoordinator,
    private val clock: ClarityClock,
) : ViewModel() {

    private val _banner = MutableStateFlow<AreasBannerView?>(null)

    /** The sentence and its caption, or null while the engine has said nothing. */
    val banner: StateFlow<AreasBannerView?> = _banner.asStateFlow()

    private val throttle = BannerThrottle()

    /**
     * One coroutine at a time, so two entries to the Areas tab in the same frame do not
     * both read the log. The lock is held around the check as well as the work, which is
     * what makes the throttle a rate limit rather than a suggestion.
     */
    private val gate = Mutex()

    /**
     * Recomputes the banner if an hour of app use has passed since the last attempt.
     *
     * Called from the composable on every entry to the Areas screen. **Calling it more
     * often is free**, which is the property that makes it safe to call from a
     * `LaunchedEffect`: it is a lock, a subtraction and a return.
     */
    fun refresh() {
        viewModelScope.launch {
            gate.withLock {
                val now = clock.nowMillis()
                if (!throttle.isDue(now)) return@withLock
                // Recorded before the work rather than after it, so a slow read cannot let
                // a second entry through the gate on the same hour.
                throttle.recordAt(now)
                _banner.value = coordinator.banner()
            }
        }
    }
}
