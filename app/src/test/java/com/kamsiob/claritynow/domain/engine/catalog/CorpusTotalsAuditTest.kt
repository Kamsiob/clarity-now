package com.kamsiob.claritynow.domain.engine.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every stated corpus total, recounted against the keyed lines it claims to describe.
 *
 * The failure this exists for is slow and silent. A total is written once, lines are added
 * and retired underneath it for years, and nobody rereads the sentence four hundred lines
 * further down that still states the original figure. Phase 5 found three such figures
 * already stale, and the facts phase found a fourth while writing this, and the only
 * reason any of them surfaced is that a builder happened to count. All four are corrected
 * and this test is what stops the next one, by holding every stated figure against a count
 * taken from the file at the moment the suite runs.
 *
 * Two properties matter more than the arithmetic:
 *
 * 1. **A locator that stops matching is a failure, not a pass.** An audit whose pattern
 *    quietly matches nothing audits nothing, and would be the most likely way for this
 *    test to rot. Every claim declares how many times its locator must match.
 * 2. **Every row of every totals table must be covered by a claim.** Adding a row to a
 *    totals table without adding a claim here fails, so the audit cannot fall behind the
 *    tables it audits.
 *
 * The approximate figures are deliberately not audited, because they are estimates rather
 * than counts and were written as such: the Report's `~1,300` observation surfaces and
 * `~4,500` plan surfaces, its `roughly 6,100` and `roughly 16,700`, and the `roughly
 * 17,200` combined surfaces in `CLARITY_LOGIC_ENGINE.md` 11.1. Every exact figure is
 * audited, including the per family surface products in volume 1 and the banner
 * combination count in volume 3, both of which are arithmetic on real benches.
 */
class CorpusTotalsAuditTest {

    @Test
    fun `every stated corpus total matches the keyed lines beneath it`() {
        val failures = mutableListOf<String>()
        for (claim in CLAIMS) {
            val text = CorpusFixture.read(claim.file)
            val matches = claim.locator.findAll(text).toList()
            if (matches.size != claim.occurrences) {
                failures += "${claim.file}: the audit could not read the stated ${claim.what}. " +
                    "Its locator matched ${matches.size} times where ${claim.occurrences} was " +
                    "expected, so either the figure moved or its wording changed. An unmatched " +
                    "locator audits nothing, which is why this is a failure and not a skip. " +
                    "Locator: ${claim.locator.pattern}"
                continue
            }
            for (match in matches) {
                val stated = match.groupValues[1].replace(",", "").toInt()
                if (stated != claim.counted) {
                    failures += "${claim.file} line ${lineNumberAt(text, match.range.first)}: " +
                        "the stated ${claim.what} is $stated and the file carries ${claim.counted}. " +
                        "The lines are the ground truth. Correct the figure rather than the file, " +
                        "unless lines were genuinely meant to be added"
                }
            }
        }
        assertTrue(
            "${failures.size} stated corpus total(s) disagree with the lines beneath them:\n" +
                failures.joinToString("\n"),
            failures.isEmpty(),
        )
    }

    @Test
    fun `every row of every totals table is covered by a claim`() {
        val failures = mutableListOf<String>()
        for ((file, heading) in TOTALS_TABLES) {
            val text = CorpusFixture.read(file)
            val covered = CLAIMS.filter { it.file == file }
                .flatMap { claim -> claim.locator.findAll(text).map { it.range }.toList() }
            val start = text.indexOf(heading)
            assertTrue("$file no longer contains the heading $heading", start >= 0)
            val bodyStart = start + heading.length + 1
            var offset = bodyStart
            for (line in text.substring(bodyStart).lineSequence()) {
                if (line.startsWith("#")) break
                val lineStart = offset
                val lineEnd = lineStart + line.length
                offset = lineEnd + 1
                if (!line.startsWith("|") || line.none { it.isDigit() }) continue
                if (covered.none { it.first < lineEnd && it.last >= lineStart }) {
                    failures += "$file line ${lineNumberAt(text, lineStart)}, under $heading, is " +
                        "a totals row that no claim in this audit reads: $line"
                }
            }
        }
        assertTrue(
            "a totals table grew a row that nothing recounts:\n" + failures.joinToString("\n"),
            failures.isEmpty(),
        )
    }

