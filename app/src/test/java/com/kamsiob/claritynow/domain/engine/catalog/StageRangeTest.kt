package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Escalation stage ranges are parsed from the corpus stage headers, are contiguous and non
 * overlapping per family, and a compound header becomes two rules pointing at the same
 * stage rather than a disjunctive range. CLARITY_LOGIC_ENGINE.md 7.3 and
 * `MASTER_BUILD_PROMPT.md` 17.
 *
 * The last test is the one that keeps the corpus in charge. It takes every range the
 * parser read out of a header and asserts a rule exists whose criteria are written against
 * it, so a header edited from `six to thirteen` to `six to twelve` fails here rather than
 * quietly leaving a day that no stage describes.
 */
class StageRangeTest {

    @Test
    fun `number words read greedily`() {
        assertEquals(29, NumberWords.read(listOf("twenty", "nine", "days"), 0)?.value)
        assertEquals("two tokens consumed, so the reader continues at `days`", 2, NumberWords.read(listOf("twenty", "nine", "days"), 0)?.next)
        assertEquals(20, NumberWords.read(listOf("twenty", "days"), 0)?.value)
        assertEquals(95, NumberWords.read(listOf("ninety", "five", "percent"), 0)?.value)
        assertEquals(null, NumberWords.read(listOf("queues", "growing"), 0))
    }

    @Test
    fun `the four shapes a header states a magnitude in`() {
        assertEquals(6..13, single("## Stage 2, six to thirteen days"))
        assertEquals(30..Int.MAX_VALUE, single("## Stage 4, thirty days and beyond"))
        assertEquals(4..Int.MAX_VALUE, single("## Stage 3, four or more consecutive quiet days"))
        assertEquals(1..1, single("## Stage 1, one quiet day"))
        assertEquals(2..4, single("### Stage 1, mild imbalance, gap of two to four"))
        assertEquals(3..4, single("## Stage 1, queue of three to four drained"))
    }

    @Test
    fun `a compound header parses to two conditions and never to one wide range`() {
        val header = StageHeaderParser.parse(
            CorpusVolume.PULSE.fileName,
            326,
            "## Stage 3, ninety five percent and above, or four or more consecutive days",
        )
        assertTrue("the header is compound", header.isCompound)
        assertEquals(2, header.conditions.size)
        assertEquals(95..Int.MAX_VALUE, (header.conditions[0] as StageCondition.Numeric).range)
        assertEquals(4..Int.MAX_VALUE, (header.conditions[1] as StageCondition.Numeric).range)
    }

    @Test
    fun `a branch whose condition is a shape rather than a magnitude is not given a number`() {
        val header = StageHeaderParser.parse(
            CorpusVolume.PULSE.fileName,
            462,
            "## Stage 3, gap of eight or more, or queues growing three weeks running",
        )
        assertEquals(2, header.conditions.size)
        assertEquals(8..Int.MAX_VALUE, (header.conditions[0] as StageCondition.Numeric).range)
        assertTrue(
            "`queues growing three weeks running` begins with no magnitude and must not be " +
                "read as three, which for accumulation would be a gap of three",
            header.conditions[1] is StageCondition.Qualitative,
        )
    }

    @Test
    fun `three weeks of rising completions is not read as a net flow of three`() {
        val header = StageHeaderParser.parse(
            CorpusVolume.PULSE.fileName,
            592,
            "## Stage 3, net of six or more, or three weeks of rising completions",
        )
        assertEquals(6..Int.MAX_VALUE, (header.conditions[0] as StageCondition.Numeric).range)
        assertTrue(header.conditions[1] is StageCondition.Qualitative)
    }

    @Test
    fun `every compound header in the corpus has a rule for each branch that has a fact`() {
        val compound = CorpusFixture.catalog.families
            .flatMap { family -> family.stages.map { family to it } }
            .filter { (_, stage) -> stage.header.isCompound }
        assertEquals(
            "three compound headers, all in CORPUS_1_PULSE.md",
            listOf("accumulation", "concentration", "throughput"),
            compound.map { it.first.key }.sorted(),
        )
        for ((family, stage) in compound) {
            val rules = CorpusFixture.catalog.rulesOf(family.purpose, family.key)
                .filter { it.stage == stage.index }
            val gap = RulesAwaitingFacts.GAPS.any { it.family == family.key && it.stage == stage.index }
            val expected = if (gap) 1 else stage.header.conditions.size
            assertEquals(
                "${family.key} stage ${stage.index} has ${stage.header.conditions.size} branches " +
                    "and 7.3 gives each of them its own rule at that stage. The register is empty, " +
                    "so every branch of every compound header now has one",
                expected,
                rules.size,
            )
            assertTrue(
                "every rule for a compound stage points at that one stage",
                rules.all { it.stage == stage.index },
            )
        }
    }

