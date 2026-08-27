package com.kamsiob.claritynow.domain.engine.catalog

/**
 * English number words to integers, for reading the corpus stage headers.
 * CLARITY_LOGIC_ENGINE.md 7.3.
 *
 * The thresholds live in the corpus because the corpus is the file an author edits, and
 * the corpus is written in prose: `Stage 2, six to thirteen days`, not `6..13`. So the
 * engine has to read English to learn its own escalation ladder.
 *
 * Zero is deliberately absent. No stage threshold in any of the three corpus files
 * begins at zero, and validator check 4 exists to make sure zero never reaches a
 * template, so a `zero` that parsed here would be a defect that parsed cleanly.
 */
object NumberWords {

    private val UNITS = mapOf(
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
        "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18,
        "nineteen" to 19,
    )

    private val TENS = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90,
    )

    /** True when [word] can begin a number. */
    fun startsNumber(word: String): Boolean = word in UNITS || word in TENS

    /**
     * Reads a number starting at [from], greedily, and returns it with the index after it.
     *
     * Greedy matters. `fourteen to twenty nine days` has to read `twenty nine` as 29 and
     * not as 20 followed by a stray `nine`, because the second reading produces a stage
     * that ends five days early and a ladder with a hole in it that no test would see.
     */
    fun read(tokens: List<String>, from: Int): Reading? {
        if (from !in tokens.indices) return null
        val first = tokens[from]
        UNITS[first]?.let { return Reading(it, from + 1) }
        val tens = TENS[first] ?: return null
        val next = tokens.getOrNull(from + 1)
        val unit = if (next == null) null else UNITS[next]
        // Only one through nine combine with a tens word. `twenty ten` is not a number,
        // and reading it as one would invent a threshold nobody wrote.
        return if (unit != null && unit in 1..9) Reading(tens + unit, from + 2) else Reading(tens, from + 1)
    }

    /** A number and the index of the token after it. */
    data class Reading(val value: Int, val next: Int)
}
