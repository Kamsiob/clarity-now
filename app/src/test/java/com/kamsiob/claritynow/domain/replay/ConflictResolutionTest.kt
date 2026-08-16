package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MASTER_BUILD_PROMPT 6.3. Every divergence has one documented answer, and it is
 * tested now rather than when sync ships, because the reducer is what implements it.
 */
class ConflictResolutionTest {

    private var lamport = 0L

    private fun event(originId: String, payload: EventPayload, lamportOverride: Long? = null): ClarityEvent {
        lamport = lamportOverride ?: (lamport + 1)
        return ClarityEvent.of(
            id = "$originId-$lamport-${payload::class.simpleName}",
            wallClock = 1_772_000_000_000L + lamport,
            lamport = lamport,
            originId = originId,
            payload = payload,
        )
    }

    private fun baseline(): List<ClarityEvent> = listOf(
        event("a", AreaCreated("area-1", "Work", "#2D7FF9", "a0")),
        event("a", ItemAdded("item-1", "area-1", "Call the printer", null, "a0", "Work")),
        event("a", ItemAdded("item-2", "area-1", "Rewrite the proposal intro", null, "a1", "Work")),
    )

    // Two active in one area ---------------------------------------------------

    @Test
    fun `two devices promoting in the same area resolve to one active item`() {
        val setup = baseline()
        // Both devices, unaware of each other, promote a different item at the same
        // logical time. The tiebreak is originId, so device-b wins.
        val fromA = event(
            "device-a",
            ItemPromoted("item-1", "area-1", ItemStatus.QUEUED, null, null, "Call the printer", "Work"),
            lamportOverride = 10,
        )
        val fromB = event(
            "device-b",
            ItemPromoted("item-2", "area-1", ItemStatus.QUEUED, null, null, "Rewrite the proposal intro", "Work"),
            lamportOverride = 10,
        )

        val merged = ClarityReplay.replay(setup + listOf(fromA, fromB))
        val reversed = ClarityReplay.replay(setup + listOf(fromB, fromA))

        assertEquals("item-2", merged.activeItemIn("area-1")?.id)
        assertEquals(merged.activeItemIn("area-1")?.id, reversed.activeItemIn("area-1")?.id)

        // The loser is not lost. It sits at the head of the queue.
        assertEquals(listOf("item-1"), merged.queueIn("area-1").map { it.id })
        assertTrue(ClarityInvariants.check(merged).isEmpty())
    }

    @Test
    fun `a resolved promotion conflict is recorded rather than swallowed`() {
        val setup = baseline()
        val fromA = event(
            "device-a",
            ItemPromoted("item-1", "area-1", ItemStatus.QUEUED, null, null, "Call the printer", "Work"),
            lamportOverride = 10,
        )
        val fromB = event(
            "device-b",
            ItemPromoted("item-2", "area-1", ItemStatus.QUEUED, null, null, "Rewrite the proposal intro", "Work"),
            lamportOverride = 10,
        )
        val state = ClarityReplay.replay(setup + listOf(fromA, fromB))

        val conflict = state.conflicts.single()
        assertEquals(ConflictKind.TWO_ACTIVE_IN_AREA, conflict.kind)
        assertEquals("item-2", conflict.winnerId)
        assertEquals("item-1", conflict.loserId)
        assertEquals("Work", conflict.areaNameSnapshot)
        // Everything the surfaced sentence needs is on the record itself, so the
        // card can be written without reading a live entity.
        assertEquals("Rewrite the proposal intro", conflict.winnerTitleSnapshot)
        assertEquals("Call the printer", conflict.loserTitleSnapshot)
    }

    @Test
    fun `an ordinary swap is not a conflict`() {
        val setup = baseline() + event(
            "device-a",
            ItemPromoted("item-1", "area-1", ItemStatus.QUEUED, null, null, "Call the printer", "Work"),
        )
        val swap = event(
            "device-a",
            ItemPromoted(
                itemId = "item-2",
                areaId = "area-1",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = "item-1",
                demotedToOrderKey = "Zz",
                titleSnapshot = "Rewrite the proposal intro",
                areaNameSnapshot = "Work",
            ),
        )
        val state = ClarityReplay.replay(setup + swap)

        assertEquals("item-2", state.activeItemIn("area-1")?.id)
        assertEquals(listOf("item-1"), state.queueIn("area-1").map { it.id })
        assertTrue("a swap must never look like a conflict", state.conflicts.isEmpty())
    }

    // Edit versus delete -------------------------------------------------------

