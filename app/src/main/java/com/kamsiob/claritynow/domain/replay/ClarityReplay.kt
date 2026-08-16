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
     * True when [events] can be applied on top of [checkpoint] without losing
     * anything. False means a full rebuild is required.
     */
    fun canResume(checkpoint: ClarityCheckpoint, events: List<ClarityEvent>): Boolean {
        val position = checkpoint.position ?: return true
        val ordered = events.inTotalOrder()
        val newest = ordered.lastOrNull()?.let(ReplayPosition::of) ?: return true
        return ReplayPosition.ORDER.compare(newest, position) >= 0 &&
            ordered.any { ReplayPosition.of(it) == position }
    }
}
