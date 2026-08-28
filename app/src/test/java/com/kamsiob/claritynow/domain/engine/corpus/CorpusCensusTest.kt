package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.ClarityCatalog
import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The corpus does not get smaller, and an exemption does not get deeper when it does.
 *
 * `CorpusCensus` carries the whole reasoning. The short version is that phase 9's gates
 * excused a bench while it was **no larger** than the size recorded beside its finding, so
 * losing a line made a bench more exempt than it had been, and the day 336 uncommitted
 * lines were destroyed every one of those gates went green over the wreckage.
 *
 * Half of this file exists to show the mechanism failing on purpose, for the reason
 * `CorpusGateTest` gives about itself: a gate with a baseline can be quietly satisfied by
 * its own exemptions and never fire again, and the only evidence that it excuses the past
 * rather than the future is watching it catch something new.
 */
class CorpusCensusTest {

    private val catalog = CorpusFixture.catalog

    @Test
    fun `no bench holds fewer lines than the census recorded`() {
        val shortfalls = CorpusCensus.shortfalls(catalog)
        assertTrue(
            "the corpus has shrunk since the census was taken. Every line named here was " +
                "written by somebody and is not in the file any more:\n" +
                shortfalls.joinToString("\n") { "  $it" },
            shortfalls.isEmpty(),
        )
    }

    /**
     * Growth is the job, so it is a reading rather than a failure.
     *
     * The floor under a line authored since the last census is the census before it, which
     * is the residue of doing it this way and is bounded by how often it is regenerated. A
     * gate that failed the build for adding a line would be turned off inside a day, and a
     * gate that regenerated itself would be a floor that moves whenever the thing it
     * measures does, which is not a floor.
     */
    @Test
    fun `the census prints what has grown since it was taken`() {
        val growth = CorpusCensus.growth(catalog)
        println("corpus census: ${CorpusCensus.BENCHES.size} benches recorded, ${growth.size} have moved")
        growth.take(SHOWN).forEach { println("  $it") }
        if (growth.size > SHOWN) println("  and ${growth.size - SHOWN} more")
        if (growth.isNotEmpty()) {
            println("paste this over CorpusCensus.BENCHES to take the floor up to today:")
            println(CorpusCensus.regenerate(catalog))
        }
    }

    /**
     * A bench that loses a line is a finding, and a bench that gains one is not.
     *
     * The corpus itself is the fixture. One line is cut out of a bench that nobody is
     * growing, which is the exact shape of the accident: the file parses, every other gate
     * is happy, and the only thing that has changed is that a sentence somebody wrote is
     * gone.
     */
    @Test
    fun `a bench that loses a line is caught`() {
        val bench = "PULSE throughput s3"
        val cut = catalogWithout("throughput.s3.10")
        assertEquals(
            "the demonstration line should be the only one removed",
            CorpusBenches.of(catalog).first { it.id == bench }.size - 1,
            CorpusBenches.of(cut).first { it.id == bench }.size,
        )
        assertTrue(
            "a bench one line shorter than the census was not found: " +
                CorpusCensus.shortfalls(cut),
            CorpusCensus.shortfalls(cut).any { it.subject == bench && "holds" in it.detail },
        )
        assertTrue(
            "adding a line is the job and must never be a finding",
            CorpusCensus.shortfalls(catalog).isEmpty(),
        )
    }

    /**
     * An exemption is a statement about a bench of exactly that size.
     *
     * Growing past it lapses it, which is what phase 9 built and what makes this file
     * expire. Falling below it lapses it too, which is what was missing: the recorded size
     * is somebody's reading of a specific bench, and a bench with lines missing from it is
     * not that bench. Both halves are asserted here because only one of them was ever true.
     */
    @Test
    fun `a grandfathered bench loses its exemption in both directions`() {
        val bench = "PULSE throughput s3"
        val recorded = CorpusGateBaseline.LENGTH_BANDS.getValue(bench)
        assertTrue("the bench is exempt at its recorded size", CorpusGateBaseline.bandExemptAt(bench, recorded))
        assertTrue(
            "growing the bench should have lapsed the exemption",
            !CorpusGateBaseline.bandExemptAt(bench, recorded + 1),
        )
        assertTrue(
            "a bench with a line missing is not the bench the exemption was written about",
            !CorpusGateBaseline.bandExemptAt(bench, recorded - 1),
        )
        val register = CorpusGateBaseline.REGISTERS.entries.first()
        assertTrue(
            "the register half holds the same in both directions",
            CorpusGateBaseline.registerExemptAt(register.key, register.value) &&
                !CorpusGateBaseline.registerExemptAt(register.key, register.value + 1) &&
                !CorpusGateBaseline.registerExemptAt(register.key, register.value - 1),
        )
    }

    /** The real corpus with one keyed line cut out of it. */
    private fun catalogWithout(variantKey: String): ClarityCatalog {
        val cut = CorpusFixture.pulseText
            .split('\n')
            .filterNot { it.trimStart().startsWith("$variantKey ") }
            .joinToString("\n")
        assertTrue("the line to cut was not in the corpus, so this test cuts nothing", cut != CorpusFixture.pulseText)
        return ClarityCatalog.build(cut, CorpusFixture.reportText, CorpusFixture.momentumText)
    }

    private companion object {
        const val SHOWN = 12
    }
}
