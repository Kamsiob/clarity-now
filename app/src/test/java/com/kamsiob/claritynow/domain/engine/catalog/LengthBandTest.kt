package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `lengthBand` is computed at catalog load from the realized word count, never read from a
 * corpus tag. CLARITY_LOGIC_ENGINE.md 7.5 and `MASTER_BUILD_PROMPT.md` 17.
 *
 * The reason for computing rather than tagging is drift, and the reason for a test rather
 * than a code review is that the drift is invisible. A stale tag does not produce a wrong
 * sentence. It produces a report whose leads all happen to sit in one band, which reads as
 * subtly monotonous with nothing failing anywhere.
 *
 * The last test is the one that would catch a regression to hand tagging. It asserts that
 * the fifteen advisory `[S]` markers in `CORPUS_2_REPORT.md` are recorded and decide
 * nothing, by checking each one against the band the words actually produce.
 */
class LengthBandTest {

    @Test
    fun `the band boundaries are the ones 7 point 5 states`() {
        assertEquals(LengthBand.SHORT, LengthBands.bandFor("one two three four five six"))
        assertEquals(LengthBand.MEDIUM, LengthBands.bandFor("one two three four five six seven"))
        assertEquals(
            LengthBand.MEDIUM,
            LengthBands.bandFor("one two three four five six seven eight nine ten eleven twelve thirteen fourteen"),
        )
        assertEquals(
            LengthBand.LONG,
            LengthBands.bandFor(
                "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen",
            ),
        )
    }

    @Test
    fun `a slot marker counts as exactly one word`() {
        assertEquals(4, LengthBands.wordCount("{n} in, {m} out."))
        assertEquals(
            "a marker collapses to one token rather than to its longest rendering, so the band " +
                "cannot change with the day's data",
            LengthBands.wordCount("{ageDays} on {itemTitle}."),
            LengthBands.wordCount("X on Y."),
        )
    }

    @Test
    fun `a line longer than the corpus declares a band for is refused`() {
        val tooLong = (1..25).joinToString(" ") { "word" }
        val thrown = runCatching { LengthBands.bandFor(tooLong) }.exceptionOrNull()
        assertTrue("expected a refusal, got $thrown", thrown is IllegalArgumentException)
    }

    @Test
    fun `every lead in every corpus file carries the band its words produce`() {
        val mismatched = CorpusFixture.catalog.allVariants.filter {
            it.lengthBand != LengthBands.bandFor(it.statement.text)
        }
        assertTrue("computed bands disagree with the templates: $mismatched", mismatched.isEmpty())
    }

    @Test
    fun `every band is reachable in the corpus as it stands`() {
        val bands = CorpusFixture.catalog.allVariants.map { it.lengthBand }.toSet()
        assertEquals(
            "a band no line reaches means the composer's rhythm rule has nothing to alternate with",
            LengthBand.entries.toSet(),
            bands,
        )
    }

    @Test
    fun `the advisory short markers are recorded and decide nothing`() {
        val marked = CorpusFixture.catalog.allVariants.filter { it.shortMarker }
        assertEquals(
            "CORPUS_2_REPORT.md carries fourteen [S] authoring hints on keyed lines",
            SHORT_MARKERS_IN_THE_REPORT_CORPUS,
            marked.size,
        )
        for (variant in marked) {
            assertEquals(
                "${variant.origin} carries an [S] hint and the computed band must still win",
                LengthBands.bandFor(variant.statement.text),
                variant.lengthBand,
            )
        }
        assertTrue(
            "every [S] hint is on a line that really is short, which is what makes the hints " +
                "harmless rather than wrong. A hint on a longer line would be a tag drifting",
            marked.all { it.lengthBand == LengthBand.SHORT },
        )
    }

    private companion object {
        const val SHORT_MARKERS_IN_THE_REPORT_CORPUS = 14
    }
}
