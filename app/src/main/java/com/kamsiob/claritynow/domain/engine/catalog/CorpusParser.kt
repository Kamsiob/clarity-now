package com.kamsiob.claritynow.domain.engine.catalog

import com.kamsiob.claritynow.domain.engine.FamilyKey

/**
 * The three corpus files, and the registers each one is allowed to use.
 * `MASTER_BUILD_PROMPT.md` 11.2, and each volume's own structure note.
 *
 * [permittedRegisters] is a real constraint rather than a convenience. Pulse has no
 * editorial and no neutral agent voice. Momentum has no editorial voice, because a clever
 * line read many times a day becomes tiresome by the third reading. The Report has every
 * register except reflective. A tag outside its volume's set is a line written for the
 * wrong surface, and the parser refuses it rather than rendering it somewhere it was
 * never reviewed for.
 */
enum class CorpusVolume(
    val fileName: String,
    val permittedRegisters: Set<Register>,
) {
    PULSE(
        "CORPUS_1_PULSE.md",
        setOf(Register.PLAIN, Register.OBSERVATIONAL, Register.REFLECTIVE),
    ),
    REPORT(
        "CORPUS_2_REPORT.md",
        setOf(Register.PLAIN, Register.OBSERVATIONAL, Register.EDITORIAL, Register.NEUTRAL_AGENT),
    ),
    MOMENTUM(
        "CORPUS_3_MOMENTUM.md",
        setOf(Register.PLAIN, Register.OBSERVATIONAL, Register.REFLECTIVE, Register.NEUTRAL_AGENT),
    ),
}

/** What one volume yielded. */
data class ParsedCorpus(
    val volume: CorpusVolume,
    val families: List<PhrasingFamily>,
    /** Corpus key prefix to `FamilyKey`, read from the volume's own prefix table where it has one. */
    val prefixes: Map<String, FamilyKey>,
    /** Benches that are not families: acknowledgments, banner captions, the basis line, the edge states. */
    val auxiliary: Map<String, List<CorpusLine>>,
    /** Sections this phase deliberately does not read, recorded so a silent skip is impossible. */
    val skipped: List<SkippedSection>,
) {
    /** Every keyed line this volume produced, families and auxiliary benches together. */
    val lineCount: Int
        get() = families.sumOf { family ->
            family.stages.sumOf {
                it.variants.size + it.extensions.size + it.questions.size + it.responsePairs.size
            }
        } + auxiliary.values.sumOf { it.size }
}

/** A section read past on purpose, with the reason. */
data class SkippedSection(val title: String, val reason: String)

/**
 * Reads the three corpus files into the phrasing catalog. CLARITY_LOGIC_ENGINE.md 7 and
 * 11, and section 4's requirement that the rule catalog carry no strings of its own.
 *
 * **Pure.** The parser takes the file's text as a `String` and never opens a file. The
 * engine has to compile and run on a desktop JVM with no Android in it, so where the
 * bytes come from is the caller's problem: the tests read the committed corpus files from
 * the repository, and the app will read them from an asset through a loader that lives
 * outside this package.
 *
 * **Strict.** Every line inside a fenced block in a section this parser reads must lex,
 * must carry a key that matches the family and stage it sits under, must carry a register
 * tag exactly where its bench declares one, and must belong to a family the engine
 * declares. Anything else throws [CorpusFormatException].
 *
 * That strictness is the point of parsing rather than transcribing. The failure it guards
 * against is not a crash. It is a family quietly not being read, so the engine never says
 * one of the things it was written to say, and nothing on screen ever looks wrong.
 */
object CorpusParser {

    /** Parses [text] as [volume]. */
    fun parse(volume: CorpusVolume, text: String): ParsedCorpus = when (volume) {
        CorpusVolume.PULSE -> PulseWalker(text).walk()
        CorpusVolume.REPORT -> ReportWalker(text).walk()
        CorpusVolume.MOMENTUM -> MomentumWalker(text).walk()
    }
}

/** Fence tracking and the shared line format. One walker per volume, because the three differ. */
internal abstract class CorpusWalker(private val text: String, val volume: CorpusVolume) {

    protected val skipped = mutableListOf<SkippedSection>()
    private var inFence = false

    fun walk(): ParsedCorpus {
        var lineNumber = 0
        for (raw in text.split('\n')) {
            lineNumber++
            val trimmed = raw.trimEnd()
            if (trimmed.trimStart().startsWith("```")) {
                inFence = !inFence
                continue
            }
            if (inFence) {
                if (trimmed.isBlank()) continue
                onFencedLine(lineNumber, trimmed)
            } else {
                onProseLine(lineNumber, trimmed)
            }
        }
        if (inFence) fail(lineNumber, "the file ends inside a code block")
        return finish()
    }

