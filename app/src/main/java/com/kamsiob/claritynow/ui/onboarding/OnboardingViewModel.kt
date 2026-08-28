package com.kamsiob.claritynow.ui.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.ui.theme.AreaPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The four beats of MASTER_BUILD_PROMPT 13.1, in order. */
enum class OnboardingBeat { SEE_IT_WORK, YOUR_AREAS, THE_REVEAL, THE_DEPTH }

/**
 * Which half of beat 2 is showing. MASTER_BUILD_PROMPT 14b.11, Addendum 01 8a.
 *
 * [FORK] is the addendum's requirement expressed as a screen. It asks for `Just start` to
 * be "a genuine equal alternative, not buried, not a text link under the real button",
 * and the only arrangement in which two options are actually equal is one where neither
 * is the page and the other an escape from it. So beat 2 opens as a choice between two
 * paths, and the area picker is the second screen of one of them rather than the beat
 * itself. Recorded under design-v3.md 15 in `OnboardingBeatTwo.kt`.
 */
enum class BeatTwoStage { FORK, JUST_START, PICK_AREAS }

/**
 * One starter area a person has chosen, held in memory and written nowhere.
 *
 * MASTER_BUILD_PROMPT 13.1: "Selections are transient in-memory structs; nothing is
 * written until beat 3." [name] is the identity here, because two areas with the same
 * name in one onboarding would be a mistake rather than a plan.
 */
@Immutable
data class StarterArea(val name: String, val colorHex: String)

@Immutable
data class OnboardingUiState(
    val beat: OnboardingBeat = OnboardingBeat.SEE_IT_WORK,
    val stage: BeatTwoStage = BeatTwoStage.FORK,
    val selections: List<StarterArea> = emptyList(),
    /** The selection whose mood color rows are open, by name, or null. */
    val focused: String? = null,
    /** The first item on the `Just start` path. Optional, and usually empty. */
    val firstItemTitle: String = "",
    /** True once beat 3 has finished writing. Nothing may reveal before this. */
    val committed: Boolean = false,
) {

    /**
     * Whether the current beat has somewhere to go.
     *
     * Beat 2 is the only one that can say no, and it says no only on the fork, where the
     * person has not chosen a path yet. The picker says yes from one selection: 8.1 puts
     * `Pick two to four` in copy and then says in as many words that "there is no limit on
     * the number of areas. The philosophy is carried by copy and layout, not a cap", so a
     * Continue that refuses at one selection would be the cap that sentence forbids.
     */
    val canAdvance: Boolean
        get() = when (beat) {
            OnboardingBeat.YOUR_AREAS -> when (stage) {
                BeatTwoStage.FORK -> false
                BeatTwoStage.JUST_START -> true
                BeatTwoStage.PICK_AREAS -> selections.isNotEmpty()
            }

            else -> true
        }

    /** Beat 1 has nothing behind it. design-v3.md 10.15 hides the chevron there. */
    val canGoBack: Boolean
        get() = beat != OnboardingBeat.SEE_IT_WORK

    val justStart: Boolean get() = stage == BeatTwoStage.JUST_START
}

/**
 * Onboarding's state and its one write. MASTER_BUILD_PROMPT 13.1 and 14b.11.
 *
 * **Nothing here writes until [commit], and [commit] runs exactly once.** 13.1 makes that
 * a requirement of beat 2 and the issue's risk list names the failure it prevents: an
 * onboarding abandoned at beat 2 must leave no debris in an event log that is supposed to
 * be the truth. So the selections are plain structs in a `StateFlow` and the repository
 * does not hear about any of them until the reveal.
 *
 * **[commit] also sets `hasCompletedOnboarding`, and that is deliberate rather than
 * early.** design-v3.md 10.15 requires a force quit after beat 3 to land on a populated
 * Areas screen rather than restarting the flow, and the flag is what decides that on the
 * next cold start. Onboarding is complete the moment it has written real data; beat 4 is
 * depth, and depth is not setup.
 */