    @Test
    fun `stage ranges are contiguous and non overlapping`() {
        val findings = CatalogIntegrity.stageRangesAreContiguous(CorpusFixture.catalog)
        assertTrue(findings.joinToString("\n"), findings.isEmpty())
    }

    @Test
    fun `the ladders that state every rung numerically are exactly these`() {
        val numeric = CorpusFixture.catalog.families
            .filter { family ->
                family.stages.size > 1 &&
                    family.stages.all { it.header.numericConditions.isNotEmpty() }
            }
            .map { it.key }
            .sorted()
        assertEquals(
            "intakeVsOutput is absent because its stage 2 and stage 3 headers state a direction " +
                "and no magnitude, so contiguity cannot be checked for it. That is a corpus gap " +
                "recorded in the phase 5 report, not a parser limitation",
            listOf(
                "accumulation", "burst", "concentration", "focusInvestment", "neglectedArea",
                "persistence", "queueDrain", "quietDay", "rebalance", "singleFocus", "spread",
                "switching", "throughput",
            ),
            numeric,
        )
    }

    @Test
    fun `every rule points at a stage whose parsed range it was written against`() {
        val expectations = mapOf(
            "pulse.persistence.s1" to (3..5),
            "pulse.persistence.s2" to (6..13),
            "pulse.persistence.s3" to (14..29),
            "pulse.persistence.s4" to (30..Int.MAX_VALUE),
            "pulse.concentration.s1" to (70..84),
            "pulse.concentration.s2" to (85..94),
            "pulse.accumulation.s1" to (2..3),
            "pulse.accumulation.s2" to (4..7),
            "pulse.throughput.s1" to (1..2),
            "pulse.throughput.s2" to (3..5),
            "pulse.quietDay.s1" to (1..1),
            "pulse.quietDay.s2" to (2..3),
            "pulse.quietDay.s3" to (4..Int.MAX_VALUE),
            "pulse.switching.s1" to (1..1),
            "pulse.switching.s2" to (2..Int.MAX_VALUE),
            "pulse.rebalance.s1" to (5..13),
            "pulse.rebalance.s2" to (14..Int.MAX_VALUE),
            "pulse.spread.s1" to (3..3),
            "pulse.spread.s2" to (4..Int.MAX_VALUE),
            "pulse.burst.s1" to (3..4),
            "pulse.burst.s2" to (5..Int.MAX_VALUE),
            "pulse.queueDrain.s1" to (3..4),
            "pulse.queueDrain.s2" to (5..Int.MAX_VALUE),
            "report.observation.singleFocus.s1" to (80..89),
            "report.observation.singleFocus.s2" to (90..Int.MAX_VALUE),
            "report.observation.focusInvestment.s1" to (1..3),
            "report.observation.focusInvestment.s2" to (4..7),
            "report.observation.focusInvestment.s3" to (8..Int.MAX_VALUE),
            "report.observation.neglectedArea.s1" to (7..13),
            "report.observation.neglectedArea.s2" to (14..Int.MAX_VALUE),
        )
        for ((key, expected) in expectations) {
            val rule = CorpusFixture.catalog.rules.single { it.key == key }
            val stage = requireNotNull(CorpusFixture.catalog.stageFor(rule)) { "$key has no stage" }
            val parsed = (stage.header.conditions.first() as StageCondition.Numeric).range
            assertEquals(
                "$key was written against $expected and the corpus header " +
                    "`${stage.header.text}` says $parsed",
                expected,
                parsed,
            )
        }
    }

    /**
     * The second branch of `concentration` stage 3, which the map above cannot cover.
     *
     * That test reads `conditions.first()`, which for a compound header is the first branch
     * and is a percentage. The days branch is the second, and the rule written against it
     * is a different rule pointing at the same stage.
     */
    @Test
    fun `the days branch of concentration stage 3 has its own rule at the range the header states`() {
        val rule = CorpusFixture.catalog.rules.single { it.key == "pulse.concentration.s3.days" }
        val stage = requireNotNull(CorpusFixture.catalog.stageFor(rule))
        assertEquals(
            "the header reads `ninety five percent and above, or four or more consecutive days`",
            4..Int.MAX_VALUE,
            (stage.header.conditions[1] as StageCondition.Numeric).range,
        )
        assertEquals(3, rule.stage)
        assertTrue(
            "the days branch reads the single area run and pairs it to its own area, which is " +
                "what StreakExceptionAudit requires of every rule reading that fact",
            rule.criteria.map { it.id }.containsAll(
                listOf("concentration.run.4plus", "concentration.run.isThisArea"),
            ),
        )
    }

    private fun single(header: String): IntRange {
        val parsed = StageHeaderParser.parse("test", 1, header)
        return (parsed.conditions.single() as StageCondition.Numeric).range
    }
}
