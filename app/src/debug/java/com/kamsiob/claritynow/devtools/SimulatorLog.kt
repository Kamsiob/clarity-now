package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.data.event.AppOpened
import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusEndedEarly
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemStatus
import com.kamsiob.claritynow.domain.query.TrailQueries
import java.time.LocalDate
import java.time.ZoneId

/**
 * The synthetic event log a persona writes, and the small projection it needs to write a
 * legal next event. CLARITY_LOGIC_ENGINE.md 12.
 *
 * ## Why this exists rather than the Trail fixtures
 *
 * `domain.query.TrailTestLog` builds the same shapes and lives in the **test** source set,
 * which the debug source set cannot see. Section 12 puts the simulator in `devtools` and
 * `MASTER_BUILD_PROMPT.md` 11.5 requires it to run before a corpus line is written, so it
 * has to be able to build a log without a test framework present. The payload shapes are
 * the ones `ClarityRepository` writes and nothing here invents a field.
 *
 * ## The projection, and why there is one
 *
 * A persona cannot write `ITEM_COMPLETED` without knowing which item is active, and
 * `ClarityReducer` refuses a completion of anything that is not, so a log written blind
 * would be a log of rejected events and a year of empty fact sets. Replaying the reducer
 * before every write would be quadratic across a simulated year. So the few pieces of
 * state a persona needs, the queue and the active item per area, are tracked here as they
 * are written.
 *
 * **This is a convenience for the writer, never a source of truth for the reader.** Every
 * number the engine sees comes back out through `TrailQueries` over the events, exactly as
 * it does on a phone. Nothing in [queries] consults the maps below.
 *
 * ## Determinism
 *
 * Ids are sequential, the lamport counter is sequential, and every instant is built from a
 * local date and a local time in [zone] rather than from epoch arithmetic. No clock is
 * read and no random number is drawn, here or anywhere a persona reaches: two runs of the
 * same persona produce byte identical logs, which is what the determinism test rests on.
 */
