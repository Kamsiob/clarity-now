package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.inTotalOrder
import kotlinx.serialization.Serializable

/** Where a checkpoint was taken, in the total order rather than in wall clock time. */
@Serializable
data class ReplayPosition(val lamport: Long, val originId: String, val eventId: String) {
    companion object {
        val ORDER: Comparator<ReplayPosition> =
            compareBy({ it.lamport }, { it.originId }, { it.eventId })

        fun of(event: ClarityEvent) = ReplayPosition(event.lamport, event.originId, event.id)
    }
}

/**
 * A serialized state plus the position it was taken at. `ClarityWeekSnapshot`
 * stores one of these when a week closes, so cold start replays a handful of
 * events rather than a year of them.
 *
 * A checkpoint is only valid while the log grows at the end. Importing or merging
 * a log can introduce events that sort before the checkpoint, so both paths throw
 * every checkpoint away and rebuild from event zero. MASTER_BUILD_PROMPT 6.4.
 */
@Serializable
data class ClarityCheckpoint(
    val position: ReplayPosition?,
    val state: ClarityState,
) {
    companion object {
        val EMPTY = ClarityCheckpoint(position = null, state = ClarityState.EMPTY)
    }
}

object ClarityReplay {

    /** A full rebuild from event zero. The only path that is always correct. */
    fun replay(events: List<ClarityEvent>): ClarityState =
        events.inTotalOrder().fold(ClarityState.EMPTY, ClarityReducer::apply)

    /**
     * Replays only what came after [checkpoint].
     *
     * Any event in [events] that sorts at or before the checkpoint position is
     * dropped, because it is already folded into the checkpoint state. A caller
     * that has just merged a foreign log must not use this; it must call [replay].
     */
    fun replayFrom(checkpoint: ClarityCheckpoint, events: List<ClarityEvent>): ClarityState {
        val position = checkpoint.position ?: return replay(events)
        val tail = events.inTotalOrder().filter {
            ReplayPosition.ORDER.compare(ReplayPosition.of(it), position) > 0
        }
        return tail.fold(checkpoint.state, ClarityReducer::apply)
    }

    /** Takes a checkpoint at the last event of [events]. */
    fun checkpoint(events: List<ClarityEvent>): ClarityCheckpoint {
        val ordered = events.inTotalOrder()
        val state = ordered.fold(ClarityState.EMPTY, ClarityReducer::apply)
        return ClarityCheckpoint(
            position = ordered.lastOrNull()?.let(ReplayPosition::of),
            state = state,
        )
    }

    /**
     * Replays [events] on top of [checkpoint] when that is safe, and rebuilds from
     * event zero when it is not.
     *
     * **This is the entry point for anything holding the whole log**, which is the
     * harness, the simulator and any future tool. `ClarityRepository` is the one
     * caller that cannot use it, because it deliberately never reads the whole log
     * on a cold start; it asks the three argument [canResume] with counts from SQL
     * and then calls [replayFrom] itself.
     *
     * Both stay public for exactly that reason, and because the tests that prove the
     * rule have to call them apart. Calling [replayFrom] without [canResume] is how
     * a merged log loses the events it inserted before the checkpoint: silently,
     * permanently, and with every screen still rendering something plausible.
     */
    fun resume(checkpoint: ClarityCheckpoint?, events: List<ClarityEvent>): ClarityState =
        if (checkpoint != null && canResume(checkpoint, events)) {
            replayFrom(checkpoint, events)
        } else {
            replay(events)
        }

    /**
     * True when [events] can be applied on top of [checkpoint] without losing
     * anything. False means a full rebuild is required.
     *
     * Counts the log rather than looking only at its ends, which is the difference
     * between a check and a gesture. See the two argument form below for why.
     */
    fun canResume(checkpoint: ClarityCheckpoint, events: List<ClarityEvent>): Boolean {
        val position = checkpoint.position ?: return true
        var atOrBefore = 0
        var present = false
        for (event in events.inTotalOrder()) {
            val side = ReplayPosition.ORDER.compare(ReplayPosition.of(event), position)
            if (side <= 0) atOrBefore++
            if (side == 0) present = true
        }
        return canResume(checkpoint, atOrBefore, present)
    }

    /**
     * The rule itself, over two numbers a caller can get from a list or from SQL.
     *
     * Stated once and in one place, because the app asks it twice: the cold start
     * path asks SQLite, since reading the whole log to decide whether it needs to
     * read the whole log would defeat the checkpoint, and the harness asks a list.
     * Two spellings of one rule is how the two answers drift apart.
     *
     * [eventsAtOrBeforePosition] is how many events the log holds at or before the
     * checkpoint's position, in the total order. [positionIsInLog] is whether the
     * event the checkpoint was taken at is still there.
     *
     * **The count is the load bearing half, and it is the half that is easy to
     * leave out.** A checkpoint is the fold of a prefix of the total order, so
     * resuming from it is only correct while the log still begins with that exact
     * prefix. Importing or merging a foreign log can insert events *before* the
     * checkpoint's position: both ends of the log still look right, the checkpoint's
     * own event is still there, and every inserted event is then dropped by
     * [replayFrom] as already folded in when it never was. The count catches it,
     * because a prefix that grew by three events no longer matches the number of
     * events the checkpoint state was folded from.
     *
     * Both merge paths throw every checkpoint away for the same reason,
     * MASTER_BUILD_PROMPT 6.4, and this is here so that forgetting to costs a slow
     * cold start rather than a wrong one.
     *
     * A log that cannot account for the checkpoint at all, an emptied one included,
     * answers false. A full rebuild is always correct and never expensive enough to
     * be worth risking the alternative.
     */
    fun canResume(
        checkpoint: ClarityCheckpoint,
        eventsAtOrBeforePosition: Int,
        positionIsInLog: Boolean,
    ): Boolean {
        if (checkpoint.position == null) return true
        if (!positionIsInLog) return false
        return eventsAtOrBeforePosition == checkpoint.state.eventsApplied
    }
}
