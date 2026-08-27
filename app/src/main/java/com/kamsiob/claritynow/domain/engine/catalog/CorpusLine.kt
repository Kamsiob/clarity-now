package com.kamsiob.claritynow.domain.engine.catalog

/**
 * One keyed line, lexed out of a fenced block in a corpus file, before anything decides
 * what it means.
 *
 * The three volumes have three different structures and share one line format, so the
 * lexer is shared and the walkers are not. Pretending one grammar covered all three
 * would mean guessing, and a parser that guesses about a corpus is how a family goes
 * missing without anything failing.
 */
data class CorpusLine(
    val sourceFile: String,
    val sourceLine: Int,
    val key: String,
    val register: Register?,
    val text: String,
    val shortMarker: Boolean,
) {
    /** The last dot separated segment, which is what says whether this is a lead, a question or an answer set. */
    val tailSegment: String get() = key.substringAfterLast('.')
}

/** Lexing, shared by all three volume walkers. */
internal object CorpusLexer {

    /**
     * `key`, two or more spaces, an optional `[R]` register tag, two or more spaces, the
     * line.
     *
     * The two space separator is a real format rule rather than tidiness, and enforcing
     * it is what keeps a corpus line apart from prose that happens to start with a word
     * and a dot.
     */
    private val KEYED = Regex("""^([a-z][A-Za-z0-9]*(?:\.[A-Za-z0-9]+)+) {2,}(?:\[([PORNE])] {2,})?(\S.*)$""")

    /** The advisory short marker from 7.5. Read, recorded, and never allowed to decide a band. */
    private const val SHORT_MARKER = "[S]"

    /** Lexes one line, or throws. */
    fun lex(sourceFile: String, sourceLine: Int, raw: String): CorpusLine {
        val match = KEYED.matchEntire(raw.trimEnd())
            ?: throw CorpusFormatException(
                sourceFile,
                sourceLine,
                "a line inside a corpus code block that is not `key  [R]  text`: $raw",
            )
        var text = match.groupValues[3].trimEnd()
        var short = false
        if (text.endsWith(SHORT_MARKER)) {
            short = true
            text = text.dropLast(SHORT_MARKER.length).trimEnd()
        }
        if (text.isEmpty()) {
            throw CorpusFormatException(sourceFile, sourceLine, "keyed line with no text: $raw")
        }
        val tag = match.groupValues[2]
        return CorpusLine(
            sourceFile = sourceFile,
            sourceLine = sourceLine,
            key = match.groupValues[1],
            register = if (tag.isEmpty()) null else registerFor(sourceFile, sourceLine, tag),
            text = text,
            shortMarker = short,
        )
    }

    /**
     * `[P]` `[O]` `[R]` `[E]` `[N]`, and nothing else.
     *
     * An unmapped tag throws rather than defaulting. A tag nobody declared, silently read
     * as plain, is a register decision made by a typo.
     */
    private fun registerFor(sourceFile: String, sourceLine: Int, tag: String): Register = when (tag) {
        "P" -> Register.PLAIN
        "O" -> Register.OBSERVATIONAL
        "R" -> Register.REFLECTIVE
        "E" -> Register.EDITORIAL
        "N" -> Register.NEUTRAL_AGENT
        else -> throw CorpusFormatException(sourceFile, sourceLine, "unknown register tag [$tag]")
    }
}
