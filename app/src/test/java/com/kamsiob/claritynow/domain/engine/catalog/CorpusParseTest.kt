package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * The parser, run against the three committed corpus files.
 *
 * The failure these tests exist for is not a crash. It is a family quietly not being read,
 * so the engine never says one of the things it was written to say and nothing on screen
 * ever looks wrong. Two of the tests below exist only to make that impossible: one asserts
 * that every declared family was found and no undeclared one was, and one asserts that
 * every keyed line in each file reached a bench.
 */
class CorpusParseTest {

    @Test
    fun `every declared family is present in a corpus file and no other`() {
        for (purpose in Purpose.entries) {
            val declared = EngineFamilies.keysFor(purpose).toSet()
            val parsed = CorpusFixture.volumes
                .flatMap { it.families }
                .filter { it.purpose == purpose }
                .map { it.key }
                .toSet()
            assertEquals("$purpose families", declared, parsed)
        }
    }

    @Test
    fun `every keyed line in every corpus file reaches a bench`() {
        for (volume in CorpusFixture.volumes) {
            val counted = CorpusFixture.keyedLineCount(volume.volume.fileName)
            // The one line the parser carries that the file does not key is the fixed
            // generated line in 5.1, which is a sentence rather than a bench.
            val parsed = volume.lineCount - volume.auxiliary["footer.generated"].orEmpty().size
            val skippedLines = counted - parsed
            if (volume.volume == CorpusVolume.REPORT) {
                // Section 4 is layer 6 and is deliberately not read in this phase. Its
                // benches are frames, cues, actions, commitment forms and the non plan
                // closings, and the walker records the skip rather than performing it
                // silently.
                assertTrue(
                    "the report walker should have recorded skipping section 4",
                    volume.skipped.any { it.title.startsWith("SECTION 4") },
                )
                assertEquals("section 4 line count", GUIDANCE_LINES, skippedLines)
            } else {
                assertEquals("${volume.volume.fileName} lines reaching a bench", counted, parsed)
            }
        }
    }

    @Test
    fun `the corpus files hold the number of lines they claim to`() {
        val drifted = KnownCorpusViolations.TOTALS.filter { it.claimed != it.actual }
        for (total in KnownCorpusViolations.TOTALS) {
            assertEquals(
                "${total.file} line count. ${total.note}",
                total.actual,
                CorpusFixture.keyedLineCount(total.file),
            )
        }
        // Empty since phase 9. This used to name CORPUS_3_MOMENTUM.md, whose totals table
        // claimed 112 Momentum headlines against 96 authored; every one of the three files
        // now agrees with its own table, so any name appearing here is a fresh drift.
        assertEquals(
            "no corpus file is known to disagree with its own totals table. A name here means " +
                "a totals table drifted from the lines beneath it",
            emptyList<String>(),
            drifted.map { it.file },
        )
    }

    @Test
    fun `a register tag outside its volume's set is refused`() {
        // The editorial register belongs to the Report. CORPUS_3_MOMENTUM.md says so in
        // prose; this asserts the parser holds the line rather than trusting the note.
        val text = CorpusFixture.momentumText.replace(
            "mo.steady.01  [P]  Active {dayCount} of the last fourteen days.",
            "mo.steady.01  [E]  Active {dayCount} of the last fourteen days.",
        )
        assertTrue("the replacement did not apply", text != CorpusFixture.momentumText)
        val thrown = runCatching { CorpusParser.parse(CorpusVolume.MOMENTUM, text) }.exceptionOrNull()
        assertTrue("expected a CorpusFormatException, got $thrown", thrown is CorpusFormatException)
    }

    @Test
    fun `a line filed under the wrong family is refused`() {
        val text = CorpusFixture.pulseText.replace(
            "persistence.s1.01  [P]",
            "concentration.s1.01  [P]",
        )
        val thrown = runCatching { CorpusParser.parse(CorpusVolume.PULSE, text) }.exceptionOrNull()
        assertTrue("expected a CorpusFormatException, got $thrown", thrown is CorpusFormatException)
    }

    @Test
    fun `a statement with no register tag is refused`() {
        val text = CorpusFixture.pulseText.replace(
            "persistence.s1.02  [P]  {ageDays} on {itemTitle}.",
            "persistence.s1.02  {ageDays} on {itemTitle}.",
        )
        val thrown = runCatching { CorpusParser.parse(CorpusVolume.PULSE, text) }.exceptionOrNull()
        assertTrue("expected a CorpusFormatException, got $thrown", thrown is CorpusFormatException)
    }

