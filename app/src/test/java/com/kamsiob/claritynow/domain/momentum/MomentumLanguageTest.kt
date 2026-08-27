package com.kamsiob.claritynow.domain.momentum

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.LengthBands
import com.kamsiob.claritynow.domain.engine.catalog.Purpose
import com.kamsiob.claritynow.domain.engine.validate.LengthLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Momentum observes and never interprets**, checked against the real corpus.
 * `CLARITY_LOGIC_ENGINE.md` 6.5 and `CORPUS_3_MOMENTUM.md`.
 *
 * 6.5: "Its families must never contain because, suggests, means, or any causal
 * construction. A test enforces this." This is that test, and the file it enforces states
 * the rule at its own head and calls it "the single rule that separates Momentum from the
 * Report".
 *
 * ## Why it runs over the parsed corpus rather than over the file
 *
 * Every line here comes out of `CorpusFixture`, which parses the three committed markdown
 * files. Scanning the raw text would also read the prose that explains the rule, which
 * contains every forbidden word in this file by necessity, and a test that has to skip
 * around its own subject is a test that eventually skips something else. Going through the
 * parser also means a line the walker never reached cannot pass by being invisible.
 *
 * ## The detector is tested too, and that is not ceremony
 *
 * A vocabulary test whose vocabulary is wrong passes forever and silently, which is the
 * exact failure mode the whole engine is built to avoid. So the detector test below runs it
 * over sentences that are causal on purpose. If
 * that test ever fails, the corpus test above it means nothing.
 */
class MomentumLanguageTest {

    /**
     * One keyed line of `CORPUS_3_MOMENTUM.md`, with enough of its origin to find it.
     */
    private data class Line(val origin: String, val text: String)

    /**
     * Every authored line in volume 3: both purposes, every family, plus the shared banner
     * caption bench.
     *
     * The caption bench is included deliberately. It is not a family and 6.5 names families,
     * but a caption sits directly under a banner sentence on the screen a person opens most
     * often, and a causal caption would break the rule where it is most visible.
     */
    private fun momentumLines(): List<Line> {
        val fromFamilies = CorpusFixture.momentum.families.flatMap { family ->
            family.stages.flatMap { stage ->
                (stage.variants + stage.extensions).map { Line(it.origin, it.statement.text) }
            }
        }
        val fromBenches = CorpusFixture.momentum.auxiliary.flatMap { (bench, lines) ->
            lines.map { Line("$bench ${it.key}", it.text) }
        }
        return fromFamilies + fromBenches
    }

