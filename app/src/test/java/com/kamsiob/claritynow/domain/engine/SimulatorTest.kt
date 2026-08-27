package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.devtools.ClaritySimulator
import com.kamsiob.claritynow.devtools.SimulatedSurface
import com.kamsiob.claritynow.devtools.SimulationChecks
import com.kamsiob.claritynow.devtools.SimulationDump
import com.kamsiob.claritynow.devtools.SimulationPersona
import com.kamsiob.claritynow.devtools.SimulationRun
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The simulator, over a full simulated year for every persona.
 * CLARITY_LOGIC_ENGINE.md 12 and `MASTER_BUILD_PROMPT.md` 11.5.
 *
 * ## What this test is for
 *
 * Three things, and they are not the same thing.
 *
 * 1. **It runs.** A full year, eleven personas, with no crash. Section 12 asks for exactly
 *    that and it is the criterion most likely to be quietly untrue: the engine is handed a
 *    fact set built by layer one from a log that grew as it spoke, which is a shape no unit
 *    test of a single layer ever produces
 * 2. **The dump is usable.** Every invocation annotated with the rule, the stage, the
 *    register, the variant key and the facts, because the annotation is what phase 9 judges
 *    corpus batches against and a dump without it is a wall of sentences
 * 3. **The checks report.** The ten checks in section 12 run and print their numbers. Six
 *    are deferred until the corpus phase and say so, with a date. The four that are
 *    enforced fail the build
 *
 * ## Why the year runs once
 *
 * A year is expensive: layer one re-reads a growing log on every simulated open, which is
 * what makes the facts honest and what makes this the slowest test in the suite. So the
 * runs are computed once for the whole class and every test reads them.
 */
class SimulatorTest {

    @Test
    fun `a full simulated year dumps for every persona without a crash`() {
        val dump = SimulationDump.of(runs)
        assertTrue("the dump is empty", dump.length > MINIMUM_DUMP_CHARACTERS)
        for (run in runs) {
            assertTrue("${run.persona.key} produced no invocations", run.invocations.isNotEmpty())
            assertTrue("${run.persona.key} wrote no events", run.eventCount > 0)
            assertEquals(ClaritySimulator.DAYS_IN_YEAR, run.days)
            assertTrue("${run.persona.key} is missing from the dump", "persona: ${run.persona.key}" in dump)
        }
    }

    @Test
    fun `every persona section 12 names is present, including the one that accepts every plan`() {
        val titles = SimulationPersona.ALL.map { it.title }
        assertEquals("section 12 names eleven personas", EXPECTED_PERSONAS, titles.size)
        assertEquals("persona keys are not unique", titles.size, SimulationPersona.ALL.map { it.key }.toSet().size)
        assertEquals(
            "exactly one persona accepts every plan and completes none",
            1,
            SimulationPersona.ALL.count { it.acceptsEveryPlan },
        )
    }

    @Test
    fun `the engine is run day by day for the Pulse and week by week for the Report`() {
        for (run in runs) {
            val pulses = run.of(SimulatedSurface.PULSE)
            assertEquals(
                "${run.persona.key} ran the Pulse on a day it did not open",
                run.openDays,
                pulses.size,
            )
            assertEquals(
                "${run.persona.key} ran the Pulse twice on one day",
                pulses.size,
                pulses.map { it.dateKey }.toSet().size,
            )
            val reportDays = run.of(SimulatedSurface.REPORT_HEADLINE).map { it.day }
            assertTrue(
                "${run.persona.key} generated a report off a week boundary",
                reportDays.all { it % DAYS_PER_WEEK == 0 },
            )
            assertEquals(
                "${run.persona.key} generated two reports for one week",
                reportDays.size,
                reportDays.toSet().size,
            )
        }
    }

    @Test
    fun `Momentum runs on every simulated open`() {
        for (run in runs) {
            assertEquals(
                "${run.persona.key} did not run Momentum on every open",
                run.openDays,
                run.of(SimulatedSurface.MOMENTUM).size,
            )
        }
    }

    /**
     * The format in section 12, checked on the output rather than on the formatter.
     *
     * A spoken invocation carries the family, the stage, the register and the variant on one
     * line, then the rule, the criteria that fired, the facts used, and the sentence itself.
     * A silent one carries the reason, which is never shown to a person and is the only
     * thing that tells a quiet week apart from a rule that can never fire.
     */
    @Test
    fun `the dump annotates every invocation with its provenance`() {
        val invocation = requireNotNull(runs.flatMap { it.invocations }.firstOrNull { it.spoken != null }) {
            "a year of eleven personas produced no sentence at all"
        }
        val line = requireNotNull(invocation.spoken)
        val rendered = SimulationDump.of(invocation)
        assertTrue("no family in the header", line.familyKey in rendered)
        assertTrue("no stage in the header", "stage ${line.stage}" in rendered)
        assertTrue("no register in the header", line.register.name.lowercase() in rendered)
        assertTrue("no variant key in the header", line.variantKey in rendered)
        assertTrue("no rule key", line.ruleKey in rendered)
        assertTrue("no criteria", "fired:" in rendered)
        assertTrue("the sentence itself is missing", line.statement in rendered)

        val silent = runs.flatMap { it.invocations }.first { it.silence != null }
        val quiet = SimulationDump.of(silent)
        assertTrue("a silent invocation does not say why", "SILENT (${silent.silence})" in quiet)
    }

