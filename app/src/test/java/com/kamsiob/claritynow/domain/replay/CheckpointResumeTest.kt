package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.inTotalOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checkpoints, MASTER_BUILD_PROMPT 6.4, and the one way they can be wrong.
 *
 * A checkpoint is the fold of a prefix of the total order plus the position the
 * prefix ends at. Resuming from one is correct while the log still begins with that
 * exact prefix, and silently wrong the moment it does not, because
 * `ClarityReplay.replayFrom` drops every event at or before the position as already
 * folded in. Nothing looks broken afterwards. The app opens, every screen renders,
 * and some number of a person's completions have simply never happened.
 *
 * `ReplayHarnessTest` proves the equivalence property. **This file is about the
 * cases where equivalence does not hold**, because the guard against them is the
 * whole of what makes a checkpoint safe to keep, and a guard nobody has watched fail
 * is a comment.
 *
 * The scenario each of these is really about is import and merge, phase 11.
 * MASTER_BUILD_PROMPT 6.4 requires both paths to throw every checkpoint away and
 * rebuild from event zero, and `ClarityRepository.ingestForeignLog` does. What is
 * here is the second line: a merge that forgot to would still be caught, because a
 * log whose prefix grew can no longer account for the checkpoint sitting on it.
 */
class CheckpointResumeTest {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    private fun serialize(state: ClarityState): String =
        json.encodeToString(ClarityState.serializer(), state.canonical())

    /**
     * A log, a checkpoint taken part way through it, and a second device's events
     * that belong before that checkpoint.
     *
     * The fork shares the lamport it was taken at, exactly as a second phone holding
     * the same log does, so every event it writes sorts *inside* the first device's
     * stream rather than after it. That is what makes this the merge case and not
     * simply a longer log.
     */
    private class Divergence(
        seed: Long,
        sharedEvents: Int,
        aheadEvents: Int,
        foreignEvents: Int,
        /**
         * Names the two devices. It has a parameter because
         * `EventStreamGenerator` derives its event ids from the origin id and a
         * counter, so two generators sharing an origin id mint the same ids, and a
         * test that mixes their output has two different events claiming to be one.
         */
        label: String = "device",
    ) {
        private val a = EventStreamGenerator(seed, "$label-a")

        val local: List<ClarityEvent>
        val foreign: List<ClarityEvent>

        init {
            a.generate(sharedEvents)
            val b = a.fork(seed + 1_000L, "$label-b")
            a.generate(aheadEvents)
            local = a.events
            b.generate(foreignEvents)
            foreign = b.events
        }

        val checkpoint: ClarityCheckpoint get() = ClarityReplay.checkpoint(local)
        val merged: List<ClarityEvent> get() = local + foreign
    }

    // The property, over every cut point rather than one -----------------------

    @Test
    fun `a checkpoint taken anywhere in a log plus its tail equals a full replay`() {
        for (seed in 1L..25L) {
            val whole = EventStreamGenerator(seed, "device-a").generate(200)
            val full = ClarityReplay.replay(whole)
            for (cut in listOf(0, 1, 2, 17, 99, 100, 151, 199, 200)) {
                val checkpoint = ClarityReplay.checkpoint(whole.take(cut))
                assertEquals(
                    "seed $seed cut $cut",
                    serialize(full),
                    serialize(ClarityReplay.replayFrom(checkpoint, whole)),
                )
                assertEquals(
                    "seed $seed cut $cut, through resume",
                    serialize(full),
                    serialize(ClarityReplay.resume(checkpoint, whole)),
                )
            }
        }
    }

    /**
     * The assumption the whole guard rests on, asserted rather than assumed.
     *
     * `canResume` compares the log against `ClarityState.eventsApplied`, which is
     * only a count of the prefix while the reducer counts every event it is handed,
     * including the ones it records a diagnostic for instead of applying. If that
     * ever changes, this fails here rather than in the field, where the symptom is a
     * checkpoint resuming over events it never folded.
     */
    @Test
    fun `a checkpoint counts every event it was folded from`() {
        for (seed in 1L..10L) {
            val whole = EventStreamGenerator(seed, "device-a").generate(150)
            for (cut in listOf(0, 3, 40, 149, 150)) {
                val head = whole.take(cut)
                val checkpoint = ClarityReplay.checkpoint(head)
                assertEquals(
                    "seed $seed cut $cut",
                    head.inTotalOrder().size,
                    checkpoint.state.eventsApplied,
                )
            }
        }
    }

    // The merge case -----------------------------------------------------------

