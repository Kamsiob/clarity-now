package com.kamsiob.claritynow.domain.engine

/**
 * FNV-1a, 64 bit. The only hash this app is permitted to use for anything that
 * two devices must agree on.
 *
 * `String.hashCode()` is not guaranteed stable across platforms or runtimes, and
 * the phone and the future Linux desktop must reach the same variant from the same
 * event log with no shared state. CLARITY_LOGIC_ENGINE.md 7.6.
 */
object StableHash {

    private const val OFFSET_BASIS = -3750763034362895579L // 14695981039346656037 unsigned
    private const val PRIME = 1099511628211L

    /** Hashes the UTF-8 bytes of [value]. Signed, so callers wanting a bucket use [bucket]. */
    fun of(value: String): Long {
        var hash = OFFSET_BASIS
        for (byte in value.encodeToByteArray()) {
            hash = hash xor (byte.toLong() and 0xFF)
            hash *= PRIME
        }
        return hash
    }

    /**
     * [of], then an avalanche, for when the minimum is taken over keys that differ
     * only in their last few bytes.
     *
     * FNV-1a mixes each byte into the low bits and then multiplies, so a change in a
     * trailing byte moves the high bits well and the low bits barely at all. That is
     * fine for a bucket and fine for a single value, and it is wrong for
     * `minWithOrNull` over a bench of variant keys, which is exactly a set of strings
     * differing only in a trailing counter.
     *
     * Measured on a bench of twelve keys named `family.s1.01` through `.12` over 365
     * dates: plain [of] produced **seven** distinct choices, three of them taking a
     * quarter of the year each, and `.03`, `.05`, `.07`, `.09` and `.11` **could not
     * win on any date**. Not rare, not seasonal: structurally unreachable. With this
     * finalizer all twelve appear and the largest share is 11 percent.
     *
     * The finalizer is splitmix64's, which is a standard bijective mix, so this stays
     * deterministic and stays a pure function of the string. [of] is deliberately left
     * alone: deliberate silence in 5.1 and [bucket] both read it, their behavior is
     * specified against it, and changing it would move decisions already recorded in
     * the log.
     *
     * `CLARITY_LOGIC_ENGINE.md` 7.6 asks for a choice that is deterministic and
     * apparently random. Plain [of] delivered the first and not the second.
     */
    fun spread(value: String): Long {
        var hash = of(value)
        hash = hash xor (hash ushr 30)
        hash *= -4658895280553007687L // 0xBF58476D1CE4E5B9
        hash = hash xor (hash ushr 27)
        hash *= -7723592293110705685L // 0x94D049BB133111EB
        return hash xor (hash ushr 31)
    }

    /** A stable, non negative value in `0 until buckets`. */
    fun bucket(value: String, buckets: Int): Int {
        require(buckets > 0) { "buckets must be positive" }
        val hash = of(value)
        // ushr avoids the sign bit rather than using abs, which overflows on MIN_VALUE.
        return ((hash ushr 1) % buckets).toInt()
    }
}
