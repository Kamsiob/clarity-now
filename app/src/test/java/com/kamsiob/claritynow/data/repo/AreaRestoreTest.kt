package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.domain.query.TrailTestLog
import com.kamsiob.claritynow.domain.query.area
import com.kamsiob.claritynow.domain.query.at
import com.kamsiob.claritynow.domain.replay.ClarityReplay
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.OrderKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The order key an area gets back when it comes out of the archive. Issue #15.
 *
 * Two separate claims are checked here and they fail in different ways.
 *
 * **The first is that a restored area lands where it was**, which is the behavior
 * design-v3.md 10.20 chose over the end of the list. It is not implemented by putting it
 * back: it is implemented by nothing ever taking its key while it is away, so the claim
 * is really that [restoredOrderKey] has nothing to say in the ordinary case. If that
 * ever stopped being true the area would simply reappear somewhere else, which is a
 * disappointment rather than a crash, and no other test in this repository would notice.
 *
 * **The second is that no two live areas ever hold one key**, which is not a
 * disappointment. It is silent when it is made, because the list still sorts and the id
 * breaks the tie, and it surfaces later as an `IllegalArgumentException` out of
 * `OrderKey.between` the first time somebody drags an area between the two of them.
 * That is exactly the defect `OrderKeyCollisionTest` holds for items: it shipped in
 * 0.2.0 and it took the replay harness to find. The last two tests here build the
 * collision on purpose, which this app's own writers cannot produce, and check that
 * restoring resolves it rather than handing it to a future drag.
 */
class AreaRestoreTest {

    private val jitter = OrderKey.jitterFor("device-a")

    /**
     * Three areas in a hand chosen order, with the middle one archived.
     *
     * Built as a log and folded rather than written as a state literal, because the
     * order keys, the archived flag and the display order are all products of the fold
     * and a fixture that skipped it would be testing an arrangement that never happens.
     */
    private fun archivedInTheMiddle(): TrailTestLog {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work", orderKey = "a0")
        log.area(at(0, 8), "area-home", "Home", orderKey = "a1")
        log.area(at(0, 8), "area-quiet", "Quiet", orderKey = "a2")
        log.add(at(1, 9), AreaArchived("area-home", "Home"))
        return log
    }

    private fun TrailTestLog.state(): ClarityState = ClarityReplay.replay(events())

    private fun ClarityState.liveIds(): List<String> = liveAreas.map { it.id }

    @Test
    fun `an archived area needs no new key, because nothing was allowed to take its own`() {
        val state = archivedInTheMiddle().state()
        assertEquals(listOf("area-work", "area-quiet"), state.liveIds())
        assertNull(
            "The ordinary restore moves nothing. An archived area keeps its key and " +
                "every writer in ClarityRepository chooses against the restorable set " +
                "rather than the visible one, so there is nothing standing on it.",
            restoredOrderKey(state, "area-home", jitter),
        )
    }

    @Test
    fun `restoring puts the area back between the same two neighbors`() {
        val log = archivedInTheMiddle()
        log.add(at(2, 9), AreaUnarchived("area-home", "Home"))
        val restored = log.state()

        assertEquals(
            "Back where it was, not at the end. design-v3.md 10.20: the person most " +
                "likely to be on that screen archived something by accident, and the " +
                "end of the list is a second thing for them to undo.",
            listOf("area-work", "area-home", "area-quiet"),
            restored.liveIds(),
        )
        assertEquals("a1", restored.areas.getValue("area-home").orderKey)
    }

    /**
     * A log this app did not write. `OrderKey.between` drops its jitter when the
     * jittered key would sort past the upper bound, which is the one path by which two
     * devices merging their logs can arrive at one key without either of them doing
     * anything wrong, and a hand edited import can produce anything at all.
     */
    private fun archivedOnATakenKey(): TrailTestLog {
        val log = archivedInTheMiddle()
        // The offender: a live area on the archived one's key.
        log.area(at(2, 8), "area-imported", "Imported", orderKey = "a1")
        return log
    }

    @Test
    fun `a key a live area is standing on is not handed back a second time`() {
        val state = archivedOnATakenKey().state()
        val key = restoredOrderKey(state, "area-home", jitter)

        assertNotNull("The key is taken, so restoring has to choose another", key)
        val chosen = requireNotNull(key)
        assertTrue("$chosen should sort after the area holding a1", chosen > "a1")
        assertTrue("$chosen should sort before the next area, a2", chosen < "a2")
        assertTrue(
            "$chosen collides with a live area all over again",
            state.liveAreas.none { it.orderKey == chosen },
        )
    }

    @Test
    fun `after a collision is resolved every neighboring pair can still be asked for a key`() {
        val log = archivedOnATakenKey()
        val moved = requireNotNull(restoredOrderKey(log.state(), "area-home", jitter))
        // The two payloads ClarityRepository.unarchiveArea hands to one commit, in the
        // order it hands them: the area is never live on the colliding key, not even
        // for the one fold between them.
        log.add(at(3, 9), AreaReordered("area-home", "a1", moved))
        log.add(at(3, 9), AreaUnarchived("area-home", "Home"))
        val restored = log.state()

        assertEquals(
            listOf("area-work", "area-imported", "area-home", "area-quiet"),
            restored.liveIds(),
        )
        // The assertion that matters. A drag asks for a key between two live
        // neighbors, and that is the call that throws when two of them share one.
        restored.liveAreas.zipWithNext().forEach { (below, above) ->
            OrderKey.between(below.orderKey, above.orderKey, jitter)
        }
    }

    @Test
    fun `a collision at the end of the list has no upper bound and still resolves`() {
        val log = TrailTestLog()
        log.area(at(0, 8), "area-work", "Work", orderKey = "a0")
        log.area(at(0, 8), "area-home", "Home", orderKey = "a1")
        log.add(at(1, 9), AreaArchived("area-home", "Home"))
        log.area(at(2, 8), "area-imported", "Imported", orderKey = "a1")
        val state = ClarityReplay.replay(log.events())

        val chosen = requireNotNull(restoredOrderKey(state, "area-home", jitter))
        assertTrue("$chosen should sort after the area holding a1", chosen > "a1")
        assertTrue(
            "$chosen collides with a live area all over again",
            state.liveAreas.none { it.orderKey == chosen },
        )
    }
}
