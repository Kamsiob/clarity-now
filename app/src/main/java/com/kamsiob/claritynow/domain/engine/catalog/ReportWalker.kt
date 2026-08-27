package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Reads `CORPUS_2_REPORT.md`. CLARITY_LOGIC_ENGINE.md 6.3, 7.1 and 7.5.
 *
 * The Report is three purposes in one file. Section 1 is the headline, one per report and
 * under eight words. Section 2 is the observations, two to four of them, each a lead with
 * an optional extension. Section 3 is the pattern line, at most one, and only once there
 * are three weeks to look back across.
 *
 * **The key prefix table is parsed, not transcribed.** The corpus says of it, in the file
 * itself, that it is the only place the mapping from a key prefix to a `FamilyKey` is
 * written down. Copying it into Kotlin would create a second place, and a second place is
 * a place to disagree. So the table is read at load and every keyed line is checked
 * against it.
 *
 * **Two sections are skipped, and both skips are recorded rather than silent.** Section 4
 * is the closing line, which is layer 6, which CLARITY_LOGIC_ENGINE.md 2 puts outside
 * layers 1 to 5 and the build order calls the last thing built. Its benches are frames,
 * cues and actions rather than families with stages. Section 7 is the composition rules,
 * which are prose and a matrix with no benches in them at all.
 */
internal class ReportWalker(text: String) : CorpusWalker(text, CorpusVolume.REPORT) {

    private val families = mutableListOf<PhrasingFamily>()
    private val auxiliary = mutableMapOf<String, MutableList<CorpusLine>>()
    private val prefixes = mutableMapOf<String, FamilyKey>()

    private var section = 0
    private var inPrefixTable = false
    private var family: FamilyBuild? = null
    private var stage: StageBuild? = null
    private var bench: ReportBench? = null
    private var auxBench: String? = null
    private var literalBench: String? = null

    private enum class ReportBench { LEADS, EXTENSIONS }

    private class FamilyBuild(val key: FamilyKey, val prefix: String, val purpose: Purpose) {
        val stages = mutableListOf<StageBuild>()
        var staged = false
    }

    private class StageBuild(val header: StageHeader) {
        val leads = mutableListOf<CorpusLine>()
        val extensions = mutableListOf<CorpusLine>()
    }

    override fun onProseLine(lineNumber: Int, raw: String) {
        if (inPrefixTable) {
            val row = PREFIX_ROW.matchEntire(raw.trim())
            if (row != null) {
                val prefix = row.groupValues[1]
                val name = row.groupValues[2].trim()
                demand(prefixes.put(prefix, name) == null, lineNumber) { "prefix $prefix listed twice" }
                return
            }
            if (raw.startsWith("#")) inPrefixTable = false
        }

        SECTION_HEADING.matchEntire(raw)?.let { match ->
            closeFamily(lineNumber)
            section = match.groupValues[1].toInt()
            auxBench = null
            literalBench = null
            if (section in SKIPPED_SECTIONS) {
                skipped += SkippedSection(raw.removePrefix("# ").trim(), SKIPPED_SECTIONS.getValue(section))
            }
            return
        }
        if (raw.startsWith("# ")) {
            closeFamily(lineNumber)
            section = 0
            auxBench = null
            literalBench = null
            val title = raw.removePrefix("# ").trim()
            if (title !in TAIL_HEADINGS) fail(lineNumber, "an unrecognized top level heading: $title")
            skipped += SkippedSection(title, "prose, not a bench")
            return
        }
        if (raw.trim() == PREFIX_TABLE_HEADING) {
            inPrefixTable = true
            return
        }
        if (section in SKIPPED_SECTIONS) return

        if (StageHeaderParser.isStageHeader(raw)) {
            val current = family ?: fail(lineNumber, "a stage header outside any family: $raw")
            closeStage()
            val header = StageHeaderParser.parse(volume.fileName, lineNumber, raw)
            demand(header.index == current.stages.size + 1, lineNumber) {
                "family ${current.key} jumps to stage ${header.index} after ${current.stages.size}"
            }
            current.staged = true
            stage = StageBuild(header)
            bench = null
            return
        }

        FAMILY_HEADING.matchEntire(raw)?.let { match ->
            val name = match.groupValues[3].trim()
            when (section) {
                1, 2, 3 -> startFamily(lineNumber, name)
                5, 6 -> startAuxiliary(lineNumber, name)
                else -> fail(lineNumber, "a family heading in section $section: $raw")
            }
            return
        }

        when (raw.trim()) {
            LEADS_HEADING -> bench = openBench(lineNumber, ReportBench.LEADS)
            EXTENSIONS_HEADING -> bench = openBench(lineNumber, ReportBench.EXTENSIONS)
        }
    }

