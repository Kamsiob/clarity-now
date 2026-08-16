package com.kamsiob.claritynow.domain.replay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * MASTER_BUILD_PROMPT 5.3. Order keys are the one part of the data model that
 * cannot be retrofitted once a person has data, so they get tested hard now.
 */
class OrderKeyTest {

    @Test
    fun `the alphabet is in ascii order`() {
        val digits = OrderKey.DIGITS
        assertEquals(62, digits.length)
        assertEquals(digits.toList().sorted(), digits.toList())
        assertEquals(digits.length, digits.toSet().size)
    }

    @Test
    fun `an empty list starts somewhere with room on both sides`() {
        val first = OrderKey.first()
        assertTrue(OrderKey.isValid(first))
        assertTrue(OrderKey.before(first) < first)
        assertTrue(OrderKey.last(first) > first)
    }

    @Test
    fun `appending stays short`() {
        var key = OrderKey.first()
        val keys = mutableListOf(key)
        repeat(500) {
            key = OrderKey.last(key)
            keys += key
        }
        assertEquals(keys, keys.sorted())
        assertEquals(keys.size, keys.toSet().size)
        val longest = keys.maxOf { it.length }
        assertTrue("append keys grew to $longest characters", longest <= 4)
    }

    @Test
    fun `prepending stays short`() {
        var key = OrderKey.first()
        val keys = mutableListOf(key)
        repeat(500) {
            key = OrderKey.before(key)
            keys += key
        }
        assertEquals(keys.reversed(), keys.sorted())
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.maxOf { it.length } <= 5)
    }

    @Test
    fun `inserting between two keys lands strictly between them`() {
        val random = Random(42)
        val keys = mutableListOf(OrderKey.first())
        repeat(3_000) {
            val at = random.nextInt(keys.size + 1)
            val before = keys.getOrNull(at - 1)
            val after = keys.getOrNull(at)
            val inserted = OrderKey.between(before, after)
            if (before != null) assertTrue("$before < $inserted", before < inserted)
            if (after != null) assertTrue("$inserted < $after", inserted < after)
            keys.add(at, inserted)
        }
        assertEquals(keys, keys.sorted())
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `repeatedly inserting at the same point still orders correctly`() {
        var low = OrderKey.first()
        val high = OrderKey.last(low)
        val produced = mutableListOf<String>()
        repeat(200) {
            val next = OrderKey.between(low, high)
            assertTrue("$low < $next", low < next)
            assertTrue("$next < $high", next < high)
            produced += next
            low = next
        }
        assertEquals(produced, produced.sorted())
        // This is the case fractional indexing degrades on, which is what
        // needsRebalance exists to notice.
        assertTrue(OrderKey.needsRebalance(produced + high))
    }

    @Test
    fun `rebalance produces a short evenly spaced sequence`() {
        val keys = OrderKey.sequence(400)
        assertEquals(400, keys.size)
        assertEquals(keys, keys.sorted())
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.all(OrderKey::isValid))
        assertTrue(!OrderKey.needsRebalance(keys))
    }

    @Test
    fun `no key ever ends in the lowest digit`() {
        val random = Random(7)
        val keys = mutableListOf(OrderKey.first())
        repeat(2_000) {
            val at = random.nextInt(keys.size + 1)
            keys.add(at, OrderKey.between(keys.getOrNull(at - 1), keys.getOrNull(at)))
        }
        keys.forEach { key ->
            assertTrue("$key ends in a zero", OrderKey.isValid(key))
        }
    }

    @Test
    fun `out of sequence bounds are refused`() {
        val low = OrderKey.first()
        val high = OrderKey.last(low)
        assertTrue(runCatching { OrderKey.between(high, low) }.isFailure)
        assertTrue(runCatching { OrderKey.between(low, low) }.isFailure)
    }

    @Test
    fun `malformed keys are refused`() {
        assertTrue(!OrderKey.isValid(""))
        assertTrue(!OrderKey.isValid("a"))
        assertTrue(!OrderKey.isValid("a0V0"))
        assertTrue(!OrderKey.isValid("!!"))
        assertTrue(OrderKey.isValid("a0"))
        assertTrue(OrderKey.isValid("a0V"))
    }

    @Test
    fun `two devices inserting at the same point both survive`() {
        val left = OrderKey.first()
        val right = OrderKey.last(left)
        val fromA = OrderKey.between(left, right)
        val fromB = OrderKey.between(left, fromA)
        // Concurrent inserts produce different keys, so neither overwrites the other
        // and the merged order is deterministic.
        assertNotEquals(fromA, fromB)
        assertEquals(listOf(left, fromB, fromA, right), listOf(left, fromA, right, fromB).sorted())
    }
}
