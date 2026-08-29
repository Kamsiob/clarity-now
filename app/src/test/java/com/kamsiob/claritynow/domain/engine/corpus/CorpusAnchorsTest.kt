package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.CorpusFixture
import com.kamsiob.claritynow.domain.engine.catalog.Register
import com.kamsiob.claritynow.domain.engine.realize.SlotBindings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `docs/CORPUS_ANCHORS.md` against the corpus it quotes.
 *
 * The anchors are the only defense against voice drift across eight authoring sessions and
 * two thousand lines, and they work by being read. A misquoted anchor therefore teaches a
 * sentence nobody approved, and a stale one teaches a sentence that no longer exists. Both
 * are silent: the file is prose and nothing else in the build reads it.
 *
 * So every row is checked here. The key must exist, the sentence must match the corpus
 * character for character, the register tag and the length band must be the ones the catalog
 * computes, and the line must be one the engine can actually say.
 */
class CorpusAnchorsTest {

    private val catalog = CorpusFixture.catalog

    private val byKey = catalog.allVariants.associateBy { it.key }

    /** `| `key` | P | SHORT | `the line.` | the note |` */
    private val ROW = Regex("""^\| `([a-z][A-Za-z0-9.]+)` \| ([PORNE]) \| (SHORT|MEDIUM|LONG) \| `(.+?)` \| (.+?) \|$""")

    private val anchors: List<MatchResult> by lazy {
        CorpusFixture.read("docs/CORPUS_ANCHORS.md").split('\n').mapNotNull { ROW.matchEntire(it.trim()) }
    }

    @Test
    fun `every anchor quotes a line that exists, exactly as the corpus writes it`() {
        assertTrue("no anchor rows were parsed out of the file at all", anchors.size > MINIMUM_ANCHORS)
        for (row in anchors) {
            val (key, tag, band, line) = row.destructured
            val variant = byKey[key]
            assertTrue("$key is an anchor and is not a line in any corpus file", variant != null)
            requireNotNull(variant)
            assertEquals("$key is quoted wrongly in the anchors", variant.statement.text, line)
            assertEquals("$key is tagged $tag in the anchors", tagOf(variant.register), tag)
            assertEquals("$key is banded $band in the anchors", variant.lengthBand.name, band)
        }
    }

    /**
     * An anchor has to be a line the engine can say.
     *
     * Anchoring on a line that never reaches a screen would teach the voice of something
     * nobody reads. Eighty six lines in the corpus cannot render, for reasons
     * `CorpusGateBaseline.UNRENDERABLE` records one by one, and none of them is here.
     */
    @Test
    fun `every anchor is a line the engine can actually say`() {
        val unsayable = anchors.map { it.groupValues[1] }.filter {
            SlotBindings.isExcluded(it) || CorpusGateBaseline.isRecordedUnrenderable(it)
        }
        assertTrue("these anchors quote lines the engine can never render: $unsayable", unsayable.isEmpty())
    }

    /** Every note is a sentence somebody could act on, rather than a restatement of the line. */
    @Test
    fun `every anchor carries a note about the voice rather than a repeat of the line`() {
        for (row in anchors) {
            val (key, _, _, line, note) = row.destructured
            assertTrue("$key has a note too short to mean anything: $note", note.length >= MINIMUM_NOTE)
            assertTrue("$key restates its own line instead of saying what it carries", note != line)
        }
    }

    /**
     * Every hot bench is represented, and no warm or long tail family is.
     *
     * The first half is the assignment: ten anchors per hot family. The second half is the
     * instruction that phase 9 grows hot benches and touches nothing else, which an anchor
     * for a warm family would quietly contradict.
     */
    @Test
    fun `the anchors cover every hot family and no other`() {
        val anchored = anchors.mapNotNull { byKey[it.groupValues[1]] }
            .map { it.purpose to it.family }
            .toSet()
        val hot = HotFamilies.ALL.map { it.purpose to it.family }.toSet()
        assertEquals("a hot family with no anchors", emptySet<Pair<*, *>>(), hot - anchored)
        assertEquals("anchors for a family phase 9 must not touch", emptySet<Pair<*, *>>(), anchored - hot)
    }

    /**
     * Ten per family, unless the family cannot supply ten sayable lines.
     *
     * Nineteen of the thirty six hot benches hold fewer than ten lines the engine can render,
     * and three of them hold fewer than seven. Where that is so the whole sayable family is
     * anchored and the file says the count out loud, which is a finding rather than a
     * shortfall.
     */
    @Test
    fun `each hot family carries ten anchors, or every sayable line it has`() {
        val counted = anchors.mapNotNull { byKey[it.groupValues[1]] }
            .groupingBy { it.purpose to it.family }
            .eachCount()
        for (hot in HotFamilies.ALL) {
            val key = hot.purpose to hot.family
            val sayable = catalog.families
                .first { it.purpose == hot.purpose && it.key == hot.family }
                .stages
                .flatMap { it.variants + it.extensions }
                .count { !SlotBindings.isExcluded(it.key) && !CorpusGateBaseline.isRecordedUnrenderable(it.key) }
            assertEquals(
                "${hot.purpose} ${hot.family} has $sayable sayable lines and should carry " +
                    "${minOf(ANCHORS_PER_FAMILY, sayable)} anchors",
                minOf(ANCHORS_PER_FAMILY, sayable),
                counted[key] ?: 0,
            )
        }
    }

