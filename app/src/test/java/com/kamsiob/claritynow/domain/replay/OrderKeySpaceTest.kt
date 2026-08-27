package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ItemStatus
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No two entities that share an ordering space ever hold the same key.
 *
 * This is the property behind a class of defect that shipped in 0.2.0 and was found
 * here in August 2026. Every instance had the same shape: a key was chosen against
 * the entities currently in view rather than against every entity that can occupy
 * the space.
 *
 * The two spaces, and what each one hides:
 *
 * - **An area's items.** The active item holds a key in the same space as the queue
 *   but is not a member of the queue. Bounds taken from queue neighbors can enclose
 *   it, and at either end of the queue one bound is null and encloses everything. It
 *   cannot be assumed to sit below the whole queue either: promotion from the head
 *   leaves it there, but a swap promotes whichever item the person chose.
 * - **Areas.** An archived area keeps its key. Archiving is reversible, so the key is
 *   not free, and unarchiving returns the area to the live list still holding it.
 *
 * What makes the class worth a property test rather than a unit test is the distance
 * between cause and symptom. A duplicate key does nothing when it is made: the list
 * still renders, the tie broken by id. It surfaces much later as an exception out of
 * `OrderKey.between`, on the first drag that asks for a key between the two of them,
 * in a session that did nothing wrong. The seeds below reach it in about one run in
 * forty, which is often enough to be a real user's Tuesday and rare enough that no
 * amount of hand testing would have found it.
 */
class OrderKeySpaceTest {

    @Test
    fun `no two live items in an area ever hold the same order key`() {
        forEachGeneratedState { seed, state ->
            val offenders = state.items.values
                .filter {
                    it.deletedAt == null &&
                        (it.status == ItemStatus.ACTIVE || it.status == ItemStatus.QUEUED)
                }
                .groupBy { it.areaId }
                .flatMap { (areaId, live) ->
                    live.groupBy { it.orderKey }
                        .filterValues { it.size > 1 }
                        .map { (key, items) ->
                            "seed $seed area $areaId key $key held by " +
                                items.joinToString { "${it.id} (${it.status})" }
                        }
                }
            assertTrue(offenders.joinToString("\n"), offenders.isEmpty())
        }
    }

    @Test
    fun `no two restorable areas ever hold the same order key`() {
        forEachGeneratedState { seed, state ->
            // Archived areas included. A tombstoned one is excluded, because a
            // tombstone never comes back and its key is genuinely free.
            val offenders = state.areas.values
                .filter { it.deletedAt == null }
                .groupBy { it.orderKey }
                .filterValues { it.size > 1 }
                .map { (key, areas) ->
                    "seed $seed key $key held by " + areas.joinToString { it.id }
                }
            assertTrue(offenders.joinToString("\n"), offenders.isEmpty())
        }
    }

    /**
     * Generation itself is half the proof. The generator computes keys the way the
     * repository does, so if a key can be minted onto an occupied slot, `between`
     * throws here before any assertion runs. That is how this was first caught.
     */
    @Test
    fun `generating a long stream never asks for a key between two identical keys`() {
        for (seed in 1L..80L) {
            val generator = EventStreamGenerator(seed, "device-a")
            generator.generate(140)
            val left = generator.fork(seed * 31 + 1, "device-a")
            val right = generator.fork(seed * 17 + 5, "device-b")
            left.generate(60)
            right.generate(60)
        }
    }

    private fun forEachGeneratedState(check: (Long, ClarityState) -> Unit) {
        for (seed in 1L..80L) {
            val generator = EventStreamGenerator(seed, "device-a")
            generator.generate(140)
            check(seed, ClarityReplay.replay(generator.events))

            val left = generator.fork(seed * 31 + 1, "device-a")
            val right = generator.fork(seed * 17 + 5, "device-b")
            left.generate(60)
            right.generate(60)
            val shared = generator.events
            val merged = shared +
                left.events.drop(shared.size) +
                right.events.drop(shared.size)
            check(seed, ClarityReplay.replay(merged))
        }
    }
}
