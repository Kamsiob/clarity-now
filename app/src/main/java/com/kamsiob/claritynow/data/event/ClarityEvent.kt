package com.kamsiob.claritynow.data.event

/**
 * One immutable, append only fact about what happened. MASTER_BUILD_PROMPT 5.1.
 *
 * This class is plain Kotlin with no Room annotations, because the reducer in
 * `domain.replay` must stay free of Android imports. The Room row that persists it
 * lives in `data.db` and maps both ways.
 *
 * @param wallClock display only. Never used for ordering; two devices will disagree.
 * @param lamport the logical clock, advanced to max(local, seen) + 1 on merge.
 * @param originId a UUID generated once at install. Identifies the device, never the person.
 */
data class ClarityEvent(
    val id: String,
    val schemaVersion: Int,
    val type: ClarityEventType,
    val wallClock: Long,
    val lamport: Long,
    val originId: String,
    val payload: EventPayload,
    val entityId: String?,
) {
    init {
        require(payloadMatchesType(type, payload)) {
            "payload ${payload::class.simpleName} does not belong to event type $type"
        }
    }

    companion object {
        /**
         * Present from the first event ever written. A log that starts without a
         * version cannot be migrated later without guessing.
         *
         * **Still 1 after the Addendum 01 schema commit, deliberately.** A version
         * exists so that a reader can tell two shapes apart and accept both, and
         * nothing has to. Every field that commit added is optional with a null
         * default, `ClarityEventJson` decodes with `ignoreUnknownKeys`, and an
         * absent key reads as the default, so a log written by an earlier build
         * replays unchanged. The one type that was renamed, FOCUS_ABANDONED, is
         * written by a phase that has not shipped, so no log anywhere contains it.
         * Moving the number with nothing to distinguish would spend the signal.
         */
        const val SCHEMA_VERSION = 1

        /**
         * The total order every device agrees on.
         *
         * `wallClock` is deliberately absent. `id` is the final tiebreak so that
         * even a same device, same lamport collision, which should be impossible,
         * still produces one answer rather than an arbitrary one.
         */
        val TOTAL_ORDER: Comparator<ClarityEvent> = compareBy({ it.lamport }, { it.originId }, { it.id })

        fun payloadMatchesType(type: ClarityEventType, payload: EventPayload): Boolean =
            when (type) {
                ClarityEventType.AREA_CREATED -> payload is AreaCreated
                ClarityEventType.AREA_RENAMED -> payload is AreaRenamed
                ClarityEventType.AREA_RECOLORED -> payload is AreaRecolored
                ClarityEventType.AREA_REORDERED -> payload is AreaReordered
                ClarityEventType.AREA_ARCHIVED -> payload is AreaArchived
                ClarityEventType.AREA_UNARCHIVED -> payload is AreaUnarchived
                ClarityEventType.AREA_DELETED -> payload is AreaDeleted
                ClarityEventType.ITEM_ADDED -> payload is ItemAdded
                ClarityEventType.ITEM_FILED -> payload is ItemFiled
                ClarityEventType.ITEM_EDITED -> payload is ItemEdited
                ClarityEventType.ITEM_ESTIMATED -> payload is ItemEstimated
                ClarityEventType.ITEM_QUEUED -> payload is ItemQueued
                ClarityEventType.ITEM_PROMOTED -> payload is ItemPromoted
                ClarityEventType.ITEM_COMPLETED -> payload is ItemCompleted
                ClarityEventType.ITEM_REOPENED -> payload is ItemReopened
                ClarityEventType.ITEM_REORDERED -> payload is ItemReordered
                ClarityEventType.ITEM_DELETED -> payload is ItemDeleted
                ClarityEventType.FOCUS_STARTED -> payload is FocusStarted
                ClarityEventType.FOCUS_COMPLETED -> payload is FocusCompleted
                ClarityEventType.FOCUS_ENDED_EARLY -> payload is FocusEndedEarly
                ClarityEventType.FOCUS_EXTENDED -> payload is FocusExtended
                ClarityEventType.PULSE_GENERATED -> payload is PulseGenerated
                ClarityEventType.PULSE_ANSWERED -> payload is PulseAnswered
                ClarityEventType.REPORT_GENERATED -> payload is ReportGenerated
                ClarityEventType.PLAN_OFFERED -> payload is PlanOffered
                ClarityEventType.PLAN_ACCEPTED -> payload is PlanAccepted
                ClarityEventType.SETTING_CHANGED -> payload is SettingChanged
                ClarityEventType.APP_OPENED -> payload is AppOpened
            }

        fun typeOf(payload: EventPayload): ClarityEventType = when (payload) {
            is AreaCreated -> ClarityEventType.AREA_CREATED
            is AreaRenamed -> ClarityEventType.AREA_RENAMED
            is AreaRecolored -> ClarityEventType.AREA_RECOLORED
            is AreaReordered -> ClarityEventType.AREA_REORDERED
            is AreaArchived -> ClarityEventType.AREA_ARCHIVED
            is AreaUnarchived -> ClarityEventType.AREA_UNARCHIVED
            is AreaDeleted -> ClarityEventType.AREA_DELETED
            is ItemAdded -> ClarityEventType.ITEM_ADDED
            is ItemFiled -> ClarityEventType.ITEM_FILED
            is ItemEdited -> ClarityEventType.ITEM_EDITED
            is ItemEstimated -> ClarityEventType.ITEM_ESTIMATED
            is ItemQueued -> ClarityEventType.ITEM_QUEUED
            is ItemPromoted -> ClarityEventType.ITEM_PROMOTED
            is ItemCompleted -> ClarityEventType.ITEM_COMPLETED
            is ItemReopened -> ClarityEventType.ITEM_REOPENED
            is ItemReordered -> ClarityEventType.ITEM_REORDERED
            is ItemDeleted -> ClarityEventType.ITEM_DELETED
            is FocusStarted -> ClarityEventType.FOCUS_STARTED
            is FocusCompleted -> ClarityEventType.FOCUS_COMPLETED
            is FocusEndedEarly -> ClarityEventType.FOCUS_ENDED_EARLY
            is FocusExtended -> ClarityEventType.FOCUS_EXTENDED
            is PulseGenerated -> ClarityEventType.PULSE_GENERATED
            is PulseAnswered -> ClarityEventType.PULSE_ANSWERED
            is ReportGenerated -> ClarityEventType.REPORT_GENERATED
            is PlanOffered -> ClarityEventType.PLAN_OFFERED
            is PlanAccepted -> ClarityEventType.PLAN_ACCEPTED
            is SettingChanged -> ClarityEventType.SETTING_CHANGED
            is AppOpened -> ClarityEventType.APP_OPENED
        }

        /** Builds an event from a payload, deriving type and entityId. */
        fun of(
            id: String,
            wallClock: Long,
            lamport: Long,
            originId: String,
            payload: EventPayload,
            schemaVersion: Int = SCHEMA_VERSION,
        ): ClarityEvent = ClarityEvent(
            id = id,
            schemaVersion = schemaVersion,
            type = typeOf(payload),
            wallClock = wallClock,
            lamport = lamport,
            originId = originId,
            payload = payload,
            entityId = payload.primaryEntityId,
        )
    }
}

/**
 * Sorts into the total order and removes duplicate deliveries of the same event id.
 *
 * Idempotency lives here rather than in the reducer so the reducer stays a plain
 * fold. Delivering the same event twice must not change state, and after a merge
 * of two files that both contain a shared ancestor, it always will be.
 */
fun List<ClarityEvent>.inTotalOrder(): List<ClarityEvent> {
    val sorted = sortedWith(ClarityEvent.TOTAL_ORDER)
    val seen = HashSet<String>(sorted.size)
    return sorted.filter { seen.add(it.id) }
}
