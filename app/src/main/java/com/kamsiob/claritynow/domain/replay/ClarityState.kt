package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.domain.engine.FactRef
import kotlinx.serialization.Serializable

/**
 * Everything the app knows, derived entirely from the event log.
 *
 * The Room tables that mirror this exist for query speed and can be dropped and
 * rebuilt with no data loss. A debug action does exactly that as a proof.
 *
 * Deleted and archived entities stay in the state as tombstones and flags. They are
 * filtered out at the projection boundary rather than removed, because a Trail
 * entry from last March still has to render.
 */
@Serializable
data class ClarityState(
    val areas: Map<String, AreaState> = emptyMap(),
    val items: Map<String, ItemState> = emptyMap(),
    val focusSessions: Map<String, FocusSessionState> = emptyMap(),
    /** Keyed by dateKey. At most one Pulse per local calendar day. */
    val pulses: Map<String, PulseEntryState> = emptyMap(),
    /** Keyed by weekStartKey. */
    val reports: Map<String, ReportState> = emptyMap(),
    /** Keyed by planId. */
    val plans: Map<String, PlanState> = emptyMap(),
    /** Only settings that affect behavior history. Device preferences are not events. */
    val settings: Map<String, String> = emptyMap(),
    val conflicts: List<ClarityConflict> = emptyList(),
    val diagnostics: List<ReplayDiagnostic> = emptyList(),
    val lastLamport: Long = 0L,
    val eventsApplied: Int = 0,
) {
    /** Areas a person can see: not archived, not tombstoned, in display order. */
    val liveAreas: List<AreaState>
        get() = areas.values
            .filter { it.deletedAt == null && !it.archived }
            .sortedWith(compareBy({ it.orderKey }, { it.id }))

    val archivedAreas: List<AreaState>
        get() = areas.values
            .filter { it.deletedAt == null && it.archived }
            .sortedWith(compareBy({ it.orderKey }, { it.id }))

    /**
     * Items with no area, oldest first. The inbox. Addendum 01 4a.
     *
     * The three area scoped projections below take a non null area id and compare
     * it against a nullable field, so an unfiled item is excluded from all of them
     * by the type system rather than by a filter someone has to remember. This is
     * the one projection that can see them, and it is the reason a null is a better
     * answer than a synthetic inbox area: there is exactly one place that knows
     * about the state, instead of a special case in every enumeration of areas.
     * DECISIONS.md C8.
     *
     * Ordered by [ItemState.orderKey] like every other list of items, so filing
     * from the inbox and reordering inside it work the same way they do in a queue.
     */
    val unfiledItems: List<ItemState>
        get() = items.values
            .filter { it.areaId == null && it.deletedAt == null }
            .sortedWith(compareBy({ it.orderKey }, { it.id }))

    /**
     * The one thing happening in an area, or null.
     *
     * An unfiled item can never be the answer, and not only because `null` never
     * equals a real area id: an item with no area cannot be ACTIVE at all, which
     * `ClarityInvariants` checks and the reducer refuses to produce.
     */
    fun activeItemIn(areaId: String): ItemState? = items.values.firstOrNull {
        it.areaId == areaId && it.status == ItemStatus.ACTIVE && it.deletedAt == null
    }

    /** The queue for an area, head first. Unfiled items are in no area's queue. */
    fun queueIn(areaId: String): List<ItemState> = items.values
        .filter { it.areaId == areaId && it.status == ItemStatus.QUEUED && it.deletedAt == null }
        .sortedWith(compareBy({ it.orderKey }, { it.id }))

    /**
     * Every live item in an area, active first, then the queue in order.
     *
     * The active item and the queued items share one ordering space: a promoted item
     * keeps the key it had, and a demoted one rejoins the queue carrying a key from
     * that same space. Anything choosing a new key for this area has to look at all
     * of them, and looking at [queueIn] alone is how two items end up holding one
     * key. See `OrderKeyCollisionTest`.
     */
    fun liveItemsIn(areaId: String): List<ItemState> =
        listOfNotNull(activeItemIn(areaId)) + queueIn(areaId)

    fun completedIn(areaId: String): List<ItemState> = items.values
        .filter { it.areaId == areaId && it.status == ItemStatus.COMPLETED && it.deletedAt == null }
        .sortedWith(compareByDescending<ItemState> { it.completedAt ?: 0L }.thenBy { it.id })

    /**
     * A canonical form with every map and list in a fixed order, for byte level
     * comparison and for writing the golden fixture. Two replays of the same log
     * already agree, but insertion order is not something a JSON file should rely on.
     */
    fun canonical(): ClarityState = copy(
        areas = areas.toSortedMap(),
        items = items.toSortedMap(),
        focusSessions = focusSessions.toSortedMap(),
        pulses = pulses.toSortedMap(),
        reports = reports.toSortedMap(),
        plans = plans.toSortedMap(),
        settings = settings.toSortedMap(),
        conflicts = conflicts.sortedBy { it.id },
        diagnostics = diagnostics.sortedWith(compareBy({ it.eventId }, { it.reason })),
    )

    companion object {
        val EMPTY = ClarityState()
    }
}

@Serializable
data class AreaState(
    val id: String,
    val name: String,
    val colorHex: String,
    val orderKey: String,
    val archived: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long,
    /**
     * The wall clock of the most recent event concerning this area or anything in
     * it. Display only, like every other wall clock in the log, and never used for
     * ordering. The area card's `Last active` line reads this rather than running a
     * query, so the number on screen and the number in the state cannot disagree.
     */
    val lastEventAt: Long,
    val lastEventLamport: Long,
)

