package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusExtended
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemEstimated
import com.kamsiob.claritynow.data.event.ItemFiled
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemStatus
import java.time.LocalDate
import java.time.ZoneId

/**
 * The shared shape of a log for the Trail tests, on the `EventStreamGenerator`
 * precedent in `domain.replay`.
 *
 * Every instant is built from a local date and a local time in [TEST_ZONE] rather
 * than from an epoch offset, because half of what these tests exist to catch is a
 * calendar answer computed by dividing milliseconds. A fixture that names its own
 * days in epoch arithmetic cannot fail that way and so cannot test for it.
 *
 * The zone is deliberately not UTC. An implementation that quietly used
 * `ZoneId.systemDefault()` or assumed a zero offset would agree with a UTC fixture
 * on every assertion and be wrong on a real phone.
 */
internal val TEST_ZONE: ZoneId = ZoneId.of("America/New_York")

/** 2026-01-04, a Sunday, so a week boundary falls on day zero. */
internal val TEST_START_DATE: LocalDate = LocalDate.of(2026, 1, 4)

internal const val TEST_ORIGIN = "device-a"

/** A local wall clock instant, [day] days after [TEST_START_DATE], in [TEST_ZONE]. */
internal fun at(day: Int, hour: Int = 9, minute: Int = 0): Long =
    TEST_START_DATE.plusDays(day.toLong())
        .atTime(hour, minute)
        .atZone(TEST_ZONE)
        .toInstant()
        .toEpochMilli()

/** Local midnight of that day. The lower bound of a day window. */
internal fun startOfDay(day: Int): Long = at(day, 0, 0)

/**
 * The local date key of that day, the exact string `APP_OPENED` carries.
 *
 * Derived from [TEST_START_DATE] rather than formatted from a millisecond value,
 * so a fixture cannot accidentally name a different day from the one its events
 * land on. `LocalDate.toString` is ISO-8601, which is the `yyyy-MM-dd` shape
 * MASTER_BUILD_PROMPT 5.2 specifies for the payload.
 */
internal fun dateKey(day: Int): String = TEST_START_DATE.plusDays(day.toLong()).toString()

/**
 * Builds a log the way `ClarityRepository` does: one lamport per event, ascending,
 * with the wall clock stated separately so a test can make the two disagree.
 */
internal class TrailTestLog(private val defaultOrigin: String = TEST_ORIGIN) {

    private val recorded = mutableListOf<ClarityEvent>()
    private var lamport = 0L

    /**
     * Appends one event. [lamportOverride] exists for the merged log cases, where
     * two devices produce events whose wall clocks and lamports disagree about
     * order. Subsequent events continue counting from the override, matching how a
     * device that has just merged advances its own clock.
     */
    fun add(
        wallClock: Long,
        payload: EventPayload,
        origin: String = defaultOrigin,
        lamportOverride: Long? = null,
    ): ClarityEvent {
        lamport = lamportOverride ?: (lamport + 1)
        val event = ClarityEvent.of(
            id = "evt-${recorded.size + 1}-$origin",
            wallClock = wallClock,
            lamport = lamport,
            originId = origin,
            payload = payload,
        )
        recorded += event
        return event
    }

    fun events(): List<ClarityEvent> = recorded.toList()

    fun queries(zone: ZoneId = TEST_ZONE): TrailQueries = TrailQueries(events(), zone)
}

// The shapes every fixture in these tests is built out of. Named so a test reads as
// the sequence of things a person did, not as a wall of payload literals.

internal fun TrailTestLog.area(
    wallClock: Long,
    areaId: String,
    name: String,
    colorHex: String = "#2D7FF9",
    orderKey: String = "a0",
): ClarityEvent = add(wallClock, AreaCreated(areaId, name, colorHex, orderKey))

internal fun TrailTestLog.item(
    wallClock: Long,
    itemId: String,
    areaId: String,
    title: String,
    orderKey: String = "a0",
    areaName: String = "Work",
    estimateMinutes: Int? = null,
): ClarityEvent =
    add(wallClock, ItemAdded(itemId, areaId, title, null, orderKey, areaName, estimateMinutes))

/**
 * A promotion, and the swap that shares its shape. Passing [demotedItemId] is the
 * only thing that makes this a swap, which is a payload fact stated in no prose.
 */
internal fun TrailTestLog.promote(
    wallClock: Long,
    itemId: String,
    areaId: String,
    title: String,
    areaName: String = "Work",
    demotedItemId: String? = null,
    demotedToOrderKey: String? = null,
): ClarityEvent = add(
    wallClock,
    ItemPromoted(
        itemId = itemId,
        areaId = areaId,
        previousStatus = ItemStatus.QUEUED,
        demotedItemId = demotedItemId,
        demotedToOrderKey = demotedToOrderKey,
        titleSnapshot = title,
        areaNameSnapshot = areaName,
    ),
)

internal fun TrailTestLog.complete(
    wallClock: Long,
    itemId: String,
    areaId: String,
    title: String,
    areaName: String = "Work",
    activeDurationDays: Int = 1,
): ClarityEvent = add(
    wallClock,
    ItemCompleted(itemId, areaId, title, areaName, activeDurationDays),
)

/**
 * A capture into the inbox. Addendum 01 4a: no area, and none required.
 *
 * A separate helper from [item] rather than a nullable parameter on it, because the
 * two are different acts and a test that reads `log.item(..., areaId = null)` hides
 * the one fact the test exists to be about. The area name snapshot goes with the
 * area: both set, or neither, which is why there is no way to pass one here.
 */
internal fun TrailTestLog.unfiled(
    wallClock: Long,
    itemId: String,
    title: String,
    orderKey: String = "a0",
    estimateMinutes: Int? = null,
    firstStep: String? = null,
): ClarityEvent = add(
    wallClock,
    ItemAdded(
        itemId = itemId,
        areaId = null,
        title = title,
        note = null,
        orderKey = orderKey,
        areaNameSnapshot = null,
        estimateMinutes = estimateMinutes,
        firstStep = firstStep,
    ),
)

/** Filing an inbox item into an area. The only transition into one. */
internal fun TrailTestLog.file(
    wallClock: Long,
    itemId: String,
    areaId: String,
    orderKey: String = "a0",
    areaName: String = "Work",
): ClarityEvent = add(wallClock, ItemFiled(itemId, areaId, orderKey, areaName))

/** An estimate set, changed or cleared after capture. Addendum 01 4c. */
internal fun TrailTestLog.estimate(
    wallClock: Long,
    itemId: String,
    previousMinutes: Int?,
    newMinutes: Int?,
): ClarityEvent = add(wallClock, ItemEstimated(itemId, previousMinutes, newMinutes))

/** Time added to a running session without ending it. Addendum 01 4f. */
internal fun TrailTestLog.extend(
    wallClock: Long,
    sessionId: String,
    addedSeconds: Int,
    newPlannedSeconds: Int,
): ClarityEvent = add(wallClock, FocusExtended(sessionId, addedSeconds, newPlannedSeconds))

/**
 * The presence marker written on the first foreground of a local day.
 *
 * [day] names both the instant and the date key, so a fixture cannot write an
 * APP_OPENED whose payload disagrees with the day it happened on. The hour defaults
 * to early morning because that is when the real one is written: on the first
 * foreground, which is by definition before whatever the person then did.
 */
internal fun TrailTestLog.opened(day: Int, hour: Int = 7): ClarityEvent =
    add(at(day, hour), AppOpened(dateKey(day)))
