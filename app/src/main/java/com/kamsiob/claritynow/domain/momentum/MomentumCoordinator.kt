package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.pulse.CorpusSource
import com.kamsiob.claritynow.domain.query.TrailQueries
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId

/**
 * Momentum and the Areas banner, wired. `MASTER_BUILD_PROMPT.md` 11.3 and 12.2.
 *
 * Everything impure about the two surfaces is here: the clock, the log and the corpus
 * text. [MomentumComposer] holds the decisions and can be tested with no Android and no
 * database; this holds the plumbing and cannot.
 *
 * **It writes nothing, and there is nothing for it to write.** Momentum is a mirror. The
 * event catalog has no `MOMENTUM_GENERATED`, so unlike the Pulse there is no append, no
 * immutability rule and no per day key. Reading it twice costs two reads and changes
 * nothing, which is what makes the once an hour banner throttle a rate limit on work
 * rather than a correctness rule.
 *
 * ## The catalog is built here, and it is the second one in the process
 *
 * **Recorded rather than hidden.** `MASTER_BUILD_PROMPT.md` 11.7 says to build the catalog
 * once and hold it, because building it parses three markdown files and runs the integrity
 * checks. `PulseCoordinator` already holds one, built from the same three assets, and the
 * right arrangement is one lazy binding in `ClarityGraph` that both coordinators take as a
 * parameter. That was not reachable from this phase: `ClarityGraph` and `ClarityApp` are
 * outside the file list this slice was given, and `PulseCoordinator` hands its catalog to
 * nobody. So this builds its own, once for the process, behind the same lock and with the
 * same failure behavior.
 *
 * **What it costs and what it does not.** One extra parse of three files, on a background
 * dispatcher, the first time the Areas banner or the Momentum tab asks for language. It is
 * not a per invocation cost and it is not on the main thread. The fix is a one line binding
 * and a constructor parameter, and phase 6's note on `ClarityApp.pulse` is the same note
 * about the same file.
 *
 * ## Losing the corpus costs the sentences and nothing else
 *
 * A missing or malformed asset gives [MomentumComposer] a null catalog, which costs the
 * headline and the whole banner and leaves every number on the Momentum screen intact.
 * That is deliberate: the dots, the tiles, the stats and the insights are counted from the
 * log and need no language at all, so a packaging fault must not take the screen down with
 * it. [languageFailure] says why, so a month of headline-less screens cannot be mistaken
 * for a month the engine had nothing to say about.
 */
class MomentumCoordinator(
    private val repository: ClarityRepository,
    private val clock: ClarityClock,
    private val corpus: CorpusSource,
) {

    private val catalogLock = Mutex()
    private var cached: ClarityCatalog? = null
    private var failure: String? = null

    /** Why the corpus could not be read, or null while it has not failed. */
    fun languageFailure(): String? = failure

    /**
     * The whole Momentum surface, as of now.
     *
     * The facade is built over the **whole** log, per the note on
     * [MomentumComposer.compose]: the history facts, the firing history and the two
     * lifetime "has this ever been used" questions all read further back than a fortnight.
     */
    suspend fun momentum(): MomentumView {
        repository.load()
        val zone = clock.zone()
        val now = clock.nowMillis()
        val order = repository.state.value.liveAreas.map { it.id }
        return composer(zone).compose(queries(zone), now, order)
    }

    /**
     * The banner sentence and caption, or null when the engine was silent or the corpus
     * could not be read.
     *
     * **Nothing here throttles.** `CLARITY_LOGIC_ENGINE.md` 6.5 puts the once an hour rule
     * in the ViewModel, and it is there, in `AreasBannerViewModel`. A second one here
     * would make the real rate a product of two numbers nobody wrote down.
     */
    suspend fun banner(): AreasBannerView? {
        repository.load()
        val zone = clock.zone()
        return composer(zone).banner(queries(zone), clock.nowMillis())
    }

    private suspend fun queries(zone: ZoneId) = TrailQueries(repository.allEvents(), zone)

    private suspend fun composer(zone: ZoneId) = MomentumComposer(catalogOrNull(), zone)

    /**
     * The catalog, built once for the process, or null with [failure] set to why.
     *
     * Held as a field rather than thrown for the reason `PulseCoordinator` gives: two
     * callers want two different things from a failure, and here the caller wants to
     * render everything that does not need language.
     */
    private suspend fun catalogOrNull(): ClarityCatalog? = catalogLock.withLock {
        cached?.let { return@withLock it }
        try {
            val text = corpus.read()
            ClarityCatalog.build(text.pulse, text.report, text.momentum).also {
                cached = it
                failure = null
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Exception) {
            failure = "${cause::class.java.simpleName}: ${cause.message ?: "no detail"}"
            null
        }
    }
}
