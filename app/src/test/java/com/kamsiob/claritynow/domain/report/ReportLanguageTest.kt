package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.realize.Measures
import com.kamsiob.claritynow.domain.engine.validate.ValidateFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The four benches that are not families, read out of the committed corpus.
 * `CORPUS_2_REPORT.md` 5 and 6.
 *
 * **These run against the real corpus file, not a fixture.** A parser tested only against a
 * synthetic corpus proves the parser, and the failure this guards against is the shipped
 * file drifting away from the code that reads it. `CorpusFixture` opens the same three files
 * the APK carries.
 */
class ReportLanguageTest {

    private val language = ReportLanguage(CorpusFixture.catalog, ReportFixture.ZONE)

    private val facts = ReportFixture.facts()

    @Test
    fun `the corpus carries all four benches this screen needs`() {
        val auxiliary = CorpusFixture.catalog.auxiliary
        listOf(
            ReportLanguage.GENERATED_BENCH,
            ReportLanguage.BASIS_BENCH,
            ReportLanguage.NOTHING_BENCH,
            ReportLanguage.FIRST_WEEK_BENCH,
        ).forEach { bench ->
            assertTrue("CORPUS_2_REPORT.md carries no $bench bench", !auxiliary[bench].isNullOrEmpty())
        }
    }

    @Test
    fun `the generated line is the corpus line, unvaried`() {
        assertEquals("Generated on your device", language.generatedLine()?.text)
    }

    @Test
    fun `both edge states resolve to a line the validator accepts`() {
        val nothing = language.nothingToReport(facts, ReportFixture.DATE_KEY)
        assertNotNull("no ed.none line survived layer 5", nothing)
        assertTrue(nothing!!.variantKey, nothing.variantKey.startsWith("ed.none."))
        assertTrue(nothing.text, nothing.text.isNotBlank())

        val first = language.firstWeek(facts, ReportFixture.DATE_KEY)
        assertNotNull("no ed.first line survived layer 5", first)
        assertTrue(first!!.variantKey, first.variantKey.startsWith("ed.first."))
    }

    @Test
    fun `an edge state varies with the day, because the bench is chosen the way every bench is`() {
        val keys = (1..28).map { day ->
            language.nothingToReport(facts, "2026-03-%02d".format(day))?.variantKey
        }
        assertTrue("every day resolved to the same ed.none line: ${keys.first()}", keys.toSet().size > 1)
    }

    // ------------------------------------------------------------------- the basis line

    @Test
    fun `the basis line never disagrees with its own count`() {
        // 5.2 writes the singular and the plural as separate lines so that `1 responses`
        // cannot occur. The pairing is derived from the bench rather than from the keys, so
        // this walks the counts the pairing is derived for.
        for (answers in 1..6) {
            val basis = language.basis(
                ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = answers)),
                ReportFixture.DATE_KEY,
            )
            assertNotNull("no basis line for $answers answered pulses", basis)
            val rendered = basis!!.rendered
            assertTrue(rendered, rendered.startsWith("Based on $answers "))
            if (answers == 1) {
                assertTrue(rendered, "response" in rendered)
                assertFalse("a count of one in front of a plural noun: $rendered", "responses" in rendered)
            } else {
                assertTrue(rendered, "responses" in rendered)
            }
        }
    }

    @Test
    fun `the basis line is absent when every clause of it would be zero`() {
        val facts = ValidateFixture.facts(pulse = ValidateFixture.pulse(answeredInWindow = 0))
        assertNull(language.basis(facts, ReportFixture.DATE_KEY))
    }

    @Test
    fun `the first week states its basis in words, because there is no history to count`() {
        val facts = ValidateFixture.facts(
            history = ValidateFixture.history(daysSinceInstall = 3, weeksOfData = 0),
        )
        assertEquals("Based on your first week.", language.basis(facts, ReportFixture.DATE_KEY)?.rendered)
    }

    /**
     * The gap this slice found and did not paper over, held by a test so it cannot be
     * forgotten.
     *
     * Three of the six basis lines read `and {m} weeks of data`, `{m}` is
     * `HistoryFacts.weeksOfData`, and no entry in `Measures` reads it, so those three cannot
     * be filled and drop out of the bench. The report states the shorter, true line instead.
     *
     * **When somebody adds that measure, this test fails**, and the thing to do is delete
     * this test and the note on `ReportLanguage`, because the gap will have closed. A gap
     * recorded only in a comment is a gap nobody finds.
     */
    @Test
    fun `the weeks of data clause waits on a measure, and says so`() {
        assertNull(
            "`Measures` now declares weeksOfData. The fuller basis lines in CORPUS_2_REPORT.md " +
                "5.2 can be filled, so delete this test and the note on ReportLanguage",
            Measures.byId(ReportLanguage.BINDINGS.getValue("m")),
        )
        val basis = language.basis(facts, ReportFixture.DATE_KEY)
        assertFalse("the basis line states weeks of data: ${basis?.rendered}", "weeks" in basis!!.rendered)
    }

    // ------------------------------------------------------------------- the caption

    @Test
    fun `the caption states three numbers, each read through a measure`() {
        val totals = language.totals(facts)
        assertEquals(listOf("totalEvents", "completions", "additions"), totals.map { it.measure })
        assertEquals(listOf(12, 5, 3), totals.map { it.value })
        assertEquals(
            listOf("window.totalEvents", "window.completions", "window.additions"),
            totals.map { it.ref.toString() },
        )
    }

    @Test
    fun `a total of zero is absent from the caption rather than stated as nought`() {
        val quiet = ValidateFixture.facts(
            window = ValidateFixture.window(totalEvents = 4, completions = 0, additions = 4, netFlow = -4),
        )
        val totals = language.totals(quiet)
        assertEquals(listOf("totalEvents", "additions"), totals.map { it.measure })
    }
}
