package com.kamsiob.claritynow.domain.engine.validate

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every authored line in all three corpora, read against the patterns the validator vetoes
 * on. CLARITY_LOGIC_ENGINE.md 8, checks 8 and 10.
 *
 * ## Why this test is here rather than in the catalog package
 *
 * It is not checking the corpus. The language gate in `build.gradle.kts` and the catalog
 * integrity tests already do that. **It is checking the validator**, from the only angle
 * that catches the mistake this layer is most likely to make.
 *
 * A pattern written a little too widely does not fail anything. It vetoes an approved line
 * at runtime, the next ranked selection is realized instead, and the only symptom is an
 * engine that speaks slightly less than it should and never uses one of its benches. There
 * is no screen on which that looks like a bug. Running all 1,500 authored lines through the
 * word level checks is the only way to see it, and it costs one file read.
 *
 * The failure this guards against already came close once. Section 8 lists `have been`
 * among the passive forms check 10 should reject, and three approved neutral agent lines
 * are built on it: `{areaName} has been still since {sinceRef}`, `The week has been quiet
 * here` and `Nothing has been lost. It is all still here.` A literal reading of that
 * example would have taken all three off the screen.
 *
 * ## What counts as an authored line
 *
 * A line inside a fenced block that begins with a dotted key, which is how every volume
 * counts its own totals. That deliberately includes questions and response labels as well
 * as statements: all three are rendered text, and a banned word is banned in a tappable
 * label exactly as much as in a sentence.
 */
class CorpusVocabularyTest {

    private data class Authored(val file: String, val number: Int, val key: String, val tag: String?, val text: String) {
        override fun toString(): String = "$file:$number $key `$text`"
    }

    @Test
    fun `no authored line uses a word or construction the validator bans`() {
        val offenders = authored().mapNotNull { line ->
            val patterns = ValidatorVocabulary.BANNED_WORDS +
                ValidatorVocabulary.BANNED_PHRASES +
                ValidatorVocabulary.BLAME_CONSTRUCTIONS +
                ValidatorVocabulary.OTHER_SPELLING_FORMS
            val hit = patterns.firstOrNull { (pattern, _) -> pattern.containsMatchIn(line.text) }
            if (hit == null) null else "$line matches ${hit.second}"
        }
        assertTrue(
            "the validator would veto these authored lines at runtime, and nothing on a screen " +
                "would show it:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    @Test
    fun `no authored line carries a dash, an exclamation mark or a character above ASCII`() {
        val offenders = authored().mapNotNull { line ->
            when {
                line.text.contains(ValidatorVocabulary.EM_DASH) -> "$line has an em dash"
                line.text.contains(ValidatorVocabulary.EN_DASH) -> "$line has an en dash"
                line.text.contains('!') -> "$line has an exclamation mark"
                line.text.any { it.code > 127 } -> "$line has a character above ASCII"
                else -> null
            }
        }
        assertTrue(offenders.joinToString("\n"), offenders.isEmpty())
    }

    /**
     * The register check, over every line the corpus tags `[N]`.
     *
     * This is the pattern with the least margin in it, because the construction it rejects
     * and the construction the register requires are one word apart.
     */
    @Test
    fun `no neutral agent line reads as the passive voice with the agent deleted`() {
        val neutral = authored().filter { it.tag == "N" }
        assertTrue("no line in any corpus is tagged [N], so this test is checking nothing", neutral.size > 20)
        val offenders = neutral.mapNotNull { line ->
            val hit = ValidatorVocabulary.AGENT_DELETED_PASSIVES
                .firstNotNullOfOrNull { (pattern, name) -> pattern.find(line.text)?.let { name to it.value } }
            if (hit == null) null else "$line reads as ${hit.first}: `${hit.second}`"
        }
        assertTrue(
            "check 10 would silence these approved neutral agent lines:\n" + offenders.joinToString("\n"),
            offenders.isEmpty(),
        )
    }

    /** Every keyed line inside a fenced block, across the three volumes. */
    private fun authored(): List<Authored> = CORPORA.flatMap { fileName ->
        var inFence = false
        read(fileName).split('\n').mapIndexedNotNull { index, raw ->
            if (raw.trimStart().startsWith("```")) {
                inFence = !inFence
                return@mapIndexedNotNull null
            }
            if (!inFence) return@mapIndexedNotNull null
            val matched = KEYED.matchEntire(raw.trimEnd()) ?: return@mapIndexedNotNull null
            val (key, tag, text) = matched.destructured
            Authored(
                file = fileName,
                number = index + 1,
                key = key,
                tag = tag.ifEmpty { null },
                // The advisory `[S]` marker is an authoring hint, never part of the sentence.
                text = text.removeSuffix("[S]").trim(),
            )
        }
    }

    /**
     * `key  [R]  the sentence`, with the register tag optional because the pattern benches
     * carry none.
     */
    private val KEYED = Regex("""^([a-z][A-Za-z0-9]*(?:\.[A-Za-z0-9]+)+)\s+(?:\[([A-Z])]\s+)?(\S.*)$""")

    private val CORPORA = listOf("CORPUS_1_PULSE.md", "CORPUS_2_REPORT.md", "CORPUS_3_MOMENTUM.md")

    /**
     * Unit tests run from the app module directory, which `DomainPurityTest` asserts before
     * it scans anything and `GoldenFixture` has relied on since phase 1. Checked here too,
     * because a wrong working directory would empty every list above and pass silently.
     */
    private fun read(fileName: String): String {
        val file = File("..", fileName)
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run is in " +
                File("").absolutePath,
            File("build.gradle.kts").isFile,
        )
        assertTrue("missing ${file.path}", file.isFile)
        return file.readText()
    }
}
