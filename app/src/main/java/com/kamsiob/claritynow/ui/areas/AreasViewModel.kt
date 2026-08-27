package com.kamsiob.claritynow.ui.areas

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.data.repo.CompletionOutcome
import com.kamsiob.claritynow.data.repo.FocusCountdown
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.daysBetween
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.ItemState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One area card. design-v3.md 10.3. Everything the card draws, and nothing else.
 *
 * [activeItemFirstStep] is row three, Addendum 01 4b. It is the active item's first
 * step and nothing else: a queued item's first step never reaches this screen,
 * because the card is about the one thing happening. Null when the item has none,
 * and the card renders nothing in its place rather than a placeholder or a reserved
 * row, per design-v3.md 10.3.
 *
 * The estimate is deliberately absent from this model. design-v3.md 10.17 keeps it
 * off the card entirely, and leaving it out of the card's own data is how that stays
 * true: a later session cannot draw a number the card was never handed.
 */
@Immutable
data class AreaCardModel(
    val id: String,
    val name: String,
    val colorHex: String,
    val activeItemId: String?,
    val activeItemTitle: String?,
    val activeItemFirstStep: String?,
    val queueLength: Int,
    val completedCount: Int,
    val daysSinceLastEvent: Int,
    val focusMinutesRemaining: Int?,
) {
    val isIdle: Boolean get() = activeItemId == null

    /** Whether Complete and Swap are offered, design-v3.md 10.3.1 state gating. */
    val offersComplete: Boolean get() = !isIdle
    val offersSwap: Boolean get() = !isIdle && queueLength > 0
}

@Immutable
data class ConflictCardModel(
    val id: String,
    val areaName: String,
    val winnerTitle: String,
    val loserTitle: String,
)

/**
 * A promotion that just happened, handed to the card so it can play the hero
 * animation. Cleared once played, so a recomposition does not replay it.
 */
@Immutable
data class PromotionCue(val id: Long, val previousTitle: String)

/**
 * [unfiledCount] is the whole of what the Areas screen knows about the inbox.
 *
 * design-v3.md 10.16 puts one number in the header chip and states that no other
 * surface in the document reports the inbox's size. A count rather than the list
 * keeps that honest: the screen cannot render what it was not given, and the sheet
 * reads the items when it opens, the same way the queue and the completed list do.
 *
 * Zero means there is no entry point at all rather than a chip reading `Inbox 0`. An
 * empty inbox needs no door, and the next unfiled capture brings the chip back.
 */
@Immutable
data class AreasUiState(
    val loading: Boolean = true,
    val areas: List<AreaCardModel> = emptyList(),
    val conflicts: List<ConflictCardModel> = emptyList(),
    val promotions: Map<String, PromotionCue> = emptyMap(),
    val unfiledCount: Int = 0,
) {
    val isEmpty: Boolean get() = !loading && areas.isEmpty()
}

/**
 * ViewModels never touch a DAO. Everything here goes through the repository, which
 * is the only writer in the app.
 */
