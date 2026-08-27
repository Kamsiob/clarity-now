package com.kamsiob.claritynow.data.event

import com.kamsiob.claritynow.domain.engine.FactRef
import kotlinx.serialization.Serializable

/**
 * The event payloads, MASTER_BUILD_PROMPT 5.2.
 *
 * Every payload carries full before and after values, so a replay reconstructs state
 * without reading any other table. This is the trap in event sourcing that is easy
 * to miss: a log that reads nicely is not the same as a log that replays correctly.
 * `ITEM_PROMOTED` carrying `demotedItemId` is the clearest example, and a descriptive
 * log would omit it.
 *
 * Display snapshots are a separate question, and they are not universal. Eight of
 * these twenty four carry enough of one to name both the subject and the area of a
 * Trail row with no lookup at all: the five area events that carry a name, and
 * [ItemAdded], [ItemPromoted] and [ItemCompleted], which carry a title and an area
 * name together. The other sixteen carry a partial snapshot or none, and an entry
 * from a year ago resolves what it is missing by folding the log to the instant of
 * the event, which is what `domain.query.TrailQueries` does. Neither path reads a
 * live entity table, so renaming an area never rewrites an older Trail entry.
 */
sealed interface EventPayload {
    /** The entity this event is primarily about. Indexed on the row. */
    val primaryEntityId: String
}

// Areas ----------------------------------------------------------------------

@Serializable
data class AreaCreated(
    val areaId: String,
    val name: String,
    val colorHex: String,
    val orderKey: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaRenamed(
    val areaId: String,
    val previousName: String,
    val newName: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaRecolored(
    val areaId: String,
    val previousHex: String,
    val newHex: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaReordered(
    val areaId: String,
    val previousOrderKey: String,
    val newOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaArchived(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

@Serializable
data class AreaUnarchived(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

/** A tombstone. The row is never removed, so Trail entries keep their subject. */
@Serializable
data class AreaDeleted(val areaId: String, val nameSnapshot: String) : EventPayload {
    override val primaryEntityId get() = areaId
}

// Items ----------------------------------------------------------------------

@Serializable
data class ItemAdded(
    val itemId: String,
    val areaId: String,
    val title: String,
    val note: String?,
    val orderKey: String,
    val areaNameSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemEdited(
    val itemId: String,
    val previousTitle: String,
    val newTitle: String,
    val previousNote: String?,
    val newNote: String?,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemQueued(
    val itemId: String,
    val areaId: String,
    val orderKey: String,
    val previousStatus: ItemStatus,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/**
 * Promotion, and the swap that shares its shape.
 *
 * [demotedItemId] is what makes a swap replay correctly. Without it a second device
 * replaying this event would leave two active items in one area and have no record
 * of which one moved, so the invariant would be repaired arbitrarily.
 */
@Serializable
data class ItemPromoted(
    val itemId: String,
    val areaId: String,
    val previousStatus: ItemStatus,
    val demotedItemId: String?,
    val demotedToOrderKey: String?,
    val titleSnapshot: String,
    val areaNameSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemCompleted(
    val itemId: String,
    val areaId: String,
    val titleSnapshot: String,
    val areaNameSnapshot: String,
    val activeDurationDays: Int,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemReopened(
    val itemId: String,
    val areaId: String,
    val targetOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

@Serializable
data class ItemReordered(
    val itemId: String,
    val areaId: String,
    val previousOrderKey: String,
    val newOrderKey: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

/** A tombstone. */
@Serializable
data class ItemDeleted(
    val itemId: String,
    val areaId: String,
    val titleSnapshot: String,
) : EventPayload {
    override val primaryEntityId get() = itemId
}

// Focus ----------------------------------------------------------------------

@Serializable
data class FocusStarted(
    val sessionId: String,
    val areaId: String,
    val itemId: String,
    val plannedSeconds: Int,
) : EventPayload {
    override val primaryEntityId get() = sessionId
}

@Serializable
data class FocusCompleted(val sessionId: String, val actualSeconds: Int) : EventPayload {
    override val primaryEntityId get() = sessionId
}

/** Abandonment is neutral everywhere. No language anywhere in the app blames it. */
@Serializable
data class FocusAbandoned(val sessionId: String, val actualSeconds: Int) : EventPayload {
    override val primaryEntityId get() = sessionId
}

// Pulse ----------------------------------------------------------------------

@Serializable
data class PulseGenerated(
    val pulseId: String,
    val dateKey: String,
    val family: String,
    val escalationStage: Int,
    val register: String,
    val variantKey: String,
    val renderedObservation: String,
    val renderedQuestion: String?,
    val factSnapshot: Map<String, String>,
    val reflectionPeriod: ReflectionPeriod,
) : EventPayload {
    override val primaryEntityId get() = pulseId
}

/**
 * [responseLabel] is stored verbatim so a later callback quotes what the person
 * actually saw, not a label reworded in a newer app version.
 */
@Serializable
data class PulseAnswered(
    val pulseId: String,
    val responseKey: String,
    val responseLabel: String,
    val responseIsPositive: Boolean,
) : EventPayload {
    override val primaryEntityId get() = pulseId
}

// Report ---------------------------------------------------------------------

@Serializable
data class ReportSectionSnapshot(
    val sectionKey: String,
    val sidehead: String,
    val text: String,
)

@Serializable
data class ReportGenerated(
    val reportId: String,
    val weekStartKey: String,
    val headlineKey: String,
    val renderedSections: List<ReportSectionSnapshot>,
    val factSnapshot: Map<String, String>,
) : EventPayload {
    override val primaryEntityId get() = reportId
}

// Guidance -------------------------------------------------------------------

/**
 * [offeredLine] is nominal and [committedLine] is first person. The imperative form
 * never exists anywhere in this app, so it is not stored here either.
 */
@Serializable
data class PlanOffered(
    val planId: String,
    val weekStartKey: String,
    val frameKey: String,
    val cueKey: String,
    val actionKey: String,
    val familyKey: String,
    val subjectId: String?,
    val offeredLine: String,
    val committedLine: String,
    val resolutionFactRef: FactRef,
) : EventPayload {
    override val primaryEntityId get() = planId
}

/** Declining writes nothing at all. There is no PLAN_DECLINED, deliberately. */
@Serializable
data class PlanAccepted(val planId: String) : EventPayload {
    override val primaryEntityId get() = planId
}

// Settings -------------------------------------------------------------------

/**
 * Only for settings that affect behavior history, such as `afterCompleting`.
 * Ordinary per device preferences live in DataStore and are never events.
 */
@Serializable
data class SettingChanged(
    val key: String,
    val previousValue: String,
    val newValue: String,
) : EventPayload {
    override val primaryEntityId get() = key
}
