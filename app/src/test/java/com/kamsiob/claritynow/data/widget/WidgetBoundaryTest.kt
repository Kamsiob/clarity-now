package com.kamsiob.claritynow.data.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The rule the whole widget phase rests on, checked by reading the source rather than
 * by trusting review. MASTER_BUILD_PROMPT 13.3 and 11.1.
 *
 * **Widgets never read a corpus and never run the engine.** A widget that composed its
 * own sentence would be a second path to the screen, and it would be the one path with
 * no validator on it: `ClarityValidator` and the report integrity checks all sit inside
 * the engine, so a sentence assembled in a Glance composable would reach a home screen
 * having passed none of them. Nothing else in this repository would notice, which is
 * why this file exists.
 *
 * It scans the source the way `domain/DomainPurityTest.kt` does, and for the same
 * reason: an import is a fact about a file that a reviewer can miss and a scan cannot.
 * Three details of the mechanism are load bearing there and are load bearing here.
 * Comment lines are stripped, so a KDoc explaining why the engine is absent does not
 * fail the check. The working directory is asserted, so a scan that finds no files
 * fails rather than passes. And both widget packages are required to exist and to hold
 * Kotlin, so deleting one does not turn this file green.
 */
class WidgetBoundaryTest {

    /** Everything a widget draws with, and the boundary it draws through. */
    private val widgetDirs = listOf(
        "src/main/java/com/kamsiob/claritynow/widget",
        "src/main/java/com/kamsiob/claritynow/data/widget",
    )

    /** The Glance half alone. The snapshot half is allowed to see the projection. */
    private val glanceDir = "src/main/java/com/kamsiob/claritynow/widget"

    private val mainSource = "src/main/java"

    private data class SourceLine(val path: String, val number: Int, val text: String)

    private fun kotlinFilesIn(path: String): List<File> {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    private fun isComment(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
    }

    private fun codeLinesIn(paths: List<String>): List<SourceLine> =
        paths.flatMap(::kotlinFilesIn).flatMap { file ->
            file.readLines().mapIndexedNotNull { index, text ->
                if (isComment(text)) null else SourceLine(file.path, index + 1, text)
            }
        }

    private fun offenders(paths: List<String>, vararg patterns: Regex): List<String> =
        codeLinesIn(paths)
            .filter { line -> patterns.any { it.containsMatchIn(line.text) } }
            .map { "${it.path}:${it.number}: ${it.text.trim()}" }

    @Test
    fun `the widget packages exist where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        widgetDirs.forEach { path ->
            assertTrue("missing widget package $path", File(path).isDirectory)
            assertTrue(
                "no Kotlin source under $path, so every scan in this class would pass " +
                    "vacuously",
                kotlinFilesIn(path).isNotEmpty(),
            )
        }
    }

    @Test
    fun `no widget reads a corpus or runs the engine`() {
        val found = offenders(
            widgetDirs,
            Regex("""^\s*import\s+com\.kamsiob\.claritynow\.domain\.engine"""),
            Regex("""^\s*import\s+com\.kamsiob\.claritynow\.domain\.guidance"""),
            Regex("""\bCorpus(Source|Text|Volume)\b"""),
            Regex("""\bClarityEngine\b"""),
            Regex("""assets/corpus"""),
        )
        assertTrue(
            "a widget reached for the engine or a corpus. MASTER_BUILD_PROMPT 13.3: any " +
                "sentence a widget shows was produced by the engine, written into the " +
                "widget snapshot, and is repeated verbatim. Add a field to " +
                "ClarityWidgetSnapshot and fill it where the engine already runs:\n" +
                found.joinToString("\n"),
            found.isEmpty(),
        )
    }

    @Test
    fun `the Glance half reads the snapshot rather than the log`() {
        val found = offenders(
            listOf(glanceDir),
            Regex("""^\s*import\s+com\.kamsiob\.claritynow\.domain\.replay"""),
            Regex("""^\s*import\s+com\.kamsiob\.claritynow\.domain\.(pulse|report|momentum)"""),
            Regex("""^\s*import\s+com\.kamsiob\.claritynow\.data\.(db|event|repo)"""),
        )
        assertTrue(
            "a widget imported the projection, the log or the database. The snapshot is " +
                "the whole of a widget's access to this app, per design-v3.md 12.1, and " +
                "a widget update runs in a process that may have replayed nothing:\n" +
                found.joinToString("\n"),
            found.isEmpty(),
        )
    }

    /**
     * DataStore permits one instance per file per process and throws on the second, and
     * the throw lands wherever the second instance is first touched, which for this file
     * is a widget update on somebody's home screen.
     *
     * Phase 12 built its two halves at once and both needed the same file, so the rule
     * is written down here rather than remembered: one delegate per name, and everything
     * else borrows it.
     */
    @Test
    fun `no two DataStores are opened over one file`() {
        val declarations = kotlinFilesIn(mainSource).flatMap { file ->
            DATA_STORE.findAll(file.readText()).map { match -> file.path to match.groupValues[1] }
        }
        val duplicated = declarations.groupBy { it.second }.filterValues { it.size > 1 }
        assertEquals(
            "two DataStore delegates over one file name, which throws the first time " +
                "both are touched in one process: " +
                duplicated.entries.joinToString { (name, sites) ->
                    "$name in ${sites.joinToString { it.first }}"
                },
            emptyMap<String, List<Pair<String, String>>>(),
            duplicated,
        )
    }

    private companion object {
        val DATA_STORE = Regex("preferencesDataStore\\(\\s*name\\s*=\\s*\"([^\"]+)\"")
    }
}
