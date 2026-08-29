package com.kamsiob.claritynow.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The pure packages, checked by reading the source rather than by trusting review.
 * CLAUDE.md rule 5, MASTER_BUILD_PROMPT 11.4.
 *
 * `docs/ARCHITECTURE.md` has said "Tests assert this" since phase 1 and until this
 * file existed the sentence was not true. The rule it enforces is the one that makes
 * a second implementation of this app possible at all: a fold that reads a wall
 * clock, a random number or a JVM hash code answers differently on two devices, and
 * the disagreement stays invisible until two logs are merged, which is the one case
 * nobody checks by hand.
 *
 * Three details of the mechanism are load bearing. Each of them, got wrong, makes
 * this test either fail on correct code or pass on broken code.
 *
 * 1. **It scans the four pure subdirectories, never `domain` as a whole.**
 *    `ClarityClock` sits in the root `domain` package and calls
 *    `System.currentTimeMillis()`. It is the one place in the app allowed to, and
 *    the point of the layering is that everything under it takes the instant as a
 *    parameter instead.
 * 2. **It strips comment lines before matching.** `StableHash`'s own KDoc explains
 *    why it exists by naming `String.hashCode()`, so a scan that read comments would
 *    fail on the very file that solves the problem.
 * 3. **It asserts its own working directory.** Gradle runs unit tests from the module
 *    directory, which is what makes these relative paths resolve and what
 *    `GoldenFixture.LOG_PATH` has depended on since phase 1. If that ever changes,
 *    every scan below finds no files and passes, so the assumption is checked first
 *    rather than assumed.
 *
 * **`domain.guidance` was listed and not required until phase 9b, and is now both.** It did
 * not exist while layer 6 was unbuilt, so its absence had to be tolerated and its arrival
 * had to be scanned without anyone remembering to come back here. It arrived. The exemption
 * is removed rather than left standing, because a package that is optional in this list is
 * a package that could be deleted without this test noticing, and layer 6 is the one part
 * of the engine `MASTER_BUILD_PROMPT.md` 19 says may one day be deliberately removed. When
 * that day comes, this line is the one that says so out loud.
 */
class DomainPurityTest {

    private val pureDirs = listOf(
        "src/main/java/com/kamsiob/claritynow/domain/engine",
        "src/main/java/com/kamsiob/claritynow/domain/guidance",
        "src/main/java/com/kamsiob/claritynow/domain/replay",
        "src/main/java/com/kamsiob/claritynow/domain/query",
    )

    /** All four. A missing one is a broken test, not a clean package. */
    private val required = pureDirs

    private val clockPath = "src/main/java/com/kamsiob/claritynow/domain/ClarityClock.kt"

    private data class SourceLine(val path: String, val number: Int, val text: String)

    private fun kotlinFilesIn(path: String): List<File> {
        val dir = File(path)
        if (!dir.isDirectory) return emptyList()
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    private fun pureSources(): List<File> = pureDirs.flatMap(::kotlinFilesIn)

    /**
     * A line that starts a comment. Enough for a text scan, and deliberately not a
     * parser: the only false negative it can produce is a pattern hidden inside a
     * trailing comment on a line of real code, which would still be caught by the
     * code on that line being read.
     */
    private fun isComment(text: String): Boolean {
        val trimmed = text.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")
    }

    private fun codeLines(): List<SourceLine> = pureSources().flatMap { file ->
        file.readLines().mapIndexedNotNull { index, text ->
            if (isComment(text)) null else SourceLine(file.path, index + 1, text)
        }
    }

    private fun offenders(vararg patterns: Regex): List<String> = codeLines()
        .filter { line -> patterns.any { it.containsMatchIn(line.text) } }
        .map { "${it.path}:${it.number}: ${it.text.trim()}" }

    @Test
    fun `the pure packages exist where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile && File(clockPath).isFile,
        )
        required.forEach { path ->
            assertTrue("missing pure package $path", File(path).isDirectory)
            assertTrue(
                "no Kotlin source under $path, so every scan in this class would pass " +
                    "vacuously",
                kotlinFilesIn(path).isNotEmpty(),
            )
        }
        assertTrue(
            "ClarityClock must stay outside the scan; it is the one place in the app " +
                "that is allowed to read a wall clock",
            pureSources().none { it.name == "ClarityClock.kt" },
        )
    }

    @Test
    fun `nothing in the pure packages imports android`() {
        val found = offenders(
            Regex("""^\s*import\s+android\."""),
            Regex("""^\s*import\s+androidx\."""),
        )
        assertTrue(
            "an Android import in a pure package. These four packages have to compile " +
                "and run on a desktop JVM with no Android at all:\n" + found.joinToString("\n"),
            found.isEmpty(),
        )
    }

    @Test
    fun `nothing in the pure packages reads a wall clock`() {
        val found = offenders(
            Regex("""\bSystem\.currentTimeMillis\b"""),
            Regex("""\bSystem\.nanoTime\b"""),
            Regex("""\bInstant\.now\("""),
            Regex("""\bLocalDate\.now\("""),
            Regex("""\bLocalDateTime\.now\("""),
            Regex("""\bZoneId\.systemDefault\("""),
        )
        assertTrue(
            "a pure package read the ambient clock or the ambient zone. Take the instant " +
                "as a parameter, or inject ClarityClock at the layer above:\n" +
                found.joinToString("\n"),
            found.isEmpty(),
        )
    }

    @Test
    fun `nothing in the pure packages uses a random or an unstable hash`() {
        val found = offenders(
            Regex("""^\s*import\s+(kotlin|java\.util)\.[Rr]andom"""),
            Regex("""\bRandom\s*\("""),
            Regex("""\.hashCode\(\)"""),
        )
        assertTrue(
            "a pure package used a random number or a JVM hash code. Neither is stable " +
                "across two runtimes, so two devices would disagree. Use StableHash, which " +
                "is FNV-1a 64 bit, and derive anything variable from the log:\n" +
                found.joinToString("\n"),
            found.isEmpty(),
        )
    }
}
