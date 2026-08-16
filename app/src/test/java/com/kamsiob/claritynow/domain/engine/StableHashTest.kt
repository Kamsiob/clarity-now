package com.kamsiob.claritynow.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CLARITY_LOGIC_ENGINE.md 7.6. Two devices must reach the same variant from the
 * same dateKey with no shared state, so the hash is pinned to published FNV-1a
 * test vectors rather than to whatever this runtime happens to produce.
 */
class StableHashTest {

    @Test
    fun `matches the published fnv-1a 64 bit vectors`() {
        assertEquals(-3750763034362895579L, StableHash.of("")) // 0xcbf29ce484222325
        assertEquals(-5808556873153909620L, StableHash.of("a")) // 0xaf63dc4c8601ec8c
        assertEquals(-8821353812377114648L, StableHash.of("foobar")) // 0x85944171f73967e8
    }

    @Test
    fun `is stable for the same input`() {
        repeat(1_000) { index ->
            val key = "2026-03-14persistence.s2.$index"
            assertEquals(StableHash.of(key), StableHash.of(key))
        }
    }

    @Test
    fun `differs for near identical inputs`() {
        assertNotEquals(StableHash.of("2026-03-14"), StableHash.of("2026-03-15"))
        assertNotEquals(StableHash.of("persistence.s2.1"), StableHash.of("persistence.s2.2"))
    }

    @Test
    fun `bucket is always inside the range`() {
        repeat(5_000) { index ->
            val bucket = StableHash.bucket("variant-$index", 7)
            assertTrue("$bucket out of range", bucket in 0..6)
        }
    }

    @Test
    fun `bucket spreads across the range`() {
        val counts = IntArray(4)
        repeat(4_000) { index -> counts[StableHash.bucket("area-$index", 4)]++ }
        counts.forEach { count ->
            assertTrue("a bucket was starved: ${counts.toList()}", count > 700)
        }
    }

    @Test
    fun `bucket handles the most negative hash without overflowing`() {
        // abs(Long.MIN_VALUE) is still negative, which is the classic way this
        // function returns an index nobody can use.
        repeat(20_000) { index ->
            assertTrue(StableHash.bucket("k$index", 3) >= 0)
        }
    }
}
