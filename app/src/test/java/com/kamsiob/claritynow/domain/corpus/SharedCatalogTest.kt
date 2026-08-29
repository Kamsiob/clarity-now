package com.kamsiob.claritynow.domain.corpus

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One catalog for the process, and what happens when there is none.
 * `MASTER_BUILD_PROMPT.md` 11.7, issue #55.
 *
 * **The parse is counted rather than assumed.** Until this class existed, the Pulse, Momentum
 * and the Report each held a catalog, a mutex and a cached field of their own, so the three
 * corpus volumes were read and parsed three times per process. The three copies agreed,
 * because they were built from the same assets by the same parser, so nothing anybody could
 * see was wrong and nothing failed. That is the kind of defect a count catches and a
 * screenshot does not.
 *
 * The corpus these tests parse is the committed one, through [CorpusFixture], for the reason
 * that fixture gives: a holder tested against a synthetic corpus proves the holder and
 * nothing about the three files the app actually reads.
 */
class SharedCatalogTest {

    /**
     * A source that counts its reads and can be told to fail the first few.
     *
     * It yields before answering, so two callers arriving at once really do interleave
     * inside [SharedCatalog.load] rather than running one after the other by accident.
     */
    private class CountingCorpus(private val failuresFirst: Int = 0) : CorpusSource {

        val reads = AtomicInteger(0)

        override suspend fun read(): CorpusText {
            val attempt = reads.incrementAndGet()
            yield()
            if (attempt <= failuresFirst) throw FileNotFoundException("corpus/CORPUS_1_PULSE.md")
            return CorpusText(
                pulse = CorpusFixture.pulseText,
                report = CorpusFixture.reportText,
                momentum = CorpusFixture.momentumText,
            )
        }
    }

    private fun readyOf(load: CatalogLoad): CatalogLoad.Ready {
        assertTrue("expected a catalog and got $load", load is CatalogLoad.Ready)
        return load as CatalogLoad.Ready
    }

    @Test
    fun `the corpus is read and parsed once however many callers ask`() = runTest {
        val corpus = CountingCorpus()
        val shared = SharedCatalog(corpus)

        val first = readyOf(shared.load()).catalog
        repeat(5) { assertSame(first, readyOf(shared.load()).catalog) }

        assertEquals(
            "the corpus was read more than once. Every caller has to get the held catalog, " +
                "which is the whole of what MASTER_BUILD_PROMPT 11.7 asks for",
            1,
            corpus.reads.get(),
        )
    }

    /**
     * The cold start case, and the one a lock is for.
     *
     * Two surfaces asking at the same moment is ordinary rather than exotic: the first
     * foreground generates the Pulse while the shell is already building the tab a person
     * left the app on. A holder that checked a field and then built would parse twice.
     */
    @Test
    fun `callers arriving at the same moment parse once between them`() = runTest {
        val corpus = CountingCorpus()
        val shared = SharedCatalog(corpus)

        val loads = (1..8).map { async { shared.load() } }.awaitAll()

        val catalogs = loads.map { readyOf(it).catalog }
        assertEquals("eight callers did not get one catalog", 1, catalogs.distinct().size)
        assertEquals("eight concurrent callers parsed more than once", 1, corpus.reads.get())
    }

    @Test
    fun `a corpus that cannot be read is reported with the reason and never thrown`() = runTest {
        val corpus = CountingCorpus(failuresFirst = 1)
        val load = SharedCatalog(corpus).load()

        assertTrue("expected a failure and got $load", load is CatalogLoad.Failed)
        val reason = (load as CatalogLoad.Failed).reason
        assertTrue("the reason names no cause: $reason", reason.contains("FileNotFoundException"))
        assertTrue("the reason names no detail: $reason", reason.contains("CORPUS_1_PULSE.md"))
    }

    /**
     * A failure is reported and never held, so the next ask tries again.
     *
     * The catalog is the expensive thing and it is cached; a failed read is not, because the
     * cheap failure to recover from is a transient one and the expensive one, a missing
     * asset, is a broken build a retry cannot make worse.
     */
    @Test
    fun `a failed read is retried and the next ask succeeds`() = runTest {
        val corpus = CountingCorpus(failuresFirst = 1)
        val shared = SharedCatalog(corpus)

        assertTrue(shared.load() is CatalogLoad.Failed)
        val recovered = readyOf(shared.load()).catalog

        assertNotNull(recovered)
        assertEquals("the second ask did not read again", 2, corpus.reads.get())
        assertSame("the recovered catalog was not then held", recovered, readyOf(shared.load()).catalog)
        assertEquals("the third ask read a third time", 2, corpus.reads.get())
    }

    /**
     * **One surface's failure cannot become another surface's**, which is the property three
     * separate coordinators used to get for free and a shared holder has to be built to keep.
     *
     * It is kept by the answer being a value rather than a field: what a caller was handed is
     * what that caller's own attempt produced, and no later attempt by anybody can reach back
     * and change it. The old shape, a nullable catalog from one call and a failure string
     * from a field beside it, could not promise that with three callers: a surface that
     * failed would read the field after another surface had cleared it and would render
     * nothing while reporting that nothing was wrong.
     */
    @Test
    fun `a failure a caller was handed is not rewritten by a later success`() = runTest {
        val shared = SharedCatalog(CountingCorpus(failuresFirst = 1))

        val failed = shared.load()
        val succeeded = shared.load()

        assertTrue(failed is CatalogLoad.Failed)
        assertTrue(succeeded is CatalogLoad.Ready)
        assertTrue(
            "the first caller's reason did not survive the second caller's success",
            (failed as CatalogLoad.Failed).reason.contains("FileNotFoundException"),
        )
    }

    /** The held catalog is the real one, not a shell that parsed nothing. */
    @Test
    fun `the catalog that comes back is the one built from the committed corpus`() = runTest {
        val catalog = readyOf(SharedCatalog(CountingCorpus()).load()).catalog

        assertEquals(
            "the shared catalog holds a different set of families than the fixture's",
            CorpusFixture.catalog.families.map { it.purpose to it.key }.toSet(),
            catalog.families.map { it.purpose to it.key }.toSet(),
        )
        assertEquals(
            "the shared catalog holds a different set of rules than the fixture's",
            CorpusFixture.catalog.rules.size,
            catalog.rules.size,
        )
    }
}
