package com.kamsiob.claritynow.domain.engine.catalog

/**
 * An authored sentence with typed slot markers, exactly as it appears in a corpus file.
 * CLARITY_LOGIC_ENGINE.md 7.1.
 *
 * **Never assembled at runtime from fragments belonging to another family.** A template
 * is authored, reviewed and rejected as a whole. The seams that make generated text
 * recognizable are the joins, so there are no joins: 7.7 allows a hot family to use
 * frames with interchangeable clauses, and requires that the clause bench belong to
 * exactly one family and one register, with no global opener pool anywhere.
 *
 * [slots] is derived here rather than declared beside the line, for the same reason
 * [LengthBands] computes the band: a hand written list of a template's own slots drifts
 * the moment the line is edited, and 213 leads is too many to keep honest by hand.
 */
data class Template(val text: String) {

    /** Every slot marker in [text], in order of first appearance, without duplicates. */
    val slots: Set<SlotKey> = MARKER.findAll(text).map { it.groupValues[1] }.toSet()

    /** The realized word count per 7.5: every marker collapses to one placeholder token. */
    val wordCount: Int = LengthBands.wordCount(text)

    /** Computed at catalog load, never read from a corpus tag. 7.5. */
    val lengthBand: LengthBand = LengthBands.bandFor(text)

    init {
        require(text.isNotBlank()) { "a blank template" }
        val withoutMarkers = MARKER.replace(text, "")
        require(!STRAY_BRACE.containsMatchIn(withoutMarkers)) {
            "template has a brace that is not a slot marker, which would reach a screen: $text"
        }
    }

    override fun toString(): String = text

    companion object {
        /** `{itemTitle}`. Letters only, so a stray `{` or `{ }` is caught rather than parsed. */
        val MARKER = Regex("""\{([A-Za-z][A-Za-z0-9]*)\}""")

        private val STRAY_BRACE = Regex("""[{}]""")
    }
}

/**
 * Length band computation. CLARITY_LOGIC_ENGINE.md 7.5.
 *
 * **The band is computed at catalog load from the realized word count and is never read
 * from a corpus tag.** `CORPUS_2_REPORT.md` carries a handful of `[S]` markers where a
 * short line was written on purpose; they are advisory authoring hints and the computed
 * value always wins. The parser reads them, records them, and never lets one decide a
 * band.
 *
 * The reason is drift. A tag is correct on the day it is written and wrong on the day
 * the line is edited, and nothing in the build would notice. The composer's rhythm rule,
 * that no two consecutive Report leads may share a band, is only as good as the bands,
 * so a stale tag does not produce a wrong sentence: it produces a report that reads
 * subtly monotonous with no failing test anywhere.
 */
object LengthBands {

    /** 7.5. Under 7 words. */
    const val SHORT_MAX = 6

    /** 7.5. 7 to 14 words. */
    const val MEDIUM_MAX = 14

    /** 7.5. 15 to 24 words. The corpus declares no band above this. */
    const val LONG_MAX = 24

    private val MARKER = Regex("""\{[A-Za-z][A-Za-z0-9]*\}""")

    /**
     * Words after replacing every slot marker with a single placeholder token.
     *
     * A marker collapses to one token rather than to its longest plausible rendering,
     * which is deliberate: `{ageDays}` renders as `yesterday` or as `two months`, and a
     * band that changed with the day's data would make the composer's rhythm rule
     * nondeterministic across two devices reading the same log.
     */
    fun wordCount(text: String): Int =
        MARKER.replace(text, "X").trim().split(WHITESPACE).count { it.isNotEmpty() }

    /** The band for [text]. Throws on a line longer than the corpus declares a band for. */
    fun bandFor(text: String): LengthBand {
        val words = wordCount(text)
        require(words > 0) { "a template with no words: $text" }
        require(words <= LONG_MAX) {
            "$words words, and CLARITY_LOGIC_ENGINE.md 7.5 declares no band above $LONG_MAX. " +
                "A line this long is a corpus defect rather than a fourth band: $text"
        }
        return when {
            words <= SHORT_MAX -> LengthBand.SHORT
            words <= MEDIUM_MAX -> LengthBand.MEDIUM
            else -> LengthBand.LONG
        }
    }

    private val WHITESPACE = Regex("""\s+""")
}
