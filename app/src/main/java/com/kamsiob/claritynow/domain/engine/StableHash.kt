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

    /** A stable, non negative value in `0 until buckets`. */
    fun bucket(value: String, buckets: Int): Int {
        require(buckets > 0) { "buckets must be positive" }
        val hash = of(value)
        // ushr avoids the sign bit rather than using abs, which overflows on MIN_VALUE.
        return ((hash ushr 1) % buckets).toInt()
    }
}
