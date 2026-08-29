package com.kamsiob.claritynow.domain.corpus

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * What one attempt to load the language produced.
 *
 * **The catalog and the reason it is missing travel in one value on purpose.** Every caller
 * wants both, and each of the three surfaces used to ask for them separately: a nullable
 * catalog from one call and a failure string from a field beside it. Two reads of one answer
 * can disagree, and with a single holder shared by three surfaces they could disagree in a
 * new way, because a second caller succeeding between the two reads would clear the field the
 * first caller was about to read. A screen would then render nothing and report no reason.
 * There is no arrangement of two reads that closes that; one value does.
 */
sealed interface CatalogLoad {

    /** The catalog, parsed and past its integrity checks. */
    data class Ready(val catalog: ClarityCatalog) : CatalogLoad

    /**
     * Nothing was built, and this is why.
     *
     * A packaging fault rather than anything about the person's data: an asset is missing, or
     * it is present and no longer parses. Every caller renders what needs no language and
     * carries this forward, so that a month of wordless screens cannot be mistaken for a
     * month the engine had nothing to say about.
     */
    data class Failed(val reason: String) : CatalogLoad
}

/**
 * The one [ClarityCatalog] for the process. `MASTER_BUILD_PROMPT.md` 11.7.
 *
 * Building it reads three markdown files, parses them, computes every length band from the
 * realized word count and runs the integrity checks. 11.7 says to build it once and hold it,
 * and until this existed the Pulse, Momentum and the Report each held one of their own, so
 * the corpus was parsed three times per process at three different moments. Each of the three
 * copies was character for character the same code; none of them could reach the file where
 * they would meet, which is `ClarityGraph`, and each said so at its own construction site.
 *
 * **Every caller shares one instance or the point is lost**, which is why there is exactly
 * one construction of this class in the app, in `ClarityGraph`, and why `CatalogSharingTest`
 * fails the build if a second one appears.
 *
 * ## A mutex rather than a lazy delegate
 *
 * The obvious spelling of "built once for the process" is `by lazy`, and it cannot be used
 * here: reading the corpus suspends, and a lazy delegate takes a blocking initializer. The
 * shape that compiles, a delegate wrapping `runBlocking`, blocks whichever thread arrives
 * first, and on a cold start that is the thread drawing the first frame.
 *
 * A [Mutex] instead, held across the read and the parse together. Two surfaces asking at the
 * same moment is the ordinary case rather than the exotic one, since a cold start opens a tab
 * while the foreground Pulse is already running: the second one suspends, and when it takes
 * the lock the catalog is already there. **The parse happens at most once and nothing waits
 * on a thread.**
 *
 * There is deliberately no double checked fast path outside the lock. An uncontended
 * [Mutex.withLock] is a compare and swap with no suspension, the callers ask a handful of
 * times per screen, and reading a plain field outside the lock would make the correctness of
 * this class depend on the memory model rather than on the lock.
 *
 * ## The parse runs on [Dispatchers.Default], and it did not before
 *
 * `CorpusSource` puts the file read on an IO dispatcher and `withContext` returns to the
 * caller, so the parse itself used to run wherever the caller was standing. All three
 * surfaces reach this from a `viewModelScope`, which is the main dispatcher, so the one
 * expensive step was on the main thread on every surface's first ask while the three notes
 * about it each said it was on a background dispatcher. It is dispatched here rather than at
 * three call sites, because that is the property a caller cannot check.
 *
 * ## A failure is reported and never cached
 *
 * The catalog is held; the failure is not. A read that failed is tried again on the next ask,
 * because the cheap failure to recover from is a transient one and the expensive one, a
 * missing asset, costs one failed open per ask and is a broken build in any case. Nothing
 * throws: a packaging fault must not take down a screen whose numbers are all counted from
 * the log.
 */
class SharedCatalog(private val source: CorpusSource) {

    private val lock = Mutex()

    /** Set exactly once, under [lock], and never cleared. */
    private var built: ClarityCatalog? = null

    /**
     * The catalog, building it on the first ask and returning the held one afterwards.
     *
     * Suspends rather than blocks while another caller is building. Cancellation is
     * rethrown rather than reported as a corpus failure: a screen that went away is not a
     * packaging fault, and swallowing it here would leave a canceled coroutine looking like
     * a missing asset in the log.
     */
    suspend fun load(): CatalogLoad = lock.withLock {
        built?.let { return@withLock CatalogLoad.Ready(it) }
        try {
            val text = source.read()
            val catalog = withContext(Dispatchers.Default) {
                ClarityCatalog.build(text.pulse, text.report, text.momentum)
            }
            built = catalog
            CatalogLoad.Ready(catalog)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Exception) {
            CatalogLoad.Failed("${cause::class.java.simpleName}: ${cause.message ?: "no detail"}")
        }
    }
}