    /** Every fact printed beside a number carries the `FactRef` the validator re-reads. */
    @Test
    fun `every rendered number is printed with the fact it came from`() {
        val numeric = runs.flatMap { it.invocations }
            .mapNotNull { it.spoken }
            .flatMap { it.facts }
            .filter { entry ->
                val value = entry.substringAfter('=').substringBefore(' ')
                value.isNotEmpty() && value.all { it.isDigit() }
            }
        assertTrue("no numeric slot was rendered in a whole year", numeric.isNotEmpty())
        val unattributed = numeric.filterNot { '[' in it }
        assertTrue("numbers rendered with no FactRef: ${unattributed.take(5)}", unattributed.isEmpty())
    }

    /**
     * The four enforced checks. The other six are deferred and are asserted to say so.
     *
     * The whole report is printed either way. Those numbers are what phase 9 grows the
     * corpus toward, and a check that only reported on failure would tell an author nothing
     * about how far there is to go.
     */
    @Test
    fun `the simulator checks run, and the enforced ones pass`() {
        val report = SimulationChecks.run(runs)
        println(report)
        assertTrue(
            "enforced checks failed:\n" + report.fatal.joinToString("\n") { "${it.name}: ${it.failures.take(5)}" },
            report.fatal.isEmpty(),
        )
    }

    /**
     * A deferral is a date and an issue, never a skip.
     *
     * The gate has to be readable by somebody who did not write it, months later, without
     * asking anybody what it was waiting for. A check that quietly did nothing would be
     * indistinguishable from a check that passed.
     */
    @Test
    fun `every deferred check names the date it was deferred and the issue that lifts it`() {
        val report = SimulationChecks.run(runs)
        val deferred = report.checks.mapNotNull { it.deferral }
        assertTrue("nothing is deferred, which contradicts issue 3", deferred.isNotEmpty())
        for (deferral in deferred) {
            assertTrue("a deferral with no date", Regex("""^\d{4}-\d{2}-\d{2}$""").matches(deferral.since))
            assertTrue("a deferral with no lifting condition", deferral.until.isNotBlank())
            assertTrue("a deferral with no reason", deferral.why.isNotBlank())
        }
        assertTrue(
            "every check is deferred, so nothing is being enforced",
            report.checks.any { it.enforced },
        )
    }

    /**
     * The non-compliance test of section 12, asserted on its own.
     *
     * It is inside the check report as well. It is here a second time because section 12
     * attaches a consequence to it that no other check has: if a reader of this persona's
     * dump could tell plans were accepted, the follow-through implementation is **removed
     * rather than tuned**. A test carrying that consequence should be findable by its name.
     *
     * The vocabulary lives in `SimulationChecks` and is looked up by id rather than
     * restated here. A second copy of a list this delicate is a second copy to disagree
     * with the first, and the first one is the one the report prints.
     */
    @Test
    fun `the persona who accepts every plan and completes none is never told about it`() {
        val outcome = SimulationChecks.run(runs).checks.first { it.id == "nonCompliance" }
        assertTrue(
            "the plan-accepting persona was told about a plan: ${outcome.failures.take(5)}",
            outcome.passed,
        )
        assertTrue("the non-compliance check is deferred, and must not be", outcome.enforced)

        // The same claim from the other end. The simulator writes a marker where a rendered
        // plan would go, precisely so that a plan composed anywhere outside the engine would
        // show up in the dump as itself.
        val run = runs.first { it.persona.acceptsEveryPlan }
        assertFalse("a plan line reached the dump", ClaritySimulator.PLAN_LINE_MARKER in SimulationDump.of(run))
    }

    /** Two runs of the same persona produce the same year, down to the character. */
    @Test
    fun `the simulator is deterministic`() {
        val persona = SimulationPersona.ALL.first()
        val simulator = ClaritySimulator(CorpusFixture.catalog, days = DETERMINISM_DAYS)
        assertEquals(
            SimulationDump.of(simulator.run(persona)),
            SimulationDump.of(ClaritySimulator(CorpusFixture.catalog, days = DETERMINISM_DAYS).run(persona)),
        )
    }

    private companion object {

        /**
         * Every persona's year, computed once.
         *
         * `by lazy` on the companion rather than `@BeforeClass`, so the cost is paid on the
         * first test that needs it and nothing is recomputed between tests. A year per
         * persona is the slowest thing in this suite by a wide margin.
         */
        val runs: List<SimulationRun> by lazy { ClaritySimulator(CorpusFixture.catalog).runAll() }

        /** Section 12 names eleven synthetic histories. */
        const val EXPECTED_PERSONAS = 11

        const val DAYS_PER_WEEK = 7

        /** Long enough to reach a report and a second week of exclusions. */
        const val DETERMINISM_DAYS = 21

        /** A year of eleven personas that fit in less than this is a year that did not run. */
        const val MINIMUM_DUMP_CHARACTERS = 10_000
    }
}
