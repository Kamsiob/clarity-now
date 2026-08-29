package com.kamsiob.claritynow.domain.corpus

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * There is one catalog in the process, checked by reading the source.
 * `MASTER_BUILD_PROMPT.md` 11.7, issue #55.
 *
 * `SharedCatalogTest` proves that one holder parses once. That is half the claim. The other
 * half is that there is one holder, and no unit test can see the whole of a process: the
 * three coordinators are built from a Room database and an `AssetManager`, so a test that
 * assembled all three to count their parses would be an instrumentation test of the graph
 * rather than a check anybody runs.
 *
 * So it is read out of the source instead, which is the shape `DomainPurityTest` established
 * in phase 1 for exactly this reason and for exactly this class of defect: **three copies of
 * correct code, each one right where it stands.** Nothing was wrong on any screen while there
 * were three catalogs, so there was nothing for a behavioral test to catch. What can be
 * caught is a fourth appearing.
 *
 * Comment lines are stripped before matching, for the reason `DomainPurityTest` gives: two
 * files in `domain.engine.catalog` explain themselves by naming `ClarityCatalog.build`, and a
 * scan that read comments would fail on the documentation of the thing it is guarding.
 */
class CatalogSharingTest {

    private val mainRoot = "src/main/java/com/kamsiob/claritynow"

    private val holder = "$mainRoot/domain/corpus/SharedCatalog.kt"

    private val graph = "$mainRoot/di/ClarityGraph.kt"

    private val coordinators = listOf(
        "$mainRoot/domain/pulse/PulseCoordinator.kt",
        "$mainRoot/domain/momentum/MomentumCoordinator.kt",
        "$mainRoot/ui/report/ReportCoordinator.kt",
    )

    private data class SourceLine(val path: String, val number: Int, val text: String)

    private fun isComment(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
    }

    private fun codeLinesOf(file: File): List<SourceLine> =
        file.readLines().mapIndexedNotNull { index, text ->
            if (isComment(text)) null else SourceLine(file.path, index + 1, text)
        }

    private fun mainCodeLines(): List<SourceLine> =
        File(mainRoot).walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { codeLinesOf(it) }
            .toList()

    private fun hits(pattern: Regex): List<SourceLine> =
        mainCodeLines().filter { pattern.containsMatchIn(it.text) }

    private fun describe(lines: List<SourceLine>): String =
        lines.joinToString("\n") { "${it.path}:${it.number}: ${it.text.trim()}" }

    @Test
    fun `this test looks where the source actually is`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run is " +
                "in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        (listOf(holder, graph) + coordinators).forEach {
            assertTrue("missing $it, so every scan in this class would pass vacuously", File(it).isFile)
        }
    }

    @Test
    fun `exactly one place in the app builds a catalog`() {
        val found = hits(Regex("""\bClarityCatalog\.build\s*\("""))
        assertEquals(
            "a catalog is built somewhere other than SharedCatalog. Building it reads three " +
                "markdown files, parses them, computes every length band from the realized " +
                "word count and runs the integrity checks, and MASTER_BUILD_PROMPT 11.7 asks " +
                "for one per process. Take SharedCatalog as a parameter instead:\n" +
                describe(found),
            listOf(holder),
            found.map { it.path },
        )
    }

    @Test
    fun `exactly one place in the app builds the holder`() {
        // The declaration is not a construction. Everything else is.
        val found = hits(Regex("""(?<!class )\bSharedCatalog\s*\("""))
        assertEquals(
            "a second SharedCatalog would be a second catalog with extra steps. ClarityGraph " +
                "is where the one lives, because it is the only file allowed to know both " +
                "about Android and about every layer below it:\n" + describe(found),
            listOf(graph),
            found.map { it.path },
        )
    }

    @Test
    fun `all three coordinators take the shared catalog rather than a corpus`() {
        coordinators.forEach { path ->
            val code = codeLinesOf(File(path))
            assertTrue(
                "$path does not take a SharedCatalog",
                code.any { Regex("""\bcatalog:\s*SharedCatalog\b""").containsMatchIn(it.text) },
            )
            val reader = code.filter { Regex("""\bCorpusSource\b""").containsMatchIn(it.text) }
            assertTrue(
                "$path names CorpusSource. A coordinator reads no files: the platform half of " +
                    "that seam is private to ClarityGraph, and a coordinator holding one is a " +
                    "coordinator that can build its own catalog:\n" + describe(reader),
                reader.isEmpty(),
            )
        }
    }

    /**
     * The parse is dispatched, and nothing at a call site can check that.
     *
     * `CorpusSource` puts the file read on an IO dispatcher and `withContext` returns to the
     * caller, so before this holder existed the parse itself ran wherever the caller was
     * standing. All three surfaces reach it from a `viewModelScope`, which is the main
     * dispatcher, so the expensive half was on the main thread on every surface's first ask
     * while three separate notes each recorded it as being on a background one.
     *
     * **This is a source scan and not a thread assertion on purpose.** `ClarityCatalog.build`
     * is a pure function of three strings with no hook in it, so a test can observe which
     * thread called `load` and never which thread ran the parse. What can be read is that the
     * one expensive call is the dispatched work, which is the thing an edit would undo.
     */
    @Test
    fun `the parse is the work handed to the default dispatcher`() {
        val code = codeLinesOf(File(holder))
        val dispatched = code.indexOfFirst {
            Regex("""withContext\s*\(\s*Dispatchers\.Default\s*\)\s*\{""").containsMatchIn(it.text)
        }
        assertTrue("SharedCatalog dispatches nothing to Dispatchers.Default", dispatched >= 0)
        assertTrue(
            "the line after the dispatch is not the parse, so the parse may be running on " +
                "whichever thread asked for it: ${code[dispatched + 1].text.trim()}",
            code[dispatched + 1].text.contains("ClarityCatalog.build("),
        )
    }
}
