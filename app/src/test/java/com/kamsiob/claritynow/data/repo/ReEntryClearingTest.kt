package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.query.complete
import com.kamsiob.claritynow.domain.query.item
import com.kamsiob.claritynow.domain.query.promote
import com.kamsiob.claritynow.domain.query.unfiled
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The re-entry screen's second choice, and the one thing it must never do.
 * MASTER_BUILD_PROMPT 14b.4.
 *
 * > **Clearing demotes**: each active item returns to the head of its own queue with
 * > `ITEM_QUEUED`. Nothing is deleted, nothing is completed, and the wording says so.
 * > The obvious implementation of a fresh start is a delete, and a delete of a person's
 * > own work on the day they came back is the single most expensive thing this app
 * > could do.
 *
 * Half of that is held by the compiler: [activeItemsBackInTheirQueues] returns
 * `List<ItemQueued>`, so there is no expression in it that could produce a deletion or
 * a completion. What this file adds is the other half, which is that the events it does
 * produce land where the sentence says they land, and that folding them changes nothing
 * else at all: the same items, the same areas, the same completions, and every queue
 * holding what it held plus the item that was on top of it.
 */
class ReEntryClearingTest {

    private val jitter = "Zx"

    /**
     * Three live areas, one archived, and an inbox. Built as a log and folded rather
     * than written as a state literal, because everything this function reads, the
     * status, the order keys and which area an item is in, is a product of the fold and
     * a fixture that skipped it would be testing an arrangement that never happens.
     *
     * - **Work** is busy: one active item and two queued behind it, plus one completed
     * - **Home** is busy with an empty queue, which is the case where there is no head
     *   to sort before
     * - **Quiet** is idle with one queued item, and has nothing to demote
     * - **Archived** holds an active item nobody can see
     * - the inbox holds one unfiled item, which can never be active at all
     */
    private fun state(): ClarityState {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work", orderKey = "a0")
        log.area(at(0, 8), "area-home", "Home", orderKey = "a1")
        log.area(at(0, 8), "area-quiet", "Quiet", orderKey = "a2")
        log.area(at(0, 8), "area-archived", "Archived", orderKey = "a3")

        log.item(at(0, 9), "w-active", "area-work", "Draft the letter", orderKey = "a0")
        log.item(at(0, 9), "w-queued-1", "area-work", "Call the surgery", orderKey = "a1")
        log.item(at(0, 9), "w-queued-2", "area-work", "Read the report", orderKey = "a2")
        log.item(at(0, 9), "w-done", "area-work", "Book the ticket", orderKey = "a3")
        log.promote(at(0, 10), "w-done", "area-work", "Book the ticket")
        log.complete(at(0, 11), "w-done", "area-work", "Book the ticket")
        log.promote(at(0, 12), "w-active", "area-work", "Draft the letter")

        log.item(at(0, 9), "h-active", "area-home", "Water the plants", orderKey = "a0", areaName = "Home")
        log.promote(at(0, 10), "h-active", "area-home", "Water the plants", areaName = "Home")

        log.item(at(0, 9), "q-queued", "area-quiet", "Someday", orderKey = "a0", areaName = "Quiet")

        log.item(at(0, 9), "x-active", "area-archived", "Old thing", orderKey = "a0", areaName = "Archived")
        log.promote(at(0, 10), "x-active", "area-archived", "Old thing", areaName = "Archived")
        log.add(at(0, 13), AreaArchived("area-archived", "Archived"))

        log.unfiled(at(0, 9), "inbox-1", "A thought")

        return ClarityReplay.replay(log.events())
    }

    /** The state after the choice, folded through the reducer the app itself uses. */
    private fun afterClearing(before: ClarityState): ClarityState {
        val log = TrailTestLog()
        activeItemsBackInTheirQueues(before, jitter).forEach { log.add(at(1, 9), it) }
        return log.events().fold(before, ClarityReducer::apply)
    }

    /**
     * **Every event is an `ITEM_QUEUED` and there is nothing else in the list.**
     *
     * Asserted through `ClarityEvent`'s own type mapping rather than by reading the
     * Kotlin type, so what is checked is the row that would be written to the log and
     * read back by a second implementation from `docs/EVENT_FORMAT.md`.
     */
    @Test
    fun `clearing writes ITEM_QUEUED and nothing else`() {
        val log = TrailTestLog()
        activeItemsBackInTheirQueues(state(), jitter).forEach { log.add(at(1, 9), it) }

        assertEquals(
            listOf(ClarityEventType.ITEM_QUEUED, ClarityEventType.ITEM_QUEUED),
            log.events().map { it.type },
        )
        assertTrue(
            "a deletion or a completion reached the log",
            log.events().none {
                it.type == ClarityEventType.ITEM_DELETED || it.type == ClarityEventType.ITEM_COMPLETED
            },
        )
    }