class SimulatorLog(
    val zone: ZoneId,
    val startDate: LocalDate,
    private val originId: String,
) {

    private val recorded = mutableListOf<ClarityEvent>()
    private var lamport = 0L

    private val queues = mutableMapOf<String, MutableList<String>>()
    private val activeItems = mutableMapOf<String, String>()
    private val itemTitles = mutableMapOf<String, String>()
    private val activeSinceDay = mutableMapOf<String, Int>()
    private val areaNames = mutableMapOf<String, String>()
    private var itemCounter = 0
    private var sessionCounter = 0
    private var orderCounter = ORDER_KEY_BASE

    // ---------------------------------------------------------------- calendar

    /** A local wall clock instant on simulated [day], in [zone]. */
    fun at(day: Int, hour: Int, minute: Int = 0): Long =
        startDate.plusDays(day.toLong())
            .atTime(hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

    /** Local midnight opening [day]. The lower bound of a day window. */
    fun startOfDay(day: Int): Long = at(day, 0, 0)

    /**
     * The `yyyy-MM-dd` key of [day].
     *
     * Derived from [startDate] rather than formatted from a millisecond value, so a
     * persona cannot write an `APP_OPENED` whose payload names a different day from the one
     * its instant falls on.
     */
    fun dateKey(day: Int): String = startDate.plusDays(day.toLong()).toString()

    // ---------------------------------------------------------------- appending

    /** Appends one event the way `ClarityRepository` does: one lamport, ascending. */
    fun add(wallClock: Long, payload: EventPayload): ClarityEvent {
        lamport += 1
        val event = ClarityEvent.of(
            id = "evt-${recorded.size + 1}-$originId",
            wallClock = wallClock,
            lamport = lamport,
            originId = originId,
            payload = payload,
        )
        recorded += event
        return event
    }

    fun events(): List<ClarityEvent> = recorded.toList()

    val eventCount: Int get() = recorded.size

    /**
     * The facade over everything written so far.
     *
     * Rebuilt rather than cached, because the log grows on every simulated day and
     * `TrailQueries` memoizes folds against the list it was handed. The simulator builds
     * one per day and reuses it across that day's surfaces.
     */
    fun queries(): TrailQueries = TrailQueries(events(), zone)

    // ---------------------------------------------------------------- the world

    fun queueSize(areaId: String): Int = queues[areaId]?.size ?: 0

    fun activeItem(areaId: String): String? = activeItems[areaId]

    // ---------------------------------------------------------------- acts

    fun createArea(day: Int, hour: Int, areaId: String, name: String, colorHex: String) {
        areaNames[areaId] = name
        queues[areaId] = mutableListOf()
        add(at(day, hour), AreaCreated(areaId, name, colorHex, nextOrderKey()))
    }

    fun archiveArea(day: Int, hour: Int, areaId: String) {
        val name = areaNames[areaId] ?: return
        add(at(day, hour), AreaArchived(areaId, name))
        areaNames.remove(areaId)
        queues.remove(areaId)
        activeItems.remove(areaId)
    }

    /**
     * Writes something down in an area. It arrives QUEUED, per `ClarityReducer.itemAdded`.
     *
     * Returns the new item id so a caller can promote or complete it in the same day.
     */
    fun capture(day: Int, hour: Int, areaId: String, title: String): String {
        itemCounter += 1
        val itemId = "itm-$itemCounter"
        itemTitles[itemId] = title
        queues.getOrPut(areaId) { mutableListOf() }.add(itemId)
        add(
            at(day, hour),
            ItemAdded(
                itemId = itemId,
                areaId = areaId,
                title = title,
                note = null,
                orderKey = nextOrderKey(),
                areaNameSnapshot = areaNames[areaId],
            ),
        )
        return itemId
    }

    /**
     * Promotes the head of the queue, demoting whatever was active.
     *
     * **A promotion that displaces something is a swap**, which the payload states by
     * carrying `demotedItemId`, and that fact is what the `switching` family counts. An
     * area with nothing active promotes without demoting and that is not a swap.
     *
     * Returns the promoted item, or null when the queue is empty.
     */
    fun promoteNext(day: Int, hour: Int, areaId: String): String? {
        val queue = queues[areaId] ?: return null
        if (queue.isEmpty()) return null
        val itemId = queue.removeAt(0)
        val demoted = activeItems[areaId]
        if (demoted != null) {
            queue.add(demoted)
            activeSinceDay.remove(demoted)
        }
        activeItems[areaId] = itemId
        activeSinceDay[itemId] = day
        add(
            at(day, hour),
            ItemPromoted(
                itemId = itemId,
                areaId = areaId,
                previousStatus = ItemStatus.QUEUED,
                demotedItemId = demoted,
                demotedToOrderKey = if (demoted == null) null else nextOrderKey(),
                titleSnapshot = itemTitles.getValue(itemId),
                areaNameSnapshot = areaNames.getValue(areaId),
            ),
        )
        return itemId
    }

    /**
     * Completes the active item, if there is one. Returns the item completed, or null.
     *
     * `activeDurationDays` is the real span from the promotion that made it active, so
     * `TrailQueries.daysActiveForItem` and the payload agree. A fact set built from a log
     * whose durations were invented would make the `persistence` ladder describe a shape
     * the log does not hold.
     */
    fun completeActive(day: Int, hour: Int, areaId: String): String? {
        val itemId = activeItems[areaId] ?: return null
        val since = activeSinceDay[itemId] ?: day
        add(
            at(day, hour),
            ItemCompleted(
                itemId = itemId,
                areaId = areaId,
                titleSnapshot = itemTitles.getValue(itemId),
                areaNameSnapshot = areaNames.getValue(areaId),
                activeDurationDays = (day - since).coerceAtLeast(0),
            ),
        )
        activeItems.remove(areaId)
        activeSinceDay.remove(itemId)
        return itemId
    }

    /** A focus session on the area's active item, finished or ended early. */
    fun focusRun(day: Int, hour: Int, areaId: String, minutes: Int, finished: Boolean) {
        val itemId = activeItems[areaId] ?: return
        sessionCounter += 1
        val sessionId = "fs-$sessionCounter"
        val planned = minutes * SECONDS_PER_MINUTE
        add(at(day, hour), FocusStarted(sessionId, areaId, itemId, planned))
        val actual = if (finished) planned else planned / 2
        val end = at(day, hour) + actual * MILLIS_PER_SECOND
        if (finished) {
            add(end, FocusCompleted(sessionId, actual))
        } else {
            add(end, FocusEndedEarly(sessionId, actual))
        }
    }

    /** The presence marker written on the first foreground of a local day. */
    fun opened(day: Int, hour: Int) {
        add(at(day, hour), AppOpened(dateKey(day)))
    }

    /**
     * Order keys are fractional strings and never integers, per `CLAUDE.md` rule 9.
     *
     * Fixed width so the sequence sorts lexicographically, which is the property the real
     * key allocator has and the one a queue read back out of the log depends on.
     */
    private fun nextOrderKey(): String {
        orderCounter += 1
        return "a$orderCounter"
    }

    private companion object {

        /** Five digits, so a year of a hoarding persona never rolls into a sixth. */
        const val ORDER_KEY_BASE = 10000

        const val SECONDS_PER_MINUTE = 60

        const val MILLIS_PER_SECOND = 1000L
    }
}
