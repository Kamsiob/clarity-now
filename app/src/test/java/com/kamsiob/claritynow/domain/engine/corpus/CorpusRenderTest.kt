package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gate 6, and the two things that keep it honest.
 *
 * This is the slow half of the corpus gate suite: it runs eleven simulated years, which
 * takes about three minutes, so it is meant to be run once per family rather than once per
 * batch of forty. The six fast gates are in `CorpusGateTest` and take a millisecond.
 */
class CorpusRenderTest {

    @Test
    fun `every authored line renders from real facts and passes layer 5`() {
        val outcome = CorpusRenderGate.run(CorpusFixture.catalog, harvest)
        println(outcome.render())
        assertTrue(
            "these lines are in the corpus and the engine can never say them:\n" +
                outcome.findings.joinToString("\n") { "  $it" },
            outcome.passed,
        )
    }

    /**
     * The fill loop in this package against the one in `Realizer`, over every sentence eleven
     * persona years actually produced.
     *
     * `CorpusFill` writes out the slot loop so that a **named** line can be filled, which the
     * realizer offers no way to ask for. This is what stops that copy rotting: every rendered
     * sentence in the run is rebuilt here from the same facts and compared character for
     * character, so a change to `Realizer.fill` that this file did not follow fails here
     * rather than quietly making gate 6 measure the wrong thing.
     */
    @Test
    fun `this package fills every line the realizer filled, to the character`() {
        assertTrue(
            "a year of eleven personas reproduced no sentence at all, so this is checking nothing",
            harvest.spokenReproduced > MINIMUM_REPRODUCED,
        )
        assertTrue(
            "the corpus gate's fill loop has drifted from Realizer.fill:\n" +
                harvest.drift.take(DRIFT_SAMPLE).joinToString("\n") { "  $it" },
            harvest.drift.isEmpty(),
        )
        println(
            "fill agreement: ${harvest.spokenReproduced} sentences reproduced, " +
                "${harvest.spokenSkipped} skipped because they quote a stored answer the " +
                "selector resolved, ${harvest.drift.size} disagreements",
        )
    }

    /**
     * `HotFamilies` against the year it was measured from.
     *
     * The table is a constant because the six fast gates cannot afford to measure it, and a
     * constant nobody watches is a constant that goes stale. A family that crosses forty
     * firings and is not in the table is a bench being held to the long tail's standards
     * while firing weekly, which is the whole thing 11.1 sizes against.
     */
    @Test
    fun `the hot family table still matches what a simulated year measures`() {
        val measured = harvest.firings
            .filterValues { it >= HotFamilies.HOT_FIRINGS_PER_YEAR }
            .map { HotFamilies.Hot(it.key.first, it.key.second, it.value) }
            .sortedWith(compareBy({ it.purpose.ordinal }, { -it.firingsPerYear }, { it.family }))
        val declared = HotFamilies.ALL
            .sortedWith(compareBy({ it.purpose.ordinal }, { -it.firingsPerYear }, { it.family }))
        assertEquals(
            "HotFamilies.ALL is the sixth measurement and this run disagrees with it. Either a " +
                "rule or a persona changed, in which case update the table, or a bench is being " +
                "sized against the wrong tier",
            declared,
            measured,
        )
    }

    /**
     * The render gate, shown a line layer 5 will always veto.
     *
     * Gate 6 carries a recorded baseline of eighty six lines, and a gate with a baseline can
     * be silently satisfied by its own exemptions and never fire again. So it is handed a
     * corpus with a fresh defect planted in it, of the kind only this gate can see: a Report
     * headline of thirteen words, which binds cleanly, renders cleanly, and is refused by
     * check 9 every single time. The fast binding gate cannot see it, because every marker in
     * it has a fact behind it.
     *
     * The harvest is reused rather than recomputed. It is keyed by purpose, family and stage,
     * none of which the planted edit changes, so the modified line meets exactly the moments
     * the real one met.
     */
    @Test
    fun `a line layer 5 will always veto is caught`() {
        val planted = ClarityCatalog.build(
            CorpusFixture.pulseText,
            CorpusFixture.reportText.replace(
                "hd.bal.01  Attention everywhere.",
                "hd.bal.01  Attention was spread out across every one of the areas you keep here.",
            ),
            CorpusFixture.momentumText,
        )
        val outcome = CorpusRenderGate.run(planted, harvest)
        assertTrue(
            "a thirteen word headline was not reported as unrenderable: ${outcome.findings.take(3)}",
            outcome.findings.any { it.subject == "hd.bal.01" && "check 9" in it.detail },
        )
    }

    private companion object {

        /**
         * One harvest for the whole class.
         *
         * Eleven persona years is the most expensive thing in this suite by a wide margin,
         * and all three tests read the same run.
         */
        val harvest: CorpusRenderGate.Harvest by lazy { CorpusRenderGate.harvest(CorpusFixture.catalog) }

        /** Below this, the run did not happen and the agreement check is vacuous. */
        const val MINIMUM_REPRODUCED = 1_000

        const val DRIFT_SAMPLE = 10
    }
}
