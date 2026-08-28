package com.kamsiob.claritynow.data.repo

import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.OrderKey

/**
 * The second choice on the re-entry screen, as a value. MASTER_BUILD_PROMPT 14b.4.
 *
 * > It offers, second and quieter, to clear the active items and start fresh.
 * > **Clearing demotes**: each active item returns to the head of its own queue with
 * > `ITEM_QUEUED`. Nothing is deleted, nothing is completed, and the wording says so.
 * > The obvious implementation of a fresh start is a delete, and a delete of a
 * > person's own work on the day they came back is the single most expensive thing
 * > this app could do.
 *
 * **It is a pure function rather than a branch inside [ClarityRepository], because the
 * one thing this has to prove is what it does not write.** The repository holds Room
 * and DataStore and cannot be reached from a desktop JVM at all, which is the same
 * reason `FocusSession.kt` exists beside it. A test that asserts no `ITEM_DELETED` and
 * no `ITEM_COMPLETED` leaves this function has to be able to hold the whole answer
 * still and read it, and this returns the whole answer. The repository's own method is
 * then one line of writing and no deciding.
 *
 * **Every payload is an [ItemQueued] by the return type**, so the assertion that
 * nothing is deleted and nothing is completed is made by the compiler before any test
 * runs. That is deliberate rather than tidy: a list of `EventPayload` would have been
 * the ordinary signature and would have put the whole of 14b.4's most expensive
 * prohibition into a test somebody has to keep.
 */
fun activeItemsBackInTheirQueues(state: ClarityState, jitter: String): List<ItemQueued> =
    // Live areas only, in display order, so the answer is deterministic and a second
    // device replaying this log lands on the same keys in the same order.
    //
    // **An archived area's active item is left alone**, which is a choice rather than an
    // oversight. The sentence on the screen is about the active items, and the active
    // items are the cards the person is looking at; an archived area is one they put
    // away, its item is on no screen, and unarchiving it later should hand back what was
    // there. "Everything is where you left it" is the promise the whole screen makes,
    // and an area nobody could see is the place that promise costs nothing to keep.
    state.liveAreas.mapNotNull { area ->
        val active = state.activeItemIn(area.id) ?: return@mapNotNull null
        ItemQueued(
            itemId = active.id,
            areaId = area.id,
            // The head of its own queue. Deliberately the same expression the swap in
            // `ClarityRepository.swapToItem` demotes with, rather than a second rule for
            // the same move: the active item and the queue share one ordering space, the
            // head is that space's minimum, and with the item itself out of the way there
            // is nothing between the two for a tightening pass to find. The fallback is
            // the item's own key, which is what an empty queue leaves to sort before.
            orderKey = OrderKey.before(
                state.queueIn(area.id).firstOrNull()?.orderKey ?: active.orderKey,
                jitter,
            ),
            previousStatus = ItemStatus.ACTIVE,
        )
    }
