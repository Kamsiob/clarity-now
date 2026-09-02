package com.kamsiob.claritynow.domain.report

import com.kamsiob.claritynow.devtools.ClaritySimulator
import com.kamsiob.claritynow.devtools.SimulationPersona
import com.kamsiob.claritynow.devtools.SimulatorLog
import com.kamsiob.claritynow.domain.engine.FactExtractor
import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.query.TrailWindow
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A year of reports for every persona the simulator declares, week by week, against a log
 * that grew as the reports were written into it.
 *
 * ## Why this is not the same test as `ReportPropertyTest`
 *
 * That one generates fact sets directly, which is what makes ten thousand of them cheap and
 * what lets the ranges straddle every threshold in the corpus. It also means every week is
 * independent, and three of the things this phase is responsible for are not properties of
 * one week:
 *
 * - **The fourteen day family cooldown and the ninety day variant exclusion.** They exist
 *   only in a run that writes its own output back into the log, per section 12, and a
 *   generated history is a plausible history rather than the one this report would have
 * - **A `FactSet` layer one actually produced.** A generated fact set can hold a
 *   combination the extractor would never emit, and a hand built one cannot hold the shapes
 *   a year of real use produces
 * - **The `REPORT_GENERATED` payload.** It is written here and read back by
 *   `FiringHistory.from` on the following week, which is the round trip the app depends on
 *   and which nothing else exercises
 *
 * ## The one liberty, and it is the simulator's own
 *
 * The report is written back into the log, exactly as `ClaritySimulator` does and for the
 * reason section 12 gives: a run that dropped its own output would show every family at
 * stage one forever and would repeat lines it had used the week before. The sidehead
 * resolver hands the section key through, because sideheads are `strings.xml` labels and
 * `domain` cannot read resources.
 *
 * Simulated opens are not written. `APP_OPENED` is never user activity, per the phase 3b
 * decision, so it changes no fact this test reads, and skipping it keeps a year of eleven
 * personas inside a unit test's budget. **Whether the app was opened still decides whether
 * anything happens**, through `SimulationPersona.isPresentOn`: the marker is what is
 * skipped here, never the presence it records.
 */
class ReportPersonaTest {

    private val composer = ReportComposer(CorpusFixture.catalog, ClaritySimulator.DEFAULT_ZONE)

    @Test
    fun `every persona's year of reports holds every composition rule`() {
        var weeks = 0
        var composed = 0
        var empty = 0
        var withHeadline = 0
        var withPattern = 0
        val bySize = IntArray(ReportComposer.MAX_OBSERVATIONS + 1)
        val suppressed = mutableListOf<String>()
        val broken = mutableListOf<String>()
        val rhythm = ReportRhythm()
        val dropped = mutableMapOf<String, Int>()

        for (persona in SimulationPersona.ALL) {
            val log = SimulatorLog(
                ClaritySimulator.DEFAULT_ZONE,
                ClaritySimulator.DEFAULT_START_DATE,
                persona.key,
            )
            for (day in 0 until ClaritySimulator.DAYS_IN_YEAR) {
                // A day nobody was there is a day nothing is written, per
                // `SimulationPersona.isPresentOn`, which is also where the areas are created.
                if (persona.isPresentOn(day)) {
                    if (day == persona.installDay) persona.setUp(log)
                    persona.act(log, day)
                }
                if (day < DAYS_PER_WEEK || day % DAYS_PER_WEEK != 0) continue

                weeks++
                val queries = log.queries()
                val facts = FactExtractor(queries).extract(
                    TrailWindow(log.startOfDay(day - DAYS_PER_WEEK), log.startOfDay(day)),
                )
                val history = FiringHistory.from(queries, log.at(day, REPORT_HOUR))
                val outcome = composer.compose(facts, history, log.dateKey(day - DAYS_PER_WEEK))
                when (outcome) {
                    is ReportOutcome.Empty -> empty++
                    is ReportOutcome.Suppressed ->
                        suppressed += "${persona.key} day $day: ${outcome.verdict}"
                    is ReportOutcome.Composed -> {
                        composed++
                        val report = outcome.report
                        if (report.headline != null) withHeadline++
                        if (report.pattern != null) withPattern++
                        bySize[report.observations.size]++
                        rhythm.read(report)
                        report.dropped.forEach { line ->
                            dropped.merge(line.reason.substringBefore(','), 1, Int::plus)
                        }
                        ReportInvariants.violations(report, facts).forEach {
                            broken += "${persona.key} day $day: $it"
                        }
                        // Written back, so next week's cooldowns and exclusions are real.
                        log.add(
                            log.at(day, REPORT_HOUR),
                            report.payload(
                                reportId = "report-${persona.key}-$day",
                                cadenceWeekStartKey = report.weekStartKey,
                                patternSidehead = "pattern",
                            ) { it.key },
                        )
                    }
                }
            }
        }

        println(
            "composition over $weeks persona weeks: $composed composed, $empty empty, " +
                "${suppressed.size} suppressed",
        )
        println(
            "  headline on $withHeadline of $composed, pattern on $withPattern, " +
                "observations by size ${bySize.toList()}",
        )
        // A reading rather than an assertion, and it runs on every build, because the thing
        // it watches is latent rather than present. `datedFallback` is the headline the
        // corpus calls "never absent, used when nothing else qualifies", and it carries the
        // flat fourteen day Report cooldown that `EngineFamilies` gives every Report family,
        // so two consecutive weeks with nothing else qualifying would leave the second one
        // with no headline at all. Across this year it measures zero: something else always
        // qualified. The day it does not, this line says so rather than a screen quietly
        // opening without its largest text.
        println("  reports with no headline at all: ${composed - withHeadline}")

        // 9.2's two rhythm rules, as readings rather than assertions, for the reason
        // `ReportRhythm` states: both are preferences and both are allowed a residue, so the
        // number and the reason for it are what a later reader needs.
        print(rhythm.render())
        println("  lines the composer dropped, by rule: " + dropped.entries.sortedBy { it.key })

        assertTrue(
            "the composer built a report its own integrity layer refused:\n" +
                suppressed.take(SHOWN).joinToString("\n"),
            suppressed.isEmpty(),
        )
        assertTrue(
            "${broken.size} composition rule violations:\n" + broken.take(SHOWN).joinToString("\n"),
            broken.isEmpty(),
        )
        assertTrue("no persona week produced a report", composed > 0)
    }

    private companion object {
        const val DAYS_PER_WEEK = 7
        const val REPORT_HOUR = 8
        const val SHOWN = 20
    }
}
