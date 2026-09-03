package com.kamsiob.claritynow.ui.pulse

import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.engine.FactDates
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.hourOfDay
import com.kamsiob.claritynow.domain.localDate
import com.kamsiob.claritynow.domain.pulse.DailyPulse
import com.kamsiob.claritynow.domain.pulse.PulseCoordinator
import com.kamsiob.claritynow.domain.pulse.PulseDayState
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * One mark in the fourteen day rhythm row. design-v3.md section 11.
 *
 * **[state] is `PulseDayState` and not an enum of this file's own**, which is the whole
 * guard against a fourth mark appearing. design-v3.md 11 gives the row exactly three
 * states, filled amber, a hollow ring and faint, and `MASTER_BUILD_PROMPT.md` 12.1 gives
 * the day exactly three, ANSWERED, READY and IDLE. Reusing the domain enum means the
 * only way to draw a fourth kind of mark is to add a fourth day state, which the log
 * cannot produce.
 *
 * **There is no field here that could become a streak.** No run length, no consecutive
 * count, no "days since". design-v3.md 14 forbids all three and the engine has no such
 * fact by construction; the row must not reconstruct one by eye either, so it is handed
 * fourteen independent days and nothing that relates them.
 */
@Immutable
data class PulseMark(val dateKey: String, val state: PulseDayState)

/**
 * One past Pulse, as the History page renders it.
 *
 * Every string here was written by the engine and stored on `PULSE_GENERATED`, or is the
 * response label stored **verbatim** on `PULSE_ANSWERED`. Nothing on this screen composes
 * a sentence, and there is no field for one that could be composed: an entry with no
 * answer carries a null and the page draws nothing in its place, which is the whole of
 * how `MASTER_BUILD_PROMPT.md` 11.6's "never chased, never counted, never mentioned"
 * survives contact with a list view.
 */
@Immutable
data class PulsePastEntry(
    val dateKey: String,
    val date: LocalDate?,
    val observation: String,
    val question: String?,
    val answerLabel: String?,
)

/**
 * Everything the Pulse surface draws.
 *
 * [today] is null on an IDLE day, which is a silent day, a suppressed day after a return
 * and every day before install, all three of which look the same here on purpose. See
 * `PulseDayState`.
 */
data class PulseUiState(
    /** The local day this whole state is about. The key the answer is filed under. */
    val dateKey: String,
    val loading: Boolean = true,
    val today: DailyPulse? = null,
    val rhythm: List<PulseMark> = emptyList(),
    val past: List<PulsePastEntry> = emptyList(),
    val timeOfDay: PulseTimeOfDay = PulseTimeOfDay.MIDDAY,
    /** True until the first Pulse has been answered on this install. */
    val showIntro: Boolean = false,
)

/**
 * The Pulse surface's state, and the one call that records an answer.
 *
 * **This ViewModel generates nothing.** Generation is `PulseCoordinator` called once on
 * the first app foreground of a local day, per `MASTER_BUILD_PROMPT.md` 11.3, and a
 * screen that could generate would generate on every open: the sequence is idempotent
 * only because the existence check runs before it, and a second caller racing the first
 * is exactly the shape 11.3's step 2 exists to prevent. This reads what is there and
 * writes one answer.
 *
 * **It composes no sentence and reaches no corpus.** Every string it hands the screen
 * came out of the engine: the observation and the question off the stored event, the
 * response labels and the acknowledgment out of `PulseCoordinator`, which holds the one
 * catalog. `MASTER_BUILD_PROMPT.md` 11.2 closes the list of things that may read a
 * corpus and a ViewModel is not on it.
 *
 * **Every date here comes from the injected clock, with its zone.** Never from
 * `LocalDate.now()` and never from a composable: `ClarityClock` documents a date key
 * computed against a default zone as the cause of two Pulses in one day or none at all,
 * and a surface that answered that question a second way would be exactly that bug with
 * a screen in front of it.
 */
