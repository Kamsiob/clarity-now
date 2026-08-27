package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.AreaArchived
import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.AreaDeleted
import com.kamsiob.claritynow.data.event.AreaRecolored
import com.kamsiob.claritynow.data.event.AreaRenamed
import com.kamsiob.claritynow.data.event.AreaReordered
import com.kamsiob.claritynow.data.event.AreaUnarchived
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.FocusAbandoned
import com.kamsiob.claritynow.data.event.FocusCompleted
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import com.kamsiob.claritynow.data.event.PlanAccepted
import com.kamsiob.claritynow.data.event.PlanOffered
import com.kamsiob.claritynow.data.event.PulseAnswered
import com.kamsiob.claritynow.data.event.PulseGenerated
import com.kamsiob.claritynow.data.event.ReportGenerated
import com.kamsiob.claritynow.data.event.SettingChanged
import com.kamsiob.claritynow.data.event.inTotalOrder
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * The only path from the event log to a displayed number. MASTER_BUILD_PROMPT 9.
 *
 * The caller decides the extent of [events]. The engine hands it the whole log; the
 * Trail screen hands it one page. Every function is a fold over exactly what it was
 * given and reads no clock, so the same call with the same list and the same bounds
 * answers the same on two devices and on the Linux build that will one day
 * reimplement this from the same specification.
 *
 * [zone] supplies calendar boundaries only. It is never used to ask what time it is.
 * A zone is not a clock: it converts an instant the caller already supplied into a
 * calendar date, and `activeDays`, `eventsPerDay` and the busiest day facts are
 * literally uncomputable without one. What nothing in this package may ever call is
 * `Instant.now`, `LocalDate.now`, `ZoneId.systemDefault` or `System.currentTimeMillis`.
 *
 * **Two conventions hold everywhere, and both are load bearing.**
 *
 * Every window is half open, `[startMillis, endMillis)`, and `atMillis`,
 * `asOfMillis` and `beforeMillis` mean strictly before that instant. So
 * `queueSizeAt(startOfSunday)` reads "the queue as Sunday began", which is what the
 * approved Report line "longer than they were on Sunday" is comparing against. The
 * three `AsOf` snapshot resolvers are the deliberate exception and say so on
 * themselves.
 *
 * Anything that folds state forward filters the log by `wallClock` and folds the
 * survivors in `(lamport, originId, id)` order. Sorting a fold by `wallClock`
 * produces wrong state on a merged log and only on a merged log, which is the one
 * case nobody ever checks by hand. `inTotalOrder()` in the constructor is half of
 * that guarantee; never breaking out of a wall clock filter early is the other half,
 * because a later event in the total order is allowed to carry an earlier wall clock.
 *
 * **Streak facts are deliberately absent.** CLARITY_LOGIC_ENGINE.md 3.1: "No streak
 * facts exist. Deliberately. No `currentStreak`, no `longestStreak`, no
 * `daysInARow`. Their absence makes it structurally impossible for streak language
 * to appear by accident. Do not add them." [activeDayKeys] makes a streak three
 * lines of work. Those three lines must never be written here.
 */