    protected abstract fun onFencedLine(lineNumber: Int, raw: String)
    protected abstract fun onProseLine(lineNumber: Int, raw: String)
    protected abstract fun finish(): ParsedCorpus

    protected fun lex(lineNumber: Int, raw: String): CorpusLine {
        val line = CorpusLexer.lex(volume.fileName, lineNumber, raw)
        val register = line.register
        if (register != null && register !in volume.permittedRegisters) {
            fail(
                lineNumber,
                "register $register is not one of ${volume.fileName}'s registers " +
                    "${volume.permittedRegisters}: ${line.key}",
            )
        }
        return line
    }

    protected fun demand(condition: Boolean, lineNumber: Int, reason: () -> String) {
        if (!condition) fail(lineNumber, reason())
    }

    protected fun fail(lineNumber: Int, reason: String): Nothing =
        throw CorpusFormatException(volume.fileName, lineNumber, reason)
}

/** `nn`, a Pulse statement, a Report headline or a pattern line. */
internal val STATEMENT_TAIL = Regex("""^\d{2,3}$""")

/** `qNN`, a Pulse question. */
internal val QUESTION_TAIL = Regex("""^q\d{2}$""")

/** `rNN`, a Pulse response pair. */
internal val RESPONSE_TAIL = Regex("""^r\d{2}$""")

/** `lNN`, a Report lead. */
internal val LEAD_TAIL = Regex("""^l\d{2}$""")

/** `eNN`, a Report extension. */
internal val EXTENSION_TAIL = Regex("""^e\d{2}$""")

/**
 * `Deep work / Stuck`, or `Recharging / Busy elsewhere / Overwhelmed`.
 *
 * The first option is the positive one and the last is the flagged one, per 6.1.
 * `quietDay` is the one family with three, because three are genuinely distinct there,
 * and 6.2 settles the format at two everywhere else. A pair is never split across
 * families or stages: it was written to be read against itself, and the equal validity
 * test in 11.3 is a test on the pair rather than on either option.
 */
internal fun responsePairOf(line: CorpusLine): ResponsePair {
    val labels = line.text.split(" / ").map { it.trim() }.filter { it.isNotEmpty() }
    if (labels.size !in 2..3) {
        throw CorpusFormatException(
            line.sourceFile,
            line.sourceLine,
            "response pair ${line.key} has ${labels.size} options: ${line.text}",
        )
    }
    return ResponsePair(
        key = line.key,
        options = labels.mapIndexed { index, label ->
            ResponseOption(
                key = "${line.key}.${index + 1}",
                label = label,
                isPositive = index < labels.size - 1,
            )
        },
        sourceFile = line.sourceFile,
        sourceLine = line.sourceLine,
    )
}

/**
 * The range a stage matches on, for the stages that state one.
 *
 * A compound header contributes its numeric branches, and the rule catalog splits it into
 * one rule per branch, so this span is a convenience for reporting rather than the thing
 * a rule tests against. A header with no numeric branch at all keeps the widest range and
 * leans entirely on the criteria its rules carry, and [CatalogIntegrity] asserts those
 * rules do carry them.
 */
internal fun thresholdOf(header: StageHeader): ClosedRange<Int> {
    val numeric = header.numericConditions
    if (numeric.isEmpty()) return 1..Int.MAX_VALUE
    return numeric.minOf { it.range.first }..numeric.maxOf { it.range.last }
}

/**
 * Builds a [Template] from a lexed line, so a template the corpus cannot produce fails with
 * the file and the line rather than with the sentence alone.
 *
 * The two things that reach this path are a line over twenty four words, for which 7.5
 * declares no band, and a brace that is not a slot marker, which would render on a screen.
 * Both are corpus defects and both need an author to find the line.
 */
internal fun templateOf(line: CorpusLine, text: String = line.text): Template =
    try {
        Template(text)
    } catch (invalid: IllegalArgumentException) {
        throw CorpusFormatException(
            line.sourceFile,
            line.sourceLine,
            "${line.key} cannot be realized: ${invalid.message}",
        )
    }

/** Builds a [Variant] from a lexed line. */
internal fun variantOf(
    line: CorpusLine,
    family: FamilyKey,
    purpose: Purpose,
    stage: Int,
    fallbackRegister: Register,
): Variant = Variant(
    key = line.key,
    family = family,
    purpose = purpose,
    stage = stage,
    register = line.register ?: fallbackRegister,
    statement = templateOf(line),
    shortMarker = line.shortMarker,
    sourceFile = line.sourceFile,
    sourceLine = line.sourceLine,
)