    @Test
    fun `a family whose stage header is deleted is refused rather than read as a single stage`() {
        val text = CorpusFixture.pulseText.replace("## Stage 2, six to thirteen days\n", "")
        val thrown = runCatching { CorpusParser.parse(CorpusVolume.PULSE, text) }.exceptionOrNull()
        assertTrue("expected a CorpusFormatException, got $thrown", thrown is CorpusFormatException)
    }

    @Test
    fun `only freshStart declares a single stage in the Pulse volume`() {
        val singleStage = CorpusFixture.pulse.families.filter { it.stages.size == 1 }.map { it.key }
        assertEquals(listOf("freshStart"), singleStage)
    }

    @Test
    fun `the report families that carry an escalation ladder are exactly these four`() {
        val staged = CorpusFixture.report.families
            .filter { it.stages.size > 1 }
            .map { it.key }
            .sorted()
        assertEquals(
            "a family gaining or losing a ladder changes which rules exist, so the set is pinned",
            listOf("focusInvestment", "intakeVsOutput", "neglectedArea", "singleFocus"),
            staged,
        )
    }

    @Test
    fun `every Pulse stage carries all three benches`() {
        for (family in CorpusFixture.pulse.families) {
            for (stage in family.stages) {
                assertTrue("${family.key} stage ${stage.index} statements", stage.variants.isNotEmpty())
                assertTrue("${family.key} stage ${stage.index} questions", stage.questions.isNotEmpty())
                assertTrue("${family.key} stage ${stage.index} responses", stage.responsePairs.isNotEmpty())
            }
        }
    }

    @Test
    fun `only quietDay carries three response options`() {
        val threeOption = CorpusFixture.pulse.families
            .filter { family -> family.stages.any { stage -> stage.responsePairs.any { it.options.size == 3 } } }
            .map { it.key }
        assertEquals(
            "CLARITY_LOGIC_ENGINE.md 6.2 settles the format at two options everywhere but quietDay",
            listOf("quietDay"),
            threeOption,
        )
    }

    @Test
    fun `the first response option is positive and the last is not`() {
        for (family in CorpusFixture.pulse.families) {
            for (stage in family.stages) {
                for (pair in stage.responsePairs) {
                    assertTrue("${pair.key} first option", pair.options.first().isPositive)
                    assertTrue("${pair.key} last option", !pair.options.last().isPositive)
                }
            }
        }
    }

    @Test
    fun `retired keys leave a gap and the parser does not require consecutive numbering`() {
        val pattern = CorpusFixture.report.families.single {
            it.purpose == Purpose.REPORT_PATTERN && it.key == "areaGoneQuiet"
        }
        val keys = pattern.stages.single().variants.map { it.key }
        assertTrue("pt.gone.08 should be present", "pt.gone.08" in keys)
        assertTrue("pt.gone.10 should be present", "pt.gone.10" in keys)
        assertTrue(
            "pt.gone.09 is retired, and a retired key keeps its number forever so firing " +
                "history stays coherent",
            "pt.gone.09" !in keys,
        )
    }

    @Test
    fun `the key prefix table in the report corpus covers every family`() {
        val families = CorpusFixture.report.families.map { it.key }.toSet()
        val mapped = CorpusFixture.report.prefixes.values.toSet()
        val missing = families - mapped
        if (missing.isNotEmpty()) fail("families with no key prefix listed: $missing")
    }

    @Test
    fun `the auxiliary benches are read and not lost`() {
        val auxiliary = CorpusFixture.volumes.fold(emptyMap<String, List<CorpusLine>>()) { all, volume ->
            all + volume.auxiliary
        }
        assertEquals("acknowledgment lines", 12, auxiliary.getValue("ack").size)
        assertEquals("banner captions", 10, auxiliary.getValue("bnc").size)
        assertEquals("basis lines", 6, auxiliary.getValue("bs").size)
        assertEquals("nothing to report", 6, auxiliary.getValue("ed.none").size)
        assertEquals("first week", 6, auxiliary.getValue("ed.first").size)
        assertEquals("the generated line", 1, auxiliary.getValue("footer.generated").size)
        assertEquals("Generated on your device", auxiliary.getValue("footer.generated").single().text)
    }

    private companion object {
        /**
         * Section 4 of `CORPUS_2_REPORT.md`: seven frames, twenty one cues, fifty four
         * actions, three commitment forms and twenty four non plan closings, plus the two
         * accept and decline labels, which carry no key and are counted separately.
         */
        const val GUIDANCE_LINES = 7 + 21 + 54 + 3 + 24
    }
}
