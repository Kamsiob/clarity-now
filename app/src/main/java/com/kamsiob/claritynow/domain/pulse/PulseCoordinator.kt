package com.kamsiob.claritynow.domain.pulse

import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.dateKey
import com.kamsiob.claritynow.domain.corpus.CatalogLoad
import com.kamsiob.claritynow.domain.corpus.SharedCatalog
import com.kamsiob.claritynow.domain.engine.SilenceReason
import com.kamsiob.claritynow.domain.engine.catalog.ResponseOption
import com.kamsiob.claritynow.domain.query.TrailQueries
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * One day's Pulse, with everything a screen needs and nothing it has to assemble.
 *
 * The sheet renders [PulseEntryState.observation], then [PulseEntryState.question], then
 * one pill per [responses] entry, and after a tap it shows [acknowledgment]. Every one of
 * those strings came out of a corpus through the engine layers in order. **A composable
 * never builds a sentence and never opens a bench**, per `MASTER_BUILD_PROMPT.md` 11.4.
 */
data class DailyPulse(
    val entry: PulseEntryState,
    /** Two, or three for `quietDay`. Empty only when the corpus no longer has the stage. */
    val responses: List<ResponseOption>,
    /** Shown briefly after an answer. Null when the bench is absent. */
    val acknowledgment: String?,
) {
    val state: PulseDayState get() = PulseDayState.of(entry)
}

/** What a foreground generation attempt did. */
sealed interface PulseOutcome {

    /**
     * There is an entry for the day. [justGenerated] is false when it was already there,
     * which is every foreground after the first one of the day.
     */
    data class Present(val pulse: DailyPulse, val justGenerated: Boolean) : PulseOutcome

    /** The engine had nothing to say. Nothing was written. The day is IDLE. */
    data class Silent(val dateKey: String, val reason: SilenceReason) : PulseOutcome

    /** One of the first two days after a return. Nothing was written. 14b.4. */
    data class Suppressed(val dateKey: String) : PulseOutcome

    /**
     * The language could not be loaded, so nothing was attempted and nothing was written.
     *
     * This is the corpus being unreadable or unparseable, which is a build or packaging
     * fault rather than anything about the person's data. It is a state of its own so
     * that it appears in a log line as itself: an app that silently produced no Pulse for
     * a month because an asset was missing would look exactly like a quiet month.
     */
    data class Unavailable(val reason: String) : PulseOutcome
}

/**
 * The Pulse lifecycle, wired. `MASTER_BUILD_PROMPT.md` 11.3 and 12.1.
 *
 * This is the engine's first real caller, and everything impure about that is here: the
 * clock, the log, the corpus text and the one write. [PulseGenerator] holds the decision
 * and can be tested; this holds the plumbing and cannot.
 *
 * ## The catalog is not built here
 *
 * It arrives as [SharedCatalog], which is the one catalog for the process per 11.7 and is
 * built at most once however many surfaces ask for it. This class held its own until issue
 * #55, along with the mutex and the cached field that made it once, and so did the two other
 * coordinators; the three of them parsed the corpus three times between them.
 *
 * **The firing history is the opposite and is rebuilt every time**, inside the generator,
 * because it is derived from a log that merges and caching it is how two devices drift apart
 * silently.
 *
 * ## Where it is called from
 *
 * `ClarityApp` calls [generateOnForeground] on the first foreground of the process, from
 * the same callback that writes the presence marker, and **after** it: the re-entry
 * suppression in 14b.4 is measured against a log that already contains today's
 * `APP_OPENED`, so running these two the other way around would miss the return on the
 * day it happened, which is the only day it matters.
 *
 * Calling it more often is harmless and cheap. The second call of a day finds the entry
 * in the projection and returns before it reads the log at all.
 */
