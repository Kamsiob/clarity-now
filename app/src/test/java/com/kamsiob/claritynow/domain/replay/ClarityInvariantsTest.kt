package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MASTER_BUILD_PROMPT 6.2 and 14b.1, Addendum 01 4a, DECISIONS.md C8.
 *
 * **An unfiled item can never be `ACTIVE` and never `COMPLETED`.** That is the one
 * rule that replaces every area scoped invariant for an item with no area, and it is
 * the rule this file exists to hold down.
 *
 * It matters more than it looks. `ACTIVE` means "the one thing happening in this
 * area", which is the philosophical work the whole app does, and an item that reached
 * it with no area would be the one thing happening in nowhere. The queue length facts,
 * the area rollups and every engine family in phase 5 read that state, so the failure
 * would not be a crash. It would be a wrong number in a sentence about somebody's own
 * life, months later, with nothing pointing back here.
 *
 * The attempt is constructed three ways on purpose, because the rule is enforced in
 * three places and any one of them could be removed by a later session without the
 * other two noticing: the reducer refuses the event, the repository refuses the call,
 * and [ClarityInvariants] reports the state if it somehow arrives anyway. The tests
 * below cover the reducer and the checker. The repository's guard is exercised by the
 * replay harness, which drives it, and is stated in its own KDoc.
 */
class ClarityInvariantsTest {

    private companion object {
        const val ORIGIN = "01947b3f-0000-4000-8000-00000000000a"

        /** The fixture occupies 1 and 2. Tests start at 11. */
        const val FIXTURE_LAMPORTS = 10L
    }

    /**
     * Events a test writes start well above the fixture's own lamports.
     *
     * Without the gap, an event built earlier in a test body than [capture] sorts
     * before it, because the total order is the lamport and the counter runs in
     * construction order. Every test here reads naturally as "here is the attempt,
     * now replay it against the fixture", and that reading would silently apply the
     * attempt to an empty log: the promotion refusal, for instance, reported an
     * unknown item rather than an unfiled one, and passed nothing.
     *
     * Construction order in a test body is a readability choice. It must not be a
     * semantic one.
     */
    private var lamport = FIXTURE_LAMPORTS

    private fun event(payload: EventPayload): ClarityEvent {
        lamport += 1
        return ClarityEvent.of(
            id = "evt-$lamport",
            wallClock = 1_772_000_000_000L + lamport,
            lamport = lamport,
            originId = ORIGIN,
            payload = payload,
        )
    }

    /** A fixture event at a fixed lamport, below everything a test builds. */
    private fun fixture(at: Long, payload: EventPayload): ClarityEvent = ClarityEvent.of(
        id = "fixture-$at",
        wallClock = 1_772_000_000_000L + at,
        lamport = at,
        originId = ORIGIN,
        payload = payload,
    )

    /** One area, and one thought written down with no area attached to it. */
    private fun capture(): List<ClarityEvent> = listOf(
        fixture(1, AreaCreated("area-work", "Work", "#2D7FF9", "a0")),
        fixture(
            2,
            ItemAdded(
                itemId = "item-idea",
                areaId = null,
                title = "Look into the loft insulation",
                note = null,
                orderKey = "a0",
                areaNameSnapshot = null,
                estimateMinutes = 90,
                firstStep = "Find last winter heating bill",
            ),
        ),
    )

    // The attempt, refused --------------------------------------------------

    @Test
    fun `promoting an unfiled item is refused and recorded`() {
        // The construction the acceptance criteria ask for: a promotion aimed at an
        // item that has no area. Nothing in the app can produce this event, which is
        // exactly why it is worth writing by hand. A second device on an older build,
        // a hand edited import or a future bug all arrive here as a foreign event.
        val attempt = event(
            ItemPromoted(
                itemId = "item-idea",
                areaId = "area-work",
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = null,
                demotedToOrderKey = null,
                titleSnapshot = "Look into the loft insulation",
                areaNameSnapshot = "Work",
            ),
        )
        val state = ClarityReplay.replay(capture() + attempt)

        val item = state.items.getValue("item-idea")
        assertEquals("an unfiled item was promoted", ItemStatus.QUEUED, item.status)
        assertNull("promotion gave an unfiled item an area", item.areaId)
        assertNull("the item became active in a real area", state.activeItemIn("area-work"))
        assertNull("the item became active anywhere", item.activeSince)

        // Refused, never thrown, and never silent. An event the reducer cannot apply
        // becomes a diagnostic so a rebuild can say what it dropped and why.
        val diagnostic = state.diagnostics.firstOrNull { it.eventId == attempt.id }
        assertNotNull("the refusal left no diagnostic", diagnostic)
        assertTrue(
            "the diagnostic does not say why: ${diagnostic?.reason}",
            diagnostic?.reason?.contains("unfiled") == true,
        )
        ClarityInvariants.assertHolds(state)
    }

    @Test
    fun `completing an unfiled item is refused`() {
        // Completion is guarded a second time, by the rule that only an ACTIVE item
        // can be completed. Both guards are checked here because an unfiled item is
        // the one case where removing either would leave the other looking sufficient.
        val attempt = event(
            ItemCompleted(
                itemId = "item-idea",
                areaId = "area-work",
                titleSnapshot = "Look into the loft insulation",
                areaNameSnapshot = "Work",
                activeDurationDays = 0,
            ),
        )
        val state = ClarityReplay.replay(capture() + attempt)

        val item = state.items.getValue("item-idea")
        assertEquals("an unfiled item was completed", ItemStatus.QUEUED, item.status)
        assertNull("a completion stamp was written", item.completedAt)
        assertTrue("the item left the inbox", state.unfiledItems.any { it.id == "item-idea" })
        assertNotNull(state.diagnostics.firstOrNull { it.eventId == attempt.id })
        ClarityInvariants.assertHolds(state)
    }