class AreasViewModel(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
    private val clock: ClarityClock,
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val promotions = MutableStateFlow<Map<String, PromotionCue>>(emptyMap())
    private var promotionCounter = 0L

    /** Set when a completion needs the person to choose what happens next. */
    private val _queueChoiceFor = MutableStateFlow<String?>(null)
    val queueChoiceFor: StateFlow<String?> = _queueChoiceFor.asStateFlow()

    /**
     * This device's running session with the app's one ticker attached, and null the
     * rest of the time. design-v3.md 8.2 item 7.
     *
     * **The ticker is attached only while a session is actually running**, which is
     * what the `flatMapLatest` over the session id is for and is the same shape
     * `ClarityNotifications` uses. `ClarityRepository.focusCountdown` is a hot flow
     * that wakes once a second for as long as anything collects it, so collecting it
     * at rest would hold a coroutine open for the life of the process to publish the
     * same null over and over.
     *
     * Everything on this screen that changes during a session is derived from this
     * one flow, so the card and the shell cannot disagree about a session by a second.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveCountdown: StateFlow<FocusCountdown?> =
        repository.runningFocusSession
            .map { it?.id }
            .distinctUntilChanged()
            .flatMapLatest { sessionId ->
                if (sessionId == null) flowOf(null) else repository.focusCountdown
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Whether a session is running and whether its planned time has run out, for the
     * app shell. See [FocusPresence].
     *
     * **The shell reads this from here rather than from the repository**, because
     * composables do not touch a repository in this app and the Areas screen is the
     * surface that owns focus at the root: the Focus chip is in its header,
     * design-v3.md 10.1, and the live countdown is on its cards, 10.3. What the shell
     * adds on top is only the question of which surface is showing.
     *
     * It carries no countdown, so it changes twice in a session rather than once a
     * second: when the session begins and when its time is up.
     */
    internal val focusPresence: StateFlow<FocusPresence?> = liveCountdown
        .map { countdown -> countdown?.let { FocusPresence(it.sessionId, it.hasElapsed) } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * The in session card state, or null. MASTER_BUILD_PROMPT section 10.
     *
     * `distinctUntilChanged` is what keeps a value arriving once a second from
     * rebuilding every card on the screen once a second: the card renders whole
     * minutes, so fifty nine of those sixty ticks produce a value equal to the last
     * one and go no further. The tick is still the one ticker; this is a filter on it
     * and not a second clock.
     */
    private val focusHighlight: Flow<FocusHighlight?> =
        combine(liveCountdown, preferences.focusHighlightEnabled) { countdown, enabled ->
            focusHighlightFor(countdown, highlightEnabled = enabled)
        }.distinctUntilChanged()

    val uiState: StateFlow<AreasUiState> =
        combine(
            repository.state,
            loading,
            promotions,
            repository.openConflicts,
            focusHighlight,
        ) { state, isLoading, cues, conflicts, focus ->
            AreasUiState(
                loading = isLoading,
                areas = state.liveAreas.map { area -> area.toCardModel(state, focus) },
                conflicts = conflicts.mapNotNull { it.toCardModel() },
                promotions = cues,
                unfiledCount = state.unfiledItems.size,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AreasUiState())

    init {
        viewModelScope.launch {
            repository.load()
            loading.value = false
        }
    }

    // Areas -------------------------------------------------------------------

    fun createArea(name: String, colorHex: String) = viewModelScope.launch {
        repository.createArea(name, colorHex)
    }

    fun renameArea(areaId: String, name: String) = viewModelScope.launch {
        repository.renameArea(areaId, name)
    }

    fun recolorArea(areaId: String, colorHex: String) = viewModelScope.launch {
        repository.recolorArea(areaId, colorHex)
    }

    fun moveArea(areaId: String, toIndex: Int) = viewModelScope.launch {
        repository.moveArea(areaId, toIndex)
    }

    fun archiveArea(areaId: String) = viewModelScope.launch { repository.archiveArea(areaId) }

    fun deleteArea(areaId: String) = viewModelScope.launch { repository.deleteArea(areaId) }

    /** The color a new area gets when the person does not pick one. */
    fun suggestedColorIndex(): Int = repository.areaCountForColorWalk()

    // Items -------------------------------------------------------------------

    /**
     * Adds an item. A null [areaId] is a capture into the inbox, Addendum 01 4a, and
     * is the ordinary case rather than the exceptional one: **capture must never
     * require a decision**, so the FAB hands this a null and the destination is
     * chosen later or never.
     *
     * [firstStep] and [estimateMinutes] are 4b and 4c, optional forever and never
     * prompted for anywhere in this app.
     */
    fun addItem(
        areaId: String?,
        title: String,
        note: String?,
        firstStep: String? = null,
        estimateMinutes: Int? = null,
    ) = viewModelScope.launch {
        repository.addItem(areaId, title, note, firstStep, estimateMinutes)
    }

    /**
     * Saves an edit sheet, which can move two things that are recorded separately.
     *
     * The title, the note and the first step travel on `ITEM_EDITED`. The estimate
     * travels on its own `ITEM_ESTIMATED`, per Addendum 01 2b, so that revising a
     * guess never rewrites what the person first wrote down. Two events rather than
     * one is the point, and each is a no-op when its half did not change, which the
     * repository decides rather than this.
     *
     * A null [estimateMinutes] clears the estimate, and clearing writes the event
     * with a null new value rather than writing nothing.
     */
    fun saveItem(
        itemId: String,
        title: String,
        note: String?,
        firstStep: String?,
        estimateMinutes: Int?,
    ) = viewModelScope.launch {
        repository.editItem(itemId, title, note, firstStep)
        repository.estimateItem(itemId, estimateMinutes)
    }

    /** Files an inbox item into an area. Addendum 01 4a, the only transition out. */
    fun fileItem(itemId: String, areaId: String) = viewModelScope.launch {
        repository.fileItem(itemId, areaId)
    }

    /**
     * The zero areas case in design-v3.md 10.16: the inbox may hold items when no
     * area exists, so `Move to an area` has to be able to offer to create one first.
     *
     * One function rather than two calls from the sheet, because filing into an area
     * that failed to be created is the kind of gap that only shows up when somebody
     * types a name the repository refuses. If [ClarityRepository.createArea] returns
     * null the item stays in the inbox, visible, and can be filed again.
     */
    fun createAreaAndFile(itemId: String, name: String, colorHex: String) = viewModelScope.launch {
        val areaId = repository.createArea(name, colorHex) ?: return@launch
        repository.fileItem(itemId, areaId)
    }

    fun wouldBecomeActive(areaId: String): Boolean = repository.wouldBecomeActive(areaId)

    /**
     * Completing is the only place the promotion cue is minted, because it is the
     * only place a title is replaced by another one rather than simply changing.
     */
    fun completeItem(areaId: String, itemId: String) = viewModelScope.launch {
        val previousTitle = repository.state.value.items[itemId]?.title
        when (val outcome = repository.completeItem(itemId)) {
            is CompletionOutcome.Promoted -> {
                if (previousTitle != null) {
                    promotionCounter += 1
                    promotions.value = promotions.value + (areaId to PromotionCue(promotionCounter, previousTitle))
                }
            }

            is CompletionOutcome.ChooseFromQueue -> _queueChoiceFor.value = outcome.areaId
            CompletionOutcome.AreaIdle, CompletionOutcome.NotAllowed -> Unit
        }
    }

    fun promotionPlayed(areaId: String) {
        promotions.value = promotions.value - areaId
    }

    fun chooseFromQueue(itemId: String) = viewModelScope.launch {
        repository.swapToItem(itemId)
        _queueChoiceFor.value = null
    }

    /** Dismissing the chooser leaves the area idle, which is a real state. */
    fun dismissQueueChoice() { _queueChoiceFor.value = null }

    fun swapToItem(itemId: String) = viewModelScope.launch { repository.swapToItem(itemId) }

    fun reopenItem(itemId: String) = viewModelScope.launch { repository.reopenItem(itemId) }

    fun moveItem(itemId: String, toIndex: Int) = viewModelScope.launch {
        repository.moveItem(itemId, toIndex)
    }

    fun moveItemToFront(itemId: String) = viewModelScope.launch { repository.moveItemToFront(itemId) }

    /** Called only once the undo window has closed. Nothing is written before that. */
    suspend fun deleteItem(itemId: String) = repository.deleteItem(itemId)

    fun dismissConflict(conflictId: String) = viewModelScope.launch {
        repository.dismissConflict(conflictId)
    }

    fun queueFor(areaId: String): List<ItemState> = repository.state.value.queueIn(areaId)

    /**
     * The inbox, newest first. design-v3.md 10.16: plain rows, **oldest last**.
     *
     * `ClarityState.unfiledItems` is ordered by order key, which puts the oldest
     * first because every capture takes a key at the tail. The sheet reverses it, and
     * the reversal lives here rather than in the state so that the one projection
     * everything else reads keeps the same ordering rule as a queue.
     *
     * Newest first is the right way round for a pile nobody is ever asked to work
     * through: the thing a person just wrote down is the thing they came back for,
     * and an inbox that buries it under a fortnight of older captures is the pile
     * design-v3.md 10.16 refuses to put at the top of the Areas screen.
     */
    fun inboxItems(): List<ItemState> = repository.state.value.unfiledItems.asReversed()

    fun completedFor(areaId: String): List<ItemState> = repository.state.value.completedIn(areaId)

    fun activeFor(areaId: String): ItemState? = repository.state.value.activeItemIn(areaId)

    fun itemFor(itemId: String): ItemState? =
        repository.state.value.items[itemId]?.takeIf { it.deletedAt == null }

    fun areaCount(): Int = repository.state.value.liveAreas.size

    val afterCompleting = preferences.afterCompleting

    // Mapping -----------------------------------------------------------------

    /**
     * [focus] is the one running session on this device, already filtered by the
     * `focusHighlightEnabled` setting, and it reaches at most one card.
     *
     * It is matched by area rather than looked up from the projection, deliberately.
     * A merged log can legitimately hold two running sessions, one per device, and the
     * card must show the one this phone is running: reading `state.focusSessions` here
     * would put a countdown on a card for a session running on a different phone,
     * ticking down to an end nobody on this device is working toward.
     */
    private fun com.kamsiob.claritynow.domain.replay.AreaState.toCardModel(
        state: ClarityState,
        focus: FocusHighlight?,
    ): AreaCardModel {
        val active = state.activeItemIn(id)
        val remaining = focus?.takeIf { it.areaId == id }?.minutesRemaining
        return AreaCardModel(
            id = id,
            name = name,
            colorHex = colorHex,
            activeItemId = active?.id,
            activeItemTitle = active?.title,
            activeItemFirstStep = active?.firstStep,
            queueLength = state.queueIn(id).size,
            completedCount = state.completedIn(id).size,
            daysSinceLastEvent = clock.daysBetween(lastEventAt, clock.nowMillis()).coerceAtLeast(0),
            focusMinutesRemaining = remaining,
        )
    }

    private fun ClarityConflict.toCardModel(): ConflictCardModel? {
        val area = areaNameSnapshot ?: return null
        val winner = winnerTitleSnapshot ?: return null
        val loser = loserTitleSnapshot ?: return null
        return ConflictCardModel(id = id, areaName = area, winnerTitle = winner, loserTitle = loser)
    }
}
