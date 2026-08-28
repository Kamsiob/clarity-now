package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ten thousand generated reports, and not one of them breaks section 9.
 * CLARITY_LOGIC_ENGINE.md 14, and issue #6.
 *
 * > No report contains both members of any listed pair, across 10,000 generated reports.
 *
 * ## Why ten thousand and not ten
 *
 * The composition rules are conditional on each other. The matrix only bites when two
 * conflicting families both qualify, the area cap only bites when one area is interesting
 * three times over, and the parallel clause cap only bites when three numeric leads survive
 * everything above them. A handful of hand written weeks reaches none of those, and every
 * rule would look like it held because nothing ever asked it to.
 *
 * Each week is built from a hash of its case number, so a failure names a case a person can
 * go and reproduce. See [GeneratedWeeks].
 *
 * ## What is asserted, and what is only counted
 *
 * Two different things, kept apart on purpose.
 *
 * **Asserted.** Every composed report satisfies [ReportInvariants], which restates section 9
 * from the document rather than calling the code that enforces it. And no report is ever
 * **suppressed**: the composer applies every rule while it assembles, so the report scope
 * veto should never have to fire on a report the composer itself built. A suppression here
 * is not a caught defect, it is the composer failing to prevent one.
 *
 * **Counted and printed.** How often the engine had a headline, an observation, a pattern.
 * Those are readings for phase 9 rather than pass or fail conditions: the corpus is not
 * grown until then, and asserting a number today would either be trivially true or would
 * pin the build to the size of a bench nobody has finished authoring.
 */
class ReportPropertyTest {

    private val composer = ReportComposer(CorpusFixture.catalog, ReportFixture.ZONE)

    @Test
    fun `no report breaks a composition rule across ten thousand generated weeks`() {
        var composed = 0
        var empty = 0
        var withHeadline = 0
        var withPattern = 0
        var observations = 0
        val bySize = IntArray(ReportComposer.MAX_OBSERVATIONS + 1)
        val suppressed = mutableListOf<String>()
        val broken = mutableListOf<String>()
        val rhythm = ReportRhythm()

        for (case in 0 until GeneratedWeeks.CASES) {
            val facts = GeneratedWeeks.facts(case)
            val history = GeneratedWeeks.history(case, CorpusFixture.catalog)
            when (val outcome = composer.compose(facts, history, WEEK_START_KEY)) {
                is ReportOutcome.Empty -> empty++
                is ReportOutcome.Suppressed -> suppressed += "case $case: ${outcome.verdict}"
                is ReportOutcome.Composed -> {
                    composed++
                    val report = outcome.report
                    if (report.headline != null) withHeadline++
                    if (report.pattern != null) withPattern++
                    observations += report.observations.size
                    bySize[report.observations.size]++
                    rhythm.read(report)
                    ReportInvariants.violations(report, facts).forEach { broken += "case $case: $it" }
                }
            }
        }

        println(
            "composition over ${GeneratedWeeks.CASES} generated weeks: " +
                "$composed composed, $empty empty, ${suppressed.size} suppressed",
        )
        println(
            "  headline on $withHeadline, pattern on $withPattern, " +
                "$observations observations, by size ${bySize.toList()}",
        )
        print(rhythm.render())

        assertTrue(
            "the composer built a report its own integrity layer refused, ${suppressed.size} times:\n" +
                suppressed.take(SHOWN).joinToString("\n"),
            suppressed.isEmpty(),
        )
        assertTrue(
            "${broken.size} composition rule violations:\n" + broken.take(SHOWN).joinToString("\n"),
            broken.isEmpty(),
        )
        // A run where nothing ever composed would pass every assertion above and prove
        // nothing at all.
        assertTrue("no generated week produced a report", composed > 0)
    }

    @Test
    fun `composing the same week twice produces the same report`() {
        // Determinism is the engine's own property and is tested there over ten thousand
        // cases. What is tested here is that composition adds nothing that is not: the
        // grouping, the caps and the bench selections for the footer are all functions of
        // the facts and the date, and a map iteration order leaking into any of them would
        // show up as two devices printing two different reports from one log.
        for (case in 0 until REPEAT_CASES) {
            val facts = GeneratedWeeks.facts(case)
            val history = GeneratedWeeks.history(case, CorpusFixture.catalog)
            assertEquals(
                "case $case",
                composer.compose(facts, history, WEEK_START_KEY),
                composer.compose(facts, history, WEEK_START_KEY),
            )
        }
    }

    private companion object {

        /** The repeat test proves a property the determinism suite already covers at scale. */
        const val REPEAT_CASES = 500

        /** Enough failures to see the shape without a wall of them. */
        const val SHOWN = 20

        const val WEEK_START_KEY = "2026-03-08"
    }
}