    @Test
    fun `a log that grew before the checkpoint cannot be resumed`() {
        for (seed in 1L..15L) {
            val case = Divergence(seed, sharedEvents = 60, aheadEvents = 60, foreignEvents = 40)
            val checkpoint = case.checkpoint
            val position = requireNotNull(checkpoint.position) { "seed $seed produced no events" }

            val insertedBefore = case.foreign.count {
                ReplayPosition.ORDER.compare(ReplayPosition.of(it), position) < 0
            }
            assertTrue(
                "seed $seed: the fork wrote nothing before the checkpoint, so this " +
                    "scenario is not the one it claims to be",
                insertedBefore > 0,
            )
            assertFalse("seed $seed", ClarityReplay.canResume(checkpoint, case.merged))
        }
    }

    /**
     * The guard is load bearing, and this is the test that says what it is holding.
     *
     * Resuming a merged log over a checkpoint does not fail, throw or log. It
     * produces a state, and the state is missing every event the merge inserted
     * before the checkpoint's position. `eventsApplied` makes the loss legible here;
     * on a phone it would be somebody's completed items.
     */
    @Test
    fun `resuming a merged log over a checkpoint would silently drop events`() {
        for (seed in 1L..15L) {
            val case = Divergence(seed, sharedEvents = 60, aheadEvents = 60, foreignEvents = 40)
            val merged = case.merged
            val checkpoint = case.checkpoint
            val full = ClarityReplay.replay(merged)
            val naive = ClarityReplay.replayFrom(checkpoint, merged)

            assertEquals("seed $seed", merged.inTotalOrder().size, full.eventsApplied)
            assertTrue(
                "seed $seed: nothing was dropped, so this test proves nothing",
                naive.eventsApplied < full.eventsApplied,
            )
            assertNotEquals("seed $seed", serialize(full), serialize(naive))
            assertEquals(
                "seed $seed",
                serialize(full),
                serialize(ClarityReplay.resume(checkpoint, merged)),
            )
        }
    }

    /**
     * And the remedy works. Rebuilding from event zero and taking a fresh checkpoint
     * puts the fast path back, so throwing every checkpoint away on import costs one
     * slow load rather than the fast path forever.
     */
    @Test
    fun `a checkpoint taken after the merge resumes again`() {
        val case = Divergence(7L, sharedEvents = 60, aheadEvents = 60, foreignEvents = 40)
        val merged = case.merged
        val fresh = ClarityReplay.checkpoint(merged)

        assertTrue(ClarityReplay.canResume(fresh, merged))
        assertEquals(
            serialize(ClarityReplay.replay(merged)),
            serialize(ClarityReplay.resume(fresh, merged)),
        )
    }

    // Every other log a checkpoint cannot account for --------------------------

    @Test
    fun `an emptied log never resurrects the state a checkpoint held`() {
        val whole = EventStreamGenerator(13L, "device-a").generate(300)
        val checkpoint = ClarityReplay.checkpoint(whole)
        assertTrue("the fixture should not be empty", checkpoint.state.areas.isNotEmpty())

        assertFalse(ClarityReplay.canResume(checkpoint, emptyList()))
        assertEquals(
            serialize(ClarityState.EMPTY),
            serialize(ClarityReplay.resume(checkpoint, emptyList())),
        )
    }

    @Test
    fun `a log missing the event a checkpoint was taken at cannot be resumed`() {
        val whole = EventStreamGenerator(21L, "device-a").generate(120)
        val checkpoint = ClarityReplay.checkpoint(whole)
        val truncated = whole.inTotalOrder().dropLast(1)

        assertFalse(ClarityReplay.canResume(checkpoint, truncated))
        assertEquals(
            serialize(ClarityReplay.replay(truncated)),
            serialize(ClarityReplay.resume(checkpoint, truncated)),
        )
    }

    @Test
    fun `a log missing an event from inside the prefix cannot be resumed`() {
        val whole = EventStreamGenerator(23L, "device-a").generate(120)
        val checkpoint = ClarityReplay.checkpoint(whole)
        val ordered = whole.inTotalOrder()
        val gapped = ordered.filterIndexed { index, _ -> index != 40 }

        assertFalse(ClarityReplay.canResume(checkpoint, gapped))
    }

    @Test
    fun `a checkpoint of an empty log is always resumable and folds nothing`() {
        val checkpoint = ClarityReplay.checkpoint(emptyList())
        assertNull(checkpoint.position)
        val whole = EventStreamGenerator(31L, "device-a").generate(80)
        assertTrue(ClarityReplay.canResume(checkpoint, whole))
        assertEquals(
            serialize(ClarityReplay.replay(whole)),
            serialize(ClarityReplay.resume(checkpoint, whole)),
        )
    }

