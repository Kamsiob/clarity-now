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

    /**
     * The pattern section's empty state, which is the fifth bench and the newest.
     *
     * `CORPUS_2_REPORT.md` 3.16 is authored as a pattern family and is not one. It has no
     * rule, on purpose and by an owner's decision recorded at `ReportComposer.patternNote`,
     * and the Report renders it here instead. What this asserts is that renders means
     * renders: a real line out of the shipped corpus, chosen the way every bench is chosen,
     * and through layer 5 like everything else that reaches a screen.
     */
    @Test
    fun `the pattern empty state resolves to a corpus line the validator accepts`() {
        val note = language.insufficientData(facts, ReportFixture.DATE_KEY)
        assertNotNull("no pt.none line survived layer 5", note)
        assertTrue(note!!.variantKey, note.variantKey.startsWith("pt.none."))
        assertTrue(note.text, note.text.isNotBlank())
        assertTrue("an empty state states no number: ${note.text}", note.text.none { it.isDigit() })
    }

    @Test
    fun `the pattern empty state varies with the day, like every other bench`() {
        val keys = (1..28).map { day ->
            language.insufficientData(facts, "2026-04-%02d".format(day))?.variantKey
        }
        assertTrue("every day resolved to a line", keys.all { it != null })
        assertTrue("one line on every date is a bench nobody is choosing from", keys.toSet().size > 1)
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

    /**
     * 5.2: a clause is omitted when its value is zero, and the whole line when they all
     * are. **Both of them have to be zero now**, which they did not before: `{m}` bound to
     * a measure nothing declared, so this test used to pass with `weeksOfData` at the
     * fixture's 17 and prove nothing about the second clause.
     */
    @Test
    fun `the basis line is absent when every clause of it would be zero`() {
        val facts = ValidateFixture.facts(
            pulse = ValidateFixture.pulse(answeredInWindow = 0),
            history = ValidateFixture.history(weeksOfData = 0),
        )
        assertNull(language.basis(facts, ReportFixture.DATE_KEY))
    }

    /** And each clause stands on its own when the other is zero. */
    @Test
    fun `weeks of data alone is a basis line`() {
        val facts = ValidateFixture.facts(
            pulse = ValidateFixture.pulse(answeredInWindow = 0),
            history = ValidateFixture.history(weeksOfData = 6),
        )
        assertEquals("Based on 6 weeks of data.", language.basis(facts, ReportFixture.DATE_KEY)?.rendered)
    }

    @Test
    fun `the first week states its basis in words, because there is no history to count`() {
        val facts = ValidateFixture.facts(
            history = ValidateFixture.history(daysSinceInstall = 3, weeksOfData = 0),
        )
        assertEquals("Based on your first week.", language.basis(facts, ReportFixture.DATE_KEY)?.rendered)
    }

    /**
     * The gap that test recorded is closed, and this is what closed it.
     *
     * The previous version of this test asserted that `Measures.byId("weeksOfData")` was
     * null and that the basis line therefore never said `weeks`, and its own KDoc said
     * that when somebody declared the measure the test should be replaced. This is the
     * replacement: the same two facts, both inverted, so the clause cannot silently go
     * back to dropping out.
     */
    @Test
    fun `the weeks of data clause has its measure and fills`() {
        assertNotNull(
            "`m` binds to a measure that Measures does not declare, so three of the six " +
                "basis lines in CORPUS_2_REPORT.md 5.2 can never be filled",
            Measures.byId(ReportLanguage.BINDINGS.getValue("m")),
        )
        val basis = language.basis(facts, ReportFixture.DATE_KEY)
        assertTrue("the basis line states weeks of data: ${basis?.rendered}", "weeks" in basis!!.rendered)
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
