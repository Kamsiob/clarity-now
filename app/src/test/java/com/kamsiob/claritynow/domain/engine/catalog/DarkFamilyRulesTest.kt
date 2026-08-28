package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The nine families that never qualified once across eleven simulated years, and what the
 * rules pass decided about each of them.
 *
 * **This is a decision lock rather than a coverage test.** None of these families can be
 * made to fire by a unit test, because what is missing is either a fact 3.1 does not
 * declare or a life section 12 does not model. What a test can do is fail when somebody
 * reopens a decision without meaning to: moves a family's purpose, lowers a corpus
 * threshold to make a family speak, or drops the guard that stops a drain family telling
 * somebody they finished three things they deleted.
 *
 * The measurement these nine come from is the slot bindings entry in `DECISIONS.md`,
 * group one: nine families and two stages whose criteria held zero times across 3,148
 * Pulse windows and 451 report windows.
 */
class DarkFamilyRulesTest {

    /**
     * Not one of the nine moved, and the reason is structural rather than a judgment.
     *
     * The diagnosis this pass was asked to test first was that a weekly shape had been
     * authored and then assigned to the Pulse, which reads one day. It does not hold for
     * any of the nine. The three Pulse families date themselves in their own lines,
     * seventeen times between `burst` and `throughput`, and the six Report families are
     * already read over the week or the four weeks they speak about.
     *
     * A purpose is also not a field that can be edited across volumes. A Pulse bench
     * carries statements, questions and response pairs; a Report observation bench carries
     * leads and extensions and an editorial register and no question at all. Moving a
     * family's purpose means re-authoring its bench in the other volume, and
     * [CatalogIntegrity] would fail the build in between, because every family declared
     * here has to be present in the corpus file that purpose reads.
     */
    @Test
    fun `every family that never qualified still declares the purpose it was authored for`() {
        val declared = Purpose.entries
            .flatMap { purpose -> EngineFamilies.keysFor(purpose).map { it to purpose } }
            .groupBy({ it.first }, { it.second })
        for ((family, purposes) in NEVER_QUALIFIED) {
            assertEquals(
                "$family was not moved by the rules pass. A weekly shape on a one day window " +
                    "is what that pass looked for and none of these nine is one",
                purposes,
                declared[family].orEmpty().toSet(),
            )
        }
    }

    /**
     * The corpus owns every threshold on the three Pulse families, so none of them is this
     * file's to retarget.
     *
     * `three to four completions`, `queue of three to four drained` and `net of one to two`
     * are stage headers in `CORPUS_1_PULSE.md`. [StageRangeTest] parses them and asserts
     * the rules match, which is the mechanism that makes the corpus the source. This test
     * says the same thing from the other side, so that a future pass hunting for firings
     * finds a failure here before it finds a way through.
     */
    @Test
    fun `the dark Pulse families still point at stages the corpus states numerically`() {
        val numeric = CorpusFixture.catalog.families
            .filter { it.purpose == Purpose.PULSE && it.key in DARK_PULSE }
            .filter { family -> family.stages.all { it.header.numericConditions.isNotEmpty() } }
            .map { it.key }
            .toSet()
        assertEquals(
            "every rung of these three ladders is a number the corpus states, so a threshold " +
                "here cannot be moved without moving a stage header",
            DARK_PULSE,
            numeric,
        )
    }

    /**
     * Both families whose subject is a drained area require the drain to have been worked
     * through rather than deleted.
     *
     * A queue empties either way and nothing in `RollupFacts.queueDrainedAreaIds` can tell
     * the two apart. `CORPUS_1_PULSE.md` 10 says `{areaName} finished everything it was
     * holding` and `CORPUS_2_REPORT.md` 2.17 says `{areaName} cleared its entire queue this
     * week`, and a realizer may select either from a bench, so the guard belongs on the
     * rule. See [drainedByFinishing].
     */
    @Test
    fun `every rule whose subject is a drained area requires the drain to be completions`() {
        val drainRules = CorpusFixture.catalog.rules.filter { it.family in DRAIN_FAMILIES }
        assertTrue("no drain rules found, so this test would pass on nothing", drainRules.size >= 3)
        val unguarded = drainRules
            .filterNot { rule -> rule.criteria.any { it.id == DRAINED_BY_FINISHING } }
            .map { it.key }
        assertEquals(
            "a queue also empties by deletion, and both drain benches claim somebody finished " +
                "something. Without this the engine tells a person they cleared what they threw away",
            emptyList<String>(),
            unguarded,
        )
    }

    /**
     * `queueDrained` does not restate the starting queue that its own first criterion
     * already carries.
     *
     * `RollupFacts.queueDrainedAreaIds` is a queue of three or more at the window start
     * that is zero at the window end, so a second criterion asserting the starting queue
     * could never separate one fact set from another. It bought a free point of
     * specificity, which `ClarityRule` says is the one number nobody authors. Section 14's
     * discrimination report cannot catch this, because it measures a criterion across the
     * whole corpus rather than conditionally on the rest of its own rule.
     */
    @Test
    fun `the drained observation does not restate the starting queue its rollup already carries`() {
        val rule = CorpusFixture.catalog.rules.single { it.key == "report.observation.queueDrained" }
        val restated = rule.criteria.map { it.id }.filter { it.startsWith("drained.hadAQueue") }
        assertEquals(
            "membership in queueDrainedAreaIds already means the queue was three or more at " +
                "the window start. Restating it is padding, not a requirement",
            emptyList<String>(),
            restated,
        )
        assertTrue(
            "the rule still asks the rollup which areas drained",
            rule.criteria.any { it.id == "drained.area" },
        )
    }

    /**
     * The ninth family, and the only one of the nine already closed.
     *
     * `insufficientData` was a catalog defect rather than a threshold: its rule required
     * fewer than three weeks of snapshots and 6.3 asks for a pattern only at three or more,
     * so the gate that admitted it excluded the only condition it fired on. It is rendered
     * by the composer now and has no rule at all, which is the state this asserts.
     */
    @Test
    fun `the ninth family is closed, rendered directly and holding no rule`() {
        assertTrue(
            "insufficientData is an empty state rather than an observation, and the composer " +
                "renders it. See ReportRules.RENDERED_DIRECTLY",
            "insufficientData" in ReportRules.RENDERED_DIRECTLY,
        )
        assertTrue(
            "a family the composer renders must not also be selected by the engine, or the " +
                "report says there is no pattern twice",
            CorpusFixture.catalog.rules.none { it.family == "insufficientData" },
        )
    }

    private companion object {

        /** `CORPUS_2_REPORT.md` 3.16 aside, these are group one of the slot bindings entry. */
        val NEVER_QUALIFIED: Map<String, Set<Purpose>> = mapOf(
            "throughput" to setOf(Purpose.PULSE),
            "burst" to setOf(Purpose.PULSE),
            "queueDrain" to setOf(Purpose.PULSE),
            "netOutflow" to setOf(Purpose.REPORT_HEADLINE),
            "clearing" to setOf(Purpose.REPORT_HEADLINE),
            "fragmented" to setOf(Purpose.REPORT_HEADLINE),
            "queueDrained" to setOf(Purpose.REPORT_OBSERVATION),
            "weekendShift" to setOf(Purpose.REPORT_PATTERN),
            "insufficientData" to setOf(Purpose.REPORT_PATTERN),
        )

        val DARK_PULSE = setOf("throughput", "burst", "queueDrain")

        val DRAIN_FAMILIES = setOf("queueDrain", "queueDrained")

        const val DRAINED_BY_FINISHING = "queue.drainedByFinishing"
    }
}
