package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemEstimated
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.SettingChanged

/**
 * The pure function every device agrees on.
 *
 * No Android imports, no clock, no randomness, no identifier generation. Given the
 * same ordered event list it produces the same state every time, in any process,
 * on any machine. That is what makes it safe to merge two logs and what will let
 * the Linux desktop app share data with this one without a server.
 *
 * Anything it cannot apply becomes a [ReplayDiagnostic]. It never throws.
 */
object ClarityReducer {

    fun apply(state: ClarityState, event: ClarityEvent): ClarityState {
        val advanced = state.copy(
            lastLamport = maxOf(state.lastLamport, event.lamport),
            eventsApplied = state.eventsApplied + 1,
        )
        return touchArea(reduce(advanced, event), event)
    }

    /**
     * Stamps the area an event concerned with the event's wall clock.
     *
     * Done in one place rather than in twenty branches, so a new event type cannot
     * quietly forget to keep the area's last activity honest. An event that
     * concerns no area, such as a Pulse or a setting, stamps nothing.
     *
     * Two of those now arrive from the same place. An unfiled item concerns no
     * area, so adding, editing or estimating one stamps nothing, and APP_OPENED
     * concerns no area by construction. Neither resolves to a placeholder: an area
     * that had nothing happen in it must not have its last activity moved forward
     * because the person opened the app or wrote something down for later.
     */
    private fun touchArea(state: ClarityState, event: ClarityEvent): ClarityState {
        val areaId = affectedAreaId(state, event) ?: return state
        val area = state.areas[areaId] ?: return state
        if (area.lastEventAt >= event.wallClock) return state
        return state.copy(areas = state.areas + (areaId to area.copy(lastEventAt = event.wallClock)))
    }

    private fun affectedAreaId(state: ClarityState, event: ClarityEvent): String? =
        when (val payload = event.payload) {
            is AreaCreated -> payload.areaId
            is AreaRenamed -> payload.areaId
            is AreaRecolored -> payload.areaId
            is AreaReordered -> payload.areaId
            is AreaArchived -> payload.areaId
            is AreaUnarchived -> payload.areaId
            is AreaDeleted -> payload.areaId
            is ItemAdded -> payload.areaId
            is ItemFiled -> payload.areaId
            is ItemQueued -> payload.areaId
            is ItemPromoted -> payload.areaId
            is ItemCompleted -> payload.areaId
            is ItemReopened -> payload.areaId
            is ItemReordered -> payload.areaId
            is ItemDeleted -> payload.areaId
            is ItemEdited -> state.items[payload.itemId]?.areaId
            is ItemEstimated -> state.items[payload.itemId]?.areaId
            is FocusStarted -> payload.areaId
            is FocusCompleted -> state.focusSessions[payload.sessionId]?.areaId
            is FocusEndedEarly -> state.focusSessions[payload.sessionId]?.areaId
            is FocusExtended -> state.focusSessions[payload.sessionId]?.areaId
            is PulseGenerated, is PulseAnswered, is ReportGenerated,
            is PlanOffered, is PlanAccepted, is SettingChanged, is AppOpened,
            -> null
        }

