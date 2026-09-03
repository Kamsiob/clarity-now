package com.kamsiob.claritynow.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No sentence exists in both `strings.xml` and a corpus. `CLAUDE.md` rule 8.
 *
 * ## The defect this was written for
 *
 * `reentry_title` was `Everything is where you left it.`, which is `quietday.s1.07` in
 * `CORPUS_1_PULSE.md` word for word, tagged `[O]` for observation. Two paths to one
 * sentence is what rule 8 forbids, and the consequence is concrete rather than tidy: the
 * engine's repeat filter works on corpus line ids, so it cannot see a copy sitting in a
 * resource file. The app could tell somebody the same thing twice in one week while
 * believing it had said it once.
 *
 * ## Why the check is verbatim, and what that does and does not buy
 *
 * A near-duplicate is a judgment and a verbatim duplicate is a fact. This test catches
 * the fact, which is also the failure mode that actually happens: nobody writes a
 * paraphrase of a corpus line into `strings.xml` by accident, they paste the line.
 *
 * It does not, and cannot, decide whether a given interface string ought to be an
 * observation. That is rule 8's other half and it is a matter for review. What this holds
 * is the mechanical half, permanently and for free.
 *
 * ## Both directions of the comparison matter
 *
 * A corpus line pasted into `strings.xml` and an interface string promoted into a corpus
 * are the same defect seen from two ends, and this test cannot tell which happened. It
 * says only that the sentence is in both places, which is the thing that must not be
 * true.
 */
class NoCorpusInStringsTest {

    @Test
    fun `no interface string is also a corpus line`() {
        val corpus = corpusLines()
        assertTrue(
            "expected to have read the corpora, and read nothing. Without them this " +
                "test passes vacuously.",
            corpus.size > 1_000,
        )

        val collisions = interfaceStrings()
            .filter { (_, value) -> normalize(value) in corpus }
            .map { (key, value) -> "$key is a corpus line: $value" }

        assertTrue(
            "a sentence about a person's own data has two paths to the screen. " +
                "CLAUDE.md rule 8: it comes from a corpus, through the engine, or it is " +
                "not that kind of sentence and needs different words.\n" +
                collisions.joinToString("\n"),
            collisions.isEmpty(),
        )
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Every authored line in the three corpora, normalized.
     *
     * A corpus line is `id  [TAG]  the sentence`, with the tag optional on the question
     * and response rows. Anything with a `{placeholder}` in it is skipped: it cannot
     * collide with a resource string, because a resource carrying `{dayCount}` would be
     * drawing braces on the screen.
     */
    private fun corpusLines(): Set<String> = listOf(
        "CORPUS_1_PULSE.md",
        "CORPUS_2_REPORT.md",
        "CORPUS_3_MOMENTUM.md",
    ).flatMap { name ->
        File("..", name).readLines().mapNotNull { line ->
            val match = CORPUS_LINE.find(line) ?: return@mapNotNull null
            val sentence = match.groupValues[1].trim()
            if (sentence.isEmpty() || '{' in sentence) null else normalize(sentence)
        }
    }.toSet()

    /** Every `<string>` in the one resource file, by name. */
    private fun interfaceStrings(): List<Pair<String, String>> =
        STRING_ELEMENT.findAll(File("src/main/res/values/strings.xml").readText())
            .map { it.groupValues[1] to it.groupValues[2] }
            .filter { (_, value) -> '%' !in value }
            .toList()

    /**
     * Case, whitespace and the escaping the two file formats disagree about.
     *
     * `strings.xml` writes an apostrophe as `\'` and a corpus writes it plain, so the two
     * would never match on any sentence with one in it, which is a good share of the
     * corpus. XML entities are decoded for the same reason.
     */
    private fun normalize(text: String): String = text
        .replace("\\'", "'")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()

    private companion object {
        /** `someid.s1.07  [O]  The sentence.` and the tagless question and response rows. */
        val CORPUS_LINE = Regex("""^[a-z][\w.]*\s{2,}(?:\[[A-Z]]\s{2,})?(.+)$""")
        val STRING_ELEMENT = Regex("""<string name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
    }
}
