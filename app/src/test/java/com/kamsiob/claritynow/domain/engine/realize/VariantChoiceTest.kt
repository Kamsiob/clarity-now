package com.kamsiob.claritynow.domain.engine.realize

import com.kamsiob.claritynow.domain.engine.FiringHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CLARITY_LOGIC_ENGINE.md 7.6.
 *
 * The property that matters is not which line comes out. It is that the same inputs
 * produce the same line on any device, that a line seen inside ninety days does not come
 * out again, and that a reader watching for a year cannot see the ordering.
 */
class VariantChoiceTest {

    private val bench = (1..12).map { "family.s1.%02d".format(it) }

    private fun historyOf(vararg used: Pair<String, String>) = FiringHistory(
        variantsUsed = used.toMap(),
        lastStageBySubject = emptyMap(),
        lastFiredBySubject = emptyMap(),
        lastPulseFamily = null,
    )

    @Test
    fun `the same day and the same bench always choose the same line`() {
        val first = VariantChoice.choose(bench, "2026-03-14", FiringHistory.EMPTY) { it }
        val second = VariantChoice.choose(bench, "2026-03-14", FiringHistory.EMPTY) { it }
        assertEquals(first?.value, second?.value)
    }

    @Test
    fun `the bench order does not decide the line`() {
        val forwards = VariantChoice.choose(bench, "2026-03-14", FiringHistory.EMPTY) { it }
        val backwards = VariantChoice.choose(bench.reversed(), "2026-03-14", FiringHistory.EMPTY) { it }
        assertEquals(forwards?.value, backwards?.value)
    }

    @Test
    fun `a line used inside ninety days is not offered again`() {
        val chosen = requireNotNull(VariantChoice.choose(bench, "2026-03-14", FiringHistory.EMPTY) { it }).value
        val history = historyOf(chosen to "2026-02-20")
        val next = requireNotNull(VariantChoice.choose(bench, "2026-03-14", history) { it })
        assertNotEquals(chosen, next.value)
        assertFalse(next.benchExhausted)
    }

    @Test
    fun `a line used more than ninety days ago is available again`() {
        val chosen = requireNotNull(VariantChoice.choose(bench, "2026-03-14", FiringHistory.EMPTY) { it }).value
        val history = historyOf(chosen to "2025-11-01")
        assertEquals(chosen, VariantChoice.choose(bench, "2026-03-14", history) { it }?.value)
    }

    @Test
    fun `an exhausted bench is reused, without the line seen most recently`() {
        val history = historyOf(*bench.mapIndexed { index, key -> key to "2026-03-%02d".format(index + 1) }.toTypedArray())
        val choice = requireNotNull(VariantChoice.choose(bench, "2026-03-14", history) { it })
        assertTrue("the diagnostic is what tells an author the bench is too small", choice.benchExhausted)
        assertNotEquals("the line seen most recently is the one held back", bench.last(), choice.value)
    }

    @Test
    fun `an empty bench chooses nothing rather than throwing`() {
        assertNull(VariantChoice.choose(emptyList<String>(), "2026-03-14", FiringHistory.EMPTY) { it })
    }

    @Test
    fun `across a year the bench is spread rather than settled on one line`() {
        val seen = (0 until 365).map { day ->
            val key = java.time.LocalDate.of(2026, 1, 1).plusDays(day.toLong()).toString()
            requireNotNull(VariantChoice.choose(bench, key, FiringHistory.EMPTY) { it }).value
        }
        assertTrue(
            "a bench of ${bench.size} that produced only ${seen.distinct().size} lines over a year " +
                "is not being spread, and a reader would recognize the repeats",
            seen.distinct().size >= bench.size - 4,
        )
    }
}