    private fun reduce(state: ClarityState, event: ClarityEvent): ClarityState {
        val advanced = state
        return when (val payload = event.payload) {
            is AreaCreated -> areaCreated(advanced, event, payload)
            is AreaRenamed -> withLiveArea(advanced, event, payload.areaId) { area ->
                area.copy(name = payload.newName, lastEventLamport = event.lamport)
            }
            is AreaRecolored -> withLiveArea(advanced, event, payload.areaId) { area ->
                area.copy(colorHex = payload.newHex, lastEventLamport = event.lamport)
            }
            is AreaReordered -> withLiveArea(advanced, event, payload.areaId) { area ->
                area.copy(orderKey = payload.newOrderKey, lastEventLamport = event.lamport)
            }
            is AreaArchived -> withLiveArea(advanced, event, payload.areaId) { area ->
                area.copy(archived = true, lastEventLamport = event.lamport)
            }
            is AreaUnarchived -> withLiveArea(advanced, event, payload.areaId) { area ->
                area.copy(archived = false, lastEventLamport = event.lamport)
            }
            is AreaDeleted -> areaDeleted(advanced, event, payload)

            is ItemAdded -> itemAdded(advanced, event, payload)
            is ItemFiled -> itemFiled(advanced, event, payload)
            is ItemEdited -> withLiveItem(advanced, event, payload.itemId) { item ->
                item.copy(
                    title = payload.newTitle,
                    note = payload.newNote,
                    firstStep = payload.newFirstStep,
                    lastEventLamport = event.lamport,
                )
            }
            // The previous estimate is carried for the record and is not consulted,
            // exactly as ItemEdited does not consult previousTitle. The event states
            // the value after it, and that is what a projection is.
            is ItemEstimated -> withLiveItem(advanced, event, payload.itemId) { item ->
                item.copy(
                    estimateMinutes = payload.newEstimateMinutes,
                    lastEventLamport = event.lamport,
                )
            }
            is ItemQueued -> withLiveItem(advanced, event, payload.itemId) { item ->
                item.copy(
                    status = ItemStatus.QUEUED,
                    orderKey = payload.orderKey,
                    activeSince = null,
                    lastEventLamport = event.lamport,
                )
            }
            is ItemPromoted -> itemPromoted(advanced, event, payload)
            is ItemCompleted -> itemCompleted(advanced, event, payload)
            is ItemReopened -> itemReopened(advanced, event, payload)
            is ItemReordered -> withLiveItem(advanced, event, payload.itemId) { item ->
                item.copy(orderKey = payload.newOrderKey, lastEventLamport = event.lamport)
            }
            is ItemDeleted -> withLiveItem(advanced, event, payload.itemId) { item ->
                item.copy(deletedAt = event.wallClock, lastEventLamport = event.lamport)
            }

            is FocusStarted -> focusStarted(advanced, event, payload)
            is FocusCompleted -> focusEnded(
                advanced, event, payload.sessionId, payload.actualSeconds, FocusOutcome.COMPLETED,
            )
            is FocusEndedEarly -> focusEnded(
                advanced, event, payload.sessionId, payload.actualSeconds, FocusOutcome.ENDED_EARLY,
            )
            is FocusExtended -> focusExtended(advanced, event, payload)

            is PulseGenerated -> pulseGenerated(advanced, event, payload)
            is PulseAnswered -> pulseAnswered(advanced, event, payload)
            is ReportGenerated -> reportGenerated(advanced, event, payload)

            is PlanOffered -> planOffered(advanced, event, payload)
            is PlanAccepted -> planAccepted(advanced, event, payload)

            is SettingChanged -> advanced.copy(
                settings = advanced.settings + (payload.key to payload.newValue),
            )

            // APP_OPENED changes nothing, and has its own branch saying so rather
            // than falling through a default, because there is no default here to
            // fall through and there must never be one. Every other type in this
            // `when` moves the projection; this one is a presence marker whose only
            // reader is the gap detection in Addendum 01 4d, which folds the log
            // itself. Storing a last opened date on the state would be a second
            // path to the same fact and would put a tally of someone's presence in
            // the projection every screen reads, which is what 4d exists to avoid.
            is AppOpened -> advanced
        }
    }

    // Areas -------------------------------------------------------------------

    private fun areaCreated(state: ClarityState, event: ClarityEvent, payload: AreaCreated): ClarityState {
        if (state.areas.containsKey(payload.areaId)) {
            return state.note(event, "area ${payload.areaId} already exists")
        }
        val area = AreaState(
            id = payload.areaId,
            name = payload.name,
            colorHex = payload.colorHex,
            orderKey = payload.orderKey,
            createdAt = event.wallClock,
            lastEventAt = event.wallClock,
            lastEventLamport = event.lamport,
        )
        return state.copy(areas = state.areas + (area.id to area))
    }

    /**
     * Deleting an area tombstones its items too. The items get no events of their
     * own, because a cascade computed identically on every device is cheaper than
     * writing one event per item and cannot fall out of step with the parent.
     */
    private fun areaDeleted(state: ClarityState, event: ClarityEvent, payload: AreaDeleted): ClarityState {
        val area = state.areas[payload.areaId]
            ?: return state.note(event, "unknown area ${payload.areaId}")
        if (area.deletedAt != null) return state

        val tombstonedItems = state.items.mapValues { (_, item) ->
            if (item.areaId == payload.areaId && item.deletedAt == null) {
                item.copy(deletedAt = event.wallClock, lastEventLamport = event.lamport)
            } else {
                item
            }
        }
        return state.copy(
            areas = state.areas + (
                area.id to area.copy(
                    deletedAt = event.wallClock,
                    lastEventLamport = event.lamport,
                )
                ),
            items = tombstonedItems,
        )
    }

