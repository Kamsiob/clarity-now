package com.kamsiob.claritynow.ui.areas

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.data.repo.CompletionOutcome
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.daysBetween
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.ItemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil

/** One area card. design-v3.md 10.3. Everything the card draws, and nothing else. */
@Immutable
data class AreaCardModel(
    val id: String,
    val name: String,
    val colorHex: String,
    val activeItemId: String?,
    val activeItemTitle: String?,
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

@Immutable
data class AreasUiState(
    val loading: Boolean = true,
    val areas: List<AreaCardModel> = emptyList(),
    val conflicts: List<ConflictCardModel> = emptyList(),
    val promotions: Map<String, PromotionCue> = emptyMap(),
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

    val uiState: StateFlow<AreasUiState> =
        combine(
            repository.state,
            loading,
            promotions,
            repository.openConflicts,
        ) { state, isLoading, cues, conflicts ->
            AreasUiState(
                loading = isLoading,
                areas = state.liveAreas.map { area -> area.toCardModel(state) },
                conflicts = conflicts.mapNotNull { it.toCardModel() },
                promotions = cues,
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

    fun addItem(areaId: String, title: String, note: String?) = viewModelScope.launch {
        repository.addItem(areaId, title, note)
    }

    fun editItem(itemId: String, title: String, note: String?) = viewModelScope.launch {
        repository.editItem(itemId, title, note)
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

    fun completedFor(areaId: String): List<ItemState> = repository.state.value.completedIn(areaId)

    fun activeFor(areaId: String): ItemState? = repository.state.value.activeItemIn(areaId)

    fun itemFor(itemId: String): ItemState? =
        repository.state.value.items[itemId]?.takeIf { it.deletedAt == null }

    fun areaCount(): Int = repository.state.value.liveAreas.size

    val afterCompleting = preferences.afterCompleting

    // Mapping -----------------------------------------------------------------

    private fun com.kamsiob.claritynow.domain.replay.AreaState.toCardModel(
        state: ClarityState,
    ): AreaCardModel {
        val active = state.activeItemIn(id)
        val session = state.focusSessions.values.firstOrNull {
            it.areaId == id && it.outcome == FocusOutcome.RUNNING
        }
        val remaining = session?.let {
            val elapsed = (clock.nowMillis() - it.startedAt) / 1000
            val left = it.plannedSeconds - elapsed
            if (left <= 0) 0 else ceil(left / 60.0).toInt()
        }
        return AreaCardModel(
            id = id,
            name = name,
            colorHex = colorHex,
            activeItemId = active?.id,
            activeItemTitle = active?.title,
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