    @Test
    fun `a duplicate delivery of the tail changes nothing`() {
        val whole = EventStreamGenerator(37L, "device-a").generate(160)
        val checkpoint = ClarityReplay.checkpoint(whole.take(90))
        val delivered = whole + whole.takeLast(50) + whole.take(20)

        assertTrue(ClarityReplay.canResume(checkpoint, delivered))
        assertEquals(
            serialize(ClarityReplay.replay(whole)),
            serialize(ClarityReplay.resume(checkpoint, delivered)),
        )
    }

    // One rule, two spellings --------------------------------------------------

    /**
     * `ClarityRepository.resumeOrRebuildLocked` cannot ask the list form of the
     * rule, because reading the whole log to decide whether it has to read the whole
     * log would defeat the checkpoint. It asks SQLite for the same two numbers
     * instead: how many rows sit at or before the position, and whether the position
     * is still there.
     *
     * That is a second spelling of one rule, and two spellings drift. This is the
     * test that keeps them together: it computes both numbers the way the three
     * queries do and asserts the answer matches the form the harness uses, across
     * every log shape above.
     */
    @Test
    fun `the count form of the rule agrees with the list form`() {
        val whole = EventStreamGenerator(41L, "device-a").generate(140)
        val checkpoint = ClarityReplay.checkpoint(whole.take(70))
        val case = Divergence(
            seed = 43L,
            sharedEvents = 40,
            aheadEvents = 40,
            foreignEvents = 30,
            label = "merged",
        )

        val cases = listOf(
            Case("the whole log", checkpoint, whole),
            Case("the prefix alone", checkpoint, whole.take(70)),
            Case("an emptied log", checkpoint, emptyList()),
            Case("a gap in the prefix", checkpoint, whole.inTotalOrder().drop(1)),
            Case("a log missing the position", checkpoint, whole.inTotalOrder().take(69)),
            Case("a merged log", case.checkpoint, case.merged),
        )

        for ((label, cp, log) in cases) {
            val position = cp.position
            val fromCounts = ClarityReplay.canResume(
                checkpoint = cp,
                eventsAtOrBeforePosition = rowsAtOrBefore(log, position),
                positionIsInLog = log.any { it.id == position?.eventId },
            )
            assertEquals(label, ClarityReplay.canResume(cp, log), fromCounts)
        }

        // The merged log is one of them, and it is the only shape where the position
        // is still in the log and the answer is still no. Without it this proves only
        // that two functions agree about the easy case.
        assertTrue(case.merged.any { it.id == case.checkpoint.position?.eventId })
        assertFalse(ClarityReplay.canResume(case.checkpoint, case.merged))
    }

    /** One log, one checkpoint, and what to call the pair when it fails. */
    private data class Case(
        val label: String,
        val checkpoint: ClarityCheckpoint,
        val log: List<ClarityEvent>,
    )

    /** What `count()` minus the size of `after()` answers, computed over a list. */
    private fun rowsAtOrBefore(log: List<ClarityEvent>, position: ReplayPosition?): Int {
        if (position == null) return 0
        val after = log.count {
            ReplayPosition.ORDER.compare(ReplayPosition.of(it), position) > 0
        }
        return log.size - after
    }

    // The storage round trip ---------------------------------------------------

    @Test
    fun `a checkpoint survives the two columns the snapshot row stores it in`() {
        val whole = EventStreamGenerator(53L, "device-a").generate(180)
        val checkpoint = ClarityReplay.checkpoint(whole.take(100))
        val position = requireNotNull(checkpoint.position)

        val restored = requireNotNull(
            ClarityCheckpointCodec.decode(
                positionJson = ClarityCheckpointCodec.encodePosition(position),
                stateJson = ClarityCheckpointCodec.encodeState(checkpoint.state),
            ),
        )

        assertEquals(position, restored.position)
        assertEquals(serialize(checkpoint.state), serialize(restored.state))
        assertEquals(
            serialize(ClarityReplay.replay(whole)),
            serialize(ClarityReplay.resume(restored, whole)),
        )
    }

    @Test
    fun `a checkpoint that will not decode is no checkpoint at all`() {
        assertNull(ClarityCheckpointCodec.decode("not json", "{}"))
        assertNull(ClarityCheckpointCodec.decode("{}", "not json"))
        assertNull(ClarityCheckpointCodec.decode("", ""))
    }
}
