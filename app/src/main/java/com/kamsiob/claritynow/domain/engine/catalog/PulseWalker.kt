package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * Reads `CORPUS_1_PULSE.md`. CLARITY_LOGIC_ENGINE.md 6.1 and 7.1.
 *
 * Eleven families, each an escalation ladder, each rung carrying three benches that are
 * combined at realization: statements, questions and response pairs. A statement combines
 * only with a question and a response pair from its own family and its own stage. There
 * is no global pool, and this walker keeps the three benches separated per stage so there
 * is nowhere for one to appear.
 *
 * The family heading is the family name in capitals, and the key prefix is that name in
 * lowercase with the spaces removed, which is the rule the corpus states for itself. The
 * mapping is resolved against [EngineFamilies.PULSE] rather than derived, so a heading
 * for a family the engine does not declare fails here instead of producing a twelfth
 * family that nothing selects.
 */
internal class PulseWalker(text: String) : CorpusWalker(text, CorpusVolume.PULSE) {

    private val families = mutableListOf<PhrasingFamily>()
    private val auxiliary = mutableMapOf<String, MutableList<CorpusLine>>()

    private var declaration: EngineFamilies.Declaration? = null
    private var stages = mutableListOf<StageBuild>()
    private var stage: StageBuild? = null
    private var bench: PulseBench? = null
    private var singleStage: Pair<Int, String>? = null
    private var auxBench: String? = null

    private enum class PulseBench { STATEMENTS, QUESTIONS, RESPONSES }

    private class StageBuild(val header: StageHeader) {
        val statements = mutableListOf<CorpusLine>()
        val questions = mutableListOf<CorpusLine>()
        val responses = mutableListOf<CorpusLine>()
    }

    override fun onProseLine(lineNumber: Int, raw: String) {
        val familyHeading = FAMILY_HEADING.matchEntire(raw)
        if (familyHeading != null) {
            closeFamily(lineNumber)
            auxBench = null
            val words = familyHeading.groupValues[2].trim()
            val prefix = words.lowercase().replace(" ", "")
            declaration = EngineFamilies.PULSE.firstOrNull { it.prefix == prefix }
                ?: fail(lineNumber, "no Pulse family is declared for the corpus heading `$words`")
            return
        }
        if (raw.startsWith("# ")) {
            closeFamily(lineNumber)
            val title = raw.removePrefix("# ").trim()
            auxBench = if (title == ACKNOWLEDGMENTS_HEADING) ACKNOWLEDGMENTS else null
            if (auxBench == null && title !in TAIL_HEADINGS) {
                fail(lineNumber, "an unrecognized top level heading in ${volume.fileName}: $title")
            }
            if (auxBench == null) skipped += SkippedSection(title, "prose, not a bench")
            return
        }
        if (StageHeaderParser.isStageHeader(raw)) {
            val family = declaration ?: fail(lineNumber, "a stage header outside any family: $raw")
            closeStage()
            val header = StageHeaderParser.parse(volume.fileName, lineNumber, raw)
            demand(header.index == stages.size + 1, lineNumber) {
                "family ${family.key} jumps to stage ${header.index} after ${stages.size}. " +
                    "Stages are numbered from 1 with no gaps, because a gap means a header was lost"
            }
            stage = StageBuild(header)
            bench = null
            return
        }
        if (declaration != null && stage == null && raw.contains(SINGLE_STAGE_DECLARATION)) {
            singleStage = lineNumber to raw.trim()
            return
        }
        when (raw.trim()) {
            "### Statements" -> bench = openBench(lineNumber, PulseBench.STATEMENTS)
            "### Questions" -> bench = openBench(lineNumber, PulseBench.QUESTIONS)
            "### Response pairs" -> bench = openBench(lineNumber, PulseBench.RESPONSES)
        }
    }

    /**
     * Opens a bench, synthesizing stage 1 for a family that declares `Single stage.` in
     * its prose rather than carrying a stage header.
     *
     * `freshStart` is the only one. It is not a ladder: a first item in an empty area has
     * no magnitude to escalate over. Requiring the prose declaration rather than assuming
     * a single stage whenever a header is missing is what keeps a **deleted** stage
     * header from reading as a deliberate choice.
     */
    private fun openBench(lineNumber: Int, next: PulseBench): PulseBench {
        val family = declaration ?: fail(lineNumber, "a bench outside any family")
        if (stage == null) {
            val declared = singleStage ?: fail(
                lineNumber,
                "family ${family.key} opens a bench with no stage header and no " +
                    "`$SINGLE_STAGE_DECLARATION` in its prose",
            )
            stage = StageBuild(StageHeaderParser.singleStage(volume.fileName, declared.first, declared.second))
        }
        return next
    }

