package com.kamsiob.claritynow.domain.engine.catalog

/**
 * Reads a corpus stage header into an escalation stage. CLARITY_LOGIC_ENGINE.md 7.3.
 *
 * ```
 * ## Stage 2, six to thirteen days                              ->  2, 6..13
 * ## Stage 4, thirty days and beyond                            ->  4, 30..MAX
 * ## Stage 3, ninety five percent and above, or four or more consecutive days
 *                                                               ->  3, 95..MAX and 4..MAX
 * ```
 *
 * **Two things this parser refuses to do, and both refusals are the point.**
 *
 * It never encodes a disjunction inside a range. A compound header carries two
 * conditions over two different facts, and a single range that covered both would be a
 * number the engine invented. [StageHeader.conditions] holds both branches and the rule
 * catalog declares one rule per branch, pointing at the same stage.
 *
 * It never infers a number from a qualitative phrase. `clear imbalance toward intake`
 * gets a [StageCondition.Qualitative], not a guessed `5..MAX`. 7.3 gives the corpus the
 * last word on every threshold, and a threshold no author wrote is not the corpus's word.
 *
 * The splitting rule is `, or ` and never a bare ` or `, because `four or more
 * consecutive days` is one branch and splitting it on ` or ` produces two nonsense ones.
 */
object StageHeaderParser {

    /** `## Stage 2, six to thirteen days` or `### Stage 1, eighty to eighty nine percent`. */
    private val HEADER = Regex("""^#{2,3}\s+Stage\s+(\d+)\s*,\s*(.+?)\s*$""")

    /** The only branch separator. See the class note. */
    private const val BRANCH_SEPARATOR = ", or "

    /** Leading qualifiers the corpus puts in front of a magnitude: `gap of`, `net of`, `queue of`. */
    private const val QUALIFIER_JOIN = "of"

    /** True when [line] is a stage header at any heading depth. */
    fun isStageHeader(line: String): Boolean = HEADER.matches(line.trimEnd())

    /**
     * Parses [line]. Throws [CorpusFormatException] rather than returning null, because a
     * stage header the parser cannot read is a corpus that has drifted from the engine,
     * and continuing would build a ladder with a rung missing.
     */
    fun parse(sourceFile: String, sourceLine: Int, line: String): StageHeader {
        val match = HEADER.matchEntire(line.trimEnd())
            ?: throw CorpusFormatException(sourceFile, sourceLine, "not a stage header: $line")
        val index = match.groupValues[1].toInt()
        val body = match.groupValues[2]
        if (index < 1) throw CorpusFormatException(sourceFile, sourceLine, "stage numbering starts at 1: $line")

        val branches = body.split(BRANCH_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        if (branches.isEmpty()) throw CorpusFormatException(sourceFile, sourceLine, "stage header has no condition: $line")

        val conditions = branches.map { branch -> readBranch(branch) }
        return StageHeader(sourceFile, sourceLine, line.trim(), index, conditions)
    }

    /** A single stage family, which declares its shape in prose rather than in a header. */
    fun singleStage(sourceFile: String, sourceLine: Int, declaration: String): StageHeader = StageHeader(
        sourceFile = sourceFile,
        sourceLine = sourceLine,
        text = declaration,
        index = 1,
        conditions = listOf(StageCondition.Qualitative(declaration)),
    )

    /**
     * One branch of a header.
     *
     * A branch may carry a descriptive clause before the magnitude, as in `mild
     * imbalance, gap of two to four`, so the branch is read segment by segment and the
     * last segment that yields a range wins. A branch where no segment yields one is
     * qualitative, and is recorded as written.
     */
    private fun readBranch(branch: String): StageCondition {
        val segments = branch.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
        val range = segments.asReversed().firstNotNullOfOrNull(::readRange)
        return if (range == null) StageCondition.Qualitative(branch) else StageCondition.Numeric(branch, range)
    }

    /**
     * A range from one segment, or null when the segment is not a magnitude.
     *
     * The `of` test is what keeps `three weeks of rising completions` out of the numeric
     * branch. It begins with a number, so a looser parser would read it as `3..3` over
     * the family's escalation fact, which for `throughput` is net flow. `three weeks` is
     * not a net flow, and the resulting stage would fire on a net of exactly three and
     * never on the pattern the header actually describes.
     */
    private fun readRange(segment: String): IntRange? {
        var tokens = segment.lowercase().split(WHITESPACE).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null

        if (!NumberWords.startsNumber(tokens.first())) {
            val join = tokens.indexOf(QUALIFIER_JOIN)
            if (join !in 1..2) return null
            tokens = tokens.drop(join + 1)
            if (tokens.isEmpty() || !NumberWords.startsNumber(tokens.first())) return null
        }

        val low = NumberWords.read(tokens, 0) ?: return null
        val tail = tokens.drop(low.next)

        if (tail.firstOrNull() == "to") {
            val high = NumberWords.read(tail, 1) ?: return null
            return if (low.value <= high.value) low.value..high.value else null
        }
        for (i in 0 until tail.size - 1) {
            if (tail[i] in OPEN_LEAD && tail[i + 1] in OPEN_TAIL) return low.value..Int.MAX_VALUE
        }
        // A bare magnitude, as in `one quiet day` or `three areas`. Kept deliberately
        // narrow: a long tail, or one containing `of`, is prose rather than a unit.
        if (tail.size <= MAX_BARE_TAIL_WORDS && QUALIFIER_JOIN !in tail) return low.value..low.value
        return null
    }

    private val OPEN_LEAD = setOf("or", "and")
    private val OPEN_TAIL = setOf("more", "above", "beyond", "over")
    private const val MAX_BARE_TAIL_WORDS = 3
    private val WHITESPACE = Regex("""\s+""")
}

/**
 * The corpus does not conform. CLARITY_LOGIC_ENGINE.md 11 and the strictness this phase
 * was asked for: a corpus that has drifted from the engine fails loudly rather than
 * quietly losing a family.
 *
 * A dropped family is the worst possible failure mode here, because nothing on screen
 * changes. The engine simply never says one of the things it was written to say, and the
 * only way anyone finds out is by noticing an absence months later.
 */
class CorpusFormatException(
    val sourceFile: String,
    val sourceLine: Int,
    val reason: String,
) : IllegalStateException("$sourceFile:$sourceLine $reason")
