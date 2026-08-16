package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.domain.engine.StableHash
import kotlin.math.round

/**
 * Fractional indexing over a base 62 alphabet. MASTER_BUILD_PROMPT 5.3.
 *
 * Order keys are strings, never integers. Inserting between two items produces a
 * key strictly between them without touching any other row, which is what lets two
 * devices reorder the same list at the same time and still agree afterwards.
 * Integer positions break under concurrent reorder and cannot be retrofitted once
 * a person has data.
 *
 * The alphabet is in ASCII order, so ordinary string comparison is the sort.
 *
 * A key is a short integer part followed by an optional fraction. The integer part
 * keeps repeated appends short: the first hundred appends are two characters, not
 * seventeen. The fraction only appears when something is inserted between two
 * adjacent keys.
 *
 * The head character of the integer part encodes that part's length. `A` through
 * `Z` count down from 27 characters to 2, and `a` through `z` count up from 2 to
 * 27. `a0` is where an empty list starts, and there is room either side of it for
 * more insertions than any person will ever make.
 */
object OrderKey {

    const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    private const val BASE = 62
    private val SMALLEST_INTEGER = "A" + "0".repeat(26)

    /** The key for the first item in an empty list. */
    fun first(jitter: String? = null): String = between(null, null, jitter)

    /** A key that sorts after every existing key. */
    fun last(after: String, jitter: String? = null): String = between(after, null, jitter)

    /** A key that sorts before every existing key. */
    fun before(next: String, jitter: String? = null): String = between(null, next, jitter)

    /**
     * A key strictly between [a] and [b]. Either bound may be null, meaning
     * unbounded on that side. Requires `a < b` when both are present.
     *
     * [jitter] is what keeps two devices from computing the same key.
     *
     * Fractional indexing is deterministic, which is a virtue everywhere except
     * here: two people inserting at the same point in the same queue, offline,
     * would otherwise arrive at byte identical keys and the queue would hold two
     * rows claiming one position. A couple of characters derived from the device's
     * originId removes that. It is appended only when the result still lands below
     * the upper bound, because a generated key is sometimes a prefix of that bound
     * and extending it there would push it past.
     */
    fun between(a: String?, b: String?, jitter: String? = null): String {
        val base = exactlyBetween(a, b)
        if (jitter.isNullOrEmpty()) return base
        val candidate = base + jitter
        val fits = (b == null || candidate < b) && isValid(candidate)
        return if (fits) candidate else base
    }

    /**
     * Two characters from the device id, stable for the life of the install.
     * Never ends in the lowest digit, which no key is allowed to do.
     */
    fun jitterFor(originId: String): String {
        val hash = StableHash.of(originId)
        val high = DIGITS[((hash ushr 1) % BASE).toInt()]
        val lowIndex = ((hash ushr 17) % (BASE - 1)).toInt() + 1
        return "$high${DIGITS[lowIndex]}"
    }

    private fun exactlyBetween(a: String?, b: String?): String {
        a?.let(::validate)
        b?.let(::validate)
        require(a == null || b == null || a < b) { "order keys out of sequence: $a is not before $b" }

        if (a == null) {
            if (b == null) return "a0"
            val integerB = integerPartOf(b)
            val fractionB = b.substring(integerB.length)
            if (integerB == SMALLEST_INTEGER) return integerB + midpoint("", fractionB)
            if (integerB < b) return integerB
            return requireNotNull(decrementInteger(integerB)) {
                "order key space exhausted below $b; rebalance the list"
            }
        }

        if (b == null) {
            val integerA = integerPartOf(a)
            val fractionA = a.substring(integerA.length)
            val incremented = incrementInteger(integerA)
            return incremented ?: (integerA + midpoint(fractionA, null))
        }

        val integerA = integerPartOf(a)
        val fractionA = a.substring(integerA.length)
        val integerB = integerPartOf(b)
        val fractionB = b.substring(integerB.length)
        if (integerA == integerB) return integerA + midpoint(fractionA, fractionB)

        val incremented = requireNotNull(incrementInteger(integerA)) {
            "order key space exhausted above $a; rebalance the list"
        }
        return if (incremented < b) incremented else integerA + midpoint(fractionA, null)
    }

    /**
     * [count] evenly spaced keys, for the rebalance case. Rewriting a whole list is
     * only correct when the caller emits an ITEM_REORDERED or AREA_REORDERED event
     * per row, so the change replays on every device.
     */
    fun sequence(count: Int): List<String> {
        require(count >= 0) { "count must not be negative" }
        val keys = ArrayList<String>(count)
        var previous: String? = null
        repeat(count) {
            val next = between(previous, null)
            keys += next
            previous = next
        }
        return keys
    }