    // Items -------------------------------------------------------------------

    /**
     * Adding an item, filed or unfiled.
     *
     * A null area is not a missing area and is not a failure to resolve one. It is
     * the inbox, and it is the whole point of Addendum 01 4a: capture must never
     * require a decision. So the area checks run only when the payload names one,
     * and an unfiled add is never a diagnostic.
     *
     * A named area that is unknown or deleted still refuses the event, unchanged.
     * The tempting shortcut, dropping the item into the inbox instead so nothing is
     * lost, is wrong: it would silently move an item somewhere the person did not
     * put it, and every device replaying the same log would have to make the same
     * silent move for the states to agree. Refusing and recording is the behavior
     * MASTER_BUILD_PROMPT 6.2 asks for and is the same answer on every device.
     */
    private fun itemAdded(state: ClarityState, event: ClarityEvent, payload: ItemAdded): ClarityState {
        val areaId = payload.areaId
        if (areaId != null) {
            val area = state.areas[areaId]
                ?: return state.note(event, "unknown area $areaId")
            if (area.deletedAt != null) {
                return state.note(event, "area $areaId is deleted")
            }
        }
        if (state.items.containsKey(payload.itemId)) {
            return state.note(event, "item ${payload.itemId} already exists")
        }
        val item = ItemState(
            id = payload.itemId,
            areaId = areaId,
            title = payload.title,
            note = payload.note,
            firstStep = payload.firstStep,
            estimateMinutes = payload.estimateMinutes,
            orderKey = payload.orderKey,
            status = ItemStatus.QUEUED,
            createdAt = event.wallClock,
            lastEventLamport = event.lamport,
        )
        return state.copy(items = state.items + (item.id to item))
    }