    @Test
    fun `delete wins over a later edit`() {
        val setup = baseline()
        val deleted = event("device-a", ItemDeleted("item-1", "area-1", "Call the printer"), lamportOverride = 20)
        val edited = event(
            "device-b",
            ItemEdited("item-1", "Call the printer", "Call the printer twice", null, null),
            lamportOverride = 21,
        )
        val state = ClarityReplay.replay(setup + listOf(deleted, edited))

        val item = state.items.getValue("item-1")
        assertNotNull("the tombstone stays", item.deletedAt)
        assertEquals("the edit had no effect", "Call the printer", item.title)
        assertTrue(state.diagnostics.any { it.eventId == edited.id })
    }

    @Test
    fun `deleting an area tombstones the items inside it`() {
        val state = ClarityReplay.replay(
            baseline() + event("device-a", AreaDeleted("area-1", "Work")),
        )
        assertTrue(state.liveAreas.isEmpty())
        assertTrue(state.items.values.all { it.deletedAt != null })
        assertTrue(state.queueIn("area-1").isEmpty())
    }

    // Concurrent reorder -------------------------------------------------------

    @Test
    fun `two devices reordering the same queue both survive`() {
        val setup = baseline() +
            event("device-a", ItemAdded("item-3", "area-1", "Book the dentist", null, "a2", "Work"))

        val fromA = event(
            "device-a",
            ItemReordered("item-3", "area-1", "a2", OrderKey.between(null, "a0", OrderKey.jitterFor("device-a"))),
            lamportOverride = 30,
        )
        val fromB = event(
            "device-b",
            ItemReordered("item-2", "area-1", "a1", OrderKey.between(null, "a0", OrderKey.jitterFor("device-b"))),
            lamportOverride = 30,
        )

        val forwards = ClarityReplay.replay(setup + listOf(fromA, fromB))
        val backwards = ClarityReplay.replay(setup + listOf(fromB, fromA))

        assertEquals(
            forwards.queueIn("area-1").map { it.id },
            backwards.queueIn("area-1").map { it.id },
        )
        assertEquals(3, forwards.queueIn("area-1").size)
        assertTrue(ClarityInvariants.check(forwards).isEmpty())
    }

    // Duplicate date keyed rows ------------------------------------------------

    @Test
    fun `two pulses for one day resolve to the higher ordered one`() {
        fun pulse(id: String) = PulseGenerated(
            pulseId = id,
            dateKey = "2026-03-14",
            family = "persistence",
            escalationStage = 2,
            register = "REFLECTIVE",
            variantKey = "persistence.s2.$id",
            renderedObservation = "Still there.",
            renderedQuestion = "Deep work, or stuck?",
            factSnapshot = mapOf("activeItemAgeDays" to "9"),
            reflectionPeriod = ReflectionPeriod.YESTERDAY,
        )

        val fromA = event("device-a", pulse("pulse-a"), lamportOverride = 40)
        val fromB = event("device-b", pulse("pulse-b"), lamportOverride = 40)

        val forwards = ClarityReplay.replay(baseline() + listOf(fromA, fromB))
        val backwards = ClarityReplay.replay(baseline() + listOf(fromB, fromA))

        assertEquals(1, forwards.pulses.size)
        assertEquals("pulse-b", forwards.pulses.getValue("2026-03-14").id)
        assertEquals(forwards.pulses, backwards.pulses)
        assertEquals(ConflictKind.DUPLICATE_DATE_KEY, forwards.conflicts.single().kind)
    }

    // Unknown entities ---------------------------------------------------------

    @Test
    fun `an event about an entity this device has never seen is noted, not fatal`() {
        val state = ClarityReplay.replay(
            baseline() + event("device-z", ItemCompleted("ghost", "area-9", "Nothing", "Nowhere", 1)),
        )
        assertEquals(1, state.diagnostics.size)
        assertEquals("ITEM_COMPLETED", state.diagnostics.single().eventType)
        assertTrue(ClarityInvariants.check(state).isEmpty())
    }

    @Test
    fun `only an active item can be completed`() {
        val state = ClarityReplay.replay(
            baseline() + event("device-a", ItemCompleted("item-2", "area-1", "Rewrite", "Work", 1)),
        )
        assertEquals(ItemStatus.QUEUED, state.items.getValue("item-2").status)
        assertTrue(state.diagnostics.single().reason.contains("only an active item"))
    }

    @Test
    fun `completing twice is harmless`() {
        val setup = baseline() +
            event("device-a", ItemPromoted("item-1", "area-1", ItemStatus.QUEUED, null, null, "Call", "Work"))
        val completion = event("device-a", ItemCompleted("item-1", "area-1", "Call", "Work", 2))
        val once = ClarityReplay.replay(setup + completion)
        val twice = ClarityReplay.replay(setup + completion + completion.copy(id = "second", lamport = 99))

        assertEquals(ItemStatus.COMPLETED, twice.items.getValue("item-1").status)
        assertEquals(
            once.items.getValue("item-1").completedAt,
            twice.items.getValue("item-1").completedAt,
        )
        assertNull(twice.activeItemIn("area-1"))
    }
}
