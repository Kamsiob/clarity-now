package com.kamsiob.claritynow.domain.engine.corpus

import com.kamsiob.claritynow.domain.engine.catalog.Template

/**
 * How a corpus line is turned into something a machine can compare.
 *
 * Every gate in this package that reads English reads it through here, so there is one
 * normalization and one function word list rather than one per check. Two gates that
 * normalized differently would disagree about whether two lines are the same line, and the
 * one that was wrong would be the one nobody ran.
 *
 * ## The three transformations, and why each is the way it is
 *
 * **Markers collapse to `{}` rather than to a plausible rendering.** `{ageDays}` renders as
 * `yesterday` or as `two months`, so a comparison that expanded it would find two lines
 * identical on Tuesday and different on Wednesday. [Template] already collapses a marker to
 * one token for the length band, per 7.5, for the same reason.
 *
 * **Case and punctuation go.** `Still {itemTitle}.` and `still {itemTitle}` are one line
 * with two typesettings, and a check that told them apart would pass the pair a reader
 * would notice.
 *
 * **Function words go, for the duplicate check only.** See [FUNCTION_WORDS].
 */
internal object CorpusText {

    /**
     * The words removed before two lines are compared for duplication.
     *
     * **Closed, short, and conservative on purpose.** Every word here is one whose removal
     * cannot change what a sentence claims: determiners, coordinators, the copula and the
     * auxiliaries, the pronouns, and the prepositions that carry a grammatical role rather
     * than a direction.
     *
     * What is deliberately absent is the more interesting half. `not`, `no`, `never`,
     * `nothing` and `none` reverse a claim. `more`, `less`, `most`, `least`, `every`,
     * `each`, `all`, `than`, `same` and `other` are the whole content of a comparison.
     * `in`, `on`, `out`, `up`, `into`, `over`, `back` and `again` are particles that change
     * a verb, and `went in` against `went out` is the difference between two families. `one`
     * and `still` look like filler and are not: `one answer` and `is still active` are
     * claims. A list that swept those in would report opposite sentences as one sentence,
     * which is the only failure this check can have that a reader would not catch.
     */
    val FUNCTION_WORDS: Set<String> = setOf(
        "a", "an", "the",
        "and", "or", "but", "so",
        "of", "to", "for", "from", "by", "with", "as", "at",
        "that", "this", "these", "those",
        "it", "its", "there", "here", "you", "your", "yours",
        "is", "are", "was", "were", "be", "been", "being", "am",
        "has", "have", "had", "do", "does", "did",
        "then", "now", "just", "also", "too",
    )

    /** Markers collapsed, lowercased, punctuation to space, whitespace collapsed. */
    fun normalize(text: String): String =
        NON_WORD.replace(Template.MARKER.replace(text, MARKER_TOKEN).lowercase(), " ")
            .replace(WHITESPACE, " ")
            .trim()

    /** [normalize] split into words. */
    fun tokens(text: String): List<String> = normalize(text).split(' ').filter { it.isNotEmpty() }

    /** [tokens] with [FUNCTION_WORDS] removed, as a multiset. */
    fun contentSignature(text: String): Map<String, Int> =
        tokens(text).filterNot { it in FUNCTION_WORDS }.groupingBy { it }.eachCount()

    /** How many tokens are in [signature], counting repeats. */
    fun signatureSize(signature: Map<String, Int>): Int = signature.values.sum()

    /**
     * The size of the symmetric difference of two content signatures.
     *
     * One word present in one line and absent from the other is 1. One word swapped for
     * another is 2, because a swap is a removal and an addition. That asymmetry is used by
     * the duplicate gate and is the reason it is stated here rather than left implicit.
     */
    fun signatureDistance(left: Map<String, Int>, right: Map<String, Int>): Int {
        var distance = 0
        for (key in left.keys + right.keys) {
            distance += kotlin.math.abs(left.getOrDefault(key, 0) - right.getOrDefault(key, 0))
        }
        return distance
    }

    /**
     * The sentences of a line, normalized.
     *
     * Split on terminal punctuation before normalization removes it, because several
     * approved lines are two sentences and a construction that spans both is a different
     * shape from one inside a single sentence.
     */
    fun sentences(text: String): List<String> =
        SENTENCE_SPLIT.split(Template.MARKER.replace(text, MARKER_TOKEN))
            .map { normalize(it) }
            .filter { it.isNotEmpty() }

    /** Every contiguous run of [size] tokens in [text], in order. */
    fun ngrams(text: String, size: Int): List<String> {
        val words = tokens(text)
        if (words.size < size) return emptyList()
        return (0..words.size - size).map { start -> words.subList(start, start + size).joinToString(" ") }
    }

    /** How many of [gram]'s words are not function words. */
    fun contentWordsIn(gram: String): Int = gram.split(' ').count { it !in FUNCTION_WORDS }

    /** What a slot marker becomes. Written once so no gate invents its own. */
    const val MARKER_TOKEN = "{}"

    private val NON_WORD = Regex("""[^a-z0-9{}\s]""")
    private val WHITESPACE = Regex("""\s+""")
    private val SENTENCE_SPLIT = Regex("""(?<=[.?])\s+""")
}