    private fun startFamily(lineNumber: Int, name: FamilyKey) {
        closeFamily(lineNumber)
        val purpose = PURPOSE_BY_SECTION.getValue(section)
        demand(name in EngineFamilies.keysFor(purpose), lineNumber) {
            "the corpus declares a $purpose family `$name` that CLARITY_LOGIC_ENGINE.md 6.3 does not"
        }
        val wanted = SECTION_PREFIX.getValue(section)
        val prefix = prefixes.entries
            .firstOrNull { it.value == name && it.key.startsWith(wanted) }
            ?.key
            ?: fail(lineNumber, "no `$wanted` prefix is listed for family `$name` in the key prefix table")
        family = FamilyBuild(name, prefix, purpose)
        stage = null
        bench = if (section == 2) null else ReportBench.LEADS
        if (section != 2) {
            stage = StageBuild(
                StageHeaderParser.singleStage(volume.fileName, lineNumber, "$name, one bench, no ladder"),
            )
        }
    }

    /**
     * Sections 5 and 6, the footer and the edge states.
     *
     * These are corpus lines like any other and rule 11.1 of `MASTER_BUILD_PROMPT.md` has
     * no exception for them, but they are not families with rules yet: the Report screen
     * arrives in phase 8 and it is that phase that gives them rules. They are parsed and
     * carried here so nothing has to go looking for them later, and so a line added to
     * one of them is seen.
     *
     * 5.1 is a single fixed line rather than a bench. It is not varied because it is a
     * factual claim about where the report was generated, and varying a factual claim
     * weakens it.
     */
    private fun startAuxiliary(lineNumber: Int, name: String) {
        closeFamily(lineNumber)
        when (name) {
            GENERATED_LINE_HEADING -> {
                literalBench = "footer.generated"
                auxBench = null
            }
            else -> {
                auxBench = AUXILIARY_PREFIX[name]
                    ?: fail(lineNumber, "no auxiliary bench is known for section $section heading `$name`")
                literalBench = null
            }
        }
    }

    private fun openBench(lineNumber: Int, next: ReportBench): ReportBench {
        val current = family ?: fail(lineNumber, "a bench outside any family")
        if (stage == null) {
            stage = StageBuild(
                StageHeaderParser.singleStage(
                    volume.fileName,
                    lineNumber,
                    "${current.key}, one bench, no ladder",
                ),
            )
        }
        return next
    }

    override fun onFencedLine(lineNumber: Int, raw: String) {
        if (section in SKIPPED_SECTIONS) return

        literalBench?.let { key ->
            auxiliary.getOrPut(key) { mutableListOf() }.add(
                CorpusLine(volume.fileName, lineNumber, key, null, raw.trim(), shortMarker = false),
            )
            return
        }

        val line = lex(lineNumber, raw)

        auxBench?.let { aux ->
            demand(line.key.startsWith("$aux."), lineNumber) { "expected an $aux line, found ${line.key}" }
            demand(line.register == null, lineNumber) { "$aux lines carry no register tag" }
            auxiliary.getOrPut(aux) { mutableListOf() }.add(line)
            return
        }

        val current = family ?: fail(lineNumber, "a keyed line outside any family: ${line.key}")
        val build = stage ?: fail(lineNumber, "a keyed line outside any stage: ${line.key}")
        val expected =
            if (current.staged) "${current.prefix}.s${build.header.index}." else "${current.prefix}."
        demand(line.key.startsWith(expected), lineNumber) {
            "line ${line.key} sits under ${current.key}, which expects the prefix `$expected`"
        }

        if (section == 2) {
            demand(line.register != null, lineNumber) {
                "${line.key} carries no register tag, and every lead and extension in section 2 does"
            }
        } else {
            demand(line.register == null, lineNumber) {
                "${line.key} carries a register tag, and section $section is one untagged bench"
            }
        }

        when (bench ?: fail(lineNumber, "a keyed line outside any bench: ${line.key}")) {
            ReportBench.LEADS -> {
                val tail = line.tailSegment
                demand(if (section == 2) LEAD_TAIL.matches(tail) else STATEMENT_TAIL.matches(tail), lineNumber) {
                    "${line.key} is in a lead bench but its key does not end the way section $section leads do"
                }
                build.leads += line
            }
            ReportBench.EXTENSIONS -> {
                demand(EXTENSION_TAIL.matches(line.tailSegment), lineNumber) {
                    "${line.key} is in the extensions bench but its key does not end in `eNN`"
                }
                build.extensions += line
            }
        }
    }