    /**
     * Filing an unfiled item into an area. The only transition into one.
     *
     * **It never promotes and never demotes.** The item arrives QUEUED and stays
     * QUEUED, taking the order key the payload names, and whatever was active in
     * that area is untouched. Filing is bookkeeping; promotion is a choice about
     * what to do next. An inbox that could displace the one thing a person is
     * working on would make the safest possible act, writing something down, the
     * most disruptive one.
     *
     * Refused for an item that already has an area, because there is no unfile and
     * no move, so a second filing is either a duplicate or a bug and either way the
     * first answer is the one both devices already agree on. Refused for an area
     * that is unknown, deleted or archived, per MASTER_BUILD_PROMPT 6.2, which
     * leaves the item unfiled rather than losing it: an inbox item that stays in
     * the inbox is visible and refilable, and one filed into an archived area is
     * neither.
     *
     * The status is set rather than assumed. An unfiled item is always QUEUED, so
     * this is a no-op on every log this build can write, and it is written down
     * because the rule is worth stating where a later reader will see it.
     */
    private fun itemFiled(state: ClarityState, event: ClarityEvent, payload: ItemFiled): ClarityState {
        val item = state.items[payload.itemId]
            ?: return state.note(event, "unknown item ${payload.itemId}")
        if (item.deletedAt != null) return state.note(event, "item ${payload.itemId} is deleted")
        if (item.areaId != null) {
            return state.note(event, "item ${payload.itemId} is already filed in ${item.areaId}")
        }
        val area = state.areas[payload.areaId]
            ?: return state.note(event, "unknown area ${payload.areaId}")
        if (area.deletedAt != null) return state.note(event, "area ${payload.areaId} is deleted")
        if (area.archived) return state.note(event, "area ${payload.areaId} is archived")
        return state.copy(
            items = state.items + (
                item.id to item.copy(
                    areaId = payload.areaId,
                    orderKey = payload.orderKey,
                    status = ItemStatus.QUEUED,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    /**
     * Promotion, and the swap that shares its shape.
     *
     * When the log declares which item was demoted, that is what happens. When an
     * item is already active and was not the declared loser, two devices promoted
     * independently: the event being applied has the higher order by construction,
     * so it wins, the sitting item goes to the head of the queue, and a conflict is
     * recorded for the Areas screen to explain.
     *
     * An unfiled item is refused outright. Addendum 01 4a: an item with no area
     * cannot be active until it is filed, and this is the one branch that could
     * otherwise reach that state, because it is the only place a status becomes
     * ACTIVE. Left unguarded it would produce an item that is the one thing
     * happening in no area at all, which `ClarityInvariants` would then report
     * against a state that had already been shown to someone.
     */
    private fun itemPromoted(state: ClarityState, event: ClarityEvent, payload: ItemPromoted): ClarityState {
        val item = state.items[payload.itemId]
            ?: return state.note(event, "unknown item ${payload.itemId}")
        if (item.deletedAt != null) return state.note(event, "item ${payload.itemId} is deleted")
        if (item.areaId == null) {
            return state.note(event, "item ${payload.itemId} is unfiled and cannot be promoted")
        }
        if (state.areas[payload.areaId]?.deletedAt != null) {
            return state.note(event, "area ${payload.areaId} is deleted")
        }

        val sitting = state.activeItemIn(payload.areaId)?.takeIf { it.id != payload.itemId }
        var items = state.items
        var conflicts = state.conflicts

        if (sitting != null) {
            val declaredLoser = payload.demotedItemId
            val demotedKey = if (declaredLoser == sitting.id && payload.demotedToOrderKey != null) {
                payload.demotedToOrderKey
            } else {
                freshHeadKey(state, payload.areaId, event.originId)
            }
            items = items + (
                sitting.id to sitting.copy(
                    status = ItemStatus.QUEUED,
                    orderKey = demotedKey,
                    activeSince = null,
                    lastEventLamport = event.lamport,
                )
                )
            if (declaredLoser != sitting.id) {
                conflicts = conflicts + ClarityConflict(
                    id = "conflict:${event.id}:two-active",
                    kind = ConflictKind.TWO_ACTIVE_IN_AREA,
                    winnerId = payload.itemId,
                    loserId = sitting.id,
                    areaId = payload.areaId,
                    areaNameSnapshot = payload.areaNameSnapshot,
                    winnerTitleSnapshot = payload.titleSnapshot,
                    loserTitleSnapshot = sitting.title,
                    detectedAtLamport = event.lamport,
                )
            }
        }

        items = items + (
            item.id to item.copy(
                status = ItemStatus.ACTIVE,
                activeSince = event.wallClock,
                completedAt = null,
                lastEventLamport = event.lamport,
            )
            )
        return state.copy(items = items, conflicts = conflicts)
    }

    private fun itemCompleted(state: ClarityState, event: ClarityEvent, payload: ItemCompleted): ClarityState {
        val item = state.items[payload.itemId]
            ?: return state.note(event, "unknown item ${payload.itemId}")
        if (item.deletedAt != null) return state.note(event, "item ${payload.itemId} is deleted")
        if (item.status == ItemStatus.COMPLETED) return state
        if (item.status != ItemStatus.ACTIVE) {
            return state.note(event, "only an active item can be completed, ${payload.itemId} is ${item.status}")
        }
        return state.copy(
            items = state.items + (
                item.id to item.copy(
                    status = ItemStatus.COMPLETED,
                    completedAt = event.wallClock,
                    activeSince = null,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    private fun itemReopened(state: ClarityState, event: ClarityEvent, payload: ItemReopened): ClarityState {
        val item = state.items[payload.itemId]
            ?: return state.note(event, "unknown item ${payload.itemId}")
        if (item.deletedAt != null) return state.note(event, "item ${payload.itemId} is deleted")
        return state.copy(
            items = state.items + (
                item.id to item.copy(
                    status = ItemStatus.QUEUED,
                    orderKey = payload.targetOrderKey,
                    completedAt = null,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    // Focus -------------------------------------------------------------------

    private fun focusStarted(state: ClarityState, event: ClarityEvent, payload: FocusStarted): ClarityState {
        if (state.focusSessions.containsKey(payload.sessionId)) return state
        if (!state.items.containsKey(payload.itemId)) {
            return state.note(event, "unknown item ${payload.itemId}")
        }
        if (!state.areas.containsKey(payload.areaId)) {
            return state.note(event, "unknown area ${payload.areaId}")
        }
        val session = FocusSessionState(
            id = payload.sessionId,
            areaId = payload.areaId,
            itemId = payload.itemId,
            plannedSeconds = payload.plannedSeconds,
            startedAt = event.wallClock,
            lastEventLamport = event.lamport,
        )
        return state.copy(focusSessions = state.focusSessions + (session.id to session))
    }

    /**
     * Adding time to a running session. Addendum 01 4f.
     *
     * The absolute [FocusExtended.newPlannedSeconds] is applied rather than adding
     * [FocusExtended.addedSeconds] to what is already there. Applying the delta
     * would make the result depend on how many times the event was folded, which is
     * the failure mode idempotency exists to prevent: delivering the same event
     * twice after a merge would add the time twice, and nothing on screen would say
     * so. The absolute figure is also the number the person was shown.
     *
     * Extending a session that has already ended is recorded rather than silently
     * applied. It is not the same case as a second terminal event, which is a
     * benign duplicate the reducer swallows; this one means a plan was changed for
     * something that was already over, and quietly moving a finished session's
     * planned time would leave a session whose plan it never actually ran under.
     */
    private fun focusExtended(state: ClarityState, event: ClarityEvent, payload: FocusExtended): ClarityState {
        val session = state.focusSessions[payload.sessionId]
            ?: return state.note(event, "unknown focus session ${payload.sessionId}")
        if (session.outcome != FocusOutcome.RUNNING) {
            return state.note(event, "focus session ${payload.sessionId} is ${session.outcome}")
        }
        return state.copy(
            focusSessions = state.focusSessions + (
                session.id to session.copy(
                    plannedSeconds = payload.newPlannedSeconds,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    private fun focusEnded(
        state: ClarityState,
        event: ClarityEvent,
        sessionId: String,
        actualSeconds: Int,
        outcome: FocusOutcome,
    ): ClarityState {
        val session = state.focusSessions[sessionId]
            ?: return state.note(event, "unknown focus session $sessionId")
        if (session.outcome != FocusOutcome.RUNNING) return state
        return state.copy(
            focusSessions = state.focusSessions + (
                session.id to session.copy(
                    outcome = outcome,
                    actualSeconds = actualSeconds,
                    endedAt = event.wallClock,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    // Pulse, Report, Plans ----------------------------------------------------

    private fun pulseGenerated(state: ClarityState, event: ClarityEvent, payload: PulseGenerated): ClarityState {
        val existing = state.pulses[payload.dateKey]
        if (existing != null && existing.id == payload.pulseId) return state

        val entry = PulseEntryState(
            id = payload.pulseId,
            dateKey = payload.dateKey,
            family = payload.family,
            stage = payload.escalationStage,
            register = payload.register,
            variantKey = payload.variantKey,
            observation = payload.renderedObservation,
            question = payload.renderedQuestion,
            factSnapshot = payload.factSnapshot,
            reflectionPeriod = payload.reflectionPeriod,
            generatedAt = event.wallClock,
            lastEventLamport = event.lamport,
        )
        val conflicts = if (existing == null) {
            state.conflicts
        } else {
            state.conflicts + ClarityConflict(
                id = "conflict:${event.id}:duplicate-pulse",
                kind = ConflictKind.DUPLICATE_DATE_KEY,
                winnerId = payload.pulseId,
                loserId = existing.id,
                detectedAtLamport = event.lamport,
            )
        }
        return state.copy(
            pulses = state.pulses + (payload.dateKey to entry),
            conflicts = conflicts,
        )
    }

    private fun pulseAnswered(state: ClarityState, event: ClarityEvent, payload: PulseAnswered): ClarityState {
        val entry = state.pulses.values.firstOrNull { it.id == payload.pulseId }
            ?: return state.note(event, "unknown or superseded pulse ${payload.pulseId}")
        if (entry.isAnswered) return state
        return state.copy(
            pulses = state.pulses + (
                entry.dateKey to entry.copy(
                    responseKey = payload.responseKey,
                    responseLabel = payload.responseLabel,
                    responseIsPositive = payload.responseIsPositive,
                    answeredAt = event.wallClock,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    private fun reportGenerated(state: ClarityState, event: ClarityEvent, payload: ReportGenerated): ClarityState {
        val existing = state.reports[payload.weekStartKey]
        if (existing != null && existing.id == payload.reportId) return state

        val report = ReportState(
            id = payload.reportId,
            weekStartKey = payload.weekStartKey,
            headlineKey = payload.headlineKey,
            sections = payload.renderedSections,
            factSnapshot = payload.factSnapshot,
            generatedAt = event.wallClock,
            lastEventLamport = event.lamport,
            windowStartKey = payload.windowStartKey,
            headlineText = payload.headlineText,
        )
        // A regenerate on the same device replaces the week without a conflict; a
        // conflict is only recorded when the losing report came from elsewhere.
        val conflicts = if (existing == null) {
            state.conflicts
        } else {
            state.conflicts + ClarityConflict(
                id = "conflict:${event.id}:duplicate-report",
                kind = ConflictKind.DUPLICATE_DATE_KEY,
                winnerId = payload.reportId,
                loserId = existing.id,
                detectedAtLamport = event.lamport,
            )
        }
        return state.copy(
            reports = state.reports + (payload.weekStartKey to report),
            conflicts = conflicts,
        )
    }

    private fun planOffered(state: ClarityState, event: ClarityEvent, payload: PlanOffered): ClarityState {
        if (state.plans.containsKey(payload.planId)) return state
        val plan = PlanState(
            id = payload.planId,
            weekStartKey = payload.weekStartKey,
            frameKey = payload.frameKey,
            cueKey = payload.cueKey,
            actionKey = payload.actionKey,
            familyKey = payload.familyKey,
            subjectId = payload.subjectId,
            offeredLine = payload.offeredLine,
            committedLine = payload.committedLine,
            resolutionFactRef = payload.resolutionFactRef,
            offeredAt = event.wallClock,
            lastEventLamport = event.lamport,
        )
        return state.copy(plans = state.plans + (plan.id to plan))
    }

    private fun planAccepted(state: ClarityState, event: ClarityEvent, payload: PlanAccepted): ClarityState {
        val plan = state.plans[payload.planId]
            ?: return state.note(event, "unknown plan ${payload.planId}")
        if (plan.isAccepted) return state
        return state.copy(
            plans = state.plans + (
                plan.id to plan.copy(
                    acceptedAt = event.wallClock,
                    lastEventLamport = event.lamport,
                )
                ),
        )
    }

    // Helpers -----------------------------------------------------------------

    /** Applies [change] to a live area, or records why it could not. */
    private inline fun withLiveArea(
        state: ClarityState,
        event: ClarityEvent,
        areaId: String,
        change: (AreaState) -> AreaState,
    ): ClarityState {
        val area = state.areas[areaId] ?: return state.note(event, "unknown area $areaId")
        // Delete wins. The edit stays in the log and has no effect.
        if (area.deletedAt != null) return state.note(event, "area $areaId is deleted")
        return state.copy(areas = state.areas + (areaId to change(area)))
    }

    private inline fun withLiveItem(
        state: ClarityState,
        event: ClarityEvent,
        itemId: String,
        change: (ItemState) -> ItemState,
    ): ClarityState {
        val item = state.items[itemId] ?: return state.note(event, "unknown item $itemId")
        if (item.deletedAt != null) return state.note(event, "item $itemId is deleted")
        return state.copy(items = state.items + (itemId to change(item)))
    }

    /**
     * A key strictly ahead of the current queue head.
     *
     * The fallbacks exist so that an exhausted key space, which needs twenty six
     * nested insertions at the same point to reach, degrades into a wrong position
     * rather than a crash during replay.
     */
    private fun freshHeadKey(state: ClarityState, areaId: String, originId: String): String {
        val jitter = OrderKey.jitterFor(originId)
        val queue = state.queueIn(areaId)
        if (queue.isEmpty()) return OrderKey.first(jitter)
        return runCatching { OrderKey.before(queue.first().orderKey, jitter) }
            .recoverCatching { OrderKey.last(queue.last().orderKey, jitter) }
            .getOrElse { OrderKey.first(jitter) }
    }

    private fun ClarityState.note(event: ClarityEvent, reason: String): ClarityState =
        copy(
            diagnostics = diagnostics + ReplayDiagnostic(
                eventId = event.id,
                eventType = event.type.name,
                reason = reason,
            ),
        )
}
