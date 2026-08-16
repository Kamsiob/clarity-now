package com.kamsiob.claritynow.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventJson
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.ReflectionPeriod
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import com.kamsiob.claritynow.domain.engine.FactRef
import com.kamsiob.claritynow.domain.replay.AreaState
import com.kamsiob.claritynow.domain.replay.ClarityConflict
import com.kamsiob.claritynow.domain.replay.ConflictKind
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.domain.replay.PlanState
import com.kamsiob.claritynow.domain.replay.PulseEntryState
import com.kamsiob.claritynow.domain.replay.ReportState

/**
 * The append only log. Rows are never updated and never deleted.
 *
 * This is the only table in the database that holds truth. Everything below it is
 * a cache that can be dropped and rebuilt, and a debug action does exactly that.
 */
@Entity(
    tableName = "clarity_event",
    indices = [
        Index("lamport"),
        Index("type"),
        Index("entityId"),
        Index("wallClock"),
    ],
)
data class ClarityEventRow(
    @PrimaryKey val id: String,
    val schemaVersion: Int,
    val type: String,
    val wallClock: Long,
    val lamport: Long,
    val originId: String,
    val payload: String,
    val entityId: String?,
)

fun ClarityEvent.toRow() = ClarityEventRow(
    id = id,
    schemaVersion = schemaVersion,
    type = type.name,
    wallClock = wallClock,
    lamport = lamport,
    originId = originId,
    payload = ClarityEventJson.encodePayload(payload),
    entityId = entityId,
)

/** Returns null for an event type this build does not know, so a newer log still loads. */
fun ClarityEventRow.toEvent(): ClarityEvent? {
    val eventType = ClarityEventType.fromName(type) ?: return null
    return ClarityEvent(
        id = id,
        schemaVersion = schemaVersion,
        type = eventType,
        wallClock = wallClock,
        lamport = lamport,
        originId = originId,
        payload = ClarityEventJson.decodePayload(eventType, payload),
        entityId = entityId,
    )
}

// The materialized cache. MASTER_BUILD_PROMPT 5.4. All derived, all rebuildable.

