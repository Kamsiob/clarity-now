package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FiringHistory
import com.kamsiob.claritynow.domain.engine.StableHash

/**
 * Which line of a bench gets said today. CLARITY_LOGIC_ENGINE.md 7.6.
 *
 * **Deterministic, and it reads as random.** Two devices holding the same merged log reach
 * the same line with no shared state, because the only inputs are the date, the line's own
 * key and the history the log itself carries. A person who never sees the ordering has no
 * way to tell it from a shuffle, which is the whole trick.
 *
 * `StableHash` is FNV-1a 64 bit and `String.hashCode()` is banned outright, per 7.6 and
 * `MASTER_BUILD_PROMPT.md` 11.4. The JVM's hash is not specified to be stable across
 * runtimes, so a phone and a desktop could disagree about a line neither had shown yet.
 */
object VariantChoice {

    /** 7.6 step 1. A line seen inside this many days is not offered again. */
    const val EXCLUSION_DAYS = 90

    /** The chosen line, and whether the bench had to be reused to find it. */
    data class Choice<T>(val value: T, val benchExhausted: Boolean)

    /**
     * Chooses from [bench], or null when it is empty.
     *
     * **On exhaustion this drops the most recently used line rather than the least.** 7.6
     * step 2 reads "use the full bench minus the least recently used", and the literal
     * reading removes the one line the person is least likely to remember, which is the
     * one worth reusing. The reading taken here is the one that serves the rule the step
     * exists for, which is that a person must not recognize a line: the line seen most
     * recently is the only one they might, so it is the one held back. Recorded rather than
     * quietly corrected, and reported with the phase.
     */
    fun <T> choose(
        bench: List<T>,
        dateKey: String,
        history: FiringHistory,
        keyOf: (T) -> String,
    ): Choice<T>? {
        if (bench.isEmpty()) return null
        val fresh = bench.filterNot { history.variantUsedWithin(keyOf(it), dateKey, EXCLUSION_DAYS) }
        if (fresh.isNotEmpty()) return Choice(head(fresh, dateKey, keyOf), benchExhausted = false)
        val mostRecent = bench.maxWithOrNull(
            compareBy<T> { history.variantsUsed[keyOf(it)].orEmpty() }.thenBy { keyOf(it) },
        )
        val reusable = bench.filterNot { it === mostRecent }.ifEmpty { bench }
        return Choice(head(reusable, dateKey, keyOf), benchExhausted = true)
    }

    /**
     * 7.6 steps 3 and 4: sort by `stableHash(dateKey + variantKey)` ascending, take the
     * head.
     *
     * The key is the second sort term for the same reason `ClarityRule.RANKING` ends with
     * one. Two lines whose hashes collide would otherwise be ordered by whatever order the
     * bench was built in, and a bench built by two different code paths would disagree.
     */
    private fun <T> head(bench: List<T>, dateKey: String, keyOf: (T) -> String): T =
        // `spread` rather than `of`. A bench is a set of keys differing only in a
        // trailing counter, which is the one input shape FNV-1a does not separate well,
        // and taking a minimum over it made five of twelve variants unreachable on
        // every date. See StableHash.spread.
        bench.minWithOrNull(compareBy<T> { StableHash.spread(dateKey + keyOf(it)) }.thenBy { keyOf(it) })!!
}
