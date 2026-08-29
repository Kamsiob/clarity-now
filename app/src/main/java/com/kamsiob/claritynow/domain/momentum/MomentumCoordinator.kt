package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.corpus.CatalogLoad
import com.kamsiob.claritynow.domain.corpus.SharedCatalog
import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.query.TrailQueries
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
 * ## The catalog is not built here
 *
 * It arrives as [SharedCatalog], the one catalog for the process per `MASTER_BUILD_PROMPT.md`
 * 11.7. Both Momentum surfaces resolve through one coordinator, so the tab and the Areas
 * banner shared a catalog with each other from the start; what issue #55 closed is that they
 * did not share one with the Pulse or with the Report.
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
    private val catalog: SharedCatalog,
) {

    /**
     * Why the corpus could not be read, or null when it can be.
     *
     * **Asked rather than remembered.** It was a field set by the last load attempt, which
     * was correct while this class owned the only catalog it could report on and is not
     * correct over a catalog three surfaces share: a field would answer with whichever
     * surface failed last. This asks its own question and gets the reason for its own answer.
     * Loading again is what [momentum] and [banner] already do and costs nothing once the
     * catalog is held.
     */
    suspend fun languageFailure(): String? = (catalog.load() as? CatalogLoad.Failed)?.reason

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
     * The process's catalog, or null when it could not be built.
     *
     * Reported rather than thrown, for the reason `SharedCatalog` gives: the caller wants to
     * render everything that does not need language. [MomentumComposer] takes the null and
     * drops the headline and the banner, and every number on the screen is untouched.
     */
    private suspend fun catalogOrNull(): ClarityCatalog? =
        (catalog.load() as? CatalogLoad.Ready)?.catalog
}