/**
 * One item, filed or not.
 *
 * [areaId] is null for an unfiled item, which is the inbox state Addendum 01 4a
 * added so that capture never requires a decision. An unfiled item is real: it can
 * be edited, estimated, reordered and deleted, and it is the only item state that
 * sits outside every area scoped rule in MASTER_BUILD_PROMPT 6.2. It is always
 * QUEUED, because ACTIVE and COMPLETED are area scoped states and there is no area
 * for it to be the one thing in. `ITEM_FILED` is the only transition out.
 *
 * [firstStep] is Addendum 01 4b, the first physical action, shown at caption weight
 * on the active item card when present and simply absent otherwise. [estimateMinutes]
 * is 4c and moves through `ITEM_ESTIMATED`. Both are optional forever.
 */
@Serializable
data class ItemState(
    val id: String,
    val areaId: String?,
    val title: String,
    val note: String? = null,
    val firstStep: String? = null,
    val estimateMinutes: Int? = null,
    val orderKey: String,
    val status: ItemStatus,
    val createdAt: Long,
    /** When this item last became active. Drives activeDurationDays and persistence. */
    val activeSince: Long? = null,
    val completedAt: Long? = null,
    val deletedAt: Long? = null,
    val lastEventLamport: Long,
)

/**
 * How a focus session finished, or that it has not.
 *
 * ENDED_EARLY rather than ABANDONED, renamed with the event type in the Addendum 01
 * schema commit. DECISIONS.md C6 and [com.kamsiob.claritynow.data.event.FocusEndedEarly].
 * RUNNING is not a terminal value and a session sitting in it forever is a legal
 * state, because a killed process leaves exactly that and nothing is allowed to
 * infer an ending from its absence.
 */
enum class FocusOutcome { RUNNING, COMPLETED, ENDED_EARLY }

/**
 * [plannedSeconds] is what the session is currently planned to run for, not what it
 * was planned to run for when it started. `FOCUS_EXTENDED` moves it, and the event
 * carries the absolute figure so a replay of two extensions cannot disagree with
 * the number the person was shown. The original figure is in the session's own
 * FOCUS_STARTED, which is where anything needing it should read.
 */
@Serializable
data class FocusSessionState(
    val id: String,
    val areaId: String,
    val itemId: String,
    val plannedSeconds: Int,
    val startedAt: Long,
    val endedAt: Long? = null,
    val actualSeconds: Int? = null,
    val outcome: FocusOutcome = FocusOutcome.RUNNING,
    val lastEventLamport: Long,
)

@Serializable
data class PulseEntryState(
    val id: String,
    val dateKey: String,
    val family: String,
    val stage: Int,
    val register: String,
    val variantKey: String,
    val observation: String,
    val question: String? = null,
    val factSnapshot: Map<String, String> = emptyMap(),
    val reflectionPeriod: ReflectionPeriod,
    val generatedAt: Long,
    val responseKey: String? = null,
    val responseLabel: String? = null,
    val responseIsPositive: Boolean? = null,
    val answeredAt: Long? = null,
    val lastEventLamport: Long,
) {
    val isAnswered: Boolean get() = responseKey != null
}

/**
 * One report, as the history page reads it back.
 *
 * [weekStartKey] is the Sunday the report is filed under and [windowStartKey] is the first
 * of the seven days it described. See `ReportGenerated`, which says why both are kept.
 * [windowStartKey] and [headlineText] are null only for a report written before those two
 * fields existed, which is no report any install has: nothing wrote the event until then.
 */
@Serializable
data class ReportState(
    val id: String,
    val weekStartKey: String,
    val headlineKey: String,
    val sections: List<ReportSectionSnapshot> = emptyList(),
    val factSnapshot: Map<String, String> = emptyMap(),
    val generatedAt: Long,
    val lastEventLamport: Long,
    val windowStartKey: String? = null,
    val headlineText: String? = null,
)

@Serializable
data class PlanState(
    val id: String,
    val weekStartKey: String,
    val frameKey: String,
    val cueKey: String,
    val actionKey: String,
    val familyKey: String,
    val subjectId: String? = null,
    val offeredLine: String,
    val committedLine: String,
    val resolutionFactRef: FactRef,
    val offeredAt: Long,
    val acceptedAt: Long? = null,
    val lastEventLamport: Long,
) {
    /** Declining writes nothing, so an unaccepted plan is simply one that was never accepted. */
    val isAccepted: Boolean get() = acceptedAt != null
}

enum class ConflictKind {
    /** Two devices made different items active in the same area. */
    TWO_ACTIVE_IN_AREA,

    /** Two devices produced a row for the same calendar day or week. */
    DUPLICATE_DATE_KEY,
}

/**
 * A divergence the reducer resolved. Never silent, never a technical dialog, never
 * data loss. The Areas screen surfaces the unresolved ones in the app's voice.
 */
@Serializable
data class ClarityConflict(
    val id: String,
    val kind: ConflictKind,
    val winnerId: String,
    val loserId: String,
    val areaId: String? = null,
    val areaNameSnapshot: String? = null,
    val winnerTitleSnapshot: String? = null,
    val loserTitleSnapshot: String? = null,
    val detectedAtLamport: Long,
)

/**
 * Something in the log the reducer could not apply. Recorded rather than thrown:
 * an event referencing an unknown entity must never crash the app.
 */
@Serializable
data class ReplayDiagnostic(
    val eventId: String,
    val eventType: String,
    val reason: String,
)
