package com.kamsiob.claritynow.domain.query

import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.ClarityEventType
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
 * [unresolved] exists so that no rule anywhere ever infers an early ending by
 * subtraction. A killed process leaves a started session with no terminal event, so
 * `started != completed + endedEarly` is a legal state rather than a bug, and
 * `ClarityReducer.focusEnded` already records the mirror case as a diagnostic.
 * MASTER_BUILD_PROMPT 10 treats an early ending neutrally everywhere; guessing that
 * an unresolved session ended early would put a number behind that language that
 * the log does not support.
 *
 * [endedEarly] was `abandoned` until the Addendum 01 schema commit renamed the
 * event, and this field moved with it. The comment above had already made the
 * argument for the new name on its own grounds, before the addendum arrived: the
 * log does not reliably know that a session was abandoned, so a counter that says
 * it does is the wrong name for the number regardless of what a person can see.
 * DECISIONS.md C6.
 */
data class FocusCounts(
    val started: Int,
    val completed: Int,
    val endedEarly: Int,
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
 * Events a person caused. Excludes the four the app writes without a user act.
 *
 * PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED arrive with no user gesture.
 * If they counted, someone who opened the app daily and touched nothing would read
 * as active on every day of the fortnight, and CLARITY_LOGIC_ENGINE.md 6.1's
 * `quietDay` and `quietWeek` families could never fire at all.
 *
 * **APP_OPENED is excluded for the same reason and it is the sharp case**, because
 * for it that sentence is not a hypothetical. It is written on the first foreground
 * of each local day, so counting it would literally mark a day active for someone
 * who opened the app and did nothing. Three numbers would go wrong and each would
 * still look plausible: CORPUS_3's `mo.steady`, active on 9 or more of the last 14
 * days, would tell that person they had been steady; CORPUS_2's `ob.day.l03`,
 * `{n} of seven days had activity`, would become a count of app opens presented as
 * a count of activity; and `quietDay`, which fires below two events in a window,
 * would be close to unreachable. Worse than any of those, APP_OPENED exists only to
 * detect an absence for Addendum 01 4d, and a returning person must never be
 * greeted by a measurement of their absence. DECISIONS.md C7.
 *
 * PULSE_ANSWERED, PLAN_ACCEPTED and SETTING_CHANGED stay in: the person did those.
 * A single setting change on its own still leaves a day below `quietDay`'s bar of
 * two events, so including it costs nothing and excluding it would be an
 * interpretation this predicate is not entitled to make. ITEM_FILED, ITEM_ESTIMATED
 * and FOCUS_EXTENDED stay in for the same reason: filing something, revising an
 * estimate and adding ten minutes to a running session are all gestures.
 *
 * **This predicate is a negation, so a new event type counts as activity by
 * default.** That is safe only while somebody is looking at it, which is why every
 * type added from here on is classified against it deliberately, in the same commit
 * that adds it, with the classification argued in a comment. APP_OPENED was one
 * line away from shipping the other way.
 *
 * There is deliberately one predicate here, not two. A narrower "what counts as
 * movement" test, which would also drop ITEM_REORDERED and AREA_RECOLORED, is a
 * Phase 5 question about Pulse language, and inventing it early would leave two
 * meanings of the word active in the codebase with nothing to tell them apart.
 */
val ClarityEventType.isUserActivity: Boolean
    get() = !isWrittenWithoutUserAct

/**
 * The four the app writes with no gesture behind them. The complement of
 * [isUserActivity].
 *
 * Named for what is true of all four rather than for who wrote them. Three are
 * authored by the engine; APP_OPENED is written by the app shell on launch and is
 * not engine authored at all. Folding it under the old name to get the right answer
 * would have recorded a false reason, and the reason is the part a later session
 * actually reads.
 */
val ClarityEventType.isWrittenWithoutUserAct: Boolean
    get() = this == ClarityEventType.PULSE_GENERATED ||
        this == ClarityEventType.REPORT_GENERATED ||
        this == ClarityEventType.PLAN_OFFERED ||
        this == ClarityEventType.APP_OPENED

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
 * page are enough to fetch the naming history for all of them. The four focus types
 * are keyed by their session instead, and the item they were run on is named only
 * inside the `FOCUS_STARTED` payload, which is itself only reachable by that session
 * id. So a page's own entity ids are not the set of items it needs to name, and a
 * loader that assumes they are renders "Finished 25 minutes of focus on" with nothing
 * after the preposition for any session whose item was added before the page began.
 *
 * FOCUS_EXTENDED joined that family and needs nothing new here: it is keyed by its
 * session like the two terminal types, so the same second round resolves it.
 * ITEM_FILED and ITEM_ESTIMATED are keyed by their item like every other `ITEM_*`
 * type, and are listed below because neither carries a title snapshot of its own,
 * which is precisely the case this set exists to cover.
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
            is ItemFiled -> payload.itemId
            is ItemEdited -> payload.itemId
            is ItemEstimated -> payload.itemId
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
