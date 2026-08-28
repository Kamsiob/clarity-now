package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The seven fast gates, over the three committed corpus files.
 * CLARITY_LOGIC_ENGINE.md 7.5, 7.7, 11.1 and 11.3.
 *
 * ## What these replace
 *
 * The owner reads the corpus once, at the end, as one annotated file. Phase 9 writes roughly
 * two thousand lines into it across eight sessions before that happens. Everything a reader
 * would otherwise have had to check batch by batch is checked here instead, on every run, in
 * about a millisecond, and every finding names the line and the file position to fix.
 *
 * The eighth gate, which renders every line against facts a simulated year produced, is in
 * `CorpusRenderTest`, because it needs three minutes. These are meant to be run after every
 * batch of forty and that one after every family.
 *
 * ## Why half of this file is about catching a violation on purpose
 *
 * Every gate here carries a recorded baseline, because the corpus was authored before any of
 * them existed and all but one of them finds something in it today. A gate with a baseline
 * has a failure mode a gate without one does not: it can be silently satisfied by its own
 * exemptions and never fire again. So each gate is also shown a corpus with a fresh
 * violation planted in it and has to find that one, which is the only evidence that the
 * exemptions excuse the past rather than the future.
 */
class CorpusGateTest {

    private val catalog = CorpusFixture.catalog

    @Test
    fun `the gate report prints, and every gate passes`() {
        val report = CorpusGates.run(catalog)
        println(report.render())
        println(CorpusGates.bindableSlotTable(catalog))
        assertTrue(
            "corpus gates failed. Every finding names the line to fix:\n" +
                report.failed.joinToString("\n") { it.render() },
            report.failed.isEmpty(),
        )
    }

