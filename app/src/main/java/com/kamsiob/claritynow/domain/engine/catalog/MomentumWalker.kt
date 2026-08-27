package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Reads `CORPUS_3_MOMENTUM.md`. CLARITY_LOGIC_ENGINE.md 6.5.
 *
 * Two purposes, thirteen families, one bench each. **No escalation stages**, and the file
 * says why: these surfaces state the current shape and have nothing to escalate about.
 * The Momentum headline is read many times a day and the Areas banner more often than
 * that, so every family here is a single rung.
 *
 * The family heading carries its own key prefix, as in `## mo.steady, steadyStretch`, and
 * the file repeats the whole mapping in a table at the end. Both are read and the two are
 * required to agree, which is worth the few lines it costs: a heading and a table that
 * disagree is exactly the sort of drift nobody notices, and here it would silently file a
 * family's lines under the wrong name.
 */
internal class MomentumWalker(text: String) : CorpusWalker(text, CorpusVolume.MOMENTUM) {

    private val families = mutableListOf<PhrasingFamily>()
    private val auxiliary = mutableMapOf<String, MutableList<CorpusLine>>()
    private val prefixes = mutableMapOf<String, FamilyKey>()
    private val tablePrefixes = mutableMapOf<String, FamilyKey>()

    private var purpose: Purpose? = null
    private var inPrefixTable = false
    private var family: FamilyBuild? = null
    private var auxBench: String? = null

    private class FamilyBuild(val key: FamilyKey, val prefix: String, val purpose: Purpose, val line: Int) {
        val lines = mutableListOf<CorpusLine>()
    }

    override fun onProseLine(lineNumber: Int, raw: String) {
        if (inPrefixTable) {
            val row = PREFIX_ROW.matchEntire(raw.trim())
            if (row != null) {
                val prefix = row.groupValues[1]
                val declaredPurpose = row.groupValues[2].trim()
                val name = row.groupValues[3].trim()
                if (FAMILY_NAME.matches(name)) {
                    demand(declaredPurpose in PURPOSE_NAMES, lineNumber) {
                        "the key prefix table names a purpose the engine does not declare: $declaredPurpose"
                    }
                    demand(tablePrefixes.put(prefix, name) == null, lineNumber) { "prefix $prefix listed twice" }
                }
                return
            }
            if (raw.startsWith("#")) inPrefixTable = false
        }

        PURPOSE_HEADING.matchEntire(raw)?.let { match ->
            closeFamily(lineNumber)
            auxBench = null
            val name = match.groupValues[1].trim()
            purpose = PURPOSE_BY_NAME[name]
                ?: fail(lineNumber, "the corpus declares a purpose the engine does not: $name")
            return
        }
        if (raw.startsWith("# ")) {
            closeFamily(lineNumber)
            purpose = null
            auxBench = null
            val title = raw.removePrefix("# ").trim()
            if (title == PREFIX_TABLE_HEADING) {
                inPrefixTable = true
                return
            }
            if (title !in TAIL_HEADINGS) fail(lineNumber, "an unrecognized top level heading: $title")
            skipped += SkippedSection(title, "prose, not a bench")
            return
        }
        if (raw.trim() == CAPTIONS_HEADING) {
            closeFamily(lineNumber)
            auxBench = CAPTIONS
            return
        }
        FAMILY_HEADING.matchEntire(raw)?.let { match ->
            closeFamily(lineNumber)
            val active = purpose ?: fail(lineNumber, "a family heading outside any purpose: $raw")
            val prefix = match.groupValues[1]
            val name = match.groupValues[2].trim()
            demand(name in EngineFamilies.keysFor(active), lineNumber) {
                "the corpus declares a $active family `$name` that CLARITY_LOGIC_ENGINE.md 6.5 does not"
            }
            demand(prefixes.put(prefix, name) == null, lineNumber) { "prefix $prefix used by two families" }
            family = FamilyBuild(name, prefix, active, lineNumber)
        }
    }