class PulseCoordinator(
    private val repository: ClarityRepository,
    private val clock: ClarityClock,
    private val catalog: SharedCatalog,
    private val newPulseId: () -> String = { UUID.randomUUID().toString() },
) {

    /**
     * Runs the sequence for today, and appends `PULSE_GENERATED` if the engine spoke.
     *
     * Three of the four decisions write nothing at all. Only [PulseDecision.Speak] reaches
     * a writer, and the writer is the repository, which is the only thing in this app that
     * writes.
     *
     * **A failure to read or parse the corpus is caught and reported, and a failure to
     * write is not.** The first is a packaging fault that must not take down the first
     * screen of somebody's day, and the app is fully usable without a Pulse. The second is
     * the database refusing an append, which is not a Pulse problem and is not this class's
     * to swallow.
     */
    suspend fun generateOnForeground(): PulseOutcome {
        repository.load()
        val zone = clock.zone()
        val now = clock.nowMillis()
        val day = PulseSchedule.dayAt(now, zone)

        // Step 2, asked of the projection first. It is a map lookup, and it is what keeps
        // every foreground after the first one of the day off the log entirely.
        repository.pulseFor(day.dateKey)?.let { return present(it, justGenerated = false) }

        val language = when (val load = catalog.load()) {
            is CatalogLoad.Ready -> PulseLanguage(load.catalog)
            is CatalogLoad.Failed -> return PulseOutcome.Unavailable(load.reason)
        }
        val decision = try {
            val queries = TrailQueries(repository.allEvents(), zone)
            PulseGenerator(language.catalog, zone, newPulseId).decide(queries, now)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            return PulseOutcome.Unavailable(describe(failure))
        }

        return when (decision) {
            is PulseDecision.Speak -> present(
                repository.recordPulseGenerated(decision.payload),
                justGenerated = true,
            )

            is PulseDecision.Silent -> PulseOutcome.Silent(decision.day.dateKey, decision.reason)

            is PulseDecision.SuppressedAfterReturn -> PulseOutcome.Suppressed(decision.day.dateKey)

            // The log holds an entry the projection does not. Nothing writes to one without
            // the other, so this is unreachable, and it is reported rather than assumed.
            is PulseDecision.AlreadyWritten ->
                repository.pulseFor(decision.day.dateKey)?.let { present(it, justGenerated = false) }
                    ?: PulseOutcome.Unavailable(
                        "the log holds a Pulse for ${decision.day.dateKey} that the projection does not",
                    )
        }
    }

    /** Today's Pulse, or null when the day is IDLE. */
    suspend fun today(): DailyPulse? = pulseOn(clock.dateKey())

    /** The Pulse for one day, or null when that day is IDLE. */
    suspend fun pulseOn(dateKey: String): DailyPulse? {
        repository.load()
        return repository.pulseFor(dateKey)?.let { dailyPulse(it) }
    }

    /**
     * One day's Pulse as it changes, for a screen.
     *
     * Derived from the repository's own projection, so an answer written anywhere reaches
     * every collector. The entry is the only thing that changes; the responses and the
     * acknowledgment are functions of it and of the corpus.
     */
    fun observe(dateKey: String): Flow<DailyPulse?> =
        repository.state
            .map { state -> state.pulses[dateKey] }
            .distinctUntilChanged()
            .map { entry -> entry?.let { dailyPulse(it) } }

    /**
     * The state of one local day. IDLE, READY or ANSWERED.
     *
     * **This is the predicate the reminder is allowed to ask.** 12.1: the daily
     * notification is posted only if that day's entry exists and is unanswered, and never
     * when IDLE. See [reminderIsDue], which is this and nothing else.
     */
    suspend fun stateOn(dateKey: String): PulseDayState {
        repository.load()
        return PulseDayState.of(repository.pulseFor(dateKey))
    }

    /**
     * Whether a reminder may be posted for [dateKey]. `MASTER_BUILD_PROMPT.md` 12.1.
     *
     * True only in [PulseDayState.READY]. A silent day is IDLE and posts nothing, which is
     * the whole reason this is a function on the lifecycle rather than a check inside a
     * worker: designed silence followed by a notification saying there is something to
     * answer is a broken promise, and the rule is easier to keep in one place than in
     * every place that might post.
     */
    suspend fun reminderIsDue(dateKey: String): Boolean = stateOn(dateKey) == PulseDayState.READY

    /**
     * Records an answer, storing [option]'s label **verbatim**.
     *
     * The whole option travels rather than three strings, so the label, the key and the
     * polarity cannot be assembled from different places. A later callback quotes what the
     * person actually saw, per `CLARITY_LOGIC_ENGINE.md` 3.1, and that is only true if the
     * string written here is the string the pill carried.
     *
     * Answering twice is a no op, and so is answering a day with no entry. Neither is an
     * error: a double tap and a stale screen are both ordinary.
     */
    suspend fun answer(dateKey: String, option: ResponseOption): DailyPulse? {
        repository.load()
        val entry = repository.answerPulse(dateKey, option) ?: return null
        return dailyPulse(entry)
    }

    private suspend fun present(entry: PulseEntryState, justGenerated: Boolean): PulseOutcome =
        PulseOutcome.Present(dailyPulse(entry), justGenerated)

    /**
     * An entry with its benches attached, or the entry alone when the corpus cannot be
     * read.
     *
     * The observation and the question are on the event and always render. Losing the
     * corpus costs the pills and the acknowledgment and nothing else, which is the right
     * failure: a person can still read what the app noticed.
     */
    private suspend fun dailyPulse(entry: PulseEntryState): DailyPulse {
        val language = languageOrNull()
        return DailyPulse(
            entry = entry,
            responses = language?.responsesFor(entry).orEmpty(),
            acknowledgment = language?.acknowledgmentFor(entry.dateKey),
        )
    }

    /**
     * The two benches over the process's catalog, or null when the corpus cannot be read.
     *
     * [PulseLanguage] holds nothing but a reference to the catalog and resolves both benches
     * on demand, so wrapping the shared catalog on each ask costs an allocation and no parse.
     * It is not cached for that reason: a cached wrapper would be a second thing that has to
     * be invalidated with the catalog, and there is nothing in it to save.
     *
     * **The failure is not asked for here and that is the difference from [generateOnForeground].**
     * That one reports the reason, so it reads the whole [CatalogLoad]; this one renders what
     * it can without language, so a failure is simply an absence.
     */
    private suspend fun languageOrNull(): PulseLanguage? =
        (catalog.load() as? CatalogLoad.Ready)?.let { PulseLanguage(it.catalog) }

    private fun describe(cause: Throwable): String =
        "${cause::class.java.simpleName}: ${cause.message ?: "no detail"}"
}