    private fun closeStage() {
        val current = stage ?: return
        family?.stages?.add(current)
        stage = null
    }

    private fun closeFamily(lineNumber: Int) {
        val current = family ?: return
        closeStage()
        if (current.stages.isEmpty()) fail(lineNumber, "family ${current.key} produced no stages")
        families += PhrasingFamily(
            key = current.key,
            purpose = current.purpose,
            keyPrefix = current.prefix,
            cooldownDays = EngineFamilies.reportCooldownDays(current.key),
            stages = current.stages.map { build ->
                EscalationStage(
                    index = build.header.index,
                    threshold = thresholdOf(build.header),
                    header = build.header,
                    variants = build.leads.map {
                        variantOf(it, current.key, current.purpose, build.header.index, Register.PLAIN)
                    },
                    extensions = build.extensions.map {
                        variantOf(it, current.key, current.purpose, build.header.index, Register.PLAIN)
                    },
                )
            },
        )
        family = null
        bench = null
    }

    override fun finish(): ParsedCorpus {
        closeFamily(0)
        for (purpose in PURPOSE_BY_SECTION.values) {
            val declared = EngineFamilies.keysFor(purpose).toSet()
            val found = families.filter { it.purpose == purpose }.map { it.key }.toSet()
            if (declared != found) {
                throw CorpusFormatException(
                    volume.fileName,
                    0,
                    "$purpose declared but absent from the corpus: ${declared - found}; " +
                        "present in the corpus but not declared: ${found - declared}",
                )
            }
        }
        return ParsedCorpus(
            volume = volume,
            families = families.toList(),
            prefixes = prefixes.toMap(),
            auxiliary = auxiliary.mapValues { it.value.toList() },
            skipped = skipped.toList(),
        )
    }

    private companion object {
        val SECTION_HEADING = Regex("""^#\s+SECTION\s+(\d+):\s+.+$""")
        val FAMILY_HEADING = Regex("""^##\s+(\d+)\.(\d+)\s+(.+?)\s*$""")
        val PREFIX_ROW = Regex("""^\|\s*`([a-z][a-z.]*)`\s*\|\s*([A-Za-z]+)\s*\|$""")
        const val PREFIX_TABLE_HEADING = "## Key prefixes"
        const val LEADS_HEADING = "**Leads**"
        const val EXTENSIONS_HEADING = "**Extensions**"
        const val GENERATED_LINE_HEADING = "Generated line"

        val PURPOSE_BY_SECTION = mapOf(
            1 to Purpose.REPORT_HEADLINE,
            2 to Purpose.REPORT_OBSERVATION,
            3 to Purpose.REPORT_PATTERN,
        )
        val SECTION_PREFIX = mapOf(1 to "hd.", 2 to "ob.", 3 to "pt.")
        val AUXILIARY_PREFIX = mapOf(
            "Basis line" to "bs",
            "Nothing to report" to "ed.none",
            "First week" to "ed.first",
        )
        val SKIPPED_SECTIONS = mapOf(
            4 to "layer 6, the closing line. Built in phase 9b, and its benches are frames, " +
                "cues and actions rather than families with stages",
            7 to "composition rules. Prose and the incompatibility matrix, no benches",
        )
        val TAIL_HEADINGS = setOf(
            "Clarity Phrasing Corpus, Volume 2: The Clarity Report",
            "CORPUS TOTALS, VOLUME 2",
            "AUTHORING RULES SPECIFIC TO THE REPORT",
        )
    }
}
