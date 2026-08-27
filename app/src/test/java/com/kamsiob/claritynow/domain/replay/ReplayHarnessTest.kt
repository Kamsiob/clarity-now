package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.inTotalOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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

    // The Addendum 01 vocabulary ----------------------------------------------

    /**
     * Every type in the catalog is reachable from the generator, and therefore
     * covered by all four properties above.
     *
     * The four property tests are proofs over whatever `EventStreamGenerator`
     * happens to emit, so a type it cannot produce is a type whose determinism,
     * idempotency, merge behavior and checkpoint equivalence have never been
     * checked. That gap is invisible: every test still passes, and it passes for a
     * smaller catalog than the one that shipped. This is the assertion that makes
     * adding an operation to the generator part of adding a type.
     */
    @Test
    fun `the generator reaches every event type in the catalog`() {
        val produced = mutableSetOf<ClarityEventType>()
        for (seed in 1L..30L) {
            EventStreamGenerator(seed, "device-a").generate(400).mapTo(produced) { it.type }
        }
        val missing = ClarityEventType.entries - produced
        assertTrue(
            "the generator can never emit: ${missing.joinToString()}. Every property " +
                "test in this class is a proof over what it emits, so an unreachable " +
                "type is an unproved one.",
            missing.isEmpty(),
        )
    }

    /**
     * An unfiled item is real, and a replay keeps it that way. Addendum 01 4a,
     * DECISIONS.md C8.
     *
     * The failure worth guarding against is not that the item disappears. It is
     * that it quietly acquires an area, or a status only an area can give it, and
     * then reads as work sitting in a queue somebody never put it in.
     */
    @Test
    fun `an unfiled item survives a replay with no area and is never active`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-a", "area-work", "Call the printer", null, "a0", "Work"))
        log.add(0, promotion("item-a", "area-work", "Call the printer"))
        log.add(1, ItemAdded("item-idea", null, "Look into the loft insulation", null, "a0", null))

        val state = log.state()
        val idea = state.items.getValue("item-idea")

        assertNull(idea.areaId)
        assertEquals(ItemStatus.QUEUED, idea.status)
        assertNull(idea.activeSince)
        assertNull(idea.completedAt)
        assertEquals(listOf("item-idea"), state.unfiledItems.map { it.id })
        // In no area's queue, and not the one thing happening anywhere.
        assertEquals("item-a", state.activeItemIn("area-work")?.id)
        assertTrue(state.queueIn("area-work").isEmpty())
        assertTrue(ClarityInvariants.check(state).isEmpty())
        assertTrue(state.diagnostics.isEmpty())
        // A shuffled delivery reaches the same place, which is the whole guarantee.
        assertEquals(serialize(state), serialize(ClarityReplay.replay(log.events().reversed())))
    }

    /**
     * Promoting an unfiled item is refused rather than applied.
     *
     * `itemPromoted` is the only branch in the reducer that sets ACTIVE, so it is
     * the only place this state could be reached. Left unguarded it would produce
     * an item that is the one thing happening in no area at all, and the invariant
     * check would then be reporting against a state a person had already been
     * shown.
     */
    @Test
    fun `an unfiled item cannot be promoted, and the refusal is recorded`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-idea", null, "Look into the loft insulation", null, "a0", null))
        log.add(1, promotion("item-idea", "area-work", "Look into the loft insulation"))

        val state = log.state()
        assertEquals(ItemStatus.QUEUED, state.items.getValue("item-idea").status)
        assertNull(state.items.getValue("item-idea").areaId)
        assertNull(state.activeItemIn("area-work"))
        // Recorded, never thrown. An event referencing a state it cannot reach is a
        // diagnostic, which is what lets a foreign log be replayed at all.
        assertEquals(1, state.diagnostics.size)
        assertEquals(ClarityEventType.ITEM_PROMOTED.name, state.diagnostics.single().eventType)
        assertTrue(ClarityInvariants.check(state).isEmpty())
    }

    /**
     * The filing transition, into an area that is sitting idle.
     *
     * **The idle area is the case that matters, and this is about the event rather
     * than about the app's behavior.** `ITEM_FILED` sets an area and an order key
     * and leaves the status where it was, full stop. Whether the app writes an
     * `ITEM_PROMOTED` beside it when the destination is idle is a separate question
     * and an open one: MASTER_BUILD_PROMPT 14b.1 says it should, in the same
     * transaction, matching what an add into an idle area does, while the build
     * instruction for the schema commit said filing is a separate, later, optional
     * act and `ClarityRepository.fileItem` records the disagreement rather than
     * settling it quietly.
     *
     * That question does not reach here. A promotion that happens is a second event
     * with its own payload, and this asserts what the first one means on its own,
     * which is what a second implementation reading `docs/EVENT_FORMAT.md` has to
     * get right either way. The two readings already agree about the case that
     * could cost somebody something: neither can displace an item a person is
     * already working on, because 14b.1's promotion fires only into an idle area
     * and the reducer refuses to move a status here in either direction.
     */
    @Test
    fun `filing into an idle area queues the item rather than making it active`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-idea", null, "Look into the loft insulation", null, "a0", null))
        log.add(3, ItemFiled("item-idea", "area-work", "a1", "Work"))

        val state = log.state()
        val idea = state.items.getValue("item-idea")

        assertEquals("area-work", idea.areaId)
        assertEquals("a1", idea.orderKey)
        assertEquals(ItemStatus.QUEUED, idea.status)
        assertNull(idea.activeSince)
        // The area was idle and stayed idle.
        assertNull(state.activeItemIn("area-work"))
        assertEquals(listOf("item-idea"), state.queueIn("area-work").map { it.id })
        assertTrue(state.unfiledItems.isEmpty())
        assertTrue(state.diagnostics.isEmpty())
        assertTrue(ClarityInvariants.check(state).isEmpty())
    }

    /**
     * From the moment it is filed, an item is an item. Nothing downstream needs to
     * know where it came from.
     *
     * Two logs that differ only in how the item arrived in Work, run through the
     * same promotion, focus session and completion afterwards, and compared on the
     * item itself. `lastEventLamport` is normalized because the second log is one
     * event longer, which is the only difference the inbox is allowed to leave
     * behind.
     */
    @Test
    fun `a filed item behaves exactly like one added straight into the area`() {
        val straight = Scenario()
        straight.add(0, area("area-work", "Work"))
        straight.add(0, ItemAdded("item-a", "area-work", "Call the printer", null, "a1", "Work"))
        tail(straight)

        val viaInbox = Scenario()
        viaInbox.add(0, area("area-work", "Work"))
        viaInbox.add(0, ItemAdded("item-a", null, "Call the printer", null, "a0", null))
        viaInbox.add(0, ItemFiled("item-a", "area-work", "a1", "Work"))
        tail(viaInbox)

        val direct = straight.state()
        val filed = viaInbox.state()

        assertEquals(
            direct.items.getValue("item-a").copy(lastEventLamport = 0L),
            filed.items.getValue("item-a").copy(lastEventLamport = 0L),
        )
        assertEquals(ItemStatus.COMPLETED, filed.items.getValue("item-a").status)
        assertEquals(
            direct.focusSessions.getValue("focus-1").copy(lastEventLamport = 0L),
            filed.focusSessions.getValue("focus-1").copy(lastEventLamport = 0L),
        )
        assertTrue(filed.diagnostics.isEmpty())
        assertTrue(ClarityInvariants.check(filed).isEmpty())
    }

    /**
     * Adding time does not end the session and does not start a new one.
     * Addendum 01 4f.
     *
     * A session has exactly one FOCUS_STARTED and at most one terminal event, and an
     * extension is neither, so `started` stays one and the outcome stays RUNNING.
     * Ending a timer should not have to break flow to add ten minutes to it.
     */
    @Test
    fun `an extension moves the planned time without ending or restarting the session`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-a", "area-work", "Call the printer", null, "a0", "Work"))
        log.add(0, promotion("item-a", "area-work", "Call the printer"))
        log.add(1, FocusStarted("focus-1", "area-work", "item-a", 900))
        log.add(1, FocusExtended("focus-1", 600, 1500))

        val session = log.state().focusSessions.getValue("focus-1")
        assertEquals(1500, session.plannedSeconds)
        assertEquals(FocusOutcome.RUNNING, session.outcome)
        assertNull(session.endedAt)
        assertNull(session.actualSeconds)
        assertEquals(1, log.state().focusSessions.size)
        assertTrue(log.state().diagnostics.isEmpty())
    }

    /**
     * Two extensions accumulate. A second delivery of one does not.
     *
     * The payload states the absolute total rather than only the delta, and this is
     * the pair of assertions that is worth the extra field. Folding the delta would
     * make the replayed plan depend on how many times the event was delivered, and
     * after a merge of two files sharing an ancestor it always is delivered twice.
     * Nothing on screen would say that a session's planned time had drifted.
     */
    @Test
    fun `two extensions accumulate and a repeated delivery of one does not`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-a", "area-work", "Call the printer", null, "a0", "Work"))
        log.add(0, promotion("item-a", "area-work", "Call the printer"))
        log.add(1, FocusStarted("focus-1", "area-work", "item-a", 900))
        log.add(1, FocusExtended("focus-1", 600, 1500))
        log.add(1, FocusExtended("focus-1", 600, 2100))

        val once = log.state()
        assertEquals(2100, once.focusSessions.getValue("focus-1").plannedSeconds)

        val twice = ClarityReplay.replay(log.events() + log.events())
        assertEquals(2100, twice.focusSessions.getValue("focus-1").plannedSeconds)
        assertEquals(serialize(once), serialize(twice))

        // And the order the two extensions arrive in does not change the answer,
        // because each states where the plan ended up rather than how far it moved.
        assertEquals(
            serialize(once),
            serialize(ClarityReplay.replay(log.events().reversed())),
        )
    }

    /**
     * Extending a session that is already over is recorded rather than applied.
     *
     * Not the same case as a second terminal event, which is a benign duplicate the
     * reducer swallows. This one means the plan was changed for something that had
     * already finished, and quietly moving it would leave a session whose stated
     * plan it never actually ran under.
     */
    @Test
    fun `extending a finished session changes nothing and is recorded`() {
        val log = Scenario()
        log.add(0, area("area-work", "Work"))
        log.add(0, ItemAdded("item-a", "area-work", "Call the printer", null, "a0", "Work"))
        log.add(0, promotion("item-a", "area-work", "Call the printer"))
        log.add(1, FocusStarted("focus-1", "area-work", "item-a", 900))
        log.add(1, FocusCompleted("focus-1", 900))
        log.add(2, FocusExtended("focus-1", 600, 1500))

        val session = log.state().focusSessions.getValue("focus-1")
        assertEquals(900, session.plannedSeconds)
        assertEquals(FocusOutcome.COMPLETED, session.outcome)
        assertEquals(1, log.state().diagnostics.size)
        assertEquals(ClarityEventType.FOCUS_EXTENDED.name, log.state().diagnostics.single().eventType)
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

    // Fixtures ----------------------------------------------------------------

    /**
     * A hand written log, one lamport per event, ascending.
     *
     * Separate from `EventStreamGenerator` on purpose. The generator proves that a
     * property holds over a wide space of streams; these prove that one named
     * transition does one named thing, which a random stream can only reach by
     * accident and can never assert about.
     */
    private class Scenario(private val originId: String = "device-a") {
        private val recorded = mutableListOf<ClarityEvent>()
        private var lamport = 0L

        /** [day] is a whole day offset from a fixed start, which is display data only. */
        fun add(day: Long, payload: EventPayload): ClarityEvent {
            lamport += 1
            val built = ClarityEvent.of(
                id = "evt-$lamport",
                wallClock = EventStreamGenerator.START_MILLIS + day * 86_400_000L,
                lamport = lamport,
                originId = originId,
                payload = payload,
            )
            recorded += built
            return built
        }

        fun events(): List<ClarityEvent> = recorded.toList()

        fun state(): ClarityState = ClarityReplay.replay(recorded)
    }

    private fun area(areaId: String, name: String): EventPayload =
        AreaCreated(areaId, name, "#2D7FF9", "a0")

    private fun promotion(itemId: String, areaId: String, title: String): EventPayload =
        ItemPromoted(
            itemId = itemId,
            areaId = areaId,
            previousStatus = ItemStatus.QUEUED,
            demotedItemId = null,
            demotedToOrderKey = null,
            titleSnapshot = title,
            areaNameSnapshot = "Work",
        )

    /** The same life after the item is in Work, whichever way it got there. */
    private fun tail(log: Scenario) {
        log.add(4, promotion("item-a", "area-work", "Call the printer"))
        log.add(5, FocusStarted("focus-1", "area-work", "item-a", 900))
        log.add(5, FocusExtended("focus-1", 600, 1500))
        log.add(5, FocusCompleted("focus-1", 1500))
        log.add(6, ItemCompleted("item-a", "area-work", "Call the printer", "Work", 2))
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