    /**
     * The anchors span the registers and the bands their bench can reach.
     *
     * 11.2 asks for anchors, and an anchor set drawn entirely from one register would hold the
     * voice of one third of a bench while the batch being written fills all three. Checked as
     * a floor rather than as a distribution: where a family has more than one register or more
     * than one band among its sayable lines, the anchors must show more than one.
     */
    @Test
    fun `the anchors span the registers and bands their family has`() {
        val byFamily = anchors.mapNotNull { byKey[it.groupValues[1]] }.groupBy { it.purpose to it.family }
        for ((key, chosen) in byFamily) {
            val available = catalog.families
                .first { it.purpose == key.first && it.key == key.second }
                .stages
                .flatMap { it.variants + it.extensions }
                .filter { !SlotBindings.isExcluded(it.key) && !CorpusGateBaseline.isRecordedUnrenderable(it.key) }
            if (available.map { it.register }.toSet().size > 1) {
                assertTrue(
                    "$key draws every anchor from one register while the family has several",
                    chosen.map { it.register }.toSet().size > 1,
                )
            }
            if (available.map { it.lengthBand }.toSet().size > 1) {
                assertTrue(
                    "$key draws every anchor from one length band while the family has several",
                    chosen.map { it.lengthBand }.toSet().size > 1,
                )
            }
        }
    }

    /**
     * The guidance anchors, which are three columns because section 4 has no fifth thing.
     *
     * A frame, a cue, an action, a commitment form and a closing carry no register tag, and
     * `ReportWalker` rejects one on any of them. A length band is computed on the assembled
     * sentence rather than on the three pieces separately, so a band beside a frame would be
     * a number about nothing. What is left is what this checks: the key is one of the parsed
     * banks', and the line is quoted character for character.
     *
     * Section 4 is where the only advice in the application is written, and phase 9b's own
     * pass added a hundred and fifty seven lines to it, so it is exactly the surface the
     * anchors exist for.
     */
    @Test
    fun `every guidance anchor quotes a section 4 line exactly as the corpus writes it`() {
        val byKey = catalog.auxiliary.values.flatten().associateBy { it.key }
        val rows = CorpusFixture.read("docs/CORPUS_ANCHORS.md")
            .split('\n')
            .mapNotNull { GUIDANCE_ROW.matchEntire(it.trim()) }
        assertTrue("no guidance anchor rows were parsed at all", rows.size >= MINIMUM_GUIDANCE)
        for (row in rows) {
            val (key, line, note) = row.destructured
            val corpus = byKey[key]
            assertTrue("$key is a guidance anchor and is not a line in section 4", corpus != null)
            requireNotNull(corpus)
            assertEquals("$key is quoted wrongly in the anchors", corpus.text, line)
            assertTrue("$key has a note too short to mean anything: $note", note.length >= MINIMUM_NOTE)
            assertTrue("$key restates its own line instead of saying what it carries", note != line)
        }
        assertEquals(
            "the guidance anchors do not span the five banks section 4 declares",
            GUIDANCE_BANKS,
            rows.map { it.groupValues[1].substringBefore('.') }.toSet(),
        )
    }

    private fun tagOf(register: Register): String = when (register) {
        Register.PLAIN -> "P"
        Register.OBSERVATIONAL -> "O"
        Register.REFLECTIVE -> "R"
        Register.EDITORIAL -> "E"
        Register.NEUTRAL_AGENT -> "N"
    }

    private companion object {

        /** 11.2 step 3. */
        const val ANCHORS_PER_FAMILY = 10

        /** Below this the file was not parsed, whatever else the assertions say. */
        const val MINIMUM_ANCHORS = 250

        /** A note shorter than this is not saying anything about a voice. */
        const val MINIMUM_NOTE = 25

        /** `| `cls.let.07` | `Rest is not a gap in the record.` | the note |` */
        val GUIDANCE_ROW = Regex("""^\| `([a-z][A-Za-z0-9.]+)` \| `(.+?)` \| (.+?) \|$""")

        /** Below this the guidance table was not parsed, whatever else the assertions say. */
        const val MINIMUM_GUIDANCE = 10

        /** The five banks of `CORPUS_2_REPORT.md` 4, which the anchors have to span. */
        val GUIDANCE_BANKS = setOf("frm", "cue", "act", "com", "cls")
    }
}