class PulseViewModel(
    private val repository: ClarityRepository,
    private val coordinator: PulseCoordinator,
    private val clock: ClarityClock,
    private val preferences: ClarityPreferences,
) : ViewModel() {

    /**
     * False until the log has been read.
     *
     * Without it the surface would draw one frame of the empty projection, which is an
     * IDLE day, before the real state arrived: a person whose Pulse is waiting would see
     * "nothing to answer today" flash past. `ClarityRepository.load` is idempotent and
     * cheap after the first call, so calling it here costs nothing on any open but the
     * first of the process.
     */
    private val loaded = MutableStateFlow(false)

    /**
     * The local date, re-emitted at every local midnight while anything is collecting.
     *
     * The same tick the Trail runs, and here for a sharper reason than a stale day header.
     * This ViewModel is resolved against the Activity's store, so it outlives any one
     * opening of the sheet: a date key captured once at construction would have somebody
     * who opened the app on Tuesday evening and the Pulse on Wednesday morning looking at
     * Tuesday's question, answering it, and filing the answer under the wrong day.
     *
     * The wait is computed against the zone rather than as a fixed 24 hours, so a daylight
     * saving boundary lands on the real midnight rather than an hour off it, and the floor
     * keeps a clock that jumps backwards from turning the tick into a busy loop.
     */
    private val today: Flow<LocalDate> = flow {
        while (true) {
            val now = clock.nowMillis()
            val date = clock.localDate(now)
            emit(date)
            val nextMidnight = date.plusDays(1).atStartOfDay(clock.zone()).toInstant().toEpochMilli()
            delay((nextMidnight - now).coerceAtLeast(MIN_MIDNIGHT_WAIT_MILLIS))
        }
    }

    val state: StateFlow<PulseUiState> = combine(
        loaded,
        today,
        repository.state,
        preferences.hasSeenPulseIntro,
    ) { ready, date, projection, seenIntro ->
        val dateKey = FactDates.keyOf(date)
        if (!ready) {
            PulseUiState(dateKey = dateKey, loading = true, timeOfDay = timeOfDayNow())
        } else {
            PulseUiState(
                dateKey = dateKey,
                loading = false,
                // Asked of the lifecycle rather than of the projection, because the pills
                // and the acknowledgment are benches it resolves out of the one catalog
                // it holds. Nothing here opens a corpus. MASTER_BUILD_PROMPT 11.2.
                today = coordinator.pulseOn(dateKey),
                rhythm = rhythmOf(date, projection),
                past = pastOf(projection),
                timeOfDay = timeOfDayNow(),
                showIntro = !seenIntro,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = PulseUiState(dateKey = clock.dateKey(), timeOfDay = timeOfDayNow()),
    )

    init {
        viewModelScope.launch {
            repository.load()
            loaded.value = true
        }
    }

    /**
     * Records the answer, storing [option]'s label verbatim through the repository.
     *
     * The whole option travels rather than its label, per `ClarityRepository.answerPulse`,
     * so the key, the label and the polarity cannot be assembled from different places.
     * Answering twice is a no op down in the repository, so a double tap needs no guard
     * here, and so is answering a day with no entry.
     *
     * **It files the answer under the day that is on the screen**, read off the state
     * rather than from the clock. They are the same key except in the one second after a
     * midnight the person was looking at the sheet through, and there the screen is right:
     * the question they answered was yesterday's.
     *
     * Launched in the ViewModel's own scope rather than in a composition scope: the sheet
     * can be dismissed in the same frame as the tap and the write must not be canceled
     * halfway by the screen going away.
     */
    fun answer(option: ResponseOption) {
        val dateKey = state.value.dateKey
        viewModelScope.launch {
            coordinator.answer(dateKey, option)
            // Set on the answer rather than on the draw. Somebody who opened the Pulse,
            // read the line and left without answering has not finished with it.
            preferences.setHasSeenPulseIntro(true)
        }
    }

    /**
     * Which background the amber night is wearing. design-v3.md 3.3.
     *
     * Read from the injected clock at every emission rather than ticked, so it is never
     * stale on a surface that outlives an hour boundary and there is no timer running for
     * a thing the specification says must be felt rather than noticed.
     */
    private fun timeOfDayNow(): PulseTimeOfDay = PulseTimeOfDay.atHour(clock.hourOfDay())

    /**
     * The last fourteen local days ending on [date], oldest first, so the row reads left
     * to right and today is the rightmost mark.
     *
     * Built by walking calendar dates rather than by subtracting milliseconds, which is
     * wrong across every daylight saving boundary and wrong quietly. `FactDates` is the
     * same helper the engine files entries with, so a mark and the entry it draws can
     * never be looking at two different keys.
     */
    private fun rhythmOf(date: LocalDate, projection: ClarityState): List<PulseMark> =
        (RHYTHM_DAYS - 1 downTo 0).map { daysBack ->
            val key = FactDates.keyOf(date.minusDays(daysBack.toLong()))
            PulseMark(dateKey = key, state = PulseDayState.of(projection.pulses[key]))
        }

    /** Every Pulse ever generated, newest first. Date keys sort correctly as strings. */
    private fun pastOf(projection: ClarityState): List<PulsePastEntry> =
        projection.pulses.values
            .sortedByDescending { it.dateKey }
            .map { it.toPastEntry() }

    private fun PulseEntryState.toPastEntry() = PulsePastEntry(
        dateKey = dateKey,
        date = FactDates.parse(dateKey),
        observation = observation,
        question = question,
        answerLabel = responseLabel,
    )

    private companion object {

        /** design-v3.md section 11 and `MASTER_BUILD_PROMPT.md` 12.1. Fourteen, always. */
        const val RHYTHM_DAYS = 14

        /** Long enough to survive a rotation without rebuilding the projection map. */
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L

        /**
         * A floor under the wait for the next local midnight, so a clock that jumps
         * backwards over the boundary cannot turn the tick into a busy loop.
         */
        const val MIN_MIDNIGHT_WAIT_MILLIS = 1_000L
    }
}