@Entity(tableName = "clarity_area", indices = [Index("orderKey")])
data class AreaRow(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val orderKey: String,
    val archived: Boolean,
    val createdAt: Long,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

@Entity(tableName = "clarity_item", indices = [Index("areaId"), Index("status"), Index("orderKey")])
data class ItemRow(
    @PrimaryKey val id: String,
    val areaId: String,
    val title: String,
    val note: String?,
    val orderKey: String,
    val status: String,
    val createdAt: Long,
    val activeSince: Long?,
    val completedAt: Long?,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

@Entity(tableName = "clarity_focus_session", indices = [Index("areaId"), Index("startedAt")])
data class FocusSessionRow(
    @PrimaryKey val id: String,
    val areaId: String,
    val itemId: String,
    val plannedSeconds: Int,
    val startedAt: Long,
    val endedAt: Long?,
    val actualSeconds: Int?,
    val outcome: String,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

@Entity(tableName = "clarity_pulse_entry", indices = [Index("dateKey", unique = true)])
data class PulseEntryRow(
    @PrimaryKey val id: String,
    val dateKey: String,
    val family: String,
    val stage: Int,
    val register: String,
    val variantKey: String,
    val observation: String,
    val question: String?,
    val factSnapshot: Map<String, String>,
    val reflectionPeriod: String,
    val generatedAt: Long,
    val responseKey: String?,
    val responseLabel: String?,
    val responseIsPositive: Boolean?,
    val answeredAt: Long?,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

@Entity(tableName = "clarity_report", indices = [Index("weekStartKey", unique = true)])
data class ReportRow(
    @PrimaryKey val id: String,
    val weekStartKey: String,
    val headlineKey: String,
    val sections: List<ReportSectionSnapshot>,
    val factSnapshot: Map<String, String>,
    val generatedAt: Long,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

/**
 * A closed week, doubling as a replay checkpoint. MASTER_BUILD_PROMPT 6.4.
 * [stateJson] is a serialized ClarityState and [positionJson] is where it was taken.
 */
@Entity(tableName = "clarity_week_snapshot", indices = [Index("weekStartKey", unique = true)])
data class WeekSnapshotRow(
    @PrimaryKey val weekStartKey: String,
    val takenAt: Long,
    val lamport: Long,
    val positionJson: String,
    val stateJson: String,
)

@Entity(tableName = "clarity_plan", indices = [Index("weekStartKey")])
data class PlanRow(
    @PrimaryKey val id: String,
    val weekStartKey: String,
    val frameKey: String,
    val cueKey: String,
    val actionKey: String,
    val familyKey: String,
    val subjectId: String?,
    val offeredLine: String,
    val committedLine: String,
    val resolutionCategory: String,
    val resolutionPath: String,
    val offeredAt: Long,
    val acceptedAt: Long?,
    val resolvedAt: Long?,
    val resolvedValue: String?,
    val deletedAt: Long?,
    val lastEventLamport: Long,
)

@Entity(tableName = "clarity_conflict")
data class ConflictRow(
    @PrimaryKey val id: String,
    val kind: String,
    val winnerId: String,
    val loserId: String,
    val areaId: String?,
    val areaNameSnapshot: String?,
    val winnerTitleSnapshot: String?,
    val loserTitleSnapshot: String?,
    val detectedAtLamport: Long,
    val dismissedAt: Long?,
)

// Mapping between the replay state and the cache rows -------------------------

fun AreaState.toRow() = AreaRow(
    id = id,
    name = name,
    colorHex = colorHex,
    orderKey = orderKey,
    archived = archived,
    createdAt = createdAt,
    deletedAt = deletedAt,
    lastEventLamport = lastEventLamport,
)

fun AreaRow.toState() = AreaState(
    id = id,
    name = name,
    colorHex = colorHex,
    orderKey = orderKey,
    archived = archived,
    deletedAt = deletedAt,
    createdAt = createdAt,
    lastEventLamport = lastEventLamport,
)

fun ItemState.toRow() = ItemRow(
    id = id,
    areaId = areaId,
    title = title,
    note = note,
    orderKey = orderKey,
    status = status.name,
    createdAt = createdAt,
    activeSince = activeSince,
    completedAt = completedAt,
    deletedAt = deletedAt,
    lastEventLamport = lastEventLamport,
)

fun ItemRow.toState() = ItemState(
    id = id,
    areaId = areaId,
    title = title,
    note = note,
    orderKey = orderKey,
    status = ItemStatus.valueOf(status),
    createdAt = createdAt,
    activeSince = activeSince,
    completedAt = completedAt,
    deletedAt = deletedAt,
    lastEventLamport = lastEventLamport,
)

fun FocusSessionState.toRow() = FocusSessionRow(
    id = id,
    areaId = areaId,
    itemId = itemId,
    plannedSeconds = plannedSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    actualSeconds = actualSeconds,
    outcome = outcome.name,
    deletedAt = null,
    lastEventLamport = lastEventLamport,
)

fun FocusSessionRow.toState() = FocusSessionState(
    id = id,
    areaId = areaId,
    itemId = itemId,
    plannedSeconds = plannedSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    actualSeconds = actualSeconds,
    outcome = FocusOutcome.valueOf(outcome),
    lastEventLamport = lastEventLamport,
)

fun PulseEntryState.toRow() = PulseEntryRow(
    id = id,
    dateKey = dateKey,
    family = family,
    stage = stage,
    register = register,
    variantKey = variantKey,
    observation = observation,
    question = question,
    factSnapshot = factSnapshot,
    reflectionPeriod = reflectionPeriod.name,
    generatedAt = generatedAt,
    responseKey = responseKey,
    responseLabel = responseLabel,
    responseIsPositive = responseIsPositive,
    answeredAt = answeredAt,
    deletedAt = null,
    lastEventLamport = lastEventLamport,
)

fun PulseEntryRow.toState() = PulseEntryState(
    id = id,
    dateKey = dateKey,
    family = family,
    stage = stage,
    register = register,
    variantKey = variantKey,
    observation = observation,
    question = question,
    factSnapshot = factSnapshot,
    reflectionPeriod = ReflectionPeriod.valueOf(reflectionPeriod),
    generatedAt = generatedAt,
    responseKey = responseKey,
    responseLabel = responseLabel,
    responseIsPositive = responseIsPositive,
    answeredAt = answeredAt,
    lastEventLamport = lastEventLamport,
)

fun ReportState.toRow() = ReportRow(
    id = id,
    weekStartKey = weekStartKey,
    headlineKey = headlineKey,
    sections = sections,
    factSnapshot = factSnapshot,
    generatedAt = generatedAt,
    deletedAt = null,
    lastEventLamport = lastEventLamport,
)

fun ReportRow.toState() = ReportState(
    id = id,
    weekStartKey = weekStartKey,
    headlineKey = headlineKey,
    sections = sections,
    factSnapshot = factSnapshot,
    generatedAt = generatedAt,
    lastEventLamport = lastEventLamport,
)

fun PlanState.toRow() = PlanRow(
    id = id,
    weekStartKey = weekStartKey,
    frameKey = frameKey,
    cueKey = cueKey,
    actionKey = actionKey,
    familyKey = familyKey,
    subjectId = subjectId,
    offeredLine = offeredLine,
    committedLine = committedLine,
    resolutionCategory = resolutionFactRef.category,
    resolutionPath = resolutionFactRef.path,
    offeredAt = offeredAt,
    acceptedAt = acceptedAt,
    resolvedAt = null,
    resolvedValue = null,
    deletedAt = null,
    lastEventLamport = lastEventLamport,
)

fun PlanRow.toState() = PlanState(
    id = id,
    weekStartKey = weekStartKey,
    frameKey = frameKey,
    cueKey = cueKey,
    actionKey = actionKey,
    familyKey = familyKey,
    subjectId = subjectId,
    offeredLine = offeredLine,
    committedLine = committedLine,
    resolutionFactRef = FactRef(resolutionCategory, resolutionPath),
    offeredAt = offeredAt,
    acceptedAt = acceptedAt,
    lastEventLamport = lastEventLamport,
)

fun ClarityConflict.toRow() = ConflictRow(
    id = id,
    kind = kind.name,
    winnerId = winnerId,
    loserId = loserId,
    areaId = areaId,
    areaNameSnapshot = areaNameSnapshot,
    winnerTitleSnapshot = winnerTitleSnapshot,
    loserTitleSnapshot = loserTitleSnapshot,
    detectedAtLamport = detectedAtLamport,
    dismissedAt = null,
)

fun ConflictRow.toState() = ClarityConflict(
    id = id,
    kind = ConflictKind.valueOf(kind),
    winnerId = winnerId,
    loserId = loserId,
    areaId = areaId,
    areaNameSnapshot = areaNameSnapshot,
    winnerTitleSnapshot = winnerTitleSnapshot,
    loserTitleSnapshot = loserTitleSnapshot,
    detectedAtLamport = detectedAtLamport,
)
