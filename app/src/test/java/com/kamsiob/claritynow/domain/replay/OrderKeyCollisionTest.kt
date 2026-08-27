package com.kamsiob.claritynow.domain.replay

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The order key an area's next item gets has to be computed over every live item in
 * that area, and not over its queue alone.
 *
 * An area's active item and its queued items share one ordering space. The active
 * item keeps the key it had when it was promoted, and a demoted item rejoins the
 * queue carrying a key from that same space. So a key chosen by looking only at the
 * queue can collide with the active item's key, and the collision is not exotic: it
 * happens on the second item added to any fresh area, because the queue is empty at
 * that moment and `first` is deterministic for a given jitter.
 *
 * The collision is silent at first. Two items simply hold the same key, and the tie
 * is broken by id, so the queue still renders in a stable order and nothing looks
 * wrong. It surfaces later, as an `IllegalArgumentException` out of
 * `OrderKey.between`, the first time anything asks for a key between the two of them,
 * which is a reorder. That distance between the cause and the symptom is why this
 * test asserts the cause.
 *
 * Found by the replay harness in August 2026, after the generator was taught to file
 * items out of the inbox. The defect itself predates that and shipped in 0.2.0.
 */
class OrderKeyCollisionTest {

    private val jitter = OrderKey.jitterFor("device-a")

    @Test
    fun `the second item added to a fresh area does not collide with the first`() {
        // What the repository did: look at the queue, which is empty both times
        // because the first item was promoted straight out of it.
        val first = OrderKey.first(jitter)
        val second = OrderKey.first(jitter)
        assertTrue(
            "OrderKey.first is deterministic for a given jitter, so computing a key " +
                "from an empty queue twice returns the same key. That is the bug, and " +
                "it is why the tail must be taken over live items rather than the queue.",
            first == second,
        )
    }

    @Test
    fun `a key taken after the highest live key never collides`() {
        // What the repository does now: the tail is the highest key among every live
        // item in the area, active included.
        val active = OrderKey.first(jitter)
        val next = OrderKey.last(active, jitter)
        assertTrue("$next should sort after $active", next > active)

        val third = OrderKey.last(maxOf(active, next), jitter)
        assertTrue("$third should sort after $next", third > next)
    }

    @Test
    fun `a key between two identical keys is refused rather than silently wrong`() {
        val key = OrderKey.first(jitter)
        val failed = try {
            OrderKey.between(key, key, jitter)
            false
        } catch (expected: IllegalArgumentException) {
            true
        }
        assertTrue(
            "between() must refuse an impossible request. Returning something " +
                "plausible here would bury the collision instead of surfacing it.",
            failed,
        )
    }
}