class OnboardingViewModel(
    private val repository: ClarityRepository,
    private val preferences: ClarityPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    /**
     * How many areas the color walk should consider already spent.
     *
     * Zero on the first run, which is every run that matters here. It is read from the
     * projection rather than assumed because 13.1 makes onboarding replayable from
     * Settings, and a replay on a phone that already has four areas should not hand out
     * the same four colors again.
     */
    private var walkOffset = 0

    private var writing = false

    init {
        viewModelScope.launch {
            repository.load()
            walkOffset = repository.areaCountForColorWalk()
        }
    }

    fun advance() {
        _state.update { current ->
            if (!current.canAdvance) return@update current
            when (current.beat) {
                OnboardingBeat.SEE_IT_WORK -> current.copy(beat = OnboardingBeat.YOUR_AREAS)
                OnboardingBeat.YOUR_AREAS -> current.copy(beat = OnboardingBeat.THE_REVEAL)
                OnboardingBeat.THE_REVEAL -> current.copy(beat = OnboardingBeat.THE_DEPTH)
                OnboardingBeat.THE_DEPTH -> current
            }
        }
    }

    /**
     * design-v3.md 10.15: back returns to the previous beat and is hidden on beat 1.
     *
     * Inside beat 2 it returns to the fork first, because the picker and the `Just start`
     * field are two screens of one beat and stepping straight past them to beat 1 would
     * strand a person who tapped the wrong path.
     */
    fun back() {
        _state.update { current ->
            when {
                current.beat == OnboardingBeat.YOUR_AREAS && current.stage != BeatTwoStage.FORK ->
                    current.copy(stage = BeatTwoStage.FORK)

                current.beat == OnboardingBeat.THE_DEPTH ->
                    current.copy(beat = OnboardingBeat.THE_REVEAL)

                current.beat == OnboardingBeat.THE_REVEAL ->
                    current.copy(beat = OnboardingBeat.YOUR_AREAS)

                current.beat == OnboardingBeat.YOUR_AREAS ->
                    current.copy(beat = OnboardingBeat.SEE_IT_WORK)

                else -> current
            }
        }
    }

    fun chooseJustStart() = _state.update { it.copy(stage = BeatTwoStage.JUST_START) }

    fun choosePickAreas() = _state.update { it.copy(stage = BeatTwoStage.PICK_AREAS) }

    fun setFirstItemTitle(value: String) = _state.update { it.copy(firstItemTitle = value) }

    /** Adds [name] if it is not already chosen, removes it if it is. */
    fun toggleSuggestion(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        _state.update { current ->
            val existing = current.selections.firstOrNull { it.name.equals(trimmed, true) }
            if (existing != null) current.without(existing) else current.with(trimmed)
        }
    }

    fun addCustom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        _state.update { current ->
            if (current.selections.any { it.name.equals(trimmed, true) }) {
                current.copy(focused = trimmed)
            } else {
                current.with(trimmed)
            }
        }
    }

    fun remove(name: String) = _state.update { current ->
        current.selections.firstOrNull { it.name == name }
            ?.let { current.without(it) }
            ?: current
    }

    /** Opens or closes the mood color rows for one selection. */
    fun focusOn(name: String) = _state.update { current ->
        current.copy(focused = if (current.focused == name) null else name)
    }

    fun recolor(name: String, colorHex: String) = _state.update { current ->
        current.copy(
            selections = current.selections.map {
                if (it.name == name) it.copy(colorHex = colorHex) else it
            },
        )
    }

    /**
     * Beat 3's write. MASTER_BUILD_PROMPT 13.1 and 14b.11.
     *
     * Every selected area as a real `AREA_CREATED`, in the order they were chosen, then
     * `hasCompletedOnboarding`. On the `Just start` path that is one area named [todayName]
     * with the first color the mood walk yields, plus the person's first item if they
     * typed one, which lands active because the area is empty.
     *
     * [todayName] arrives as a parameter rather than being written here, because it is an
     * area name a person will see and rename, so it belongs in `strings.xml` with every
     * other piece of fixed copy and this class has no business reading resources.
     *
     * **It launches into the ViewModel's own scope and reports completion through the
     * state**, rather than being a suspend function the reveal awaits directly. A reveal
     * interrupted by a rotation must not be able to abandon a half finished sequence of
     * writes; [awaitCommitted] can be stopped and the writing cannot.
     */
    fun commit(todayName: String) {
        if (writing) return
        writing = true
        viewModelScope.launch {
            repository.load()
            val current = _state.value
            if (current.justStart) {
                val areaId = repository.createArea(
                    rawName = todayName,
                    colorHex = AreaPalette.defaultColorForIndex(walkOffset),
                )
                val title = current.firstItemTitle.trim()
                if (areaId != null && title.isNotEmpty()) {
                    repository.addItem(areaId = areaId, rawTitle = title)
                }
            } else {
                current.selections.forEach { area ->
                    repository.createArea(rawName = area.name, colorHex = area.colorHex)
                }
            }
            preferences.setHasCompletedOnboarding(true)
            _state.update { it.copy(committed = true) }
        }
    }

    /** Suspends until [commit] has finished. Stopping this stops nothing that writes. */
    suspend fun awaitCommitted() {
        state.first { it.committed }
    }

    /**
     * `Jump in`, from the persistent nav overlay. MASTER_BUILD_PROMPT 13.1.
     *
     * It records that onboarding is behind the person and writes nothing else. **It does
     * not quietly create an area for them.** design-v3.md 10.15 specifies the zero areas
     * state in full, down to the FAB creating an area while it holds, so leaving is a
     * supported destination rather than a hole; creating something on behalf of someone
     * who just declined to create anything would be the app making the one decision this
     * whole phase exists to stop making for them.
     */
    fun leaveEarly() {
        viewModelScope.launch { preferences.setHasCompletedOnboarding(true) }
    }

    private fun OnboardingUiState.with(name: String): OnboardingUiState {
        val area = StarterArea(
            name = name,
            colorHex = AreaPalette.defaultColorForIndex(walkOffset + selections.size),
        )
        return copy(selections = selections + area, focused = name)
    }

    private fun OnboardingUiState.without(area: StarterArea): OnboardingUiState = copy(
        selections = selections - area,
        focused = if (focused == area.name) null else focused,
    )
}