    @Test
    fun `the invariant reports an unfiled active item if one ever reaches the state`() {
        // The reducer refuses to produce this, so the only honest way to prove the
        // checker sees it is to build it by hand. Without this test the two guards
        // above could both be deleted and every test in the suite would still pass:
        // a checker nothing can trip is a checker nobody can trust.
        val state = ClarityReplay.replay(capture())
        val forced = state.copy(
            items = state.items + (
                "item-idea" to state.items.getValue("item-idea").copy(
                    status = ItemStatus.ACTIVE,
                    activeSince = 1_772_000_000_000L,
                )
                ),
        )

        val violations = ClarityInvariants.check(forced)
        assertEquals(
            "expected exactly the unfiled rule to fire, got ${violations.map { it.rule }}",
            listOf("an unfiled item is never active or completed"),
            violations.map { it.rule },
        )
    }

    @Test
    fun `the invariant reports an unfiled completed item`() {
        val state = ClarityReplay.replay(capture())
        val forced = state.copy(
            items = state.items + (
                "item-idea" to state.items.getValue("item-idea").copy(
                    status = ItemStatus.COMPLETED,
                    completedAt = 1_772_000_000_000L,
                )
                ),
        )

        assertEquals(
            listOf("an unfiled item is never active or completed"),
            ClarityInvariants.check(forced).map { it.rule },
        )
    }

    // What an unfiled item is allowed to be ---------------------------------

    @Test
    fun `an unfiled item is outside every area scoped projection`() {
        val state = ClarityReplay.replay(capture())

        assertEquals(listOf("item-idea"), state.unfiledItems.map { it.id })
        assertTrue("an unfiled item joined an area queue", state.queueIn("area-work").isEmpty())
        assertTrue("an unfiled item joined an area", state.liveItemsIn("area-work").isEmpty())
        assertTrue(state.completedIn("area-work").isEmpty())
        assertNull(state.activeItemIn("area-work"))
        ClarityInvariants.assertHolds(state)
    }

    @Test
    fun `an unfiled item keeps its first step and its estimate`() {
        // Both are optional forever and both survive the one transition an unfiled
        // item has. Filing moves the area and the order key and touches nothing else,
        // which is what makes it bookkeeping rather than an edit.
        val filed = event(ItemFiled("item-idea", "area-work", "a1", "Work"))
        val state = ClarityReplay.replay(capture() + filed)

        val item = state.items.getValue("item-idea")
        assertEquals("area-work", item.areaId)
        assertEquals("Find last winter heating bill", item.firstStep)
        assertEquals(90, item.estimateMinutes)
    }

    @Test
    fun `filing is the only way an item becomes active`() {
        // The same promotion that was refused above is accepted once the item has an
        // area, which is the point: the rule is about the missing area and not about
        // the item, and filing is what removes the obstacle.
        val events = capture() + listOf(
            event(ItemFiled("item-idea", "area-work", "a1", "Work")),
            event(
                ItemPromoted(
                    itemId = "item-idea",
                    areaId = "area-work",
                    previousStatus = ItemStatus.QUEUED,
                    demotedItemId = null,
                    demotedToOrderKey = null,
                    titleSnapshot = "Look into the loft insulation",
                    areaNameSnapshot = "Work",
                ),
            ),
        )
        val state = ClarityReplay.replay(events)

        assertEquals("item-idea", state.activeItemIn("area-work")?.id)
        assertTrue("a filed item stayed in the inbox", state.unfiledItems.isEmpty())
        ClarityInvariants.assertHolds(state)
    }

    @Test
    fun `an item deleted while still unfiled leaves the inbox and no trace in an area`() {
        // Addendum 01 4a: deleting is one of the three things an unfiled item may do,
        // and `ItemDeleted.areaId` is nullable precisely so this path does not have to
        // invent an area to name. A tombstone, never a row removal: the Trail entry
        // for the capture still has to render next March.
        val deleted = event(ItemDeleted("item-idea", null, "Look into the loft insulation"))
        val state = ClarityReplay.replay(capture() + deleted)

        val item = state.items.getValue("item-idea")
        assertNotNull("the delete removed the row instead of tombstoning it", item.deletedAt)
        assertTrue("a deleted item is still in the inbox", state.unfiledItems.isEmpty())
        assertTrue(state.queueIn("area-work").isEmpty())
        assertFalse("the delete produced a diagnostic", state.diagnostics.any { it.eventId == deleted.id })
        ClarityInvariants.assertHolds(state)
    }

    @Test
    fun `deleting an area does not orphan its items into the inbox`() {
        // The choice recorded in `DECISIONS.md` for this phase, asserted so that
        // reversing it is a deliberate act rather than a side effect.
        //
        // The obvious answer is to orphan, because nothing is lost that way. It loses
        // anyway: the delete already carries a typed confirmation and copy saying the
        // area and everything in it goes, and thirty queued items reappearing in the
        // inbox would contradict a sentence the person read and typed DELETE against.
        // Archive is the non destructive path and it says so.
        val events = capture() + listOf(
            event(ItemAdded("item-proposal", "area-work", "Rewrite the proposal intro", null, "a0", "Work")),
            event(AreaDeleted("area-work", "Work")),
        )
        val state = ClarityReplay.replay(events)

        assertNotNull("the area's item survived the delete", state.items.getValue("item-proposal").deletedAt)
        assertEquals(
            "deleting an area orphaned its items into the inbox",
            listOf("item-idea"),
            state.unfiledItems.map { it.id },
        )
        ClarityInvariants.assertHolds(state)
    }
}