    @Test
    fun `no Momentum family line contains a causal construction`() {
        val offenders = momentumLines().mapNotNull { line ->
            causalConstructionIn(line.text)?.let { "${line.origin}: `${line.text}` contains `$it`" }
        }

        assertEquals(
            "CLARITY_LOGIC_ENGINE.md 6.5: Momentum observes and never interprets, and its " +
                "lines may not contain because, suggests, means or any causal construction. " +
                "That vocabulary belongs to the Report",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `the detector finds a causal construction when there is one`() {
        // One per mechanism the detector implements: a single word, a multi word phrase,
        // and the bare causal `so`, which is the one that needs a rule rather than a list.
        val causal = listOf(
            "Work moved most because you finished the report.",
            "The queues grew, which is why Home stalled.",
            "Nothing moved in Home, so the week stayed narrow.",
            "A quiet fortnight suggests a change of pace.",
            "Fourteen days, nine active, which means a steady stretch.",
        )

        causal.forEach { sentence ->
            assertTrue(
                "the causal detector missed `$sentence`, which makes the corpus test above " +
                    "it vacuous",
                causalConstructionIn(sentence) != null,
            )
        }
    }

    @Test
    fun `the detector leaves the temporal uses alone`() {
        // Both appear in the real corpus and neither is causal. `so far` is a point in a
        // week and `since` is a date. A detector that banned either would fail the file it
        // is meant to protect, and the next person would loosen the detector rather than
        // the line.
        val innocent = listOf(
            "A strong week so far.",
            "Your best fortnight since March.",
            "{n} things have gone since it picked up.",
        )

        innocent.forEach { sentence ->
            assertEquals(
                "the causal detector flagged `$sentence`, which is temporal rather than causal",
                null,
                causalConstructionIn(sentence),
            )
        }
    }

    @Test
    fun `no Momentum line asks a question`() {
        // `CORPUS_3_MOMENTUM.md` authoring rule 2: the Pulse asks, Momentum does not. A
        // question on a screen read many times a day is a demand rather than a mirror.
        val offenders = momentumLines()
            .filter { it.text.contains('?') }
            .map { "${it.origin}: `${it.text}`" }

        assertEquals("Momentum never asks a question", emptyList<String>(), offenders)
    }

    @Test
    fun `every Momentum headline is under twelve words`() {
        val limit = LengthLimits.MOMENTUM_HEADLINE_MAX_WORDS
        val offenders = CorpusFixture.momentum.families
            .filter { it.purpose == Purpose.MOMENTUM_HEADLINE }
            .flatMap { it.allVariants }
            .filter { LengthBands.wordCount(it.statement.text) > limit }
            .map { "${it.origin}: ${LengthBands.wordCount(it.statement.text)} words" }

        assertEquals(
            "CLARITY_LOGIC_ENGINE.md 6.5 and 8 check 9: a Momentum headline is under twelve " +
                "words, which is $limit or fewer",
            emptyList<String>(),
            offenders,
        )
    }

    @Test
    fun `both Momentum purposes have language and neither is empty`() {
        // The failure this catches is a heading quietly renaming itself, which takes a
        // whole purpose out of the corpus without anything crashing: the screen would then
        // be silent forever and look exactly like a person nothing applied to.
        listOf(Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER).forEach { purpose ->
            val lines = CorpusFixture.momentum.families
                .filter { it.purpose == purpose }
                .flatMap { it.allVariants }
            assertTrue("$purpose has no authored lines at all", lines.isNotEmpty())
        }
    }

    /**
     * The causal construction in [text], or null.
     *
     * Three mechanisms, and the third is the reason this is a function rather than a
     * regular expression.
     *
     * **Single words**, matched whole so `causes` is caught and `because` inside no real
     * word is missed.
     *
     * **Phrases**, matched as substrings, for the constructions that are causal only as a
     * unit.
     *
     * **`so`, but only where it is causal.** `CORPUS_3_MOMENTUM.md` bans `so` and uses `so
     * far` eleven times, in six families, because `so far` is a point in the week and not a
     * reason for anything. `since` is the same shape in the other direction, temporal in
     * both of its appearances, and is deliberately not on the list at all: banning it would
     * cost two true lines to catch a construction nobody has written.
     */
    private fun causalConstructionIn(text: String): String? {
        val lower = text.lowercase()
        CAUSAL_WORDS.firstOrNull { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(lower) }
            ?.let { return it }
        CAUSAL_PHRASES.firstOrNull { lower.contains(it) }?.let { return it }
        if (CAUSAL_SO.containsMatchIn(lower)) return "so"
        return null
    }

    private companion object {

        /**
         * The vocabulary 6.5 and `CORPUS_3_MOMENTUM.md` name, plus the rest of the same
         * family.
         *
         * The three the specification names by hand are `because`, `suggests` and `means`.
         * The others are the words that do the same job: they assert that one thing follows
         * from another, which is an interpretation, and interpretation belongs to the
         * Report.
         */
        val CAUSAL_WORDS = listOf(
            "because", "therefore", "thus", "hence", "consequently", "accordingly",
            "suggest", "suggests", "suggesting", "mean", "means", "meaning",
            "imply", "implies", "indicate", "indicates", "reflect", "reflects",
            "cause", "causes", "caused", "causing", "explain", "explains",
        )

        val CAUSAL_PHRASES = listOf(
            "which is why", "that is why", "due to", "as a result",
            "leads to", "led to", "in order to", "so that", "the reason",
        )

        /** `so` used as a conjunction. `so far` is temporal and is the one exemption. */
        val CAUSAL_SO = Regex("""\bso\b(?!\s+far\b)""")
    }
}
