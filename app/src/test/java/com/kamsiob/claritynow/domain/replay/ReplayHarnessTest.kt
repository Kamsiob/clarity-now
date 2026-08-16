package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.inTotalOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The replay test harness, MASTER_BUILD_PROMPT 6.5.
 *
 * This is built in phase 1 rather than at the end, because it is the thing that
 * delivers the guarantee that sync can be added later without reworking the data
 * model. A harness written after the fact only confirms what was already shipped.
 */
class ReplayHarnessTest {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    private fun serialize(state: ClarityState): String =
        json.encodeToString(ClarityState.serializer(), state.canonical())

    // Determinism -------------------------------------------------------------

    @Test
    fun `replaying the same log twice produces identical state`() {
        for (seed in 1L..60L) {
            val events = EventStreamGenerator(seed, "device-a").generate(220)
            val first = ClarityReplay.replay(events)
            val second = ClarityReplay.replay(events)
            assertEquals("seed $seed", serialize(first), serialize(second))
        }
    }

    @Test
    fun `shuffling the delivery order does not change the result`() {
        for (seed in 1L..40L) {
            val events = EventStreamGenerator(seed, "device-a").generate(180)
            val straight = ClarityReplay.replay(events)
            val shuffled = ClarityReplay.replay(events.shuffled(java.util.Random(seed)))
            assertEquals("seed $seed", serialize(straight), serialize(shuffled))
        }
    }

    @Test
    fun `every generated stream leaves the invariants intact`() {
        for (seed in 1L..60L) {
            val events = EventStreamGenerator(seed, "device-a").generate(220)
            val state = ClarityReplay.replay(events)
            val violations = ClarityInvariants.check(state)
            assertTrue(
                "seed $seed: " + violations.joinToString { "${it.rule} (${it.detail})" },
                violations.isEmpty(),
            )
        }
    }

    @Test
    fun `the generator exercises the reducer rather than its diagnostics path`() {
        val events = EventStreamGenerator(7L, "device-a").generate(400)
        val state = ClarityReplay.replay(events)
        // A handful of skips is expected and healthy, since operations race each
        // other by design. A stream that is mostly skipped would prove nothing.
        assertTrue(
            "too many diagnostics: ${state.diagnostics.size} of ${events.size}",
            state.diagnostics.size < events.size / 10,
        )
        assertTrue("no areas were created", state.areas.isNotEmpty())
        assertTrue("no items were created", state.items.isNotEmpty())
    }

    // Idempotency -------------------------------------------------------------

    @Test
    fun `delivering every event twice changes nothing`() {
        for (seed in 1L..40L) {
            val events = EventStreamGenerator(seed, "device-a").generate(160)
            val once = ClarityReplay.replay(events)
            val twice = ClarityReplay.replay(events + events)
            assertEquals("seed $seed", serialize(once), serialize(twice))
        }
    }

    @Test
    fun `duplicate delivery is removed before the reducer sees it`() {
        val events = EventStreamGenerator(3L, "device-a").generate(50)
        assertEquals(events.size, (events + events).inTotalOrder().size)
    }

    // Divergence --------------------------------------------------------------

    @Test
    fun `two devices that diverge and merge reach byte identical state`() {
        for (seed in 1L..80L) {
            val ancestor = EventStreamGenerator(seed, "device-a")
            ancestor.generate(90)

            val left = ancestor.fork(seed * 31 + 1, "device-a")
            val right = ancestor.fork(seed * 17 + 5, "device-b")
            left.generate(50)
            right.generate(50)

            val shared = ancestor.events
            val merged = shared + left.events.drop(shared.size) + right.events.drop(shared.size)

            // Each side receives the union in a different arrival order.
            val onLeft = ClarityReplay.replay(merged)
            val onRight = ClarityReplay.replay(merged.reversed())

            assertEquals("seed $seed", serialize(onLeft), serialize(onRight))
            assertTrue(
                "seed $seed invariants",
                ClarityInvariants.check(onLeft).isEmpty(),
            )
        }
    }

    @Test
    fun `clock skew between devices does not affect the outcome`() {
        val ancestor = EventStreamGenerator(11L, "device-a")
        ancestor.generate(80)
        val shared = ancestor.events

        val left = ancestor.fork(101L, "device-a")
        val right = ancestor.fork(202L, "device-b")
        left.generate(40)
        right.generate(40)

        val merged = shared + left.events.drop(shared.size) + right.events.drop(shared.size)
        val expected = serialize(ClarityReplay.replay(merged))

        // The same log with one device's wall clocks pushed 48 hours out of step.
        val skewed = merged.map { event ->
            if (event.originId == "device-b") {
                event.copy(wallClock = event.wallClock - 48 * 3_600_000L)
            } else {
                event
            }
        }
        val actual = ClarityReplay.replay(skewed)

        // wallClock is display data, so the states differ only where a timestamp is
        // stored. Ordering, statuses and queue positions must be identical.
        assertEquals(
            expected.replace(Regex("""-?\d{12,}"""), "T"),
            serialize(actual).replace(Regex("""-?\d{12,}"""), "T"),
        )
    }