    /**
     * One event per active item in a live area, and none for anything else.
     *
     * The idle area has nothing to demote, the inbox item cannot be active at all, per
     * Addendum 01 4a, and the archived area's item is deliberately left where it is:
     * it is on no screen, so the fresh start the person asked for does not include it,
     * and unarchiving later hands back what was there.
     */
    @Test
    fun `only the active items of live areas are demoted`() {
        val demotions = activeItemsBackInTheirQueues(state(), jitter)

        assertEquals(listOf("w-active", "h-active"), demotions.map { it.itemId })
        assertEquals(listOf("area-work", "area-home"), demotions.map { it.areaId })
        assertTrue(demotions.all { it.previousStatus == ItemStatus.ACTIVE })
    }

    /** Nothing to clear writes nothing, so the choice is free on a fresh start. */
    @Test
    fun `a person with no active items produces no events`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-quiet", "Quiet")
        log.item(at(0, 9), "q-queued", "area-quiet", "Someday", areaName = "Quiet")

        assertEquals(
            emptyList<Any>(),
            activeItemsBackInTheirQueues(ClarityReplay.replay(log.events()), jitter),
        )
    }

    /**
     * **Every demoted item lands at the head of its own queue**, both where there was a
     * queue to sort before and where there was not.
     *
     * The key is checked after the fold rather than as a string, because what 14b.4
     * asks for is a position in a queue and the queue is what the reducer produces.
     */
    @Test
    fun `every demoted item is the head of its own queue afterwards`() {
        val after = afterClearing(state())

        assertEquals(
            listOf("w-active", "w-queued-1", "w-queued-2"),
            after.queueIn("area-work").map { it.id },
        )
        assertEquals(listOf("h-active"), after.queueIn("area-home").map { it.id })
        assertNull("the area is idle afterwards", after.activeItemIn("area-work"))
        assertNull("the area is idle afterwards", after.activeItemIn("area-home"))
    }

    /**
     * **Nothing is deleted and nothing is completed**, stated against the whole
     * projection rather than against the events.
     *
     * The item count is unchanged, no tombstone appears, the one item that was already
     * completed is still completed and no second one has joined it, and the two things
     * the choice never touches, the idle area's queue and the inbox, are exactly as they
     * were.
     */
    @Test
    fun `nothing is deleted, nothing is completed, and nothing else moves`() {
        val before = state()
        val after = afterClearing(before)

        assertEquals(before.items.size, after.items.size)
        assertTrue("a tombstone appeared", after.items.values.none { it.deletedAt != null })
        assertEquals(
            before.items.values.filter { it.status == ItemStatus.COMPLETED }.map { it.id }.sorted(),
            after.items.values.filter { it.status == ItemStatus.COMPLETED }.map { it.id }.sorted(),
        )
        assertEquals(listOf("q-queued"), after.queueIn("area-quiet").map { it.id })
        assertEquals(listOf("inbox-1"), after.unfiledItems.map { it.id })
        assertEquals(
            "the archived area keeps the item nobody could see",
            "x-active",
            after.activeItemIn("area-archived")?.id,
        )
    }

    /**
     * The demoted item keeps its title, its area and everything else about it. A fresh
     * start is a position change, and the only fields that move are the ones the
     * reducer's `ITEM_QUEUED` branch names.
     */
    @Test
    fun `a demoted item is the same item in a different place`() {
        val before = state()
        val after = afterClearing(before)
        val was = before.items.getValue("w-active")
        val now = after.items.getValue("w-active")

        assertEquals(was.title, now.title)
        assertEquals(was.areaId, now.areaId)
        assertEquals(was.note, now.note)
        assertEquals(was.estimateMinutes, now.estimateMinutes)
        assertEquals(ItemStatus.QUEUED, now.status)
        assertNull("an item in a queue is not being worked on", now.activeSince)

        // The key it took is a new one and it is nobody else's. An active item and the
        // queue share one ordering space, per `ClarityState.liveItemsIn`, and two rows
        // claiming one position is the defect that shipped in 0.2.0 and surfaces much
        // later, as an exception the first time somebody drags something. It is not
        // asserted to be lower than the key it had: the head of a queue is sorted
        // before its neighbors rather than before its own past.
        val keys = after.liveItemsIn("area-work").map { it.orderKey }
        assertEquals("two items in one area hold one key", keys.size, keys.distinct().size)
        assertTrue(
            "it sorts before the queue it rejoined",
            now.orderKey < after.items.getValue("w-queued-1").orderKey,
        )
    }
}
