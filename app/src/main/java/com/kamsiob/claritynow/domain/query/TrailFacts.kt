package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
import com.kamsiob.claritynow.data.event.FocusStarted
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemCompleted
import com.kamsiob.claritynow.data.event.ItemDeleted
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemPromoted
import com.kamsiob.claritynow.data.event.ItemQueued
import com.kamsiob.claritynow.data.event.ItemReopened
import com.kamsiob.claritynow.data.event.ItemReordered
import java.time.Instant
import java.time.ZoneId

/**
 * One completion, read straight from the payload snapshots. MASTER_BUILD_PROMPT 8.1.
 *
 * Every field is a value the writing device stored at the moment it happened. None
 * of them is a lookup against a live entity, because an area renamed in July must
 * not rewrite what a completion in March says, and a deleted area must still name
 * itself. [activeDurationDays] in particular is the payload's own number: the
 * device that wrote it had already shown that figure to the person, and recomputing
 * it here against a second device's clock would contradict what they were told.
 */
data class CompletedRecord(
    val itemId: String,
    val titleSnapshot: String,
    val areaId: String,
    val areaNameSnapshot: String,
    /** The payload's own value. Never recomputed. */
    val activeDurationDays: Int,
    val completedAt: Long,
)

/**
 * Focus session outcomes in a window, attributed to the FOCUS_STARTED instant.
 *
 * [unresolved] exists so that no rule anywhere ever infers abandonment by
 * subtraction. A killed process leaves a started session with no terminal event, so
 * `started != completed + abandoned` is a legal state rather than a bug, and
 * `ClarityReducer.focusEnded` already records the mirror case as a diagnostic.
 * MASTER_BUILD_PROMPT 10 treats abandonment neutrally everywhere; guessing that an
 * unresolved session was abandoned would put a number behind that language that the
 * log does not support.
 */
data class FocusCounts(
    val started: Int,
    val completed: Int,
    val abandoned: Int,
    val unresolved: Int,
)

/**
 * A half open wall clock window, `[fromMillis, toMillis)`.
 *
 * Every bound in this package is exclusive on the upper end, so two adjacent
 * windows share no event and a day boundary belongs to exactly one of them.
 */
data class TrailWindow(val fromMillis: Long, val toMillis: Long)

/**
 * Events a person caused. Excludes the three the engine writes on its own.
 *
 * PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED arrive with no user gesture.
 * If they counted, someone who opened the app daily and touched nothing would read
 * as active on every day of the fortnight, and CLARITY_LOGIC_ENGINE.md 6.1's
 * `quietDay` and `quietWeek` families could never fire at all.
 *
 * PULSE_ANSWERED, PLAN_ACCEPTED and SETTING_CHANGED stay in: the person did those.
 * A single setting change on its own still leaves a day below `quietDay`'s bar of
 * two events, so including it costs nothing and excluding it would be an
 * interpretation this predicate is not entitled to make.
 *
 * There is deliberately one predicate here, not two. A narrower "what counts as
 * movement" test, which would also drop ITEM_REORDERED and AREA_RECOLORED, is a
 * Phase 5 question about Pulse language, and inventing it early would leave two
 * meanings of the word active in the codebase with nothing to tell them apart.
 */
val ClarityEventType.isUserActivity: Boolean
    get() = !isEngineAuthored

/** The three the engine writes on its own. The complement of [isUserActivity]. */
val ClarityEventType.isEngineAuthored: Boolean
    get() = this == ClarityEventType.PULSE_GENERATED ||
        this == ClarityEventType.REPORT_GENERATED ||
        this == ClarityEventType.PLAN_OFFERED

/**
 * The Trail's pagination arithmetic. MASTER_BUILD_PROMPT 9.
 *
 * Pure and separate from the screen so the awkward cases are unit testable: a page
 * that spans a spring forward, a page that spans a fall back, and a page in a zone
 * whose offset is not a whole number of hours. All three are wrong under the
 * obvious implementation, which divides milliseconds by 86400000.
 */
object TrailPaging {

    /** MASTER_BUILD_PROMPT 9: "Pagination loads 14 days per page". */
    const val PAGE_DAYS = 14

    /**
     * The [days] long local day window whose last day contains [anchorMillis].
     *
     * The lower bound is local midnight of the first day, computed by stepping back
     * calendar dates rather than by subtracting a fixed number of milliseconds, so a
     * daylight saving shift inside the window does not lose or gain a day. The upper
     * bound is one millisecond past the anchor, because every window in this package
     * is exclusive on the upper end and the anchor event itself has to be inside its
     * own page.
     */
    fun pageEndingAt(anchorMillis: Long, zone: ZoneId, days: Int = PAGE_DAYS): TrailWindow {
        require(days >= 1) { "a page covers at least one day, not $days" }
        val lastDay = Instant.ofEpochMilli(anchorMillis).atZone(zone).toLocalDate()
        val firstDay = lastDay.minusDays((days - 1).toLong())
        return TrailWindow(
            fromMillis = firstDay.atStartOfDay(zone).toInstant().toEpochMilli(),
            toMillis = anchorMillis + 1,
        )
    }
}

/**
 * Every item id a page of Trail events needs a title for.
 *
 * This exists because one family does not follow the rule the rest of the log
 * follows. Every `ITEM_*` event is keyed by the item it is about, so the ids on a
 * page are enough to fetch the naming history for all of them. The three focus types
 * are keyed by their session instead, and the item they were run on is named only
 * inside the `FOCUS_STARTED` payload, which is itself only reachable by that session
 * id. So a page's own entity ids are not the set of items it needs to name, and a
 * loader that assumes they are renders "Finished 25 minutes of focus on" with nothing
 * after the preposition for any session whose item was added before the page began.
 *
 * Stated here as a pure rule rather than inline in a loader, because the loader that
 * got this wrong could not be tested and this can.
 *
 * @param page the events being displayed.
 * @param focusStarts the `FOCUS_STARTED` events resolved for that page's sessions.
 */
fun itemIdsNeededBy(page: List<ClarityEvent>, focusStarts: List<ClarityEvent>): Set<String> {
    val direct = page.mapNotNull { event ->
        when (val payload = event.payload) {
            is ItemAdded -> payload.itemId
            is ItemEdited -> payload.itemId
            is ItemQueued -> payload.itemId
            is ItemPromoted -> payload.itemId
            is ItemCompleted -> payload.itemId
            is ItemReopened -> payload.itemId
            is ItemReordered -> payload.itemId
            is ItemDeleted -> payload.itemId
            is FocusStarted -> payload.itemId
            else -> null
        }
    }
    val throughSessions = focusStarts.mapNotNull { (it.payload as? FocusStarted)?.itemId }
    return (direct + throughSessions).toSet()
}