    @Test
    fun `a clause moved into a second family is caught`() {
        val planted = CorpusGates.sharedFragments(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "quietday.s1.01  [P]  Yesterday was quiet here.",
                    "quietday.s1.01  [P]  The queue behind {itemTitle} has not moved in {ageDays}.",
                ),
            ),
        )
        assertTrue(
            "a six word run lifted out of persistence and dropped into quietDay was not found",
            planted.findings.any { "quietDay" in it.detail && "persistence" in it.detail },
        )
    }

    @Test
    fun `a construction reaching a third family is caught`() {
        val planted = CorpusGates.overusedConstructions(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "spread.s1.01  [P]  Yesterday touched {areaCount} areas.",
                    "spread.s1.01  [P]  Not one area.",
                ),
            ),
        )
        assertTrue(
            "a `Not X.` sentence in a family that had none was not found: ${planted.findings}",
            planted.findings.any { "notXthenY" in it.subject && "spread" in it.detail },
        )
    }

    @Test
    fun `a banned word in a new line is caught`() {
        val planted = CorpusGates.bannedVocabulary(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "spread.s1.01  [P]  Yesterday touched {areaCount} areas.",
                    "spread.s1.01  [P]  You should look at three areas.",
                ),
            ),
        )
        assertTrue(
            "a line using a banned word was not found: ${planted.findings}",
            planted.findings.any { it.subject.endsWith("spread.s1.01") && "should" in it.detail },
        )
    }

    @Test
    fun `a marker with no fact behind it is caught`() {
        val planted = CorpusGates.slotBindings(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "persistence.s1.05  [O]  Nothing has moved past {itemTitle} yet.",
                    "persistence.s1.05  [O]  Nothing has moved past {itemTitle}, at {pct} of the week.",
                ),
            ),
        )
        assertTrue(
            "a marker with no binding in this family was not found: ${planted.findings}",
            planted.findings.any { it.subject == "persistence.s1.05" && "{pct}" in it.detail },
        )
    }

    /**
     * The gate that finds a number describing a quantity nobody asked for.
     *
     * The plant is the shape the gate was written from. `{areaCount}` is bound to the areas
     * that moved, so a line writing `days` after it would render a count of areas in front
     * of the word days, on a screen, with a `FactRef` behind it that check 3 re-reads
     * happily. Neither the binding gate nor the render gate can see that: the marker has a
     * binding and the line fills and passes layer 5.
     */
    @Test
    fun `a number standing in front of the wrong unit is caught`() {
        val planted = CorpusGates.unitNouns(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "spread.s1.01  [P]  Yesterday touched {areaCount} areas.",
                    "spread.s1.01  [P]  Yesterday touched {areaCount} days.",
                ),
            ),
        )
        assertTrue(
            "a count of areas rendered in front of the word days was not found: ${planted.findings}",
            planted.findings.any { it.subject == "spread.s1.01" && "areasWithEvents" in it.detail },
        )
    }

    /**
     * And the same gate is silent where English counts one thing with another thing's noun.
     *
     * `You sat down for focused time {sessions} times` counts sessions and writes `times`,
     * which is how English counts occurrences of anything. A gate that read every noun after
     * a marker as the unit would report forty three of these on the corpus as it stands, and
     * an author would turn it off rather than argue with it.
     */
    @Test
    fun `a generic countable after a marker is not a claim about the unit`() {
        val outcome = CorpusGates.unitNouns(catalog)
        assertTrue(
            "the corpus counts sessions as `times` and swaps as `times`, and neither is a defect",
            outcome.findings.isEmpty() && outcome.grandfathered.isEmpty(),
        )
    }

    @Test
    fun `a near duplicate of an approved line is caught`() {
        val planted = CorpusGates.nearDuplicates(
            catalogWith(
                pulse = CorpusFixture.pulseText.replace(
                    "persistence.s1.05  [O]  Nothing has moved past {itemTitle} yet.",
                    "persistence.s1.05  [O]  The queue behind {itemTitle} has not moved at all in {ageDays}.",
                ),
            ),
        )
        assertTrue(
            "one line differing from another by a single content word was not found: ${planted.findings}",
            planted.findings.any { "persistence.s1.05" in it.detail && "persistence.s1.07" in it.detail },
        )
    }

    /**
     * The exemption for a bench lapses the moment the bench grows.
     *
     * This is the mechanism the whole baseline rests on, and it is the one thing in it that
     * could go wrong silently: an exemption that held forever would let phase 9 grow a bench
     * from fifteen lines to sixty with every new line in the same band and nothing would say
     * so.
     */
    @Test
    fun `a grandfathered bench loses its exemption as soon as a line is added`() {
        // The demonstration bench has to be one nobody is growing, or this test fails the day
        // the mechanism it demonstrates starts working. It was written against
        // `PULSE persistence s1`, which is the first bench phase 9 takes from fifteen lines to
        // sixty, so it is now read against a bench 11.1 sizes at four to eight and phase 9
        // never touches.
        val bench = "PULSE throughput s3"
        assertEquals(
            "the recorded size is the bench as it stands",
            10,
            CorpusBenches.of(catalog).first { it.id == bench }.size,
        )
        assertTrue("the bench is exempt at its recorded size", CorpusGateBaseline.bandExemptAt(bench, 10))
        assertTrue("the exemption survived a new line", !CorpusGateBaseline.bandExemptAt(bench, 11))
        assertTrue(
            "a bench nobody recorded is not exempt at any size",
            !CorpusGateBaseline.bandExemptAt("PULSE throughput s9", 1),
        )

        val grown = catalogWith(
            pulse = CorpusFixture.pulseText.replace(
                "throughput.s3.10  [R]  A stretch like this is worth noticing.",
                "throughput.s3.10  [R]  A stretch like this is worth noticing.\n" +
                    "throughput.s3.11  [O]  The queues have come down every week for a month " +
                    "while nothing new arrived to replace what left.",
            ),
        )
        assertTrue(
            "growing a grandfathered bench did not bring back its length band finding",
            CorpusGates.lengthBands(grown).findings.any { it.subject == bench },
        )
        // The register half moved for the same reason the band half did. It was written
        // against `REPORT_OBSERVATION intakeVsOutput s1`, whose thin register was a single
        // editorial lead, and the tone pass of `MASTER_BUILD_PROMPT.md` 14b.10 gave that
        // bench a second one along with the neutral agent lines it owed. `firstMilestone`
        // fires once per `FirstEver` flag ever, so 11.1 sizes it in the long tail and no
        // phase grows it.
        assertTrue(
            "growing a bench with a thin register did not bring back its register finding",
            CorpusGates.registerDepth(
                catalogWith(
                    report = CorpusFixture.reportText.replace(
                        "ob.first.l06  [E]  This week contained something that had not happened before.",
                        "ob.first.l06  [E]  This week contained something that had not happened before.\n" +
                            "ob.first.l07  [P]  Your first swap of an active item.",
                    ),
                ),
            ).findings.any { it.subject == "REPORT_OBSERVATION firstMilestone s1" },
        )
    }

    /** The real corpus with one volume replaced, so a planted violation is the only difference. */
    private fun catalogWith(
        pulse: String = CorpusFixture.pulseText,
        report: String = CorpusFixture.reportText,
        momentum: String = CorpusFixture.momentumText,
    ): ClarityCatalog {
        assertTrue(
            "the planted edit did not apply, so this test is checking the untouched corpus",
            pulse != CorpusFixture.pulseText ||
                report != CorpusFixture.reportText ||
                momentum != CorpusFixture.momentumText,
        )
        return ClarityCatalog.build(pulse, report, momentum)
    }
}