    override fun onFencedLine(lineNumber: Int, raw: String) {
        val line = lex(lineNumber, raw)
        val aux = auxBench
        if (aux != null) {
            demand(line.key.startsWith("$aux."), lineNumber) { "expected an $aux line, found ${line.key}" }
            demand(line.register == null, lineNumber) { "$aux lines carry no register tag" }
            auxiliary.getOrPut(aux) { mutableListOf() }.add(line)
            return
        }
        val family = declaration ?: fail(lineNumber, "a keyed line outside any family: ${line.key}")
        val current = stage ?: fail(lineNumber, "a keyed line outside any stage: ${line.key}")
        val expected = "${family.prefix}.s${current.header.index}."
        demand(line.key.startsWith(expected), lineNumber) {
            "line ${line.key} sits under ${family.key} stage ${current.header.index}, " +
                "which expects the prefix `$expected`"
        }
        when (bench ?: fail(lineNumber, "a keyed line outside any bench: ${line.key}")) {
            PulseBench.STATEMENTS -> {
                demand(STATEMENT_TAIL.matches(line.tailSegment), lineNumber) {
                    "${line.key} is in the statements bench but its key does not end in a number"
                }
                demand(line.register != null, lineNumber) { "statement ${line.key} carries no register tag" }
                current.statements += line
            }
            PulseBench.QUESTIONS -> {
                demand(QUESTION_TAIL.matches(line.tailSegment), lineNumber) {
                    "${line.key} is in the questions bench but its key does not end in `qNN`"
                }
                demand(line.register == null, lineNumber) { "question ${line.key} carries a register tag" }
                current.questions += line
            }
            PulseBench.RESPONSES -> {
                demand(RESPONSE_TAIL.matches(line.tailSegment), lineNumber) {
                    "${line.key} is in the response bench but its key does not end in `rNN`"
                }
                demand(line.register == null, lineNumber) { "response pair ${line.key} carries a register tag" }
                current.responses += line
            }
        }
    }

    private fun closeStage() {
        val current = stage ?: return
        stages.add(current)
        stage = null
    }

    private fun closeFamily(lineNumber: Int) {
        val family = declaration ?: return
        closeStage()
        if (stages.isEmpty()) fail(lineNumber, "family ${family.key} produced no stages")
        families += PhrasingFamily(
            key = family.key,
            purpose = Purpose.PULSE,
            keyPrefix = family.prefix,
            cooldownDays = family.cooldownDays,
            stages = stages.map { build -> stageOf(family.key, build) },
        )
        stages = mutableListOf()
        declaration = null
        singleStage = null
        bench = null
    }

    private fun stageOf(family: FamilyKey, build: StageBuild): EscalationStage {
        if (build.questions.isEmpty() || build.responses.isEmpty()) {
            fail(
                build.header.sourceLine,
                "$family stage ${build.header.index} is missing a question or a response " +
                    "bench, and a Pulse that states without asking is not a Pulse",
            )
        }
        return EscalationStage(
            index = build.header.index,
            threshold = thresholdOf(build.header),
            header = build.header,
            variants = build.statements.map {
                variantOf(it, family, Purpose.PULSE, build.header.index, Register.PLAIN)
            },
            questions = build.questions.map {
                QuestionLine(it.key, family, build.header.index, templateOf(it), it.sourceFile, it.sourceLine)
            },
            responsePairs = build.responses.map(::responsePairOf),
        )
    }

    override fun finish(): ParsedCorpus {
        closeFamily(0)
        val declared = EngineFamilies.PULSE.map { it.key }.toSet()
        val found = families.map { it.key }.toSet()
        if (declared != found) {
            throw CorpusFormatException(
                volume.fileName,
                0,
                "declared but absent from the corpus: ${declared - found}; " +
                    "present in the corpus but not declared: ${found - declared}",
            )
        }
        return ParsedCorpus(
            volume = volume,
            families = families.toList(),
            prefixes = families.associate { it.keyPrefix to it.key },
            auxiliary = auxiliary.mapValues { it.value.toList() },
            skipped = skipped.toList(),
        )
    }

    private companion object {
        val FAMILY_HEADING = Regex("""^#\s+(\d+)\.\s+([A-Z][A-Z ]*[A-Z])\s*$""")
        const val ACKNOWLEDGMENTS = "ack"
        const val ACKNOWLEDGMENTS_HEADING = "Acknowledgment lines"
        const val SINGLE_STAGE_DECLARATION = "Single stage."
        val TAIL_HEADINGS = setOf(
            "Clarity Phrasing Corpus, Volume 1: Pulse",
            "Corpus totals, volume 1",
            "Authoring rules for anyone extending this file",
        )
    }
}