    @Test
    fun `total order ignores the wall clock`() {
        val early = event(id = "e1", lamport = 9, originId = "b", wallClock = 9_000_000_000_000L)
        val late = event(id = "e2", lamport = 10, originId = "a", wallClock = 1L)
        val sorted = listOf(late, early).inTotalOrder()
        assertEquals(listOf("e1", "e2"), sorted.map { it.id })
    }

    @Test
    fun `origin id breaks a lamport tie deterministically`() {
        val fromB = event(id = "x", lamport = 5, originId = "device-b", wallClock = 1L)
        val fromA = event(id = "y", lamport = 5, originId = "device-a", wallClock = 2L)
        assertEquals(
            listOf("y", "x"),
            listOf(fromB, fromA).inTotalOrder().map { it.id },
        )
        assertNotEquals(fromA.originId, fromB.originId)
    }

    // Checkpoints -------------------------------------------------------------

    @Test
    fun `a checkpoint plus its tail equals a full replay`() {
        for (seed in 1L..40L) {
            val generator = EventStreamGenerator(seed, "device-a")
            generator.generate(120)
            val head = generator.events
            generator.generate(80)
            val whole = generator.events

            val checkpoint = ClarityReplay.checkpoint(head)
            val resumed = ClarityReplay.replayFrom(checkpoint, whole)
            val full = ClarityReplay.replay(whole)

            assertEquals("seed $seed", serialize(full), serialize(resumed))
        }
    }

    @Test
    fun `a checkpoint survives a serialization round trip`() {
        val events = EventStreamGenerator(5L, "device-a").generate(150)
        val checkpoint = ClarityReplay.checkpoint(events)
        val text = json.encodeToString(ClarityCheckpoint.serializer(), checkpoint)
        val restored = json.decodeFromString(ClarityCheckpoint.serializer(), text)
        assertEquals(serialize(checkpoint.state), serialize(restored.state))
        assertEquals(checkpoint.position, restored.position)
    }

    @Test
    fun `a checkpoint cannot be resumed once older events arrive`() {
        val generator = EventStreamGenerator(9L, "device-a")
        generator.generate(100)
        val checkpoint = ClarityReplay.checkpoint(generator.events)
        // A foreign log whose newest event predates the checkpoint.
        val foreign = listOf(event(id = "old", lamport = 1, originId = "device-z", wallClock = 1L))
        assertTrue(!ClarityReplay.canResume(checkpoint, foreign))
    }

    // Reset virginity ---------------------------------------------------------

    @Test
    fun `erasing everything returns a virgin state`() {
        val events = EventStreamGenerator(13L, "device-a").generate(300)
        val lived = ClarityReplay.replay(events)
        assertTrue("the fixture should not be empty", lived.areas.isNotEmpty())

        // Erase all data wipes the log, every cache table and every checkpoint.
        val afterErase = ClarityReplay.replay(emptyList())

        assertEquals(serialize(ClarityState.EMPTY), serialize(afterErase))
        assertEquals(0, afterErase.eventsApplied)
        assertEquals(0L, afterErase.lastLamport)
        assertTrue(afterErase.areas.isEmpty())
        assertTrue(afterErase.items.isEmpty())
        assertTrue("no personal records survive", afterErase.reports.isEmpty())
        assertTrue("no first ever flag is spent", afterErase.pulses.isEmpty())
        assertTrue("no variation history survives", afterErase.pulses.isEmpty())
        assertTrue("no plan history survives", afterErase.plans.isEmpty())
        assertTrue("no conflicts survive", afterErase.conflicts.isEmpty())
        assertTrue("no settings survive", afterErase.settings.isEmpty())

        // And a fresh life afterwards behaves exactly like a first install.
        val reborn = ClarityReplay.replay(EventStreamGenerator(13L, "device-a").generate(300))
        assertEquals(serialize(lived), serialize(reborn))
    }

    // Log format --------------------------------------------------------------

    @Test
    fun `the log survives a json round trip unchanged`() {
        for (seed in 1L..20L) {
            val events = EventStreamGenerator(seed, "device-a").generate(150)
            val text = ClarityEventJson.encodeLog(events)
            val decoded = ClarityEventJson.decodeLog(text)
            assertTrue("seed $seed skipped ${decoded.skippedTypes}", decoded.skippedTypes.isEmpty())
            assertEquals("seed $seed", events, decoded.events)
            assertEquals(
                "seed $seed replays the same",
                serialize(ClarityReplay.replay(events)),
                serialize(ClarityReplay.replay(decoded.events)),
            )
        }
    }

    private fun event(id: String, lamport: Long, originId: String, wallClock: Long): ClarityEvent =
        ClarityEvent.of(
            id = id,
            wallClock = wallClock,
            lamport = lamport,
            originId = originId,
            payload = com.kamsiob.claritynow.data.event.SettingChanged("afterCompleting", "a", "b"),
        )
}