class TrailQueries(
    events: List<ClarityEvent>,
    private val zone: ZoneId,
) {

    /**
     * Sorted and deduplicated once, at construction.
     *
     * An unsorted list is the single most likely source of a silently wrong number,
     * and no caller can hand one to this class. Doing it here also costs one sort
     * per fact extraction rather than one per function, which matters when layer one
     * of the engine builds thirty facts in a single pass.
     */
    private val log: List<ClarityEvent> = events.inTotalOrder()

    /**
     * Every event grouped by the entity it is primarily about, still in total order.
     *
     * Keyed on `payload.primaryEntityId` rather than on the row's `entityId` column,
     * because the payload is the authority and the column is a denormalized copy of
     * it. Built lazily: a `TrailQueries` constructed only to count a window never
     * pays for it.
     */
    private val byEntity: Map<String, List<ClarityEvent>> by lazy {
        log.groupBy { it.payload.primaryEntityId }
    }

    /**
     * The most recent forward fold, kept so that asking three questions about one
     * instant costs one replay rather than three. Memoization changes no answer;
     * it is stored as a single reference so a concurrent reader sees either the
     * whole previous result or none of it, never half of it.
     */
    private var memo: Pair<Long, ClarityState>? = null

    // Completions -------------------------------------------------------------

    /** ITEM_COMPLETED events in `[startMillis, endMillis)`. */
    fun completionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_COMPLETED }

    /** Completions in the window keyed by the area the payload names. */
    fun completionsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload is ItemCompleted) counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * Every completion in the window with its snapshots, newest first.
     *
     * Newest first uses the display order, not the total order: a completion and the
     * promotion that followed it share one wall clock reading, and `lamport`
     * descending is what keeps a same instant pair in the order they happened.
     */
    fun completedItemsBetween(startMillis: Long, endMillis: Long): List<CompletedRecord> =
        eventsIn(startMillis, endMillis)
            .filter { it.payload is ItemCompleted }
            .sortedWith(TRAIL_DISPLAY_ORDER)
            .mapNotNull { event ->
                val payload = event.payload as? ItemCompleted ?: return@mapNotNull null
                CompletedRecord(
                    itemId = payload.itemId,
                    titleSnapshot = payload.titleSnapshot,
                    areaId = payload.areaId,
                    areaNameSnapshot = payload.areaNameSnapshot,
                    activeDurationDays = payload.activeDurationDays,
                    completedAt = event.wallClock,
                )
            }

    /** Completions bucketed by local calendar day. Days with none are absent. */
    fun completionsPerDay(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (event.type != ClarityEventType.ITEM_COMPLETED) continue
            val key = dateKeyOf(event.wallClock)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    // Events and areas --------------------------------------------------------

    /**
     * User activity events in the window keyed by resolved area.
     *
     * An event whose area cannot be resolved is absent from the map entirely rather
     * than bucketed under a placeholder key. A placeholder would eventually be
     * printed, and CLARITY_LOGIC_ENGINE.md 1 is blunt about what one fabricated area
     * name costs.
     */
    fun eventsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (!event.type.isUserActivity) continue
            val areaId = areaIdOf(event) ?: continue
            counts[areaId] = (counts[areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /** User activity events in the window. The denominator of every share. */
    fun totalEvents(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type.isUserActivity }

    /**
     * The newest event for that area strictly before [beforeMillis], or null when
     * the area has never been touched.
     *
     * A wall clock maximum rather than the last event in the total order, because
     * the question this answers is "how long ago", which is a calendar question.
     */
    fun lastEventForArea(areaId: String, beforeMillis: Long): Long? {
        var newest: Long? = null
        for (event in log) {
            if (event.wallClock >= beforeMillis) continue
            if (areaIdOf(event) != areaId) continue
            if (newest == null || event.wallClock > newest) newest = event.wallClock
        }
        return newest
    }

    /**
     * The area an event belongs to, or null for the six types that have none.
     *
     * Eighteen of the twenty four resolve exactly. ITEM_EDITED carries only an item
     * id and resolves through that item's ITEM_ADDED, which is exact rather than a
     * heuristic because an item's area is assigned once when it is added and no
     * event type moves an item between areas. The two terminal focus types resolve
     * through their own session's FOCUS_STARTED, which can genuinely be missing on
     * an imported or merged log, so the answer is nullable rather than a crash.
     *
     * The six that cannot resolve are excluded rather than guessed. PULSE_GENERATED,
     * PULSE_ANSWERED and REPORT_GENERATED carry a `factSnapshot` whose keys no
     * document specifies, so nothing may be parsed out of it. PLAN_OFFERED carries a
     * `subjectId` but not the `SubjectKind` that would say whether it names an area
     * or an item; testing the id against the known area ids would work in practice
     * and is a heuristic no document authorizes. PLAN_ACCEPTED carries a plan id
     * with the same missing kind behind it, and a setting is global by definition.
     */
    fun areaIdOf(event: ClarityEvent): String? = when (val payload = event.payload) {
        is AreaCreated -> payload.areaId
        is AreaRenamed -> payload.areaId
        is AreaRecolored -> payload.areaId
        is AreaReordered -> payload.areaId
        is AreaArchived -> payload.areaId
        is AreaUnarchived -> payload.areaId
        is AreaDeleted -> payload.areaId

        is ItemAdded -> payload.areaId
        is ItemEdited -> areaIdOfItem(payload.itemId)
        is ItemQueued -> payload.areaId
        is ItemPromoted -> payload.areaId
        is ItemCompleted -> payload.areaId
        is ItemReopened -> payload.areaId
        is ItemReordered -> payload.areaId
        is ItemDeleted -> payload.areaId

        is FocusStarted -> payload.areaId
        is FocusCompleted -> areaIdOfFocusSession(payload.sessionId)
        is FocusAbandoned -> areaIdOfFocusSession(payload.sessionId)

        is PulseGenerated, is PulseAnswered, is ReportGenerated,
        is PlanOffered, is PlanAccepted, is SettingChanged,
        -> null
    }

    /**
     * Areas created, not archived and not tombstoned strictly before the instant.
     *
     * CLARITY_LOGIC_ENGINE.md 1.1 prohibition 3 forbids ever referencing a deleted
     * entity, and archived areas are outside `AreaFacts` entirely, so a rollup that
     * counted either would print a number about things the person cannot see.
     */
    fun liveAreaIdsAt(atMillis: Long): Set<String> =
        stateBefore(atMillis).liveAreas.map { it.id }.toSet()

    /** The wall clock of AREA_CREATED, or null when no such area was ever created. */
    fun areaCreatedAt(areaId: String): Long? = areaCreatedEvent(areaId)?.wallClock

    /**
     * The newest AREA_ARCHIVED not superseded by a later AREA_UNARCHIVED, or null.
     *
     * Archive and unarchive after a delete are ignored, matching the reducer, which
     * refuses both on a tombstoned area and records a diagnostic instead.
     */
    fun areaArchivedAt(areaId: String, asOfMillis: Long): Long? {
        var archivedAt: Long? = null
        var deleted = false
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock >= asOfMillis) continue
            when (event.payload) {
                is AreaArchived -> if (!deleted) archivedAt = event.wallClock
                is AreaUnarchived -> if (!deleted) archivedAt = null
                is AreaDeleted -> deleted = true
                else -> Unit
            }
        }
        return archivedAt
    }

    /** The wall clock of AREA_DELETED. A tombstone, never a row delete. */
    fun areaDeletedAt(areaId: String, asOfMillis: Long): Long? =
        stateBefore(asOfMillis).areas[areaId]?.deletedAt

    // Snapshots as of an instant ----------------------------------------------

    /**
     * The area's name at [atMillis], not its name now.
     *
     * This and the two resolvers below are the reason renaming an area does not
     * rewrite what an older Trail entry says. They fold the log; they never consult
     * a live entity, which would return the current name for a renamed area and
     * nothing at all for a deleted one.
     *
     * **Inclusive of [atMillis], unlike every other bound in this class.** A
     * snapshot resolver is asked "what was this called when that happened", and one
     * commit stamps every event it writes with one clock reading, so an exclusive
     * bound would make an area unable to resolve its own name at the instant it was
     * created. Stated on all three, so the exception is visible wherever it is used.
     */
    fun areaNameAsOf(areaId: String, atMillis: Long): String? {
        var name: String? = null
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is AreaCreated -> name = payload.name
                is AreaRenamed -> name = payload.newName
                else -> Unit
            }
        }
        return name
    }

    /** The area's color hex at [atMillis]. Inclusive of the instant, see [areaNameAsOf]. */
    fun areaColorHexAsOf(areaId: String, atMillis: Long): String? {
        var hex: String? = null
        for (event in byEntity[areaId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is AreaCreated -> hex = payload.colorHex
                is AreaRecolored -> hex = payload.newHex
                else -> Unit
            }
        }
        return hex
    }

    /** The item's title at [atMillis]. Inclusive of the instant, see [areaNameAsOf]. */
    fun itemTitleAsOf(itemId: String, atMillis: Long): String? {
        var title: String? = null
        for (event in byEntity[itemId].orEmpty()) {
            if (event.wallClock > atMillis) continue
            when (val payload = event.payload) {
                is ItemAdded -> title = payload.title
                is ItemEdited -> title = payload.newTitle
                else -> Unit
            }
        }
        return title
    }

    /**
     * The area an item belongs to, fixed for the item's whole life.
     *
     * `ItemAdded.areaId` is the only assignment the reducer ever makes to an item's
     * area and no event type moves an item between areas, so this is exact rather
     * than an approximation of a current value.
     */
    fun areaIdOfItem(itemId: String): String? = itemAddedPayload(itemId)?.areaId

    /** The area a focus session ran in, from its own FOCUS_STARTED. */
    fun areaIdOfFocusSession(sessionId: String): String? = focusStartPayload(sessionId)?.areaId

    /** The item a focus session ran on, from its own FOCUS_STARTED. */
    fun itemIdOfFocusSession(sessionId: String): String? = focusStartPayload(sessionId)?.itemId

    // Days --------------------------------------------------------------------

    /**
     * User activity events bucketed by local calendar day.
     *
     * Bucketed by converting each instant to a date in [zone], never by dividing
     * milliseconds by the length of a day. The division is wrong across every
     * daylight saving boundary and in every zone whose offset is not a whole number
     * of hours, and it is wrong quietly.
     */
    fun eventsPerDay(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            if (!event.type.isUserActivity) continue
            val key = dateKeyOf(event.wallClock)
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    /**
     * Days with at least one user activity event. Not days with a completion.
     *
     * The two are different quantities and must never be folded together: the Report
     * needs "{n} of seven days had activity" and the Pulse separately needs
     * "{dayCount} without a completion". A single number cannot serve both.
     */
    fun activeDays(startMillis: Long, endMillis: Long): Int =
        eventsPerDay(startMillis, endMillis).values.count { it > 0 }

    /** The active days themselves, for the fourteen dot grid. */
    fun activeDayKeys(startMillis: Long, endMillis: Long): Set<String> =
        eventsPerDay(startMillis, endMillis).filterValues { it > 0 }.keys

    // Focus -------------------------------------------------------------------

    /**
     * Seconds of focus in the window, over completed and abandoned sessions alike.
     *
     * Abandoned sessions count. CLARITY_LOGIC_ENGINE.md 3.1 gives three separate
     * session counts and one undivided total, and it is not named `completedSeconds`.
     * The approved Report line "The sessions that finished averaged {minutes}
     * minutes" only means something if the ordinary minutes figure is unrestricted.
     * And MASTER_BUILD_PROMPT 10 treats abandonment neutrally everywhere, which
     * deleting those minutes from the total would quietly stop being true.
     *
     * A session is attributed entirely to the instant it started, so one that
     * crosses midnight belongs to the day it began and lands on exactly one day of a
     * heat strip. Attributing to the terminal event instead would split a session
     * across two windows and make the two halves of "{sessions} started, {n}
     * completed" disagree with each other.
     */
    fun focusSecondsTotal(startMillis: Long, endMillis: Long): Long {
        var total = 0L
        for (event in log) {
            val terminal = terminalFocusOf(event) ?: continue
            if (focusTerminalEvent(terminal.first)?.id != event.id) continue
            val start = focusStartEvent(terminal.first) ?: continue
            if (start.wallClock < startMillis || start.wallClock >= endMillis) continue
            total += terminal.second.toLong()
        }
        return total
    }

    /** [focusSecondsTotal] in whole minutes, rounded down. Rounding happens once, here. */
    fun focusMinutes(startMillis: Long, endMillis: Long): Int =
        (focusSecondsTotal(startMillis, endMillis) / 60L).toInt()

    /**
     * Session outcomes for the sessions that started in the window.
     *
     * A session with no terminal event is counted as unresolved, never as abandoned.
     * A killed process leaves exactly that, and inferring abandonment from
     * `started - completed` would put a number behind language that is careful never
     * to blame.
     */
    fun focusSessionCounts(startMillis: Long, endMillis: Long): FocusCounts {
        var started = 0
        var completed = 0
        var abandoned = 0
        var unresolved = 0
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload !is FocusStarted) continue
            // A second start for a known session changes nothing in the reducer, so
            // it must not add a session here either.
            if (focusStartEvent(payload.sessionId)?.id != event.id) continue
            started++
            when (focusTerminalEvent(payload.sessionId)?.payload) {
                is FocusCompleted -> completed++
                is FocusAbandoned -> abandoned++
                else -> unresolved++
            }
        }
        return FocusCounts(
            started = started,
            completed = completed,
            abandoned = abandoned,
            unresolved = unresolved,
        )
    }

    // Queue and intake --------------------------------------------------------

    /** The active item in each live area at the instant. At most one per area. */
    fun activeItemPerAreaAt(atMillis: Long): Map<String, String> {
        val state = stateBefore(atMillis)
        return state.liveAreas.mapNotNull { area ->
            state.activeItemIn(area.id)?.let { area.id to it.id }
        }.toMap()
    }

    /**
     * Queued items per area at the instant. The active one is not queued.
     *
     * ACTIVE, COMPLETED and DELETED are all excluded. The corpus settles it beyond
     * argument: "{areaName} is holding {n} items behind its active one", and an area
     * whose queue reads zero is still allowed to say "It still has an active item".
     *
     * Archived areas are excluded by default, because CLARITY_LOGIC_ENGINE.md 3.1
     * leaves them out of `AreaFacts` entirely and a rollup including them would count
     * things the person cannot see. Tombstoned areas are excluded unconditionally,
     * with no parameter to turn that off.
     *
     * Every qualifying area appears, including one whose queue is empty, because
     * "an area went from 3 or more queued to 0" is a trigger that needs the zero.
     */
    fun queueSizeByAreaAt(atMillis: Long, includeArchived: Boolean = false): Map<String, Int> {
        val state = stateBefore(atMillis)
        val areas = if (includeArchived) {
            state.areas.values.filter { it.deletedAt == null }
        } else {
            state.liveAreas
        }
        return areas.associate { it.id to state.queueIn(it.id).size }.toSortedMap()
    }

    /** The whole queue at the instant, summed over [queueSizeByAreaAt]. */
    fun queueSizeAt(atMillis: Long, includeArchived: Boolean = false): Int =
        queueSizeByAreaAt(atMillis, includeArchived).values.sum()

    /**
     * ITEM_ADDED events in the window.
     *
     * There is no signed net flow function here and there never will be.
     * CLARITY_LOGIC_ENGINE.md 3.1 declares `netFlow` as completions minus additions
     * while the Pulse accumulation family escalates on additions minus completions.
     * Two opposite conventions, so each caller chooses its own sign at the point of
     * use and neither can leak into the other.
     */
    fun additionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_ADDED }

    /** Additions in the window keyed by the area the payload names. */
    fun additionsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload is ItemAdded) counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
        }
        return counts.toSortedMap()
    }

    // Swaps and promotions ----------------------------------------------------

    /**
     * Every ITEM_PROMOTED in the window, including the automatic promotion that
     * follows a completion and the first item added to an empty area.
     */
    fun promotionsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count { it.type == ClarityEventType.ITEM_PROMOTED }

    /**
     * Promotions that displaced something, which is what a swap is.
     *
     * A swap is an ITEM_PROMOTED carrying a non null `demotedItemId`. That fact is
     * stated in no prose anywhere in the specification and lives only in the shape of
     * the payload, which is why it is encoded once here rather than rediscovered by
     * every later phase that needs it.
     */
    fun swapsBetween(startMillis: Long, endMillis: Long): Int =
        eventsIn(startMillis, endMillis).count {
            val payload = it.payload
            payload is ItemPromoted && payload.demotedItemId != null
        }

    /** Swaps in the window keyed by the area the payload names. */
    fun swapsPerArea(startMillis: Long, endMillis: Long): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (event in eventsIn(startMillis, endMillis)) {
            val payload = event.payload
            if (payload is ItemPromoted && payload.demotedItemId != null) {
                counts[payload.areaId] = (counts[payload.areaId] ?: 0) + 1
            }
        }
        return counts.toSortedMap()
    }

    // Item age. Two quantities, two names, deliberately ------------------------

    /**
     * When the item last became active, or null when it is not active at the instant.
     *
     * Measured from the most recent ITEM_PROMOTED, not from ITEM_ADDED, and cleared
     * by a later ITEM_QUEUED, by being demoted under someone else's promotion, and by
     * completion. CLARITY_LOGIC_ENGINE.md 7.3 names a promotion resetting the age as
     * the behavior escalation monotonicity exists to absorb downstream, which settles
     * that it is the intended reading rather than a defect: "a new active item starts
     * at stage 1".
     */
    fun activeSinceForItem(itemId: String, asOfMillis: Long): Long? =
        stateBefore(asOfMillis).items[itemId]?.activeSince

    /**
     * Whole local days the item has been active, or null when it is not.
     *
     * **No fallback to when the item was added.** `ClarityRepository` has one,
     * unreachable there because adding an item into an empty area writes the add and
     * the promotion in a single commit. Copying it here would silently turn a never
     * promoted item into an add based age, which is the wrong number with no visible
     * symptom. A null answer is the honest one.
     */
    fun daysActiveForItem(itemId: String, asOfMillis: Long): Int? {
        val since = activeSinceForItem(itemId, asOfMillis) ?: return null
        return wholeDaysBetween(since, asOfMillis)
    }

    /** The wall clock of ITEM_ADDED. */
    fun itemAddedAt(itemId: String): Long? = itemAddedEvent(itemId)?.wallClock

    /**
     * Whole local days since the item was added.
     *
     * How long it has been waiting, which is a different quantity from how long it
     * has been active. Three approved Report lines need this one and not the other,
     * and one function serving both meanings would be silently wrong in one of them.
     */
    fun daysSinceItemAdded(itemId: String, asOfMillis: Long): Int? {
        val added = itemAddedAt(itemId) ?: return null
        return wholeDaysBetween(added, asOfMillis)
    }

    // Rows --------------------------------------------------------------------

    /**
     * The snapshot values a row needs that its own payload does not carry.
     *
     * Resolved at the event's own instant, so a later rename or recolor cannot reach
     * back into it.
     */
    fun contextFor(event: ClarityEvent): TrailRowContext {
        val areaId = areaIdOf(event)
        val itemId = subjectItemIdOf(event)
        return TrailRowContext(
            areaId = areaId,
            areaColorHex = areaId?.let { areaColorHexAsOf(it, event.wallClock) },
            itemTitle = itemId?.let { itemTitleAsOf(it, event.wallClock) },
            areaName = areaId?.let { areaNameAsOf(it, event.wallClock) },
        )
    }

    /**
     * One page of events as rows, in display order, with the timestamp clusters
     * already decided.
     *
     * The screen calls this and gets everything it needs. It is never handed a
     * `ClarityState`, which is what makes the live entity shortcut unavailable to it
     * rather than merely discouraged.
     */
    fun rows(events: List<ClarityEvent>): List<TrailRow> =
        trailRows(events, zone, this::contextFor)

    // Internals ---------------------------------------------------------------

    /** The half open window filter. Every windowed function starts here. */
    private fun eventsIn(startMillis: Long, endMillis: Long): List<ClarityEvent> =
        log.filter { it.wallClock >= startMillis && it.wallClock < endMillis }

    /**
     * The state as it stood strictly before [atMillis].
     *
     * Folded through `ClarityReducer` rather than through a second implementation of
     * the same rules. A queue length or an active item computed by a private copy of
     * the promotion, demotion and cascade logic would agree with the reducer on the
     * cases anyone writes a test for and diverge on the conflict path, which is
     * exactly the path that only appears after a merge. There is one definition of
     * what the log means and this reads it.
     *
     * Note the filter rather than a `takeWhile`: `log` is in total order, and a later
     * event in that order is allowed to carry an earlier wall clock, so stopping at
     * the first event past the bound would drop real history.
     */
    private fun stateBefore(atMillis: Long): ClarityState {
        memo?.let { (at, cached) -> if (at == atMillis) return cached }
        var state = ClarityState.EMPTY
        for (event in log) {
            if (event.wallClock < atMillis) state = ClarityReducer.apply(state, event)
        }
        memo = atMillis to state
        return state
    }

    private fun areaCreatedEvent(areaId: String): ClarityEvent? =
        byEntity[areaId]?.firstOrNull { it.payload is AreaCreated }

    private fun itemAddedEvent(itemId: String): ClarityEvent? =
        byEntity[itemId]?.firstOrNull { it.payload is ItemAdded }

    private fun itemAddedPayload(itemId: String): ItemAdded? =
        itemAddedEvent(itemId)?.payload as? ItemAdded

    private fun focusStartEvent(sessionId: String): ClarityEvent? =
        byEntity[sessionId]?.firstOrNull { it.payload is FocusStarted }

    private fun focusStartPayload(sessionId: String): FocusStarted? =
        focusStartEvent(sessionId)?.payload as? FocusStarted

    /** The first terminal event for a session. A second one changes nothing. */
    private fun focusTerminalEvent(sessionId: String): ClarityEvent? =
        byEntity[sessionId]?.firstOrNull {
            it.payload is FocusCompleted || it.payload is FocusAbandoned
        }

    /** The session and the seconds of a terminal focus event, or null for anything else. */
    private fun terminalFocusOf(event: ClarityEvent): Pair<String, Int>? =
        when (val payload = event.payload) {
            is FocusCompleted -> payload.sessionId to payload.actualSeconds
            is FocusAbandoned -> payload.sessionId to payload.actualSeconds
            else -> null
        }

    /** The item a row is about, for the types whose payload carries no title. */
    private fun subjectItemIdOf(event: ClarityEvent): String? = when (val payload = event.payload) {
        is ItemAdded -> payload.itemId
        is ItemEdited -> payload.itemId
        is ItemQueued -> payload.itemId
        is ItemPromoted -> payload.itemId
        is ItemCompleted -> payload.itemId
        is ItemReopened -> payload.itemId
        is ItemReordered -> payload.itemId
        is ItemDeleted -> payload.itemId
        is FocusStarted -> payload.itemId
        is FocusCompleted -> itemIdOfFocusSession(payload.sessionId)
        is FocusAbandoned -> itemIdOfFocusSession(payload.sessionId)
        else -> null
    }

    private fun localDateOf(atMillis: Long): LocalDate =
        Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    /**
     * The `yyyy-MM-dd` key every daily thing is stored under, matching
     * `ClarityClock.dateKey`.
     *
     * Formatted with the ISO formatter rather than a pattern, because a pattern
     * resolves its digits against the default locale, and a default locale is
     * ambient environment of exactly the kind this package refuses to read.
     */
    private fun dateKeyOf(atMillis: Long): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(localDateOf(atMillis))

    /**
     * Whole local days between two instants, counted by calendar date rather than by
     * dividing milliseconds, so a daylight saving shift does not lose or gain a day.
     */
    private fun wholeDaysBetween(fromMillis: Long, toMillis: Long): Int =
        ChronoUnit.DAYS.between(localDateOf(fromMillis), localDateOf(toMillis))
            .toInt()
            .coerceAtLeast(0)
}
