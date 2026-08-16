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

    fun activeItemIn(areaId: String): ItemState? = items.values.firstOrNull {
        it.areaId == areaId && it.status == ItemStatus.ACTIVE && it.deletedAt == null
    }

    /** The queue for an area, head first. */
    fun queueIn(areaId: String): List<ItemState> = items.values
        .filter { it.areaId == areaId && it.status == ItemStatus.QUEUED && it.deletedAt == null }
        .sortedWith(compareBy({ it.orderKey }, { it.id }))

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
    val lastEventLamport: Long,
)

@Serializable
data class ItemState(
    val id: String,
    val areaId: String,
    val title: String,
    val note: String? = null,
    val orderKey: String,
    val status: ItemStatus,
    val createdAt: Long,
    /** When this item last became active. Drives activeDurationDays and persistence. */
    val activeSince: Long? = null,
    val completedAt: Long? = null,
    val deletedAt: Long? = null,
    val lastEventLamport: Long,
)

enum class FocusOutcome { RUNNING, COMPLETED, ABANDONED }

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

@Serializable
data class ReportState(
    val id: String,
    val weekStartKey: String,
    val headlineKey: String,
    val sections: List<ReportSectionSnapshot> = emptyList(),
    val factSnapshot: Map<String, String> = emptyMap(),
    val generatedAt: Long,
    val lastEventLamport: Long,
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
