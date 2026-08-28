package com.kamsiob.claritynow.data.widget

import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.domain.replay.AreaState
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.FocusOutcome
import com.kamsiob.claritynow.domain.replay.FocusSessionState
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.domain.replay.PlanState
import com.kamsiob.claritynow.domain.engine.FactRef

/**
 * A projection built by hand, which is unusual in this repository and is right here.
 *
 * Every other test that needs a `ClarityState` folds an event log, because what it is
 * testing is the fold. What these tests are about is the boundary between a projection
 * and a home screen, so the projection is the input and building it directly says what
 * the case is in four lines instead of twenty.
 */
internal object Fixture {

    const val DAY: Long = 86_400_000L

    fun area(
        id: String,
        name: String = id.replaceFirstChar { it.uppercase() },
        colorHex: String = "#2D7FF9",
        orderKey: String = id,
        archived: Boolean = false,
        deletedAt: Long? = null,
        lastEventAt: Long = 0L,
    ) = AreaState(
        id = id,
        name = name,
        colorHex = colorHex,
        orderKey = orderKey,
        archived = archived,
        deletedAt = deletedAt,
        createdAt = 0L,
        lastEventAt = lastEventAt,
        lastEventLamport = 1L,
    )

    fun item(
        id: String,
        areaId: String?,
        title: String = id,
        status: ItemStatus = ItemStatus.QUEUED,
        firstStep: String? = null,
        orderKey: String = id,
    ) = ItemState(
        id = id,
        areaId = areaId,
        title = title,
        firstStep = firstStep,
        orderKey = orderKey,
        status = status,
        createdAt = 0L,
        lastEventLamport = 1L,
    )

    fun session(
        id: String = "session",
        areaId: String,
        itemId: String,
        startedAt: Long = 1_000L,
        plannedSeconds: Int = 1_500,
    ) = FocusSessionState(
        id = id,
        areaId = areaId,
        itemId = itemId,
        plannedSeconds = plannedSeconds,
        startedAt = startedAt,
        outcome = FocusOutcome.RUNNING,
        lastEventLamport = 1L,
    )

    fun plan(
        id: String,
        weekStartKey: String,
        committedLine: String,
        acceptedAt: Long?,
    ) = PlanState(
        id = id,
        weekStartKey = weekStartKey,
        frameKey = "frame",
        cueKey = "cue",
        actionKey = "action",
        familyKey = "family",
        offeredLine = "offered",
        committedLine = committedLine,
        resolutionFactRef = FactRef("counts", "completedThisWeek"),
        offeredAt = 0L,
        acceptedAt = acceptedAt,
        lastEventLamport = 1L,
    )

    fun state(
        areas: List<AreaState> = emptyList(),
        items: List<ItemState> = emptyList(),
        plans: List<PlanState> = emptyList(),
    ) = ClarityState(
        areas = areas.associateBy { it.id },
        items = items.associateBy { it.id },
        plans = plans.associateBy { it.id },
    )
}