    override fun onFencedLine(lineNumber: Int, raw: String) {
        val line = lex(lineNumber, raw)
        auxBench?.let { aux ->
            demand(line.key.startsWith("$aux."), lineNumber) { "expected a $aux line, found ${line.key}" }
            demand(line.register == null, lineNumber) {
                "caption ${line.key} carries a register tag. Captions are arithmetic and carry no tone"
            }
            auxiliary.getOrPut(aux) { mutableListOf() }.add(line)
            return
        }
        val current = family ?: fail(lineNumber, "a keyed line outside any family: ${line.key}")
        demand(line.key.startsWith("${current.prefix}."), lineNumber) {
            "line ${line.key} sits under ${current.key}, which expects the prefix `${current.prefix}.`"
        }
        demand(STATEMENT_TAIL.matches(line.tailSegment), lineNumber) {
            "${line.key} does not end in a number"
        }
        demand(line.register != null, lineNumber) { "${line.key} carries no register tag" }
        current.lines += line
    }

    private fun closeFamily(lineNumber: Int) {
        val current = family ?: return
        if (current.lines.isEmpty()) fail(lineNumber, "family ${current.key} has no lines")
        families += PhrasingFamily(
            key = current.key,
            purpose = current.purpose,
            keyPrefix = current.prefix,
            cooldownDays = EngineFamilies.NO_COOLDOWN,
            stages = listOf(
                EscalationStage(
                    index = 1,
                    threshold = 1..Int.MAX_VALUE,
                    header = StageHeaderParser.singleStage(
                        volume.fileName,
                        current.line,
                        "${current.key}, one bench, no ladder",
                    ),
                    variants = current.lines.map {
                        variantOf(it, current.key, current.purpose, 1, Register.PLAIN)
                    },
                ),
            ),
        )
        family = null
    }

    override fun finish(): ParsedCorpus {
        closeFamily(0)
        for (active in listOf(Purpose.MOMENTUM_HEADLINE, Purpose.AREAS_BANNER)) {
            val declared = EngineFamilies.keysFor(active).toSet()
            val found = families.filter { it.purpose == active }.map { it.key }.toSet()
            if (declared != found) {
                throw CorpusFormatException(
                    volume.fileName,
                    0,
                    "$active declared but absent from the corpus: ${declared - found}; " +
                        "present in the corpus but not declared: ${found - declared}",
                )
            }
        }
        if (prefixes != tablePrefixes) {
            throw CorpusFormatException(
                volume.fileName,
                0,
                "the family headings and the key prefix table disagree. Headings only: " +
                    "${prefixes - tablePrefixes.keys}; table only: ${tablePrefixes - prefixes.keys}",
            )
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
        val PURPOSE_HEADING = Regex("""^#\s+PURPOSE:\s+([A-Z_]+)\s*$""")
        val FAMILY_HEADING = Regex("""^##\s+((?:mo|bn)\.[a-z]+)\s*,\s*([A-Za-z]+)\s*$""")
        val PREFIX_ROW = Regex("""^\|\s*`([a-z][a-z.]*)`\s*\|\s*([A-Z_]+)\s*\|\s*([^|]+?)\s*\|$""")
        val FAMILY_NAME = Regex("""^[A-Za-z]+$""")
        const val PREFIX_TABLE_HEADING = "Key prefixes"
        const val CAPTIONS_HEADING = "## Banner captions"
        const val CAPTIONS = "bnc"

        val PURPOSE_BY_NAME = mapOf(
            "MOMENTUM_HEADLINE" to Purpose.MOMENTUM_HEADLINE,
            "AREAS_BANNER" to Purpose.AREAS_BANNER,
        )
        val PURPOSE_NAMES = PURPOSE_BY_NAME.keys
        val TAIL_HEADINGS = setOf(
            "Clarity Phrasing Corpus, Volume 3: Momentum and the Areas Banner",
            "Totals, volume 3",
            "Authoring rules for this volume",
        )
    }
}