    /**
     * True when the keys have grown long enough to be worth rewriting. Fractional
     * indexing only degrades under repeated insertion at the same point, so this is
     * rare, and the threshold is generous rather than tuned.
     */
    fun needsRebalance(keys: Collection<String>, maxLength: Int = 20): Boolean =
        keys.any { it.length > maxLength }

    fun isValid(key: String): Boolean = runCatching { validate(key) }.isSuccess

    // Internals ---------------------------------------------------------------

    private fun validate(key: String) {
        require(key.isNotEmpty()) { "order key must not be empty" }
        require(key != SMALLEST_INTEGER) { "order key $key is the reserved lower bound" }
        val integerPart = integerPartOf(key)
        val fraction = key.substring(integerPart.length)
        require(!fraction.endsWith("0")) { "order key fraction must not end in 0: $key" }
        require(key.all { it in DIGITS }) { "order key contains a character outside the alphabet: $key" }
    }

    private fun integerLengthOf(head: Char): Int = when (head) {
        in 'a'..'z' -> head - 'a' + 2
        in 'A'..'Z' -> 'Z' - head + 2
        else -> throw IllegalArgumentException("invalid order key head: $head")
    }

    private fun integerPartOf(key: String): String {
        val length = integerLengthOf(key[0])
        require(length <= key.length) { "order key is shorter than its integer part: $key" }
        return key.substring(0, length)
    }

    /** Returns null when the integer part cannot grow any further. */
    private fun incrementInteger(integer: String): String? {
        val head = integer[0]
        val digits = integer.substring(1).toCharArray()
        var carry = true
        var index = digits.lastIndex
        while (carry && index >= 0) {
            val next = DIGITS.indexOf(digits[index]) + 1
            if (next == BASE) {
                digits[index] = DIGITS[0]
            } else {
                digits[index] = DIGITS[next]
                carry = false
            }
            index--
        }
        if (!carry) return head + String(digits)

        return when {
            head == 'Z' -> "a" + DIGITS[0]
            head == 'z' -> null
            else -> {
                val nextHead = head + 1
                val body = if (nextHead > 'a') String(digits) + DIGITS[0] else String(digits).dropLast(1)
                nextHead + body
            }
        }
    }

    /** Returns null when the integer part cannot shrink any further. */
    private fun decrementInteger(integer: String): String? {
        val head = integer[0]
        val digits = integer.substring(1).toCharArray()
        var borrow = true
        var index = digits.lastIndex
        while (borrow && index >= 0) {
            val next = DIGITS.indexOf(digits[index]) - 1
            if (next == -1) {
                digits[index] = DIGITS[BASE - 1]
            } else {
                digits[index] = DIGITS[next]
                borrow = false
            }
            index--
        }
        if (!borrow) return head + String(digits)

        return when {
            head == 'a' -> "Z" + DIGITS[BASE - 1]
            head == 'A' -> null
            else -> {
                val nextHead = head - 1
                val body = if (nextHead < 'Z') String(digits) + DIGITS[BASE - 1] else String(digits).dropLast(1)
                nextHead + body
            }
        }
    }

    /**
     * A fraction strictly between [a] and [b], where both are fraction strings
     * without an integer part and [b] null means one.
     */
    private fun midpoint(a: String, b: String?): String {
        require(b == null || a < b) { "midpoint bounds out of sequence" }
        require(!a.endsWith("0")) { "fraction must not end in 0" }
        require(b == null || !b.endsWith("0")) { "fraction must not end in 0" }

        if (b != null) {
            var shared = 0
            while (shared < b.length && (a.getOrNull(shared) ?: '0') == b[shared]) shared++
            if (shared > 0) {
                return b.substring(0, shared) +
                    midpoint(a.drop(shared), b.substring(shared))
            }
        }

        val digitA = if (a.isNotEmpty()) DIGITS.indexOf(a[0]) else 0
        val digitB = if (b != null && b.isNotEmpty()) DIGITS.indexOf(b[0]) else BASE

        if (digitB - digitA > 1) {
            val middle = round(0.5 * (digitA + digitB)).toInt()
            return DIGITS[middle].toString()
        }

        return if (b != null && b.length > 1) {
            b.substring(0, 1)
        } else {
            DIGITS[digitA] + midpoint(a.drop(1), null)
        }
    }
}