    @Test
    fun `the audit counts keyed lines the way the corpus files count them`() {
        // This audit walks the files itself rather than reading the parsed catalog, because
        // section 4 of the Report is not parsed in this phase and its totals still have to be
        // checked. That second walker is only trustworthy while it agrees with the canonical
        // one, so it is held against it here rather than assumed.
        for (volume in CorpusVolume.entries) {
            assertEquals(
                "${volume.fileName} keyed line count, this audit against CorpusFixture",
                CorpusFixture.keyedLineCount(volume.fileName),
                scan(volume.fileName).size,
            )
        }
    }

    @Test
    fun `no corpus key is used twice`() {
        // A duplicated key inflates every count that contains it, so the counts above are only
        // meaningful while the keys are unique. The catalog enforces this for the families it
        // parses; this covers the whole of every file, section 4 included.
        for (volume in CorpusVolume.entries) {
            val duplicates = scan(volume.fileName)
                .groupBy { it.key }
                .filterValues { it.size > 1 }
                .map { (key, lines) -> "$key on lines ${lines.map { it.number }}" }
            assertTrue(
                "${volume.fileName} reuses a key, which no total can survive: $duplicates",
                duplicates.isEmpty(),
            )
        }
    }

    // ---------------------------------------------------------------- the claims

    /**
     * One stated figure, and the count it must equal.
     *
     * [locator] must capture the figure in group 1 and match exactly [occurrences] times in
     * the file. Commas in the stated figure are removed before it is read, so `10,569` and
     * `10569` compare equal.
     */
    private class Claim(
        val file: String,
        val what: String,
        val locator: Regex,
        val counted: Int,
        val occurrences: Int = 1,
    )

    private companion object {

        private val KEYED = Regex("""^[a-z][A-Za-z0-9]*(?:\.[A-Za-z0-9]+)+\s""")
        private const val LOGIC_ENGINE = CorpusFixture.LOGIC_ENGINE
        private val PULSE = CorpusVolume.PULSE.fileName
        private val REPORT = CorpusVolume.REPORT.fileName
        private val MOMENTUM = CorpusVolume.MOMENTUM.fileName

        private val TOTALS_TABLES = listOf(
            PULSE to "# Corpus totals, volume 1",
            REPORT to "## 4.8 Totals",
            REPORT to "# CORPUS TOTALS, VOLUME 2",
            MOMENTUM to "# Totals, volume 3",
        )

        private data class KeyedLine(val number: Int, val key: String)

        /** The keyed lines of [fileName], by the rule each volume's own totals table uses. */
        private fun scan(fileName: String): List<KeyedLine> {
            var inFence = false
            val found = mutableListOf<KeyedLine>()
            CorpusFixture.read(fileName).split('\n').forEachIndexed { index, line ->
                if (line.trimStart().startsWith("```")) {
                    inFence = !inFence
                } else if (inFence && KEYED.containsMatchIn(line)) {
                    found += KeyedLine(index + 1, line.substringBefore(' '))
                }
            }
            return found
        }

        private val pulseLines = scan(PULSE)
        private val reportLines = scan(REPORT)
        private val momentumLines = scan(MOMENTUM)

        private fun List<KeyedLine>.under(prefix: String) = count { it.key.startsWith("$prefix.") }

        private fun List<KeyedLine>.familiesUnder(prefix: String) =
            filter { it.key.startsWith("$prefix.") }
                .map { it.key.split('.').take(2).joinToString(".") }
                .distinct()
                .size

        /** Leads end in `.lNN` and extensions in `.eNN`, per the Report's key prefix table. */
        private fun observationsEndingIn(mark: Char) =
            reportLines.count { it.key.startsWith("ob.") && it.key.substringAfterLast('.').first() == mark }

        /**
         * Statements times questions times response pairs, summed over the stages of one
         * Pulse family. Within a stage a bare number marks a statement, `q` a question and
         * `r` a response pair, which is what volume 1's surface column multiplies.
         */
        private fun pulseSurfaces(prefix: String): Int =
            pulseLines.filter { it.key.startsWith("$prefix.") }
                .groupBy { it.key.split('.')[1] }
                .values
                .sumOf { stage ->
                    val marks = stage.map { it.key.split('.')[2].first() }
                    val questions = marks.count { it == 'q' }
                    val responses = marks.count { it == 'r' }
                    (marks.size - questions - responses) * questions * responses
                }

        /**
         * The eleven Pulse families and the acknowledgment bench, as volume 1's totals table
         * names them. Acknowledgments are one flat bench rather than staged, so their surface
         * count is their line count.
         */
        private val PULSE_TABLE = listOf(
            "Persistence" to "persistence",
            "Concentration" to "concentration",
            "Accumulation" to "accumulation",
            "Throughput" to "throughput",
            "Quiet day" to "quietday",
            "Spread" to "spread",
            "Switching" to "switching",
            "Burst" to "burst",
            "Rebalance" to "rebalance",
            "Queue drain" to "queuedrain",
            "Fresh start" to "freshstart",
            "Acknowledgments" to "ack",
        )

        private val CLAIMS: List<Claim> = buildList {
            addAll(pulseVolumeClaims())
            addAll(reportVolumeClaims())
            addAll(momentumVolumeClaims())
            addAll(logicEngineClaims())
        }

        private fun pulseVolumeClaims(): List<Claim> = buildList {
            for ((label, prefix) in PULSE_TABLE) {
                val lines = pulseLines.under(prefix)
                add(
                    Claim(
                        PULSE, "$label line count",
                        Regex("""\| $label \| ([0-9,]+) \|"""), lines,
                    ),
                )
                add(
                    Claim(
                        PULSE, "$label surface count",
                        Regex("""\| $label \| [0-9,]+ \| ([0-9,]+) \|"""),
                        if (prefix == "ack") lines else pulseSurfaces(prefix),
                    ),
                )
            }
            add(
                Claim(
                    PULSE, "volume 1 total lines",
                    Regex("""\| \*\*Total\*\* \| \*\*([0-9,]+)\*\*"""), pulseLines.size,
                ),
            )
            add(
                Claim(
                    PULSE, "volume 1 total surfaces",
                    Regex("""\| \*\*Total\*\* \| \*\*[0-9,]+\*\* \| \*\*([0-9,]+)\*\* \|"""),
                    PULSE_TABLE.sumOf { (_, p) -> if (p == "ack") pulseLines.under(p) else pulseSurfaces(p) },
                ),
            )
        }

        private fun reportVolumeClaims(): List<Claim> = buildList {
            val headlines = reportLines.under("hd")
            val patterns = reportLines.under("pt")
            val leads = observationsEndingIn('l')
            val extensions = observationsEndingIn('e')
            add(
                Claim(
                    REPORT, "section 1 headline count",
                    Regex("""\*\*Headline totals: ([0-9,]+) lines"""), headlines,
                ),
            )
            add(
                Claim(
                    REPORT, "section 1 headline family count",
                    Regex("""\*\*Headline totals: [0-9,]+ lines across ([0-9]+) families"""),
                    reportLines.familiesUnder("hd"),
                ),
            )
            add(
                Claim(
                    REPORT, "section 2 lead count",
                    Regex("""\*\*Observation totals: ([0-9,]+) leads"""), leads,
                ),
            )
            add(
                Claim(
                    REPORT, "section 2 extension count",
                    Regex("""\*\*Observation totals: [0-9,]+ leads and ([0-9,]+) extensions"""),
                    extensions,
                ),
            )
            add(
                Claim(
                    REPORT, "section 2 observation family count",
                    Regex("""extensions across ([0-9]+) families"""), reportLines.familiesUnder("ob"),
                ),
            )
            add(
                Claim(
                    REPORT, "section 3 pattern count",
                    Regex("""\*\*Pattern totals: ([0-9,]+) lines"""), patterns,
                ),
            )
            add(
                Claim(
                    REPORT, "section 3 pattern family count",
                    Regex("""\*\*Pattern totals: [0-9,]+ lines across ([0-9]+) families"""),
                    reportLines.familiesUnder("pt"),
                ),
            )
            // The guidance banks are stated twice, once in 4.8 and once in the volume table.
            // Both readings are held against the same count.
            add(
                Claim(
                    REPORT, "frame count",
                    Regex("""\| (?:Guidance f|F)rames \| ([0-9,]+) \|"""),
                    reportLines.under("frm"), occurrences = 2,
                ),
            )
            add(
                Claim(
                    REPORT, "cue count",
                    Regex("""\| (?:Guidance c|C)ues \| ([0-9,]+) \|"""),
                    reportLines.under("cue"), occurrences = 2,
                ),
            )
            add(
                Claim(
                    REPORT, "action count",
                    Regex("""\| (?:Guidance a|A)ctions \| ([0-9,]+) \|"""),
                    reportLines.under("act"), occurrences = 2,
                ),
            )
            add(
                Claim(
                    REPORT, "commitment form count",
                    Regex("""\| Commitment forms \| ([0-9,]+) \|"""),
                    reportLines.under("com"), occurrences = 2,
                ),
            )
            add(
                Claim(
                    REPORT, "non-plan closing count",
                    Regex("""\| Non-plan closings \| ([0-9,]+) \|"""),
                    reportLines.under("cls"), occurrences = 2,
                ),
            )
            add(
                Claim(
                    REPORT, "section 4 guidance total",
                    // Anchored on the line break so it reads the guidance total and not the
                    // volume total three hundred lines below, which carries a surfaces column.
                    Regex("""\| \*\*Total\*\* \| \*\*([0-9,]+)\*\* \|\n"""),
                    listOf("frm", "cue", "act", "com", "cls").sumOf { reportLines.under(it) },
                ),
            )
            add(Claim(REPORT, "volume 2 headline count", Regex("""\| Headlines \| ([0-9,]+) \|"""), headlines))
            add(
                Claim(
                    REPORT, "volume 2 headline surfaces",
                    Regex("""\| Headlines \| [0-9,]+ \| ([0-9,]+) \|"""), headlines,
                ),
            )
            add(Claim(REPORT, "volume 2 lead count", Regex("""\| Observation leads \| ([0-9,]+) \|"""), leads))
            add(
                Claim(
                    REPORT, "volume 2 extension count",
                    Regex("""\| Observation extensions \| ([0-9,]+) \|"""), extensions,
                ),
            )
            add(Claim(REPORT, "volume 2 pattern count", Regex("""\| Patterns \| ([0-9,]+) \|"""), patterns))
            add(
                Claim(
                    REPORT, "volume 2 pattern surfaces",
                    Regex("""\| Patterns \| [0-9,]+ \| ([0-9,]+) \|"""), patterns,
                ),
            )
            add(
                Claim(
                    REPORT, "volume 2 non-plan closing surfaces",
                    Regex("""\| Non-plan closings \| [0-9,]+ \| ([0-9,]+) \|"""), reportLines.under("cls"),
                ),
            )
            add(
                Claim(
                    REPORT, "basis and edge state count",
                    Regex("""\| Basis and edge states \| ([0-9,]+) \|"""),
                    reportLines.under("bs") + reportLines.under("ed"),
                ),
            )
            add(
                Claim(
                    REPORT, "basis and edge state surfaces",
                    Regex("""\| Basis and edge states \| [0-9,]+ \| ([0-9,]+) \|"""),
                    reportLines.under("bs") + reportLines.under("ed"),
                ),
            )
            add(
                Claim(
                    REPORT, "volume 2 total lines",
                    Regex("""\| \*\*Total\*\* \| \*\*([0-9,]+)\*\* \| \*\*~[0-9,]+\*\* \|"""),
                    reportLines.size,
                ),
            )
            add(
                Claim(
                    REPORT, "volumes 1 and 2 combined line count",
                    Regex("""\*\*Combined with volume 1: ([0-9,]+) authored lines"""),
                    pulseLines.size + reportLines.size,
                ),
            )
        }

        private fun momentumVolumeClaims(): List<Claim> = buildList {
            val headlines = momentumLines.under("mo")
            val sentences = momentumLines.under("bn")
            val captions = momentumLines.count { it.key.startsWith("bnc.") }
            add(
                Claim(
                    MOMENTUM, "Momentum headline count",
                    Regex("""\*\*Momentum headline totals: ([0-9,]+) lines"""), headlines,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "Momentum headline family count",
                    Regex("""\*\*Momentum headline totals: [0-9,]+ lines across ([0-9]+) families"""),
                    momentumLines.familiesUnder("mo"),
                ),
            )
            add(
                Claim(
                    MOMENTUM, "banner sentence count in prose",
                    Regex("""\*\*Banner totals: ([0-9,]+) sentences"""), sentences,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "banner caption count in prose",
                    Regex("""sentences and ([0-9,]+) captions"""), captions,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "banner combination count in prose",
                    Regex("""captions, ([0-9,]+) combinations"""), sentences * captions,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "banner family count",
                    Regex("""combinations across ([0-9]+) families"""), momentumLines.familiesUnder("bn"),
                ),
            )
            add(
                Claim(
                    MOMENTUM, "table Momentum headline count",
                    Regex("""\| Momentum headlines \| ([0-9,]+) \|"""), headlines,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "table Momentum headline surfaces",
                    Regex("""\| Momentum headlines \| [0-9,]+ \| ([0-9,]+) \|"""), headlines,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "table banner sentence count",
                    Regex("""\| Banner sentences \| ([0-9,]+) \|"""), sentences,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "table banner caption count",
                    Regex("""\| Banner captions \| ([0-9,]+) \|"""), captions,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "table banner combination count",
                    Regex("""\| Banner captions \| [0-9,]+ \| ([0-9,]+) combined \|"""), sentences * captions,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "volume 3 total lines",
                    Regex("""\| \*\*Total\*\* \| \*\*([0-9,]+)\*\*"""), momentumLines.size,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "volume 3 total surfaces",
                    Regex("""\| \*\*Total\*\* \| \*\*[0-9,]+\*\* \| \*\*([0-9,]+)\*\* \|"""),
                    headlines + sentences * captions,
                ),
            )
            add(
                Claim(
                    MOMENTUM, "corpus grand total",
                    Regex("""grand total across all three volumes: ([0-9,]+) authored lines"""),
                    pulseLines.size + reportLines.size + momentumLines.size,
                ),
            )
        }

        /** The six exact figures in `CLARITY_LOGIC_ENGINE.md` 11.1. */
        private fun logicEngineClaims(): List<Claim> = listOf(
            Claim(
                LOGIC_ENGINE, "Pulse line count",
                Regex("""\*\*([0-9,]+) Pulse lines producing"""), pulseLines.size,
            ),
            Claim(
                LOGIC_ENGINE, "Pulse surface count",
                Regex("""Pulse lines producing ([0-9,]+) surfaces"""),
                PULSE_TABLE.sumOf { (_, p) -> if (p == "ack") pulseLines.under(p) else pulseSurfaces(p) },
            ),
            Claim(
                LOGIC_ENGINE, "Report line count",
                Regex("""surfaces, ([0-9,]+) Report and guidance lines"""), reportLines.size,
            ),
            Claim(
                LOGIC_ENGINE, "Momentum line count",
                Regex("""and ([0-9,]+) Momentum and banner lines producing"""), momentumLines.size,
            ),
            Claim(
                LOGIC_ENGINE, "Momentum surface count",
                Regex("""Momentum and banner lines producing ([0-9,]+)\."""),
                momentumLines.under("mo") +
                    momentumLines.under("bn") * momentumLines.count { it.key.startsWith("bnc.") },
            ),
            Claim(
                LOGIC_ENGINE, "combined line count",
                Regex("""Combined, ([0-9,]+) authored lines"""),
                pulseLines.size + reportLines.size + momentumLines.size,
            ),
        )

        private fun lineNumberAt(text: String, index: Int): Int =
            text.substring(0, index).count { it == '\n' } + 1
    }
}
